package org.eclipse.stardust.ui.web.modeler.xpdl.edit;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.eclipse.stardust.model.xpdl.carnot.ActivityImplementationType;
import org.eclipse.stardust.model.xpdl.carnot.ActivityType;
import org.eclipse.stardust.model.xpdl.carnot.DataType;
import org.eclipse.stardust.model.xpdl.carnot.ProcessDefinitionType;
import org.eclipse.stardust.ui.web.modeler.utils.test.GenericModelingAssertions;

public class TestAuthorization extends RecordingTestcase
{

      
   @Test
   public void testProcessPermission() throws Throwable
   {
      providerModel = modelService.findModel("PermissionModelEmpty");
      
      
      InputStream requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/changePermissionProcess.txt");
      InputStreamReader requestStream = new InputStreamReader(requestInput);

      replay(requestStream, "testProcessPermission", false);
      
      //saveReplayModel("C:/development/");

      ProcessDefinitionType process = GenericModelingAssertions.assertProcess(
            providerModel, "PermissionProcess", "PermissionProcess");
           
      GenericModelingAssertions.assertAttribute(process, "authorization:processDefinition.abortProcessInstances", "__carnot_internal_all_permissions__");
      GenericModelingAssertions.assertAttribute(process, "authorization:processDefinition.modifyAttributes", "__carnot_internal_all_permissions__");
      GenericModelingAssertions.assertAttribute(process, "authorization:processDefinition.modifyProcessInstances[0]", "Role1");
      GenericModelingAssertions.assertAttribute(process, "authorization:processDefinition.modifyProcessInstances[1]", "Role3");
      GenericModelingAssertions.assertAttribute(process, "authorization:processDefinition.deleteProcessInstances[0]", "Administrator");
      GenericModelingAssertions.assertAttribute(process, "authorization:processDefinition.deleteProcessInstances[1]", "Organization1");
      GenericModelingAssertions.assertAttribute(process, "authorization:processDefinition.deleteProcessInstances[2]", "Role4");
      GenericModelingAssertions.assertAttribute(process, "authorization:processDefinition.deleteProcessInstances[3]", "Organization3");
      GenericModelingAssertions.assertAttribute(process, "authorization:processDefinition.readProcessInstanceData", "__carnot_internal_all_permissions__");
      GenericModelingAssertions.assertAttribute(process, "authorization:processDefinition.startProcesses[0]", "Administrator");
      GenericModelingAssertions.assertAttribute(process, "authorization:processDefinition.manageEventHandlers[0]", "Administrator");

   }
   
   @Test
   public void testActivityPermission() throws Throwable
   {
      providerModel = modelService.findModel("PermissionModelEmpty");
      
      
      InputStream requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/changePermissionActivity.txt");
      InputStreamReader requestStream = new InputStreamReader(requestInput);

      replay(requestStream, "testActivityPermission", false);
            
      ProcessDefinitionType process = GenericModelingAssertions.assertProcess(
            providerModel, "PermissionProcess", "PermissionProcess");
      ActivityType activity = GenericModelingAssertions.assertActivity(process, "PermissionActivity", "PermissionActivity", ActivityImplementationType.MANUAL_LITERAL);
            
      GenericModelingAssertions.assertAttribute(activity, "authorization:activity.delegateToOther[0]", "Role1");
      GenericModelingAssertions.assertAttribute(activity, "authorization:activity.delegateToDepartment", "__carnot_internal_all_permissions__");
      GenericModelingAssertions.assertAttribute(activity, "authorization:activity.manageEventHandlers[0]", "Role1");
      GenericModelingAssertions.assertAttribute(activity, "authorization:activity.manageEventHandlers[1]", "Role2");
      GenericModelingAssertions.assertAttribute(activity, "authorization:activity.manageEventHandlers[2]", "Role3");
      GenericModelingAssertions.assertAttribute(activity, "authorization:activity.manageEventHandlers[3]", "Role4");
      GenericModelingAssertions.assertAttribute(activity, "authorization:activity.modifyAttributes[0]", "Organization1");
      GenericModelingAssertions.assertAttribute(activity, "authorization:activity.modifyAttributes[1]", "Organization2");
      GenericModelingAssertions.assertAttribute(activity, "authorization:activity.modifyAttributes[2]", "Organization3");
      GenericModelingAssertions.assertAttribute(activity, "authorization:activity.readActivityInstanceData[0]", "Role3");
      GenericModelingAssertions.assertAttribute(activity, "authorization:activity.readActivityInstanceData[1]", "Role4");
      GenericModelingAssertions.assertAttribute(activity, "authorization:activity.readActivityInstanceData[2]", "Organization2");
      GenericModelingAssertions.assertAttribute(activity, "authorization:activity.readActivityInstanceData[3]", "Organization3");
      GenericModelingAssertions.assertAttribute(activity, "authorization:activity.abortActivityInstances", "__carnot_internal_all_permissions__");

   }
   
