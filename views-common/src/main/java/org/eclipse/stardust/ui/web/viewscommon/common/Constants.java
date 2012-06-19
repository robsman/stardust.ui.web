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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;


/**
 * This file contains all constants specific to Views Common Project
 * 
 * @author Yogesh.Manware
 * 
 */
public class Constants
{
   public static final String PROCESS_HISTORY_IMAGES_BASE_PATH = "/plugins/common/images/icons/process-history/active/";
   public static final String WORKLIST_QA_AWAIT_STATE_IMAGE = "/plugins/processportal/images/icons/clock-small.png";
   public static final String WORKLIST_QA_FAILED_STATE_IMAGE = "/plugins/processportal/images/icons/cross-small-circle.png";

   public static final String ACTIVITY_QA_FAILED_IMAGE = "/plugins/views-common/images/icons/activity_qa_failed.png";
   public static final String ACTIVITY_QA_PASSED_IMAGE = "/plugins/views-common/images/icons/activity_qa_passed.png";
   public static final String ACTIVITY_QA_AWAIT_IMAGE = "/plugins/views-common/images/icons/activity_qa_await.png";

   public final static String LOGGED_IN = "loggedIn";
   public final static String LOGGED_INTO_PREFIX = LOGGED_IN + "_";

   public final static String LOGIN_APP_ID = "carnot.login.APPLICATION_ID";
   public final static String LOGIN_ADMIN_ROLE_REQUIRED = "carnot.login.ADMINISTRATOR_ROLE_REQUIRED";
   public final static String LOGIN_MODEL_REQUIRED = "carnot.login.MODEL_REQUIRED";
   public final static String LOGIN_HEADING = "carnot.login.HEADING";

   public final static String SESSION_LISTENER_BEANS = "carnot.SESSION_LISTENERS";
   public final static String PRINCIPAL_PAGE = "carnot.PRINCIPAL_PAGE";

   public final static String WORKFLOW_SUCCESS = "success";

   public final static String WORKFLOW_PRINCIPAL_LOGIN = "principalLogin";

   public final static String ACTION_LOGOUT = "action:logout";

   public final static String HTTP_LOGIN_PROP_ATTR = "infinity.login.properties";

   private final static Map criticalDurationThreshold;
   private final static String PI_CRITICAL_THRESHOLD = "Carnot.Critical.Duration.Threshold";

   private final static ActivityInstanceStates activityInstanceStates = new ActivityInstanceStates();

   public static final int SECONDS_PER_MINUTE = 60;

   private final static String CASE_SENSITIVE_SEARCH_PROP = "Carnot.Client.Search.CaseSensitive";

   public static final Boolean searchCaseSensitive;
   
   public ActivityInstanceStates getActivityInstanceState()
   {
      return activityInstanceStates;
   }
   
   public final static class ActivityInstanceStates
   {
      public ActivityInstanceState getCompleted()
      {
         return ActivityInstanceState.Completed;
      }
      
      public ActivityInstanceState getCreated()
      {
         return ActivityInstanceState.Created;
      }
      
      public ActivityInstanceState getApplication()
      {
         return ActivityInstanceState.Application;
      }

      public ActivityInstanceState getInterrupted()
      {
         return ActivityInstanceState.Interrupted;
      }
      
      public ActivityInstanceState getSuspended()
      {
         return ActivityInstanceState.Suspended;
      }
      
      public ActivityInstanceState getAborted()
      {
         return ActivityInstanceState.Aborted;
      }
      
      public ActivityInstanceState getHibernated()
      {
         return ActivityInstanceState.Hibernated;
      }
   }
   
   public final static float getCriticalDurationThreshold(int priorityIdent, float defaultValue)
   {
      Float threshold = (Float)criticalDurationThreshold
         .get(new Integer(priorityIdent));
      if(threshold == null)
      {
         threshold = (Float)criticalDurationThreshold.get(null);
      }
      return threshold == null ? defaultValue : threshold.floatValue();
   }
   
   public final static boolean isSearchCaseSensitive()
   {
      return searchCaseSensitive.booleanValue();
   }
   
   static
   {
      criticalDurationThreshold = new HashMap(4);
      String criticalPct = Parameters.instance().getString(PI_CRITICAL_THRESHOLD);
      if(!StringUtils.isEmpty(criticalPct))
      {
         float pct = (new Float(criticalPct)).floatValue() / 100.0f;
         criticalDurationThreshold.put(null, new Float(pct));
      }
      criticalPct = Parameters.instance().getString(PI_CRITICAL_THRESHOLD + ".1");
      if(!StringUtils.isEmpty(criticalPct))
      {
         float pct = (new Float(criticalPct)).floatValue() / 100.0f;
         criticalDurationThreshold.put(new Integer(1), new Float(pct));
      }
      criticalPct = Parameters.instance().getString(PI_CRITICAL_THRESHOLD + ".0");
      if(!StringUtils.isEmpty(criticalPct))
      {
         float pct = (new Float(criticalPct)).floatValue() / 100.0f;
         criticalDurationThreshold.put(new Integer(0), new Float(pct));
      }
      criticalPct = Parameters.instance().getString(PI_CRITICAL_THRESHOLD + ".-1");
      if(!StringUtils.isEmpty(criticalPct))
      {
         float pct = (new Float(criticalPct)).floatValue() / 100.0f;
         criticalDurationThreshold.put(new Integer(-1), new Float(pct));
      }      
      
      searchCaseSensitive = new Boolean(Parameters.instance()
            .getBoolean(CASE_SENSITIVE_SEARCH_PROP, true));
   }
   
   
   
}
