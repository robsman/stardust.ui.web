package org.eclipse.stardust.ui.web.modeler.xpdl.edit;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Proxy;
import java.util.Map;

import org.eclipse.stardust.model.xpdl.builder.connectionhandler.EObjectProxyHandler;
import org.eclipse.stardust.model.xpdl.builder.utils.XpdlModelIoUtils;
import org.eclipse.stardust.model.xpdl.carnot.ActivityImplementationType;
import org.eclipse.stardust.model.xpdl.carnot.ActivityType;
import org.eclipse.stardust.model.xpdl.carnot.ApplicationType;
import org.eclipse.stardust.model.xpdl.carnot.AttributeType;
import org.eclipse.stardust.model.xpdl.carnot.CarnotWorkflowModelPackage;
import org.eclipse.stardust.model.xpdl.carnot.DataType;
import org.eclipse.stardust.model.xpdl.carnot.IExtensibleElement;
import org.eclipse.stardust.model.xpdl.carnot.IIdentifiableElement;
import org.eclipse.stardust.model.xpdl.carnot.IdRef;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.model.xpdl.carnot.ProcessDefinitionType;
import org.eclipse.stardust.model.xpdl.carnot.RoleType;
import org.eclipse.stardust.model.xpdl.carnot.util.AttributeUtil;
import org.eclipse.stardust.model.xpdl.carnot.util.ModelUtils;
import org.eclipse.stardust.model.xpdl.xpdl2.ExternalPackage;
import org.eclipse.stardust.model.xpdl.xpdl2.ExternalReferenceType;
import org.eclipse.stardust.model.xpdl.xpdl2.TypeDeclarationType;
import org.eclipse.stardust.model.xpdl.xpdl2.util.ExtendedAttributeUtil;
import org.eclipse.stardust.ui.web.modeler.utils.test.GenericModelingAssertions;
import org.eclipse.stardust.ui.web.modeler.xpdl.edit.utils.ExternalReferenceUtils;
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

      replay(requestStream);

      ProcessDefinitionType process = GenericModelingAssertions.assertProcess(consumerModel, "ConsumerProcess");
      assertReferencedPrimitiveData(consumerModel, providerModel, "ProvidedPrimitive", "ProvidedPrimitive", "String");
      assertReferencedTypeDeclaration(consumerModel, providerModel, "ProvidedTypeDeclaration", "ProvidedTypeDeclaration", "struct", "ProvidedTypeDeclaration", "cnx://file/typeDeclaration/ProvidedTypeDeclaration");
      assertReferencedDocumentData(consumerModel, providerModel, "ProvidedDocument", "ProvidedDocument", "ProvidedTypeDeclaration", "cnx://file/typeDeclaration/ProvidedTypeDeclaration");
      ActivityType activity = GenericModelingAssertions.assertActivity(process, "ProvidedUIMashup", "ProvidedUIMashup", ActivityImplementationType.APPLICATION_LITERAL);
      assertReferencedApplication(consumerModel, providerModel, process, activity, "ProvidedUIMashup", "cnx://file/application/ProvidedUIMashup");
      activity = GenericModelingAssertions.assertActivity(process, "ProvidedProcess", "ProvidedProcess", ActivityImplementationType.SUBPROCESS_LITERAL);
      assertReferencedProcess(consumerModel, providerModel, process, activity, "ProvidedProcess", "cnx://file/processDefinition/ProvidedProcess");
      assertReferencedRole(consumerModel, providerModel, "ProvidedRole", "ProvidedRole");

      // saveReplayModel("C:/development/");
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
      replay(requestStream);

      ProcessDefinitionType process = GenericModelingAssertions.assertProcess(consumerModel, "ConsumerSubProcess");
      ActivityType activity = GenericModelingAssertions.assertActivity(process, "ConsumerUIMashup", "ConsumerUIMashup", ActivityImplementationType.APPLICATION_LITERAL);
      assertReferencedApplication(consumerModel, providerModel, process, activity, "ProvidedUIMashup", "cnx://file/application/ProvidedUIMashup");
      assertReferencedTypeDeclaration(consumerModel, providerModel, "ConsumerStructData", "ConsumerStructData", "struct", "ProvidedTypeDeclaration", "cnx://file/typeDeclaration/ProvidedTypeDeclaration");
      assertReferencedTypeDeclaration(consumerModel, providerModel, "ConsumerDocData", "ConsumerDocData", "dmsDocument", "ProvidedTypeDeclaration", "cnx://file/typeDeclaration/ProvidedTypeDeclaration");
      GenericModelingAssertions.assertDocumentData(consumerModel, "ConsumerDocData", "ConsumerDocData", "ProviderModel{ProvidedTypeDeclaration}");

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
      replay(requestStream);

      ModelType brokenModel = XpdlModelIoUtils.loadModel(brokenModelXML, modelService.getModelManagementStrategy());

      Map<String, ModelType> models = modelService.currentSession().modelManagementStrategy().getModels();
      ExternalReferenceUtils.fixExternalReferences(models, brokenModel);

      ProcessDefinitionType process = GenericModelingAssertions.assertProcess(brokenModel, "ConsumerProcess");
      assertReferencedPrimitiveData(brokenModel, providerModel, "RenamedProvidedPrimitive", "RenamedProvidedPrimitive", "String");
      assertReferencedTypeDeclaration(brokenModel, providerModel, "ProvidedTypeDeclaration", "ProvidedTypeDeclaration", "struct", "RenamedProvidedTypeDeclaration", "cnx://file/typeDeclaration/RenamedProvidedTypeDeclaration");
      assertReferencedDocumentData(brokenModel, providerModel, "RenamedProvidedDocument", "RenamedProvidedDocument", "RenamedProvidedTypeDeclaration", "cnx://file/typeDeclaration/RenamedProvidedTypeDeclaration");
      ActivityType activity = GenericModelingAssertions.assertActivity(process, "ProvidedUIMashup", "ProvidedUIMashup", ActivityImplementationType.APPLICATION_LITERAL);
      assertReferencedApplication(brokenModel, providerModel, process, activity, "RenamedProvidedUIMashup", "cnx://file/application/RenamedProvidedUIMashup");
      activity = GenericModelingAssertions.assertActivity(process, "ProvidedProcess", "ProvidedProcess", ActivityImplementationType.SUBPROCESS_LITERAL);
      assertReferencedProcess(brokenModel, providerModel, process, activity, "RenameProvidedProcess", "cnx://file/processDefinition/RenameProvidedProcess");
      assertReferencedRole(brokenModel, providerModel, "RenamedProvidedRole", "RenamedProvidedRole");

      consumerModel = brokenModel;

      //saveReplayModel("C:/development/");
   }

   public static void assertReferencedApplication(ModelType consumerModel, ModelType providerModel,  ProcessDefinitionType process, ActivityType activity, String refApplicationID, String uri)
   {
      ApplicationType application = GenericModelingAssertions.assertApplication(providerModel, refApplicationID);
      assertExternalReference(application, providerModel, refApplicationID, uri, activity);
   }

   public static void assertReferencedProcess(ModelType consumerModel, ModelType providerModel,  ProcessDefinitionType process, ActivityType activity, String refProcessID, String uri)
   {
      ProcessDefinitionType refProcess = GenericModelingAssertions.assertProcess(providerModel, refProcessID);
      assertExternalReference(refProcess, providerModel, refProcessID, uri, activity);
   }

   public static void assertReferencedPrimitiveData(ModelType consumerModel, ModelType providerModel, String dataID, String dataName,
         String primitiveType)
   {
      DataType data = GenericModelingAssertions.assertPrimitiveData(consumerModel,
            dataID, dataName, primitiveType);
      assertProxyReference(providerModel, data);
   }

   public static void assertReferencedRole(ModelType consumerModel, ModelType providerModel, String roleID, String roleName)
   {
      RoleType role = GenericModelingAssertions.assertRole(consumerModel, roleID, roleName);
      assertProxyReference(providerModel, role);
   }

   public static DataType assertReferencedDocumentData(ModelType consumerModel, ModelType providerModel, String dataID, String dataName,
         String assignedDeclaration, String uri)
   {
      DataType data = GenericModelingAssertions.assertDocumentData(consumerModel, dataID, dataName, assignedDeclaration);
      assertProxyReference(providerModel, data);
      return data;
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

   public static void assertProxyReference(ModelType providerModel, IIdentifiableElement data)
   {
      assertThat(data.eIsProxy(), is(true));
      assertThat(Proxy.getInvocationHandler(data), is(not(nullValue())));
      assertThat(Proxy.getInvocationHandler(data), instanceOf(EObjectProxyHandler.class));
      EObjectProxyHandler handler = (EObjectProxyHandler) Proxy
            .getInvocationHandler(data);
      assertThat(handler.getSelf(), is(not(nullValue())));
      assertThat(handler.getSelf(), instanceOf(IIdentifiableElement.class));
      assertThat(handler.getTarget(), is(not(nullValue())));
      assertThat(handler.getTarget(), instanceOf(IIdentifiableElement.class));
      IIdentifiableElement self = (IIdentifiableElement) handler.getSelf();
      IIdentifiableElement target = (IIdentifiableElement) handler.getTarget();
      assertThat((ModelType) target.eContainer(), is(equalTo(providerModel)));
      assertThat(self.getId(), is(target.getId()));
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
