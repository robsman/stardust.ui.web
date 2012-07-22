package org.eclipse.stardust.ui.web.modeler.portal;

import org.springframework.stereotype.Component;

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
}
