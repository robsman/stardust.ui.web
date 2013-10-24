package org.eclipse.stardust.ui.web.modeler.edit;

import static com.google.common.io.ByteStreams.toByteArray;
import static com.google.common.io.Closeables.closeQuietly;
import static java.util.Arrays.asList;
import static org.eclipse.stardust.ui.web.modeler.utils.test.TestUserIdProvider.testUser;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;

import java.io.InputStream;

import javax.annotation.Resource;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

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
import org.eclipse.stardust.ui.web.modeler.marshaling.JsonMarshaller;
import org.eclipse.stardust.ui.web.modeler.model.conversion.BeanInvocationExecutor;
import org.eclipse.stardust.ui.web.modeler.model.conversion.RequestExecutor;
import org.eclipse.stardust.ui.web.modeler.service.DefaultModelManagementStrategy;
import org.eclipse.stardust.ui.web.modeler.service.ModelService;
import org.eclipse.stardust.ui.web.modeler.service.rest.ModelerResource;
import org.eclipse.stardust.ui.web.modeler.service.rest.ModelerSessionRestController;
import org.eclipse.stardust.ui.web.modeler.service.rest.ModelerSessionRestController.ModelLockJto;
import org.eclipse.stardust.ui.web.modeler.utils.test.ChangeApiDriver;
import org.eclipse.stardust.ui.web.modeler.utils.test.MockServiceFactoryLocator;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"../web-modeler-test-context.xml"})
@FixMethodOrder(MethodSorters.JVM)
public class TestModelEditLocking
{
   private static final String MODEL_ID = "ModelConversionReferenceModel";

   private static final String OTHER_MODEL_ID = "OffsettingP1";

   @Resource
   MockServiceFactoryLocator mockServiceFactoryLocator;

   @Resource
   JsonMarshaller jsonIo;

   @Resource
   ModelerResource modelerRestController;

   @Resource
   ModelService modelService;

   @Resource
   private ModelerSessionRestController restController;

   @Resource
   private ModelingSessionManager modelingSessionManager;

   @Resource
   private ModelLockManager modelLockManager;

   private ChangeApiDriver changeApiDriver;

   private ModelingSession mySession;

   private ModelingSession otherSession;

