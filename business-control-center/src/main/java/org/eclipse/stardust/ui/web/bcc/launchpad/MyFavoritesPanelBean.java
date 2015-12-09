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
/**
 * 
 */
package org.eclipse.stardust.ui.web.bcc.launchpad;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.core.preferences.PreferenceScope;
import org.eclipse.stardust.engine.core.preferences.Preferences;
import org.eclipse.stardust.ui.web.common.uielement.AbstractLaunchPanel;
import org.eclipse.stardust.ui.web.viewscommon.utils.ServiceFactoryUtils;
import org.springframework.beans.factory.InitializingBean;

/**
 * @author Abhay.Thappan
 * 
 */
public class MyFavoritesPanelBean extends AbstractLaunchPanel implements InitializingBean
{
   private static final long serialVersionUID = -1188695396112164126L;

   private static final String moduleId = "FAVORITE";

   private static final String preferenceId = "trafficLightViewNew";

   private List<MyFavoriteModel> items = CollectionUtils.newArrayList();

   private boolean initialized;

   public MyFavoritesPanelBean()
   {
      super("myFavorites");
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
    */
   public void afterPropertiesSet() throws Exception
   {
      buildList();
   }

   /**
    * 
    */
   public void update()
   {
      initialized = true;
      buildList();
   }

   /**
    * 
    */
   private void buildList()
   {
      if (isExpanded())
      {
         items.clear();
         // Getting All TLV favorites for Current user
         Preferences preferences = ServiceFactoryUtils.getQueryService().getPreferences(PreferenceScope.USER, moduleId,
               preferenceId);
         Map<String, Serializable> prefMap = preferences.getPreferences();
         if (CollectionUtils.isNotEmpty(prefMap))
         {
            for (String favoriteName : prefMap.keySet())
            {
               items.add(new MyFavoriteModel(favoriteName));
            }
         }

      }
   }

   @Override
   public void setExpanded(boolean expanded)
   {
      super.setExpanded(expanded);

      if (!initialized && isExpanded())
      {
         update();
      }
   }

   public List<MyFavoriteModel> getItems()
   {
      return items;
   }
}
