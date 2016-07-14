package org.eclipse.stardust.ui.web.modeler.xpdl.edit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.junit.Test;

import com.google.gson.JsonArray;

import org.eclipse.stardust.model.xpdl.carnot.AttributeType;
import org.eclipse.stardust.model.xpdl.carnot.DataMappingType;
import org.eclipse.stardust.model.xpdl.carnot.DataPathType;
import org.eclipse.stardust.model.xpdl.carnot.IExtensibleElement;
import org.eclipse.stardust.model.xpdl.carnot.util.ModelVariable;
import org.eclipse.stardust.model.xpdl.carnot.util.VariableContext;
import org.eclipse.stardust.model.xpdl.carnot.util.VariableContextHelper;
import org.eclipse.stardust.ui.web.modeler.utils.test.GenericModelingAssertions;

public class TestConfigurationVariables extends RecordingTestcase
{

   @Test
   public void testCreateConfigurationVariables() throws Exception
   {
      providerModel = modelService.findModel(PROVIDER_MODEL_ID);
      consumerModel = modelService.findModel(CONSUMER_MODEL_ID);

      InputStream requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/createConfigurationVariables.txt");
      InputStreamReader requestStream = new InputStreamReader(requestInput);
      replay(requestStream, "testCreateConfigurationVariables", false);
      
      JsonArray variablesJson = modelService.getConfigurationVariables(providerModel.getId());
      //Todo: Jsontest
        
      VariableContextHelper contextHelper = modelService.currentSession().variableContextHelper();
      VariableContext context = contextHelper.getContext(providerModel);
      
      List<ModelVariable> variables = context.getVariables();
      
      assertThat(variables.size(), is(3));
      
      ModelVariable variable1 = context.getModelVariableByName("${Variable1}");
      ModelVariable variable2 = context.getModelVariableByName("${Variable2}");
      ModelVariable variable3 = context.getModelVariableByName("${Variable3}");
      
      assertThat(variable1, is(not(nullValue())));
      assertThat(variable2, is(not(nullValue())));
      assertThat(variable3, is(not(nullValue())));
      
      List<EObject> references1 = context.getReferences(variable1);
      List<EObject> references2 = context.getReferences(variable2);
      List<EObject> references3 = context.getReferences(variable3);
      
      assertThat(references1.isEmpty(), is(false));
      assertThat(references2.isEmpty(), is(false));
      assertThat(references3.isEmpty(), is(false));
      
      assertThat(references1.size(), is(1));
      assertThat(references2.size(), is(2));
      assertThat(references3.size(), is(1));
      
      assertThat(variable1.getDefaultValue(), is(""));
      assertThat(variable2.getDefaultValue(), is(""));
      assertThat(variable3.getDefaultValue(), is(""));
      
   }
   