   @Test
   public void testDataPermission() throws Throwable
   {
      providerModel = modelService.findModel("PermissionModelEmpty");
      
      
      InputStream requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/changePermissionData.txt");
      InputStreamReader requestStream = new InputStreamReader(requestInput);

      replay(requestStream, "testDataPermission", true);
      
      DataType data = GenericModelingAssertions.assertPrimitiveData(providerModel, "PermissionData", "PermissionData", "String");
               
      GenericModelingAssertions.assertAttribute(data, "authorization:data.readDataValues[0]", "Administrator");      
      GenericModelingAssertions.assertAttribute(data, "authorization:data.modifyDataValues[0]", "Role1");
      GenericModelingAssertions.assertAttribute(data, "authorization:data.modifyDataValues[1]", "Role2");
      GenericModelingAssertions.assertAttribute(data, "authorization:data.modifyDataValues[2]", "Role3");
      GenericModelingAssertions.assertAttribute(data, "authorization:data.modifyDataValues[3]", "Role4");
      GenericModelingAssertions.assertAttribute(data, "authorization:data.modifyDataValues[4]", "Organization1");
      GenericModelingAssertions.assertAttribute(data, "authorization:data.modifyDataValues[5]", "Organization2");
      GenericModelingAssertions.assertAttribute(data, "authorization:data.modifyDataValues[6]", "Organization3");

   }   
   
   public void testDataPermissionCallback(TestResponse response)
         throws AssertionError
   {
      if (response.getResponseNumber() == 2)
      {         
         assertThat(response.getModified().size(), is(1));
         JsonObject dataJson = response.getModified().get(0).getAsJsonObject();
         GenericModelingAssertions.assertJsonHas(dataJson, "permissions");
         JsonArray permissions = dataJson.get("permissions").getAsJsonArray();
         
         //Permission "readDataValues" 
         JsonObject readDataValues = permissions.get(0).getAsJsonObject();
         GenericModelingAssertions.assertJsonHasKeyValue(readDataValues, "id=data.readDataValues","isEmpty=false","defaultAll=true","defaultOwner=false");
         GenericModelingAssertions.assertJsonHas(readDataValues,"participants", "defaultParticipants", "fixedParticipants");
         JsonArray participants = readDataValues.get("participants").getAsJsonArray();
         assertThat(participants.size(), is(1));
         JsonObject participant = participants.get(0).getAsJsonObject();
         GenericModelingAssertions.assertJsonHasKeyValue(participant, "participantFullId=PermissionModelEmpty:Administrator");
         
         //Permissions "modifyDataValues"
         JsonObject modifyDataValues = permissions.get(1).getAsJsonObject();
         GenericModelingAssertions.assertJsonHasKeyValue(modifyDataValues, "id=data.modifyDataValues","isEmpty=false","defaultAll=true","defaultOwner=false");
         GenericModelingAssertions.assertJsonHas(modifyDataValues,"participants", "defaultParticipants", "fixedParticipants");
         participants = modifyDataValues.get("participants").getAsJsonArray();
         assertThat(participants.size(), is(7));
         GenericModelingAssertions.assertJsonArrayHasKeyValue(participants, "participantFullId=PermissionModelEmpty:Role1","participantFullId=PermissionModelEmpty:Role2","participantFullId=PermissionModelEmpty:Role3","participantFullId=PermissionModelEmpty:Role4","participantFullId=PermissionModelEmpty:Organization1","participantFullId=PermissionModelEmpty:Organization2","participantFullId=PermissionModelEmpty:Organization3");         
      }
   }
   
   
   
