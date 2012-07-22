package org.eclipse.stardust.ui.web.modeler.portal;

import org.springframework.stereotype.Component;


@Component
public class GenericApplicationView extends AbstractAdapterView {
   public GenericApplicationView()
   {
      super("/plugins/bpm-modeler/views/modeler/genericApplicationView.html", "genericApplicationFrameAnchor");
   }
}
