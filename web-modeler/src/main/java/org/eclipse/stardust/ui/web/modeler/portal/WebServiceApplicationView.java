package org.eclipse.stardust.ui.web.modeler.portal;

import org.springframework.stereotype.Component;

import org.eclipse.stardust.ui.web.common.event.ViewEvent;


@Component
public class WebServiceApplicationView extends AbstractAdapterView {
   public WebServiceApplicationView()
   {
      super("/plugins/bpm-modeler/views/modeler/webServiceApplicationView.html", "webServiceApplicationFrameAnchor",
            "applicationName");
   }

   @Override
   public void handleEvent(ViewEvent event)
   {
      super.handleEvent(event);

      switch (event.getType())
      {
      case CREATED:
         event.getView().setIcon(
               "/plugins/bpm-modeler/images/icons/application-web-service.png");
         break;
      }
   }
}
