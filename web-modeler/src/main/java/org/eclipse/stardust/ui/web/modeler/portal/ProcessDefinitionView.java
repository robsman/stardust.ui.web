package org.eclipse.stardust.ui.web.modeler.portal;

import org.springframework.stereotype.Component;

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
      }
   }

}
