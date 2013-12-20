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

import javax.servlet.ServletContext;

import com.google.gson.JsonObject;

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
import org.eclipse.stardust.ui.web.processportal.interaction.Interaction;
import org.eclipse.stardust.ui.web.processportal.interaction.iframe.ManualActivityDocumentController;
import org.eclipse.stardust.ui.web.processportal.interaction.iframe.ManualActivityDocumentController.DOCUMENT;
import org.eclipse.stardust.ui.web.processportal.view.manual.ModelUtils;
import org.eclipse.stardust.ui.web.processportal.view.manual.RawDocument;

/**
 * @author Subodh.Godbole
 * @author Yogesh.Manware
 *
 */
public class InteractionDataUtils
{
   private static final Logger trace = LogManager.getLogger(InteractionDataUtils.class);
   

   /**
    * 
    * @param interaction
    * @param inData
    * @param servletContext
    * @param qualifiedResponse
    * @return
    */
   @SuppressWarnings("unchecked")
   public static JsonObject marshalData(Interaction interaction,
         Map<String, ? extends Serializable> inData, ServletContext servletContext, boolean qualifiedResponse)
   {
      Model model = interaction.getModel();
      ApplicationContext context =  interaction.getDefinition();
      
      JsonObject root = new JsonObject();
      JsonHelper jsonHelper = new JsonHelper();
      List<DataMapping> dataMappings = context.getAllInDataMappings();

      for (DataMapping dm : dataMappings)
      {
         for (Entry<String, ? extends Serializable> entry : inData.entrySet())
         {
            if (entry.getKey().equals(dm.getId()))
            {
               JsonObject elemDM = new JsonObject();
               
               if (ModelUtils.isDocumentType(model, dm))
               {
                  jsonHelper.toJsonDocument(entry.getValue(), dm, elemDM, model,
                        interaction, qualifiedResponse);
                  root.add(dm.getId(), elemDM);
               }
               else
               {
                  jsonHelper.toJson(dm.getId(), entry.getValue(), root);
               }
               break;
            }
         }
      }

      return root;
   }

   /**
    * Converts Data back as per the Data Types
    * 
    * @param model
    * @param context
    * @param elem
    * @param interaction
    * @param servletContext
    * @return
    * @throws DataException
    */
   @SuppressWarnings("unchecked")
   public static Map<String, Serializable> unmarshalData(Model model,
         ApplicationContext context, Map<String, Object> elem, Interaction interaction,
         ServletContext servletContext) throws DataException
   {
      Map<String, Throwable> errors = new HashMap<String, Throwable>();

      Map<String, Serializable> ret = new HashMap<String, Serializable>();

      List<DataMapping> dataMappings = context.getAllOutDataMappings();

      for (DataMapping dm : dataMappings)
      {
         for (Entry<String, Object> entry : elem.entrySet())
         {
            if (entry.getKey().equals(dm.getId()))
            {
               try
               {
                  if (trace.isDebugEnabled())
                  {
                     trace.debug("DM: " + entry.getKey());
                  }
   
                  Object value = evaluateClientSideOutMapping(model, entry.getValue(), dm, interaction, servletContext);
   
                  if (trace.isDebugEnabled())
                  {
                     trace.debug(", Value: " + value);
                  }
   
                  if (value instanceof Serializable || value == null)
                  {
                     ret.put(entry.getKey(), (Serializable) value);
                  }
                  else
                  {
                     if (trace.isDebugEnabled())
                     {
                        trace.debug("value is not serializable for " + dm.getId());
                     }
                  }
               }
               catch(Exception e)
               {
                  errors.put(entry.getKey(), e);
               }
               break;
            }
         }
      }

      if (errors.size() > 0)
      {
         throw new DataException(errors);
      }
         
      return ret;
   }

   /**
    * @param model
    * @param value
    * @param outMapping
    * @param interaction 
    * @return
    */
   public static Object evaluateClientSideOutMapping(Model model, Object value,
         DataMapping outMapping, Interaction interaction, ServletContext servletContext)
   {
      Object result = null;

      if (ModelUtils.isDocumentType(model, outMapping))
      {
         if (value instanceof Map<? , ? >)
         {
            Map<String, Object> details = (Map<String, Object>) value;

            ManualActivityDocumentController dc = interaction.getDocumentControllers().get(outMapping.getId());
            if (details.get("deleteDocument") != null)
            {
               // document removed or no change
               dc.delete((String) details.get(DOCUMENT.ID));
            }
            else if (dc.isJCRDocument()) // document is uploaded and saved from viewer
            {
               // JCR document
               result = dc.getJCRDocument();
            }
            else
            {
               // document is uploaded and not saved from viewer
               result = (String) details.get(DOCUMENT.ID);

               // use this data when creating JCR document
               RawDocument rawDocument = new RawDocument();
               rawDocument.setName((String) details.get(DOCUMENT.NAME));
               rawDocument.setDescription((String) details.get(DOCUMENT.DESCRIPTION));
               rawDocument.setComments((String) details.get(DOCUMENT.VERSION_COMMENT));
               dc.setRawDocument(rawDocument);
            }
         }
      }
      else if (ModelUtils.isPrimitiveType(model, outMapping))
      {
         result = (Serializable)DataFlowUtils.unmarshalPrimitiveValue(model, outMapping, value.toString());
      }
      else if (ModelUtils.isStructuredType(model, outMapping))
      {
         Data data = model.getData(outMapping.getDataId());
         if (data.getModelOID() != model.getModelOID())
         {
            model = org.eclipse.stardust.ui.web.viewscommon.utils.ModelUtils.getModel(data.getModelOID());
         }
         result = evaluateStructMapping(model, outMapping, value, outMapping.getApplicationPath());
      }
      else
      {
         // TODO support additional types?
         trace.warn("Ignored unsupported Out Data Mapping - " + outMapping.getId());
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
