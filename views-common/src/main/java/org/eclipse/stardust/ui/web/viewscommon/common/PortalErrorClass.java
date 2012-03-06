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

import java.util.ListResourceBundle;
import java.util.Locale;

import org.eclipse.stardust.common.error.ErrorCase;
import org.eclipse.stardust.ui.web.common.BpmPortalErrorMessages;

public class PortalErrorClass extends ErrorCase
{
   private static final long serialVersionUID = 7290025404372830591L;
   public final static PortalErrorClass UNABLE_TO_CONVERT_DATAMAPPING_VALUE = new PortalErrorClass("CWP01000");
   public final static PortalErrorClass UNABLE_TO_INITIALIZE_SESSION = new PortalErrorClass("CWP01001");
   public final static PortalErrorClass DELEGATE_FORBIDDEN = new PortalErrorClass("CWP01002");
   public final static PortalErrorClass UNABLE_TO_DELEGATE_ACTIVITY = new PortalErrorClass("CWP01003");
   public final static PortalErrorClass SESSION_EXPIRED = new PortalErrorClass("CWP01004");
   public final static PortalErrorClass UNKNOWN_ERROR_OCCURED_DURING_LOGIN = new PortalErrorClass("CWP01005");
   public final static PortalErrorClass NO_DEPLOYED_MODEL = new PortalErrorClass("CWP01006");
   public final static PortalErrorClass UNABLE_TO_DELEGATE_ACTIVITY_NOT_IN_WORKLIST = new PortalErrorClass("CWP01007");

   private PortalErrorClass detailErrorClass;

   protected PortalErrorClass(String id)
   {
      super(id);
   }

   public String getLocalizedMessage(Locale locale)
   {
      return Localizer.getString(locale, new LocalizerKey(getResourceBundleName(), getId(), false));
   }

   public boolean isDetailAvailable()
   {
      return detailErrorClass != null;
   }

   public PortalErrorClass getDetailErrorClass()
   {
      return detailErrorClass;
   }

   public String getLocalizedDetail(Locale locale)
   {
      if (isDetailAvailable())
      {
         return getDetailErrorClass().getLocalizedMessage(locale);
      }
      return null;
   }

   protected String getResourceBundleName()
   {
      return BasePortalErrorResourceBundle.class.getName();
   }

   public static class BasePortalErrorResourceBundle extends ListResourceBundle
   {

      public Object[][] getContents()
      {
         return contents;
      }

      private static final String[][] contents = 
      {
         {"CWP01000", BpmPortalErrorMessages.getString("CWP01000")},
         {"CWP01001", BpmPortalErrorMessages.getString("CWP01001")},
         {"CWP01002", BpmPortalErrorMessages.getString("CWP01002")},
         {"CWP01003", BpmPortalErrorMessages.getString("CWP01003")},
         {"CWP01004", BpmPortalErrorMessages.getString("CWP01004")},
         {"CWP01005", BpmPortalErrorMessages.getString("CWP01005")},
         {"CWP01006", BpmPortalErrorMessages.getString("CWP01006")},
         {"CWP01007", BpmPortalErrorMessages.getString("CWP01007")},
      };
   }
}
