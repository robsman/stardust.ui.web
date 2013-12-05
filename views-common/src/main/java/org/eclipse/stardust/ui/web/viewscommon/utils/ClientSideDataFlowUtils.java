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
package org.eclipse.stardust.ui.web.viewscommon.utils;

import static org.eclipse.stardust.common.StringUtils.isEmpty;

import java.util.Map;
import java.util.Set;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.AccessPoint;
import org.eclipse.stardust.engine.api.model.Data;
import org.eclipse.stardust.engine.api.model.DataMapping;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.model.TypeDeclaration;
import org.eclipse.stardust.engine.core.pojo.data.Type;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.AccessPathEvaluationContext;
import org.eclipse.stardust.engine.core.struct.ClientXPathMap;
import org.eclipse.stardust.engine.core.struct.IXPathMap;
import org.eclipse.stardust.engine.core.struct.StructuredDataConstants;
import org.eclipse.stardust.engine.core.struct.StructuredDataConverter;
import org.eclipse.stardust.engine.core.struct.StructuredDataXPathUtils;
import org.eclipse.stardust.engine.core.struct.StructuredTypeRtUtils;
import org.eclipse.stardust.engine.core.struct.TypedXPath;
import org.eclipse.stardust.engine.core.struct.spi.StructuredDataXPathEvaluator;
import org.eclipse.stardust.engine.core.struct.sxml.Document;
import org.eclipse.stardust.engine.core.struct.sxml.Element;
import org.eclipse.stardust.engine.core.struct.sxml.Node;


public class ClientSideDataFlowUtils
{
   private static final Logger trace = LogManager.getLogger(ClientSideDataFlowUtils.class);

   public static Object evaluateClientSideInMapping(Model model, Object accessPoint,
         DataMapping inMapping, Object inValue)
   {
      Object result = null;

      if (isStructuredType(model, inMapping.getApplicationAccessPoint()))
      {
         Data data = model.getData(inMapping.getDataId());
         if (data.getModelOID() != model.getModelOID())
         {
            model = org.eclipse.stardust.ui.web.viewscommon.utils.ModelUtils.getModel(data.getModelOID());
         }
         result = evaluateStructInMapping(model, inMapping.getApplicationAccessPoint(),
               accessPoint, inMapping.getApplicationPath(), inValue);
      }
      else if (isPrimitiveType(model, inMapping.getApplicationAccessPoint()))
      {
         // TODO
         if (isEmpty(inMapping.getApplicationPath()))
         {
            result = inValue;
         }
         else
         {
            // TODO support primitive deref?
            throw new IllegalArgumentException("");
         }
      }
      else
      {
         // TODO support additional types?
      }

      return result;
   }

   public static Object evaluateClientSideOutMapping(Model model, Object value,
         DataMapping outMapping)
   {
      Object result = null;

      if (isStructuredType(model, outMapping.getApplicationAccessPoint()))
      {
         Data data = model.getData(outMapping.getDataId());
         if (data.getModelOID() != model.getModelOID())
         {
            model = org.eclipse.stardust.ui.web.viewscommon.utils.ModelUtils.getModel(data.getModelOID());
         }
         result = evaluateStructOutMapping(model, outMapping.getApplicationAccessPoint(),
               value, outMapping.getApplicationPath());
      }
      else if (isPrimitiveType(model, outMapping.getApplicationAccessPoint()))
      {
         // TODO
         if (isEmpty(outMapping.getApplicationPath()))
         {
            result = value;
         }
         else
         {
            // TODO support primitive deref?
         }
      }
      else
      {
         // TODO support additional types?
      }

      return result;
   }

