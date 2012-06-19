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
package org.eclipse.stardust.ui.web.common.app;

import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.stardust.ui.web.common.spi.navigation.NavigationProvider;
import org.eclipse.stardust.ui.web.common.spi.preference.PreferenceProvider;
import org.eclipse.stardust.ui.web.common.spi.theme.ThemeProvider;
import org.eclipse.stardust.ui.web.common.spi.user.UserProvider;
import org.eclipse.stardust.ui.web.common.util.AbstractMessageBean;
import org.springframework.beans.factory.InitializingBean;


/**
 * Singleton UI Controller. This class is responsible for managing the views and access to
 * service providers necessary for the management of to those views.
 * 
 * @author Pierre Asselin
 */

public class UIController implements InitializingBean
{
   private static final Logger logger = Logger.getLogger(UIController.class);

   private static UIController instance;

   private NavigationProvider navigationProvider;

   private UserProvider userProvider;

   private ThemeProvider themeProvider;
   
   private PreferenceProvider preferenceProvider;

   /* (non-Javadoc)
    * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
    */
   public void afterPropertiesSet() throws Exception
   {
      logger.info("Initializing UIController.");
      instance = this;
   }

   /**
    * @return
    */
   public static UIController getInstance()
   {
      return instance;
   }
   
   /**
    * nested parameter should be false when calling this. In short nesting is not supported in this API
    */
   public void openInFocusTab()
   {
      PortalApplication.getInstance().openInFocusTab();
   }

   /**
    * @param viewId
    * @param params
    * @param msgBean
    */
   public void openInFocusTab(String viewId, Map<String, Object> params, AbstractMessageBean msgBean)
   {
      PortalApplication.getInstance().openInFocusTab(viewId, params, msgBean);
   }

   /**
    * 
    */
   public void openInNewTab()
   {
      PortalApplication.getInstance().openView();
   }
   
   /**
    * @param viewId
    * @param params
    * @param msgBean
    * @param nested
    */
   public void openInNewTab(String viewId, Map<String, Object> params, AbstractMessageBean msgBean, boolean nested)
   {
      PortalApplication.getInstance().openViewById(viewId, params, msgBean, nested);
   }

   public void setNavigationProvider(NavigationProvider navigationProvider)
   {
      this.navigationProvider = navigationProvider;
   }

   public void setUserProvider(UserProvider userProvider)
   {
      this.userProvider = userProvider;
   }

   public void setThemeProvider(ThemeProvider themeProvider)
   {
      this.themeProvider = themeProvider;
   }

   public NavigationProvider getNavigationProvider()
   {
      return navigationProvider;
   }

   public UserProvider getUserProvider()
   {
      return userProvider;
   }

   public ThemeProvider getThemeProvider()
   {
      return themeProvider;
   }

   public PreferenceProvider getPreferenceProvider()
   {
      return preferenceProvider;
   }

   public void setPreferenceProvider(PreferenceProvider preferenceProvider)
   {
      this.preferenceProvider = preferenceProvider;
   }
}