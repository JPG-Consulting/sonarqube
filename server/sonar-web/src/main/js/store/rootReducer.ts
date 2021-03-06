/*
 * SonarQube
 * Copyright (C) 2009-2018 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
import { combineReducers } from 'redux';
import appState from './appState';
import globalMessages, * as fromGlobalMessages from './globalMessages';
import languages, * as fromLanguages from './languages';
import metrics, * as fromMetrics from './metrics';
import organizations, * as fromOrganizations from './organizations';
import users, * as fromUsers from './users';
import { AppState } from '../app/types';
import permissionsApp, * as fromPermissionsApp from '../apps/permissions/shared/store/rootReducer';
import projectAdminApp, * as fromProjectAdminApp from '../apps/project-admin/store/rootReducer';
import settingsApp, * as fromSettingsApp from '../apps/settings/store/rootReducer';

export type Store = {
  appState: AppState;
  globalMessages: fromGlobalMessages.State;
  languages: fromLanguages.Languages;
  metrics: fromMetrics.State;
  organizations: fromOrganizations.State;
  users: fromUsers.State;

  // apps
  permissionsApp: any;
  projectAdminApp: any;
  settingsApp: any;
};

export default combineReducers<Store>({
  appState,
  globalMessages,
  languages,
  metrics,
  organizations,
  users,

  // apps
  permissionsApp,
  projectAdminApp,
  settingsApp
});

export function getAppState(state: Store) {
  return state.appState;
}

export function getGlobalMessages(state: Store) {
  return fromGlobalMessages.getGlobalMessages(state.globalMessages);
}

export function getLanguages(state: Store) {
  return fromLanguages.getLanguages(state.languages);
}

export function getCurrentUser(state: Store) {
  return fromUsers.getCurrentUser(state.users);
}

export function getMetrics(state: Store) {
  return fromMetrics.getMetrics(state.metrics);
}

export function getMetricsKey(state: Store) {
  return fromMetrics.getMetricsKey(state.metrics);
}

export function getOrganizationByKey(state: Store, key: string) {
  return fromOrganizations.getOrganizationByKey(state.organizations, key);
}

export function getMyOrganizations(state: Store) {
  return fromOrganizations.getMyOrganizations(state.organizations);
}

export function areThereCustomOrganizations(state: Store) {
  return getAppState(state).organizationsEnabled;
}

export function getPermissionsAppUsers(state: Store) {
  return fromPermissionsApp.getUsers(state.permissionsApp);
}

export function getPermissionsAppGroups(state: Store) {
  return fromPermissionsApp.getGroups(state.permissionsApp);
}

export function isPermissionsAppLoading(state: Store) {
  return fromPermissionsApp.isLoading(state.permissionsApp);
}

export function getPermissionsAppQuery(state: Store) {
  return fromPermissionsApp.getQuery(state.permissionsApp);
}

export function getPermissionsAppFilter(state: Store) {
  return fromPermissionsApp.getFilter(state.permissionsApp);
}

export function getPermissionsAppSelectedPermission(state: Store) {
  return fromPermissionsApp.getSelectedPermission(state.permissionsApp);
}

export function getPermissionsAppError(state: Store) {
  return fromPermissionsApp.getError(state.permissionsApp);
}

export function getGlobalSettingValue(state: Store, key: string) {
  return fromSettingsApp.getValue(state.settingsApp, key);
}

export function getSettingsAppDefinition(state: Store, key: string) {
  return fromSettingsApp.getDefinition(state.settingsApp, key);
}

export function getSettingsAppAllCategories(state: Store) {
  return fromSettingsApp.getAllCategories(state.settingsApp);
}

export function getSettingsAppDefaultCategory(state: Store) {
  return fromSettingsApp.getDefaultCategory(state.settingsApp);
}

export function getSettingsAppSettingsForCategory(
  state: Store,
  category: string,
  componentKey: string
) {
  return fromSettingsApp.getSettingsForCategory(state.settingsApp, category, componentKey);
}

export function getSettingsAppChangedValue(state: Store, key: string) {
  return fromSettingsApp.getChangedValue(state.settingsApp, key);
}

export function isSettingsAppLoading(state: Store, key: string) {
  return fromSettingsApp.isLoading(state.settingsApp, key);
}

export function getSettingsAppValidationMessage(state: Store, key: string) {
  return fromSettingsApp.getValidationMessage(state.settingsApp, key);
}

export function getSettingsAppEncryptionState(state: Store) {
  return fromSettingsApp.getEncryptionState(state.settingsApp);
}

export function getProjectAdminProjectModules(state: Store, projectKey: string) {
  return fromProjectAdminApp.getProjectModules(state.projectAdminApp, projectKey);
}
