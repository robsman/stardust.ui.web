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
package org.eclipse.stardust.ui.web.viewscommon.common;

import java.io.Serializable;


public class LocalizerKey implements Serializable
{
   private final static long serialVersionUID = 1l;
   
   private final static String COMMON_MESSAGE_BUNDLE = "web-ui-common-messages";// "CommonMessages";
   public static final LocalizerKey UNMODIFIABLE_WEB_USER = new LocalizerKey("unmodifiableWebUser");
   public static final LocalizerKey INVALID_LOGIN_CREDENTIALS = new LocalizerKey("invalidLoginCredentials");
   public static final LocalizerKey DELEGATE_NOTES = new LocalizerKey("delegateNotes");
   public static final LocalizerKey ERROR_OCCURED = new LocalizerKey("errorOccured");
   public static final LocalizerKey ACCESS_FORBIDDEN = new LocalizerKey("accessForbidden");
   public static final LocalizerKey INVALID_DATE = new LocalizerKey("invalidDate");
   public static final LocalizerKey INVALID_DATA_VALUE = new LocalizerKey("invalidDataValue");
   public static final LocalizerKey DATE_VALID_FROM = new LocalizerKey("dateValidFrom");
   public static final LocalizerKey DATE_VALID_TO = new LocalizerKey("dateValidTo");
   public static final LocalizerKey ERROR_WHILE_GETTING_VALUE = new LocalizerKey("errorWhileGettingValue");
   public static final LocalizerKey ACTIVITY = new LocalizerKey("activity");
   public static final LocalizerKey PROCESS_DEFINITION = new LocalizerKey("processDefinition");
   public static final LocalizerKey PH_DELEGATION_TYPE = new LocalizerKey("processHistoryDelegationType");
   public static final LocalizerKey PH_EXCEPTION_TYPE = new LocalizerKey("processHistoryExceptionType");
   public static final LocalizerKey PH_NOTE_TYPE = new LocalizerKey("processHistoryNoteType");
   public static final LocalizerKey PH_RESUBMISSION_TYPE = new LocalizerKey("processHistoryResubmissionType");
   public static final LocalizerKey PH_ACTIVITY_ACTIVE_TYPE = new LocalizerKey("processHistoryActivityActiveType");
   public static final LocalizerKey PH_SUSPENDED_TYPE = new LocalizerKey("processHistorySuspendedType");
   public static final LocalizerKey PH_ABORTED_TYPE = new LocalizerKey("processHistoryAbortedType");
   public static final LocalizerKey PH_ABORTING_TYPE = new LocalizerKey("processHistoryAbortingType");
   public static final LocalizerKey PH_COMPLETED_TYPE = new LocalizerKey("processHistoryCompletedType");
   public static final LocalizerKey PH_INTERRUPTED_TYPE = new LocalizerKey("processHistoryInterruptedType");

   
   private final String bundleName;
   
   private final String key;

   private boolean mandatory;
   
   public LocalizerKey(String key)
   {
      this(COMMON_MESSAGE_BUNDLE, key);
   }
   
   public LocalizerKey(String bundleName, String key)
   {
      this(bundleName, key, true);
   }
   
   public LocalizerKey(String bundleName, String key, boolean mandatory)
   {
      this.bundleName = bundleName;
      this.key = key;
      this.mandatory = mandatory;
   }
   
   public String getBundleName()
   {
      return (null != bundleName) ? bundleName : COMMON_MESSAGE_BUNDLE;
   }
   
   public String getKey()
   {
      return key;
   }

   public boolean isMandatory()
   {
      return mandatory;
   }
}
