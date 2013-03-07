package org.eclipse.stardust.ui.web.modeler.portal;

import org.springframework.stereotype.Component;

import org.eclipse.stardust.ui.web.common.event.ViewEvent;


@Component
public class GenericApplicationView extends AbstractAdapterView {
   public GenericApplicationView()
   {
      super("/plugins/bpm-modeler/views/modeler/genericApplicationView.html", "genericApplicationFrameAnchor");
   }

   @Override
   public void handleEvent(ViewEvent event)
   {
      super.handleEvent(event);

      switch (event.getType())
      {
      case CREATED:
         event.getView().setIcon("/plugins/bpm-modeler/images/icons/applications-blue.png");
         break;
      }
   }
}
