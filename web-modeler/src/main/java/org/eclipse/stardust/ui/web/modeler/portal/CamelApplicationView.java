package org.eclipse.stardust.ui.web.modeler.portal;

import org.springframework.stereotype.Component;


@Component
public class CamelApplicationView extends AbstractAdapterView {
   public CamelApplicationView()
   {
      super("/plugins/bpm-modeler/views/modeler/camelApplicationView.html", "camelApplicationFrameAnchor");
   }
}
