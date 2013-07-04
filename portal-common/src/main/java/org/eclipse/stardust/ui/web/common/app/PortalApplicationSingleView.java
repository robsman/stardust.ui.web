package org.eclipse.stardust.ui.web.common.app;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.application.Application;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.servlet.http.HttpServletRequest;

import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.util.SessionRendererHelper;
import org.eclipse.stardust.ui.web.common.util.StringUtils;
import org.springframework.beans.factory.InitializingBean;

import com.icesoft.util.encoding.Base64;

/**
 * Request Scoped Bean backing Single Views
 *
 * @author Subodh.Godbole
 * 
 */
public class PortalApplicationSingleView implements Serializable, InitializingBean
{
   private static final long serialVersionUID = 1L;
   private static final Logger trace = LogManager.getLogger(PortalApplicationSingleView.class);

   private PortalApplicationSingleViewEventScript singleViewEventScript;

   private View singleView;
   private List<View> breadCrumb;

   boolean syncLaunchPanels = true;

   /* 
    * Request scope bean, set view context immediately after construction
    * (non-Javadoc)
    * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
    */
   public void afterPropertiesSet() throws Exception
   {
      FacesContext facesContext = FacesContext.getCurrentInstance();
      UIViewRoot viewRoot = facesContext.getViewRoot();

      HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();
      String singleViewId = request.getParameter("singleViewId");
      String singleViewKey = request.getParameter("singleViewKey");

      // First time - View just now opened
      if (StringUtils.isNotEmpty(singleViewId))
      {
         if (-1 != singleViewId.indexOf("::"))
         {
            singleViewId = singleViewId.substring(singleViewId.indexOf("::") + 2);
         }

         // Same view information into view root for later use
         Application facesApp = facesContext.getApplication();
         viewRoot.setValueBinding("singleViewId", facesApp.createValueBinding("#{'" + singleViewId + "'}"));
         viewRoot.setValueBinding("singleViewKey", facesApp.createValueBinding("#{'" + singleViewKey + "'}"));
      }
      else // Actions within View
      {
         singleViewId = (String)viewRoot.getValueBinding("singleViewId").getValue(facesContext);
         singleViewKey = (String)viewRoot.getValueBinding("singleViewKey").getValue(facesContext);
      }
    
      associateView(singleViewId, singleViewKey);
      setBreadcrumb();
      trace.info("Single View Context = " + singleViewId + ":" + singleViewKey);
   }

   /**
    * @param singleViewId
    * @param singleViewKey
    */
   private void associateView(String singleViewId, String singleViewKey)
   {
      try
      {
         PortalApplication portalApp = PortalApplication.getInstance();
         singleView = portalApp.getPortalUiController().findView(singleViewId, singleViewKey);
         if (null == singleView)
         {
            @SuppressWarnings("rawtypes")
            Map requestMap = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
            Map<String, Object> params = new HashMap<String, Object>();
            for (Object obj : requestMap.keySet())
            {
               if (null != obj && requestMap.get(obj) instanceof String)
               {
                  params.put(obj.toString(), requestMap.get(obj));
               }
            }
            params.put("standaloneMode", "true");
            params.put("doNotCopyParams", "true");

            singleView = portalApp.openViewById(singleViewId, singleViewKey, params, null, false);
         }

         if (null != singleView && !singleView.getViewParams().containsKey("addedToSessionRenderer"))
         {
            String sessionId = SessionRendererHelper.getPortalSessionRendererId(portalApp.getLoggedInUser());
            sessionId += singleView.getIdentityParams();
            sessionId = Base64.encode(sessionId);
            //SessionRendererHelper.addCurrentSession(sessionId);

            singleView.getViewParams().put("addedToSessionRenderer", true);
         }

         if(null == singleView)
         {
            trace.error("Can not find View for " + singleViewId + ":" + singleViewKey);
            if (trace.isDebugEnabled())
            {
               trace.debug("", new Throwable());
            }
            // Fallback?
            //singleView = PortalApplication.getInstance().getActiveView();
         }
      }
      catch (Exception e)
      {
         trace.error("Could not set view context for " + singleViewId + ":" + singleViewKey, e);
      }
   }

   /**
    * 
    */
   private void setBreadcrumb()
   {
      List<View> views = new ArrayList<View>();

      View view = getView();
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
      breadCrumb = new ArrayList<View>();
      for (int i = views.size() - 1; i >= 0; i-- )
      {
         breadCrumb.add(views.get(i));
      }
   }

   /**
    * @return
    */
   public String getEventScripts()
   {
      String scripts = singleViewEventScript.getEventScripts(syncLaunchPanels);
      scripts = PortalApplicationSingleViewEventScript.wrapIntoRunScript(scripts);
      trace.info("SingleApp_View:getEventScripts():\n" + scripts);
      return scripts;
   }

   /**
    * @param event
    */
   public void activeViewSync(ValueChangeEvent event)
   {
      syncLaunchPanels = false; // No need to sync LPs, just active view is changed
      trace.info("Active View Synced");
   }

   public View getView()
   {
      return singleView;
   }

   public List<View> getBreadCrumb()
   {
      return breadCrumb;
   }

   public PortalApplicationSingleViewEventScript getSingleViewEventScript()
   {
      return singleViewEventScript;
   }

   public void setSingleViewEventScript(PortalApplicationSingleViewEventScript singleViewEventScript)
   {
      this.singleViewEventScript = singleViewEventScript;
   }
}
