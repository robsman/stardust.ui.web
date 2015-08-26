package org.eclipse.stardust.ui.web.modeler.xpdl.edit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import org.eclipse.stardust.model.xpdl.builder.utils.ExternalReferenceUtils;
import org.eclipse.stardust.model.xpdl.builder.utils.XpdlModelIoUtils;
import org.eclipse.stardust.model.xpdl.carnot.AccessPointType;
import org.eclipse.stardust.model.xpdl.carnot.ActivityImplementationType;
import org.eclipse.stardust.model.xpdl.carnot.ActivityType;
import org.eclipse.stardust.model.xpdl.carnot.ApplicationType;
import org.eclipse.stardust.model.xpdl.carnot.AttributeType;
import org.eclipse.stardust.model.xpdl.carnot.CarnotWorkflowModelPackage;
import org.eclipse.stardust.model.xpdl.carnot.ContextType;
import org.eclipse.stardust.model.xpdl.carnot.DataType;
import org.eclipse.stardust.model.xpdl.carnot.DirectionType;
import org.eclipse.stardust.model.xpdl.carnot.IAccessPointOwner;
import org.eclipse.stardust.model.xpdl.carnot.IExtensibleElement;
import org.eclipse.stardust.model.xpdl.carnot.IdRef;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.model.xpdl.carnot.ProcessDefinitionType;
import org.eclipse.stardust.model.xpdl.carnot.util.AttributeUtil;
import org.eclipse.stardust.model.xpdl.carnot.util.ModelUtils;
import org.eclipse.stardust.model.xpdl.xpdl2.DataTypeType;
import org.eclipse.stardust.model.xpdl.xpdl2.ExtendedAttributeType;
import org.eclipse.stardust.model.xpdl.xpdl2.ExternalPackage;
import org.eclipse.stardust.model.xpdl.xpdl2.ExternalReferenceType;
import org.eclipse.stardust.model.xpdl.xpdl2.FormalParameterType;
import org.eclipse.stardust.model.xpdl.xpdl2.ModeType;
import org.eclipse.stardust.model.xpdl.xpdl2.TypeDeclarationType;
import org.eclipse.stardust.model.xpdl.xpdl2.util.ExtendedAttributeUtil;
import org.eclipse.stardust.ui.web.modeler.utils.test.GenericModelingAssertions;

import org.junit.Test;

public class TestCrossModelSupport extends TestGeneralModeling
{
   @Test
   public void testDragAndDropFromProviderToConsumer() throws Exception
   {
      providerModel = modelService.findModel(PROVIDER_MODEL_ID);
      consumerModel = modelService.findModel(CONSUMER_MODEL_ID);

      testBasicModelElementsInProvider();
      initUUIDMap();

      InputStream requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/dragAndDropFromProviderToConsumer.txt");
      InputStreamReader requestStream = new InputStreamReader(requestInput);

      replay(requestStream, "testDragAndDropFromProviderToConsumer", false);

      ProcessDefinitionType process = GenericModelingAssertions.assertProcess(consumerModel, "ConsumerProcess", "ConsumerProcess");
      assertReferencedPrimitiveData(consumerModel, providerModel, "ProvidedPrimitive", "ProvidedPrimitive", "String");
      assertReferencedTypeDeclaration(consumerModel, providerModel, "ProvidedTypeDeclaration", "ProvidedTypeDeclaration", "struct", "ProvidedTypeDeclaration", "cnx://file/typeDeclaration/ProvidedTypeDeclaration");
      assertReferencedDocumentData(consumerModel, providerModel, "ProvidedDocument", "ProvidedDocument", "ProvidedTypeDeclaration", "cnx://file/typeDeclaration/ProvidedTypeDeclaration");
      ActivityType activity = GenericModelingAssertions.assertActivity(process, "ProvidedUIMashup", "ProvidedUIMashup", ActivityImplementationType.APPLICATION_LITERAL);
      assertReferencedApplication(consumerModel, providerModel, process, activity, "ProvidedUIMashup", "cnx://file/application/ProvidedUIMashup");
      activity = GenericModelingAssertions.assertActivity(process, "ProvidedProcess", "ProvidedProcess", ActivityImplementationType.SUBPROCESS_LITERAL);
      assertReferencedProcess(consumerModel, providerModel, process, activity, "ProvidedProcess", "cnx://file/processDefinition/ProvidedProcess");
      GenericModelingAssertions.assertReferencedRole(consumerModel, providerModel, "ProvidedRole", "ProvidedRole");

      //saveReplayModel("C:/development/");
   }

