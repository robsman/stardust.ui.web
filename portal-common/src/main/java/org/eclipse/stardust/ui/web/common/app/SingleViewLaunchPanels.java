package org.eclipse.stardust.ui.web.common.app;

import java.util.List;

import javax.faces.event.ValueChangeEvent;

import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.util.SessionRendererHelper;
import org.eclipse.stardust.ui.web.common.util.StringUtils;
import org.springframework.beans.factory.InitializingBean;

import com.icesoft.util.encoding.Base64;

/**
 * @author Subodh.Godbole
 *
 */
public class SingleViewLaunchPanels implements InitializingBean
{
   private static final Logger trace = LogManager.getLogger(SingleViewLaunchPanels.class);

   private PortalApplicationSingleViewEventScript singleViewEventScript;

   private String launchPanelsWidth = "auto";

   /* (non-Javadoc)
    * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
    */
   public void afterPropertiesSet() throws Exception
   {
   }

   /**
    * @return
    */
   public String getEventScripts()
   {
      String scripts = singleViewEventScript.getEventScripts(false);

      if (StringUtils.isNotEmpty(scripts))
      {
         scripts = PortalApplicationSingleViewEventScript.wrapIntoRunScript(scripts);
      }
      if (trace.isDebugEnabled())
      {
         trace.debug("SingleApp_LaunchPanels:getEventScripts():\n" + scripts);
      }
      return scripts;
   }

   /**
    * @param event
    */
   public void activeViewChanged(ValueChangeEvent event)
   {
      try
      {
         PortalApplication portalApp = PortalApplication.getInstance();
   
         String value = (String)event.getNewValue();
         if (StringUtils.isNotEmpty(value))
         {
            List<String> values = StringUtils.splitAndKeepOrder(value, ":");
            String viewId = values.get(0);
            String viewKey = (values.size() == 2) ? values.get(1) : null;
            
            View view = findView(viewId, viewKey, portalApp);
   
            View focusView = portalApp.getFocusView();
            if (trace.isDebugEnabled())
            {
               trace.debug("Before:: Focus View: " + focusView);
            }
            if (focusView != view)
            {
               portalApp.setFocusView(view);
   
               if (null != view)
               {
                  portalApp.addEventScript("parent.BridgeUtils.View.syncActiveView();");
   
                  String sessionId = SessionRendererHelper.getPortalSessionRendererId(portalApp.getLoggedInUser());
                  sessionId += view.getIdentityParams();
                  sessionId = Base64.encode(sessionId);
                  //SessionRendererHelper.render(sessionId);
               }
            }
            if (trace.isDebugEnabled())
            {
               trace.debug("After:: Focus View: " + portalApp.getFocusView());
            }
            portalApp.printOpenViews();
         }
      }
      catch (Exception e)
      {
         trace.error("", e);
      }
   }

   /**
    * @param event
    */
   public void viewClosing(ValueChangeEvent event)
   {
      try
      {
         PortalApplication portalApp = PortalApplication.getInstance();
   
         String value = (String)event.getNewValue();
         if (trace.isDebugEnabled())
         {
            trace.debug("Closing view: " + value);
         }
         
         if (StringUtils.isNotEmpty(value))
         {
            List<String> views = StringUtils.splitAndKeepOrder(value, "$$");
            for (String viewInfo : views)
            {
               closeView(viewInfo, portalApp);
            }

            portalApp.printOpenViews();
         }
      }
      catch (Exception e)
      {
         trace.error("", e);
      }
   }
   
