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
package org.eclipse.stardust.ui.web.common.messages;

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

import javax.faces.context.FacesContext;

import org.eclipse.stardust.ui.web.common.util.StringUtils;

/**
 * @author Yogesh.Manware
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
      initBundle(getInitLocale());
   }
   
   private Locale getInitLocale()
   {
      if (FacesContext.getCurrentInstance() != null && FacesContext.getCurrentInstance().getExternalContext() != null)
      {
         return FacesContext.getCurrentInstance().getExternalContext().getRequestLocale();
      }
      return null;
   }

   /**
    * 
    */
   public AbstractMessageBean(String bundleName, Locale locale)
   {
      this.bundleName = bundleName;
      initBundle(locale);
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
      String country = getLocaleObject().getCountry();
      if(StringUtils.isEmpty(country))
         country = "US";
      
      return "Locale." + country;
   }

   /**
    * @param locale
    */
   protected void initBundle(Locale locale)
   {
      if (locale != null)
      {
         bundle = ResourceBundle.getBundle(bundleName, locale);
      }
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
      initBundle(new Locale("en"));
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
