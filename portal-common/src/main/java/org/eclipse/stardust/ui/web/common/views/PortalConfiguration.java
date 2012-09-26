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
package org.eclipse.stardust.ui.web.common.views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.faces.event.ValueChangeEvent;

import org.eclipse.stardust.ui.web.common.IPerspectiveDefinition;
import org.eclipse.stardust.ui.web.common.PreferencePage;
import org.eclipse.stardust.ui.web.common.PreferencesDefinition;
import org.eclipse.stardust.ui.web.common.ResourcePaths;
import org.eclipse.stardust.ui.web.common.UIComponentBean;
import org.eclipse.stardust.ui.web.common.UiElement;
import org.eclipse.stardust.ui.web.common.ViewDefinition;
import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.common.app.PortalUiController;
import org.eclipse.stardust.ui.web.common.categorytree.GenericCategory;
import org.eclipse.stardust.ui.web.common.categorytree.GenericCategoryTree;
import org.eclipse.stardust.ui.web.common.categorytree.GenericCategoryTreeUserObject;
import org.eclipse.stardust.ui.web.common.categorytree.IGenericCategoryTreeUserObjectCallback;
import org.eclipse.stardust.ui.web.common.configuration.PreferencesScopesHelper;
import org.eclipse.stardust.ui.web.common.event.ViewEvent;
import org.eclipse.stardust.ui.web.common.event.ViewEventHandler;
import org.eclipse.stardust.ui.web.common.event.ViewEvent.ViewEventType;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.common.util.MessagePropertiesBean;
import org.eclipse.stardust.ui.web.common.util.StringUtils;
import org.springframework.beans.factory.InitializingBean;

import com.icesoft.faces.component.menubar.MenuItem;

/**
 * @author subodh.godbole
 * 
 */
