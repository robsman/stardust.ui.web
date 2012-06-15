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
package org.eclipse.stardust.ui.web.viewscommon.common.structureddata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.util.ReflectionUtils;

/**
 * Creates JSF table models for list elements of structured data 
 */
public class ModelCreator extends HashMap
{
   private static final Logger trace = LogManager.getLogger(ModelCreator.class);

   public static final String STRUCT_DATA_TABLE_MODEL = "ag.carnot.web.jsf.common.structureddata.StructuredDataTableModel";

   private Object complexType;

   private Map tableModelCache;

   /**
    * @param complexType
    */
   public ModelCreator(Object complexType)
   {
      this.complexType = complexType;
      this.tableModelCache = new HashMap();
   }

   public Object get(Object o)
   {
      if (this.tableModelCache.containsKey(o))
      {
         // return cached table model
         return this.tableModelCache.get(o);
      }
      else
      {
         // create new table model
         String key = (String) o;
         // XPath for the table model data inside the structured data is in "key"
         List list = findList(key);
         
         if (ReflectionUtils.isClassInClassPath(STRUCT_DATA_TABLE_MODEL))
         {
            //StructuredDataTableModel model = new StructuredDataTableModel(list, key);
            Object model = ReflectionUtils.createInstance(STRUCT_DATA_TABLE_MODEL, new Class[] {
                  Object.class, String.class}, new Object[] {(Object)list, key});
            this.tableModelCache.put(key, model);
            return model;
         }
         else
         {
            trace.error("Unable to find required class '" + STRUCT_DATA_TABLE_MODEL + "' to fultil the request.");
            return null;
         }
      }
   }

   /**
    * @param key
    * @return
    */
   private List findList(String key)
   {
      StringTokenizer st = new StringTokenizer(key, ".");
      
      Object currentElement = this.complexType;
      Map parent = null;
      String xPathPart = null;
      while (st.hasMoreTokens())
      {
         xPathPart = st.nextToken();

         if (st.hasMoreElements() && !((Map) currentElement).containsKey(xPathPart))
         {
            // create a new complex type if it does not exists
            ((Map) currentElement).put(xPathPart, new HashMap());
         }

         parent = (Map) currentElement;

         currentElement = ((Map) currentElement).get(xPathPart);
      }
      // create the last level as ArrayList (if needed)
      if (currentElement == null && parent != null && xPathPart != null)
      {
         currentElement = new ArrayList();
         parent.put(xPathPart, currentElement);
      }
      
      return (List) currentElement;
   }
}