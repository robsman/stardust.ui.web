package org.eclipse.stardust.ui.web.modeler.xpdl.edit;

import static com.google.common.io.ByteStreams.toByteArray;
import static com.google.common.io.Closeables.closeQuietly;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.eclipse.emf.ecore.EObject;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ReflectionUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.model.xpdl.builder.common.EObjectUUIDMapper;
import org.eclipse.stardust.model.xpdl.builder.utils.XpdlModelIoUtils;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.ui.web.modeler.common.exception.ModelerException;
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

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"../../web-modeler-recording-test-context.xml"})
@FixMethodOrder(MethodSorters.JVM)
public class RecordingTestcase
{
   protected static final String PROVIDER_MODEL_ID = "ProviderModel";
   protected static final String PROVIDER_MODEL_ID2 = "ProviderModel2";

   protected static final String CONSUMER_MODEL_ID = "ConsumerModel";

   protected byte[] consumerModelModelBytes;

   @Resource
   MockServiceFactoryLocator mockServiceFactoryLocator;

   @Resource
   JsonMarshaller jsonIo;

   @Resource
   ModelService modelService;

   @Resource
   protected ModelerSessionController restController;

   @Resource
   protected ModelChangeRecorder modelChangeRecorder;

   @Resource
   @ModelFormat(ModelFormat.XPDL)
   protected ModelMarshaller xpdlMarshaller;

   @Resource
   @ModelFormat(ModelFormat.XPDL)
   protected ModelUnmarshaller xpdlUnmarshaller;

   protected ChangeApiDriver changeApiDriver;

   protected ModelingSession mySession;

   protected ModelType providerModel;
   protected ModelType providerModel2;

   protected ModelType consumerModel;

   protected String modelLocation = "../../service/rest/";

   protected List<TestResponse> testResponses = new ArrayList<TestResponse>();

   protected class TestResponse
   {
      private int responseNumber;

      private String commandID;

      private JsonArray added;

      private JsonArray modified;

      private JsonArray removed;

      public TestResponse(int count, JsonObject response)
      {
         JsonObject changes = response.get("changes").getAsJsonObject();
         commandID = response.get("commandId").getAsString();
         added = changes.get("added").getAsJsonArray();
         modified = changes.get("modified").getAsJsonArray();
         removed = changes.get("removed").getAsJsonArray();
         responseNumber = count;
      }

      public JsonArray getAdded()
      {
         return added;
      }

      public JsonArray getModified()
      {
         return modified;
      }

      public JsonArray getRemoved()
      {
         return removed;
      }

      public String getCommandID()
      {
         return commandID;
      }

      public int getResponseNumber()
      {
         return responseNumber;
      }


   };

   protected void initUUIDMap()
   {
      modelService.getModelManagementStrategy().uuidMapper().empty();
      EObjectUUIDMapper eObjectUUIDMapper = modelService.getModelManagementStrategy()
            .uuidMapper();

      eObjectUUIDMapper.map(providerModel);
      for (Iterator<EObject> i = providerModel.eAllContents(); i.hasNext();)
      {
         eObjectUUIDMapper.map(i.next());
      }
      if (includeProviderModel2())
      {
         eObjectUUIDMapper.map(providerModel2);
         for (Iterator<EObject> i = providerModel2.eAllContents(); i.hasNext();)
         {
            eObjectUUIDMapper.map(i.next());
         }
      }

      if (includeConsumerModel())
      {
         eObjectUUIDMapper.map(consumerModel);
         for (Iterator<EObject> i = consumerModel.eAllContents(); i.hasNext();)
         {
            eObjectUUIDMapper.map(i.next());
         }
      }
   }


