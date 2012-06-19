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


public class PortalErrorClass extends ErrorCase
{
   public final static PortalErrorClass UNABLE_TO_CONVERT_DATAMAPPING_VALUE =
      new PortalErrorClass("CWP01000");
   public final static PortalErrorClass UNABLE_TO_INITIALIZE_SESSION =
      new PortalErrorClass("CWP01001");
   public final static PortalErrorClass DELEGATE_FORBIDDEN =
      new PortalErrorClass("CWP01002");
   public final static PortalErrorClass UNABLE_TO_DELEGATE_ACTIVITY =
      new PortalErrorClass("CWP01003");
   public final static PortalErrorClass SESSION_EXPIRED =
      new PortalErrorClass("CWP01004");
   public final static PortalErrorClass UNKNOWN_ERROR_OCCURED_DURING_LOGIN =
      new PortalErrorClass("CWP01005");
   public final static PortalErrorClass NO_DEPLOYED_MODEL =
      new PortalErrorClass("CWP01006");
   public final static PortalErrorClass UNABLE_TO_DELEGATE_ACTIVITY_NOT_IN_WORKLIST =
      new PortalErrorClass("CWP01007");
   
   private PortalErrorClass detailErrorClass; 

   protected PortalErrorClass(String id)
   {
      super(id);
   }
   
   public String getLocalizedMessage(Locale locale)
   {
      return Localizer.getString(locale, 
            new LocalizerKey(getResourceBundleName(), getId(), false));
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
      if(isDetailAvailable())
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

      private static final String[][] contents = {
         {UNABLE_TO_CONVERT_DATAMAPPING_VALUE.getId(), "Unable to convert data mapping value"},
         {UNABLE_TO_INITIALIZE_SESSION.getId(), "Unable to initialize new session"},
         {DELEGATE_FORBIDDEN.getId(), "You have no permission to delegate the activity"},
         {UNABLE_TO_DELEGATE_ACTIVITY.getId(), "Unable to delegate the activity"},
         {SESSION_EXPIRED.getId(), "Unable to recover the current session, because it has possibly expired. Please login again!"},
         {UNKNOWN_ERROR_OCCURED_DURING_LOGIN.getId(), "An unknown error occured during login"},
         {NO_DEPLOYED_MODEL.getId(), "The audit trail contains no model."},
         {UNABLE_TO_DELEGATE_ACTIVITY_NOT_IN_WORKLIST.getId(), "Unable to delegate the activity, because it is no longer available in the worklist."}
      };
   }
   
   
}
