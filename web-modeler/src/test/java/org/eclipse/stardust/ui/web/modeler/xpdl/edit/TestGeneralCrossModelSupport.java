package org.eclipse.stardust.ui.web.modeler.xpdl.edit;

import static com.google.common.io.ByteStreams.toByteArray;
import static com.google.common.io.Closeables.closeQuietly;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.Resource;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.api.runtime.UserService;
import org.eclipse.stardust.model.xpdl.builder.common.EObjectUUIDMapper;
import org.eclipse.stardust.model.xpdl.builder.utils.XpdlModelIoUtils;
import org.eclipse.stardust.model.xpdl.carnot.ActivityType;
import org.eclipse.stardust.model.xpdl.carnot.CarnotWorkflowModelPackage;
import org.eclipse.stardust.model.xpdl.carnot.IIdentifiableElement;
import org.eclipse.stardust.model.xpdl.carnot.IdRef;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.model.xpdl.carnot.util.ModelUtils;
import org.eclipse.stardust.model.xpdl.xpdl2.ExternalPackage;
import org.eclipse.stardust.ui.web.modeler.edit.ModelingSession;
import org.eclipse.stardust.ui.web.modeler.edit.jto.CommandJto;
import org.eclipse.stardust.ui.web.modeler.edit.recording.ModelChangeRecorder;
import org.eclipse.stardust.ui.web.modeler.marshaling.JsonMarshaller;
import org.eclipse.stardust.ui.web.modeler.marshaling.ModelMarshaller;
import org.eclipse.stardust.ui.web.modeler.marshaling.ModelUnmarshaller;
import org.eclipse.stardust.ui.web.modeler.model.conversion.BeanInvocationExecutor;
import org.eclipse.stardust.ui.web.modeler.model.conversion.RequestExecutor;
import org.eclipse.stardust.ui.web.modeler.service.DefaultModelManagementStrategy;
import org.eclipse.stardust.ui.web.modeler.service.ModelService;
import org.eclipse.stardust.ui.web.modeler.service.ModelerSessionController;
import org.eclipse.stardust.ui.web.modeler.spi.ModelFormat;
import org.eclipse.stardust.ui.web.modeler.utils.test.ChangeApiDriver;
import org.eclipse.stardust.ui.web.modeler.utils.test.MockServiceFactoryLocator;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"../../web-modeler-test-context.xml"})
@FixMethodOrder(MethodSorters.JVM)
public class TestGeneralCrossModelSupport
{
   private static final String PROVIDER_MODEL_ID = "ProviderModel";

   private static final String CONSUMER_MODEL_ID = "ConsumerModel";

   private byte[] consumerModelModelBytes;

   @Resource
   MockServiceFactoryLocator mockServiceFactoryLocator;

   @Resource
   JsonMarshaller jsonIo;

   @Resource
   ModelService modelService;

   @Resource
   private ModelerSessionController restController;

   @Resource
   private ModelChangeRecorder modelChangeRecorder;

   @Resource
   @ModelFormat(ModelFormat.XPDL)
   private ModelMarshaller xpdlMarshaller;

   @Resource
   @ModelFormat(ModelFormat.XPDL)
   private ModelUnmarshaller xpdlUnmarshaller;

   private ChangeApiDriver changeApiDriver;

   private ModelingSession mySession;

   private ModelType providerModel;

   private ModelType consumerModel;

   protected String modelLocation = "../../service/rest/";

