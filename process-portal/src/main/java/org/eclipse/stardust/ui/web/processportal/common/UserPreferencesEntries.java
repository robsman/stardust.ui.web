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
package org.eclipse.stardust.ui.web.processportal.common;

/**
 * @author Subodh.Godbole
 *
 */
public interface UserPreferencesEntries
{
   public static final String M_WORKFLOW = "ipp-workflow-perspective"; 

   public static final String V_ACTIVITY_PANEL = "activityPanel";
   public static final String V_WORKLIST = "worklist";
   public static final String V_NOTES = "processportalNotesTable";
   public static final String V_QA_CODES = "qaCodes";
   
   public static final String F_DISPLAY_DOCUMENTS = "prefs.displayDocuments"; // true/false
   public static final String F_DISPLAY_DOCUMENTS_TYPE = "prefs.displayDocumentsType"; // ALL or OLDEST
   public static final String F_DISPLAY_MAPPED_DOCS = "prefs.displayMappedDocs"; // true/false
   public static final String F_DISPLAY_NOTES = "prefs.displayNotes"; // true/false
   public static final String F_DISPLAY_PROCESS_DETAILS = "prefs.displayProcessDetails"; // true/false
   public static final String F_MINIMIZE_LAUNCH_PANELS = "prefs.minimizeLaunchPanels"; // true/false
   public static final String F_MAXIMIZE_VIEW = "prefs.maximizeView"; // true/false
   public static final String F_CLOSE_RELATED_VIEWS = "prefs.closeRelatedViews"; // true/false
   public static final String F_PROVIDERS = "prefs.filterProviders";
   public static final String F_DOCUMENTS_DISPLAY_MODE = "prefs.DocumentsDisplayMode"; // PORTAL or NEWBROWSER
   public static final String F_PIN_ACTIVITY_VIEW = "prefs.pinActivityView"; // true/false
   public static final String F_PIN_ACTIVITY_VIEW_TYPE = "prefs.pinActivityViewType"; // VERTICAL or HPRIZONTAL
   public static final String F_SHOW_MAPPED_DOC_WARNING = "prefs.showMappedDocWarning"; // true/false
   public static final String F_NO_OF_COLUMNS_IN_COLUMN_LAYOUT = "prefs.noOfColumnsInColumnLayout"; // int
   public static final String F_NO_OF_COLUMNS_IN_TABLE = "prefs.noOfColumnsInTable"; // int
   public static final String F_REFRESH_INTERVAL = "prefs.refreshInterval"; // int
   
   //Preference IDs for Participant/Process Worklist Column Configuration and 
   public static final String P_WORKLIST_PART_CONF = "worklist-participant-columns";
   public static final String P_WORKLIST_PROC_CONF = "worklist-process-columns";
   
}
