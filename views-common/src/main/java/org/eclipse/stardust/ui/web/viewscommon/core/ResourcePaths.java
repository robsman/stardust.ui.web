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
package org.eclipse.stardust.ui.web.viewscommon.core;

public interface ResourcePaths
{
   static final String VIEW_PROCESS_INSTANCE_HISTORY_COLUMNS = "/plugins/views-common/processhistory/processInstanceHistoryTableColumns.xhtml";
   
   static final String VIEW_ACTIVITY_INSTANCE_HISTORY_COLUMNS = "/plugins/views-common/processhistory/activityInstanceHistoryTableColumns.xhtml";

   static final String VIEW_DELEGATION_COLUMNS = "/plugins/views-common/dialogs/delegationColumns.xhtml";

   static final String VIEW_PARTICIPANTS_PANEL_COLUMNS = "/plugins/views-common/participantspanel/participantsPanelColumns.xhtml";

   static final String VID_REPOSITORY = "genericRepositoryView";

   static final String VID_MY_DOCUMENTS = "myDocumentsTreeView";
   
   static final String VID_RESOURCE_MGMT = "resourceMgmtTreeView";
   
   static final String VID_MY_REPORTS = "myReportsView";
   
   static final String V_TIFF_BOOKMARKS_VIEW = "tiffBookmarksView";
   
   static final String V_CRITICALITY_CONFIG_VIEW = "criticalityConfigurationView";

   static final String V_DOCUMENT_MGMT = "resourceMgmtView";
   
   static final String V_AUTHORIZATION_MANAGER_VIEW = "authorizationManagerView";

   static final String V_RESOURCEDOWNLOAD = "/plugins/views-common/views/doctree/resourceDownload.xhtml";
   
   static final String V_SECURITY_DIALOG_COLUMNS = "/plugins/views-common/security/securityDialogColumns.xhtml";

   static final String V_DOCUMENT_VERSION_COLUMNS = "/plugins/views-common/views/doctree/documentVersionColumns.xhtml";

   static final String V_ACTIVITY_TABLE_COLUMNS = "/plugins/views-common/activityTableHelper/activityTableColumns.xhtml";

   static final String V_PROCESS_TABLE_COLUMNS = "/plugins/views-common/processTableHelper/processTableColumns.xhtml";
   
   static final String V_DESC_TABLE_COLUMNS = "/plugins/views-common/views/processContextExplorer/descriptorTableColumns.xhtml";
   
   static final String V_DOCUMENT_DESC_COLUMNS = "/plugins/views-common/processTableHelper/documentDescriptorColumns.xhtml";

   static final String I_FOLDER_CORRESPONDANCE = "pi pi-folder pi-lg";
   
   static final String I_EMAIL_GO = "pi pi-other pi-lg";

   static final String I_FOLDER_PERSONAL = "pi pi-folder pi-lg";

   static final String I_FOLDER = "pi pi-folder pi-lg";

   static final String I_DOCUMENT = "pi pi-case pi-lg";

   static final String I_DOCUMENT_PATH = "/plugins/views-common/images/icons/mime-types/";

   static final String I_PROCESS = "pi pi-process pi-lg";
   
   static final String I_CASE = "pi pi-case pi-lg";

   static final String I_PROCESS_ATTACHMENT = "pi pi-process-attachments pi-lg";
   
   static final String I_PROCESS_ATTACHMENT_BLANK = "pi pi-process-attachments-empty pi-lg";
   static final String I_PROCESS_ATTACHMENT_FILLED = "pi pi-process-attachments pi-lg";

   static final String I_NOTES_BLANK = "pi pi-notes-empty pi-lg";
   static final String I_NOTES_FILLED = "pi pi-notes pi-lg";
   
   static final String I_NOTES = "pi pi-document-repository pi-lg";

   static final String I_NOTES_FILE = "pi pi-notes pi-lg";
   static final String MIME_TYPE_PATH = "/plugins/views-common/images/icons/mime-types/";

   static final String V_NOTES_TOOLTIP = "/plugins/views-common/views/doctree/toolTip.xhtml";
   
   static final String I_REPOSITORY_ROOT = "pi pi-database pi-lg";
   static final String I_REPOSITORY = "pi pi-database pi-lg";
   static final String I_REPOSITORY_DEFAULT  = "pi pi-database pi-lg";
   
   static final String V_AUTOCOMPLETE_MULTI_USER_SELECTOR = "/plugins/views-common/user/userAutocompleteMultiSelector.xhtml";
   static final String V_AUTOCOMPLETE_SINGLE_USER_SELECTOR = "/plugins/views-common/user/userAutocompleteSingleSelector.xhtml";
   static final String V_AUTOCOMPLETE_USER_SELECTOR_TABLE = "/plugins/views-common/user/userAutocompleteMultiSelectorUserTable.xhtml";