   @Test
   public void testUpdateConfigurationVariable() throws Exception
   {
      providerModel = modelService.findModel(PROVIDER_MODEL_ID);
      consumerModel = modelService.findModel(CONSUMER_MODEL_ID);

      InputStream requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/createConfigurationVariables.txt");
      InputStreamReader requestStream = new InputStreamReader(requestInput);
      replay(requestStream, "testCreateConfigurationVariables", false);
      
      
      VariableContextHelper variableContextHelper = modelService.currentSession().variableContextHelper();
      
      VariableContext context = variableContextHelper.getContext(providerModel);
      if (context == null)
      {
         variableContextHelper.createContext(providerModel);
         context = variableContextHelper.getContext(providerModel);  
      }
      
      JsonArray variablesJson = modelService.getConfigurationVariables(providerModel.getId());
      
      String command = "{\"commandId\":\"configVariable.update\",\"modelId\":\"ProviderModel\",\"account\":\"motu\",\"changeDescriptions\":[{\"uuid\":\"00000000-0000-0000-0000-000000000001\",\"changes\":{\"variableName\":\"${Variable2}\",\"defaultValue\":\"valueVariable2\"}}]}";
      replaySimple(command, "testUpdateConfigurationVariable", null, true);
      
      command = "{\"commandId\":\"configVariable.update\",\"modelId\":\"ProviderModel\",\"account\":\"motu\",\"changeDescriptions\":[{\"uuid\":\"00000000-0000-0000-0000-000000000001\",\"changes\":{\"variableName\":\"${Variable3}\",\"defaultValue\":\"valueVariable3\"}}]}";
      replaySimple(command, "testUpdateConfigurationVariable", null, true);  
      
      command = "{\"commandId\":\"configVariable.update\",\"modelId\":\"ProviderModel\",\"account\":\"motu\",\"changeDescriptions\":[{\"uuid\":\"00000000-0000-0000-0000-000000000001\",\"changes\":{\"variableName\":\"${Variable1}\",\"defaultValue\":\"valueVariable1\"}}]}";
      replaySimple(command, "testUpdateConfigurationVariable", null, true);  
                  
      List<ModelVariable> variables = context.getVariables();
      
      assertThat(variables.size(), is(3));
      
      ModelVariable variable1 = context.getModelVariableByName("${Variable1}");
      ModelVariable variable2 = context.getModelVariableByName("${Variable2}");
      ModelVariable variable3 = context.getModelVariableByName("${Variable3}");
      
      assertThat(variable1, is(not(nullValue())));
      assertThat(variable2, is(not(nullValue())));
      assertThat(variable3, is(not(nullValue())));
      
      List<EObject> references1 = context.getReferences(variable1);
      List<EObject> references2 = context.getReferences(variable2);
      List<EObject> references3 = context.getReferences(variable3);
      
      assertThat(references1.isEmpty(), is(false));
      assertThat(references2.isEmpty(), is(false));
      assertThat(references3.isEmpty(), is(false));
      
      assertThat(references1.size(), is(1));
      assertThat(references2.size(), is(2));
      assertThat(references3.size(), is(1));

      assertThat(variable1.getDefaultValue(), is("valueVariable1"));
      assertThat(variable2.getDefaultValue(), is("valueVariable2"));
      assertThat(variable3.getDefaultValue(), is("valueVariable3"));
      
   }
   
