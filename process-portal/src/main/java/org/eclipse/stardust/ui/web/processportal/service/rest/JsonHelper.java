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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.DataMapping;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.runtime.DeployedModel;
import org.eclipse.stardust.engine.extensions.dms.data.DocumentType;
import org.eclipse.stardust.ui.web.common.util.DateUtils;
import org.eclipse.stardust.ui.web.processportal.interaction.Interaction;
import org.eclipse.stardust.ui.web.processportal.interaction.iframe.ManualActivityDocumentController;
import org.eclipse.stardust.ui.web.processportal.interaction.iframe.ManualActivityDocumentController.DOCUMENT;
import org.eclipse.stardust.ui.web.viewscommon.utils.TypedDocumentsUtil;


/**
 * @author Subodh.Godbole
 * @author Yogesh.Manware
 *
 */
public class JsonHelper
{
   private static final Logger trace = LogManager.getLogger(InteractionDataUtils.class);

   private String dateFormat = "yyyy-MM-dd'T'HH:mm:ss";
  
   /**
    * 
    */
   public JsonHelper()
   {
   }
   
   /**
    * @param dateFormat
    */
   public JsonHelper(String dateFormat)
   {
      this.dateFormat = dateFormat;
   }

   /*
    * 
    */
   public void toJson(Map<String, ? extends Serializable> data, JsonObject parent)
   {
      for (Entry<String, ? extends Serializable> entry : data.entrySet())
      {
         if (null == entry.getValue())
         {
            continue;
         }
         toJson(entry.getKey(), entry.getValue(), parent);
      }
   }
   
   /**
    * 
    * @param document
    * @param dm
    * @param elemDM
    * @param model
    * @param interaction
    * @param qualifiedResponse
    */
   public void toJsonDocument(Object document, DataMapping dm, JsonObject elemDM, Model model, Interaction interaction,
         boolean qualifiedResponse)
   {
      trace.debug("create document json...");

      // In case of mobile - where document data is not supported - the document controllers
      // are not set. Hence the following check.
      if (null == interaction.getDocumentControllers())
      {
         return;
      }
      
      ManualActivityDocumentController dc = interaction.getDocumentControllers().get(dm.getId());

      if (dc.isJCRDocument())
      {
         elemDM.add(DOCUMENT.ID, new JsonPrimitive(dc.getDocument().getId()));
         elemDM.add(DOCUMENT.NAME, new JsonPrimitive(dc.getDocument().getName()));
      }

      if (qualifiedResponse)
      {
         trace.debug("created quailified response...");
         return;
      }

      DocumentType docType = dc.getDocumentType();

      if (docType != null)
      {
         elemDM.add(DOCUMENT.TYPE_ID, new JsonPrimitive(docType.getDocumentTypeId()));
         elemDM.add(DOCUMENT.TYPE_NAME,
               new JsonPrimitive(TypedDocumentsUtil.getDocumentTypeLabel(docType, (DeployedModel) model)));
      }

      elemDM.add(DOCUMENT.DOC_INTERACTION_ID, new JsonPrimitive(dc.getDocInteractionId()));
      elemDM.add(DOCUMENT.PROCESS_INSTANCE_OID, new JsonPrimitive(interaction.getActivityInstance()
            .getProcessInstanceOID()));

      elemDM.add(DOCUMENT.DATA_ID, new JsonPrimitive(dc.getDataMapping().getDataId()));

      if (dc.getDataMapping().getDataPath() != null)
      {
         elemDM.add(DOCUMENT.DATA_PATH_ID, new JsonPrimitive(dc.getDataMapping().getDataPath()));
      }

      elemDM.add(DOCUMENT.ICON, new JsonPrimitive(dc.getIconPath()));
   }
   
  /**
   *  
   * @param key
   * @param value
   * @param parent
   */
   public void toJson(String key, Object value, JsonObject parent)
   {  
      if (value instanceof Map)
      {
         JsonObject json = new JsonObject();
         parent.add(key, json);
         toJson((Map)value, json);
      }
      else if (value instanceof List)
      {
         JsonArray json = new JsonArray();
         parent.add(key, json);
         toJson((List)value, json);
      }
      else if(value != null) // Primitive
      {
         JsonPrimitive primitive = toJson(value);
         parent.add(key, primitive);
      }
   }
   
   
   /*
    * 
    */
   @SuppressWarnings({"unchecked", "rawtypes"})
   public void toJson(List<? extends Serializable> data, JsonArray parent)
   {
      for (Serializable value : data)
      {
         if (null == value)
         {
            continue;
         }

         if (value instanceof Map)
         {
            JsonObject json = new JsonObject();
            parent.add(json);
            toJson((Map)value, json);
         }
         else if (value instanceof List)
         {
            JsonArray json = new JsonArray();
            parent.add(json);
            toJson((List)value, json);
         }
         else // Primitive
         {
            JsonPrimitive primitive = toJson(value);
            if (null != primitive)
            {
               parent.add(primitive);
            }
         }
      }
   }

