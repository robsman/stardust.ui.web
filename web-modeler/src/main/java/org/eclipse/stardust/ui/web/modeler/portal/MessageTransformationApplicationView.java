package org.eclipse.stardust.ui.web.modeler.portal;

import org.springframework.stereotype.Component;

import org.eclipse.stardust.ui.web.common.event.ViewEvent;
import org.eclipse.stardust.ui.web.modeler.service.ModelService;

@Component
public class MessageTransformationApplicationView extends AbstractAdapterView
{
   /**
    *
    */
   public MessageTransformationApplicationView()
   {
      super(
            "/plugins/bpm-modeler/views/modeler/messageTransformationApplicationView.html",
            "messageTransformationApplicationFrameAnchor");
   }

   @Override
   public void handleEvent(ViewEvent event)
   {
      super.handleEvent(event);

      event.getView().setIcon("/plugins/bpm-modeler/images/icons/application-message-trans.png");
   }
}
