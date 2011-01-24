/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2009 SonarSource SA
 * mailto:contact AT sonarsource DOT com
 *
 * Sonar is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.batch;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.model.Reporting;
import org.apache.maven.project.MavenProject;
import org.slf4j.LoggerFactory;
import org.sonar.api.CoreProperties;
import org.sonar.api.database.DatabaseSession;
import org.sonar.api.resources.Project;
import org.sonar.api.utils.SonarException;
import org.sonar.batch.bootstrapper.ProjectDefinition;
import org.sonar.batch.bootstrapper.Reactor;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ProjectTree {

  private List<Project> projects;
  private List<MavenProject> poms;
  private MavenProjectBuilder projectBuilder;

  public ProjectTree(MavenReactor mavenReactor, DatabaseSession databaseSession) {
    this.poms = mavenReactor.getSortedProjects();
    this.projectBuilder = new MavenProjectBuilder(databaseSession);
  }

  /**
   * Hack for non-Maven environments.
   */
  public ProjectTree(Reactor sonarReactor, DatabaseSession databaseSession) {
    this(createMavenReactor(sonarReactor), databaseSession);
  }

  private static MavenReactor createMavenReactor(Reactor sonarReactor) {
    List<ProjectDefinition> sonarProjects = sonarReactor.getSortedProjects();
    List<MavenProject> mavenProjects = Lists.newArrayList();
    for (ProjectDefinition project : sonarProjects) {
      mavenProjects.add(createInMemoryPom(project));
    }
    return new MavenReactor(mavenProjects);
  }

  static MavenProject createInMemoryPom(final ProjectDefinition project) {
    MavenProject pom = new MavenProject() {
      /**
       * This allows to specify base directory without specifying location of a pom.xml
       */
      public File getBasedir() {
        return project.getBaseDir();
      };
    };

    Properties properties = project.getProperties();

    String key = getPropertyOrDie(properties, CoreProperties.PROJECT_KEY_PROPERTY);
    String[] keys = key.split(":");
    pom.setGroupId(keys[0]);
    pom.setArtifactId(keys[1]);
    pom.setVersion(getPropertyOrDie(properties, CoreProperties.PROJECT_VERSION_PROPERTY));

    pom.getModel().setProperties(properties);

    pom.setArtifacts(Collections.EMPTY_SET);

    // Configure fake directories
    String buildDirectory = getPropertyOrDie(properties, "project.build.directory");
    pom.getBuild().setDirectory(buildDirectory);
    pom.getBuild().setOutputDirectory(buildDirectory + "/classes"); // TODO hard-coded value
    Reporting reporting = new Reporting();
    reporting.setOutputDirectory(buildDirectory + "/site"); // TODO hard-coded value
    pom.setReporting(reporting);

    // Configure source directories
    for (String dir : project.getSourceDirs()) {
      pom.addCompileSourceRoot(dir);
    }

    // Configure test directories
    for (String dir : project.getTestDirs()) {
      pom.addTestCompileSourceRoot(dir);
    }

    return pom;
  }

  private static String getPropertyOrDie(Properties properties, String key) {
    String value = properties.getProperty(key);
    if (StringUtils.isBlank(value)) {
      throw new SonarException("Property '" + key + "' must be specified");
    }
    return value;
  }

  /**
   * for unit tests
   */
  protected ProjectTree(MavenProjectBuilder projectBuilder, List<MavenProject> poms) {
    this.projectBuilder = projectBuilder;
    this.poms = poms;
  }

  /**
   * for unit tests
   */
  protected ProjectTree(List<Project> projects) {
    this.projects = new ArrayList<Project>(projects);
  }

  public void start() throws IOException {
    projects = Lists.newArrayList();
    Map<String, Project> paths = Maps.newHashMap(); // projects by canonical path

    for (MavenProject pom : poms) {
      Project project = projectBuilder.create(pom);
      projects.add(project);
      paths.put(pom.getBasedir().getCanonicalPath(), project);
    }

    for (Map.Entry<String, Project> entry : paths.entrySet()) {
      Project project = entry.getValue();
      MavenProject pom = project.getPom();
      for (Object moduleId : pom.getModules()) {
        File modulePath = new File(pom.getBasedir(), (String) moduleId);
        Project module = paths.get(modulePath.getCanonicalPath());
        if (module != null) {
          module.setParent(project);
        }
      }
    }

    configureProjects();
    applyModuleExclusions();
  }

  private void configureProjects() {
    for (Project project : projects) {
      projectBuilder.configure(project);
    }
  }

  void applyModuleExclusions() {
    for (Project project : projects) {
      String[] excludedArtifactIds = project.getConfiguration().getStringArray("sonar.skippedModules");
      String[] includedArtifactIds = project.getConfiguration().getStringArray("sonar.includedModules");

      Set<String> includedModulesIdSet = new HashSet<String>();
      Set<String> excludedModulesIdSet = new HashSet<String>();

      if (includedArtifactIds != null) {
        includedModulesIdSet.addAll(Arrays.asList(includedArtifactIds));
      }

      if (excludedArtifactIds != null) {
        excludedModulesIdSet.addAll(Arrays.asList(excludedArtifactIds));
        includedModulesIdSet.removeAll(excludedModulesIdSet);
      }

      if (!includedModulesIdSet.isEmpty()) {
        for (Project currentProject : projects) {
          if (!includedModulesIdSet.contains(currentProject.getPom().getArtifactId())) {
            exclude(currentProject);
          }
        }
      } else {
        for (String excludedArtifactId : excludedModulesIdSet) {
          Project excludedProject = getProjectByArtifactId(excludedArtifactId);
          exclude(excludedProject);
        }
      }
    }

    for (Iterator<Project> it = projects.iterator(); it.hasNext();) {
      Project project = it.next();
      if (project.isExcluded()) {
        LoggerFactory.getLogger(getClass()).info("Module {} is excluded from analysis", project.getName());
        project.removeFromParent();
        it.remove();
      }
    }
  }

  private void exclude(Project project) {
    if (project != null) {
      project.setExcluded(true);
      for (Project module : project.getModules()) {
        exclude(module);
      }
    }
  }

  public List<Project> getProjects() {
    return projects;
  }

  public Project getProjectByArtifactId(String artifactId) {
    for (Project project : projects) {
      if (project.getPom().getArtifactId().equals(artifactId)) {
        return project;
      }
    }
    return null;
  }

  public Project getRootProject() {
    for (Project project : projects) {
      if (project.getParent() == null) {
        return project;
      }
    }
    throw new IllegalStateException("Can not find the root project from the list of Maven modules");
  }
}