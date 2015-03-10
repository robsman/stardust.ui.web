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
package org.eclipse.stardust.ui.web.rest.service.dto;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * @author Subodh.Godbole
 * @version $Revision: $
 */
public class PathDTO /*extends AbstractDTO*/
{
   public String id;
   public String name;
   public String fullXPath;
   public Boolean readonly;
   public String typeName;
   public Boolean isPrimitive;
   public Boolean isList;
   public Boolean isEnum;
   public String[] enumValues;
   public Map<String, String> properties;

   /**
    * @param json
    * @return
    */
   public static List<PathDTO> toList(String json)
   {
      Gson gson = new Gson();
      Type listType = new TypeToken<List<PathDTO>>(){}.getType();
      return gson.fromJson(json, listType);
   }
}
