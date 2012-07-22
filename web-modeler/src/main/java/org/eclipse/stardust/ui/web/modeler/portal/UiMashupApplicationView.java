package org.eclipse.stardust.ui.web.modeler.portal;

import org.springframework.stereotype.Component;


@Component
public class UiMashupApplicationView extends AbstractAdapterView {
   public UiMashupApplicationView()
   {
      super("/plugins/bpm-modeler/views/modeler/uiMashupApplicationView.html", "uiMashupApplicationFrameAnchor");
   }
}
