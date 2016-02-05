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

import org.junit.Test;

import org.eclipse.stardust.model.xpdl.carnot.*;
import org.eclipse.stardust.ui.web.modeler.utils.test.GenericModelingAssertions;

public class TestMandatoryDataMappings extends RecordingTestcase
{
   @Test
   public void testMandatoryDataMappingAttribute() throws Throwable
   {
      providerModel = modelService.findModel("MandatoryDataMappingsModel");

      String command = "{\"commandId\":\"modelElement.update\",\"modelId\":\"MandatoryDataMappingsModel\",\"changeDescriptions\":[{\"oid\":\"62\",\"changes\":{\"mandatoryDataMapping\" : \"true\"}}]}";
      replaySimple(command, "mandatoryDataMapping", null, true);    
      
      ProcessDefinitionType processDefinitionType = providerModel.getProcessDefinition().get(0);
      ActivityType activityType = processDefinitionType.getActivity().get(0);
      DataMappingType dataMappingType = activityType.getDataMapping().get(0);

      GenericModelingAssertions.assertAttribute(dataMappingType, "mandatoryDataMapping", "true");      
   }
  
   @Override
   protected String getProviderModelID()
   {
      return "MandatoryDataMappingsModel";
   }

   protected boolean includeConsumerModel()
   {
      return false;
   }
}