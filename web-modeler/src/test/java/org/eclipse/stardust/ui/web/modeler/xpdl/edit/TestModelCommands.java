package org.eclipse.stardust.ui.web.modeler.xpdl.edit;

import static com.google.common.io.ByteStreams.toByteArray;
import static com.google.common.io.Closeables.closeQuietly;
import static java.lang.System.currentTimeMillis;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.InputStream;

import javax.annotation.Resource;

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

import com.google.gson.JsonObject;

import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.api.runtime.UserService;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.model.xpdl.carnot.util.ModelUtils;
import org.eclipse.stardust.ui.web.modeler.edit.ModelingSession;
import org.eclipse.stardust.ui.web.modeler.edit.ModelingSessionManager;
import org.eclipse.stardust.ui.web.modeler.marshaling.JsonMarshaller;
import org.eclipse.stardust.ui.web.modeler.model.conversion.BeanInvocationExecutor;
import org.eclipse.stardust.ui.web.modeler.model.conversion.RequestExecutor;
import org.eclipse.stardust.ui.web.modeler.service.DefaultModelManagementStrategy;
import org.eclipse.stardust.ui.web.modeler.service.ModelService;
import org.eclipse.stardust.ui.web.modeler.service.ModelerSessionController;
import org.eclipse.stardust.ui.web.modeler.utils.test.ChangeApiDriver;
import org.eclipse.stardust.ui.web.modeler.utils.test.MockServiceFactoryLocator;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"../../web-modeler-test-context.xml"})
@FixMethodOrder(MethodSorters.JVM)
public class TestModelCommands
{
   private static final String MODEL_ID = "ModelConversionReferenceModel";

   @Resource
   MockServiceFactoryLocator mockServiceFactoryLocator;

   @Resource
   JsonMarshaller jsonIo;

   @Resource
   ModelService modelService;

   @Resource
   private ModelerSessionController restController;

   @Resource
   private ModelingSessionManager modelingSessionManager;

   private ChangeApiDriver changeApiDriver;

   private ModelingSession mySession;

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

      final Document xpdlModel = Mockito.mock(Document.class);
      Mockito.when(xpdlModel.getId()).thenReturn(MODEL_ID);
      Mockito.when(xpdlModel.getName()).thenReturn(MODEL_ID + ".xpdl");

      Folder modelsFolder = Mockito.mock(Folder.class);
      Mockito.when(modelsFolder.getDocuments()).thenReturn(asList(xpdlModel));

      DocumentManagementService dmsService = mockServiceFactoryLocator.get()
            .getDocumentManagementService();

      Mockito.when(dmsService.getFolder(DefaultModelManagementStrategy.MODELS_DIR))
            .thenReturn(modelsFolder);
      Mockito.when(dmsService.retrieveDocumentContent(xpdlModel.getId())).thenAnswer(
            new Answer<byte[]>()
            {
               @Override
               public byte[] answer(InvocationOnMock invocation) throws Throwable
               {
                  InputStream isModel = getClass().getResourceAsStream(
                        "../../service/rest/" + xpdlModel.getName());
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
   }

   private void destroyMySession()
   {
      modelService.destroyModelingSession();
      this.mySession = null;
   }

   @Test
   public void testModelNameCanBeChanged() throws Exception
   {
      ModelType modelUnderTest = modelService.findModel(MODEL_ID);

      String newName = "name - " + currentTimeMillis();

      // preconditions
      assertThat(modelUnderTest, is(not(nullValue())));
      assertThat(modelUnderTest.getName(), is(not(newName)));

      JsonObject changeJson = new JsonObject();
      changeJson.addProperty(ModelerConstants.NAME_PROPERTY, newName);
      changeApiDriver.updateModelElement(MODEL_ID, changeJson);

      // assertions
      assertThat(modelUnderTest.getName(), is(newName));
   }

   @Test
   public void testModelDescriptionCanBeChanged() throws Exception
   {
      // TODO preconditions
      ModelType modelUnderTest = modelService.findModel(MODEL_ID);
      assertThat(modelUnderTest, is(not(nullValue())));
      assertThat(modelUnderTest.getDescription(), is(nullValue()));

      JsonObject changeJson = new JsonObject();
      String newDescription = "description - " + currentTimeMillis();
      changeJson.addProperty(ModelerConstants.DESCRIPTION_PROPERTY, newDescription);

      changeApiDriver.updateModelElement(MODEL_ID, changeJson);

      // TODO assertion

      assertThat(modelUnderTest.getDescription(), is(not(nullValue())));
      assertThat(ModelUtils.getDescriptionText(modelUnderTest.getDescription()),
            is(newDescription));
   }

}
