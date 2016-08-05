/*******************************************************************************
* Copyright (c) 2016 SunGard CSA LLC and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Barry.Grotjahn (SunGard CSA LLC) - initial API and implementation and/or initial documentation
*******************************************************************************/

package org.eclipse.stardust.ui.web.modeler.xpdl.edit;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class TestModelLoading extends RecordingTestcase
{
   /**
    * Test, if a corrupt Model (not UTF-8) will be loaded into failed models array with error message.
    */
   @Test
   public void testModelLoading()
   {
      String allModels = modelService.getAllModels(true);      
      JsonParser parser = new JsonParser();
      JsonObject json = (JsonObject) parser.parse(allModels);      
      JsonArray failedArray = json.get("failed").getAsJsonArray();
      JsonObject failed = (JsonObject) failedArray.get(0);

      String errorString = failed.get("error").toString();
      
      assertThat(errorString, is("\"Failed loading XPDL model.\""));      
   }

   protected boolean includeFailedModel()
   {
      return true;
   }
   
   protected boolean includeConsumerModel()
   {
      return false;
   }
}