   /**
    * This code was copied from
    * {@link StructuredDataXPathEvaluator#evaluate(org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint, Object, String, org.eclipse.stardust.engine.core.spi.extensions.runtime.AccessPathEvaluationContext)}
    * and stripped down.
    *
    * @TODO merge back
    *
    */
   private static Object evaluateStructOutMapping(Model model,
         AccessPoint accessPointDefinition, Object accessPointInstance, String outPath)
   {
      TypeDeclaration typeDeclaration = model.getTypeDeclaration((String) accessPointDefinition.getAttribute(StructuredDataConstants.TYPE_DECLARATION_ATT));

      @SuppressWarnings("unchecked")
      Set<TypedXPath> xPaths = StructuredTypeRtUtils.getAllXPaths(model, typeDeclaration);

      final IXPathMap xPathMap = new ClientXPathMap(xPaths);

      StructuredDataConverter converter = new StructuredDataConverter(xPathMap);
      Document document;

      // data value is in accessPointInstance
      Node[] nodes = converter.toDom(accessPointInstance, "", true);
      Assert.condition(nodes.length == 1);
      document = new Document((Element) nodes[0]);

      boolean namespaceAware = StructuredDataXPathUtils.isNamespaceAware(document);

      Object returnValue = converter.toCollection(document.getRootElement(), outPath,
            namespaceAware);
      if (trace.isDebugEnabled())
      {
         if (null == returnValue)
         {
            trace.debug("returning null for outPath '" + outPath + "'");
         }
         else
         {
            trace.debug("returning returnValue of type '"
                  + returnValue.getClass().getName() + "' for outPath '" + outPath + "'");
         }
      }

      return returnValue;
   }

   /**
    * This code was copied from
    * {@link StructuredDataXPathEvaluator#evaluate(org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint, Object, String, AccessPathEvaluationContext, Object)}
    * and stripped down.
    *
    * @TODO merge back
    *
    */
   private static Object evaluateStructInMapping(Model model,
         AccessPoint accessPointDefinition, Object accessPointInstance, String inPath, Object value)
   {
      TypeDeclaration typeDeclaration = model.getTypeDeclaration((String) accessPointDefinition.getAttribute(StructuredDataConstants.TYPE_DECLARATION_ATT));

      @SuppressWarnings("unchecked")
      Set<TypedXPath> xPaths = StructuredTypeRtUtils.getAllXPaths(model, typeDeclaration);

      final IXPathMap xPathMap = new ClientXPathMap(xPaths);

      // always operate with namespaceAware=true
      final boolean namespaceAware = true;

      StructuredDataConverter converter = new StructuredDataConverter(xPathMap);
      Document document;
      if (accessPointInstance == null)
      {
         // data value is being set for the first time, create it first
         document = new Document(StructuredDataXPathUtils.createElement(xPathMap.getRootXPath(), namespaceAware));
      }
      else
      {
         Node [] nodes = converter.toDom(accessPointInstance, "", namespaceAware);
         Assert.condition(nodes.length == 1);
         document = new Document((Element)nodes[0]);
      }

      if (trace.isDebugEnabled())
      {
         trace.debug("document before change: " + document.toXML());
      }

      StructuredDataXPathUtils.putValue(document, xPathMap, inPath, value,
            namespaceAware, true);

      if (trace.isDebugEnabled())
      {
         trace.debug("document after change: " + document.toXML());
      }

      // can be casted to map since XPath is empty (root XPath)
      Map newAccessPointInstance = (Map) converter.toCollection(document.getRootElement(), "", namespaceAware);
      if (accessPointInstance == null)
      {
         accessPointInstance = newAccessPointInstance;
      }
      else if (accessPointInstance instanceof Map)
      {
         // update accessPointInstance (do not just return another instance, try "refilling"
         // the existing one
         ((Map)accessPointInstance).clear();
         ((Map)accessPointInstance).putAll(newAccessPointInstance);
      }
      else
      {
         throw new InternalException("Unexpected accessPointInstance class: '"+accessPointInstance.getClass().getName()+"'");
      }

      return accessPointInstance;
   }

   public static boolean isStructuredType(Model model, AccessPoint accessPoint)
   {
      return null != accessPoint.getAttribute(StructuredDataConstants.TYPE_DECLARATION_ATT);
   }

   public static boolean isPrimitiveType(Model model, AccessPoint ap)
   {
      return (ap.getAttribute(PredefinedConstants.TYPE_ATT) instanceof Type);
   }

}
