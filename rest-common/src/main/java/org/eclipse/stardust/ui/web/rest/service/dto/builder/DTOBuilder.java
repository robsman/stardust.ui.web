/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Anoop.Nair (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.rest.service.dto.builder;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.util.StringUtils;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.util.GsonUtils;
import org.eclipse.stardust.ui.web.common.util.ReflectionUtils;
import org.eclipse.stardust.ui.web.rest.JsonMarshaller;
import org.eclipse.stardust.ui.web.rest.service.dto.common.DTOAttribute;
import org.eclipse.stardust.ui.web.rest.service.dto.common.DTOClass;


/**
 * @author Subodh.Godbole
 * @author Johnson.Quadras
 *
 */
public class DTOBuilder
{
   private static final Logger trace = LogManager.getLogger(DTOBuilder.class);

   /**
    * 
    */
   private DTOBuilder()
   {      
   }

   /**
    * @param <DTO>
    * @param <T>
    * @param fromInstance
    * @param toClass
    * @return
    */
   public static <DTO, T> DTO build(T fromInstance, Class<DTO> toClass)
   {
      DTO toInstance = null;
      try
      {
         toInstance = toClass.newInstance();

         Class<?> iteratorClass = toClass;
         while (iteratorClass != Object.class)
         {
            for(Field field : iteratorClass.getDeclaredFields())
            {
               if (field.isAnnotationPresent(DTOAttribute.class))
               {
                  DTOAttribute annotation = field.getAnnotation(DTOAttribute.class);
                  String fieldName = annotation.value();
                  if (StringUtils.isEmpty(fieldName))
                  {
                     fieldName = field.getName();
                  }
   
                  try
                  {
                     Object value = getFieldValue(fromInstance, fieldName);
                     
                     Class<?> fieldClass = field.getType();
                     if (null != value && fieldClass.isAnnotationPresent(DTOClass.class))
                     {
                        value = build(value, fieldClass);
                     }

                     setFieldValue(toInstance, field, value);
                  }
                  catch (Exception e)
                  {
                     trace.error("Error in retriving field: " + fieldName, e);
                  }
               }
            }
            
            iteratorClass = iteratorClass.getSuperclass();
         }
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
      
      return toInstance;
   }

   /**
    * @param <DTO>
    * @param <T>
    * @param fromInstance
    * @param toClass
    * @return
    */
   public static <DTO, T> List<DTO> buildList(Collection<T> fromInstances, Class<DTO> toClass)
   {
      List<DTO> list = new ArrayList<DTO>();

      for (T fromInstance : fromInstances)
      {
         list.add(build(fromInstance, toClass));
      }

      return list;
   }
   
  /**
   * Builds a DTO from a JSON String
   * @param jsonString
   * @param toClass
   * @param customTokens
   * @return
   * @throws Exception
   */
   public static <DTO, T> DTO buildFromJSON(String jsonString, Class<DTO> toClass,
         Map<String, Type> customTokens) throws Exception
   {
      JsonMarshaller jsonIo = new JsonMarshaller();
      JsonObject json = jsonIo.readJsonObject(jsonString);
      return buildFromJSON(json, toClass, customTokens);
   }
   /**
    * Builds a DTO from a JSON
    * @param json
    * @param toClass
    * @param customTokens
    * @return
    * @throws Exception
    */
   
   public static <DTO, T> DTO buildFromJSON(JsonObject json, Class<DTO> toClass,
         Map<String, Type> customTokens) throws Exception
   {
      DTO toInstance = null;
      toInstance = toClass.newInstance();

      for (Field field : toClass.getDeclaredFields())
      {
         if (null != json.get(field.getName()))
         {
            if (String.class.equals(field.getType()))
            {
               setFieldValue(toInstance, field, json.get(field.getName()).getAsString());
            }
            else if (int.class.equals(field.getType())
                  || Integer.class.equals(field.getType()))
            {
               setFieldValue(toInstance, field, json.get(field.getName()).getAsInt());
            }
            else if (Long.class.equals(field.getType())
                  || long.class.equals(field.getType()))
            {
               setFieldValue(toInstance, field, json.get(field.getName()).getAsLong());
            }
            else if (Float.class.equals(field.getType()))
            {
               setFieldValue(toInstance, field, json.get(field.getName()).getAsFloat());
            }
            else if (Boolean.class.equals(field.getType())
                  || boolean.class.equals(field.getType()))
            {
               setFieldValue(toInstance, field, json.get(field.getName()).getAsBoolean());
            }
            else if (List.class.equals(field.getType()))
            {
               Type listType = new TypeToken<List<String>>()
               {
               }.getType();
               if (customTokens.containsKey(field.getName()))
               {
                  listType = customTokens.get(field.getName());
               }
               setFieldValue(toInstance, field, GsonUtils.extractList(
                     json.get(field.getName()).getAsJsonArray(), listType));
            }
            else
            {
               Class< ? > fieldClass = field.getType();
               setFieldValue(
                     toInstance,
                     field,
                     buildFromJSON(json.get(field.getName()).toString(), fieldClass,
                           customTokens));
            }
         }
      }
      return toInstance;
   }

   /**
    * @param instance
    * @param fieldName
    * @return
    * @throws IllegalAccessException
    */
   private static Object getFieldValue(Object instance, String fieldName) throws Exception
   {
      if (fieldName.indexOf(".") > -1)
      {
         String[] parts = StringUtils.split(fieldName, ".");
         Object obj = instance;
         for (String part : parts)
         {
            obj = getPlainField(obj, part);
            if (null == obj)
            {
               break;
            }
         }
         return obj;
      }
      else
      {
         return getPlainField(instance, fieldName);
      }
   }
   
   /**
    * @param instance
    * @param fieldName
    * @return
    * @throws IllegalAccessException
    */
   private static Object getPlainField(Object instance, String fieldName) throws Exception
   {
      Object value = null;

      try
      {
         if (fieldName.endsWith("()"))
         {
            fieldName = fieldName.substring(0, fieldName.length() - 2);
            value = ReflectionUtils.invokeMethod(instance, fieldName);
         }
         else
         {
            value = ReflectionUtils.invokeGetterMethod(instance, fieldName);
         }
      }
      catch (Exception e)
      {
         trace.warn("Error in invoking getter method for, class: " + instance.getClass().getName() + ", field: "
               + fieldName);

         if (null != instance)
         {
            Class< ? > clazz = instance.getClass();
            for (Field field : clazz.getDeclaredFields())
            {
               if (field.getName().equals(fieldName))
               {
                  field.setAccessible(true);
                  return field.get(instance);
               }
            }
         }
      }

      return value;
   }

   /*
    * TODO: Performance: Check availability of setter method first using simple if than relying on exception
    */
   private static void setFieldValue(Object instance, Field field, Object value) throws Exception 
   {
      try
      {
         // First try with setter method
         String methodName = "set" + ReflectionUtils.toSentenseCase(field.getName());

         Object[] params = new Object[1];
         params[0] = value;
   
         ReflectionUtils.invokeMethod(instance, methodName, params);
      }
      catch(Exception e)
      {
         // Now try with direct field
         if (!field.isAccessible())
         {
            field.setAccessible(true);
         }
         field.set(instance, value);
      }
   }
}
