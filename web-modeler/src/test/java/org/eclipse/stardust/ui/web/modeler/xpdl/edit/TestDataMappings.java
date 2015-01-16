package org.eclipse.stardust.ui.web.modeler.xpdl.edit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.junit.Test;

import org.eclipse.stardust.model.xpdl.carnot.*;
import org.eclipse.stardust.ui.web.modeler.utils.test.GenericModelingAssertions;

public class TestDataMappings extends RecordingTestcase
{
   @Test
   public void testUIMashUpDatamappings() throws Exception
   {
      providerModel = modelService.findModel(PROVIDER_MODEL_ID);

      InputStream requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/createUIMashupDataMappingOperations.txt");
      InputStreamReader requestStream = new InputStreamReader(requestInput);

      replay(requestStream, "testUIMashUpDatamappings");

      ProcessDefinitionType process = GenericModelingAssertions.assertProcess(providerModel, "DatamappingProcess","Datamapping Process");
      ActivityType activity = assertUIMashupProcess(process);

      List<DataMappingType> dataMappings = activity.getDataMapping();
      assertThat(dataMappings, is(not(nullValue())));
      assertThat(dataMappings.size(), is(4));

      DataType data = GenericModelingAssertions.assertPrimitiveData(providerModel, "InPrimitiveTypeLong", "InPrimitiveTypeLong", "long");
      assertApplicationDataMapping(activity, "InPrimitiveTypeLong", "InPrimitiveTypeLong", "PrimitiveParameterIN", "externalWebApp", DirectionType.IN_LITERAL, data);

      data = GenericModelingAssertions.assertPrimitiveData(providerModel, "OutPrimitiveTypeText", "OutPrimitiveTypeText", "String");
      assertApplicationDataMapping(activity, "OutPrimitiveTypeText", "OutPrimitiveTypeText", "PrimitiveParameterOUT", "externalWebApp", DirectionType.OUT_LITERAL, data);


      data = GenericModelingAssertions.assertStructData(providerModel, "InStructType", "InStructType", "InStructType");
      assertApplicationDataMapping(activity, "InStructType", "InStructType", "StructParameterIN", "externalWebApp", DirectionType.IN_LITERAL, data);

      data = GenericModelingAssertions.assertStructData(providerModel, "OutStructType", "OutStructType", "OutStructType");
      assertApplicationDataMapping(activity, "OutStructType", "OutStructType", "StructParameterOUT", "externalWebApp", DirectionType.OUT_LITERAL, data);


      //saveReplayModel("C:/development/");
   }

   @Test
   public void testChangeDataMappingsToInputOutput() throws Exception
   {
      providerModel = modelService.findModel(PROVIDER_MODEL_ID);
      testUIMashUpDatamappings();
      initUUIDMap();

      InputStream requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/changeDataMappingsToInputOutput.txt");
      InputStreamReader requestStream = new InputStreamReader(requestInput);

      replay(requestStream, "testChangeDataMappingsToInputOutput");

      ProcessDefinitionType process = GenericModelingAssertions.assertProcess(providerModel, "DatamappingProcess","Datamapping Process");
      ActivityType activity = assertUIMashupProcess(process);

      List<DataMappingType> dataMappings = activity.getDataMapping();
      assertThat(dataMappings, is(not(nullValue())));
      assertThat(dataMappings.size(), is(6));

      DataType data = GenericModelingAssertions.assertPrimitiveData(providerModel, "InPrimitiveTypeLong", "InPrimitiveTypeLong", "long");
      DataMappingType dataMapping = assertApplicationDataMapping(activity, "InPrimitiveTypeLong", "InPrimitiveTypeLong", "PrimitiveParameterIN", "externalWebApp", DirectionType.IN_LITERAL, data);

      data = GenericModelingAssertions.assertPrimitiveData(providerModel, "OutPrimitiveTypeText", "OutPrimitiveTypeText", "String");
      dataMapping = GenericModelingAssertions.assertDataMapping(activity, "OutPrimitiveTypeText", "OutPrimitiveTypeText", "default", DirectionType.IN_LITERAL, data);
      assertThat(dataMapping.getApplicationAccessPoint(), is(nullValue()));

      assertApplicationDataMapping(activity, "OutPrimitiveTypeText", "OutPrimitiveTypeText", "PrimitiveParameterOUT", "externalWebApp", DirectionType.OUT_LITERAL, data);

      data = GenericModelingAssertions.assertStructData(providerModel, "InStructType", "InStructType", "InStructType");
      assertApplicationDataMapping(activity, "InStructType", "InStructType", "StructParameterIN", "externalWebApp", DirectionType.IN_LITERAL, data);

      dataMapping = GenericModelingAssertions.assertDataMapping(activity, "InStructType", "InStructType", "default", DirectionType.OUT_LITERAL, data);
      assertThat(dataMapping.getApplicationAccessPoint(), is(nullValue()));

      data = GenericModelingAssertions.assertStructData(providerModel, "OutStructType", "OutStructType", "OutStructType");
      assertApplicationDataMapping(activity, "OutStructType", "OutStructType", "StructParameterOUT", "externalWebApp", DirectionType.OUT_LITERAL, data);



      //saveReplayModel("C:/development/");
   }

