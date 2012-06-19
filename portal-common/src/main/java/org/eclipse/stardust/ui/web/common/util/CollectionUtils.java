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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author Subodh.Godbole
 *
 */
public class CollectionUtils
{
   /**
    * Returns true if set1 contains at least one element from set2
    * @param set1
    * @param set2
    * @return
    */
   public static boolean containsAny(Set<?> set1, Set<?> set2)
   {
      for (Object role : set1)
      {
         if(set2.contains(role))
         {
            return true;
         }
      }
      return false;
   }
   
   /**
    * @param <T>
    * @return
    */
   public static <T> List<T> newList()
   {
      return newArrayList();
   }
   
   /**
    * @param <K>
    * @param <V>
    * @return
    */
   public static <K, V> Map<K, V> newHashMap()
   {
      return new HashMap<K, V>();
   }
   
   /**
    * @param <T>
    * @return
    */
   public static <T> HashSet<T> newHashSet()
   {
      return new HashSet<T>();
   }
   
   /**
    * @param <T>
    * @return
    */
   public static <T> List<T> newArrayList()
   {
      return new ArrayList<T>();
   }
   
   /**
    * @param <K>
    * @param <V>
    * @return
    */
   public static <K, V> TreeMap<K, V> newTreeMap()
   {
      return new TreeMap<K, V>();
   }
   
   /**
    * @param array
    * @return
    */
   public static boolean isEmpty(String[] array)
   {
      return (null != array && array.length > 0) ? false : true; 
   }

   /**
    * @param <T>
    * @param c
    * @return
    */
   public static <T> boolean isEmpty(Collection<T> c)
   {
      return (null != c && c.size() > 0) ? false : true; 
   }

   /**
    * @param <K>
    * @param <V>
    * @param value
    * @return
    */
   public static <K, V> boolean isEmpty(Map<K, V> value)
   {
      return (null != value && value.size() > 0) ? false : true; 
   }
}
