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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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

import org.eclipse.stardust.ui.web.html5.ManagedBeanUtils;

/**
 * @author Subodh.Godbole
 *
 */
public abstract class AbstractMessageBean implements Map<String, String>, Serializable
{

   private static final long serialVersionUID = 6752664049471262496L;
   private String bundleName;
   private transient ResourceBundle bundle;

   /**
    * 
    */
   public AbstractMessageBean(String bundleName)
   {
      this.bundleName = bundleName;
      initBundle();
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
         
         // Maintained 2 seperate counters, as index used in 'value' may not be in
         // sequence
         // i.e {0},{1},{2} or may be {0}, {2},{3}
         int iterateIndex = 0;
         // Index to maintain check if all params are substituted
         int paramIndex = 0;
         // For jcrId used for Extract Page msg, multiple'{ and }' are part of value
         // added check for param length
         while (value.indexOf("{") >= 0 && paramIndex < params.length)
         {
            int startInd = value.indexOf("{" + iterateIndex + "}");
            int lenght = new String("{" + iterateIndex + "}").length();
            // If 'value' has iterateIndex value say '{1}' replace with param runtime value  
            if(startInd != -1)
            {
               value = value.replace(value.substring(startInd, startInd + lenght), params[paramIndex]);
               paramIndex++;
            }
            // increments if index available or not in value String
            iterateIndex++;
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
      String country = getLocaleObject().getCountry();
      if(StringUtils.isEmpty(country))
         country = "US";
      
      return "Locale." + country;
   }

   /**
    * 
    */
   private void initBundle()
   {
      bundle = ResourceBundle.getBundle(bundleName, ManagedBeanUtils.getLocale());
   }

   /**
    * @param out
    * @throws IOException
    */
   private void writeObject(ObjectOutputStream out) throws IOException
   {
      out.defaultWriteObject();
   }

   /**
    * @param in
    * @throws IOException
    * @throws ClassNotFoundException
    */
   private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
   {
      in.defaultReadObject();
      initBundle();
   }
   
   /**
    * @return
    */
   public Locale getLocaleObject()
   {
      Locale locale = bundle.getLocale();
      if (StringUtils.isEmpty(locale.toString()))
      {
         // When server JVM and client locale are not-translated, return default locale
         return new Locale("en");
      }
      return locale;
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
