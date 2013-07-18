package org.eclipse.stardust.ui.web.modeler.bpmn2.serialization;

import org.eclipse.bpmn2.Bpmn2Package;
import org.eclipse.bpmn2.util.Bpmn2ResourceImpl;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.xmi.XMLHelper;
import org.eclipse.emf.ecore.xmi.impl.XMLSaveImpl;
import org.eclipse.xsd.XSDComponent;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.Element;
import org.w3c.dom.Entity;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

public class StardustBpmn2XmlSave extends XMLSaveImpl
{

   public StardustBpmn2XmlSave(XMLHelper helper)
   {
      super(helper);
   }

   /**
    * Adopted from {@link Bpmn2ResourceImpl#createXMLSave}
    */
   @Override
   protected boolean shouldSaveFeature(EObject o, EStructuralFeature f)
   {
      if (Bpmn2Package.eINSTANCE.getDocumentation_Text().equals(f))
         return false;
      if (Bpmn2Package.eINSTANCE.getFormalExpression_Body().equals(f))
         return false;
      return super.shouldSaveFeature(o, f);
   }

   protected void saveElement(EObject o, EStructuralFeature f)
   {
      if (o instanceof XSDComponent)
      {
         XSDComponent component = (XSDComponent) o;
         component.updateElement();
         if (toDOM)
         {
            currentNode.appendChild(document.importNode(component.getElement(), true));
         }
         else
         {
            Element element = component.getElement();
            saveDomElement(element);
         }
         return;
      }

      super.saveElement(o, f);
   }

   private void saveDomElement(Element element)
   {
      boolean empty = true;
      doc.startElement(element.getTagName());
      NamedNodeMap attributes = element.getAttributes();
      for (int i = 0; i < attributes.getLength(); i++ )
      {
         Attr attribute = (Attr) attributes.item(i);
         doc.addAttribute(attribute.getName(), attribute.getValue());
      }
      NodeList list = element.getChildNodes();
      for (int i = 0; i < list.getLength(); i++ )
      {
         Node node = list.item(i);
         if (node instanceof Element)
         {
            empty = false;
            saveDomElement((Element) node);
         }
         else if (node instanceof Text)
         {
            empty = false;
            doc.addText(stripWhitespace(((Text) node).getData()));
         }
         else if (node instanceof CDATASection)
         {
            empty = false;
            doc.addCDATA(((CDATASection) node).getData());
         }
         else if (node instanceof Comment)
         {
            empty = false;
            doc.addComment(((Comment) node).getData());
         }
         else if (node instanceof Entity)
         {
            // ignore
         }
         else if (node instanceof ProcessingInstruction)
         {
            // ignore
         }
      }
      if (empty)
      {
         doc.endEmptyElement();
      }
      else
      {
         doc.endElement();
      }
   }

   private String stripWhitespace(String value)
   {
      int begin = 0;
      int end = value.length();
      while (end > begin && Character.isWhitespace(value.charAt(end - 1)))
      {
         end-- ;
      }
      if (end < 0)
      {
         return ""; //$NON-NLS-1$
      }
      while (begin < end && Character.isWhitespace(value.charAt(begin)))
      {
         begin++ ;
      }
      return value.substring(begin, end);
   }
}