   @Test
   public void testCrossModelingByDropDown() throws Exception
   {
      providerModel = modelService.findModel(PROVIDER_MODEL_ID);
      consumerModel = modelService.findModel(CONSUMER_MODEL_ID);

      testDragAndDropFromProviderToConsumer();
      initUUIDMap();

      InputStream requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/crossModelingByDropDown.txt");
      InputStreamReader requestStream = new InputStreamReader(requestInput);
      replay(requestStream, "testCrossModelingByDropDown", false);

      ProcessDefinitionType process = GenericModelingAssertions.assertProcess(consumerModel, "ConsumerSubProcess", "ConsumerSubProcess");
      ActivityType activity = GenericModelingAssertions.assertActivity(process, "ConsumerUIMashup", "ConsumerUIMashup", ActivityImplementationType.APPLICATION_LITERAL);
      assertReferencedApplication(consumerModel, providerModel, process, activity, "ProvidedUIMashup", "cnx://file/application/ProvidedUIMashup");
      assertReferencedTypeDeclaration(consumerModel, providerModel, "ConsumerStructData", "ConsumerStructData", "struct", "ProvidedTypeDeclaration", "cnx://file/typeDeclaration/ProvidedTypeDeclaration");
      assertReferencedTypeDeclaration(consumerModel, providerModel, "ConsumerDocData", "ConsumerDocData", "dmsDocument", "ProvidedTypeDeclaration", "cnx://file/typeDeclaration/ProvidedTypeDeclaration");
      GenericModelingAssertions.assertDocumentData(consumerModel, "ConsumerDocData", "ConsumerDocData", "ProviderModel{ProvidedTypeDeclaration}");

      //saveReplayModel("C:/development/");
   }

   @Test
   public void testSwitchSubprocessFromRemoteToLocal() throws Exception
   {
      providerModel = modelService.findModel(PROVIDER_MODEL_ID);
      consumerModel = modelService.findModel(CONSUMER_MODEL_ID);

      testCrossModelingByDropDown();
      initUUIDMap();

      InputStream requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/switchSubprocessFromRemoteToLocal.txt");
      InputStreamReader requestStream = new InputStreamReader(requestInput);
      replay(requestStream, "testSwitchSubprocessFromRemoteToLocal", false);

      ProcessDefinitionType process = GenericModelingAssertions.assertProcess(consumerModel, "ConsumerProcess", "ConsumerProcess");
      ActivityType activity = GenericModelingAssertions.assertActivity(process, "ProvidedProcess", "ProvidedProcess", ActivityImplementationType.SUBPROCESS_LITERAL);
      GenericModelingAssertions.assertSubProcess(activity, "ConsumerSubProcess");

      //saveReplayModel("C:/development/");

   }



   @Test
   public void testSwitchSubprocessFromLocalToRemote() throws Exception
   {
      providerModel = modelService.findModel(PROVIDER_MODEL_ID);
      consumerModel = modelService.findModel(CONSUMER_MODEL_ID);

      testCrossModelingByDropDown();
      initUUIDMap();

      InputStream requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/switchSubprocessFromLocalToRemote.txt");
      InputStreamReader requestStream = new InputStreamReader(requestInput);
      replay(requestStream, "testSwitchSubprocessFromLocalToRemote", false);

      ProcessDefinitionType process = GenericModelingAssertions.assertProcess(consumerModel, "ConsumerProcess", "ConsumerProcess");
      ActivityType activity = GenericModelingAssertions.assertActivity(process, "ProvidedProcess", "ProvidedProcess", ActivityImplementationType.SUBPROCESS_LITERAL);
      assertReferencedProcess(consumerModel, providerModel, process, activity, "ProvidedProcess", "cnx://file/processDefinition/ProvidedProcess");

      // saveReplayModel("C:/development/");

   }

