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

public class TestDataMappings extends RecordingTestcase
{
   @Test
   public void testUIMashUpDatamappings() throws Exception
   {
      providerModel = modelService.findModel(PROVIDER_MODEL_ID);

      InputStream requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/createUIMashupDataMappingOperations.txt");
      InputStreamReader requestStream = new InputStreamReader(requestInput);

      replay(requestStream, "testUIMashUpDatamappings", true);

      //saveReplayModel("C:/development/");

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
   }

   public void testUIMashUpDatamappingsCallback(TestResponse response)
         throws AssertionError
   {
      if (response.getCommandID().equals("connection.create"))
      {
         if (response.getResponseNumber() == 47)
         {
            JsonObject modelElement = response.getAdded().get(1).getAsJsonObject().get("modelElement").getAsJsonObject();
            GenericModelingAssertions.assertJsonHas(modelElement, "type", "dataMappings", "id", "name", "dataFullId", "activityId");
            JsonArray dataMappings = modelElement.get("dataMappings").getAsJsonArray();
            JsonObject dataMapping = dataMappings.get(0).getAsJsonObject();
            GenericModelingAssertions.assertJsonHas(dataMapping, "id", "name", "direction", "dataPath");
            assertThat(dataMapping.get("id").getAsString(), is("InPrimitiveTypeLong"));
            assertThat(dataMapping.get("name").getAsString(), is("InPrimitiveTypeLong"));
            assertThat(dataMapping.get("direction").getAsString(), is("IN"));
         }
      }
   }

   @Test
   public void testChangeDataMappingsToInputOutput() throws Exception
   {
      providerModel = modelService.findModel(PROVIDER_MODEL_ID);

      InputStream requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/changeDataMappingsToInputOutput.txt");
      InputStreamReader requestStream = new InputStreamReader(requestInput);


      replay(requestStream, "testChangeDataMappingsToInputOutput", false);

      ProcessDefinitionType process = GenericModelingAssertions.assertProcess(providerModel, "DatamappingProcess","Datamapping Process");
      ActivityType activity = assertUIMashupProcess(process);

      List<DataMappingType> dataMappings = activity.getDataMapping();
      assertThat(dataMappings, is(not(nullValue())));
      assertThat(dataMappings.size(), is(6));

      DataType data = GenericModelingAssertions.assertPrimitiveData(providerModel, "InPrimitiveTypeLong", "InPrimitiveTypeLong", "long");
      DataMappingType dataMapping = assertApplicationDataMapping(activity, "InPrimitiveTypeLong", "InPrimitiveTypeLong", "PrimitiveParameterIN", "externalWebApp", DirectionType.IN_LITERAL, data);

      data = GenericModelingAssertions.assertPrimitiveData(providerModel, "OutPrimitiveTypeText", "OutPrimitiveTypeText", "String");
      dataMapping = GenericModelingAssertions.assertDataMapping(activity, "OutPrimitiveTypeText", "OutPrimitiveTypeText", "default", DirectionType.IN_LITERAL, data, null, null, null);
      assertThat(dataMapping.getApplicationAccessPoint(), is(nullValue()));

      assertApplicationDataMapping(activity, "OutPrimitiveTypeText", "OutPrimitiveTypeText", "PrimitiveParameterOUT", "externalWebApp", DirectionType.OUT_LITERAL, data);

      data = GenericModelingAssertions.assertStructData(providerModel, "InStructType", "InStructType", "InStructType");
      assertApplicationDataMapping(activity, "InStructType", "InStructType", "StructParameterIN", "externalWebApp", DirectionType.IN_LITERAL, data);

      dataMapping = GenericModelingAssertions.assertDataMapping(activity, "InStructType", "InStructType", "default", DirectionType.OUT_LITERAL, data, null, null, null);
      assertThat(dataMapping.getApplicationAccessPoint(), is(nullValue()));

      data = GenericModelingAssertions.assertStructData(providerModel, "OutStructType", "OutStructType", "OutStructType");
      assertApplicationDataMapping(activity, "OutStructType", "OutStructType", "StructParameterOUT", "externalWebApp", DirectionType.OUT_LITERAL, data);



      //saveReplayModel("C:/development/");
   }

   @Test
   public void testChangeDataMappingsGeneral() throws Exception
   {
      providerModel = modelService.findModel(PROVIDER_MODEL_ID);

      InputStream requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/changeDataMappingsGeneral.txt");
      InputStreamReader requestStream = new InputStreamReader(requestInput);

      replay(requestStream, "changeDataMappingsGeneral", true);

      //saveReplayModel("C:/development/");

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
   }

   public void changeDataMappingsGeneralCallback(TestResponse response)
         throws AssertionError
   {
      if (response.getCommandID().equals("datamapping.delete"))
      {
         System.out.println("RESPONSE: " + response.getRemoved().toString());
         if (response.getResponseNumber() == 60)
         {
            //ToDo
         }
      }
   }

