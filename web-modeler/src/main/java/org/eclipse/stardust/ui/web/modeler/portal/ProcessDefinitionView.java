package org.eclipse.stardust.ui.web.modeler.portal;

import org.springframework.stereotype.Component;

@Component
public class ProcessDefinitionView extends AbstractAdapterView {
   /**
    * 
    */
   public ProcessDefinitionView()
   {
      super("/plugins/bpm-modeler/views/modeler/processDefinitionView.html", "processDefinitionFrameAnchor");
   }
}