   @Test
   public void testProcessInterfaceUsesReferencedTypedeclarations() throws Exception
   {
      providerModel = modelService.findModel(PROVIDER_MODEL_ID);
      consumerModel = modelService.findModel(CONSUMER_MODEL_ID);

      testCrossModelingByDropDown();
      initUUIDMap();

      InputStream requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/processInterfaceUsesReferencedTypedeclarations.txt");
      InputStreamReader requestStream = new InputStreamReader(requestInput);
      replay(requestStream, "processInterfaceUsesReferencedTypedeclarations", false);

      ProcessDefinitionType process = GenericModelingAssertions.assertProcessInterface(consumerModel, "ConsumerSubProcess", "ConsumerSubProcess", 2);

      assertReferencedDocumentFormalParameter(process, "InDocument", "In Document", ModeType.IN, providerModel, "ProvidedTypeDeclaration");
      assertReferencedStructFormalParameter(process, "OutStruct", "Out Struct", ModeType.OUT, providerModel, "ProvidedTypeDeclaration");

      //saveReplayModel("C:/development/");
   }

   @Test
   public void testCrossModelingUIMashupParameter() throws Exception
   {
      providerModel = modelService.findModel(PROVIDER_MODEL_ID);
      consumerModel = modelService.findModel(CONSUMER_MODEL_ID);

      testCrossModelingByDropDown();
      initUUIDMap();

      ApplicationType application = GenericModelingAssertions.assertApplication(consumerModel, "ConsumerUIMashup");
      ContextType context = GenericModelingAssertions.assertApplicationContextType(application, "externalWebApp");

      AccessPointType accessPoint = assertReferencedStructAccessPoint(providerModel, context, "StructPArameterINAndOUT", "Struct PArameter IN And OUT", DirectionType.IN_LITERAL, "typeDeclaration:{ProviderModel}ProvidedTypeDeclaration");
      AttributeType attribute = AttributeUtil.getAttribute(accessPoint, "IS_INOUT_PARAM");
      assertThat(attribute, is(not(nullValue())));
      assertThat(attribute.getAttributeValue(), is("true"));

      assertReferencedStructAccessPoint(providerModel, context, "StructPArameterINAndOUT", "Struct PArameter IN And OUT", DirectionType.OUT_LITERAL, "typeDeclaration:{ProviderModel}ProvidedTypeDeclaration");
      attribute = AttributeUtil.getAttribute(accessPoint, "IS_INOUT_PARAM");
      assertThat(attribute, is(not(nullValue())));
      assertThat(attribute.getAttributeValue(), is("true"));


      //saveReplayModel("C:/development/");

   }

