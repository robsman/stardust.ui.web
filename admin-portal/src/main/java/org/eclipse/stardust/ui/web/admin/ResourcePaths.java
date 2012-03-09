/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.admin;

/**
 * @author Ankita.Patel
 * @version $Revision: $
 */
public interface ResourcePaths
{
   static final String LP_allManagementViews = "ippAdmAllManagementViews";
   static final String LP_administrativeActions = "ippAdmAdministrativeActions";
   
   static final String V_activityView = "activityView";
   static final String V_processView = "processView";
   static final String V_overview = "overview";
   static final String V_userGroupMgmt = "userGroupMgmt";
   static final String V_realmMgmt = "realmMgmt";
   static final String V_daemons = "daemons";
   //static final String V_modelMgmtView = "modelMgmtView";
   static final String V_modelManagementView = "modelManagementView";
   //static final String V_deployModelView = "deployModelView";
   static final String V_configurationVariablesView = "configurationVariablesView";
   //static final String V_resourceMgmtView = "resourceMgmtView";
   //static final String V_repositoryView = "repositoryView";
   static final String V_participantMgmt = "participantMgmt";
   static final String V_qaManagement = "qaManagementView";
   
//   public static final String V_OVERVIEW_VIEW = "/plugins/admin-portal/views/overviewView.xhtml";
//   public static final String V_USERGROUPMGMT_VIEW = "/plugins/admin-portal/views/userGroupMgmtView.xhtml";
//   public static final String V_REALMGMT_VIEW = "/plugins/admin-portal/views/realmMgmtView.xhtml";
//   public static final String V_DAEMONS_VIEW = "/plugins/admin-portal/views/daemonsView.xhtml";
//   public static final String V_MODELMGMT_VIEW = "/plugins/admin-portal/views/modelMgmtView.xhtml";
//   public static final String V_DEPLOY_MODEL_VIEW = "/plugins/admin-portal/views/model/deployModelView.xhtml";
//   public static final String V_MODEL_CONFIGURATION_VIEW = "/plugins/admin-portal/views/model/modelVariablesView.xhtml";
   public static final String V_MODEL_CONFIGURATION_VIEW_COLUMNS = "/plugins/admin-portal/views/model/configurationVariablesTableColumns.xhtml";
   public static final String V_MODEL_MANAGEMENT_VIEW_COLUMNS = "/plugins/admin-portal/views/model/modelManagementTableColumns.xhtml";
  // public static final String V_DEPLOY_MODEL_VIEW_COLUMNS = "/plugins/admin-portal/views/model/deployModelTableColumns.xhtml";
   public static final String V_DEPLOY_MODEL_DIALOG_COLUMNS = "/plugins/admin-portal/views/model/_deployModelTableColumns.xhtml";
   public static final String V_DEPLOYMENT_STATUS_COLUMNS = "/plugins/admin-portal/views/model/deploymentStatusTableColumns.xhtml";
   //public static final String V_PARTICIPANTMGMT_VIEW = "/plugins/admin-portal/views/participantMgmtView.xhtml";
   
   public static final String V_OVERVIEWCOLUMNS_VIEW = "/plugins/admin-portal/views/overviewTableColumns.xhtml";
  // public static final String V_ACTIVITYCOLUMNS_VIEW = "/plugins/admin-portal/views/activityViewColumns.xhtml";
  // public static final String V_PROCESSCOLUMNS_VIEW = "/plugins/admin-portal/views/processViewColumns.xhtml";
   public static final String V_USERCOLUMNS_VIEW = "/plugins/admin-portal/views/userMgmtViewColumns.xhtml";
   public static final String V_USERGROUPCOLUMNS_VIEW = "/plugins/admin-portal/views/userGroupMgmtViewColumns.xhtml";
   public static final String V_REALCOLUMNS_VIEW = "/plugins/admin-portal/views/realmMgmtViewColumns.xhtml";
   public static final String V_DAEMONCOLUMNS_VIEW = "/plugins/admin-portal/views/daemonsViewColumns.xhtml";
   //public static final String V_RESOURCEMGMTCOLUMNS_VIEW = "/plugins/admin-portal/views/resourceMgmtViewColumns.xhtml";
   
   //public static final String V_USERDETAILS_VIEW = "/plugins/admin-portal/views/userDetails.xhtml";
  // public static final String V_USERPROCESSDETAILS_VIEW = "/plugins/admin-portal/views/userDetailsProcessColumns.xhtml";
   
  // public static final String V_CREATEUSER = "/plugins/admin-portal/views/createUser.xhtml";
  // public static final String V_CREATEORMODIFYUSERGROUP = "/plugins/admin-portal/views/createOrModifyUserGroup.xhtml";
   //public static final String V_CREATEREALM = "/plugins/admin-portal/views/realm.xhtml";
  // public static final String V_NOTIFICATION = "/plugins/views-common/contentmgmt/views/notificationMessage.xhtml";
   
   //public static final String V_RESOURCEDOWNLOAD = "/plugins/admin-portal/views/resourceDownload.xhtml";   
   public static final String V_ConfirmEncyprtPwd = "/plugins/admin-portal/views/confirmEncryptPwd.xhtml";
   public static final String LP_CleanAuditTrailDB = "/plugins/admin-portal/extensions/launchpad/cleanAuditTrailDataBase.xhtml";
   public static final String LP_CleanAuditAndModelTrailDB = "/plugins/admin-portal/extensions/launchpad/cleanAuditTrailAndModelDatabase.xhtml";
   public static final String LP_Recovery = "/plugins/admin-portal/extensions/launchpad/revoveryPopupContent.xhtml";
   public static final String V_QA_ACTIVITIES_TABLE_COLUMNS = "/plugins/admin-portal/views/qualityAssuranceActivityTableColumns.xhtml";
   public static final String V_QA_DEPARTMENT_TABLE_COLUMNS = "/plugins/admin-portal/views/qualityAssuranceDepartmentTableColumns.xhtml";
}