   @Before
   public void initServiceFactory()
   {
      assertThat(modelService, is(not(nullValue())));
      assertThat(jsonIo, is(not(nullValue())));
      assertThat(restController, is(not(nullValue())));

      RequestExecutor requestExecutor = new BeanInvocationExecutor(jsonIo, modelService,
            restController);
      this.changeApiDriver = new ChangeApiDriver(requestExecutor, jsonIo);

      mockServiceFactoryLocator.init();

      User mockUser = Mockito.mock(User.class);
      Mockito.when(mockUser.getAccount()).thenReturn("motu");

      UserService userService = mockServiceFactoryLocator.get().getUserService();
      Mockito.when(userService.getUser()).thenReturn(mockUser);

      final Document providerModel = Mockito.mock(Document.class);
      Mockito.when(providerModel.getId()).thenReturn(PROVIDER_MODEL_ID);
      Mockito.when(providerModel.getName()).thenReturn(PROVIDER_MODEL_ID + ".xpdl");

      final Document consumerModel = Mockito.mock(Document.class);
      Mockito.when(consumerModel.getId()).thenReturn(CONSUMER_MODEL_ID);
      Mockito.when(consumerModel.getName()).thenReturn(CONSUMER_MODEL_ID + ".xpdl");

      Folder modelsFolder = Mockito.mock(Folder.class);
      Mockito.when(modelsFolder.getDocuments()).thenReturn(
            asList(providerModel, consumerModel));

      DocumentManagementService dmsService = mockServiceFactoryLocator.get()
            .getDocumentManagementService();

      Mockito.when(dmsService.getFolder(DefaultModelManagementStrategy.MODELS_DIR))
            .thenReturn(modelsFolder);
      Mockito.when(dmsService.retrieveDocumentContent(providerModel.getId())).thenAnswer(
            new Answer<byte[]>()
            {
               @Override
               public byte[] answer(InvocationOnMock invocation) throws Throwable
               {
                  InputStream isModel = getClass().getResourceAsStream(
                        modelLocation + providerModel.getName());
                  try
                  {
                     return toByteArray(isModel);
                  }
                  finally
                  {
                     closeQuietly(isModel);
                  }
               }
            });

      Mockito.when(dmsService.retrieveDocumentContent(consumerModel.getId())).thenAnswer(
            new Answer<byte[]>()
            {
               @Override
               public byte[] answer(InvocationOnMock invocation) throws Throwable
               {
                  InputStream isModel = getClass().getResourceAsStream(
                        modelLocation + consumerModel.getName());
                  try
                  {
                     return toByteArray(isModel);
                  }
                  finally
                  {
                     closeQuietly(isModel);
                  }
               }
            });
   }

   @Before
   public void initModelingSession()
   {
      this.mySession = modelService.currentSession();
      assertThat(mySession, is(not(nullValue())));
   }

   @After
   public void cleanup()
   {
      destroyMySession();
      this.changeApiDriver = null;
      this.modelChangeRecorder = null;
   }

   private void destroyMySession()
   {
      modelService.destroyModelingSession();
      this.mySession = null;
      this.modelService = null;
   }

   @Test
   public void testCreationOfGeneralCrossModelEditing() throws Exception
   {
      providerModel = modelService.findModel(PROVIDER_MODEL_ID);
      consumerModel = modelService.findModel(CONSUMER_MODEL_ID);

      InputStream requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/generalCrossModelOperations.txt");
      InputStreamReader requestStream = new InputStreamReader(requestInput);

      replay(requestStream);

      assertReferencedData();
      assertReferencedParticipant();
      assertProcess();

      // saveReplayModel();
      // initUUIDMap();

   }

   @Test
   public void testRenameProvidedElements() throws Exception
   {

      providerModel = modelService.findModel(PROVIDER_MODEL_ID);
      consumerModel = modelService.findModel(CONSUMER_MODEL_ID);

      InputStream requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/generalCrossModelOperations.txt");
      InputStreamReader requestStream = new InputStreamReader(requestInput);

      replay(requestStream);

      requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/renameModelElementsOperations.txt");
      requestStream = new InputStreamReader(requestInput);
      replay(requestStream);

      assertRenamedData();
      assertRenamedParticipant();
      assertRenamedApplication();
      assertRenamedProcess();

      // saveReplayModel();

   }

   private void assertRenamedProcess()
   {
      // TODO Auto-generated method stub

   }

   private void assertRenamedApplication()
   {
      // TODO Auto-generated method stub

   }

   private void assertRenamedParticipant()
   {
      // TODO Auto-generated method stub

   }

   private void assertRenamedData()
   {
      // TODO Auto-generated method stub

   }

   private void assertProcess()
   {
      IIdentifiableElement processType = ModelUtils.findIdentifiableElement(
            consumerModel,
            CarnotWorkflowModelPackage.eINSTANCE.getModelType_ProcessDefinition(),
            "Process1");
      assertThat(processType, is(not(nullValue())));

      ActivityType activityType = (ActivityType) ModelUtils.findIdentifiableElement(
            processType,
            CarnotWorkflowModelPackage.eINSTANCE.getProcessDefinitionType_Activity(),
            "ProvidedApplication1");
      assertThat(activityType, is(not(nullValue())));

      IdRef externalReference = activityType.getExternalRef();
      assertThat(externalReference, is(not(nullValue())));

      ExternalPackage externalPackage = externalReference.getPackageRef();
      assertThat(externalPackage, is(not(nullValue())));

      assertThat(externalPackage.getHref(), is(not(nullValue())));
      assertThat(externalPackage.getId(), is(not(nullValue())));
      assertThat(externalPackage.getName(), is(not(nullValue())));

      assertThat(externalPackage.getName(), is("ProviderModel"));
      assertThat(externalPackage.getId(), is("ProviderModel"));
      assertThat(externalPackage.getHref(), is("ProviderModel"));

   }