   @Test
   public void testCrossModelingRename() throws Exception
   {
      providerModel = modelService.findModel(PROVIDER_MODEL_ID);
      consumerModel = modelService.findModel(CONSUMER_MODEL_ID);

      testCrossModelingByDropDown();
      initUUIDMap();

      byte[] brokenModelXML = XpdlModelIoUtils.saveModel(consumerModel);

      InputStream requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/crossModelingRename.txt");
      InputStreamReader requestStream = new InputStreamReader(requestInput);
      replay(requestStream, "testCrossModelingRename", false);

      ModelType brokenModel = XpdlModelIoUtils.loadModel(brokenModelXML, modelService.getModelManagementStrategy());

      Map<String, ModelType> models = modelService.currentSession().modelManagementStrategy().getModels();
      ExternalReferenceUtils.fixExternalReferences(models, brokenModel);

      ProcessDefinitionType process = GenericModelingAssertions.assertProcess(brokenModel, "ConsumerProcess", "ConsumerProcess");
      assertReferencedPrimitiveData(brokenModel, providerModel, "RenamedProvidedPrimitive", "RenamedProvidedPrimitive", "String");
      assertReferencedTypeDeclaration(brokenModel, providerModel, "ProvidedTypeDeclaration", "ProvidedTypeDeclaration", "struct", "RenamedProvidedTypeDeclaration", "cnx://file/typeDeclaration/RenamedProvidedTypeDeclaration");
      assertReferencedDocumentData(brokenModel, providerModel, "RenamedProvidedDocument", "RenamedProvidedDocument", "RenamedProvidedTypeDeclaration", "cnx://file/typeDeclaration/RenamedProvidedTypeDeclaration");
      ActivityType activity = GenericModelingAssertions.assertActivity(process, "ProvidedUIMashup", "ProvidedUIMashup", ActivityImplementationType.APPLICATION_LITERAL);
      assertReferencedApplication(brokenModel, providerModel, process, activity, "RenamedProvidedUIMashup", "cnx://file/application/RenamedProvidedUIMashup");
      activity = GenericModelingAssertions.assertActivity(process, "ProvidedProcess", "ProvidedProcess", ActivityImplementationType.SUBPROCESS_LITERAL);
      assertReferencedProcess(brokenModel, providerModel, process, activity, "RenameProvidedProcess", "cnx://file/processDefinition/RenameProvidedProcess");
      GenericModelingAssertions.assertReferencedRole(brokenModel, providerModel, "RenamedProvidedRole", "RenamedProvidedRole");

      ApplicationType application = GenericModelingAssertions.assertApplication(consumerModel, "ConsumerUIMashup");
      ContextType context = GenericModelingAssertions.assertApplicationContextType(application, "externalWebApp");

      assertReferencedStructAccessPoint(providerModel, context, "StructPArameterINAndOUT", "Struct PArameter IN And OUT", DirectionType.IN_LITERAL, "typeDeclaration:{ProviderModel}RenamedProvidedTypeDeclaration");
      assertReferencedStructAccessPoint(providerModel, context, "StructPArameterINAndOUT", "Struct PArameter IN And OUT", DirectionType.OUT_LITERAL, "typeDeclaration:{ProviderModel}RenamedProvidedTypeDeclaration");

      consumerModel = brokenModel;

      //saveReplayModel("C:/development/");
   }

   @Test
   public void testCloneProcessSwitchSubprocessFromLocalToRemote() throws Exception
   {
      providerModel = modelService.findModel(PROVIDER_MODEL_ID);
      consumerModel = modelService.findModel(CONSUMER_MODEL_ID);

      testCrossModelingByDropDown();
      initUUIDMap();

      InputStream requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/switchSubprocessFromLocalToRemote.txt");
      InputStreamReader requestStream = new InputStreamReader(requestInput);
      replay(requestStream, "testSwitchSubprocessFromLocalToRemote", false);

      String command = "{\"commandId\":\"process.clone\",\"modelId\":\"ConsumerModel\",\"changeDescriptions\":[{\"oid\":\"ConsumerModel\",\"changes\":{\"id\":\"ConsumerProcess\"}}]}";

      replaySimple(command, "testCloneProcessDragAndDropFromProviderToConsumer", null, false);

      ProcessDefinitionType process = GenericModelingAssertions.assertProcess(consumerModel, "CLONE_ConsumerProcess", "CLONE - ConsumerProcess");
      ActivityType activity = GenericModelingAssertions.assertActivity(process, "ProvidedProcess", "ProvidedProcess", ActivityImplementationType.SUBPROCESS_LITERAL);
      assertReferencedProcess(consumerModel, providerModel, process, activity, "ProvidedProcess", "cnx://file/processDefinition/ProvidedProcess");

      // saveReplayModel("C:/development/");

   }