   /**
    * @param value
    * @param portalApp
    */
   private void closeView(String value, PortalApplication portalApp)
   {
      List<String> values = StringUtils.splitAndKeepOrder(value, ":");
      String viewId = values.get(0);
      String viewKey = (values.size() == 2) ? values.get(1) : null;
            
      View view = findView(viewId, viewKey, portalApp);
      if (null != view)
      {
         if (trace.isDebugEnabled())
         {
            trace.debug("Trying to Close View: " + view);
            trace.debug("Before:: View Count: " + portalApp.getOpenViewsSize());
         }
         portalApp.closeView(view);
         if (trace.isDebugEnabled())
         {
            trace.debug("After:: View Count: " + portalApp.getOpenViewsSize());
         }
         // After succesfull view close, sync active view
         portalApp.addEventScript("parent.BridgeUtils.View.syncActiveView();");
         
         String sessionId = SessionRendererHelper.getPortalSessionRendererId(portalApp.getLoggedInUser());
         sessionId += view.getIdentityParams();
         sessionId = Base64.encode(sessionId);
         //SessionRendererHelper.removeCurrentSession(sessionId);
      }
      else
      {
         if (trace.isDebugEnabled())
         {
            trace.debug("Could not close view: " + value);
         }
         // Unexpected Situation! View is not open at IPP, but it's open on UI at HTML5 Framework.
         // Fire JS for it's closing. It might be internal/native or external view
         // TODO: Enhance?
         if ("configurationTreeView".equals(viewId))
         {
            String html5FWViewId = "/bpm/portal/configurationTreeView";
            String script = "parent.BridgeUtils.View.closeView('" + html5FWViewId + "');";
            portalApp.addEventScript(script);                  
         }
         else
         {
            // For External
            String html5FWViewId = "/bpm/portal/Ext/" + viewId + "/" + viewKey;
            String script = "parent.BridgeUtils.View.closeView('" + html5FWViewId + "');";
            portalApp.addEventScript(script);

            // For Internal/Native
            html5FWViewId = "/bpm/portal/Int/" + viewId + "/" + viewKey;
            script = "parent.BridgeUtils.View.closeView('" + html5FWViewId + "');";
            portalApp.addEventScript(script);
         }
      }
   }

   /**
    * @param event
    */
   public void launchPanelsSynced(ValueChangeEvent event)
   {
      String value = (String)event.getNewValue();
      if (StringUtils.isNotEmpty(value) && value.startsWith("parent.BridgeUtils."))
      {
         PortalApplication.getInstance().addEventScript(value);
      }
      trace.debug("Launch Panels Synced");
   }

   /**
    * @param event
    */
   public void updateLaunchPanelsWidth(ValueChangeEvent event)
   {
      String value = (String)event.getNewValue();
      launchPanelsWidth = value.split("_")[0];
      if (StringUtils.isEmpty(launchPanelsWidth))
      {
         launchPanelsWidth = "auto";
      }
   }

   /**
    * @param event
    */
   public void logout(ValueChangeEvent event)
   {
      try
      {
         PortalApplication portalApp = PortalApplication.getInstance();
         portalApp.closeAllViews();
         
         String script;
         if (portalApp.getOpenViewsSize() == 0)
         {
            script = "parent.BridgeUtils.logout(true);";
         }
         else
         {
            portalApp.printOpenViews();

            script = "parent.BridgeUtils.showAlert('Not all Views got closed successully. Cannot logout...');";
            trace.warn("Not all Views got closed successully. Cannot logout...");
         }
         portalApp.addEventScript(script);
      }
      catch (Exception e)
      {
         trace.error("", e);
      }
   }

   /**
    * @param event
    */
   public void messageReceived(ValueChangeEvent event)
   {
      try
      {
         PortalApplication portalApp = PortalApplication.getInstance();
         portalApp.postMessage((String)event.getNewValue());
      }
      catch (Exception e)
      {
         trace.error("", e);
      }
   }
   
   /**
    * 
    * @param viewId
    * @param viewKey
    * @param portalApp
    * @return
    */
   private View findView(String viewId, String viewKey, PortalApplication portalApp)
   {
      if (FrameworkViewInfo.DEFAULT_VIEW_KEY.equals(viewKey))
      {
         viewKey = null;
      }
      return portalApp.getPortalUiController().findView(viewId, viewKey);
   }
   
   public PortalApplicationSingleViewEventScript getSingleViewEventScript()
   {
      return singleViewEventScript;
   }

   public void setSingleViewEventScript(PortalApplicationSingleViewEventScript singleViewEventScript)
   {
      this.singleViewEventScript = singleViewEventScript;
   }
   
   public String getEmptyString()
   {
      return "";
   }

   public void setEmptyString(String empty)
   {
      // IGNORE
   }

   public String getLaunchPanelsWidth()
   {
      return launchPanelsWidth;
   }
}