public class PortalConfiguration extends UIComponentBean
      implements IGenericCategoryTreeUserObjectCallback, InitializingBean, ViewEventHandler
{
   private static final long serialVersionUID = 1L;

   private static final String PRE = "views.configurationTreeView.tree.";

   private GenericCategoryTree tree;

   private String configInclude;

   private String configTitle;
   
   private PreferencesScopesHelper prefScopesHelper;
   
   private ArrayList<PortalConfigurationListener> configurationListeners;

   /**
    * 
    */
   public PortalConfiguration()
   {
      super("configurationTreeView");
   }

   public static PortalConfiguration getInstance()
   {
      return (PortalConfiguration)FacesUtils.getBeanFromContext("ippPortalConfig");
   }
   
   /* (non-Javadoc)
    * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
    */
   public void afterPropertiesSet() throws Exception
   {
      initialize();
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.event.ViewEventHandler#handleEvent(org.eclipse.stardust.ui.web.common.event.ViewEvent)
    */
   public void handleEvent(ViewEvent event)
   {
      if (ViewEventType.CREATED == event.getType())
      {
         buildUI();
      }
   }

   @Override
   public void initialize()
   {
      prefScopesHelper = new PreferencesScopesHelper();
      configurationListeners = new ArrayList<PortalConfigurationListener>();
   }
   
   /**
    * 
    */
   private void buildUI()
   {
      // BUILD TREE
      MessagePropertiesBean messageBean = MessagePropertiesBean.getInstance();
      PortalUiController uiController = PortalUiController.getInstance();

      tree = new GenericCategoryTree("confRoot", messageBean.getString(PRE + "root"),
            this);

      // ***** PORTAL CONFIG *****
      GenericCategory portal = tree.getRootCategory().addSubCategory("portalConfig",
            messageBean.getString(PRE + "portalConfig"), "portalConfig");
      portal.addItem("portalView", messageBean.getString(PRE + "portalView"),
            "portalView");
      portal.setExpanded(true);

      // ***** PERSPECTIVES *****
      GenericCategory perspectivesCategory = null;

      // uiController.getPerspectiveItems() returns alphabetically sorted list, so use it as is
      for (MenuItem menuItem : uiController.getPerspectiveItems())
      {
         IPerspectiveDefinition pd = uiController.getPerspectives().get(menuItem.getId());
         if (hasConfigPanel(pd.getPreferences()))
         {
            // Add the perspectives node only if any perspective with configuration panel exists
            if(perspectivesCategory == null)
            {
               perspectivesCategory = tree.getRootCategory().addSubCategory(
                     "perspectives", messageBean.getString(PRE + "perspectives"));
               perspectivesCategory.setExpanded(true);
            }

            perspectivesCategory.addItem(menuItem.getId(), (String) menuItem.getValue(),
                  pd.getPreferences().getPreference(
                        PreferencesDefinition.PREF_CONFIG_PANEL));
         }
      }

      // ***** VIEWS *****
      List<ViewDefinition> allViews = new ArrayList<ViewDefinition>();
      Map<String, IPerspectiveDefinition> perspectives = uiController.getPerspectives();
      for (IPerspectiveDefinition pd : perspectives.values())
      {
         for (ViewDefinition vd : pd.getViews())
         {
            if (hasConfigPanel(vd.getPreferences()) && !allViews.contains(vd))
            {
               allViews.add(vd);
            }
         }
      }

      if(allViews.size() > 0)
      {
         Collections.sort(allViews, new Comparator<ViewDefinition>()
         {
            public int compare(ViewDefinition vd1, ViewDefinition vd2)
            {
               String title1 = getViewLabelTitle(vd1);
               String title2 = getViewLabelTitle(vd2);

               return title1.compareTo(title2);
            }
         });

         GenericCategory viewsCategory = tree.getRootCategory().addSubCategory("views",
               messageBean.getString(PRE + "views"));
         viewsCategory.setExpanded(true);
         
         for (ViewDefinition vd : allViews)
         {
            viewsCategory.addItem(vd.getName(), getViewLabelTitle(vd),
                  vd.getPreferences().getPreference(PreferencesDefinition.PREF_CONFIG_PANEL));
        }
      }

      tree.refreshTreeModel();
   }

   /**
    * @param vd
    * @return
    */
   private String getViewLabelTitle(ViewDefinition vd)
   {
      String title;

      if (vd.hasMessage(UiElement.PRE_LABEL_TITLE, null))
      {
         title = vd.getMessage(UiElement.PRE_LABEL_TITLE, null);
      }
      else
      {
         title = vd.getMessage(UiElement.PRE_LABEL, null);
      }

      return title;
   }

   /**
    * @param preferences
    * @return
    */
   private boolean hasConfigPanel(PreferencesDefinition preferences)
   {
      if (preferences != null
            && preferences.getPreference(PreferencesDefinition.PREF_CONFIG_PANEL) != null)
      {
         return true;
      }
      return false;
   }

   /**
    * 
    */
   public void openConfiguration()
   {
      PortalApplication.getInstance().openViewById("configurationTreeView", "configurationTreeView", null, null, false);
   }

   public boolean isConfigSelected()
   {
      return !StringUtils.isEmpty(configInclude);
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.categorytree.IGenericCategoryTreeUserObjectCallback#itemClicked(org.eclipse.stardust.ui.web.common.categorytree.GenericCategoryTreeUserObject)
    */
   public void itemClicked(GenericCategoryTreeUserObject treeUserobject)
   {
      if (treeUserobject.isReferencingItem())
      {
         configTitle = treeUserobject.getItem().getLabel();
         Object itemObject = treeUserobject.getItem().getItemObject();
         if (itemObject instanceof String)
         {
            if ("portalView".equals(itemObject))
            {
               configInclude = ResourcePaths.V_CONFIGURATION_PANEL;
            }
         }
         else if (itemObject instanceof PreferencePage)
         {
            PreferencePage prefPage = (PreferencePage) itemObject;
            configInclude = prefPage.getInclude();
         }
      }
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.categorytree.IGenericCategoryTreeUserObjectCallback#collapsed(org.eclipse.stardust.ui.web.common.categorytree.GenericCategoryTreeUserObject)
    */
   public void collapsed(GenericCategoryTreeUserObject treeUserobject)
   {
      //NOP
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.categorytree.IGenericCategoryTreeUserObjectCallback#expanded(org.eclipse.stardust.ui.web.common.categorytree.GenericCategoryTreeUserObject)
    */
   public void expanded(GenericCategoryTreeUserObject treeUserobject)
   {
      //NOP
   }

   /**
    * @param event
    */
   public void preferenceScopeValueChanged(ValueChangeEvent event)
   {
      if (!event.getPhaseId().equals(javax.faces.event.PhaseId.INVOKE_APPLICATION))
      {
         event.setPhaseId(javax.faces.event.PhaseId.INVOKE_APPLICATION);
         event.queue();
         return;
      }
      
      for (PortalConfigurationListener confListener : configurationListeners)
      {
         if(confListener != null)
         {
            confListener.preferencesScopeChanged(prefScopesHelper.getSelectedPreferenceScope());
         }
      }
   }

   /**
    * @param listener
    */
   public void addListener(PortalConfigurationListener listener)
   {
      if(listener != null)
      {
         configurationListeners.add(listener);
      }
   }
   
   public GenericCategoryTree getTree()
   {
      return tree;
   }

   public String getConfigInclude()
   {
      return configInclude;
   }

   public String getConfigTitle()
   {
      return configTitle;
   }
   
   public PreferencesScopesHelper getPrefScopesHelper()
   {
      return prefScopesHelper;
   }
}