   @Test
   public void testCloneProcessSwitchSubprocessFromRemoteToLocal() throws Exception
   {
      providerModel = modelService.findModel(PROVIDER_MODEL_ID);
      consumerModel = modelService.findModel(CONSUMER_MODEL_ID);

      testCrossModelingByDropDown();
      initUUIDMap();

      InputStream requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/switchSubprocessFromRemoteToLocal.txt");
      InputStreamReader requestStream = new InputStreamReader(requestInput);
      replay(requestStream, "testSwitchSubprocessFromRemoteToLocal", false);

      String command = "{\"commandId\":\"process.clone\",\"modelId\":\"ConsumerModel\",\"changeDescriptions\":[{\"oid\":\"ConsumerModel\",\"changes\":{\"id\":\"ConsumerProcess\"}}]}";

      replaySimple(command, "testCloneProcessDragAndDropFromProviderToConsumer", null, false);

      ProcessDefinitionType process = GenericModelingAssertions.assertProcess(consumerModel, "CLONE_ConsumerProcess", "CLONE - ConsumerProcess");
      ActivityType activity = GenericModelingAssertions.assertActivity(process, "ProvidedProcess", "ProvidedProcess", ActivityImplementationType.SUBPROCESS_LITERAL);
      GenericModelingAssertions.assertSubProcess(activity, "ConsumerSubProcess");
   }

   @Test
   public void testCloneProcessCrossModelingByDropDown() throws Exception
   {
      providerModel = modelService.findModel(PROVIDER_MODEL_ID);
      consumerModel = modelService.findModel(CONSUMER_MODEL_ID);

      testDragAndDropFromProviderToConsumer();
      initUUIDMap();

      InputStream requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/crossModelingByDropDown.txt");
      InputStreamReader requestStream = new InputStreamReader(requestInput);
      replay(requestStream, "testCloneProcessCrossModelingByDropDown", false);

      String command = "{\"commandId\":\"process.clone\",\"modelId\":\"ConsumerModel\",\"changeDescriptions\":[{\"oid\":\"ConsumerModel\",\"changes\":{\"id\":\"ConsumerSubProcess\"}}]}";

      replaySimple(command, "testCloneProcessDragAndDropFromProviderToConsumer", null, false);

      ProcessDefinitionType process = GenericModelingAssertions.assertProcess(consumerModel, "CLONE_ConsumerSubProcess", "CLONE - ConsumerSubProcess");
      ActivityType activity = GenericModelingAssertions.assertActivity(process, "ConsumerUIMashup", "ConsumerUIMashup", ActivityImplementationType.APPLICATION_LITERAL);
      assertReferencedApplication(consumerModel, providerModel, process, activity, "ProvidedUIMashup", "cnx://file/application/ProvidedUIMashup");
      assertReferencedTypeDeclaration(consumerModel, providerModel, "ConsumerStructData", "ConsumerStructData", "struct", "ProvidedTypeDeclaration", "cnx://file/typeDeclaration/ProvidedTypeDeclaration");
      assertReferencedTypeDeclaration(consumerModel, providerModel, "ConsumerDocData", "ConsumerDocData", "dmsDocument", "ProvidedTypeDeclaration", "cnx://file/typeDeclaration/ProvidedTypeDeclaration");
      GenericModelingAssertions.assertDocumentData(consumerModel, "ConsumerDocData", "ConsumerDocData", "ProviderModel{ProvidedTypeDeclaration}");

      //saveReplayModel("C:/development/");
   }