   @Test
   public void testDeleteConfigurationVariableWithDefaultValue() throws Exception
   {
      providerModel = modelService.findModel(PROVIDER_MODEL_ID);
      consumerModel = modelService.findModel(CONSUMER_MODEL_ID);

      InputStream requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/createConfigurationVariables.txt");
      InputStreamReader requestStream = new InputStreamReader(requestInput);
      replay(requestStream, "testCreateConfigurationVariables", false);
      
      
      VariableContextHelper variableContextHelper = modelService.currentSession().variableContextHelper();
      
      VariableContext context = variableContextHelper.getContext(providerModel);
      if (context == null)
      {
         variableContextHelper.createContext(providerModel);
         context = variableContextHelper.getContext(providerModel);  
      }
      
      JsonArray variablesJson = modelService.getConfigurationVariables(providerModel.getId());
      
      
      String command = "{\"commandId\":\"configVariable.update\",\"modelId\":\"ProviderModel\",\"account\":\"motu\",\"changeDescriptions\":[{\"uuid\":\"00000000-0000-0000-0000-000000000001\",\"changes\":{\"variableName\":\"${Variable2}\",\"defaultValue\":\"valueVariable2\"}}]}";
      replaySimple(command, "testUpdateConfigurationVariable", null, true);
      
      command = "{\"commandId\":\"configVariable.update\",\"modelId\":\"ProviderModel\",\"account\":\"motu\",\"changeDescriptions\":[{\"uuid\":\"00000000-0000-0000-0000-000000000001\",\"changes\":{\"variableName\":\"${Variable3}\",\"defaultValue\":\"valueVariable3\"}}]}";
      replaySimple(command, "testUpdateConfigurationVariable", null, true);  
      
      command = "{\"commandId\":\"configVariable.update\",\"modelId\":\"ProviderModel\",\"account\":\"motu\",\"changeDescriptions\":[{\"uuid\":\"00000000-0000-0000-0000-000000000001\",\"changes\":{\"variableName\":\"${Variable1}\",\"defaultValue\":\"valueVariable1\"}}]}";
      replaySimple(command, "testUpdateConfigurationVariable", null, true); 
      
      ModelVariable variable = context.getModelVariableByName("${Variable2}");
      
      AttributeType attribute1 = GenericModelingAssertions.assertConfigVariableAttributeReference(context, variable, 0, "${Variable2}${Variable3}");
      AttributeType attribute2 = GenericModelingAssertions.assertConfigVariableAttributeReference(context, variable, 1, "${Variable1} and ${Variable2}");
      
      
      command = "{\"commandId\":\"configVariable.delete\",\"modelId\":\"ProviderModel\",\"account\":\"motu\",\"changeDescriptions\":[{\"uuid\":\"00000000-0000-0000-0000-000000000001\",\"changes\":{\"variableName\":\"${Variable1}\",\"deleteOptions\":{\"mode\":\"defaultValue\"}}}]}";
      replaySimple(command, "testDeleteConfigurationVariableWithDefaultValue", null, true);
           
      assertThat(attribute1.getValue(), is("${Variable2}${Variable3}"));
      assertThat(attribute2.getValue(), is("valueVariable1 and ${Variable2}"));
      
      modelService.getConfigurationVariables(providerModel.getId());
     
      List<ModelVariable> variables = context.getVariables();
      
      assertThat(variables.size(), is(2));
      
      ModelVariable variable1 = context.getModelVariableByName("${Variable1}");
      ModelVariable variable2 = context.getModelVariableByName("${Variable2}");
      ModelVariable variable3 = context.getModelVariableByName("${Variable3}");
      
      assertThat(variable1, is((nullValue())));
      assertThat(variable2, is(not(nullValue())));
      assertThat(variable3, is(not(nullValue())));
      
      List<EObject> references2 = context.getReferences(variable2);
      List<EObject> references3 = context.getReferences(variable3);
      
      assertThat(references2.isEmpty(), is(false));
      assertThat(references3.isEmpty(), is(false));
      
      assertThat(references2.size(), is(2));
      assertThat(references3.size(), is(1));

      assertThat(variable2.getDefaultValue(), is("valueVariable2"));
      assertThat(variable3.getDefaultValue(), is("valueVariable3"));
      
   }
   
