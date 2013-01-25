package org.eclipse.stardust.ui.web.modeler.portal;

import javax.faces.context.FacesContext;

import org.springframework.stereotype.Component;

import com.icesoft.faces.context.effects.JavascriptContext;

import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.common.event.ViewEvent;

@Component
public class ProcessDefinitionView extends AbstractAdapterView {
   /**
    *
    */
   public ProcessDefinitionView()
   {
      super("/plugins/bpm-modeler/views/modeler/processDefinitionView.html", "processDefinitionFrameAnchor");
   }

   @Override
   public void handleEvent(ViewEvent event)
   {
      super.handleEvent(event);

      switch (event.getType())
      {
      case CREATED:
         event.getView().setIcon("/plugins/bpm-modeler/images/icons/process.png");
         break;

      case LAUNCH_PANELS_ACTIVATED:
      case LAUNCH_PANELS_DEACTIVATED:
      case FULL_SCREENED:
      case RESTORED_TO_NORMAL:
      case TO_BE_ACTIVATED:
      case PINNED:
      case PERSPECTIVE_CHANGED:
         fireResizeIframeEvent(event);
         break;
      }
   }

   /**
    * @param event
    */
   private void fireResizeIframeEvent(ViewEvent event)
   {
      String iframeId = "mf_" + event.getView().getIdentityParams();
      String resizeIframe = "InfinityBpm.ProcessPortal.resizeProcessDefinitionIFrame('" + iframeId
            + "'" + ",'" + event.getType() + "');";
      JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), resizeIframe);
      PortalApplication.getInstance().addEventScript(resizeIframe);
   }
}