   @Test
   public void testCloneProcessDragAndDropFromProviderToConsumer() throws Exception
   {
      providerModel = modelService.findModel(PROVIDER_MODEL_ID);
      consumerModel = modelService.findModel(CONSUMER_MODEL_ID);

      testBasicModelElementsInProvider();
      initUUIDMap();

      InputStream requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/dragAndDropFromProviderToConsumer.txt");
      InputStreamReader requestStream = new InputStreamReader(requestInput);

      replay(requestStream, "testCloneProcessDragAndDropFromProviderToConsumer", false);

      String command = "{\"commandId\":\"process.clone\",\"modelId\":\"ConsumerModel\",\"changeDescriptions\":[{\"oid\":\"ConsumerModel\",\"changes\":{\"id\":\"ConsumerProcess\"}}]}";

      replaySimple(command, "testCloneProcessDragAndDropFromProviderToConsumer", null, false);

      ProcessDefinitionType process = GenericModelingAssertions.assertProcess(consumerModel, "CLONE_ConsumerProcess", "CLONE - ConsumerProcess");
      assertReferencedPrimitiveData(consumerModel, providerModel, "ProvidedPrimitive", "ProvidedPrimitive", "String");
      assertReferencedTypeDeclaration(consumerModel, providerModel, "ProvidedTypeDeclaration", "ProvidedTypeDeclaration", "struct", "ProvidedTypeDeclaration", "cnx://file/typeDeclaration/ProvidedTypeDeclaration");
      assertReferencedDocumentData(consumerModel, providerModel, "ProvidedDocument", "ProvidedDocument", "ProvidedTypeDeclaration", "cnx://file/typeDeclaration/ProvidedTypeDeclaration");
      ActivityType activity = GenericModelingAssertions.assertActivity(process, "ProvidedUIMashup", "ProvidedUIMashup", ActivityImplementationType.APPLICATION_LITERAL);
      assertReferencedApplication(consumerModel, providerModel, process, activity, "ProvidedUIMashup", "cnx://file/application/ProvidedUIMashup");
      activity = GenericModelingAssertions.assertActivity(process, "ProvidedProcess", "ProvidedProcess", ActivityImplementationType.SUBPROCESS_LITERAL);
      assertReferencedProcess(consumerModel, providerModel, process, activity, "ProvidedProcess", "cnx://file/processDefinition/ProvidedProcess");
      GenericModelingAssertions.assertReferencedRole(consumerModel, providerModel, "ProvidedRole", "ProvidedRole");

      //saveReplayModel("C:/development/");


   }

   @Test
   public void testCloneProcessProcessInterfaceUsesReferencedTypedeclarations() throws Exception
   {
      providerModel = modelService.findModel(PROVIDER_MODEL_ID);
      consumerModel = modelService.findModel(CONSUMER_MODEL_ID);

      testCrossModelingByDropDown();
      initUUIDMap();

      InputStream requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/processInterfaceUsesReferencedTypedeclarations.txt");
      InputStreamReader requestStream = new InputStreamReader(requestInput);
      replay(requestStream, "processInterfaceUsesReferencedTypedeclarations", false);

      String command = "{\"commandId\":\"process.clone\",\"modelId\":\"ConsumerModel\",\"changeDescriptions\":[{\"oid\":\"ConsumerModel\",\"changes\":{\"id\":\"ConsumerSubProcess\"}}]}";

      replaySimple(command, "testCloneProcessDragAndDropFromProviderToConsumer", null, false);

      ProcessDefinitionType process = GenericModelingAssertions.assertProcessInterface(consumerModel, "CLONE_ConsumerSubProcess", "CLONE - ConsumerSubProcess", 2);

      assertReferencedDocumentFormalParameter(process, "InDocument", "In Document", ModeType.IN, providerModel, "ProvidedTypeDeclaration");
      assertReferencedStructFormalParameter(process, "OutStruct", "Out Struct", ModeType.OUT, providerModel, "ProvidedTypeDeclaration");

      //saveReplayModel("C:/development/");

   }

   public static void assertReferencedApplication(ModelType consumerModel, ModelType providerModel,  ProcessDefinitionType process, ActivityType activity, String refApplicationID, String uri)
   {
      ApplicationType application = GenericModelingAssertions.assertApplication(providerModel, refApplicationID);
      assertExternalReference(application, providerModel, refApplicationID, uri, activity);
   }

