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

import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.util.ReflectionUtils;
import org.eclipse.stardust.ui.web.rest.service.dto.common.DTOAttribute;
import org.eclipse.stardust.ui.web.rest.service.dto.common.DTOClass;
import org.springframework.util.StringUtils;


/**
 * @author Subodh.Godbole
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

         for(Field field : toClass.getDeclaredFields())
         {
            if (field.isAnnotationPresent(DTOAttribute.class))
            {
               try
               {
                  DTOAttribute annotation = field.getAnnotation(DTOAttribute.class);
                  String fieldName = annotation.value();
                  Object value = getFieldValue(fromInstance, fieldName);
                  
                  Class<?> fieldClass = field.getType();
                  if (null != value && fieldClass.isAnnotationPresent(DTOClass.class))
                  {
                     value = build(value, fieldClass);
                  }
   
                  field.setAccessible(true);
                  field.set(toInstance, value);
               }
               catch (Exception e)
               {
                  e.printStackTrace();
               }
            }
         }
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
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
         value = ReflectionUtils.invokeGetterMethod(instance, fieldName);
      }
      catch (Exception e)
      {
         trace.warn("Error in invoking getter method for, class: " + instance.getClass().getName() + ", field: "
               + fieldName);
      }

      if (null == value)
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

      return value;
   }
}