   protected String[] replay(InputStreamReader requestStream, String testScenarioName, boolean performResponseCallback) throws IOException
   {
      System.out.println("Replay Commands for '" + this.getClass().getSimpleName() + "." + testScenarioName + "'");
      String line;
      String responseString = null;
      String expectedResponse = null;
      int responseNumber = 0;
      BufferedReader requestReader = new BufferedReader(requestStream);
      while ((line = requestReader.readLine()) != null)
      {
         String command = line.toString();
         System.out.println(" COMMAND : " + command);
         CommandJto newJto = jsonIo.gson().fromJson(line.toString(), CommandJto.class);
         if (newJto != null)
         {
            newJto = jsonIo.gson().fromJson(command, CommandJto.class);
            JsonObject response = null;
            try
            {
               response = changeApiDriver.performChange(newJto);
            }
            catch (ModelerException e)
            {
            }

            if (performResponseCallback && response != null)
            {
               TestResponse testResponse = new TestResponse(++responseNumber, response);
               Method method = ReflectionUtils.findMethod(this.getClass(),
                     testScenarioName + "Callback", new Class[] {TestResponse.class});
               if (method != null)
               {
                  try
                  {
                     method.invoke(this, new Object[] {testResponse});
                  }
                  catch (InvocationTargetException t)
                  {
                     System.out.println("Assertion of response " + responseNumber + " failed.");
                     throw new AssertionError(t.getTargetException());
                  }
                  catch (Throwable t)
                  {
                  }
               }
            }
         }
      }
      System.out.println("Replay finished.");
      return new String[] {responseString, expectedResponse};
   }

   protected List<TestResponse> getTestResponses()
   {
      return testResponses;
   }

   public void saveModel()
   {
      XpdlModelIoUtils.saveModel(providerModel);
      if (providerModel2 != null)
      {
         XpdlModelIoUtils.saveModel(providerModel2);
      }
      if (consumerModel != null)
      {
         XpdlModelIoUtils.saveModel(consumerModel);
      }
   }

   protected void saveReplayModel(String filePath)
   {
      byte[] bytes = XpdlModelIoUtils.saveModel(providerModel);
      String xmlString = new String(bytes);
      try
      {
         PrintWriter out = new PrintWriter(filePath + "/" + providerModel.getName()
               + ".xpdl");
         out.println(xmlString);
         out.flush();
         out.close();
      }
      catch (FileNotFoundException e)
      {
         e.printStackTrace();
      }

      if (providerModel2 != null)
      {
         bytes = XpdlModelIoUtils.saveModel(providerModel2);
         xmlString = new String(bytes);
         try
         {
            PrintWriter out = new PrintWriter(filePath + "/" + providerModel2.getName()
                  + ".xpdl");
            out.println(xmlString);
            out.flush();
            out.close();
         }
         catch (FileNotFoundException e)
         {
            e.printStackTrace();
         }
      }

      if (consumerModel != null)
      {
         bytes = XpdlModelIoUtils.saveModel(consumerModel);
         xmlString = new String(bytes);
         try
         {
            PrintWriter out = new PrintWriter(filePath + "/" + consumerModel.getName()
                  + ".xpdl");
            out.println(xmlString);
            out.flush();
            out.close();
         }
         catch (FileNotFoundException e)
         {
            e.printStackTrace();
         }
      }

   }

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

      final Document providerModel2 = Mockito.mock(Document.class);
      Mockito.when(providerModel2.getId()).thenReturn(PROVIDER_MODEL_ID2);
      Mockito.when(providerModel2.getName()).thenReturn(PROVIDER_MODEL_ID2 + ".xpdl");

      final Document consumerModel = Mockito.mock(Document.class);
      Mockito.when(consumerModel.getId()).thenReturn(CONSUMER_MODEL_ID);
      Mockito.when(consumerModel.getName()).thenReturn(CONSUMER_MODEL_ID + ".xpdl");

      Folder modelsFolder = Mockito.mock(Folder.class);

      if (includeConsumerModel())
      {
         if (includeProviderModel2())
         {
            Mockito.when(modelsFolder.getDocuments()).thenReturn(
                  asList(providerModel, providerModel2, consumerModel));
         }
         else
         {
            Mockito.when(modelsFolder.getDocuments()).thenReturn(
                  asList(providerModel, consumerModel));
         }
      }
      else
      {
         Mockito.when(modelsFolder.getDocuments()).thenReturn(asList(providerModel));

      }


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
      Mockito.when(dmsService.retrieveDocumentContent(providerModel2.getId())).thenAnswer(
            new Answer<byte[]>()
            {
               @Override
               public byte[] answer(InvocationOnMock invocation) throws Throwable
               {
                  InputStream isModel = getClass().getResourceAsStream(
                        modelLocation + providerModel2.getName());
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

   protected boolean includeConsumerModel()
   {
      return true;
   }

   protected boolean includeProviderModel2()
   {
      return false;
   }


}