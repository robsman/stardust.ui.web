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
import org.eclipse.stardust.engine.core.runtime.beans.DocumentTypeUtils;
import org.eclipse.stardust.ui.web.common.util.DateUtils;
import org.eclipse.stardust.ui.web.processportal.interaction.Interaction;
import org.eclipse.stardust.ui.web.processportal.interaction.IppDocumentController;
import org.eclipse.stardust.ui.web.processportal.service.rest.InteractionDataUtils.DOCUMENT;
import org.eclipse.stardust.ui.web.viewscommon.utils.MIMEType;
import org.eclipse.stardust.ui.web.viewscommon.utils.TypedDocumentsUtil;
/**
 * @author Subodh.Godbole
 * @author Yogesh.Manware
 *
 */
public class JsonHelper
{
   private static final Logger trace = LogManager.getLogger(InteractionDataUtils.class);

   private String dateFormat = "yyyy-MM-dd";
  
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
    * @param dataId
    * @param elemDM
    * @param model
    * @param servletContext
    */
   public void toJsonDocument(Object document, DataMapping dm, JsonObject elemDM,
         Model model, Interaction interaction)
   {
      trace.debug("create document json...");
      
      String typeDeclarationId = DocumentTypeUtils.getMetaDataTypeDeclarationId(model.getData(dm.getDataId()));
      
      elemDM.add(DOCUMENT.TYPE_ID, new JsonPrimitive(typeDeclarationId));
      elemDM.add(
            DOCUMENT.TYPE_NAME,
            new JsonPrimitive(
                  TypedDocumentsUtil.getDocumentTypeLabel(DocumentTypeUtils.getDocumentType(
                        typeDeclarationId, model), (DeployedModel) model)));

      org.eclipse.stardust.engine.api.runtime.Document doc = (org.eclipse.stardust.engine.api.runtime.Document) document;

      if (doc != null)
      {
         //prepare response
         elemDM.add(DOCUMENT.TYPEJ, new JsonPrimitive(DOCUMENT.TYPE.JCR.toString()));
         
         IppDocumentController dc = interaction.getDocumentControllers().get(dm.getId());
         
         MIMEType mType = dc.getDocumentViewerInputParameters().getDocumentContentInfo().getMimeType();

         elemDM.add(DOCUMENT.ID, new JsonPrimitive(doc.getId()));
         elemDM.add(DOCUMENT.NAME, new JsonPrimitive(doc.getName()));
         elemDM.add(DOCUMENT.ICON, new JsonPrimitive(DOCUMENT.DOC_PATH + "mime-types/"
               + mType.getIconPath()));
         
         elemDM.add(DOCUMENT.VIEW_KEY, new JsonPrimitive(dc.getViewKey()));

      }
      else
      {
         elemDM.add(DOCUMENT.TYPEJ, new JsonPrimitive(DOCUMENT.TYPE.NONE.toString()));
         elemDM.add(DOCUMENT.ICON, new JsonPrimitive(DOCUMENT.DOC_PATH
               + "page_white_error.png"));
      }
   }
   
  /**
   *  
   * @param key
   * @param value
   * @param parent
   */
   public void toJson(String key, Object value, JsonObject parent){
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
      else // Primitive
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
         if (value instanceof Float || value instanceof Double || value instanceof Number)
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
