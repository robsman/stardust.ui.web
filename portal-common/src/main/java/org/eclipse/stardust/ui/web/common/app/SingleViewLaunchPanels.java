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

      trace.info("SingleApp_LaunchPanels:getEventScripts():\n" + scripts);
      return scripts;
   }

   /**
    * @param event
    */
   public void activeViewChanged(ValueChangeEvent event)
   {
      PortalApplication portalApp = PortalApplication.getInstance();

      String value = (String)event.getNewValue();
      if (StringUtils.isNotEmpty(value))
      {
         List<String> values = StringUtils.splitAndKeepOrder(value, ":");
         String viewId = values.get(0);
         String viewKey = (values.size() == 2) ? values.get(1) : null;

         View view = portalApp.getPortalUiController().findView(viewId, viewKey);

         View focusView = portalApp.getFocusView();
         trace.info("Before:: Focus View: " + focusView);
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
         trace.info("After:: Focus View: " + portalApp.getFocusView());

         printViews();
      }
   }

   /**
    * @param event
    */
   public void viewClosed(ValueChangeEvent event)
   {
      PortalApplication portalApp = PortalApplication.getInstance();

      String value = (String)event.getNewValue();
      trace.error("Closing view: " + value);

      if (StringUtils.isNotEmpty(value))
      {
         List<String> values = StringUtils.splitAndKeepOrder(value, ":");
         String viewId = values.get(0);
         String viewKey = (values.size() == 2) ? values.get(1) : null;

         View view = portalApp.getPortalUiController().findView(viewId, viewKey);
         if (null != view)
         {
            trace.info("Before:: View Count: " + portalApp.getOpenViewsSize());
            view.getViewParams().put("skipViewCloseScript", "true");
            portalApp.closeView(view, true);
            trace.info("After:: View Count: " + portalApp.getOpenViewsSize() + ", View Closed: " + view);

            String sessionId = SessionRendererHelper.getPortalSessionRendererId(portalApp.getLoggedInUser());
            sessionId += view.getIdentityParams();
            sessionId = Base64.encode(sessionId);
            //SessionRendererHelper.removeCurrentSession(sessionId);
         }
         else
         {
            trace.error("Could not close view: " + value);
         }

         portalApp.printOpenViews();
      }
   }

   /**
    * @param event
    */
   public void launchPanelsSynced(ValueChangeEvent event)
   {
      trace.info("********** Launch Panels Synced");
   }

   /**
    * @param event
    */
   public void sidebarChanged(ValueChangeEvent event)
   {
      // Pinned/Unpinned,Expanded/Collapsed
      // TODO
   }

   /**
    * @param event
    */
   public void logout(ValueChangeEvent event)
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
         script = "parent.BridgeUtils.showAlert('Not all Views got closed successully. Cannot logout...');";
         trace.warn("Not all Views got closed successully. Cannot logout...");
      }
      portalApp.addEventScript(script);
   }

   /**
    * @param event
    */
   public void messageReceived(ValueChangeEvent event)
   {
      PortalApplication portalApp = PortalApplication.getInstance();
      portalApp.postMessage((String)event.getNewValue());
   }

   /**
    *
    */
   private void printViews()
   {
      trace.debug("All Views: ");
      PortalApplication portalApp = PortalApplication.getInstance();
      for (View view : portalApp.getOpenViews())
      {
         trace.debug("\t" + view);
      }
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
}
