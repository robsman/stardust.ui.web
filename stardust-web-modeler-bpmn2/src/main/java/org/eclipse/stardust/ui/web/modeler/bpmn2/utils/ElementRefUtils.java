package org.eclipse.stardust.ui.web.modeler.bpmn2.utils;

import static org.eclipse.stardust.ui.web.modeler.bpmn2.Bpmn2Utils.getModelUuid;

import org.eclipse.bpmn2.BaseElement;
import org.eclipse.bpmn2.Definitions;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.ui.web.modeler.bpmn2.Bpmn2Utils;

public class ElementRefUtils
{
   public static String encodeReference(BaseElement element)
   {
      return encodeReference(Bpmn2Utils.findContainingModel(element), element.getId());
   }

   public static String encodeReference(Definitions model, BaseElement element)
   {
      return encodeReference(model, element.getId());
   }

   public static String encodeReference(Definitions model, String elementId)
   {
      return getModelUuid(model) + ":" + elementId;
   }

   public static String resolveModelIdFromReference(String encodedReference)
   {
      int colonIdx = encodedReference.indexOf(':');
      if (colonIdx <= 0)
      {
         throw new PublicException("Illegal reference value (contains no model ID): " + encodedReference);
      }
      else
      {
         return encodedReference.substring(0, colonIdx);
      }
   }

   public static String resolveElementIdFromReference(String encodedReference)
   {
      int colonIdx = encodedReference.indexOf(':');
      if (colonIdx <= 0)
      {
         throw new PublicException("Illegal reference value (contains no model ID): " + encodedReference);
      }
      else
      {
         return encodedReference.substring(colonIdx + 1);
      }
   }
}
