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
package org.sonar.api.resources;

import org.sonar.api.BatchExtension;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

/**
 * @since 1.10
 */
public interface ProjectFileSystem extends BatchExtension {
  /**
   * Source encoding.
   * Never null, it returns the default platform charset if it is not defined in project.
   * (Maven property 'project.build.sourceEncoding').
   */
  Charset getSourceCharset();

  /**
   * Project root directory.
   */
  File getBasedir();

  /**
   * Build directory. It's "${basedir}/target" by default in Maven projects.
   */
  File getBuildDir();

  /**
   * Directory where classes are placed. It's "${basedir}/target/classes" by default in Maven projects.
   */
  File getBuildOutputDir();

  /**
   * The list of directories for sources
   */
  List<File> getSourceDirs();

  /**
   * Adds a source directory
   * 
   * @return the current object
   */
  ProjectFileSystem addSourceDir(File dir);

  /**
   * The list of directories for tests
   */
  List<File> getTestDirs();

  /**
   * Adds a test directory
   * 
   * @return the current object
   */
  ProjectFileSystem addTestDir(File dir);

  /**
   * @return the directory where reporting is placed. Default is target/sites
   */
  File getReportOutputDir();

  /**
   * @return the Sonar working directory. Default is "target/sonar"
   */
  File getSonarWorkingDirectory();

  /**
   * Get file from path. It can be absolute or relative to project basedir. For example resolvePath("pom.xml") or
   * resolvePath("src/main/java")
   */
  File resolvePath(String path);

  /**
   * Source files, excluding unit tests and files matching project exclusion patterns.
   * 
   * @param langs language filter. Check all files, whatever their language, if null or empty.
   * @deprecated since 2.6 use {@link #mainFiles(Language...)} instead.
   *             See http://jira.codehaus.org/browse/SONAR-2126
   */
  @Deprecated
  List<File> getSourceFiles(Language... langs);

  /**
   * Java source files, excluding unit tests and files matching project exclusion patterns. Shortcut for getSourceFiles(Java.INSTANCE)
   * 
   * @deprecated since 2.6 use {@link #mainFiles(Language...)} instead.
   *             See http://jira.codehaus.org/browse/SONAR-2126
   */
  @Deprecated
  List<File> getJavaSourceFiles();

  /**
   * Check if the project has Java files, excluding unit tests and files matching project exclusion patterns.
   */
  boolean hasJavaSourceFiles();

  /**
   * Unit test files, excluding files matching project exclusion patterns.
   * 
   * @deprecated since 2.6 use {@link #testFiles(Language...)} instead.
   *             See http://jira.codehaus.org/browse/SONAR-2126
   */
  @Deprecated
  List<File> getTestFiles(Language... langs);

  /**
   * Check if the project has unit test files, excluding files matching project exclusion patterns.
   */
  boolean hasTestFiles(Language lang);

  /**
   * Save data into a new file of Sonar working directory.
   * 
   * @return the created file
   */
  File writeToWorkingDirectory(String content, String fileName) throws IOException;

  File getFileFromBuildDirectory(String filename);

  Resource toResource(File file);

  /**
   * Source files, excluding unit tests and files matching project exclusion patterns.
   * 
   * @param langs language filter. If null or empty, will return empty list
   * @since 2.6
   */
  List<InputFile> mainFiles(Language... langs);

  /**
   * TODO comment me
   * 
   * @param langs language filter. If null or empty, will return empty list
   * @since 2.6
   */
  List<InputFile> testFiles(Language... langs);

}
