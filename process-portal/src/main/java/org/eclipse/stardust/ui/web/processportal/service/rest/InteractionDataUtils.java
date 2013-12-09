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

package org.eclipse.stardust.ui.web.processportal.service.rest;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.ApplicationContext;
import org.eclipse.stardust.engine.api.model.Data;
import org.eclipse.stardust.engine.api.model.DataMapping;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.core.struct.ClientXPathMap;
import org.eclipse.stardust.engine.core.struct.IXPathMap;
import org.eclipse.stardust.engine.core.struct.StructuredDataConverter;
import org.eclipse.stardust.engine.core.struct.StructuredDataXPathUtils;
import org.eclipse.stardust.engine.core.struct.TypedXPath;
import org.eclipse.stardust.engine.core.struct.sxml.Document;
import org.eclipse.stardust.engine.core.struct.sxml.Element;
import org.eclipse.stardust.engine.core.struct.sxml.Node;
import org.eclipse.stardust.ui.web.processportal.view.manual.ModelUtils;

/**
 * @author Subodh.Godbole
 *
 */
public class InteractionDataUtils
{
   private static final Logger trace = LogManager.getLogger(InteractionDataUtils.class);

   /**
    * Converts Data back as per the Data Types
    * @param elem
    * @param context
    * @return
    */
   @SuppressWarnings("unchecked")
   public static Map<String, Serializable> unmarshalData(Model model, ApplicationContext context, Map<String, Object> elem)
   {
      Map<String, Serializable> ret = new HashMap<String, Serializable>();

      List<DataMapping> dataMappings = context.getAllOutDataMappings();

      for (DataMapping dm : dataMappings)
      {
         for (Entry<String, Object> entry : elem.entrySet())
         {
            if (entry.getKey().equals(dm.getId()))
            {
               if (trace.isDebugEnabled())
               {
                  trace.debug("DM: " + entry.getKey());
               }

               Object value = evaluateClientSideOutMapping(model, entry.getValue(), dm);

               if (trace.isDebugEnabled())
               {
                  trace.debug(", Value: " + value);
               }

               ret.put(entry.getKey(), (Serializable)value);
               break;
            }
         }
      }

      return ret;
   }

   /**
    * @param model
    * @param value
    * @param outMapping
    * @return
    */
   public static Object evaluateClientSideOutMapping(Model model, Object value, DataMapping outMapping)
   {
      Object result = null;

      if (ModelUtils.isStructuredType(model, outMapping))
      {
         Data data = model.getData(outMapping.getDataId());
         if (data.getModelOID() != model.getModelOID())
         {
            model = org.eclipse.stardust.ui.web.viewscommon.utils.ModelUtils.getModel(data.getModelOID());
         }
         result = evaluateStructMapping(model, outMapping, value, outMapping.getApplicationPath());
      }
      else if (ModelUtils.isPrimitiveType(model, outMapping))
      {
         result = (Serializable)DataFlowUtils.unmarshalPrimitiveValue(model, outMapping, value.toString());
      }
      else
      {
         // TODO support additional types?
      }

      return result;
   }
   
   /**
    * This code was copied from
    *   org.eclipse.stardust.ui.web.viewscommon.utils.ClientSideDataFlowUtils#evaluateStructOutMapping(...)
    * and stripped down.
    *
    */
   private static Object evaluateStructMapping(Model model, DataMapping mapping, Object data, String outPath)
   {
      Set<TypedXPath> xPaths = ModelUtils.getXPaths(model, mapping);

      final IXPathMap xPathMap = new ClientXPathMap(xPaths);

      StructuredDataConverter converter = new StructuredDataConverter(xPathMap);
      Document document;

      Node[] nodes = converter.toDom(data, "", true);
      Assert.condition(nodes.length == 1);
      document = new Document((Element) nodes[0]);

      boolean namespaceAware = StructuredDataXPathUtils.isNamespaceAware(document);

      Object returnValue = converter.toCollection(document.getRootElement(), outPath, namespaceAware);

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
}
