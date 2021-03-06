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


import static org.eclipse.stardust.ui.web.common.util.CollectionUtils.newArrayList;
import static org.eclipse.stardust.ui.web.common.util.StringUtils.areEqual;
import static org.eclipse.stardust.ui.web.common.util.StringUtils.isEmpty;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;

import org.eclipse.stardust.ui.web.common.IPerspectiveDefinition;
import org.eclipse.stardust.ui.web.common.LaunchPanel;
import org.eclipse.stardust.ui.web.common.MenuSection;
import org.eclipse.stardust.ui.web.common.PerspectiveDefinition;
import org.eclipse.stardust.ui.web.common.PerspectiveExtension;
import org.eclipse.stardust.ui.web.common.PreferencePage;
import org.eclipse.stardust.ui.web.common.PreferencesDefinition;
import org.eclipse.stardust.ui.web.common.ToolbarSection;
import org.eclipse.stardust.ui.web.common.UiElementWithPermissions;
import org.eclipse.stardust.ui.web.common.ViewDefinition;
import org.eclipse.stardust.ui.web.common.app.View.ViewState;
import org.eclipse.stardust.ui.web.common.event.PerspectiveEvent;
import org.eclipse.stardust.ui.web.common.event.PerspectiveEventHandler;
import org.eclipse.stardust.ui.web.common.event.ViewDataEvent;
import org.eclipse.stardust.ui.web.common.event.ViewDataEventHandler;
import org.eclipse.stardust.ui.web.common.event.ViewEvent;
import org.eclipse.stardust.ui.web.common.event.ViewEventHandler;
import org.eclipse.stardust.ui.web.common.event.PerspectiveEvent.PerspectiveEventType;
import org.eclipse.stardust.ui.web.common.event.ViewEvent.ViewEventType;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.message.MessageDialog;
import org.eclipse.stardust.ui.web.common.spi.menu.CommonMenuProvider;
import org.eclipse.stardust.ui.web.common.spi.user.IAuthorizationProvider;
import org.eclipse.stardust.ui.web.common.spi.user.UserProvider;
import org.eclipse.stardust.ui.web.common.spring.scope.TabScopeManager;
import org.eclipse.stardust.ui.web.common.spring.scope.TabScopeUtils;
import org.eclipse.stardust.ui.web.common.util.AbstractMessageBean;
import org.eclipse.stardust.ui.web.common.util.CollectionUtils;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.common.util.MessagePropertiesBean;
import org.eclipse.stardust.ui.web.common.util.StringUtils;
import org.eclipse.stardust.ui.web.common.util.UserUtils;
import org.eclipse.stardust.ui.web.plugin.support.ServiceLoaderUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.icesoft.faces.component.menubar.MenuItem;
import com.icesoft.faces.webapp.http.servlet.ServletExternalContext;




/**
 * @author robert.sauer
 * @version $Revision: $
 */
