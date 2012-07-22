package org.eclipse.stardust.ui.web.modeler.portal;

import org.springframework.stereotype.Component;


@Component
public class WebServiceApplicationView extends AbstractAdapterView {
   public WebServiceApplicationView()
   {
      super("/plugins/bpm-modeler/views/modeler/webServiceApplicationView.html", "webServiceApplicationFrameAnchor");
   }
}
