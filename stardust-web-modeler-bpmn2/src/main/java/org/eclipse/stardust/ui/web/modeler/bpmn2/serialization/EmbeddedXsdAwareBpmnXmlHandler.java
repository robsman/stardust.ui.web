/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.modeler.bpmn2.serialization;

import java.io.StringReader;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

import org.eclipse.bpmn2.BaseElement;
import org.eclipse.bpmn2.ExtensionAttributeValue;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.XMLHelper;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.core.model.beans.ModelBean;
import org.eclipse.stardust.engine.core.model.beans.XMLConstants;
import org.eclipse.stardust.model.xpdl.xpdl2.XpdlPackage;
import org.eclipse.stardust.ui.web.modeler.bpmn2.utils.Bpmn2ExtensionUtils;
import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.util.XSDConstants;
import org.eclipse.xsd.util.XSDParser;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class EmbeddedXsdAwareBpmnXmlHandler extends StardustBpmnXmlHandler
{
   private boolean inSchema = false;

   private EmbeddedXsdParser xsdParser = new EmbeddedXsdParser();

   private Stack<Map<String, String>> namespaces = new Stack<Map<String, String>>();

   private Map<String, String> current = null;

   private int schemaElementCount = 0;

   // MUST be kept in ascending order
   private static final String[] SCHEMA_KEYWORDS_45 = {
      "complexType", "element", "enumeration", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      "restriction", "schema", "sequence", "simpleType"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

   private boolean isSchemaKeyword45(String name)
   {
      return Arrays.binarySearch(SCHEMA_KEYWORDS_45, name) >= 0;
   }

   public EmbeddedXsdAwareBpmnXmlHandler(XMLResource xmiResource, XMLHelper helper, Map<?, ?> options)
   {
      super(xmiResource, helper, options);
      // TODO this.hrefAttribute = WorkflowModelManager.PROXY_ATT;
      // TODO xmiResource.eAdapters().add(new SchemaLocatorAdapter());
   }

   @Override
   protected EPackage getPackageForURI(String uriString)
   {
      EPackage ePackage = super.getPackageForURI(uriString);
      if (ePackage == null)
         return null;
      else
         return ePackage;
   }

   @Override
   public void startPrefixMapping(String prefix, String uri)
   {
      // eat prefix mappings
      if (current == null)
      {
         this.current = new TreeMap<String, String>();
      }
      current.put(prefix,  uri);
   }

   @Override
   public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException
   {
      if (isSchemaKeyword45(localName) && isXpdlNamespace(uri))
      {
         uri = XMLResource.XML_SCHEMA_URI;
      }
      if (current != null && !inSchema && !isSchemaElement(uri))
      {
         // fire start prefix mappings
         for (Map.Entry<String, String> entry : current.entrySet())
         {
            super.startPrefixMapping(entry.getKey(), entry.getValue());
         }
      }
      super.startElement(uri, localName, name, attributes);
   }

   @Override
   @Deprecated
   protected void handleNamespaceAttribs()
   {
      super.handleNamespaceAttribs();
   }

   @Override
   public void startElement(String uri, String localName, String name)
   {
      namespaces.push(current == null ? CollectionUtils.<String, String>newMap() : current);
      if (!inSchema && isSchemaElement(uri))
      {
         startXsdDocument();
      }
      if (inSchema)
      {
         schemaElementCount++;
         elements.push(name);
         try
         {
            xsdParser.startElement(uri, localName, name, attribs);
            if (current != null)
            {
               for (Map.Entry<String, String> entry : current.entrySet())
               {
                  String key = entry.getKey();
                  xsdParser.declareNamespace(entry.getValue(), key);
               }
            }
            // declare all used namespaces that are not yet declared
            declareNamespaceForQName(name, uri);
            for (int i = 0, l = attribs.getLength(); i < l; i++)
            {
               declareNamespaceForQName(attribs.getQName(i), attribs.getURI(i));
            }
         }
         catch (Throwable e)
         {
            // TODO: propagate error
            e.printStackTrace();
         }
      }
      else
      {
         super.startElement(uri, localName, name);
      }
      current = null;
   }

   private void declareNamespaceForQName(String qname, String uri)
   {
      if (!uri.isEmpty())
      {
         String prefix = getPrefix(qname);
         for (int i = namespaces.size() - 1, l = schemaElementCount; i >= 0; i--, l--)
         {
            Map<String, String> ns = namespaces.get(i);
            if (ns.containsKey(prefix))
            {
               if (l <= 0)
               {
                  xsdParser.declareNamespace(uri, prefix);
               }
               break;
            }
         }
      }
   }

   private void startXsdDocument()
   {
      inSchema = true;
      xsdParser.startDocument();
   }

   private String getPrefix(String name)
   {
      int ix = name.indexOf(':');
      return ix < 0 ? "" : name.substring(0, ix); //$NON-NLS-1$
   }

   private boolean isSchemaElement(String uri)
   {
      return XMLResource.XML_SCHEMA_URI.equals(uri);
   }

   private boolean isXpdlNamespace(String uri)
   {
      return XpdlPackage.eNS_URI.equals(uri);
   }

   @Override
   public void endPrefixMapping(String prefix)
   {
      // do nothing, events are explicitly fired after endElement
   }

   @Override
   public void endElement(String uri, String localName, String name)
   {
      if (isSchemaKeyword45(localName) && isXpdlNamespace(uri))
      {
         uri = XMLResource.XML_SCHEMA_URI;
      }
      Map<String, String> expired = namespaces.pop();
      if (inSchema)
      {
         schemaElementCount--;
         elements.pop();
         try
         {
            xsdParser.endElement(uri, localName, name);
            if (schemaElementCount == 0)
            {
               inSchema = false;
               xsdParser.endDocument();
               Object peek = objects.peek();
               if (peek instanceof ExtensionAttributeValue)
               {
                  XSDSchema schema = xsdParser.getSchema();
                  ((InternalEObject) schema).eSetResource((Resource.Internal) xmlResource, null);
                  // TODO ((BaseElement) peek).setSchema(schema);
                  Bpmn2ExtensionUtils.setExtensionValue(
                        (BaseElement) ((ExtensionAttributeValue) peek).eContainer(), "schema",
                        javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI, schema);
               }
            }
         }
         catch (Throwable e)
         {
            // TODO: propagate error
            e.printStackTrace();
         }
      }
      else
      {
         super.endElement(uri, localName, name);
         if (!inSchema)
         {
            // fire end prefix mappings
            for (String entry : expired.keySet())
            {
               super.endPrefixMapping(entry);
            }
         }
      }
   }

   @Override
   public void startEntity(String name)
   {
      if (inSchema)
      {
         xsdParser.startEntity(name);
      }
      else
      {
         super.startEntity(name);
      }
   }

   @Override
   public void endEntity(String name)
   {
      if (inSchema)
      {
         xsdParser.endEntity(name);
      }
      else
      {
         super.endEntity(name);
      }
   }

   @Override
   public void comment(char[] ch, int start, int length)
   {
      // (fh) should we ignore comments?
      if (inSchema)
      {
         try
         {
            xsdParser.comment(ch, start, length);
         }
         catch (SAXException e)
         {
            // TODO: propagate error
            e.printStackTrace();
         }
      }
      else
      {
         super.comment(ch, start, length);
      }
   }

   @Override
   public void startCDATA()
   {
      if (inSchema)
      {
         xsdParser.startCDATA();
      }
      else
      {
         super.startCDATA();
      }
   }

   @Override
   public void endCDATA()
   {
      if (inSchema)
      {
         xsdParser.endCDATA();
      }
      else
      {
         super.endCDATA();
      }
   }

   @Override
   public InputSource resolveEntity(String publicId, String systemId) throws SAXException
   {
      if (null != systemId)
      {
         if (XMLConstants.WORKFLOWMODEL_30_DTD_URL.equals(systemId)
               || XMLConstants.WORKFLOWMODEL_31_DTD_URL.equals(systemId)
               || systemId.endsWith(XMLConstants.DTD_NAME))
         {
            // strip old DTD (not doing so would include the DTD inline on save)
            return new InputSource(new StringReader("")); //$NON-NLS-1$
         }
         else if (XMLConstants.WORKFLOWMODEL_31_XSD_URL.equals(systemId)
               || systemId.endsWith(XMLConstants.WORKFLOWMODEL_XSD))
         {
            try
            {
               URL xsdUrl = ModelBean.class.getResource(XMLConstants.WORKFLOWMODEL_XSD);
               if (null != xsdUrl)
               {
                  return new InputSource(xsdUrl.openStream());
               }
            }
            catch (Exception e)
            {
               // e.printStackTrace();
            }
         }
      }
      return inSchema
            ? xsdParser.resolveEntity(publicId, systemId)
            : super.resolveEntity(publicId, systemId);
   }

   @Override
   public void processingInstruction(String target, String data)
   {
      if (inSchema)
      {
         xsdParser.processingInstruction(target, data);
      }
      else
      {
         super.processingInstruction(target, data);
      }
   }

   @Override
   public void characters(char[] ch, int start, int length)
   {
      if (inSchema)
      {
         try
         {
            xsdParser.characters(ch, start, length);
         }
         catch (SAXException e)
         {
            // TODO: propagate error
            e.printStackTrace();
         }
      }
      else
      {
         super.characters(ch, start, length);
      }
   }

   class EmbeddedXsdParser extends XSDParser
   {
      public EmbeddedXsdParser()
      {
         super(null);
      }

      @Override
      public void characters(char[] ch, int start, int length) throws SAXException
      {
         // (fh) size control
         if (ch != null && start >= 0 && length > 0 && ch.length >= start + length)
         {
            try
            {
               while (start < length && Character.isWhitespace(ch[start]))
               {
                  start++;
                  length--;
               }
               while (length > 0 && Character.isWhitespace(ch[start + length -1]))
               {
                  length--;
               }
               if (length > 0)
               {
                  super.characters(ch, start, length);
               }
            }
            catch (Throwable e)
            {
               // TODO: propagate error
               e.printStackTrace();
            }
         }
      }

      public void declareNamespace(String uri, String prefix)
      {
         String attributeURI = XSDConstants.XMLNS_URI_2000;
         String attributeQName = XMLResource.XML_NS;
         if (prefix.length() > 0)
         {
            attributeQName = attributeQName + ':' + prefix;
         }
         String attributeValue = uri;
         element.setAttributeNS(attributeURI, attributeQName, attributeValue);
      }
   }
}
