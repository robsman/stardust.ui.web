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
import java.util.Iterator;

import javax.annotation.Resource;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.api.runtime.UserService;
import org.eclipse.stardust.model.xpdl.builder.common.EObjectUUIDMapper;
import org.eclipse.stardust.model.xpdl.builder.utils.XpdlModelIoUtils;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
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
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"../../web-modeler-recording-test-context.xml"})
@FixMethodOrder(MethodSorters.JVM)
public class RecordingTestcase
{
   protected static final String PROVIDER_MODEL_ID = "ProviderModel";

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

   protected ModelType consumerModel;

   protected String modelLocation = "../../service/rest/";

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

      if (includeConsumerModel())
      {
         eObjectUUIDMapper.map(consumerModel);
         for (Iterator<EObject> i = consumerModel.eAllContents(); i.hasNext();)
         {
            eObjectUUIDMapper.map(i.next());
         }
      }
   }


   protected String[] replay(InputStreamReader requestStream, String testScenarioName) throws IOException
   {
      System.out.println("Replay Commands for '" + this.getClass().getSimpleName() + "." + testScenarioName + "'");
      String line;
      String responseString = null;
      String expectedResponse = null;
      BufferedReader requestReader = new BufferedReader(requestStream);
      while ((line = requestReader.readLine()) != null)
      {
         String command = line.toString();
         System.out.println(" COMMAND : " + command);
         CommandJto newJto = jsonIo.gson().fromJson(line.toString(), CommandJto.class);
         if (newJto != null)
         {
            newJto = jsonIo.gson().fromJson(command, CommandJto.class);
            Object o = changeApiDriver.performChange(newJto);
         }
      }
      System.out.println("Replay finished.");
      return new String[] {responseString, expectedResponse};
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
      }
      catch (FileNotFoundException e)
      {
         e.printStackTrace();
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

      final Document consumerModel = Mockito.mock(Document.class);
      Mockito.when(consumerModel.getId()).thenReturn(CONSUMER_MODEL_ID);
      Mockito.when(consumerModel.getName()).thenReturn(CONSUMER_MODEL_ID + ".xpdl");

      Folder modelsFolder = Mockito.mock(Folder.class);

      if (includeConsumerModel())
      {
         Mockito.when(modelsFolder.getDocuments()).thenReturn(
               asList(providerModel, consumerModel));
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

}
