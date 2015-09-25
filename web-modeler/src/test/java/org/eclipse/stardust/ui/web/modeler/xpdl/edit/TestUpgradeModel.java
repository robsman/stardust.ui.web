package org.eclipse.stardust.ui.web.modeler.xpdl.edit;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.eclipse.emf.common.util.EList;
import org.junit.Test;

import org.eclipse.stardust.model.xpdl.carnot.*;
import org.eclipse.stardust.model.xpdl.carnot.util.DiagramUtil;

public class TestUpgradeModel extends RecordingTestcase
{
   @Test
   public void testUpgradeModel() throws Exception
   {
      upgradeModel = modelService.findModel(UPGRADE_MODEL_ID);
      ProcessDefinitionType processDefinitionType = upgradeModel.getProcessDefinition().get(0);
      DiagramType diagramType = processDefinitionType.getDiagram().get(0);
      PoolSymbol defaultPool = DiagramUtil.getDefaultPool(diagramType);
      EList<DataMappingConnectionType> dataMappingConnection = defaultPool.getDataMappingConnection();
      assertThat(dataMappingConnection.size(), is(0));
      LaneSymbol laneSymbol = defaultPool.getLanes().get(0);
      dataMappingConnection = laneSymbol.getDataMappingConnection();
      assertThat(dataMappingConnection.size(), is(1));
      
      modelService.upgradeAllModels();
      //saveModel();

      upgradeModel = modelService.findModel(UPGRADE_MODEL_ID);
      processDefinitionType = upgradeModel.getProcessDefinition().get(0);
      diagramType = processDefinitionType.getDiagram().get(0);
      defaultPool = DiagramUtil.getDefaultPool(diagramType);
      dataMappingConnection = defaultPool.getDataMappingConnection();
      assertThat(dataMappingConnection.size(), is(1));
      laneSymbol = defaultPool.getLanes().get(0);
      dataMappingConnection = laneSymbol.getDataMappingConnection();
      assertThat(dataMappingConnection.size(), is(0));
      
      
      // saveReplayModel("C:/tmp");
   }

   @Override
   protected boolean includeConsumerModel()
   {
      return false;
   }

   protected boolean includeUpgradeModel()
   {
      return true;
   }
}