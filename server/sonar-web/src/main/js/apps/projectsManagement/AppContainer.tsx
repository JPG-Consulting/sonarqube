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
import * as React from 'react';
import { connect } from 'react-redux';
import App from './App';
import forSingleOrganization from '../organizations/forSingleOrganization';
import { Organization, LoggedInUser, Visibility } from '../../app/types';
import { getAppState, getOrganizationByKey, getCurrentUser, Store } from '../../store/rootReducer';
import { receiveOrganizations } from '../../store/organizations';
import { changeProjectDefaultVisibility } from '../../api/permissions';
import { fetchOrganization } from '../organizations/actions';

interface StateProps {
  appState: { defaultOrganization: string; qualifiers: string[] };
  currentUser: LoggedInUser;
  organization?: Organization;
}

interface DispatchProps {
  fetchOrganization: (organization: string) => void;
  onVisibilityChange: (organization: Organization, visibility: Visibility) => void;
}

interface OwnProps {
  onRequestFail: (error: any) => void;
  organization: Organization;
}

class AppContainer extends React.PureComponent<OwnProps & StateProps & DispatchProps> {
  componentDidMount() {
    // if there is no organization, that means we are in the global scope
    // let's fetch defails for the default organization in this case
    if (!this.props.organization || !this.props.organization.projectVisibility) {
      this.props.fetchOrganization(this.props.appState.defaultOrganization);
    }
  }

  handleVisibilityChange = (visibility: Visibility) => {
    if (this.props.organization) {
      this.props.onVisibilityChange(this.props.organization, visibility);
    }
  };

  render() {
    const { organization } = this.props;

    if (!organization) {
      return null;
    }

    const topLevelQualifiers = organization.isDefault ? this.props.appState.qualifiers : ['TRK'];

    return (
      <App
        currentUser={this.props.currentUser}
        hasProvisionPermission={organization.canProvisionProjects}
        onVisibilityChange={this.handleVisibilityChange}
        organization={organization}
        topLevelQualifiers={topLevelQualifiers}
      />
    );
  }
}

const mapStateToProps = (state: Store, ownProps: OwnProps) => ({
  appState: getAppState(state),
  currentUser: getCurrentUser(state) as LoggedInUser,
  organization:
    ownProps.organization || getOrganizationByKey(state, getAppState(state).defaultOrganization)
});

const onVisibilityChange = (organization: Organization, visibility: Visibility) => (
  dispatch: Function
) => {
  const currentVisibility = organization.projectVisibility;
  dispatch(receiveOrganizations([{ ...organization, projectVisibility: visibility }]));
  changeProjectDefaultVisibility(organization.key, visibility).catch(() => {
    dispatch(receiveOrganizations([{ ...organization, projectVisibility: currentVisibility }]));
  });
};

const mapDispatchToProps = (dispatch: Function) => ({
  fetchOrganization: (key: string) => dispatch(fetchOrganization(key)),
  onVisibilityChange: (organization: Organization, visibility: Visibility) =>
    dispatch(onVisibilityChange(organization, visibility))
});

export default forSingleOrganization(
  connect(
    mapStateToProps,
    mapDispatchToProps
  )(AppContainer)
);