   /*
    * 
    */
   public JsonPrimitive toJson(Object value)
   {
      JsonPrimitive ret = null;

      try
      {
         if (value instanceof Double)
         {
            Double doubleValue = ((Number)value).doubleValue();
            if (!Double.isInfinite(doubleValue) && !Double.isNaN(doubleValue))
            {
               if (doubleValue.toString().contains("E") || doubleValue.toString().contains("e"))
               {
                  BigDecimal decimalValue = new BigDecimal(doubleValue);
                  ret = new JsonPrimitive(decimalValue.toPlainString());
               }
               else
               {
                  ret = new JsonPrimitive(doubleValue);
               }
            }
            else
            {
               ret = new JsonPrimitive("");
            }
         }
         else if (value instanceof Float)
         {
            Float floatValue = ((Number)value).floatValue();
            if (!Float.isInfinite(floatValue) && !Float.isNaN(floatValue))
            {
               if (floatValue.toString().contains("E") || floatValue.toString().contains("e"))
               {
                  BigDecimal decimalValue = new BigDecimal(floatValue);
                  ret = new JsonPrimitive(decimalValue.toPlainString());
               }
               else
               {
                  ret = new JsonPrimitive(floatValue);
               }
            }
            else
            {
               ret = new JsonPrimitive("");
            }
         }
         else if (value instanceof Number)
         {
            ret = new JsonPrimitive((Number)value);
         }
         else if (value instanceof Boolean)
         {
            ret = new JsonPrimitive((Boolean)value);               
         }
         else if (value instanceof Character)
         {
            ret = new JsonPrimitive((Character)value);               
         }
         else if (value instanceof Date)
         {
            ret = new JsonPrimitive(DateUtils.format((Date)value, dateFormat, Locale.getDefault(), TimeZone.getDefault()));
         }
         else if (value instanceof Calendar)
         {
            ret = new JsonPrimitive(DateUtils.format(((Calendar)value).getTime(), dateFormat, Locale.getDefault(), TimeZone.getDefault()));
         }
         else if (value instanceof String)
         {
            ret = new JsonPrimitive((String)value);
         }
         else
         {
            ret = new JsonPrimitive(value.toString());
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
         trace.error("Something went wrong", e);
      }
      
      return ret;
   }

   /**
    * @param elem
    * @return
    */
   public Object toObject(JsonElement elem)
   {
      if(elem.isJsonArray())
      {
         return toObject((JsonArray)elem);
      }
      else if(elem.isJsonObject())
      {
         return toObject((JsonObject)elem);
      }
      else if(elem.isJsonPrimitive())
      {
         return toObject((JsonPrimitive)elem);
      }
      else
      {
         return null;
      }
   }

   /**
    * @param elem
    * @return
    */
   public Map<String, Object> toObject(JsonObject elem)
   {
      Map<String, Object> data = new HashMap<String, Object>();
      for (Entry<String, JsonElement> entry : elem.entrySet())
      {
         data.put(entry.getKey(), toObject(entry.getValue()));
      }
      
      return data;
   }

   /**
    * @param elem
    * @return
    */
   public List<Object> toObject(JsonArray elem)
   {
      List<Object> data = new ArrayList<Object>();
      for (int i = 0; i < elem.size(); i++)
      {
         data.add(toObject(elem.get(i)));
      }
      
      return data;
   }

   /**
    * @param elem
    * @return
    */
   public Object toObject(JsonPrimitive elem)
   {
      if (elem.isNumber())
      {
         return elem.getAsNumber();
      }
      else if (elem.isString())
      {
         return elem.getAsString();
      }
      else if (elem.isBoolean())
      {
         return elem.getAsBoolean();
      }
      else
      {
         return null;
      }
   }
}