   @Test
   public void testChangeDataMappingsGeneral() throws Exception
   {
      providerModel = modelService.findModel(PROVIDER_MODEL_ID);
      testChangeDataMappingsToInputOutput();
      initUUIDMap();

      InputStream requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/changeDataMappingsGeneral.txt");
      InputStreamReader requestStream = new InputStreamReader(requestInput);

      replay(requestStream, "changeDataMappingsGeneral");

      ProcessDefinitionType process = GenericModelingAssertions.assertProcess(providerModel, "DatamappingProcess","Datamapping Process");
      ActivityType activity = assertUIMashupProcess(process);

      List<DataMappingType> dataMappings = activity.getDataMapping();
      assertThat(dataMappings, is(not(nullValue())));
      assertThat(dataMappings.size(), is(6));

      DataType data = GenericModelingAssertions.assertPrimitiveData(providerModel, "InPrimitiveTypeLong", "InPrimitiveTypeLong", "long");
      assertApplicationDataMapping(activity, "InPrimitiveTypeLong", "InPrimitiveTypeLong", "PrimitiveParameterIN", "externalWebApp", DirectionType.IN_LITERAL, data);

      data = GenericModelingAssertions.assertPrimitiveData(providerModel, "OutPrimitiveTypeText", "OutPrimitiveTypeText", "String");
      assertApplicationDataMapping(activity, "OutPrimitiveTypeText", "OutPrimitiveTypeText", "PrimitiveParameterIN", "externalWebApp", DirectionType.IN_LITERAL, data);
      assertApplicationDataMapping(activity, "OutPrimitiveTypeText", "OutPrimitiveTypeText", "PrimitiveParameterOUT", "externalWebApp", DirectionType.OUT_LITERAL, data);

      data = GenericModelingAssertions.assertStructData(providerModel, "InStructType", "InStructType", "InStructType");
      assertApplicationDataMapping(activity, "InStructType", "InStructType", "StructParameterIN", "externalWebApp", DirectionType.IN_LITERAL, data);
      assertApplicationDataMapping(activity, "InStructType", "InStructType", "StructParameterOUT", "externalWebApp", DirectionType.OUT_LITERAL, data);

      data = GenericModelingAssertions.assertStructData(providerModel, "OutStructType", "OutStructType", "OutStructType");
      assertApplicationDataMapping(activity, "OutStructType", "OutStructType", "StructParameterIN", "externalWebApp", DirectionType.IN_LITERAL, data);

      //saveReplayModel("C:/development/");
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

   private DataMappingType assertApplicationDataMapping(ActivityType activity, String dataMappingID, String dataMappingName,
         String accessPointID, String context, DirectionType direction, DataType data)
   {
      DataMappingType dataMapping = GenericModelingAssertions.assertDataMapping(activity, dataMappingID, dataMappingName, context, direction, data);
      assertThat(dataMapping.getApplicationAccessPoint(), is(not(nullValue())));
      assertThat(dataMapping.getApplicationAccessPoint(), is(accessPointID));
      return dataMapping;
   }



   protected boolean includeConsumerModel()
   {
      return false;
   }


}
