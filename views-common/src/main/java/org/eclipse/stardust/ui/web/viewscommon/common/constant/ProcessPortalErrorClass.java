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
package org.eclipse.stardust.ui.web.viewscommon.common.constant;

import org.eclipse.stardust.ui.web.viewscommon.common.PortalErrorClass;

public class ProcessPortalErrorClass extends PortalErrorClass
{
   public final static PortalErrorClass UNABLE_TO_ACTIVATE_ACTIVITY =
      new ProcessPortalErrorClass("PP01000");
   public final static PortalErrorClass UNABLE_TO_ABORT_ACTIVITY =
      new ProcessPortalErrorClass("PP01001");
   public final static ProcessPortalErrorClass ABORT_ACTIVITY_DENIED =
      new ProcessPortalErrorClass("PP01002");
   public final static PortalErrorClass UNABLE_TO_SUSPEND_ACTIVITY =
      new ProcessPortalErrorClass("PP01003");
   public final static PortalErrorClass UNABLE_TO_START_ACTIVITY =
      new ProcessPortalErrorClass("PP01004");
   public final static PortalErrorClass UNABLE_TO_COMPLETE_ACTIVITY =
      new ProcessPortalErrorClass("PP01005");
   public final static PortalErrorClass FAILED_INVOKING_COMPLETION_METHOD =
      new ProcessPortalErrorClass("PP01006");
   public final static PortalErrorClass UNKNOWN_APP_CONTEXT_FOR_METHOD_INVOCATION =
      new ProcessPortalErrorClass("PP01007");
   public final static PortalErrorClass FAILED_EVALUATING_OUT_DATA_MAPPING =
      new ProcessPortalErrorClass("PP01008");
   
   protected ProcessPortalErrorClass(String id)
   {
      super(id);
   }

   protected String getResourceBundleName()
   {
      return ProcessPortalErrorResourceBundle.class.getName();
   }

   public static class ProcessPortalErrorResourceBundle extends BasePortalErrorResourceBundle
   {

      public Object[][] getContents()
      {
         return contents;
      }

      static final Object[][] contents = {
            {UNABLE_TO_ACTIVATE_ACTIVITY.getId(), "Unable to activate the activity"},
            {UNABLE_TO_ABORT_ACTIVITY.getId(), "Unable to abort the current activity"},
            {ABORT_ACTIVITY_DENIED.getId(), "Current activity does not allow abort by performer"},
            {UNABLE_TO_SUSPEND_ACTIVITY.getId(), "Unable to suspend the current activity"},
            {UNABLE_TO_START_ACTIVITY.getId(), "Unable to start the activity"},
            {UNABLE_TO_COMPLETE_ACTIVITY.getId(), "Unable to complete the current activity"},
            {FAILED_INVOKING_COMPLETION_METHOD.getId(), "Failed invoking completion method"},
            {UNKNOWN_APP_CONTEXT_FOR_METHOD_INVOCATION.getId(), "No suitable application context found for method invocation"},
            {FAILED_EVALUATING_OUT_DATA_MAPPING.getId(), "Failed evaluating out data mapping"}
      };
   }
   
     
}
