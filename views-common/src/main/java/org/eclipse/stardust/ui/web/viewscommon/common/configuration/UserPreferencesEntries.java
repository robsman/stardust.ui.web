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
package org.eclipse.stardust.ui.web.viewscommon.common.configuration;

/**
 * @author Shrikant.Gangal
 *
 */
public interface UserPreferencesEntries
{
   public static final String M_VIEWS_COMMON = "ipp-views-common";
   
   public static final String M_ADMIN_PORTAL = "ipp-admin-portal";
   //BCC
   public static final String M_BCC = "ipp-business-control-center";
   
   public static final String V_MY_PICTURE = "user-profile";
   public static final String V_ACTIVITY_WITH_PRIO = "activityWithPrio";
   public static final String V_PROCESS_TABLE = "processTable";
   public static final String V_PARTICIPANTS = "participants";
   public static final String V_PROCESS_HISTORY = "mytab.processHistory";
   public static final String V_USER_AUTOCOMPLETE = "userAutoComplete";
   public static final String V_PARTICIPANT_AUTOCOMPLETE = "participantAutoComplete";
   public static final String V_PORTAL_CONFIG = "configuration";
   public static final String V_VERSION_HISTORY = "VersionHistory";
   public static final String V_PROCESS_INSTANCE_DETAILS = "processInstanceDetails";
   public static final String V_WORKFLOW_EXEC_CONFIG_PANEL = "workflowExecutionConfigurationPanel";
   public static final String V_IMAGE_VIEWER_CONFIG = "imageViewerConfiguration";
   public static final String V_USER_NAME_DISPALY_FORMAT = "userNameDisplayFormat";   
   
   //BCC
   public static final String V_REPORTS_CONFIG = "ReportsConfiguration";
   public static final String V_SWITCH_PROCESS = "SwitchProcessDialog";
   
   public static final String F_MY_PICTURE_TYPE = "prefs.myPicture.type";
   public static final String F_MY_PICTURE_HTTP_URL = "prefs.myPicture.http.url";
   public static final String F_PROCESS_INSTANCE = "prefs.processInstance";
   public static final String F_APPLICATION_ACTIVITY = "prefs.applicationActivity";
   public static final String F_MANUAL_ACTIVITY = "prefs.manualActivity";
   public static final String F_AUXILIARY = "prefs.auxiliary";
   public static final String F_DESCRIPTOR = "prefs.descriptor";
   public static final String F_DELEGATE = "prefs.delegate";
   public static final String F_EXCEPTION = "prefs.exception";
   public static final String F_NOTE = "prefs.note";
   public static final String F_ACTIVITY_COMPLETED = "prefs.activityCompleted";
   public static final String F_STATE_CHANGED = "prefs.stateChange";
   public static final String F_DOCUMENT = "prefs.document";
   public static final String F_DOCUMENT_SET = "prefs.documentSet";
   public static final String F_SUPPRESS_BLANK_DESCRIPTORS = "prefs.suppressBlankDescriptors";
   public static final String F_AUXILIARY_PROCESS = "prefs.auxiliaryProcess";
   public static final String F_PROPAGATE_PRIORITY = "prefs.propagatePriority";
   public static final String F_PROCESS_ABORT_SCOPE = "prefs.processAbortScope";
   public static final String F_ACTIVITY_ABORT_SCOPE = "prefs.activityAbortScope";

   public static final String F_IMAGE_VIEWER_SHOW_SIDE_PANEL = "prefs.imageViewer.showSidePanel";
   public static final String F_IMAGE_VIEWER_SHOW_ANNOTATIONS = "prefs.imageViewer.showAnnotation";
   public static final String F_IMAGE_VIEWER_INVERT_IMAGE = "prefs.imageViewer.invertImage";
   public static final String F_IMAGE_VIEWER_HIGHLIGHT_DATA_FIELDS_ENABLED = "prefs.imageViewer.highlightDataFieldsEnabled";
   public static final String F_IMAGE_VIEWER_DATANAME_IN_TARGET_INCLUDED = "prefs.imageViewer.datanameInTargetIncluded";
   public static final String F_IMAGE_VIEWER_MAGNIFY_FIELDS = "prefs.imageViewer.magnifyFields";
   public static final String F_IMAGE_VIEWER_BOLD_SELECTED = "prefs.imageViewer.boldSelected";
   public static final String F_IMAGE_VIEWER_ITALIC_SELECTED = "prefs.imageViewer.italicSelected";
   public static final String F_IMAGE_VIEWER_UNDERLINE_SELECTED = "prefs.imageViewer.underlineSelected";
   public static final String F_IMAGE_VIEWER_SELECTED_DISPLAY_ZOOM_LIVEL = "prefs.imageViewer.selectedDisplayZoomLevel";
   public static final String F_IMAGE_VIEWER_SELECTED_STICKY_NOTE_COLOUR = "prefs.imageViewer.selectedStickyNoteColour";
   public static final String F_IMAGE_VIEWER_SELECTED_HIGHLIGHTER_COLOUR = "prefs.imageViewer.selectedHighlighterColour";
   public static final String F_IMAGE_VIEWER_SELECTED_DATA_FIELD_HIGHLIGHTER_COLOUR = "prefs.imageViewer.selectedDataFieldHighlightColour";
   public static final String F_IMAGE_VIEWER_ENABLE_PAGE_DELETE = "prefs.imageViewer.enablePageDeletion";
   public static final String F_IMAGE_VIEWER_SELECTED_DOC_PRIOR_VERSION_ACTION = "prefs.imageViewer.selectedDocPriorVersionAction";
   public static final String F_IMAGE_VIEWER_SELECTED_DATA_FIELD_HIGHLIGHTER_OPACITY = "prefs.imageViewer.selectedDataFieldHighlightOpacity";
   public static final String F_IMAGE_VIEWER_SELECTED_NOTE_FONT_SIZE = "prefs.imageViewer.selectedNoteFontSize";
   public static final String F_IMAGE_VIEWER_SELECTED_STAMP = "prefs.imageViewer.selectedStamp";
   public static final String F_IMAGE_VIEWER_ENABLE_EXTRACT_PAGES = "prefs.imageViewer.enableExtractPages";
   public static final String F_IMAGE_VIEWER_ALLOW_DELETE_FROM_ORIGINAL = "prefs.imageViewer.allowDeleteFromOriginal";
   
   //BCC
   public static final String F_REPORTS_FAVORITE = "prefs.reports.favoriteReports";
   
   public static final String F_USER_NAME_DISPLAY_FORMAT_DEFAULT = "prefs.displayFormatDefault";
   public static final String F_USER_NAME_DISPLAY_FORMAT = "prefs.displayFormat";
   
   /* 
    * If there is a need to store a map (dynamic number of of key / value pairs) then 
    * the approach of generating key (using MODULEID + VIEW_ID + FEATURE_ID) doesn't suit
    * and we need to store the key / value pairs against a new and unique preference key.
    * (and we should not use the default preference id - "preference")
    * 
    * Such preference id entries should go here with a prefix "P_"
    */
   public static final String P_ACTIVITY_CRITICALITY_CONFIG = "workflow-criticality-categories";

   
}