   static final String V_AUTOCOMPLETE_USER_SELECTOR_TABLE_FILTER = "/plugins/views-common/user/userAutocompleteTableDataFilter.xhtml";

   static final String V_AUTOCOMPLETE_SINGLE_PARTICIPANT_SELECTOR = "/plugins/views-common/user/participantAutocompleteSingleSelector.xhtml";
   
   static final String V_AUTOCOMPLETE_MULTIPLE_PARTICIPANT_SELECTOR = "/plugins/views-common/user/participantAutocompleteMultiSelector.xhtml";
   static final String V_AUTOCOMPLETE_PARTICIPANT_SELECTOR_TABLE = "/plugins/views-common/user/participantAutocompleteMultiSelectorUserTable.xhtml";
   static final String V_AUTOCOMPLETE_PARTICIPANT_SELECTOR_TABLE_COLUMNS = "/plugins/views-common/user/participantAutocompleteMultiSelectorUserTableColumns.xhtml";
   static final String V_AUTOCOMPLETE_PARTICIPANT_SELECTOR_TABLE_FILTER = "/plugins/views-common/user/participantAutocompleteTableDataFilter.xhtml";
   
   static final String V_AUTOCOMPLETE_CRITICALITY_SINGLE_SELECTOR = "/plugins/views-common/criticality/criticalityAutocompleteSingleSelector.xhtml";
   static final String V_AUTOCOMPLETE_CRITICALITY_MULTIPLE_SELECTOR = "/plugins/views-common/criticality/criticalityAutocompleteMultiSelector.xhtml";
   static final String V_CRITICALITY_TABLE_FILTER = "/plugins/views-common/criticality/criticalityTableDataFilter.xhtml";
   static final String V_AUTOCOMPLETE_CRITICALITY_SELECTOR_TABLE = "/plugins/views-common/criticality/criticalityMultiSelectorTable.xhtml";
   static final String V_USER_NAME_TABLE_FILTER = "/plugins/views-common/user/userNameTableDataFilter.xhtml";

   static final String V_AUTOCOMPLETE_PRIORITY_SINGLE_SELECTOR = "/plugins/views-common/common/priorityAutocompleteSingleSelector.xhtml";
   static final String V_PRIORITY_TABLE_FILTER = "/plugins/views-common/common/priorityTableDataFilter.xhtml";
   static final String V_AUTOCOMPLETE_PRIORITY_MULTIPLE_SELECTOR = "/plugins/views-common/common/priorityAutocompleteMultiSelector.xhtml";
   static final String V_AUTOCOMPLETE_PRIORITY_SELECTOR_TABLE = "/plugins/views-common/common/priorityMultiSelectorTable.xhtml";
   
   static final String I_USER_ONLINE = "pi pi-user-status pi-lg pi-success";
   static final String I_USER_OFFLINE = "pi pi-user-status pi-lg pi-user-status-offline";

   static final String I_USER_CHAT_INITIATOR = "pi pi-user-status pi-lg pi-danger";
   static final String VIEW_DOCUMENT_SEARCH_COLUMN = "/plugins/views-common/views/documentsearch/documentSearchColumns.xhtml";
   static final String I_EMPTY_CORE_DOCUMENT = "/plugins/views-common/images/icons/page_white_error.png";
   static final String I_CORE_DOCUMENTS = "pi pi-process-attachments pi-lg";
   static final String I_REPORT = "pi pi-report pi-lg";
   
   static final String V_EXTRACT_PAGES_TABLE_COLUMNS = "/plugins/views-common/views/document/extractPageTableColumns.xhtml";
   static final String V_SPAWN_PROCESS_TABLE_COLUMNS = "/plugins/views-common/dialogs/spawnProcessTableColumns.xhtml";
   static final String V_EXTRACT_PAGE_CONF_DLG = "/plugins/views-common/dialogs/extractPageConfirmDialog.xhtml";
   static final String V_SWITCH_PROCESS_CONF_DLG = "/plugins/views-common/dialogs/switchProcessConfirmDialog.xhtml";
   static final String V_JOIN_PROCESS_CONF_DLG = "/plugins/views-common/dialogs/joinProcessNotificationDialog.xhtml";
   static final String V_ATTACH_CASE_CONF_DLG = "/plugins/views-common/views/case/attachToCaseNotification.xhtml";
   static final String V_DETACH_CASE_CONF_DLG = "/plugins/views-common/views/case/detachCaseNotification.xhtml";
   
   static final String V_PANELTOOLTIP_URL = "/plugins/views-common/common/genericPanelToolTip.xhtml";
   
   static final String V_CONFIRMATION_DIALOG_OPTIONS = "/plugins/views-common/dialogs/confirmationDialogOptions.xhtml";
   
   static final String V_PROCESS_ACTIVITY_FILTER = "/plugins/views-common/processActivityDataFilter.xhtml";

   static final String V_deputyManagementViewColumns = "/plugins/views-common/deputy/deputyManagementViewColumns.xhtml";
}
