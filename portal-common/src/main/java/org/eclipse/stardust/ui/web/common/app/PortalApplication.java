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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.eclipse.stardust.ui.web.common.Constants;
import org.eclipse.stardust.ui.web.common.PreferencePage;
import org.eclipse.stardust.ui.web.common.PreferencesDefinition;
import org.eclipse.stardust.ui.web.common.ViewDefinition;
import org.eclipse.stardust.ui.web.common.app.View.ViewState;
import org.eclipse.stardust.ui.web.common.configuration.ConfigurationConstants;
import org.eclipse.stardust.ui.web.common.configuration.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.common.configuration.UserPreferencesHelper;
import org.eclipse.stardust.ui.web.common.event.PerspectiveEvent.PerspectiveEventType;
import org.eclipse.stardust.ui.web.common.event.ViewDataEvent;
import org.eclipse.stardust.ui.web.common.event.ViewDataEventHandler;
import org.eclipse.stardust.ui.web.common.event.ViewEvent.ViewEventType;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.message.MessageDialog;
import org.eclipse.stardust.ui.web.common.spi.env.RuntimeEnvironmentInfoProvider;
import org.eclipse.stardust.ui.web.common.spi.user.User;
import org.eclipse.stardust.ui.web.common.spi.user.UserProvider;
import org.eclipse.stardust.ui.web.common.util.AbstractMessageBean;
import org.eclipse.stardust.ui.web.common.util.DateUtils;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.common.util.MessagePropertiesBean;
import org.eclipse.stardust.ui.web.common.util.SessionRendererHelper;
import org.eclipse.stardust.ui.web.common.util.StringUtils;
import org.springframework.beans.factory.InitializingBean;

import com.icesoft.faces.component.paneltabset.TabChangeEvent;
import com.icesoft.faces.component.paneltabset.TabChangeListener;
import com.icesoft.faces.context.effects.JavascriptContext;


/**
 * @author Subodh.Godbole
 *
 */
