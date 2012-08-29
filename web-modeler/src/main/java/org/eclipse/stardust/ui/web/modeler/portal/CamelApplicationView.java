package org.eclipse.stardust.ui.web.modeler.portal;

import org.springframework.stereotype.Component;

import org.eclipse.stardust.ui.web.common.event.ViewEvent;


@Component
public class CamelApplicationView extends AbstractAdapterView {
   public CamelApplicationView()
   {
      super("/plugins/bpm-modeler/views/modeler/camelApplicationView.html", "camelApplicationFrameAnchor");
   }
   
   @Override
   public void handleEvent(ViewEvent event)
   {
      super.handleEvent(event);
      
      event.getView().setIcon("/plugins/bpm-modeler/images/icons/applications.png");
   }  
}