   @Before
   public void initServiceFactory()
   {
      assertThat(modelService, is(not(nullValue())));
      assertThat(jsonIo, is(not(nullValue())));
      assertThat(restController, is(not(nullValue())));
      assertThat(modelLockManager, is(not(nullValue())));

      RequestExecutor requestExecutor = new BeanInvocationExecutor(jsonIo, modelService,
            restController);
      this.changeApiDriver = new ChangeApiDriver(requestExecutor, jsonIo);

      mockServiceFactoryLocator.init();

      User mockUser = Mockito.mock(User.class);
      Mockito.when(mockUser.getAccount()).thenReturn("motu");

      UserService userService = mockServiceFactoryLocator.get().getUserService();
      Mockito.when(userService.getUser()).thenReturn(mockUser);

      Document xpdlModel = Mockito.mock(Document.class);
      Mockito.when(xpdlModel.getId()).thenReturn(OTHER_MODEL_ID);
      Mockito.when(xpdlModel.getName()).thenReturn("OffsettingP1.xpdl");

      Document xpdlReferenceModel = Mockito.mock(Document.class);
      Mockito.when(xpdlReferenceModel.getId()).thenReturn(MODEL_ID);
      Mockito.when(xpdlReferenceModel.getName()).thenReturn("ModelConversionReferenceModel.xpdl");

      Folder modelsFolder = Mockito.mock(Folder.class);
      Mockito.when(modelsFolder.getDocuments()).thenReturn(asList(xpdlModel, xpdlReferenceModel));

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
                        "../service/rest/OffsettingP1.xpdl");
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
      Mockito.when(dmsService.retrieveDocumentContent(xpdlReferenceModel.getId())).thenAnswer(
            new Answer<byte[]>()
            {
               @Override
               public byte[] answer(InvocationOnMock invocation) throws Throwable
               {
                  InputStream isModel = getClass().getResourceAsStream(
                        "../service/rest/ModelConversionReferenceModel.xpdl");
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
      destroyOtherSession();
      destroyMySession();
   }

   private void createOtherSession()
   {
      assertThat(otherSession, is(nullValue()));
      this.otherSession = modelingSessionManager.getOrCreateSession(testUser("not-" + mySession.getOwnerId(), "Other User"));
      assertThat(otherSession, is(not(nullValue())));
   }

   private void destroyMySession()
   {
      modelService.destroyModelingSession();
      this.mySession = null;
   }

   private void destroyOtherSession()
   {
      if (null != otherSession)
      {
         assertThat(modelingSessionManager.getCurrentSession("not-" + mySession.getOwnerId()), is(otherSession));
         modelingSessionManager.destroySession(otherSession.getOwnerId());
         this.otherSession = null;
         assertThat(modelingSessionManager.getCurrentSession("not-" + mySession.getOwnerId()), is(nullValue()));
      }
   }

   @Test
   public void testModelGetsLockedUponChange() throws Exception
   {
      assertModelIsNotLocked(MODEL_ID, OTHER_MODEL_ID);

      JsonObject changeJson = new JsonObject();
      changeJson.addProperty(ModelerConstants.DESCRIPTION_PROPERTY, "description 1");

      changeApiDriver.updateModelElement(MODEL_ID, changeJson);

      assertModelIsLockedByMe(MODEL_ID);
      assertModelIsNotLocked(OTHER_MODEL_ID);
   }

   @Test
   public void testMultipleModelsGetLockedUponChange() throws Exception
   {
      assertModelIsNotLocked(MODEL_ID, OTHER_MODEL_ID);

      testModelGetsLockedUponChange();
      assertModelIsLockedByMe(MODEL_ID);
      assertModelIsNotLocked(OTHER_MODEL_ID);

      JsonObject changeJson = new JsonObject();
      changeJson.addProperty(ModelerConstants.DESCRIPTION_PROPERTY, "description 2");

      changeApiDriver.updateModelElement(OTHER_MODEL_ID, changeJson);

      assertModelIsLockedByMe(MODEL_ID, OTHER_MODEL_ID);
   }

   @Test
   public void testModelRemainsLockedUponSave() throws Exception
   {
      assertModelIsNotLocked(MODEL_ID, OTHER_MODEL_ID);

      testModelGetsLockedUponChange();
      assertModelIsLockedByMe(MODEL_ID);
      assertModelIsNotLocked(OTHER_MODEL_ID);

      modelService.saveModel(MODEL_ID);
      assertModelIsLockedByMe(MODEL_ID);
      assertModelIsNotLocked(OTHER_MODEL_ID);
   }

   @Test
   public void testMultipleModelsRemainLockedUponSave() throws Exception
   {
      assertModelIsNotLocked(MODEL_ID, OTHER_MODEL_ID);

      testMultipleModelsGetLockedUponChange();
      assertModelIsLockedByMe(MODEL_ID, OTHER_MODEL_ID);

      modelService.saveModel(MODEL_ID);
      modelService.saveModel(OTHER_MODEL_ID);
      assertModelIsLockedByMe(MODEL_ID, OTHER_MODEL_ID);
   }

   @Test
   public void testModelLockGetsReleasedUponSaveAll() throws Exception
   {
      assertModelIsNotLocked(MODEL_ID, OTHER_MODEL_ID);

      testModelGetsLockedUponChange();
      assertModelIsLockedByMe(MODEL_ID);
      assertModelIsNotLocked(OTHER_MODEL_ID);

      modelService.saveAllModels();
      assertModelIsNotLocked(MODEL_ID, OTHER_MODEL_ID);
   }

   @Test
   public void testMultipleModelLocksGetReleasedUponSaveAll() throws Exception
   {
      assertModelIsNotLocked(MODEL_ID, OTHER_MODEL_ID);

      testMultipleModelsGetLockedUponChange();
      assertModelIsLockedByMe(MODEL_ID, OTHER_MODEL_ID);

      modelService.saveAllModels();
      assertModelIsNotLocked(MODEL_ID, OTHER_MODEL_ID);
   }

   @Test
   public void testModelCanBeLockedAgainAfterSaveAll() throws Exception
   {
      assertModelIsNotLocked(MODEL_ID, OTHER_MODEL_ID);

      testModelLockGetsReleasedUponSaveAll();
      assertModelIsNotLocked(MODEL_ID, OTHER_MODEL_ID);

      testModelGetsLockedUponChange();
      assertModelIsLockedByMe(MODEL_ID);
      assertModelIsNotLocked(OTHER_MODEL_ID);
   }

   @Test
   public void testMultipleModelsCanBeLockedAgainAfterSaveAll() throws Exception
   {
      assertModelIsNotLocked(MODEL_ID, OTHER_MODEL_ID);

      testMultipleModelLocksGetReleasedUponSaveAll();
      assertModelIsNotLocked(MODEL_ID, OTHER_MODEL_ID);

      testMultipleModelsGetLockedUponChange();
      assertModelIsLockedByMe(MODEL_ID, OTHER_MODEL_ID);
   }

   @Test
   public void testModelLockGetsReleasedUponSessionDestroy() throws Exception
   {
      assertModelIsNotLocked(MODEL_ID, OTHER_MODEL_ID);

      testModelGetsLockedUponChange();
      assertModelIsLockedByMe(MODEL_ID);
      assertModelIsNotLocked(OTHER_MODEL_ID);

      destroyMySession();
      assertModelIsNotLocked(MODEL_ID, OTHER_MODEL_ID);
   }

   @Test
   public void testMultipleModelLocksGetReleasedUponSessionDestroy() throws Exception
   {
      assertModelIsNotLocked(MODEL_ID, OTHER_MODEL_ID);

      testMultipleModelsGetLockedUponChange();
      assertModelIsLockedByMe(MODEL_ID, OTHER_MODEL_ID);

      destroyMySession();
      assertModelIsNotLocked(MODEL_ID, OTHER_MODEL_ID);
   }

   @Test
   public void testModelLockedByMeCanBeChangedFurther() throws Exception
   {
      assertModelIsNotLocked(MODEL_ID, OTHER_MODEL_ID);

      testModelGetsLockedUponChange();

      assertModelIsLockedByMe(MODEL_ID);
      assertModelIsNotLocked(OTHER_MODEL_ID);

      JsonObject changeJson = new JsonObject();
      changeJson.addProperty(ModelerConstants.DESCRIPTION_PROPERTY, "description 2");
      changeApiDriver.updateModelElement(MODEL_ID, changeJson);

      assertModelIsLockedByMe(MODEL_ID);
      assertModelIsNotLocked(OTHER_MODEL_ID);
   }

   @Test
   public void testMultipleModelsLockedByMeCanBeChangedFurther() throws Exception
   {
      assertModelIsNotLocked(MODEL_ID, OTHER_MODEL_ID);

      testMultipleModelsGetLockedUponChange();

      assertModelIsLockedByMe(MODEL_ID, OTHER_MODEL_ID);

      JsonObject changeJson = new JsonObject();
      changeJson.addProperty(ModelerConstants.DESCRIPTION_PROPERTY, "description 2");

      changeApiDriver.updateModelElement(MODEL_ID, changeJson);

      assertModelIsLockedByMe(MODEL_ID, OTHER_MODEL_ID);

      changeApiDriver.updateModelElement(OTHER_MODEL_ID, changeJson);

      assertModelIsLockedByMe(MODEL_ID, OTHER_MODEL_ID);
   }

   @Test
   public void testModelLockCanBeBrokenByMyself() throws Exception
   {
      assertModelIsNotLocked(MODEL_ID, OTHER_MODEL_ID);

      testModelGetsLockedUponChange();

      assertModelIsLockedByMe(MODEL_ID);
      assertModelIsNotLocked(OTHER_MODEL_ID);

      restController.breakEditLockForModel(MODEL_ID);

      assertModelIsNotLocked(MODEL_ID, OTHER_MODEL_ID);
   }

   @Test
   public void testModelLockCanBeBrokenByAnAdministrator() throws Exception
   {
      assertModelIsNotLocked(MODEL_ID, OTHER_MODEL_ID);

      testModelGetsLockedUponChange();

      assertModelIsLockedByMe(MODEL_ID);
      assertModelIsNotLocked(OTHER_MODEL_ID);

      // forcibly break model lock
      ModelingSession adminSession = modelingSessionManager
            .getOrCreateSession(testUser("admin"));
      adminSession.setSessionAttribute(ModelingSession.SUPERUSER, true);
      modelLockManager.breakEditLock(adminSession, MODEL_ID);

      assertModelIsNotLocked(MODEL_ID, OTHER_MODEL_ID);
   }

   @Test
   public void testBrokenModelLockPreventsFurtherChange() throws Exception
   {
      assertModelIsNotLocked(MODEL_ID, OTHER_MODEL_ID);

      testModelLockCanBeBrokenByMyself();

      assertModelIsNotLocked(MODEL_ID, OTHER_MODEL_ID);

      try
      {
         JsonObject changeJson = new JsonObject();
         changeJson.addProperty(ModelerConstants.DESCRIPTION_PROPERTY, "description 2");
         changeApiDriver.updateModelElement(MODEL_ID, changeJson);

         assertThat("Expected an exception.", true, is(false));
      }
      catch (WebApplicationException wae)
      {
         assertThat(wae.getResponse().getStatus(), is(Status.CONFLICT.getStatusCode()));
         assertThat(wae.getResponse().getEntity(), is(instanceOf(String.class)));
         assertThat((String) wae.getResponse().getEntity(), startsWith("Missing write permission"));
      }
   }

   @Test
   public void testBrokenModelLockPreventsSave() throws Exception
   {
      assertModelIsNotLocked(MODEL_ID, OTHER_MODEL_ID);

      testModelLockCanBeBrokenByMyself();

      assertModelIsNotLocked(MODEL_ID, OTHER_MODEL_ID);

      // saveModel via service
      try
      {
         modelService.saveModel(MODEL_ID);

         assertThat("Expected an exception.", true, is(false));
      }
      catch (MissingWritePermissionException mwpe)
      {
         assertThat(mwpe.getMessage(), startsWith("Failed"));
      }

      // saveModel via REST facade
      Response response = modelerRestController.saveModel(MODEL_ID);

      assertThat(response.getStatus(), is(Status.CONFLICT.getStatusCode()));
      assertThat(response.getEntity(), is(instanceOf(String.class)));
      assertThat((String) response.getEntity(), startsWith("Missing write permission"));
   }

   @Test
   public void testBrokenModelLockPreventsSaveAll() throws Exception
   {
      assertModelIsNotLocked(MODEL_ID, OTHER_MODEL_ID);

      testModelLockCanBeBrokenByMyself();

      assertModelIsNotLocked(MODEL_ID, OTHER_MODEL_ID);

      // saveAll via service
      try
      {
         modelService.saveAllModels();

         assertThat("Expected an exception.", true, is(false));
      }
      catch (MissingWritePermissionException mwpe)
      {
         assertThat(mwpe.getMessage(), startsWith("Failed"));
      }

      // saveAll via REST facade
      Response response = modelerRestController.saveAllModels();

      assertThat(response.getStatus(), is(Status.CONFLICT.getStatusCode()));
      assertThat(response.getEntity(), is(instanceOf(String.class)));
      assertThat((String) response.getEntity(), startsWith("Missing write permission"));
   }

   @Test
   public void testModelLockedByOtherCantBeChanged() throws Exception
   {
      createOtherSession();

      assertModelIsNotLocked(MODEL_ID, OTHER_MODEL_ID);

      modelLockManager.lockForEditing(otherSession, MODEL_ID);

      assertModelIsLockedByOther(MODEL_ID);
      assertModelIsNotLocked(OTHER_MODEL_ID);

      try
      {
         JsonObject changeJson = new JsonObject();
         changeJson.addProperty(ModelerConstants.DESCRIPTION_PROPERTY, "description 1");
         changeApiDriver.updateModelElement(MODEL_ID, changeJson);

         assertThat("Expected an exception.", true, is(false));
      }
      catch (WebApplicationException wae)
      {
         assertThat(wae.getResponse().getStatus(), is(Status.CONFLICT.getStatusCode()));
         assertThat(wae.getResponse().getEntity(), is(instanceOf(String.class)));
         assertThat((String) wae.getResponse().getEntity(), startsWith("Missing write permission"));
      }

      assertModelIsLockedByOther(MODEL_ID);
      assertModelIsNotLocked(OTHER_MODEL_ID);
   }

   @Test
   public void testModelLockedByOtherPreventsSaveByMe() throws Exception
   {
      createOtherSession();

      assertModelIsNotLocked(MODEL_ID, OTHER_MODEL_ID);

      modelLockManager.lockForEditing(otherSession, MODEL_ID);

      assertModelIsLockedByOther(MODEL_ID);
      assertModelIsNotLocked(OTHER_MODEL_ID);

      // saveModel via service
      try
      {
         modelService.saveModel(MODEL_ID);

         assertThat("Expected an exception.", true, is(false));
      }
      catch (MissingWritePermissionException mwpe)
      {
         assertThat(mwpe.getMessage(), startsWith("Failed"));
      }

      // saveModel via REST facade
      Response response = modelerRestController.saveModel(MODEL_ID);

      assertThat(response.getStatus(), is(Status.CONFLICT.getStatusCode()));
      assertThat(response.getEntity(), is(instanceOf(String.class)));
      assertThat((String) response.getEntity(), startsWith("Missing write permission"));

      assertModelIsLockedByOther(MODEL_ID);
      assertModelIsNotLocked(OTHER_MODEL_ID);
   }

   @Test
   public void testModelLockedByOtherPreventsSaveAllByMe() throws Exception
   {
      createOtherSession();

      assertModelIsNotLocked(MODEL_ID, OTHER_MODEL_ID);

      modelLockManager.lockForEditing(otherSession, MODEL_ID);

      assertModelIsLockedByOther(MODEL_ID);
      assertModelIsNotLocked(OTHER_MODEL_ID);

      // saveModel via service
      try
      {
         modelService.saveAllModels();

         assertThat("Expected an exception.", true, is(false));
      }
      catch (MissingWritePermissionException mwpe)
      {
         assertThat(mwpe.getMessage(), startsWith("Failed"));
      }

      // saveModel via REST facade
      Response response = modelerRestController.saveAllModels();

      assertThat(response.getStatus(), is(Status.CONFLICT.getStatusCode()));
      assertThat(response.getEntity(), is(instanceOf(String.class)));
      assertThat((String) response.getEntity(), startsWith("Missing write permission"));

      assertModelIsLockedByOther(MODEL_ID);
      assertModelIsNotLocked(OTHER_MODEL_ID);
   }

   @Test
   public void testModelGetsLockedUponChangeDespiteOtherModelBeingLockedByOther() throws Exception
   {
      createOtherSession();
      assertModelIsNotLocked(MODEL_ID, OTHER_MODEL_ID);

      modelLockManager.lockForEditing(otherSession, OTHER_MODEL_ID);

      assertModelIsNotLocked(MODEL_ID);
      assertModelIsLockedByOther(OTHER_MODEL_ID);

      JsonObject changeJson = new JsonObject();
      changeJson.addProperty(ModelerConstants.DESCRIPTION_PROPERTY, "description 1");

      changeApiDriver.updateModelElement(MODEL_ID, changeJson);

      assertModelIsLockedByMe(MODEL_ID);
      assertModelIsLockedByOther(OTHER_MODEL_ID);
   }

   @Test
   public void testModelLockedByMeRemainsLockedDespiteOtherSessionTerminates() throws Exception
   {
      createOtherSession();
      assertModelIsNotLocked(MODEL_ID, OTHER_MODEL_ID);

      modelLockManager.lockForEditing(otherSession, OTHER_MODEL_ID);

      assertModelIsNotLocked(MODEL_ID);
      assertModelIsLockedByOther(OTHER_MODEL_ID);

      JsonObject changeJson = new JsonObject();
      changeJson.addProperty(ModelerConstants.DESCRIPTION_PROPERTY, "description 1");

      changeApiDriver.updateModelElement(MODEL_ID, changeJson);

      assertModelIsLockedByMe(MODEL_ID);
      assertModelIsLockedByOther(OTHER_MODEL_ID);

      destroyOtherSession();

      assertModelIsLockedByMe(MODEL_ID);
      assertModelIsNotLocked(OTHER_MODEL_ID);
   }

   private void assertModelIsNotLocked(String... modelIds)
   {
      for (String modelId : modelIds)
      {
         assertThat("Model must not have an edit lock", modelLockManager.getEditLockInfo(modelId), is(nullValue()));

         if (null != mySession)
         {
            assertThat(modelLockManager.isLockedByMe(mySession, modelId), is(false));
            assertThat(modelLockManager.isLockedByOther(mySession, modelId), is(false));
         }

         if (null != otherSession)
         {
            assertThat(modelLockManager.isLockedByMe(otherSession, modelId), is(false));
            assertThat(modelLockManager.isLockedByOther(otherSession, modelId), is(false));
         }


         ModelLockJto lockJto = jsonIo.gson().fromJson(
               restController.getEditLockStatus(modelId),
               ModelerSessionRestController.ModelLockJto.class);
         assertThat(lockJto, is(not(nullValue())));
         assertThat(lockJto.modelId, is(modelId));
         assertThat(lockJto.lockStatus, is(""));
      }
   }

   private void assertModelIsLockedByMe(String... modelIds)
   {
      for (String modelId : modelIds)
      {
         assertThat(modelLockManager.getEditLockInfo(modelId), is(not(nullValue())));

         assertThat(mySession, is(not(nullValue())));
         assertThat(modelLockManager.isLockedByMe(mySession, modelId), is(true));
         assertThat(modelLockManager.isLockedByOther(mySession, modelId), is(false));

         if (null != otherSession)
         {
            assertThat(modelLockManager.isLockedByMe(otherSession, modelId), is(false));
            assertThat(modelLockManager.isLockedByOther(otherSession, modelId), is(true));
         }

         ModelLockJto lockJto = jsonIo.gson().fromJson(
               restController.getEditLockStatus(modelId),
               ModelerSessionRestController.ModelLockJto.class);
         assertThat(lockJto, is(not(nullValue())));
         assertThat(lockJto.modelId, is(modelId));
         assertThat(lockJto.lockStatus, is("lockedByMe"));
      }
   }

   private void assertModelIsLockedByOther(String... modelIds)
   {
      for (String modelId : modelIds)
      {
         assertThat(modelLockManager.getEditLockInfo(modelId), is(not(nullValue())));

         assertThat(otherSession, is(not(nullValue())));
         assertThat(modelLockManager.isLockedByMe(otherSession, modelId), is(true));
         assertThat(modelLockManager.isLockedByOther(otherSession, modelId), is(false));

         if (null != mySession)
         {
            assertThat(modelLockManager.isLockedByMe(mySession, modelId), is(false));
            assertThat(modelLockManager.isLockedByOther(mySession, modelId), is(true));
         }

         ModelLockJto lockJto = jsonIo.gson().fromJson(
               restController.getEditLockStatus(modelId),
               ModelerSessionRestController.ModelLockJto.class);
         assertThat(lockJto, is(not(nullValue())));
         assertThat(lockJto.modelId, is(modelId));
         assertThat(lockJto.lockStatus, is("lockedByOther"));
      }
   }
}
