package org.eclipse.stardust.ui.web.common.app;

import static org.eclipse.stardust.ui.web.common.util.StringUtils.isEmpty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;

import org.eclipse.stardust.ui.web.common.IPerspectiveDefinition;
import org.eclipse.stardust.ui.web.common.ViewDefinition;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.util.SessionRendererHelper;
import org.eclipse.stardust.ui.web.common.util.StringUtils;
import org.springframework.beans.factory.InitializingBean;

import com.icesoft.util.encoding.Base64;

/**
 * TODO: Move some / all logic to Phase Listener
 *
 * @author Subodh.Godbole
 *
 */
public class PortalApplicationSingleView implements Serializable, InitializingBean
{
   private static final long serialVersionUID = 1L;
   private static final Logger trace = LogManager.getLogger(PortalApplicationSingleView.class);

   private PortalApplicationSingleViewEventScript singleViewEventScript;

   private String perspectiveId;
   private String viewId;
   private String viewKey;
   private String viewParams;

   /* (non-Javadoc)
    * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
    */
   public void afterPropertiesSet() throws Exception
   {
   }


   /**
    * @return
    */
   @SuppressWarnings({"rawtypes", "unchecked"})
   public View getView()
   {
      try
      {
         View view = null;
         PortalApplication portalApp = PortalApplication.getInstance();

         if (!isViewJustNowOpened())
         {
            // Actions within View
            view = portalApp.getPortalUiController().findView(viewId, viewKey);
         }
         else
         {
            // View Just now Opened
            Map requestMap = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
            String fullViewId = (String) requestMap.get("singleViewId");

            String params = ViewInfo.calculateViewParams(requestMap);
            if (null != viewId && fullViewId.equals(perspectiveId + "::" + viewId) && params.equals(viewParams))
            {
               view = portalApp.getPortalUiController().findView(viewId, viewKey);
            }

            if (null == view)
            {
               ViewInfo viewInfo = new ViewInfo();
               initialize(viewInfo);
               viewInfo.getParams().put("standaloneMode", "true");
               viewInfo.getParams().put("doNotCopyParams", "true");

               view = portalApp.openViewById(viewInfo.getViewId(), viewInfo.getViewKey(), viewInfo.getParams(), null,
                     false);
            }

            if (null != view)
            {
               if (!view.getViewParams().containsKey("addedToSessionRenderer"))
               {
                  String sessionId = SessionRendererHelper.getPortalSessionRendererId(portalApp.getLoggedInUser());
                  sessionId += view.getIdentityParams();
                  sessionId = Base64.encode(sessionId);
                  //SessionRendererHelper.addCurrentSession(sessionId);

                  view.getViewParams().put("addedToSessionRenderer", true);
               }
            }
         }

         if(null == view)
         {
            trace.error("View Can't be NULL. Fallback to active View");
            //view = PortalApplication.getInstance().getActiveView();
         }

         return view;
      }
      catch (Exception e)
      {
         //TODO Handle
         e.printStackTrace();

         return null;
      }
   }

   /**
    * @return
    */
   @SuppressWarnings("rawtypes")
   public boolean isViewJustNowOpened()
   {
      Map requestMap = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
      String fullViewId = (String) requestMap.get("singleViewId");
      return StringUtils.isNotEmpty(fullViewId);
   }

   /**
    * @param viewInfo
    */
   private void initialize(ViewInfo viewInfo)
   {
      setPerspectiveId(viewInfo.getPerspectiveId());
      setViewId(viewInfo.getViewId());
      setViewKey(viewInfo.getViewKey());
      setViewParams(viewInfo.getViewParams());
   }

   /**
    * @return
    */
   public List<View> getBreadCrumb()
   {
      List<View> breadCrumb;
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

      return breadCrumb;
   }

   /**
    * @return
    */
   public String getEventScripts()
   {
      String scripts = singleViewEventScript.getEventScripts(true);
      scripts = PortalApplicationSingleViewEventScript.wrapIntoRunScript(scripts);
      trace.info("SingleApp_View:getEventScripts():\n" + scripts);
      return scripts;
   }

   /**
    * @param event
    */
   public void activeViewSync(ValueChangeEvent event)
   {
      trace.info("********** Active View Synced");
   }

   public String getViewKey()
   {
      return viewKey;
   }

   public void setViewKey(String viewKey)
   {
      this.viewKey = viewKey;
   }

   public void setViewParams(String viewParams)
   {
      this.viewParams = viewParams;
   }

   public void setPerspectiveId(String perspectiveId)
   {
      this.perspectiveId = perspectiveId;
   }

   public String getViewId()
   {
      return viewId;
   }

   public void setViewId(String viewId)
   {
      this.viewId = viewId;
   }

   public PortalApplicationSingleViewEventScript getSingleViewEventScript()
   {
      return singleViewEventScript;
   }

   public void setSingleViewEventScript(PortalApplicationSingleViewEventScript singleViewEventScript)
   {
      this.singleViewEventScript = singleViewEventScript;
   }

   /**
    * @author Subodh.Godbole
    *
    */
   @SuppressWarnings("rawtypes")
   public static class ViewInfo implements Serializable
   {
      private static final long serialVersionUID = 1L;

      private String perspectiveId;
      private String viewId;
      private String viewKey;
      private String viewParams;
      private Map<String, Object> params;

      private ViewDefinition viewDef;

      /**
       * @return
       */
      private static String calculateViewParams(Map params)
      {
         StringBuffer sbParams = new StringBuffer();
         for (Object key : params.keySet())
         {
            if (key instanceof String)
            {
               sbParams.append(key).append("=").append(params.get(key)).append("&");
            }
         }

         if (sbParams.length() > 0)
         {
            sbParams.deleteCharAt(sbParams.length() - 1);
         }

         return sbParams.toString();
      }

      /**
       * @param perspectiveId
       * @param viewId
       * @param viewKey
       */
      public ViewInfo()
      {
         Map requestMap = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();

         String fullViewId = (String) requestMap.get("singleViewId");

         if (-1 == fullViewId.indexOf("::"))
         {
            perspectiveId = "*";
            viewId = fullViewId;
         }
         else
         {
            perspectiveId = fullViewId.substring(0, fullViewId.indexOf("::"));
            viewId = fullViewId.substring(fullViewId.indexOf("::") + 2);
         }

         viewKey = (String) requestMap.get("singleViewKey");

         this.params = new HashMap<String, Object>();
         for (Object obj : requestMap.keySet())
         {
            if (null != obj && requestMap.get(obj) instanceof String)
            {
               this.params.put(obj.toString(), requestMap.get(obj));
            }
         }

         this.viewParams = calculateViewParams(requestMap);

         PortalApplication portalApp = PortalApplication.getInstance();

         IPerspectiveDefinition perspectiveDef = isEmpty(perspectiveId) || "*".equals(perspectiveId)
               ? portalApp.getPortalUiController().getPerspective()
               : portalApp.getPortalUiController().getPerspective(perspectiveId);
         if (null != perspectiveDef)
         {
            this.viewDef = perspectiveDef.getViewDefinition(viewId);
         }
      }

      public String getPerspectiveId()
      {
         return perspectiveId;
      }

      public String getViewId()
      {
         return viewId;
      }

      public String getViewKey()
      {
         return viewKey;
      }

      public String getViewParams()
      {
         return viewParams;
      }

      public Map getParams()
      {
         return params;
      }

      public ViewDefinition getViewDef()
      {
         return viewDef;
      }
   }
}