   @Test
   public void testResetToDefaults() throws Throwable
   {
      providerModel = modelService.findModel("PermissionModelEmpty");

      InputStream requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/changePermissionProcess.txt");
      InputStreamReader requestStream = new InputStreamReader(requestInput);
      replay(requestStream, "testProcessPermission", false);

      String command = "{\"commandId\":\"permission.restoreDefaults\",\"modelId\":\"PermissionModelEmpty\",\"changeDescriptions\":[{\"uuid\":\"00000000-0000-0000-0000-000000000278\",\"changes\":{\"permissionID\":\"processDefinition.deleteProcessInstances\"}}]}";
      replaySimple(command, "testResetToDefaults", null, true);    
      
      

      ProcessDefinitionType process = GenericModelingAssertions.assertProcess(
            providerModel, "PermissionProcess", "PermissionProcess");
      
      
      GenericModelingAssertions.assertAttribute(process, "authorization:processDefinition.abortProcessInstances", "__carnot_internal_all_permissions__");
      GenericModelingAssertions.assertAttribute(process, "authorization:processDefinition.modifyAttributes", "__carnot_internal_all_permissions__");
      GenericModelingAssertions.assertAttribute(process, "authorization:processDefinition.modifyProcessInstances[0]", "Role1");
      GenericModelingAssertions.assertAttribute(process, "authorization:processDefinition.modifyProcessInstances[1]", "Role3");
      GenericModelingAssertions.assertAttribute(process, "authorization:processDefinition.deleteProcessInstances[0]", "Administrator");
      GenericModelingAssertions.assertAttribute(process, "authorization:processDefinition.readProcessInstanceData", "__carnot_internal_all_permissions__");
      GenericModelingAssertions.assertAttribute(process, "authorization:processDefinition.startProcesses[0]", "Administrator");
      GenericModelingAssertions.assertAttribute(process, "authorization:processDefinition.manageEventHandlers[0]", "Administrator");

   }
   
   public void testResetToDefaultsCallback(TestResponse response)
         throws AssertionError
   {
      if (response.getResponseNumber() == 1) 
      {
         assertThat(response.getModified().size(), is(1));
         JsonObject dataJson = response.getModified().get(0).getAsJsonObject();
         GenericModelingAssertions.assertJsonHas(dataJson, "permissions");
         JsonArray permissions = dataJson.get("permissions").getAsJsonArray();
         
         //Permission "deleteProcessInstances" 
         JsonObject readDataValues = permissions.get(3).getAsJsonObject();
         GenericModelingAssertions.assertJsonHasKeyValue(readDataValues, "id=processDefinition.deleteProcessInstances","isEmpty=false","defaultAll=false","defaultOwner=false");
         GenericModelingAssertions.assertJsonHas(readDataValues,"participants", "defaultParticipants", "fixedParticipants");
         JsonArray participants = readDataValues.get("participants").getAsJsonArray();
         assertThat(participants.size(), is(1));
         JsonObject participant = participants.get(0).getAsJsonObject();
         GenericModelingAssertions.assertJsonHasKeyValue(participant, "participantFullId=PermissionModelEmpty:Administrator");
         JsonArray defaultParticipants = readDataValues.get("defaultParticipants").getAsJsonArray();
         assertThat(defaultParticipants.size(), is(1));
         JsonObject defaultParticipant = defaultParticipants.get(0).getAsJsonObject();
         GenericModelingAssertions.assertJsonHasKeyValue(defaultParticipant, "participantFullId=PermissionModelEmpty:Administrator");
      }

   }
   
   
   

  
   @Override
   protected String getProviderModelID()
   {
      return "PermissionModelEmpty";
   }


   protected boolean includeConsumerModel()
   {
      return false;
   }
   
    
}