   private void assertReferencedParticipant()
   {
      IIdentifiableElement roleType = ModelUtils.findIdentifiableElement(consumerModel,
            CarnotWorkflowModelPackage.eINSTANCE.getModelType_Role(), "ProvidedRole");
      assertThat(roleType, is(not(nullValue())));
      assertThat(roleType.eIsProxy(), is(true));
   }

   private void assertReferencedData()
   {
      IIdentifiableElement dataType = ModelUtils
            .findIdentifiableElement(consumerModel,
                  CarnotWorkflowModelPackage.eINSTANCE.getModelType_Data(),
                  "ProvidedPrimitive");
      assertThat(dataType, is(not(nullValue())));
      assertThat(dataType.eIsProxy(), is(true));
   }

   private void initUUIDMap()
   {
      modelService.getModelManagementStrategy().uuidMapper().empty();
      EObjectUUIDMapper eObjectUUIDMapper = modelService.getModelManagementStrategy()
            .uuidMapper();

      eObjectUUIDMapper.map(providerModel);
      for (Iterator<EObject> i = providerModel.eAllContents(); i.hasNext();)
      {
         eObjectUUIDMapper.map(i.next());
      }

      eObjectUUIDMapper.map(consumerModel);
      for (Iterator<EObject> i = consumerModel.eAllContents(); i.hasNext();)
      {
         eObjectUUIDMapper.map(i.next());
      }
   }

   private String[] replay(InputStreamReader requestStream) throws IOException
   {
      String line;
      String responseString = null;
      String expectedResponse = null;
      BufferedReader requestReader = new BufferedReader(requestStream);
      while ((line = requestReader.readLine()) != null)
      {
         String command = line.toString();
         CommandJto newJto = jsonIo.gson().fromJson(line.toString(), CommandJto.class);
         if (newJto != null)
         {
            newJto = jsonIo.gson().fromJson(command, CommandJto.class);
            changeApiDriver.performChange(newJto);
         }
      }

      return new String[] {responseString, expectedResponse};
   }

   private boolean sameJsons(String firstJsonString, String secondJsonString)
   {
      char[] firstCharArray = firstJsonString.toCharArray();
      char[] secondCharArray = secondJsonString.toCharArray();
      Arrays.sort(firstCharArray);
      Arrays.sort(secondCharArray);
      return Arrays.equals(firstCharArray, secondCharArray);
   }

   public TreeMap<String, Object> parse(TreeMap<String, Object> map, String json)
   {
      JsonParser parser = new com.google.gson.JsonParser();
      JsonElement object = (JsonElement) parser.parse(json);
      if (object.isJsonArray())
      {
         JsonArray jsonArray = (JsonArray) object;
         TreeMap<String, Object> arrayMap = new TreeMap<String, Object>();
         for (int i = 0; i < jsonArray.size(); i++)
         {
            JsonElement je = jsonArray.get(i);
            parse(arrayMap, je.toString());
         }
         return arrayMap;
      }
      if (object.isJsonObject())
      {
         JsonObject jsonObject = (JsonObject) object;
         Set<Map.Entry<String, JsonElement>> set = jsonObject.entrySet();
         Iterator<Map.Entry<String, JsonElement>> iterator = set.iterator();
         while (iterator.hasNext())
         {
            Map.Entry<String, JsonElement> entry = iterator.next();
            String key = entry.getKey();
            JsonElement value = entry.getValue();
            if (value.isJsonPrimitive())
            {
               map.put(key, value.toString());
            }
            else
            {
               TreeMap<String, Object> objectMap = new TreeMap<String, Object>();
               map.put(key, parse(objectMap, value.toString()));
            }
         }
      }
      return map;
   }

   private void saveReplayModel()
   {
      byte[] bytes = XpdlModelIoUtils.saveModel(providerModel);
      String xmlString = new String(bytes);
      try
      {
         PrintWriter out = new PrintWriter("C:/development/" + providerModel.getName()
               + ".xpdl");
         out.println(xmlString);
         out.flush();
      }
      catch (FileNotFoundException e)
      {
         e.printStackTrace();
      }

      bytes = XpdlModelIoUtils.saveModel(consumerModel);
      xmlString = new String(bytes);
      try
      {
         PrintWriter out = new PrintWriter("C:/development/" + consumerModel.getName()
               + ".xpdl");
         out.println(xmlString);
         out.flush();
      }
      catch (FileNotFoundException e)
      {
         e.printStackTrace();
      }
   }

}