   public static void assertReferencedProcess(ModelType consumerModel, ModelType providerModel,  ProcessDefinitionType process, ActivityType activity, String refProcessID, String uri)
   {
      ProcessDefinitionType refProcess = GenericModelingAssertions.assertProcess(providerModel, refProcessID, refProcessID);
      assertExternalReference(refProcess, providerModel, refProcessID, uri, activity);
   }



   public static void assertReferencedPrimitiveData(ModelType consumerModel, ModelType providerModel, String dataID, String dataName,
         String primitiveType)
   {
      DataType data = GenericModelingAssertions.assertPrimitiveData(consumerModel,
            dataID, dataName, primitiveType);
      GenericModelingAssertions.assertProxyReference(providerModel, data);
   }

   public static DataType assertReferencedDocumentData(ModelType consumerModel, ModelType providerModel, String dataID, String dataName,
         String assignedDeclaration, String uri)
   {
      DataType data = GenericModelingAssertions.assertDocumentData(consumerModel, dataID, dataName, assignedDeclaration);
      GenericModelingAssertions.assertProxyReference(providerModel, data);
      return data;
   }

   public static AccessPointType assertReferencedStructAccessPoint(ModelType providerModel,
         IAccessPointOwner accessPointOwner, String accessPointID,
         String accessPointName, DirectionType direction, String type)
   {
      AccessPointType accessPoint = GenericModelingAssertions.assertAccessPoint(accessPointOwner, accessPointID, accessPointName, direction, "struct", type);
      String refDecl = type.substring(type.indexOf("}") + 1);
      AttributeType connectionUUIDAttribute = AttributeUtil.getAttribute(accessPoint, "carnot:connection:uuid");
      assertThat(connectionUUIDAttribute, is(not(nullValue())));
      String connectionUUID = connectionUUIDAttribute.getAttributeValue();
      TypeDeclarationType declaration = GenericModelingAssertions.assertTypeDeclaration(providerModel, refDecl, refDecl);
      assertThat(declaration, is(not(nullValue())));
      ExtendedAttributeType modelUUIDAttribute = ExtendedAttributeUtil.getAttribute(declaration, "carnot:model:uuid");
      assertThat(modelUUIDAttribute, is(not(nullValue())));
      String modelUUID = modelUUIDAttribute.getValue();
      assertThat(connectionUUID, is(modelUUID));
      return accessPoint;
   }

   public static DataTypeType assertReferencedDeclarationType(FormalParameterType parameter, String carnotTypeID, ModelType providerModel, String declarationID)
   {
      DataTypeType dataTypeType = parameter.getDataType();
      assertThat(dataTypeType, is(not(nullValue())));
      assertThat(dataTypeType.getCarnotType(), is(not(nullValue())));
      assertThat(dataTypeType.getCarnotType(), is(carnotTypeID));

      ExternalReferenceType externalReference = dataTypeType.getExternalReference();
      assertThat(externalReference.getLocation(), is(not(nullValue())));
      assertThat(externalReference.getLocation(), is(providerModel.getId()));
      assertThat(externalReference.getXref(), is(not(nullValue())));
      assertThat(externalReference.getXref(), is(declarationID));
      assertThat(externalReference.getUuid(), is(not(nullValue())));

      TypeDeclarationType typeDeclaration = GenericModelingAssertions.assertTypeDeclaration(providerModel, externalReference.getXref(), externalReference.getXref());
      String modelUUID = ExtendedAttributeUtil.getAttributeValue(typeDeclaration.getExtendedAttributes(),  "carnot:model:uuid");
      assertThat(externalReference.getUuid(), is(modelUUID));

      return dataTypeType;
   }

   public static FormalParameterType assertReferencedDocumentFormalParameter(ProcessDefinitionType process, String parameterID, String parameterName, ModeType modeType, ModelType providerModel, String declarationID)
   {
      FormalParameterType parameter = GenericModelingAssertions.assertFormalParameter(process, parameterID, parameterName, modeType);
      assertReferencedDeclarationType(parameter, "dmsDocument", providerModel, declarationID);
      return parameter;
   }

