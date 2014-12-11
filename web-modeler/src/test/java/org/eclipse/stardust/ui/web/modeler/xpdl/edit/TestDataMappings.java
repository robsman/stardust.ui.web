package org.eclipse.stardust.ui.web.modeler.xpdl.edit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.eclipse.stardust.model.xpdl.carnot.ActivityImplementationType;
import org.eclipse.stardust.model.xpdl.carnot.ActivityType;
import org.eclipse.stardust.model.xpdl.carnot.ApplicationType;
import org.eclipse.stardust.model.xpdl.carnot.ContextType;
import org.eclipse.stardust.model.xpdl.carnot.DataMappingType;
import org.eclipse.stardust.model.xpdl.carnot.DataType;
import org.eclipse.stardust.model.xpdl.carnot.DirectionType;
import org.eclipse.stardust.model.xpdl.carnot.ProcessDefinitionType;
import org.eclipse.stardust.model.xpdl.carnot.util.ActivityUtil;
import org.eclipse.stardust.ui.web.modeler.utils.test.GenericModelingAssertions;
import org.junit.Test;

public class TestDataMappings extends RecordingTestcase
{
   @Test
   public void testUIMashUpDatamappings() throws Exception
   {
      providerModel = modelService.findModel(PROVIDER_MODEL_ID);
      //consumerModel = modelService.findModel(CONSUMER_MODEL_ID);

      InputStream requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/createUIMashupDataMappingOperations.txt");
      InputStreamReader requestStream = new InputStreamReader(requestInput);

      replay(requestStream, "testUIMashUpDatamappings");

      ProcessDefinitionType process = GenericModelingAssertions.assertProcess(providerModel, "DatamappingProcess","Datamapping Process");
      ActivityType activity = assertUIMashupProcess(process);
      assertDataMappings(activity);


      //saveReplayModel("C:/development/");
   }



   private void assertDataMappings(ActivityType activity)
   {
      List<DataMappingType> dataMappings = activity.getDataMapping();
      assertThat(dataMappings, is(not(nullValue())));
      assertThat(dataMappings.size(), is(4));

      DataType data = GenericModelingAssertions.assertPrimitiveData(providerModel, "InPrimitiveTypeLong", "InPrimitiveTypeLong", "long");
      GenericModelingAssertions.assertDataMapping(activity, "InPrimitiveTypeLong", "PrimitiveParameterIN", "externalWebApp", DirectionType.IN_LITERAL, data);

      data = GenericModelingAssertions.assertPrimitiveData(providerModel, "OutPrimitiveTypeText", "OutPrimitiveTypeText", "String");
      GenericModelingAssertions.assertDataMapping(activity, "OutPrimitiveTypeText", "PrimitiveParameterOUT", "externalWebApp", DirectionType.OUT_LITERAL, data);


      data = GenericModelingAssertions.assertStructData(providerModel, "InStructType", "InStructType", "InStructType");
      GenericModelingAssertions.assertDataMapping(activity, "InStructType", "StructParameterIN", "externalWebApp", DirectionType.IN_LITERAL, data);

      data = GenericModelingAssertions.assertStructData(providerModel, "OutStructType", "OutStructType", "OutStructType");
      GenericModelingAssertions.assertDataMapping(activity, "OutStructType", "StructParameterOUT", "externalWebApp", DirectionType.OUT_LITERAL, data);

   }

   private ActivityType assertUIMashupProcess(ProcessDefinitionType process)
   {
      ActivityType activity = GenericModelingAssertions.assertActivity(process, "UIMashup", "UI Mashup", ActivityImplementationType.APPLICATION_LITERAL);
      ApplicationType application = GenericModelingAssertions.assertApplication(providerModel, "UIMashup");
      ContextType context = GenericModelingAssertions.assertApplicationContextType(application, "externalWebApp");
      GenericModelingAssertions.assertAccessPoint(context, "StructParameterIN", "Struct Parameter IN", DirectionType.IN_LITERAL, "struct", "InStructType");
      GenericModelingAssertions.assertAccessPoint(context, "StructParameterOUT", "Struct Parameter OUT", DirectionType.OUT_LITERAL, "struct", "OutStructType");
      GenericModelingAssertions.assertAccessPoint(context, "PrimitiveParameterIN", "Primitive Parameter IN", DirectionType.IN_LITERAL, "primitive", "long");
      GenericModelingAssertions.assertAccessPoint(context, "PrimitiveParameterOUT", "Primitive Parameter OUT", DirectionType.OUT_LITERAL, "primitive", "String");
      return activity;
   }

   protected boolean includeConsumerModel()
   {
      return false;
   }


}
