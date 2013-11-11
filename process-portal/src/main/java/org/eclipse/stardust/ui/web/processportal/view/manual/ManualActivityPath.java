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
package org.eclipse.stardust.ui.web.processportal.view.manual;

import java.util.List;
import java.util.Map;

import org.eclipse.stardust.ui.common.introspection.Path;

/**
 * This is 'Silent' Parent Path. This does not take part in rendering and in FullXPath
 * It only collects all direct data mappings to Activity under one Parent - Path
 * 
 * @author Subodh.Godbole
 *
 */
public class ManualActivityPath extends Path
{
   /**
    * @param parentPath
    * @param id
    * @param readonly
    */
   public ManualActivityPath(String id, boolean readonly)
   {
      super(null, id, readonly);
   }

   /**
    * @see org.eclipse.stardust.ui.common.introspection.Path#getFullXPath()
    */
   public String getFullXPath()
   {
      return ""; // Return blank, because this does not take part in FullXPath
   }

   @Override
   public int getDepth()
   {
      return 0; // Return 0, because this is silent parent
   }

   @Override
   public boolean isPrimitive()
   {
      return false;
   }

   @Override
   public boolean isEnumeration()
   {
      return false;
   }

   @Override
   public boolean isList()
   {
      return false;
   }

   @Override
   public boolean isNumber()
   {
      return false;
   }

   @Override
   public Class< ? > getJavaClass()
   {
      return Class.class;
   }

   @Override
   public String getTypeName()
   {
      return "ManualActivity";
   }

   @Override
   public List<String> getEnumerationValues()
   {
      return null;
   }

   @Override
   public Object mapToObject(Map<String, Object> object)
   {
      return null;
   }

   @Override
   public Map<String, Object> objectToMap(Object object)
   {
      return null;
   }
   
   @Override
   public String toJsonString()
   {
      return childPathsToJson();
   }
}