   @Test
   public void testMultipleDatamappings() throws Exception
   {
      providerModel = modelService.findModel(PROVIDER_MODEL_ID);

      InputStream requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/createMultipleDataMappings.txt");
      InputStreamReader requestStream = new InputStreamReader(requestInput);

      replay(requestStream, "testMultipleDatamappings", false);

      DataType data = GenericModelingAssertions.assertPrimitiveData(providerModel, "PrimitiveData", "Primitive Data", "String");
      ProcessDefinitionType process = GenericModelingAssertions.assertProcess(providerModel, "ProviderProcess", "Provider Process");
      ActivityType activity = GenericModelingAssertions.assertActivity(process, "Activity1", "Activity 1", ActivityImplementationType.MANUAL_LITERAL);
      GenericModelingAssertions.assertDataMapping(activity, "PrimitiveData", "PrimitiveData", "engine", DirectionType.OUT_LITERAL, data, "activityInstance", null, null);
      GenericModelingAssertions.assertDataMapping(activity, "PrimitiveData", "PrimitiveData", "default", DirectionType.IN_LITERAL, data, null, null, null);
      GenericModelingAssertions.assertDataMapping(activity, "PrimitiveData_2", "PrimitiveData_2", "engine", DirectionType.OUT_LITERAL, data, "activityInstance", null, null);
      GenericModelingAssertions.assertDataMapping(activity, "PrimitiveData_3", "PrimitiveData_3", "engine", DirectionType.OUT_LITERAL, data, "activityInstance", "getAttributes()", "charAt(int)");

      //saveReplayModel("C:/development/");
   }


   @Test
   public void testRenameDatamappings() throws Exception
   {
      providerModel = modelService.findModel(PROVIDER_MODEL_ID);

      InputStream requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/renameDataMappings.txt");
      InputStreamReader requestStream = new InputStreamReader(requestInput);

      replay(requestStream, "renameDataMappings", false);

      DataType data = GenericModelingAssertions.assertPrimitiveData(providerModel, "PrimitiveData", "Primitive Data", "String");
      ProcessDefinitionType process = GenericModelingAssertions.assertProcess(providerModel, "ProviderProcess", "Provider Process");
      ActivityType activity = GenericModelingAssertions.assertActivity(process, "Activity1", "Activity 1", ActivityImplementationType.MANUAL_LITERAL);
      GenericModelingAssertions.assertDataMapping(activity, "PrimitiveData_3", "PrimitiveData_31", "engine", DirectionType.OUT_LITERAL, data, "activityInstance", null, null);
      GenericModelingAssertions.assertDataMapping(activity, "PrimitiveData_3", "PrimitiveData_31", "default", DirectionType.IN_LITERAL, data, null, null, null);
      GenericModelingAssertions.assertDataMapping(activity, "PrimitiveData_2", "PrimitiveData_2", "engine", DirectionType.OUT_LITERAL, data, "activityInstance", null, null);
      GenericModelingAssertions.assertDataMapping(activity, "PrimitiveData_3", "PrimitiveData_3", "engine", DirectionType.OUT_LITERAL, data, "activityInstance", "getAttributes()", "charAt(int)");

      //saveReplayModel("C:/development/");

   }

   @Test
   public void testCloneModelMultipleDatamappings() throws Exception
   {
      providerModel = modelService.findModel(PROVIDER_MODEL_ID);

      InputStream requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/createMultipleDataMappings.txt");
      InputStreamReader requestStream = new InputStreamReader(requestInput);

      replay(requestStream, "testMultipleDatamappings", false);

      String command = "{\"commandId\":\"process.clone\",\"modelId\":\"ProviderModel\",\"changeDescriptions\":[{\"oid\":\"ProviderModel\",\"changes\":{\"id\":\"ProviderProcess\"}}]}";

      replaySimple(command, "testCloneModelMultipleDatamappings", null);

      DataType data = GenericModelingAssertions.assertPrimitiveData(providerModel, "PrimitiveData", "Primitive Data", "String");
      ProcessDefinitionType process = GenericModelingAssertions.assertProcess(providerModel, "ProviderProcessCloned", "CLONE - Provider Process");
      ActivityType activity = GenericModelingAssertions.assertActivity(process, "Activity1", "Activity 1", ActivityImplementationType.MANUAL_LITERAL);
      GenericModelingAssertions.assertDataMapping(activity, "PrimitiveData", "PrimitiveData", "engine", DirectionType.OUT_LITERAL, data, "activityInstance", null, null);
      GenericModelingAssertions.assertDataMapping(activity, "PrimitiveData", "PrimitiveData", "default", DirectionType.IN_LITERAL, data, null, null, null);
      GenericModelingAssertions.assertDataMapping(activity, "PrimitiveData_2", "PrimitiveData_2", "engine", DirectionType.OUT_LITERAL, data, "activityInstance", null, null);
      GenericModelingAssertions.assertDataMapping(activity, "PrimitiveData_3", "PrimitiveData_3", "engine", DirectionType.OUT_LITERAL, data, "activityInstance", "getAttributes()", "charAt(int)");

      //saveReplayModel("C:/development/");
   }


   @Test
   public void testCloneProcessUIMashUpDatamappings() throws Exception
   {
      providerModel = modelService.findModel(PROVIDER_MODEL_ID);

      InputStream requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/createUIMashupDataMappingOperations.txt");
      InputStreamReader requestStream = new InputStreamReader(requestInput);

      replay(requestStream, "testUIMashUpDatamappings", false);


      String command = "{\"commandId\":\"process.clone\",\"modelId\":\"ProviderModel\",\"changeDescriptions\":[{\"oid\":\"ProviderModel\",\"changes\":{\"id\":\"DatamappingProcess\"}}]}";

      replaySimple(command, "testCloneProcessUIMashUpDatamappings", null);

      ProcessDefinitionType process = GenericModelingAssertions.assertProcess(providerModel, "DatamappingProcessCloned","CLONE - Datamapping Process");
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
      DataMappingType dataMapping = GenericModelingAssertions.assertDataMapping(activity, dataMappingID, dataMappingName, context, direction, data, accessPointID, null, null);
      assertThat(dataMapping.getApplicationAccessPoint(), is(not(nullValue())));
      assertThat(dataMapping.getApplicationAccessPoint(), is(accessPointID));
      return dataMapping;
   }





   protected boolean includeConsumerModel()
   {
      return false;
   }


}
