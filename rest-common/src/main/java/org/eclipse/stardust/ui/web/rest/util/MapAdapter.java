/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *	SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.rest.util;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * @author Yogesh.Manware
 * @version $Revision: $
 */
public class MapAdapter implements JsonSerializer<Map<String, Serializable>>
{

   @Override
   public JsonElement serialize(Map<String, Serializable> anyMap, Type typeOfSrc, JsonSerializationContext context)
   {
      JsonHelper helper = new JsonHelper();
      JsonObject jo = new JsonObject();
      helper.toJson(anyMap, jo);
      return jo;
   }
}
