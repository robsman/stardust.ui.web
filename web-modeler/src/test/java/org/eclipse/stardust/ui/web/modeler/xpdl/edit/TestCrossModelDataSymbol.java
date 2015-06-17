package org.eclipse.stardust.ui.web.modeler.xpdl.edit;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.eclipse.emf.common.util.EList;
import org.junit.Test;

import org.eclipse.stardust.model.xpdl.carnot.*;
import org.eclipse.stardust.model.xpdl.xpdl2.ExternalPackage;

public class TestCrossModelDataSymbol extends RecordingTestcase
{
   @Test
   public void testCrossModelDataSymbol() throws Exception
   {
      providerModel = modelService.findModel(PROVIDER_MODEL_ID);
      providerModel2 = modelService.findModel(PROVIDER_MODEL_ID2);

      consumerModel = modelService.findModel(CONSUMER_MODEL_ID);

      InputStream requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/createCrossModelDataSymbols.txt");
      InputStreamReader requestStream = new InputStreamReader(requestInput);
      replay(requestStream, "createCrossModelDataSymbols", false);

      
      DataType primitiveData1 = null;
      DataType primitiveData2 = null;
      boolean duplicates = false;
      for(DataType data : consumerModel.getData())
      {
         if(data.getId().equals("PrimitiveData1"))
         {
            if(primitiveData1 == null)
            {
               primitiveData1 = data;
            }
            else
            {
               duplicates = true;
            }            
         }
         if(data.getId().equals("PrimitiveData2"))
         {
            if(primitiveData2 == null)
            {
               primitiveData2 = data;
            }
            else
            {
               duplicates = true;
            }            
         }
      }

      assertThat(primitiveData2, is(not(nullValue())));
      assertThat(primitiveData1, is(not(nullValue())));
      assertThat(primitiveData1.eIsProxy(), is(true));
      assertThat(duplicates, is(false));
      
      EList<ProcessDefinitionType> processDefinitions = consumerModel.getProcessDefinition();
      ProcessDefinitionType processDefinition = processDefinitions.get(0);
      assertThat(processDefinition, is(not(nullValue())));
      DiagramType diagramType = processDefinition.getDiagram().get(0);
      assertThat(diagramType, is(not(nullValue())));
      PoolSymbol poolSymbol = diagramType.getPoolSymbols().get(0);
      assertThat(poolSymbol, is(not(nullValue())));
      LaneSymbol laneSymbol = poolSymbol.getLanes().get(0);
      assertThat(laneSymbol, is(not(nullValue())));
      EList<DataSymbolType> dataSymbol = laneSymbol.getDataSymbol();
      assertThat(dataSymbol.size(), is(2));
      DataSymbolType symbol1 = dataSymbol.get(0);
      assertThat(symbol1.getData(), is(primitiveData1));
      DataSymbolType symbol2 = dataSymbol.get(1);
      assertThat(symbol2.getData(), is(primitiveData1));
      
      
      EList<ExternalPackage> externalPackage = consumerModel.getExternalPackages().getExternalPackage();
      assertThat(externalPackage.size(), is(1));

      saveReplayModel("C:/tmp");
   }

   protected boolean includeProviderModel2()
   {
      return true;
   }
}