public class PortalUiController
      implements TabScopeManager, ApplicationContextAware, InitializingBean, Serializable
{
   private static final long serialVersionUID = 1L;

   public static final String BEAN_NAME = "ippPortalUi";

   private static final Logger trace = LogManager.getLogger(PortalUiController.class);
   
   private transient ApplicationContext appContext;

   private IPerspectiveDefinition currentPerspective;

   private Map<String, IPerspectiveDefinition> perspectives;
   
   private Map<String, PerspectiveDefinition> systemPerspectives;

   private PerspectiveController perspectiveController;
   
   private transient List<MenuItem> perspectiveItems;
   
   private Map<String, View> views;

   private List<View> openViews;

   private View focusView;

   private View activeView;
   
   private Map<String, View> recreatableViews;
   
   private UserProvider userProvider;

   private CommonMenuIframeHandler commonMenuIframeHandler;
   
   private transient Map<View, List<ViewDataEventHandler>> viewDataEventHandlers;
   
   private List<IPerspectiveDefinition> allPerspectives;
   
   private IAuthorizationProvider authorizationProvider; 
   private boolean authorizationProviderInitialized = false;
   
   public PortalUiController()
   {
      this.views = new HashMap<String, View>();
      this.openViews = new ArrayList<View>();
      this.recreatableViews = new HashMap<String, View>();
      this.viewDataEventHandlers = new HashMap<View, List<ViewDataEventHandler>>();
   }
   
   /**
    * @return
    */
   public static PortalUiController getInstance()
   {
      return (PortalUiController)FacesUtils.getBeanFromContext(BEAN_NAME);
   }

   public IPerspectiveDefinition getPerspective()
   {
      return currentPerspective;
   }

   /**
    * @return
    */
   public List<LaunchPanel> getIcefacesLaunchPanels()
   {
      List<LaunchPanel> icefacesLaunchPanels = new ArrayList<LaunchPanel>();
      List<LaunchPanel> launcPanels = getPerspective().getLaunchPanels();
      for (LaunchPanel launchPanel : launcPanels)
      {
         if (!launchPanel.getInclude().endsWith(".html"))
         {
            icefacesLaunchPanels.add(launchPanel);
         }
      }
      
      return icefacesLaunchPanels;
   }

   public IPerspectiveDefinition getPerspective(String perspectiveId)
   {
      return perspectives.get(perspectiveId);
   }
   
   public List<ToolbarSection> getToolbarSections()
   {
      ViewDefinition currentView = (null != getActiveView())
            ? getActiveView().getDefinition()
            : null;

            List<ToolbarSection> ret = newArrayList();

      // add all toolbar section from current perspective
      ret.addAll(getPerspective().getToolbarSections());

      for (IPerspectiveDefinition definition : perspectives.values())
      {
         if (definition.getViews().contains(currentView))
         {
            // add view dependent toolbar from perspective defining the view
            for (ToolbarSection toolbar : definition.getToolbarSections())
            {
               if ( !ret.contains(toolbar)
                     && FacesUtils.isToolbarEnabledForView(getActiveView(), toolbar))
               {
                  ret.add(toolbar);
               }
            }
         }
      }
      return ret;
   }

   /**
    * @param view
    * @return
    */
   public static List<ToolbarSection> getToolbarSections(View view)
   {
      ViewDefinition currentView = view.getDefinition();

      List<ToolbarSection> ret = newArrayList();
      for (IPerspectiveDefinition definition : PortalUiController.getInstance().getPerspectives().values())
      {
         if (definition.getViews().contains(currentView))
         {
            // add view dependent toolbar from perspective defining the view
            for (ToolbarSection toolbar : definition.getToolbarSections())
            {
               if (!ret.contains(toolbar) && FacesUtils.isToolbarEnabledForView(view, toolbar))
               {
                  ret.add(toolbar);
               }
            }
         }
      }
      return ret;
   }
   
   /**
    * @deprecated replaced by request bound view scope manager
    */
   public Map<String, Object> getCurrentTabScope()
   {
      return (null != perspectiveController.getFocusView())
            ? perspectiveController.getFocusView().getViewMap()
            : null;
   }

   /**
    * @deprecated replaced by request bound view scope manager
    */
   public Map<String, Runnable> getCurrentTabScopeDestructionCallbacks()
   {
      return (null != perspectiveController.getFocusView())
            ? perspectiveController.getFocusView().getViewScopeDestructionCallbacks()
            : null;
   }

   /**
    * @deprecated Use portalUiController directly
    */
   @Deprecated
   public PerspectiveController getPerspectiveController()
   {
      return perspectiveController;
   }

   /* (non-Javadoc)
    * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
    */
   public void afterPropertiesSet() throws Exception
   {
      perspectives = new HashMap<String, IPerspectiveDefinition>();
      
      // Collect Perspectives based on Role
      PerspectiveDefinition pd;
      systemPerspectives = appContext.getBeansOfType(PerspectiveDefinition.class);
      
      for (String key : systemPerspectives.keySet())
      {
         pd = systemPerspectives.get(key);
         if (isAuthorized(pd))
         {
            perspectives.put(key, PerspectiveAuthorizationProxy.newInstance(pd));
         }
      }
      
      // Process Collected Perspectives
      if ((null != perspectives) && (0 < perspectives.size()))
      {
         // Collect All Perspectives
         allPerspectives = new ArrayList<IPerspectiveDefinition>();
         for (IPerspectiveDefinition perspectiveDef : perspectives.values())
         {
            allPerspectives.add(perspectiveDef);
         }

         // Sort Perspectives
         Collections.sort(allPerspectives, new Comparator<IPerspectiveDefinition>()
         {
            public int compare(IPerspectiveDefinition arg0, IPerspectiveDefinition arg1)
            {
               // For time being till Authorization is not implemented for
               // Admin Perspective sort in reverse (descending) order
               // so that other users can login into portal
               return arg0.getLabel().compareTo(arg1.getLabel());
            }
         });

         initPerspectiveItems();
         recordConfiguredPermissions();
         
         if (null == currentPerspective)
         {
            // Show first perspective by default
            this.currentPerspective = allPerspectives.get(0);
            this.perspectiveController = new PerspectiveController(this);
         }
      }
      else
      {
         // If no applicable perspectives are found. Add Default Blank Perspective
         PerspectiveDefinition pdDefault = new PerspectiveDefinition();
         pdDefault.setName("NoPerspectiveDefined");
         perspectives.put(pdDefault.getName(), pdDefault);

         setPerspective(pdDefault);
      }
   }
   
   private void initPerspectiveItems()
   {
      this.perspectiveItems = new ArrayList<MenuItem>();

      Map<String, PerspectiveExtension> extensions = appContext.getBeansOfType(PerspectiveExtension.class);

      for (IPerspectiveDefinition perspective : allPerspectives)
      {
         if (null == currentPerspective && perspective.isDefaultPerspective())
         {
            // Set default perspective
            setPerspective(perspective);
         }

         perspectiveItems.add(createMenuItem(perspective));

         for (PerspectiveExtension extension : extensions.values())
         {
            //populate default Authorization
            authorizationProvider.addDefaultPermissions(extension.getName(), extension.getRequiredRolesSet(), true);
            authorizationProvider.addDefaultPermissions(extension.getName(), extension.getExcludeRolesSet(), false);

            if (isAuthorized(extension))
            {
               perspective.addExtension(extension);
            }
         }
      }
   }
   
   //store default permissions
   private void recordConfiguredPermissions()
   {
      IAuthorizationProvider authorizationProvider = getAuthorizationProvider();
      
      for (String key : systemPerspectives.keySet())
      {
         IPerspectiveDefinition perspective = systemPerspectives.get(key);
         authorizationProvider.addDefaultPermissions(perspective.getName(), perspective.getRequiredRolesSet(), true);
         authorizationProvider.addDefaultPermissions(perspective.getName(), perspective.getExcludeRolesSet(), false);

         // Launch Panels
         for (ViewDefinition view : perspective.getViews())
         {
            authorizationProvider.addDefaultPermissions(view.getName(), view.getRequiredRolesSet(), true);
            authorizationProvider.addDefaultPermissions(view.getName(), view.getExcludeRolesSet(), false);
         }

         // Views
         for (LaunchPanel lp : perspective.getLaunchPanels())
         {
            authorizationProvider.addDefaultPermissions(lp.getName(), lp.getRequiredRolesSet(), true);
            authorizationProvider.addDefaultPermissions(lp.getName(), lp.getExcludeRolesSet(), false);
         }
      }
   }
   
   /**
    * @param perspectiveId
    * @param params
    * @return
    */
   public boolean loadPerspective(String perspectiveId, Map<String, Object> params)
   {
      IPerspectiveDefinition perspectiveDef = getPerspective(perspectiveId);
      if (null != perspectiveDef)
      {
         setPerspective(perspectiveDef, params);
         return true;
      }
      else
      {
         return false;
      }
   }

   /**
    * @param perspectiveId
    * @return
    */
   public boolean loadPerspective(String perspectiveId)
   {
      return loadPerspective(perspectiveId, null);
   }

   public void setApplicationContext(ApplicationContext appContext) throws BeansException
   {
      this.appContext = appContext;
   }

   private MenuItem createMenuItem(IPerspectiveDefinition perspective)
   {
      MenuItem result = new MenuItem();
      result.setTitle(perspective.getLabel());
      result.setId(perspective.getName());

      String icon = null;
      if (null != perspective.getPreferences())
      {
         PreferencePage iconPreference = perspective.getPreferences().getPreference(PreferencesDefinition.PREF_ICON);
         if (null != iconPreference)
         {
            icon = iconPreference.getInclude();
         }
      }

      if (StringUtils.isEmpty(icon))
      {
         icon = "pi pi-perspective-default"; // Default Icon
      }

      // Check if icon is image URL or a css class. It's mapping with MenuItem -
      // - Icon property = Image URL
      // - Value Property = CSS Class
      if (icon.contains("/") && icon.contains(".")) // It's an image URL
      {
         result.setIcon(icon);
         result.setValue(perspective.getName() + " " + PortalApplication.deriveIconClass(icon));
      }
      else
      {
         result.setLink(null);
         result.setIcon("");
         result.setValue(perspective.getName() + " " + icon);
      }

      return result;
   }

   private ViewDefinition lookupViewID(String viewId)
   {
      for (IPerspectiveDefinition perspective : perspectives.values())
      {

         for (ViewDefinition viewDef : perspective.getViews())
         {
            if (viewDef.getName().equals(viewId))
            {
               return viewDef;
            }
         }
      }
      return null;
   }
   
   /**
    * @param viewId
    * @return
    */
   public boolean isViewAvailable(String viewId)
   {
      return null != lookupViewID(viewId);
   }

   /**
    * @return
    */
   public List<ViewDefinition> getDeclaredViews()
   {
      List<ViewDefinition> declaredViews = new ArrayList<ViewDefinition>();
      for (IPerspectiveDefinition perspective : perspectives.values())
      {
         declaredViews.addAll(perspective.getViews());
      }

      return declaredViews;
   }

   public List<MenuItem> getPerspectiveItems()
   {
      return perspectiveItems;
   }

   public List<IPerspectiveDefinition> getAllPerspectives()
   {
      return allPerspectives;
   }

   /**
    *
    * @param ae
    */
   public void perspectiveChangeAction(ActionEvent ae) throws AbortProcessingException
   {
      FacesContext context = FacesContext.getCurrentInstance();
      Map<String, String> params = context.getExternalContext().getRequestParameterMap();
      
      String perspectiveId = params.get("perspectiveId");
      processPerspectiveChange(perspectiveId);
   }

   /**
    * @param perspectiveId
    * @throws AbortProcessingException
    */
   private void processPerspectiveChange(String perspectiveId) throws AbortProcessingException
   {
      // Avoiding accessing PortalApplication statically in this class
      // So using bean name and get bean from context directly
      PortalApplication portalApplication = (PortalApplication) FacesUtils.getBeanFromContext("ippPortalApp");

      for (IPerspectiveDefinition perspective : perspectives.values())
      {
         if (areEqual(perspectiveId, perspective.getName()) && (currentPerspective != perspective))
         {
            setPerspective(perspective);

           if (portalApplication.isPinViewOpened() && null != portalApplication.getPinView())
            {
               broadcastNonVetoableViewEvent(portalApplication.getPinView(), ViewEventType.PERSPECTIVE_CHANGED);
            }
            break;
         }
      }
   }

   /**
    * @param newPerspective
    * @param params
    */
   private void setPerspective(IPerspectiveDefinition newPerspective, Map<String, Object> params)
   {
      IPerspectiveDefinition previousPerspective = this.currentPerspective;
      if (null != previousPerspective && !previousPerspective.getName().equals(newPerspective.getName()))
      {
         broadcastNonVetoablePerspectiveEvent(previousPerspective, PerspectiveEventType.DEACTIVATED);
      }

      this.currentPerspective = newPerspective;
      this.perspectiveController = new PerspectiveController(this);

      broadcastNonVetoablePerspectiveEvent(currentPerspective, PerspectiveEventType.ACTIVATED, params);

      // Fire View Event
      broadcastNonVetoableViewEvent(focusView, ViewEventType.PERSPECTIVE_CHANGED);
   }

   /**
    * @param newPerspective
    */
   private void setPerspective(IPerspectiveDefinition newPerspective)
   {
      setPerspective(newPerspective, null);
   }

   // *********************** View management **********************

   public View getFocusView()
   {
      return focusView;
   }

   public boolean setFocusView(View newFocusView)
   {
      if (getFocusView() == newFocusView)
      {
         return true;
      }

      if (null != getFocusView() && ViewState.CLOSED != getFocusView().getViewState())
      {
         View oldFocusView = getFocusView();

         boolean success = broadcastVetoableViewEvent(oldFocusView, ViewEventType.TO_BE_DEACTIVATED);
         if(!success)
         {
            return false;
         }

         oldFocusView.setSelected(false);
         oldFocusView.setViewState(ViewState.INACTIVE);
         this.focusView = null;
         
         broadcastNonVetoableViewEvent(oldFocusView, ViewEventType.DEACTIVATED);
      }

      if (null != newFocusView && ViewState.CLOSED != newFocusView.getViewState())
      {
         boolean success = broadcastVetoableViewEvent(newFocusView, ViewEventType.TO_BE_ACTIVATED);
         if(!success)
         {
            return false;
         }

         this.focusView = newFocusView;
         newFocusView.setViewState(ViewState.ACTIVE);
         newFocusView.setSelected(true);

         broadcastNonVetoableViewEvent(newFocusView, ViewEventType.ACTIVATED);
      }
      else
      {
         this.focusView = null;
      }
      
      return true;
   }

   public View getActiveView()
   {
      return activeView;
   }

   public void setActiveView(View activeView)
   {
      this.activeView = activeView;
   }

   public List<View> getOpenViews()
   {
      return openViews;
   }

   public View findView(String identityUrl)
   {
      return views.get(identityUrl);
   }
   
   /**
    * @param viewId
    * @param viewKey
    * @return
    */
   public View findView(String viewId, String viewKey)
   {
      ViewDefinition viewDefinition = lookupViewID(viewId);
      if (null != viewDefinition)
      {
         return findView(viewDefinition, viewKey);
      }

      return null;
   }
   
   /**
    * @param viewDefinition
    * @param viewKey
    * @return
    */
   public View findView(ViewDefinition viewDefinition, String viewKey)
   {
      return views.get(View.createURL(viewDefinition, viewKey));
   }

   /**
    * @param viewId
    * @param viewParams
    * @param msgBean
    * @param nested
    * @return
    */
   public View openViewById(String viewId, Map<String, Object> viewParams, AbstractMessageBean msgBean, boolean nested)
   {
      ViewDefinition viewDefinition = lookupViewID(viewId);
      if (null == viewDefinition)
      {
         MessageDialog.addErrorMessage(MessagePropertiesBean.getInstance().getString(
               "portalFramework.error.viewNotAvailable"));
         return null;
      }

      String viewKey = View.getViewIdentityParams(viewDefinition, viewParams);
      return openViewById(viewId, viewKey, viewParams, msgBean, nested);
   }

   /**
    * Used to open View from Java Bean
    * 
    * @param viewId
    *           like specified in <ippui:view> (name attribute)
    * @param viewKey
    *           must be unique to open distinctive Views of the same viewId type. e.g.
    *           "oid=10".
    * @param viewParams
    *           additional parameters for initialization of the Views beans
    * @param msgBean
    *           Properties bean to pick View's Label and Description
    * @param nestedView
    * @return the created <code>View</code>
    */
   public View openViewById(String viewId, String viewKey,
         Map<String, Object> viewParams, AbstractMessageBean msgBean, boolean nested)
   {
      
      ViewDefinition viewDefinition = lookupViewID(viewId);

      if (null == viewDefinition)
      {
         MessageDialog.addErrorMessage(MessagePropertiesBean.getInstance().getString(
         "portalFramework.error.viewNotAvailable"));
         return null;
      }

      return openView(viewDefinition, viewKey, viewParams, msgBean, nested);
   }
   
   public View getViewById(String viewId, String viewKey)
   {
      ViewDefinition viewDef = lookupViewID(viewId);
      if(viewDef != null)
      {
         String viewUrl = View.createURL(viewDef, viewKey);
         return views.get(viewUrl);
      }
      
      return null;
   }
   
   /**
    * @param viewUrl
    * @return
    */
   public View getViewByUrl(String viewUrl)
   {
      return views.get(viewUrl);
   }   

   public View openView(ViewDefinition viewDef, String viewKey,
         Map<String, Object> params, AbstractMessageBean msgBean, boolean nestedView)
   {
      // For backward compatibility
      // If View Key is empty then only calculate it 
      if (StringUtils.isEmpty(viewKey))
      {
         viewKey = View.getViewIdentityParams(viewDef, params);
      }  

      String viewUrl = View.createURL(viewDef, viewKey);

      if (trace.isDebugEnabled())
      {
         trace.debug(("openView: viewUrl=" + viewUrl + ", viewDef=" + viewDef + ", msgBean=" + msgBean));
      }
      
      View view = views.get(viewUrl);

      boolean newView = false;
      if (null == view)
      {
         view = createView(viewDef, viewKey, params, msgBean, nestedView);
         if (null == view)
         {
            return null; // create was not successful
         }
         openViews.add(view);
         newView = true;
      }
      else
      {
         if ( !openViews.contains(view))
         {
            openViews.add(view);
         }
      }

      // replace map
      if (view != null && (params != null && !params.isEmpty()) && params.get("doNotCopyParams") == null)
      {
         view.setViewParams(params);
      }

      if(setFocusView(view))
      {
         return view;
      }
      else
      {
         if(newView)
         {
            openViews.remove(view);
            views.remove(view);
         }
      }

      return null;
   }

   @SuppressWarnings("unchecked")
   public View openView()
   {
      try
      {
         FacesContext context = FacesContext.getCurrentInstance();
         Map<String, String> params = context.getExternalContext().getRequestParameterMap();
   
         String msgBeanName = (String) params.get("msgBean");
         AbstractMessageBean msgBean = !isEmpty(msgBeanName) ? (AbstractMessageBean) FacesUtils
               .getBeanFromContext(msgBeanName) : null;
   
         String nested = (String) params.get("nested");
         boolean nestedView = isEmpty(nested) ? false : Boolean.parseBoolean(nested);
   
         String viewId = params.get("viewId");
         String viewKey = params.get("viewIdentity");
         
         // Required, as params are returning Literal null instead of NULL
         if("null".equalsIgnoreCase(viewKey))
         {
            viewKey = null;
         }

         if (StringUtils.isNotEmpty(viewId))
         {
            ViewDefinition viewDefinition = lookupViewID(viewId);

            if (null == viewDefinition)
            {
               MessageDialog.addErrorMessage(MessagePropertiesBean.getInstance().getString(
               "portalFramework.error.viewNotAvailable"));
               return null;
            }

            // Default createView = true
            String createView = params.get("createView");
            boolean create = isEmpty(createView) ? true : Boolean.parseBoolean(createView);
            
            if (!create && null == findView(viewId, viewKey))
            {
               MessageDialog.addErrorMessage(MessagePropertiesBean.getInstance().getParamString(
                     "portalFramework.error.viewAlreadyClosed", viewId));
               return null;
            }
            
            String viewParams = params.get("viewParams");
            
            return openViewById(viewId, viewKey, View.parseParams(viewParams), msgBean, nestedView);
         }
   
         MessageDialog.addErrorMessage(MessagePropertiesBean.getInstance().getString(
               "portalFramework.error.viewIdEmpty"));

         return null;
      }
      catch(Exception e)
      {
         MessageDialog.addErrorMessage(MessagePropertiesBean.getInstance().getString(
               "portalFramework.error.viewAlreadyClosed"), e);
         return null;
      }
   }

   public View openView(String viewUrl, AbstractMessageBean msgBean)
   {
      return openView(null, viewUrl, msgBean, false);
   }

   public View openView(String viewUrl, AbstractMessageBean msgBean, boolean nestedView)
   {
      return openView(null, viewUrl, msgBean, nestedView);
   }

   public View openView(ViewDefinition viewDef, String viewUrl,
         AbstractMessageBean msgBean)
   {
      return openView(viewDef, viewUrl, msgBean, false);
   }

   public View openView(ViewDefinition viewDef, String viewUrl,
         AbstractMessageBean msgBean, boolean nestedView)
   {
      if (trace.isDebugEnabled())
      {
         trace.debug(("openView: viewUrl=" + viewUrl + ", viewDef=" + viewDef + ", msgBean=" + msgBean));
      }
      
      View view = views.get(viewUrl);

      boolean newView = false;
      if (null == view)
      {
         view = createView(viewDef, viewUrl, msgBean, nestedView);
         
         if (null == view)
         {
            // create was not successful
            return null;
         }
         
         openViews.add(view);
         newView = true;
      }
      else
      {
         if ( !openViews.contains(view))
         {
            openViews.add(view);
         }
      }

      if(setFocusView(view))
      {
         return view;
      }
      else
      {
         if(newView)
         {
            openViews.remove(view);
            views.remove(view);
         }
      }

      return null;
   }

   /**
    * @param viewDef
    * @param url
    * @param messageBean
    * @return
    */
   public View createView(ViewDefinition viewDef, String url,
         AbstractMessageBean messageBean)
   {
      return createView(viewDef, url, messageBean, false);
   }

   /**
    * @param viewDef
    * @param url
    * @param messageBean
    * @param nestedView
    * @return
    */
   public View createView(ViewDefinition viewDef, String url,
         AbstractMessageBean messageBean, boolean nestedView)
   {
      if (trace.isDebugEnabled())
      {
         trace.debug(("createView: Creating View: url=" + url + ", viewDef=" + viewDef + ", nestedView" + nestedView));
      }

      View view = new View(viewDef, url, messageBean);
      setViewIcon(view);
      
      String viewKeyUrl = View.createURL(viewDef, view.getIdentityParams());
      views.put(viewKeyUrl, view);
      recreatableViews.put(viewKeyUrl, view);
      
      view.setViewState(ViewState.CREATED);
      if (broadcastVetoableViewEvent(view, ViewEventType.CREATED))
      {
         // resolve again to reflect changes to view params during create
         view.resolveLabelAndDescription();
         
         // Set View's Nesting Dependency
         if (nestedView)
            view.setOpenerView(getActiveView());
      }
      else
      {
         views.remove(viewKeyUrl);
         recreatableViews.remove(viewKeyUrl);
         view = null;
      }

      return view;
   }

   /**
    * @param viewId
    * @param viewKey
    * @param viewParams
    * @param msgBean
    * @param nested
    * @return
    */
   public View createView(String viewId, String viewKey,
         Map<String, Object> viewParams, AbstractMessageBean messageBean, boolean nestedView)
   {
      ViewDefinition viewDefinition = lookupViewID(viewId);

      if (null == viewDefinition)
         return null;
     
      return createView(viewDefinition, viewKey, viewParams, messageBean, nestedView);
   }

   /**
    * @param viewDef
    * @param url
    * @param messageBean
    * @param nestedView
    * @return
    */
   public View createView(ViewDefinition viewDef, String viewKey,
         Map<String, Object> viewParams, AbstractMessageBean messageBean,
         boolean nestedView)
   {
      String url = View.createURL(viewDef, viewKey);

      if (trace.isDebugEnabled())
      {
         trace.debug(("createView: Creating View: url=" + url + ", viewDef=" + viewDef 
               + ", nestedView" + nestedView));
      }

      View view = new View(viewDef, viewKey, viewParams, messageBean);
      setViewIcon(view);
      views.put(url, view);
      if(viewParams == null || viewParams.size() == 0)
      {
         recreatableViews.put(url, view);
      }

      view.setViewState(ViewState.CREATED);
      if (broadcastVetoableViewEvent(view, ViewEventType.CREATED))
      {
         // resolve again to reflect changes to view params during create
         view.resolveLabelAndDescription();
         
         // Set View's Nesting Dependency
         if (nestedView)
            view.setOpenerView(getActiveView());
      }
      else
      {
         views.remove(url);
         recreatableViews.remove(url);
         view = null;
      }
      
      return view;
   }

   /**
    * @param view
    * @param newViewKey
    */
   public void updateViewKey(View view, String newViewKey)
   {  
      if (views.containsKey(view.getUrl()))
      {
         views.remove(view.getUrl());

         view.updateViewKey(newViewKey);
         views.put(view.getIdentityUrl(), view);
      }
   }
   
   private void setViewIcon(View view)
   {
      if(view.getDefinition() != null && view.getDefinition().getPreferences() != null)
      {
         PreferencePage prefPage = view.getDefinition().getPreferences().getPreference(
               PreferencesDefinition.PREF_ICON);
         if(prefPage != null)
            view.setIcon(prefPage.getInclude());
      }
   }
   
   public void selectView()
   {
      try
      {
         FacesContext context = FacesContext.getCurrentInstance();
         /* cast is necessary for JSF 1.1 */
         String viewUrl = (String)context.getExternalContext().getRequestParameterMap().get(
               "viewUrl");

         setFocusView(views.get(viewUrl));

         if ( !openViews.contains(getActiveView()))
         {
            openViews.add(getActiveView());
         }
      }
      catch (Exception exception)
      {
         // MessageDialog.open(exception);
      }
   }

   /**
    * Closes the Focus view from TabSet
    */
   public boolean closeFocusView()
   {
      View focusView = getFocusView();
      View openerView = focusView.getOpenerView();
      
      if(closeView(focusView))
      {
         // Activate Next View
         if (openerView != null && getOpenViews().contains(openerView))
         {
            setFocusView(openerView);
         }
         else if ( !isEmpty(getOpenViews()))
         {
            setFocusView(getOpenViews().get(0));
         }
         
         return true;
      }

      return false;
   }

   /**
    * Closes Any view
    * @return
    */
   public View closeView()
   {
      FacesContext context = FacesContext.getCurrentInstance();
      /* cast is necessary for JSF 1.1 */
      String identityUrl = (String) context.getExternalContext().getRequestParameterMap().get("identityUrl");

      View closeView = views.get(identityUrl);

      boolean wasClosed = false;
      if (null != closeView)
      {
         if (getFocusView() == closeView)
         {
            wasClosed = closeFocusView();
         }
         else
         {
            wasClosed = closeView(closeView);
         }
      }
      else
      {
         MessageDialog.addErrorMessage(MessagePropertiesBean.getInstance().getParamString(
               "portalFramework.error.viewIdentityUrlInvalid", identityUrl));
      }

      return wasClosed ? closeView : null;
   }

   public boolean closeView(View view)
   {
      return closeView(view, false);
   }

   public boolean closeView(View view, boolean force)
   {
      if ( !broadcastVetoableViewEvent(view, ViewEventType.TO_BE_CLOSED))
      {
         if ( !force)
         {
            String eventScript = "parent.BridgeUtils.View.syncActiveView(true);";
            PortalApplicationEventScript.getInstance().addEventScript(eventScript);
            // close command was vetoed
            return false;
         }
      }
      
      getOpenViews().remove(view);
      view.setViewState(ViewState.CLOSED);

      if (getFocusView() == view)
      {
         setFocusView(null);
      }
      
      broadcastVetoableViewEvent(view, ViewEventType.CLOSED);
      
      // closed event must be sent before actually destroying the view, as all view scoped
      // beans will be destroyed afterwards, too
      view.destroy();
      
      for (String viewUrl : views.keySet())
      {
         if(views.get(viewUrl) == view)
         {
            views.remove(viewUrl);
            break;
         }
      }
      
      return true;
   }

   /**
    * @param view
    * @param recursive
    * @return
    */
   public List<View> getChildViews(View parentView, boolean recursive)
   {
      List<View> childViews = new ArrayList<View>();
      View opener;
      for (View view : views.values())
      {
         opener = view.getOpenerView();
         while (opener != null)
         {
            if (opener.getUrl().equals(parentView.getUrl()))
            {
               childViews.add(view);
               break;
            }
            
            if(recursive)
            {
               opener = opener.getOpenerView();
            }
            else
            {
               break;
            }
         }
      }

      return childViews;
   }

   public Map<String, IPerspectiveDefinition> getPerspectives()
   {
      return perspectives;
   }
   
   public Map<String, PerspectiveDefinition> getSystemPerspectives()
   {
   return systemPerspectives;
   }
   
   
   /**
    * @param name
    * @return
    */
   public LaunchPanel getLaunchPanel(String name)
   {
      LaunchPanel launchPanel = null;
      
      for (IPerspectiveDefinition perspective : perspectives.values())
      {
         launchPanel = perspective.getLaunchPanel(name);
         if(launchPanel != null)
            break;
      }
      
      return launchPanel;
   }
   
   /**
    * @param name
    * @return
    */
   public MenuSection getMenuSection(String name)
   {
      MenuSection menuSection = null;
      
      for (IPerspectiveDefinition perspective : perspectives.values())
      {
         menuSection = perspective.getMenuSection(name);
         if(menuSection != null)
            break;
      }
      
      return menuSection;
   }
   
   /**
    * @param name
    * @return
    */
   public ToolbarSection getToolbarSection(String name)
   {
      ToolbarSection toolbarSection = null;
      
      for (IPerspectiveDefinition perspective : perspectives.values())
      {
         toolbarSection = perspective.getToolbarSection(name);
         if(toolbarSection != null)
            break;
      }
      
      return toolbarSection;
   }

   /**
    * @param name
    * @return
    */
   public ViewDefinition getViewDefinition(String name)
   {
      ViewDefinition viewDefinition = null;
      
      for (IPerspectiveDefinition perspective : perspectives.values())
      {
         viewDefinition = perspective.getViewDefinition(name);
         if(viewDefinition != null)
            break;
      }
      
      return viewDefinition;
   }

   public void broadcastNonVetoableViewEvent(View view, ViewEventType eventType)
   {
      if (null != view)
      {
         ViewEventHandler handler = resolveViewController(view, ViewEventHandler.class);
         
         if (null != handler)
         {
            ViewEvent event = new ViewEvent(view, eventType, false);
            if (trace.isDebugEnabled())
            {
               trace.debug("Triggering View Event: " + eventType + ", for View: " + view.getViewId());
            }
            handler.handleEvent(event);
         }
      }
   }
   
   public boolean broadcastVetoableViewEvent(View view, ViewEventType eventType)
   {
      if (null != view)
      {
         ViewEventHandler handler = resolveViewController(view, ViewEventHandler.class);
         
         if (null != handler)
         {
            ViewEvent event = new ViewEvent(view, eventType);
            if (trace.isDebugEnabled())
            {
               trace.debug("Triggering View Event: " + eventType + ", for View: " + view.getViewId());
            }
            handler.handleEvent(event);
            
            if (event.isVetoed())
            {
               return false;
            }
         }
      }
      
      return true;
   }

   protected <I> I resolveViewController(View view, Class<I> clazz)
   {
      if ((null != view.getDefinition())
            && !isEmpty(view.getDefinition().getController()))
      {
         Object handler = TabScopeUtils.resolveBean(
               view.getDefinition().getController(), view);

         if (clazz.isInstance(handler))
         {
            return clazz.cast(handler);
         }
         else if (trace.isDebugEnabled())
         {
            trace.debug("View controller bean " + view.getDefinition().getController()
                  + " is not an instance of " + clazz.getName());
         }
      }

      return null;
   }

   public void broadcastNonVetoablePerspectiveEvent(IPerspectiveDefinition perspective, PerspectiveEventType eventType, Map<String, Object> params)
   {
      if (null != perspective)
      {
         PerspectiveEventHandler handler = resolvePerspectiveController(perspective, PerspectiveEventHandler.class);
         
         if (null != handler)
         {
            PerspectiveEvent event = new PerspectiveEvent(perspective, eventType, params);
            if (trace.isDebugEnabled())
            {
               trace.debug("Triggering Perspective Event: " + eventType + ", for Perspective: " + perspective.getName());
            }
            handler.handleEvent(event);
         }
      }
   }
   
   public void broadcastNonVetoablePerspectiveEvent(IPerspectiveDefinition perspective, PerspectiveEventType eventType)
   {
      broadcastNonVetoablePerspectiveEvent(perspective, eventType, null);
   }
   
   /**
    * Broadcasts events for the "current perspective".
    * 
    * @param eventType
    */
   public void broadcastNonVetoablePerspectiveEvent(PerspectiveEventType eventType)
   {
      broadcastNonVetoablePerspectiveEvent(currentPerspective, eventType);
   }

   protected <I> I resolvePerspectiveController(IPerspectiveDefinition perspective, Class<I> clazz)
   {
      if ((null != perspective) && !isEmpty(perspective.getController()))
      {
         FacesContext fc = FacesContext.getCurrentInstance();
         ExternalContext externalContext = fc.getExternalContext();
         if (externalContext instanceof ServletExternalContext)
         {
            Object handler = FacesUtils.getBeanFromContext(fc, perspective.getController());
   
            if (clazz.isInstance(handler))
            {
               return clazz.cast(handler);
            }
            else if (trace.isDebugEnabled())
            {
               trace.debug("Perspective controller bean " + perspective.getController()
                     + " is not an instance of " + clazz.getName());
            }
         }
         else
         {
            trace.warn("Portlets are not yet supported.");
         }
      }

      return null;
   }

   /**
    * @param view
    * @param handler
    */
   public void registerViewDataEventHandler(View view, ViewDataEventHandler handler)
   {
      List<ViewDataEventHandler> handlers = viewDataEventHandlers.get(view);
      if (CollectionUtils.isEmpty(handlers))
      {
         handlers = new ArrayList<ViewDataEventHandler>();
         viewDataEventHandlers.put(view, handlers);
      }
      handlers.add(handler);
   }

   /**
    * @param view
    * @param handler
    */
   public void unregisterViewDataEventHandler(View view, ViewDataEventHandler handler)
   {
      List<ViewDataEventHandler> handlers = viewDataEventHandlers.get(view);
      if (!CollectionUtils.isEmpty(handlers))
      {
         // Remove Current Handler
         handlers.remove(handler);
         
         // If it's the last one, remove the View itself
         if (CollectionUtils.isEmpty(handlers))
         {
            viewDataEventHandlers.remove(view);
         }
      }
   }

   /**
    * @param event
    */
   public void broadcastViewDataEvent(ViewDataEvent event)
   {
      List<ViewDataEventHandler> handlers = viewDataEventHandlers.get(event.getView());
      if (!CollectionUtils.isEmpty(handlers))
      {
         for (ViewDataEventHandler handler : handlers)
         {
            try
            {
               handler.handleEvent(event);
            }
            catch (Exception e)
            {
               trace.error("Handler is unable to handle ViewDataEvent. Handler = " + handler, e);
            }
         }
      }
   }

   /**
    * @param out
    * @throws IOException
    */
   private void writeObject(ObjectOutputStream out) throws IOException
   {
      out.defaultWriteObject();
   }

   /**
    * @param in
    * @throws IOException
    * @throws ClassNotFoundException
    */
   private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
   {
      in.defaultReadObject();
      // handle transient variables
      initPerspectiveItems();
      this.viewDataEventHandlers = new HashMap<View, List<ViewDataEventHandler>>();
   }
   
   /**
    * @param userProvider the userProvider to set
    */
   public void setUserProvider(UserProvider userProvider)
   {
      this.userProvider = userProvider;
   }

   /**
    * @param commonMenuProvider Implementation class providing the Common Menu 
    */
   public void setCommonMenuProvider(CommonMenuProvider commonMenuProvider)
   {
      this.commonMenuIframeHandler = new CommonMenuIframeHandler(commonMenuProvider);
   }
   
   /**
    * @param commonMenuProvider Implementation class providing the Common Menu 
    */
   public CommonMenuIframeHandler getCommonMenuIframeHandler()
   {
      return this.commonMenuIframeHandler;
   }

   /**
    * @param perspectiveDef
    * @return
    * 
    */
   private boolean isAuthorized(PerspectiveDefinition perspectiveDef)
   {
      Boolean isAuthorized = isAuthorized_(perspectiveDef.getName());

      if (null != isAuthorized)
      {
         return isAuthorized;
      }
      else
      {
         return UserUtils.isAuthorized(userProvider.getUser(), perspectiveDef.getRequiredRolesSet(),
               perspectiveDef.getExcludeRolesSet());
      }
   }

   /**
    * @param perspectiveExt
    * @return
    */
   private boolean isAuthorized(PerspectiveExtension perspectiveExt)
   {
      Boolean isAuthorized = isAuthorized_(perspectiveExt.getName());

      if (null != isAuthorized)
      {
         return isAuthorized;
      }
      else
      {
         return UserUtils.isAuthorized(userProvider.getUser(), perspectiveExt.getRequiredRolesSet(),
               perspectiveExt.getExcludeRolesSet());
      }
   }

   /**
    * 
    * @param uiElement
    * @return
    */
   public boolean isAuthorized(UiElementWithPermissions uiElement)
   {
      Boolean isAuthorized = isAuthorized_(uiElement.getName());

      if (null != isAuthorized)
      {
         return isAuthorized;
      }
      else
      {
         return UserUtils.isAuthorized(userProvider.getUser(), uiElement.getRequiredRolesSet(),
               uiElement.getExcludeRolesSet());
      }
   }

   /**
    * (Portal UI Authorization)
    * @param elementName
    * @return
    */
   private Boolean isAuthorized_(String elementName)
   {
      IAuthorizationProvider authorizationProvider = getAuthorizationProvider();
      Boolean isAuthorized = null;

      if (null != authorizationProvider)
      {
         isAuthorized = authorizationProvider.isAuthorized(userProvider.getUser(), elementName);
      }

      return isAuthorized;
   }

   private IAuthorizationProvider getAuthorizationProvider()
   {
      if (!authorizationProviderInitialized)
      {
         authorizationProviderInitialized = true;
         authorizationProvider = getAuthorizationProvider_();
      }
      return authorizationProvider;
   }

   /**
    * NOTE: Do not use this method directly, use the instance from PortalApplication
    * 
    * @return AuthorizationProvider
    * @author Yogesh.Manware
    */
   private static IAuthorizationProvider getAuthorizationProvider_()
   {
      Iterator<IAuthorizationProvider.Factory> serviceProviders = ServiceLoaderUtils
            .searchProviders(IAuthorizationProvider.Factory.class);

      IAuthorizationProvider.Factory factory = null;

      if (null != serviceProviders)
      {
         while (serviceProviders.hasNext())
         {
            factory = serviceProviders.next();
            if (null != factory)
            {
               return factory.getAuthorizationProvider();
            }
         }
      }
      return null;
   }
}
