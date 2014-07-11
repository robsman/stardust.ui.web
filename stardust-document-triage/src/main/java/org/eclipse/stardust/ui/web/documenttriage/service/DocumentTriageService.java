/*******************************************************************************
 * Copyright (c) 2013 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.stardust.ui.web.documenttriage.service;

import com.google.gson.JsonObject;

import org.eclipse.stardust.ui.web.documenttriage.rest.JsonMarshaller;

public class DocumentTriageService {
	public DocumentTriageService() {
		super();

		new JsonMarshaller();
	}

	/**
	 * 
	 * @return
	 */
	public JsonObject login(JsonObject credentialsJson) {
      JsonObject userJson = new JsonObject();
      userJson.addProperty("id", "John");
      userJson.addProperty("firstName", "John");
      userJson.addProperty("lastName", "Smith");
      userJson.addProperty("name", "John Smith");
      userJson.addProperty("eMail", "john.smith@sungard.com");
      userJson.addProperty("description", "");
	   
		return userJson;
	}
	
   /**
    * 
    * @return
    */
   public JsonObject logout() {      
      // TODO
      return new JsonObject();
   }
}
