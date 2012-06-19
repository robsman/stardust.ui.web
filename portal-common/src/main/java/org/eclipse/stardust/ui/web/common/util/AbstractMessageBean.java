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
package org.eclipse.stardust.ui.web.common.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import javax.faces.context.FacesContext;

/**
 * @author Subodh.Godbole
 *
 */
public abstract class AbstractMessageBean implements Map<String, String>, Serializable
{
   private ResourceBundle bundle;

   /**
    * 
    */
   public AbstractMessageBean(String bundleName)
   {
      bundle = ResourceBundle.getBundle(bundleName, FacesContext.getCurrentInstance()
            .getExternalContext().getRequestLocale());
   }

   /**
    * @param value
    * @param params
    * @return
    */
   public String getParamString(String key, String... params)
   {
      List<String> paramList = new ArrayList<String>(Arrays.asList(params));

      String value = getString(key);
      if (StringUtils.isNotEmpty(value) && value.indexOf("{") >= 0)
      {
         int numberOfOccurances = (value.split("\\{")).length;
         while (params.length < numberOfOccurances - 1)
         {
            paramList.add("");
            params = (String[]) paramList.toArray(new String[paramList.size()]);
         }

         int i = 0;
         while (value.indexOf("{") >= 0)
         {
            if (value.indexOf("{") + 2 == value.indexOf("}"))
            {
               value = value.replace(value.substring(value.indexOf("{"), value.indexOf("}") + 1), params[i++]);
            }
            else
            // for ExtractPage Version comment jcr string contains '{,}' as data
            {
               int currentSIndex = value.indexOf("{") + 1;
               int currentEIndex = value.indexOf("}") + 1;
               if (value.indexOf("{", currentSIndex) > 0)
               {
                  value = value.replace(
                        value.substring(value.indexOf("{", currentSIndex), value.indexOf("}", currentEIndex) + 1),
                        params[i++]);
               }
               break;
            }
         }
      }

      return value;
   }
   
   /**
    * @param key
    * @return
    */
   public String getString(String key)
   {
      return get(key);
   }
   
   public boolean hasKey(String key)
   {
      try
      {
         bundle.getString(key);
         return true;
      }
      catch (MissingResourceException mre)
      {
         return false;
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.util.Map#get(java.lang.Object)
    */
   public String get(Object key)
   {
      if (!(key instanceof String))
      {
         throw new IllegalArgumentException("Key " + key + " must be of type String.");
      }

      try
      {
         return bundle.getString((String) key);
      }
      catch (Exception x)
      {
         return "%" + key + "%";
      }
   }
   
   /**
    * @return
    */
   public String getLocale()
   {
      String country = bundle.getLocale().getCountry();
      if(StringUtils.isEmpty(country))
         country = "US";
      
      return "Locale." + country;
   }

   /**
    * @return
    */
   public Locale getLocaleObject()
   {
      return bundle.getLocale();
   }

   public void clear()
   {}

   public boolean containsKey(Object key)
   {
      return false;
   }

   public boolean containsValue(Object value)
   {
      return false;
   }

   public Set entrySet()
   {
      return null;
   }

   public boolean isEmpty()
   {
      return false;
   }

   public Set keySet()
   {
      return null;
   }

   public String put(String key, String value)
   {
      return null;
   }

   public void putAll(Map t)
   {}

   public String remove(Object key)
   {
      return null;
   }

   public int size()
   {
      return 0;
   }

   public Collection values()
   {
      return null;
   }

}
