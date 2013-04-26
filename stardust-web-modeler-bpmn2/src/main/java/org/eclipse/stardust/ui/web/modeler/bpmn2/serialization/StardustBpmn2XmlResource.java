package org.eclipse.stardust.ui.web.modeler.bpmn2.serialization;

import org.eclipse.bpmn2.util.Bpmn2ResourceImpl;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.xmi.XMLLoad;

public class StardustBpmn2XmlResource extends Bpmn2ResourceImpl
{
   public StardustBpmn2XmlResource(URI uri)
   {
      super(uri);
   }

   @Override
   protected XMLLoad createXMLLoad()
   {
      return new StardustBpmn2XmlLoad(createXMLHelper());
   }

}
