package org.eclipse.stardust.ui.web.modeler.portal;

import org.springframework.stereotype.Component;

import org.eclipse.stardust.ui.web.common.event.ViewEvent;

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

   @Override
   public void handleEvent(ViewEvent event)
   {
      super.handleEvent(event);
      
      event.getView().setIcon("/plugins/bpm-modeler/images/icons/struct_data.gif");
   }
}