   @Test
   public void testDeleteConfigurationVariableWithArbitraryValue() throws Exception
   {
      providerModel = modelService.findModel(PROVIDER_MODEL_ID);
      consumerModel = modelService.findModel(CONSUMER_MODEL_ID);

      InputStream requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/createConfigurationVariables.txt");
      InputStreamReader requestStream = new InputStreamReader(requestInput);
      replay(requestStream, "testCreateConfigurationVariables", false);
      
      
      VariableContextHelper variableContextHelper = modelService.currentSession().variableContextHelper();
      
      VariableContext context = variableContextHelper.getContext(providerModel);
      if (context == null)
      {
         variableContextHelper.createContext(providerModel);
         context = variableContextHelper.getContext(providerModel);  
      }
      
      JsonArray variablesJson = modelService.getConfigurationVariables(providerModel.getId());
      
      
      String command = "{\"commandId\":\"configVariable.update\",\"modelId\":\"ProviderModel\",\"account\":\"motu\",\"changeDescriptions\":[{\"uuid\":\"00000000-0000-0000-0000-000000000001\",\"changes\":{\"variableName\":\"${Variable2}\",\"defaultValue\":\"valueVariable2\"}}]}";
      replaySimple(command, "testUpdateConfigurationVariable", null, true);
      
      command = "{\"commandId\":\"configVariable.update\",\"modelId\":\"ProviderModel\",\"account\":\"motu\",\"changeDescriptions\":[{\"uuid\":\"00000000-0000-0000-0000-000000000001\",\"changes\":{\"variableName\":\"${Variable3}\",\"defaultValue\":\"valueVariable3\"}}]}";
      replaySimple(command, "testUpdateConfigurationVariable", null, true);  
      
      command = "{\"commandId\":\"configVariable.update\",\"modelId\":\"ProviderModel\",\"account\":\"motu\",\"changeDescriptions\":[{\"uuid\":\"00000000-0000-0000-0000-000000000001\",\"changes\":{\"variableName\":\"${Variable1}\",\"defaultValue\":\"valueVariable1\"}}]}";
      replaySimple(command, "testUpdateConfigurationVariable", null, true); 
          
      ModelVariable variable = context.getModelVariableByName("${Variable2}");
                
      AttributeType attribute1 = GenericModelingAssertions.assertConfigVariableAttributeReference(context, variable, 0, "${Variable2}${Variable3}");
      AttributeType attribute2 = GenericModelingAssertions.assertConfigVariableAttributeReference(context, variable, 1, "${Variable1} and ${Variable2}");
      
      command = "{\"commandId\":\"configVariable.delete\",\"modelId\":\"ProviderModel\",\"account\":\"motu\",\"changeDescriptions\":[{\"uuid\":\"00000000-0000-0000-0000-000000000001\",\"changes\":{\"variableName\":\"${Variable2}\",\"deleteOptions\":{\"mode\":\"withLiteral\",\"literalValue\":\"myValue\"}}}]}";
      replaySimple(command, "testDeleteConfigurationVariableWithArbitraryValue", null, true);
      
      assertThat(attribute1.getAttributeValue(), is("myValue${Variable3}"));
      assertThat(attribute2.getAttributeValue(), is("${Variable1} and myValue"));
      
      modelService.getConfigurationVariables(providerModel.getId());
      
      List<ModelVariable> variables = context.getVariables();
      
      assertThat(variables.size(), is(2));
      
      ModelVariable variable1 = context.getModelVariableByName("${Variable1}");
      ModelVariable variable2 = context.getModelVariableByName("${Variable2}");
      ModelVariable variable3 = context.getModelVariableByName("${Variable3}");
      
      assertThat(variable1, is(not(nullValue())));
      assertThat(variable2, is((nullValue())));
      assertThat(variable3, is(not(nullValue())));
      
      List<EObject> references1 = context.getReferences(variable1);
      List<EObject> references3 = context.getReferences(variable3);
      
      assertThat(references1.isEmpty(), is(false));
      assertThat(references3.isEmpty(), is(false));
      
      assertThat(references1.size(), is(1));
      assertThat(references3.size(), is(1));

      assertThat(variable1.getDefaultValue(), is("valueVariable1"));
      assertThat(variable3.getDefaultValue(), is("valueVariable3"));
      
   }


   
   @Test
   public void testDeleteConfigurationVariableWithEmptyValue() throws Exception
   {
      providerModel = modelService.findModel(PROVIDER_MODEL_ID);
      consumerModel = modelService.findModel(CONSUMER_MODEL_ID);

      InputStream requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/createConfigurationVariables.txt");
      InputStreamReader requestStream = new InputStreamReader(requestInput);
      replay(requestStream, "testCreateConfigurationVariables", false);
      
      
      VariableContextHelper variableContextHelper = modelService.currentSession().variableContextHelper();
      
      VariableContext context = variableContextHelper.getContext(providerModel);
      if (context == null)
      {
         variableContextHelper.createContext(providerModel);
         context = variableContextHelper.getContext(providerModel);  
      }
      
      JsonArray variablesJson = modelService.getConfigurationVariables(providerModel.getId());
      
      
      String command = "{\"commandId\":\"configVariable.update\",\"modelId\":\"ProviderModel\",\"account\":\"motu\",\"changeDescriptions\":[{\"uuid\":\"00000000-0000-0000-0000-000000000001\",\"changes\":{\"variableName\":\"${Variable2}\",\"defaultValue\":\"valueVariable2\"}}]}";
      replaySimple(command, "testUpdateConfigurationVariable", null, true);
      
      command = "{\"commandId\":\"configVariable.update\",\"modelId\":\"ProviderModel\",\"account\":\"motu\",\"changeDescriptions\":[{\"uuid\":\"00000000-0000-0000-0000-000000000001\",\"changes\":{\"variableName\":\"${Variable3}\",\"defaultValue\":\"valueVariable3\"}}]}";
      replaySimple(command, "testUpdateConfigurationVariable", null, true);  
      
      command = "{\"commandId\":\"configVariable.update\",\"modelId\":\"ProviderModel\",\"account\":\"motu\",\"changeDescriptions\":[{\"uuid\":\"00000000-0000-0000-0000-000000000001\",\"changes\":{\"variableName\":\"${Variable1}\",\"defaultValue\":\"valueVariable1\"}}]}";
      replaySimple(command, "testUpdateConfigurationVariable", null, true); 
      
      ModelVariable variable = context.getModelVariableByName("${Variable2}");
      
      AttributeType attribute1 = GenericModelingAssertions.assertConfigVariableAttributeReference(context, variable, 0, "${Variable2}${Variable3}");
      AttributeType attribute2 = GenericModelingAssertions.assertConfigVariableAttributeReference(context, variable, 1, "${Variable1} and ${Variable2}");
      
      command = "{\"commandId\":\"configVariable.delete\",\"modelId\":\"ProviderModel\",\"account\":\"motu\",\"changeDescriptions\":[{\"uuid\":\"00000000-0000-0000-0000-000000000001\",\"changes\":{\"variableName\":\"${Variable2}\",\"deleteOptions\":{\"mode\":\"emptyLiteral\"}}}]}";
      replaySimple(command, "testDeleteConfigurationVariableWithEmptyValue", null, true);
      
      assertThat(attribute1.getAttributeValue(), is("${Variable3}"));
      assertThat(attribute2.getAttributeValue(), is("${Variable1} and "));
      
      modelService.getConfigurationVariables(providerModel.getId());
         
      List<ModelVariable> variables = context.getVariables();
      
      assertThat(variables.size(), is(2));
      
      ModelVariable variable1 = context.getModelVariableByName("${Variable1}");
      ModelVariable variable2 = context.getModelVariableByName("${Variable2}");
      ModelVariable variable3 = context.getModelVariableByName("${Variable3}");
      
      assertThat(variable1, is(not(nullValue())));
      assertThat(variable2, is((nullValue())));
      assertThat(variable3, is(not(nullValue())));
      
      List<EObject> references1 = context.getReferences(variable1);
      List<EObject> references3 = context.getReferences(variable3);
      
      assertThat(references1.isEmpty(), is(false));
      assertThat(references3.isEmpty(), is(false));
      
      assertThat(references1.size(), is(1));
      assertThat(references3.size(), is(1));

      assertThat(variable1.getDefaultValue(), is("valueVariable1"));
      assertThat(variable3.getDefaultValue(), is("valueVariable3"));
      
   }
   
