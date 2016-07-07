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
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.eclipse.stardust.model.xpdl.carnot.*;
import org.eclipse.stardust.ui.web.modeler.utils.test.GenericModelingAssertions;

public class TestDataMappingConstants extends RecordingTestcase
{

   @Test
   public void testChangeDataMappingsGeneral() throws Exception
   {
      providerModel = modelService.findModel(PROVIDER_MODEL_ID);

      InputStream requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/changeDataMappingConstants.txt");
      InputStreamReader requestStream = new InputStreamReader(requestInput);
      replay(requestStream, "changeDataMappingConstants", true);

      ProcessDefinitionType process = providerModel.getProcessDefinition().get(0);
      ActivityType activity = process.getActivity().get(0);
      
      int size = activity.getDataMapping().size();
      System.err.println("* " + size);
      DataMappingType dataMapping1 = activity.getDataMapping().get(0);
      String dataPath = dataMapping1.getDataPath();
      System.err.println("* " + dataPath);

      requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/changeDataMappingConstants2.txt");
      requestStream = new InputStreamReader(requestInput);
      replay(requestStream, "changeDataMappingConstants2", true);
      
      size = activity.getDataMapping().size();
      System.err.println("* " + size);
      DataMappingType dataMapping2 = activity.getDataMapping().get(0);
      String dataPath2 = dataMapping2.getDataPath();
      System.err.println("* " + dataPath2);
      
      
      
      //saveReplayModel("C:/development/");

      /*
      ProcessDefinitionType process = GenericModelingAssertions.assertProcess(providerModel, "DatamappingProcess","Datamapping Process");
      ActivityType activity = process.getActivity().get(0);
      */

      /*
      List<DataMappingType> dataMappings = activity.getDataMapping();
      assertThat(dataMappings, is(not(nullValue())));
      assertThat(dataMappings.size(), is(6));
      */

      saveReplayModel("C:/tmp/");
      
   }
   
   protected boolean includeConsumerModel()
   {
      return false;
   }
}