public class PortalApplication
      implements TabChangeListener, UserPreferencesEntries, ConfigurationConstants, Serializable, InitializingBean,
      Constants
{
   private static final long serialVersionUID = 1L;

   public static final String BEAN_NAME = "ippPortalApp";

   private static final Logger trace = LogManager.getLogger(PortalApplication.class);
 
   public static final int TAB_DISPLAY_MODE = 0;

   public static final int GRID_DISPLAY_MODE = 1;

   public static final int VERTICAL_SPLIT_DISPLAY_MODE = 2;

   public static final int HORIZONTAL_SPLIT_DISPLAY_MODE = 3;
   
   public static final String PIN_VIEW_MODE_VERTICAL = "vertical";

   public static final String PIN_VIEW_MODE_HORIZONTAL = "horizontal";

   private PortalUiController portalUiController;

   private boolean fullScreenModeActivated;

   private boolean launchPanelsActivated;

   private int displayMode;

   private int viewIndex;

   private List<View> activeViewBreadCrumb;

   private List<View> displayedViews;
   
   private List<View> overflowedViews;

   private View dummyViewForOverflowTab;

   private boolean showOverflowTabs;

   private int maxTabDisplay = -1;
   
   private boolean pinViewOpened = false;

   private String pinViewOrientation;

   private View pinView;
   
   private ArrayList<View> focusViewStack = new ArrayList<View>();
   
   private List<String> skinFiles;
   
   // Only one view can be in focus i.e. active in entire portal
   // This view can exist anywhere - TabSet, PinView
   private View activeView;

   private UserProvider userProvider;
   
   private PortalApplicationEventScript portalApplicationEventScript; 
   
   private TimeZone clientTimeZone = null;
   private boolean overflowTabPopupOpened = false;

   private RuntimeEnvironmentInfoProvider runtimeEnvironmentInfoProvider;
   private String version;
   private String copyrightMessage;

   private String logoutUri;

   private boolean pageRefreshOn = false;
   /**
    *
    */
   public PortalApplication()
   {
   }

   /**
    * @return
    */
   public static PortalApplication getInstance()
   {
      return (PortalApplication) FacesUtils.getBeanFromContext("ippPortalApp");
   }
   
   /**
    * @throws Exception
    */
   public void afterPropertiesSet() throws Exception
   {
      launchPanelsActivated = true;
      fullScreenModeActivated = false;
      displayMode = TAB_DISPLAY_MODE;

      dummyViewForOverflowTab = new View(null, "/dummy.xhtml");
      
      refreshSkin();

      loadPerspective();

      retrieveRuntimeEnvInfo();
      
      logoutUri = (String) FacesContext.getCurrentInstance().getExternalContext()
            .getInitParameter(CONTEXT_PARAM_LOGOUT_URI);
      
      SessionRendererHelper.addCurrentSession(SessionRendererHelper.getPortalSessionRendererId(getLoggedInUser()));
   }

   /**
    * @return
    */
   public User getLoggedInUser()
   {
      return  userProvider.getUser();
   }
   
   /**
    * @return
    */
   public PreferencePage getHelpDocPreference()
   {
      if(getPortalUiController().getPerspective().getPreferences() != null)
      {
         return getPortalUiController().getPerspective().getPreferences().getPreference(
               PreferencesDefinition.PREF_HELP_DOCUMENTATION);
      }

      return null;
   }
   
   /**
    * @return
    */
   public boolean isLaunchPanelsActivated()
   {
      return launchPanelsActivated;
   }

   /**
    *
    */
   public void activateLaunchPanels()
   {
      if (!isLaunchPanelsActivated())
      {
         launchPanelsActivated = true;

         closeOverflowTabIframePopup();

         getPortalUiController().broadcastVetoableViewEvent(getFocusView(),
               ViewEventType.LAUNCH_PANELS_ACTIVATED);

         getPortalUiController().broadcastNonVetoablePerspectiveEvent(PerspectiveEventType.LAUNCH_PANELS_ACTIVATED);

         if (isPinViewOpened())
         {
            getPortalUiController().broadcastVetoableViewEvent(getPinView(),
                  ViewEventType.LAUNCH_PANELS_ACTIVATED);
         }
      }
   }

   /**
    *
    */
   public void deactivateLaunchPanels()
   {
      if (isLaunchPanelsActivated())
      {
         launchPanelsActivated = false;

         closeOverflowTabIframePopup();

         getPortalUiController().broadcastVetoableViewEvent(getFocusView(),
               ViewEventType.LAUNCH_PANELS_DEACTIVATED);

         getPortalUiController().broadcastNonVetoablePerspectiveEvent(PerspectiveEventType.LAUNCH_PANELS_DEACTIVATED);

         if (isPinViewOpened())
         {
            getPortalUiController().broadcastVetoableViewEvent(getPinView(),
                  ViewEventType.LAUNCH_PANELS_DEACTIVATED);
         }
      }
   }

   /**
    *
    */
   public void activateFullScreenMode()
   {
      if (!isFullScreenModeActivated())
      {
         getPortalUiController().broadcastNonVetoablePerspectiveEvent(PerspectiveEventType.LAUNCH_PANELS_DEACTIVATED);

         closeOverflowTabIframePopup();

         boolean success = getPortalUiController().broadcastVetoableViewEvent(getFocusView(),
               ViewEventType.TO_BE_FULL_SCREENED);
         if(!success)
         {
            return;
         }
   
         FacesContext context = FacesContext.getCurrentInstance();
         String identityUrl = (String) context.getExternalContext().getRequestParameterMap().get("identityUrl");
         
         View newView;
         if (StringUtils.isNotEmpty(identityUrl))
         {
            newView = getPortalUiController().findView(identityUrl);
         }
         else
         {
            newView = getActiveView();
         }
         
         if (null != newView)
         {
            getPortalUiController().setFocusView(newView);
            setActiveView(newView);
      
            fullScreenModeActivated = true;

            // Vetoed not processed here. This is FYI only event,
            // As this view already returned success for TO_BE_FULL_SCREENED
            getPortalUiController().broadcastNonVetoableViewEvent(getFocusView(),
                  ViewEventType.FULL_SCREENED);
      
            // Required to refresh page
            FacesUtils.refreshPage(true);
         }
      }
   }

   /**
    * @return
    */
   public boolean isFullScreenModeActivated()
   {
      return fullScreenModeActivated;
   }

   /**
    *
    */
   public void deactivateFullScreenMode()
   {
      if (isFullScreenModeActivated())
      {
         // broadcast activation events
         setFocusView(getFocusView());

         closeOverflowTabIframePopup();

         boolean success = getPortalUiController().broadcastVetoableViewEvent(getFocusView(),
               ViewEventType.TO_BE_RESTORED_TO_NORMAL);
         if(!success)
         {
            return;
         }
         
         fullScreenModeActivated = false;
         
         // Vetoed not processed here. This is FYI only event,
         // As this view already returned success for TO_BE_FULL_SCREENED
         getPortalUiController().broadcastNonVetoableViewEvent(getFocusView(),
               ViewEventType.RESTORED_TO_NORMAL);
         getPortalUiController().broadcastNonVetoablePerspectiveEvent(PerspectiveEventType.LAUNCH_PANELS_ACTIVATED);
         FacesUtils.refreshPage(true);
      }
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
      return getPortalUiController().createView(viewDef, url, messageBean);
   }

   /**
    * @param viewDef
    * @param viewKey
    * @param viewParams
    * @param msgBean
    * @param nested
    * @return
    */
   public View createView(ViewDefinition viewDef, String viewKey,
         Map<String, Object> viewParams, AbstractMessageBean msgBean, boolean nested)
   {
      return getPortalUiController().createView(viewDef, viewKey, viewParams, msgBean,
            nested);
   }

   /**
    * @param viewId
    * @param viewKey
    * @param viewParams
    * @param messageBean
    * @param nestedView
    * @return
    */
   public View createView(String viewId, String viewKey,
         Map<String, Object> viewParams, AbstractMessageBean messageBean, boolean nestedView)
   {
      return getPortalUiController().createView(viewId, viewKey, viewParams,
            messageBean, nestedView);
   }
   
   /**
    * nested parameter should be false when calling this. In short nesting is not supported in this API
    */
   public void openInFocusTab()
   {
      View focusView = getFocusView();
      int focusViewIndex = viewIndex;
      closeView(focusView);
      
      View newView = getPortalUiController().openView();
      addToDisplayedViews(newView, true, focusViewIndex);
   }

   /**
    * @param viewId
    * @param params
    * @param msgBean
    */
   public void openInFocusTab(String viewId, Map<String, Object> params, AbstractMessageBean msgBean)
   {
      View focusView = getFocusView();
      int focusViewIndex = viewIndex;
      closeView(focusView);
      
      View newView = getPortalUiController().openViewById(viewId, params, msgBean, false);
      addToDisplayedViews(newView, true, focusViewIndex);
   }

   /**
    *
    */
   public void openView()
   {
      addToDisplayedViews(getPortalUiController().openView(), true);
   }
   
   /**
    * Opens the overFlow view selected from the Iframe and closes iframe and renders the
    * session
    */
   public void openOverflowView()
   {
      FacesContext context = FacesContext.getCurrentInstance();
      String viewUrl = (String) context.getExternalContext().getRequestParameterMap().get("viewUrl");      
      openView(viewUrl, null);

      if (overflowTabPopupOpened)
      {
         closeOverflowTabIframePopup();
      }

      renderPortalSession();
   }

   /**
    * @param viewUrl
    * @param messageBean
    */
   public void openView(String viewUrl, AbstractMessageBean messageBean)
   {
      addToDisplayedViews(getPortalUiController().openView(viewUrl, messageBean), true);
   }

   /**
    * @param viewDef
    * @param viewUrl
    * @param messageBean
    * @param nested
    * @return
    */
   public View openView(ViewDefinition viewDef, String viewUrl, AbstractMessageBean messageBean, boolean nested)
   {
      View openedView = getPortalUiController().openView(viewDef, viewUrl, messageBean, nested);

      if (null != openedView)
      {
         addToDisplayedViews(openedView, true);
      }

      return openedView;
   }

   /**
    * @param viewUrl
    * @param messageBean
    * @param nestedView
    */
   public void openView(String viewUrl, AbstractMessageBean messageBean,
         boolean nestedView)
   {
      addToDisplayedViews(getPortalUiController().openView(viewUrl, messageBean,
            nestedView), true);
   }

   /**
    * @param viewId
    * @param params
    * @param msgBean
    * @param nested
    */
   public void openViewById(String viewId, Map<String, Object> params, AbstractMessageBean msgBean, boolean nested)
   {
      addToDisplayedViews(getPortalUiController().openViewById(viewId, params, msgBean, nested), true);
   }

   /**
    * Constructor for backward-compatibility
    * @param viewId
    * @param viewKey
    * @param msgBean
    */
   @Deprecated
   public void openViewById(String viewId, String viewKey, AbstractMessageBean msgBean)
   {
      addToDisplayedViews(getPortalUiController().openViewById(viewId, viewId, null,
            msgBean, false), true);
   }

   /**
    * Constructor for backward-compatibility
    * @param viewId
    * @param viewKey
    * @param viewParams
    * @param msgBean
    */
   @Deprecated
   public void openViewById(String viewId, String viewKey,
         Map<String, Object> viewParams, AbstractMessageBean msgBean)
   {
      addToDisplayedViews(getPortalUiController().openViewById(viewId, viewId,
            viewParams, msgBean, false), true);
   }

   /**
    * @param viewId
    * @param viewKey
    * @param params
    * @param msgBean
    * @param nestedView
    * @return
    */
   public View openViewById(String viewId, String viewKey, Map<String, Object> params, AbstractMessageBean msgBean,
         boolean nestedView, int viewIndex)
   {
      View view = getPortalUiController().openViewById(viewId, viewKey, params, msgBean, nestedView);
      addToDisplayedViews(view, true, viewIndex);
      return view;
   }
   
   /**
    * @param viewId
    * @param viewKey
    * @param params
    * @param msgBean
    * @param nestedView
    * @return
    */
   public View openViewById(String viewId, String viewKey, Map<String, Object> params,
         AbstractMessageBean msgBean, boolean nestedView)
   {
      View view = getPortalUiController().openViewById(viewId, viewKey, params, msgBean, nestedView);
      addToDisplayedViews(view, true);
      return view;
   }

   /**
    * @param viewId
    * @param viewKey
    * @return
    */
   public View getViewById(String viewId, String viewKey)
   {
      return getPortalUiController().getViewById(viewId, viewKey);
   }

   /**
    *
    */
   public void selectView()
   {
      getPortalUiController().selectView();
      addToDisplayedViews(getPortalUiController().getFocusView());
   }

   /**
    * @param event
    */
   public void selectionChange(ValueChangeEvent event)
   {
      if (event.getNewValue() == null || ((String) event.getNewValue()).length() == 0)
      {
         setFocusView(null);
      }
      else
      {
         getPortalUiController().setFocusView(
               getPortalUiController().findView((String) event.getNewValue()));
         addToDisplayedViews(getPortalUiController().findView(
               (String) event.getNewValue()));
      }
   }

   /**
    * @return
    */
   public View getFocusView()
   {
      return getPortalUiController().getFocusView();
   }

   /**
    * @param focusView
    */
   public boolean setFocusView(View focusView)
   {
      if(getPortalUiController().setFocusView(focusView))
      {
         addToDisplayedViews(focusView);
         return true;
      }
      
      return false;
   }
   
   /**
    * @param view
    */
   public void setActiveView(View view)
   {
      activeView = view;
      getPortalUiController().setActiveView(view);
      setActiveViewBreadCrumb();
   }
   
   /**
    * @return
    */
   public List<View> getOpenViews()
   {
      List<View> openViews = getPortalUiController().getOpenViews();

      List<View> result = new ArrayList<View>(openViews.size());
      for (View view : openViews)
      {
         if(view != pinView)
            result.add(view);
      }
      return result;
   }

   /**
    *
    */
   public void setActiveViewBreadCrumb()
   {
      List<View> views = new ArrayList<View>();
      
      View view = getActiveView();
      if (view != null)
      {
         view = view.getOpenerView();

         while (view != null)
         {
            views.add(view);
            if (view == view.getOpenerView())
            {
               break;
            }
            view = view.getOpenerView();
         }
      }

      // Reverse the List
      activeViewBreadCrumb = new ArrayList<View>();
      for (int i = views.size() - 1; i >= 0; i-- )
      {
         activeViewBreadCrumb.add(views.get(i));
      }
   }

   /**
    * @return
    */
   public int getViewIndex()
   {
      return viewIndex;
   }

   /**
    * @param index
    */
   public void setViewIndex(int index)
   {
      this.viewIndex = index;
      setActiveView(displayedViews.get(this.viewIndex));
      setActiveViewBreadCrumb();
   }

   /**
    * @param arg0
    * @throws AbortProcessingException
    */
   public void processTabChange(TabChangeEvent arg0) throws AbortProcessingException
   {
      // If Tab Closed is Focus Tab then New and Old indexes are same
      // Ignore this event
      if (arg0.getNewTabIndex() != arg0.getOldTabIndex())
      {
         int index = arg0.getNewTabIndex();
         boolean success = setFocusView(getDisplayedViews().get(index));
         if(!success)
         {
            // Reset index back
            setViewIndex(arg0.getOldTabIndex());
         }
      }
   }

   /**
    * @param arg0
    * @throws AbortProcessingException
    */
   public void processPinViewTabChange(TabChangeEvent arg0) throws AbortProcessingException
   {
      // As we only have one view as pin view
      setActiveView(pinView);
   }
   
   /**
    * @return
    */
   public List<SelectItem> getItems()
   {
      List<SelectItem> list = new ArrayList<SelectItem>();

      for (int n = 0; n < getOpenViews().size(); ++n)
      {
         list.add(new SelectItem(getOpenViews().get(n).getUrl(), getOpenViews().get(n)
               .getLabel()));
      }

      return list;
   }

   /**
    * @return
    */
   public int getOpenViewsSize()
   {
      return getOpenViews().size();
   }

   /**
    * @return
    */
   public int getDisplayedViewsSize()
   {
      return displayedViews != null ? displayedViews.size() : 0;
   }

   /**
    * @return
    */
   public int getOverflowedViewsSize()
   {
      return overflowedViews != null ? overflowedViews.size() : 0;
   }

   /**
    * @return
    */
   public boolean getViewsOpened()
   {
      return getOpenViews().size() > 0;
   }

   /**
    * @return
    */
   public View getFirstView()
   {
      if (getOpenViews().size() >= 1)
      {
         return getOpenViews().get(0);
      }

      return null;
   }

   /**
    * @return
    */
   public View getSecondView()
   {
      if (getOpenViews().size() >= 2)
      {
         return getOpenViews().get(1);
      }

      return null;
   }

   /**
    * @return
    */
   public boolean getMoreThanTwoViewsOpened()
   {
      return getOpenViews().size() > 2;
   }

   /**
    *
    */
   public void closeAllViews()
   {
      pinViewOpened = false;
      pinView = null;
      
      List<View> allViews = getOpenViews();
      
      for (View view : allViews)
      {
         closeView(view);         
      }
     
      addToDisplayedViews(getLastFocusView());
   }

   /**
    *
    */
   public void closeFocusView()
   {
      View closedView = getFocusView();
      boolean closed = getPortalUiController().closeFocusView();
      if (closed)
      {
         handleViewClose(closedView, closedView, false);
      }
   }

   /**
    *
    */
   public void closeView()
   {
      View focusView = getFocusView();
      View closedView = getPortalUiController().closeView();

      if (null != closedView)
      {
         handleViewClose(closedView, focusView, false);
      }
   }

   /**
    * @param view
    * @return
    */
   public void closeView(View view)
   {
      closeView(view, false);
   }
   
   /**
    * @param view
    * @param force
    */
   public void closeView(View view, boolean force)
   {
      if (ViewState.CLOSED == view.getViewState())
      {
         return; // Already Closed
      }
      View focusView = getFocusView();
      if (getPortalUiController().closeView(view, force))
      {
         handleViewClose(view, focusView, force);
      }
   }

   /**
    * @param view
    * @param force
    */
   public void closeView(View view, boolean force, boolean refresh)
   {
      View focusView = getFocusView();
      if (getPortalUiController().closeView(view, force, refresh))
      {
         handleViewClose(view, focusView, force);
      }
   }
   
   /**
    * 
    */
   public void openPinViewHorizontal()
   {
      checkAndOpenPinViewInNewMode(PIN_VIEW_MODE_HORIZONTAL);
   }
   
   /**
    * 
    */
   public void openPinViewVertical()
   {
      checkAndOpenPinViewInNewMode(PIN_VIEW_MODE_VERTICAL);
   }
   
   /**
    * 
    */
   public void restorePinView()
   {
      if(pinViewOpened)
      {
         togglePinView();
      }
   }
   
   /**
    * Renders the Portal Session using Session Renderer
    */
   public void renderPortalSession()
   {
      SessionRendererHelper.render(SessionRendererHelper.getPortalSessionRendererId(getLoggedInUser()));
   }

   /**
    * If UI is already Pinned, Restore Pin View and Focus View too
    * And then Pin UI again on Focus View with new mode
    * @param mode
    */
   private void checkAndOpenPinViewInNewMode(String mode)
   {
      if (pinViewOpened)
      {
         View focusView = getFocusView();
         restorePinView();
         setFocusView(focusView);
      }

      pinViewOrientation = mode;
      togglePinView();
   }

   /**
    * @return
    */
   public String logout()
   {
      // Close All Open Views
      closeAllViews();
      
      // If All Views are successfully closed, then fire logout 
      if (getOpenViewsSize() == 0)
      {
         // don't directly logout, but redirect main page towards logout
         String logoutScript = "InfinityBpm.Core.closeSession();";
         JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), logoutScript);
         addEventScript(logoutScript); // This is required since addJavascriptCall does not work if JSF Page refresh is involved 
         
         SessionRendererHelper.removeCurrentSession(SessionRendererHelper.getPortalSessionRendererId(getLoggedInUser()));
      }
      else
      {
         if (trace.isDebugEnabled())
         {
            trace.debug("Not all Views got closed successully. Cannot logout...");
         }
      }
      
      return null;
   }

   /**
    * This resets the window size to 98% so that the new view's width can be recalculated correctly.
    * Without this, the second view of lesser wide would still assume the same width as the previous view.
    */
   private void resetWindowWidth()
   {
      PortalApplicationEventScript.getInstance().setResetWindowWidth(true);
   }

   /**
    * 
    */
   private void togglePinView()
   {
      if(!pinViewOpened)
      {
         boolean success = getPortalUiController().broadcastVetoableViewEvent(getFocusView(),
               ViewEventType.TO_BE_PINNED);
         if(!success)
         {
            return;
         }
      }

      pinViewOpened = !pinViewOpened;
      launchPanelsActivated = !pinViewOpened;

      closeOverflowTabIframePopup();

      if(pinViewOpened)
      {
         if(fullScreenModeActivated)
            fullScreenModeActivated = false;

         pinView = getFocusView();
         int index = displayedViews.indexOf(pinView);
         removeFromDisplayedViews(pinView);
         
         View nextFocusView = index < displayedViews.size()
               ? displayedViews.get(index)
               : displayedViews.get(displayedViews.size()-1);
         setFocusView(nextFocusView);
         
         // Make pin view as Active
         setActiveView(pinView);

         // Vetoed not processed here. This is FYI only event,
         // As this view already returned success for TO_BE_PINNED
         getPortalUiController().broadcastNonVetoableViewEvent(pinView, ViewEventType.PINNED);
      }
      else
      {
         View sfView = pinView;
         pinView = null;
         setFocusView(sfView);
      }
   }

   /**
    * @param view
    * @param justOpened
    * @param viewIndex
    */
   private void addToDisplayedViews(View view, boolean justOpened, int viewIndex)
   {
      resetWindowWidth();
      if(displayedViews == null)
         displayedViews = new ArrayList<View>();

      if(view != null)
      {
         if(pinView != view)
         {
            if(!displayedViews.contains(view))
            {
               displayedViews.remove(dummyViewForOverflowTab);
               if (-1 == viewIndex || viewIndex >= displayedViews.size())
               {
                  displayedViews.add(view);
               }
               else
               {
                  displayedViews.add(viewIndex, view);
               }
      
               if(getMaxTabDisplay() < displayedViews.size())
               {
                  displayedViews.remove(0);
               }

               buildOverflowViews();
      
               addDummyOverflowView();
            }
      
            closeOverflowTabIframePopup();

            int index = view != null ? displayedViews.indexOf(view) : -1;
            setViewIndex(index);
            if(index >= 0)
               addToFocusViewStack(displayedViews.get(index));
         }
         else if(pinView == view)
         {
            // Set the last focus view back to focus
            getPortalUiController().setFocusView(getLastFocusView());
            setActiveView(view);
         }
         
         if(justOpened)
         {
            firePostOpenLifeCycleEvent(view);
         }
      }
      else
      {
         if(getOpenViewsSize() == 0)
         {
            setActiveView(null);
         }
      }
   }

   public int getViewIndex(View view)
   {
      return displayedViews.indexOf(view);
   }   
   
   /**
    * @param view
    * @param justOpened
    */
   private void addToDisplayedViews(View view, boolean justOpened)
   {
      addToDisplayedViews(view, justOpened, -1);
   }

   /**
    * @param view
    */
   private void addToDisplayedViews(View view)
   {
      addToDisplayedViews(view, false);
   }

   /**
    * @param view
    */
   private void firePostOpenLifeCycleEvent(View view)
   {
      portalUiController.broadcastNonVetoableViewEvent(view, ViewEventType.POST_OPEN_LIFECYCLE);
   }
   
   /**
    * Build Overflow View List
    */
   private void buildOverflowViews()
   {
      overflowedViews = new ArrayList<View>();
      for (View openView : getOpenViews())
      {
         if(!displayedViews.contains(openView))
            overflowedViews.add(openView);
      }
   }

   /**
    * 
    */
   public void refreshSkin()
   {
      try
      {
         // Load The selected Theme from Preferences
         UserPreferencesHelper userPrefsHelper = UserPreferencesHelper.getInstance(M_PORTAL);
         UIController.getInstance().getThemeProvider().loadTheme(userPrefsHelper.getSingleString(V_PORTAL_CONFIG, F_SKIN));
         skinFiles = UIController.getInstance().getThemeProvider().getStyleSheets();
      }
      catch(Exception e)
      {
         MessageDialog.addErrorMessage(MessagePropertiesBean.getInstance().getString("common.unknownError"), e);
      }
   }

   /**
    * 
    */
   private void loadPerspective()
   {
      UserPreferencesHelper userPrefsHelper = UserPreferencesHelper.getInstance(M_PORTAL);
      String perspectiveId = userPrefsHelper.getSingleString(V_PORTAL_CONFIG, F_DEFAULT_PERSPECTIVE);
      if (StringUtils.isNotEmpty(perspectiveId))
      {
         if (!getPortalUiController().loadPerspective(perspectiveId))
         {
            trace.warn("Cannot load default Perspective, either it's not available or user is not authorized - "
                  + perspectiveId);
         }
      }
   }

   public boolean isExternalAuthentication()
   {
      return userProvider.isExternalAuthentication();      
   }
   
   public boolean isExternalAuthorization()
   {
      return userProvider.isExternalAuthorization();
   }

   /**
    * 
    */
   private void retrieveRuntimeEnvInfo()
   {
      version = "";
      copyrightMessage = "";

      if (null != runtimeEnvironmentInfoProvider)
      {
         try
         {
            version = runtimeEnvironmentInfoProvider.getVersion().getCompleteString();
         }
         catch (Exception e)
         {
            trace.error("Could not retrieve Version Information", e);
         }

         try
         {
            copyrightMessage = runtimeEnvironmentInfoProvider.getCopyrightInfo().getMessage();
         }
         catch (Exception e)
         {
            trace.error("Could not retrieve Copyright Information", e);
         }         
      }
   }
   
   /**
    * 
    * @return
    */
   public boolean isOverflowTabPopupOpened()
   {
      return overflowTabPopupOpened;
   }
   
   /**
    * 
    */
   public void toggleOverflowTabIframePopup()
   {
      if (overflowTabPopupOpened)
      {
         closeOverflowTabIframePopup();
      }
      else
      {
         openOverflowTabIframePopup();
      }
   }

   /**
    * @return
    */
   public String getOverflowTabIframePopupId()
   {
      return  "'OverflowiFrame'";
   }

   /**
    * @return
    */
   public String getOverflowTabIframePopupArgs()
   {
      String advanceArgs = "{anchorId:'ippOverflowTabAnchor', width:100, height:30, maxWidth:800, maxHeight:550, "
            + "openOnRight:false, anchorXAdjustment:120, anchorYAdjustment:5, zIndex:200, border:'1px solid black', noUnloadWarning: 'true'}";
      return advanceArgs;
   }

   /**
    * 
    */
   public void openOverflowTabIframePopup()
   {
      String iFrameId = getOverflowTabIframePopupId();
      String url = "'" + FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath()
            + "/plugins/common/overflowTabIframePopup.iface?random=" + System.currentTimeMillis() + "'";

      String script = "InfinityBpm.ProcessPortal.createOrActivateContentFrame(" + iFrameId + ", " + url + ", "
            + getOverflowTabIframePopupArgs() + ");";
      PortalApplication.getInstance().addEventScript(script);
      JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), script);
      overflowTabPopupOpened = true;
   }

   /**
    * closes the popup from portal application like completing activity
    */
   public void closeOverflowTabIframePopup()
   {
      if (overflowTabPopupOpened)
      {
         String iFrameId = getOverflowTabIframePopupId();
         String script = "InfinityBpm.ProcessPortal.closeContentFrame(" + iFrameId + ");";

         PortalApplication.getInstance().addEventScript(script);
         JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), script);

         overflowTabPopupOpened = false;
      }
   }

   /**
    * closes the popup in case any menu option from the popup is selected
    */
   public void closeOverflowTabIframePopupSelf()
   {
      if (overflowTabPopupOpened)
      {
         String iFrameId = getOverflowTabIframePopupId();
         String script = "parent.ippPortalMain.InfinityBpm.ProcessPortal.closeContentFrame(" + iFrameId + ");";

         JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), script);

         overflowTabPopupOpened = false;
      }
   }

   /**
    * @param view
    * @param handler
    */
   public void registerViewDataEventHandler(View view, ViewDataEventHandler handler)
   {
      getPortalUiController().registerViewDataEventHandler(view, handler);
   }

   /**
    * @param view
    * @param handler
    */
   public void unregisterViewDataEventHandler(View view, ViewDataEventHandler handler)
   {
      getPortalUiController().unregisterViewDataEventHandler(view, handler);
   }

   /**
    * @param event
    */
   public void broadcastViewDataEvent(ViewDataEvent event)
   {
      getPortalUiController().broadcastViewDataEvent(event);
   }
   
   /**
    *
    */
   private void addDummyOverflowView()
   {
      showOverflowTabs = false;
      if(getOpenViewsSize() > displayedViews.size())
      {
         displayedViews.add(dummyViewForOverflowTab);
         showOverflowTabs = true;
      }
   }

   /**
    * @param closedView
    * @param focusView
    * @param forceClose
    */
   private void handleViewClose(View closedView, View focusView, boolean forceClose)
   {
      resetWindowWidth();
      removeFromDisplayedViews(closedView);
      
      if(focusView == closedView)
         setFocusView(getLastFocusView());
      else
         setFocusView(getFocusView()); // This is required because the index of focus view would have changed
      
      if(getOpenViewsSize() == 0) // If this is the last view closed, activate launch Panels
      {
         launchPanelsActivated = true;
         fullScreenModeActivated = false;
         restorePinView();
      }
      
      closeChildViews(closedView, forceClose);
   }

   /**
    * @param view
    * @param forceClose
    */
   private void closeChildViews(View view, boolean forceClose)
   {
      ViewDefinition viewDefinition = view.getDefinition();
      if (viewDefinition != null) // Better to be safe!
      {
         String closingPolicy = viewDefinition.getClosingPolicy();

         if (ViewDefinition.CLOSING_POLICY_DIRECT.equalsIgnoreCase(closingPolicy)
               || ViewDefinition.CLOSING_POLICY_RECURSIVE.equalsIgnoreCase(closingPolicy))
         {
            boolean recursive = ViewDefinition.CLOSING_POLICY_RECURSIVE.equalsIgnoreCase(closingPolicy) ? true : false;
            List<View> childViews = getPortalUiController().getChildViews(view, recursive);
            boolean childViewClosed = false;
            for (View childView : childViews)
            {
               if (ViewState.CLOSED != childView.getViewState()) // Safety Check
               {
                  if (trace.isDebugEnabled())
                  {
                     trace.debug("Cascade Closing Child View -> " + childView);
                  }
                  closeView(childView, forceClose);
                  childViewClosed = true;
               }
            }
            
            if(childViewClosed)
            {
               FacesUtils.refreshPage(); // Looks like there is a need to refresh entire page when multiple views gets closed!
            }
         }
      }
   }

   /**
    * @param view
    */
   private void removeFromDisplayedViews(View view)
   {
      if(displayedViews.contains(view))
      {
         displayedViews.remove(dummyViewForOverflowTab);
         displayedViews.remove(view);

         for (View openView : getOpenViews())
         {
            if(!displayedViews.contains(openView))
            {
               displayedViews.add(openView);
               break;
            }
         }

         buildOverflowViews();
         addDummyOverflowView();
      }
      else if(view == pinView)
      {
         pinViewOpened = false;
         pinView = null;
         launchPanelsActivated = true;
      }

      removeFromFocusViewStack(view);
   }

   /**
    * @param view
    */
   private void addToFocusViewStack(View view)
   {
      if(focusViewStack.contains(view))
         focusViewStack.remove(view);
      focusViewStack.add(view);
   }
   
   /**
    * @param view
    */
   private void removeFromFocusViewStack(View view)
   {
      if(focusViewStack.contains(view))
         focusViewStack.remove(view);
   }
   
   /**
    * @return
    */
   private View getLastFocusView()
   {
      if (trace.isDebugEnabled())
      {
         trace.debug("focusViewStack = " + focusViewStack);
      }
      if(focusViewStack.size() > 0)
         return focusViewStack.get(focusViewStack.size() - 1);
      return null;
   }

   /**
    * @return
    */
   public String getLocale()
   {
      return MessagePropertiesBean.getInstance().getLocale();
   }

   /**
    * @return
    */
   public Locale getLocaleObject()
   {
      return MessagePropertiesBean.getInstance().getLocaleObject();
   }

   /**
    * @return
    */
   public TimeZone getTimeZone()
   {
      if (clientTimeZone != null)
      {
         return clientTimeZone;
      }
      else
      {
         return java.util.TimeZone.getDefault();
      }
   }

   public boolean isClientTimeZoneSet()
   {
      //Following fix was added as part of CRNT-20962 but later on with the new Icefaces jars the issue got resolved
      //Keeping the following code as comment it may required in future   
      /*         String userAgent = FacesUtils.getUserAgent();
      try
      {
         String userAgent = FacesUtils.getUserAgent();

         if (null == userAgent || userAgent.toLowerCase().contains("chrome"))
         {
            clientTimeZone = java.util.TimeZone.getDefault();
            trace.error("Browser type 'Chrome' detected or could not get User-Agent. UserAgent = " + userAgent);
            trace.error("Not supporting client timezone. Server Time will be referred.");
         }
      }
      catch (Exception e)
      {
         clientTimeZone = java.util.TimeZone.getDefault();
         trace.error("Not supporting client timezone. Server Time will be referred", e);
      }*/
      return clientTimeZone != null;
   }   

   /**
    * @param view
    * @param newViewKey
    */
   public void updateViewKey(View view, String newViewKey)
   {
      getPortalUiController().updateViewKey(view, newViewKey);
   }

   private void setDisplayMode(int displayMode)
   {
      this.displayMode = displayMode;
   }

   public boolean isTabDisplayMode()
   {
      return getDisplayMode() == TAB_DISPLAY_MODE;
   }

   public void setTabDisplayMode()
   {
      setDisplayMode(TAB_DISPLAY_MODE);
   }

   public boolean isGridDisplayMode()
   {
      return getDisplayMode() == GRID_DISPLAY_MODE;
   }

   public void setGridDisplayMode()
   {
      setDisplayMode(GRID_DISPLAY_MODE);
   }

   public boolean isHorizontalSplitDisplayMode()
   {
      return getDisplayMode() == HORIZONTAL_SPLIT_DISPLAY_MODE;
   }

   public void setHorizontalSplitDisplayMode()
   {
      setDisplayMode(HORIZONTAL_SPLIT_DISPLAY_MODE);
   }

   public boolean isVerticalSplitDisplayMode()
   {
      return getDisplayMode() == VERTICAL_SPLIT_DISPLAY_MODE;
   }

   public void setVerticalSplitDisplayMode()
   {
      setDisplayMode(VERTICAL_SPLIT_DISPLAY_MODE);
   }

   private int getDisplayMode()
   {
      return displayMode;
   }

   public PortalUiController getPortalUiController()
   {
      return portalUiController;
   }

   /**
    * @deprecated Use portalUiController directly
    */
   @Deprecated
   public PerspectiveController getPerspectiveController()
   {
      return portalUiController.getPerspectiveController();
   }

   public void setPortalUiController(PortalUiController portalUiController)
   {
      this.portalUiController = portalUiController;
   }

   public List<View> getActiveViewBreadCrumb()
   {
      return activeViewBreadCrumb;
   }

   public List<View> getDisplayedViews()
   {
      return displayedViews;
   }

   public List<View> getOverflowedViews()
   {
      return overflowedViews;
   }

   public boolean isShowOverflowTabs()
   {
      return showOverflowTabs;
   }

   public boolean isPinViewOpened()
   {
      return pinViewOpened;
   }

   public void setPinViewOpened(boolean pinViewOpened)
   {
      this.pinViewOpened = pinViewOpened;
   }
   
   public String getPinViewOrientation()
   {
      return pinViewOrientation;
   }

   public View getPinView()
   {
      return pinView;
   }
   
   public List<String> getSkinFiles()
   {
      return skinFiles;
   }
   
   public View getActiveView()
   {
      return activeView;
   }
   
   public void setUserProvider(UserProvider userProvider)
   {
      this.userProvider = userProvider;
   }

   /**
    * @return
    */
   public String getEventScripts()
   {
      return portalApplicationEventScript.getEventScripts();
   }
   
   /**
    * @param eventScript
    */
   public void addEventScript(String eventScript)
   {
      portalApplicationEventScript.addEventScript(eventScript);
   }

   /**
    * Called by Phase Listener
    */
   public void cleanEventScripts()
   {
      portalApplicationEventScript.cleanEventScripts();      
   }

   public PortalApplicationEventScript getPortalApplicationEventScript()
   {
      return portalApplicationEventScript;
   }

   public void setPortalApplicationEventScript(PortalApplicationEventScript portalApplicationEventScript)
   {
      this.portalApplicationEventScript = portalApplicationEventScript;
   }
   
   /**
    * @param event
    */
   public void timeZoneChangeListener(ValueChangeEvent event)
   {
      try
      {
         clientTimeZone = DateUtils.getClientTimeZone(event.getNewValue());
      }
      catch (Exception e)
      {
         clientTimeZone = java.util.TimeZone.getDefault();
         trace.error("Not supporting client timezone. Server Time will be referred", e);
      }
   }

   public void setRuntimeEnvironmentInfoProvider(RuntimeEnvironmentInfoProvider runtimeEnvironmentInfoProvider)
   {
      this.runtimeEnvironmentInfoProvider = runtimeEnvironmentInfoProvider;
   }

   public String getVersion()
   {
      return version;
   }

   public String getCopyrightMessage()
   {
      return copyrightMessage;
   }

   public String getLogoutUri()
   {
      return logoutUri;
   }

   public int getMaxTabDisplay()
   {
      if (maxTabDisplay == -1)
      {
         try
         {
            maxTabDisplay = UserPreferencesHelper.getInstance(M_PORTAL).getInteger(V_PORTAL_CONFIG,
                  F_TABS_MAX_TABS_DISPLAY, DEFAULT_MAX_TAB_DISPLAY);
         }
         catch (Exception e)
         {
            maxTabDisplay = DEFAULT_MAX_TAB_DISPLAY;
            trace.error("Exception occurred while reading user preferences (maxTabDisplay), Using default as "
                  + DEFAULT_MAX_TAB_DISPLAY, e);
         }
      }

      return maxTabDisplay;
   }
   
   public String getLocaleString()
   {
      return getLocaleObject().toString();
   }
   
   public void setPageRefreshOn(boolean pageRefreshOn)
   {
      this.pageRefreshOn = pageRefreshOn;
   }

   public boolean isPageRefreshOn()
   {
      return pageRefreshOn;
   }
}