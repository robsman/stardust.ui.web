package org.eclipse.stardust.ui.web.modeler.xpdl.edit;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.junit.Test;

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
   public void testResetToDefaults() throws Throwable
   {
      providerModel = modelService.findModel("PermissionModelEmpty");

      InputStream requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/changePermissionProcess.txt");
      InputStreamReader requestStream = new InputStreamReader(requestInput);
      replay(requestStream, "testProcessPermission", false);

      String command = "{\"commandId\":\"permission.restoreDefaults\",\"modelId\":\"PermissionModelEmpty\",\"changeDescriptions\":[{\"uuid\":\"00000000-0000-0000-0000-000000000278\",\"changes\":{\"permissionID\":\"processDefinition.deleteProcessInstances\"}}]}";
      replaySimple(command, "testRestoreDefaults", null);    
      
      

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
