/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.rest.dto;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

/**
 * @author Subodh.Godbole
 * @version $Revision: $
 */
public abstract class AbstractDTO
{
   /**
    * @return
    */
   public String toJson()
   {
      Gson gson = new Gson();
      return gson.toJson(this);
   }

   /**
    * @return
    */
   public static String toJson(List<?/* extends AbstractDTO */> list)
   {
      Gson gson = new Gson();
      return gson.toJson(list);
   }

   /**
    * @return
    */
   public static String toJson(Map map)
   {
      Gson gson = new Gson();
      return gson.toJson(map);
   }

   /**
    * @param json
    * @return
    */
   public static <T> T toObject(String json, Class<T> clazz)
   {
      Gson gson = new Gson();
      return gson.fromJson(json, clazz);
   }

   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder();
      try
      {
         for (Field field : this.getClass().getDeclaredFields())
         {
            if (field.isAccessible())
            {
               if (sb.toString().length() > 0)
               {
                  sb.append(",");
               }
               sb.append(field.getName()).append(":").append(field.get(this));
            }
         }
      }
      catch (Exception e)
      {
         // Ignore!
      }
      
      return sb.toString();
   }
}
