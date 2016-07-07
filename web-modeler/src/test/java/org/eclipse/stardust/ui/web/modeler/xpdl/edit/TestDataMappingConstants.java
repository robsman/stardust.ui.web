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

import org.eclipse.stardust.model.xpdl.carnot.*;

/**
 * Tests creation, modify and delete of data mapping constants.
 * 
 * @author Barry.Grotjahn
 * @version $Revision: $
 */
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
      assertThat(size, is(2));
      DataMappingType dataMapping1 = activity.getDataMapping().get(0);
      String dataPath1 = dataMapping1.getDataPath();
      assertThat(dataPath1, is("(String) hnas dampf"));
      
      DataMappingType dataMapping2 = activity.getDataMapping().get(1);
      String dataPath2 = dataMapping2.getDataPath();
      assertThat(dataPath2, is("(String) hnas dampf2"));

      requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/changeDataMappingConstants2.txt");
      requestStream = new InputStreamReader(requestInput);
      replay(requestStream, "changeDataMappingConstants2", true);
      
      size = activity.getDataMapping().size();
      assertThat(size, is(1));
      dataMapping2 = activity.getDataMapping().get(0);
      dataPath2 = dataMapping2.getDataPath();
      assertThat(dataPath2, is("(Integer) 66"));
      
      //saveReplayModel("C:/tmp/");      
   }
   
   protected boolean includeConsumerModel()
   {
      return false;
   }
}