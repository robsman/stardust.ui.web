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

import org.eclipse.stardust.ui.web.common.BpmPortalErrorMessages;
import org.eclipse.stardust.ui.web.viewscommon.common.PortalErrorClass;

public class ProcessPortalErrorClass extends PortalErrorClass
{
   private static final long serialVersionUID = -2758652922569722703L;
   public final static PortalErrorClass UNABLE_TO_ACTIVATE_ACTIVITY = new ProcessPortalErrorClass("PP01000");
   public final static PortalErrorClass UNABLE_TO_ABORT_ACTIVITY = new ProcessPortalErrorClass("PP01001");
   public final static ProcessPortalErrorClass ABORT_ACTIVITY_DENIED = new ProcessPortalErrorClass("PP01002");
   public final static PortalErrorClass UNABLE_TO_SUSPEND_ACTIVITY = new ProcessPortalErrorClass("PP01003");
   public final static PortalErrorClass UNABLE_TO_START_ACTIVITY = new ProcessPortalErrorClass("PP01004");
   public final static PortalErrorClass UNABLE_TO_COMPLETE_ACTIVITY = new ProcessPortalErrorClass("PP01005");
   public final static PortalErrorClass FAILED_INVOKING_COMPLETION_METHOD = new ProcessPortalErrorClass("PP01006");
   public final static PortalErrorClass UNKNOWN_APP_CONTEXT_FOR_METHOD_INVOCATION = new ProcessPortalErrorClass(
         "PP01007");
   public final static PortalErrorClass FAILED_EVALUATING_OUT_DATA_MAPPING = new ProcessPortalErrorClass("PP01008");

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

      static final Object[][] contents = 
      {
         {"PP01000", BpmPortalErrorMessages.getString("PP01000")},
         {"PP01001", BpmPortalErrorMessages.getString("PP01001")},
         {"PP01002", BpmPortalErrorMessages.getString("PP01002")},
         {"PP01003", BpmPortalErrorMessages.getString("PP01003")},
         {"PP01004", BpmPortalErrorMessages.getString("PP01004")},
         {"PP01005", BpmPortalErrorMessages.getString("PP01005")},
         {"PP01006", BpmPortalErrorMessages.getString("PP01006")},
         {"PP01007", BpmPortalErrorMessages.getString("PP01007")},
         {"PP01008", BpmPortalErrorMessages.getString("PP01008")},
      };
   }

}
