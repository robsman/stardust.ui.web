package org.eclipse.stardust.ui.web.modeler.portal;

import org.springframework.stereotype.Component;

import org.eclipse.stardust.ui.web.common.event.ViewEvent;


@Component
public class UiMashupApplicationView extends AbstractAdapterView {
   public UiMashupApplicationView()
   {
      super("/plugins/bpm-modeler/views/modeler/uiMashupApplicationView.html", "uiMashupApplicationFrameAnchor");
   }

   @Override
   public void handleEvent(ViewEvent event)
   {
      super.handleEvent(event);

      event.getView().setIcon("/plugins/bpm-modeler/images/icons/application-c-ext-web.png");
   }
}
