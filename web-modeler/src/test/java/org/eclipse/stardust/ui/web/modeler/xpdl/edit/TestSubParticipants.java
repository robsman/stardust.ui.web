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

import java.io.InputStream;
import java.io.InputStreamReader;

import org.junit.Test;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.eclipse.stardust.ui.web.modeler.utils.test.GenericModelingAssertions;

public class TestSubParticipants extends RecordingTestcase
{
   @Test
   public void testCreateSubParticipants() throws Throwable
   {
      providerModel = modelService.findModel(PROVIDER_MODEL_ID);

      InputStream requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/createSubParticipants.txt");
      InputStreamReader requestStream = new InputStreamReader(requestInput);

      replay(requestStream, "testCreateSubParticipants", true);
   }

   public void testCreateSubParticipantsCallback(TestResponse response)
         throws AssertionError
   {            
      if (response.getResponseNumber() == 4)
      {
         assertThat(response.getModified().size(), is(1));                  
         JsonObject subParticipantJson = response.getModified().get(0).getAsJsonObject();

         GenericModelingAssertions.assertJsonHasKeyValue(subParticipantJson,
               "cardinality=2");
      }
      if (response.getResponseNumber() == 6)
      {
         assertThat(response.getModified().size(), is(1));                  
         JsonObject subParticipantJson = response.getModified().get(0).getAsJsonObject();
         JsonElement element = subParticipantJson.get("attributes");         
         JsonObject attributes = element.getAsJsonObject();

         GenericModelingAssertions.assertJsonHasKeyValue(attributes,
               "carnot:engine:bound=true");
      }
      if (response.getResponseNumber() == 7)
      {
         assertThat(response.getModified().size(), is(1));                  
         JsonObject subParticipantJson = response.getModified().get(0).getAsJsonObject();
         JsonElement element = subParticipantJson.get("attributes");         
         JsonObject attributes = element.getAsJsonObject();

         GenericModelingAssertions.assertJsonHasKeyValue(attributes,
               "carnot:engine:bound=true", "carnot:engine:dataId=CURRENT_DATE");
      }
      if (response.getResponseNumber() == 8)
      {
         assertThat(response.getModified().size(), is(1));                  
         JsonObject subParticipantJson = response.getModified().get(0).getAsJsonObject();
         JsonElement element = subParticipantJson.get("attributes");         
         JsonObject attributes = element.getAsJsonObject();

         GenericModelingAssertions.assertJsonHasKeyValue(attributes,
               "carnot:engine:bound=true", "carnot:engine:dataId=CURRENT_DATE", "carnot:engine:dataPath=/test");
      }
      if (response.getResponseNumber() == 10)
      {
         assertThat(response.getModified().size(), is(1));                  
         JsonObject subParticipantJson = response.getModified().get(0).getAsJsonObject();
         JsonElement element = subParticipantJson.get("attributes");         
         JsonObject attributes = element.getAsJsonObject();

         GenericModelingAssertions.assertJsonHasKeyValue(attributes,
               "carnot:engine:bound=true");                  
      }
      if (response.getResponseNumber() == 11)
      {
         assertThat(response.getModified().size(), is(1));                  
         JsonObject subParticipantJson = response.getModified().get(0).getAsJsonObject();
         JsonElement element = subParticipantJson.get("attributes");         
         JsonObject attributes = element.getAsJsonObject();

         GenericModelingAssertions.assertJsonHasKeyValue(attributes,
               "carnot:engine:bound=true", "carnot:engine:dataId=PROCESS_PRIORITY");                  
      }
      if (response.getResponseNumber() == 12)
      {
         assertThat(response.getModified().size(), is(1));                  
         JsonObject subParticipantJson = response.getModified().get(0).getAsJsonObject();
         JsonElement element = subParticipantJson.get("attributes");         
         JsonObject attributes = element.getAsJsonObject();

         GenericModelingAssertions.assertJsonHasKeyValue(attributes,
               "carnot:engine:bound=true", "carnot:engine:dataId=PROCESS_PRIORITY", "carnot:engine:dataPath=/b");         
      }
   }
   
   protected boolean includeConsumerModel()
   {
      return false;
   }

   protected boolean performResponseCallback()
   {
      return true;
   }   
}