package org.eclipse.stardust.ui.web.modeler.xpdl.edit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.junit.Test;

import org.eclipse.stardust.model.xpdl.carnot.*;
import org.eclipse.stardust.ui.web.modeler.utils.test.GenericModelingAssertions;

public class TestUndoOperation extends TestGeneralModeling
{

   @Test
   public void testRemoveAndUndoDataSymbol() throws Exception
   {
      providerModel = modelService.findModel(PROVIDER_MODEL_ID);
      consumerModel = modelService.findModel(CONSUMER_MODEL_ID);



      testBasicModelElementsInProvider();
      initUUIDMap();

      InputStream requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/deleteDataSymbolAndUndo.txt");
      InputStreamReader requestStream = new InputStreamReader(requestInput);

      replay(requestStream, "deleteDataSymbolAndUndo");

      DataType data = GenericModelingAssertions.assertPrimitiveData(providerModel, "ProvidedPrimitive", "ProvidedPrimitive", "String");

      assertThat(data.getSymbols().size(), is(0));

      restController.undoMostCurrentChange();

      ProcessDefinitionType process = GenericModelingAssertions.assertProcess(providerModel, "ProvidedProcess", "ProvidedProcess");
      ActivityType activity1 = GenericModelingAssertions.assertActivity(process, "Activity1",  "Activity 1", ActivityImplementationType.MANUAL_LITERAL);
      ActivityType activity2 = GenericModelingAssertions.assertActivity(process, "Activity2",  "Activity 2", ActivityImplementationType.MANUAL_LITERAL);
      assertThat(data.getSymbols().size(), is(1));

      GenericModelingAssertions.assertDataMapping(activity1, "ProvidedPrimitive", "ProvidedPrimitive", "default", DirectionType.OUT_LITERAL, data, null, null, null);
      GenericModelingAssertions.assertDataMapping(activity2, "ProvidedPrimitive", "ProvidedPrimitive", "default", DirectionType.IN_LITERAL, data, null, null, null);

      assertDataSymbol(process, data);

      //saveReplayModel("C:/development/");

   }

   private DataSymbolType assertDataSymbol(ProcessDefinitionType process, DataType data)
   {
      DiagramType diagram = process.getDiagram().get(0);
      PoolSymbol poolSymbol = diagram.getPoolSymbols().get(0);
      LaneSymbol lane = poolSymbol.getLanes().get(0);
      assertThat(lane.getDataSymbol().size(), is(1));
      DataSymbolType symbolType = lane.getDataSymbol().get(0);
      assertThat(symbolType.getData(), is(not(nullValue())));
      assertThat(symbolType.getData(), is(data));
      return symbolType;
   }

}
