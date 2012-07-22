package org.eclipse.stardust.ui.web.modeler.portal;

import org.springframework.stereotype.Component;

@Component
public class XsdStructuredDataTypeView extends AbstractAdapterView
{
   /**
    * 
    */
   public XsdStructuredDataTypeView()
   {
      super("/plugins/bpm-modeler/views/modeler/xsdStructuredDataTypeView.html",
            "xsdStructuredDataTypeFrameAnchor");
   }
}