   public static FormalParameterType assertReferencedStructFormalParameter(ProcessDefinitionType process, String parameterID, String parameterName, ModeType modeType, ModelType providerModel, String declarationID)
   {
      FormalParameterType parameter = GenericModelingAssertions.assertFormalParameter(process, parameterID, parameterName, modeType);
      assertReferencedDeclarationType(parameter, "struct", providerModel, declarationID);
      return parameter;
   }

   public static DataType assertReferencedTypeDeclaration(ModelType consumerModel, ModelType providerModel, String dataID, String dataName, String dataTypeType,
         String assignedDeclaration, String uri)
   {
      DataType dataType = (DataType) ModelUtils.findIdentifiableElement(consumerModel,
            CarnotWorkflowModelPackage.eINSTANCE.getModelType_Data(), dataID);
      assertThat(dataType, is(not(nullValue())));
      assertThat(dataType.getType(), is(not(nullValue())));
      assertThat(dataType.getType().getId(), is(dataTypeType));
      AttributeType attribute = AttributeUtil.getAttribute(dataType,
            "carnot:connection:uri");
      assertThat(attribute, is(not(nullValue())));
      assertThat(attribute.getAttributeValue(), is(uri));
      assertThat(dataType.getName(), is(not(nullValue())));
      assertThat(dataType.getName(), is(dataName));

      ExternalReferenceType externalReference = dataType.getExternalReference();
      assertThat(externalReference.getLocation(), is(not(nullValue())));
      assertThat(externalReference.getLocation(), is(providerModel.getId()));
      assertThat(externalReference.getXref(), is(not(nullValue())));
      assertThat(externalReference.getXref(), is(assignedDeclaration));
      assertThat(externalReference.getUuid(), is(not(nullValue())));

      TypeDeclarationType typeDeclaration = GenericModelingAssertions.assertTypeDeclaration(providerModel, externalReference.getXref(), externalReference.getXref());
      String modelUUID = ExtendedAttributeUtil.getAttributeValue(typeDeclaration.getExtendedAttributes(),  "carnot:model:uuid");
      assertThat(externalReference.getUuid(), is(modelUUID));

      return dataType;
   }

   public static String assertExternalReference(IExtensibleElement element, ModelType providerModel,
         String applicationID, String uri, ActivityType activity)
   {
      IdRef externalReference = activity.getExternalRef();

      assertThat(externalReference, is(not(nullValue())));
      assertThat(externalReference.getRef(), is(not(nullValue())));
      assertThat(externalReference.getRef(), is(applicationID));

      ExternalPackage externalPackage = externalReference.getPackageRef();

      assertThat(externalPackage, is(not(nullValue())));

      assertThat(externalPackage.getHref(), is(not(nullValue())));
      assertThat(externalPackage.getId(), is(not(nullValue())));
      assertThat(externalPackage.getName(), is(not(nullValue())));

      assertThat(externalPackage.getName(), is(providerModel.getId()));
      assertThat(externalPackage.getId(), is(providerModel.getId()));
      assertThat(externalPackage.getHref(), is(providerModel.getId()));

      AttributeType attribute = AttributeUtil.getAttribute(activity,
            "carnot:connection:uri");
      assertThat(attribute, is(not(nullValue())));
      assertThat(attribute.getAttributeValue(), is(uri));

      //TODO: Extract this UUID code?

      attribute = AttributeUtil.getAttribute(element,
            "carnot:model:uuid");
      assertThat(attribute, is(not(nullValue())));
      String modelUUID = attribute.getAttributeValue();
      assertThat(modelUUID, is(not(nullValue())));

      attribute = AttributeUtil.getAttribute(activity,
            "carnot:connection:uuid");
      assertThat(attribute, is(not(nullValue())));
      String connectionUUID = attribute.getAttributeValue();
      assertThat(connectionUUID, is(not(nullValue())));

      assertThat(modelUUID, is(connectionUUID));

      return connectionUUID;
   }



}