   @Test
   public void testRemoveReference() throws Exception
   {
      providerModel = modelService.findModel(PROVIDER_MODEL_ID);
      consumerModel = modelService.findModel(CONSUMER_MODEL_ID);

      InputStream requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/createConfigurationVariables.txt");
      InputStreamReader requestStream = new InputStreamReader(requestInput);
      replay(requestStream, "testCreateConfigurationVariables", false);
      
      
      VariableContextHelper variableContextHelper = modelService.currentSession().variableContextHelper();
      
      VariableContext context = variableContextHelper.getContext(providerModel);
      if (context == null)
      {
         variableContextHelper.createContext(providerModel);
         context = variableContextHelper.getContext(providerModel);  
      }
      
      JsonArray variablesJson = modelService.getConfigurationVariables(providerModel.getId());
      
      
      String command = "{\"commandId\":\"configVariable.update\",\"modelId\":\"ProviderModel\",\"account\":\"motu\",\"changeDescriptions\":[{\"uuid\":\"00000000-0000-0000-0000-000000000001\",\"changes\":{\"variableName\":\"${Variable2}\",\"defaultValue\":\"valueVariable2\"}}]}";
      replaySimple(command, "testUpdateConfigurationVariable", null, true);
      
      command = "{\"commandId\":\"configVariable.update\",\"modelId\":\"ProviderModel\",\"account\":\"motu\",\"changeDescriptions\":[{\"uuid\":\"00000000-0000-0000-0000-000000000001\",\"changes\":{\"variableName\":\"${Variable3}\",\"defaultValue\":\"valueVariable3\"}}]}";
      replaySimple(command, "testUpdateConfigurationVariable", null, true);  
      
      command = "{\"commandId\":\"configVariable.update\",\"modelId\":\"ProviderModel\",\"account\":\"motu\",\"changeDescriptions\":[{\"uuid\":\"00000000-0000-0000-0000-000000000001\",\"changes\":{\"variableName\":\"${Variable1}\",\"defaultValue\":\"valueVariable1\"}}]}";
      replaySimple(command, "testUpdateConfigurationVariable", null, true); 
      
      ModelVariable variable = context.getModelVariableByName("${Variable2}");
      
      AttributeType attribute1 = GenericModelingAssertions.assertConfigVariableAttributeReference(context, variable, 0, "${Variable2}${Variable3}");
      AttributeType attribute2 = GenericModelingAssertions.assertConfigVariableAttributeReference(context, variable, 1, "${Variable1} and ${Variable2}");
      
      command = "{\"commandId\":\"modelElement.update\",\"modelId\":\"ProviderModel\",\"account\":\"motu\",\"changeDescriptions\":[{\"uuid\":\"00000000-0000-0000-0000-000000000163\",\"changes\":{\"attributes\":{\"carnot:engine:defaultValue\":\"${Variable1} and No Variable\"}}}]}";
      replaySimple(command, "testRemoveReference", null, true);
      
      
      assertThat(attribute1.getAttributeValue(), is("${Variable2}${Variable3}"));
      assertThat(attribute2.getAttributeValue(), is("${Variable1} and No Variable"));
      
      modelService.getConfigurationVariables(providerModel.getId());
        
     
      List<ModelVariable> variables = context.getVariables();
      
      assertThat(variables.size(), is(3));
      
      ModelVariable variable1 = context.getModelVariableByName("${Variable1}");
      ModelVariable variable2 = context.getModelVariableByName("${Variable2}");
      ModelVariable variable3 = context.getModelVariableByName("${Variable3}");
      
      assertThat(variable1, is(not(nullValue())));
      assertThat(variable2, is(not(nullValue())));
      assertThat(variable3, is(not(nullValue())));
      
      List<EObject> references1 = context.getReferences(variable1);
      List<EObject> references2 = context.getReferences(variable2);
      List<EObject> references3 = context.getReferences(variable3);
      
      assertThat(references1.isEmpty(), is(false));
      assertThat(references2.isEmpty(), is(false));
      assertThat(references3.isEmpty(), is(false));
      
      assertThat(references1.size(), is(1));
      assertThat(references2.size(), is(1));
      assertThat(references3.size(), is(1));

      assertThat(variable1.getDefaultValue(), is("valueVariable1"));
      assertThat(variable2.getDefaultValue(), is("valueVariable2"));
      assertThat(variable3.getDefaultValue(), is("valueVariable3"));
      
   }
   

}
