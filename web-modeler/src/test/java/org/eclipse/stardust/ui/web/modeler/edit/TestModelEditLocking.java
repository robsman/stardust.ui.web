package org.eclipse.stardust.ui.web.modeler.edit;

import static com.google.common.io.ByteStreams.toByteArray;
import static com.google.common.io.Closeables.closeQuietly;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;

import java.io.InputStream;

import javax.annotation.Resource;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.emf.ecore.EObject;
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
import org.eclipse.stardust.model.xpdl.builder.strategy.ModelManagementStrategy;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.ui.web.modeler.marshaling.JsonMarshaller;
import org.eclipse.stardust.ui.web.modeler.model.conversion.RequestExecutor;
import org.eclipse.stardust.ui.web.modeler.rest.RestFacadeInvocationExecutor;
import org.eclipse.stardust.ui.web.modeler.service.DefaultModelManagementStrategy;
import org.eclipse.stardust.ui.web.modeler.service.ModelService;
import org.eclipse.stardust.ui.web.modeler.service.ModelerSessionController;
import org.eclipse.stardust.ui.web.modeler.service.ModelerSessionController.ModelLockJto;
import org.eclipse.stardust.ui.web.modeler.service.rest.ModelerResource;
import org.eclipse.stardust.ui.web.modeler.service.rest.ModelerSessionRestController;
import org.eclipse.stardust.ui.web.modeler.utils.test.ChangeApiDriver;
import org.eclipse.stardust.ui.web.modeler.utils.test.MockServiceFactoryLocator;
import org.eclipse.stardust.ui.web.modeler.utils.test.TestUserIdProvider;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"../web-modeler-test-context.xml"})
@FixMethodOrder(MethodSorters.JVM)
public class TestModelEditLocking
{
   private static final String MODEL_ID = "ModelConversionReferenceModel";

   private static final String OTHER_MODEL_ID = "OffsettingP1";

   @Resource
   private TestUserIdProvider testUserIdProvider;

   @Resource(name = "otherRegularTestUserIdProvider")
   private TestUserIdProvider otherRegularTestUser;

   @Resource(name = "adminTestUserIdProvider")
   private TestUserIdProvider adminTestUser;

   @Resource(name = "partition2TestUserIdProvider")
   private TestUserIdProvider partition2TestUser;

   @Resource(name = "sflPartition1")
   MockServiceFactoryLocator mockServiceFactoryLocator;

   @Resource(name = "sflPartition2")
   MockServiceFactoryLocator sflPartition2;

   @Resource
   JsonMarshaller jsonIo;

   @Resource
   ModelerResource modelerRestController;

   @Resource
   ModelService modelService;

   @Resource
   private ModelerSessionController restController;

   @Resource
   private ModelerSessionRestController modelerSessionRestController;

   @Resource
   private ModelingSessionManager modelingSessionManager;

   @Resource
   private ModelLockManager modelLockManager;

   private ChangeApiDriver changeApiDriver;

   private ModelingSession mySession;

   private ModelingSession otherSession;

   private ModelingSession adminSession;

   @Resource(name = "partition2ModelManagementStrategy")
   private ModelManagementStrategy partition2ModelManagementStrategy;

   private ModelingSession partition2Session;

   @Before
   public void initServiceFactory()
   {
      assertThat(modelService, is(not(nullValue())));
      assertThat(jsonIo, is(not(nullValue())));
      assertThat(restController, is(not(nullValue())));
      assertThat(modelLockManager, is(not(nullValue())));

      RequestExecutor requestExecutor = new RestFacadeInvocationExecutor(jsonIo, modelService,
            modelerSessionRestController);
      this.changeApiDriver = new ChangeApiDriver(requestExecutor, jsonIo);

      initializeServiceFactory(mockServiceFactoryLocator, "partition1");
      initializeServiceFactory(sflPartition2, "partition2");
   }

   private void initializeServiceFactory(MockServiceFactoryLocator mockServiceFactoryLocator,
         String partitionId)
   {
      mockServiceFactoryLocator.init();

      User mockUser = Mockito.mock(User.class);
      Mockito.when(mockUser.getAccount()).thenReturn("motu");
      Mockito.when(mockUser.getPartitionId()).thenReturn(partitionId);

      UserService userService = mockServiceFactoryLocator.get().getUserService();
      Mockito.when(userService.getUser()).thenReturn(mockUser);

      final Document xpdlModel = Mockito.mock(Document.class);
      Mockito.when(xpdlModel.getId()).thenReturn(OTHER_MODEL_ID);
      Mockito.when(xpdlModel.getName()).thenReturn("OffsettingP1.xpdl");

      final Document xpdlReferenceModel = Mockito.mock(Document.class);
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
                        "../service/rest/" + xpdlModel.getName());
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
                        "../service/rest/" + xpdlReferenceModel.getName());
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
      destroyPartition2Session();

      destroyOtherSession();
      destroyMySession();
   }

   private void createOtherSession()
   {
      assertThat(otherSession, is(nullValue()));

      testUserIdProvider.setOverride(otherRegularTestUser);
      try
      {
         this.otherSession = modelingSessionManager.getOrCreateSession(otherRegularTestUser);
         assertThat(otherSession, is(not(nullValue())));
      }
      finally
      {
         testUserIdProvider.setOverride(null);
      }
   }

   private void createAdminSession()
   {
      assertThat(adminSession, is(nullValue()));

      testUserIdProvider.setOverride(adminTestUser);
      try
      {
         this.adminSession = modelingSessionManager.getOrCreateSession(adminTestUser);
         assertThat(adminSession, is(not(nullValue())));
      }
      finally
      {
         testUserIdProvider.setOverride(null);
      }
   }

   private void createPartition2Session()
   {
      assertThat(partition2Session, is(nullValue()));

      testUserIdProvider.setOverride(partition2TestUser);
      try
      {
         this.partition2Session = modelingSessionManager
               .getOrCreateSession(partition2TestUser);
         assertThat(partition2Session, is(not(nullValue())));

         partition2Session.setModelManagementStrategy(partition2ModelManagementStrategy);
      }
      finally
      {
         testUserIdProvider.setOverride(null);
      }
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
         assertThat(modelingSessionManager.getCurrentSession(otherRegularTestUser
               .getLoginName()), is(otherSession));
         modelingSessionManager.destroySession(otherSession.getOwnerId());
         this.otherSession = null;
         assertThat(modelingSessionManager.getCurrentSession(otherRegularTestUser
               .getLoginName()), is(nullValue()));
      }
   }

   private void destroyPartition2Session()
   {
      if (null != partition2Session)
      {
         assertThat(modelingSessionManager.getCurrentSession(partition2TestUser
               .getLoginName()), is(partition2Session));
         modelingSessionManager.destroySession(partition2Session.getOwnerId());
         this.partition2Session = null;
         assertThat(modelingSessionManager.getCurrentSession(partition2TestUser
               .getLoginName()), is(nullValue()));
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
   public void testModelGetsLockedWithPartitionScope() throws Exception
   {
      assertModelIsNotLocked(MODEL_ID, OTHER_MODEL_ID);

      testModelGetsLockedUponChange();

      assertModelIsLockedByMe(MODEL_ID);
      assertModelIsNotLocked(OTHER_MODEL_ID);

      createPartition2Session();

      assertThat(modelLockManager.isLockedByMe(partition2Session, MODEL_ID), is(false));
      assertThat(modelLockManager.isLockedByOther(partition2Session, MODEL_ID), is(false));

      // load and lock model (already locked in partition1) also in partition2
      EObject p2Model = partition2Session.modelRepository().findModel(MODEL_ID);
      assertThat(partition2Session.getEditSession(p2Model), is(notNullValue()));

      assertThat(modelLockManager.isLockedByMe(partition2Session, MODEL_ID), is(true));
      assertThat(modelLockManager.isLockedByOther(partition2Session, MODEL_ID), is(false));
      assertThat(modelLockManager.isLockedByMe(partition2Session, OTHER_MODEL_ID), is(false));
      assertThat(modelLockManager.isLockedByOther(partition2Session, OTHER_MODEL_ID), is(false));

      assertThat(partition2Session.canSaveModel(MODEL_ID), is(true));

      // load and lock other model (not locked in partition 1) in partition2
      EObject p2OtherModel = partition2Session.modelRepository().findModel(OTHER_MODEL_ID);
      assertThat(partition2Session.getEditSession(p2OtherModel), is(notNullValue()));

      assertThat(modelLockManager.isLockedByMe(partition2Session, MODEL_ID), is(true));
      assertThat(modelLockManager.isLockedByOther(partition2Session, MODEL_ID), is(false));
      assertThat(modelLockManager.isLockedByMe(partition2Session, OTHER_MODEL_ID), is(true));
      assertThat(modelLockManager.isLockedByOther(partition2Session, OTHER_MODEL_ID), is(false));

      assertThat(partition2Session.canSaveModel(MODEL_ID), is(true));
      assertThat(partition2Session.canSaveModel(OTHER_MODEL_ID), is(true));

      // break partition2's edit lock for model
      assertThat(partition2Session.breakEditLock(p2Model), is(true));

      assertThat(modelLockManager.isLockedByMe(partition2Session, MODEL_ID), is(false));
      assertThat(modelLockManager.isLockedByOther(partition2Session, MODEL_ID), is(false));
      assertThat(modelLockManager.isLockedByMe(partition2Session, OTHER_MODEL_ID), is(true));
      assertThat(modelLockManager.isLockedByOther(partition2Session, OTHER_MODEL_ID), is(false));

      assertThat(partition2Session.canSaveModel(MODEL_ID), is(false));
      assertThat(partition2Session.canSaveModel(OTHER_MODEL_ID), is(true));

      // ensure partition1 sessions remained unaffected
      assertModelIsLockedByMe(MODEL_ID);
      assertModelIsNotLocked(OTHER_MODEL_ID);
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
      createAdminSession();
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
   public void testOtherModelLockedByOtherDoesNotPreventSaveAllByMe() throws Exception
   {
      createOtherSession();

      assertModelIsNotLocked(MODEL_ID, OTHER_MODEL_ID);

      modelLockManager.lockForEditing(otherSession, OTHER_MODEL_ID);

      assertModelIsNotLocked(MODEL_ID);
      assertModelIsLockedByOther(OTHER_MODEL_ID);

      // saveModel via REST facade
      Response response = modelerRestController.saveAllModels();

      assertThat(response.getStatus(), is(Status.OK.getStatusCode()));

      assertModelIsNotLocked(MODEL_ID);
      assertModelIsLockedByOther(OTHER_MODEL_ID);
   }

   @Test
   public void testOtherModelLockedByOtherDoesNotPreventSaveAllOfModelsChangedByMe() throws Exception
   {
      assertModelIsNotLocked(MODEL_ID, OTHER_MODEL_ID);

      testModelGetsLockedUponChange();

      assertModelIsLockedByMe(MODEL_ID);
      assertModelIsNotLocked(OTHER_MODEL_ID);

      createOtherSession();
      modelLockManager.lockForEditing(otherSession, OTHER_MODEL_ID);

      assertModelIsLockedByMe(MODEL_ID);
      assertModelIsLockedByOther(OTHER_MODEL_ID);

      // saveModel via REST facade
      Response response = modelerRestController.saveAllModels();

      assertThat(response.getStatus(), is(Status.OK.getStatusCode()));

      assertModelIsNotLocked(MODEL_ID);
      assertModelIsLockedByOther(OTHER_MODEL_ID);
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
         if (null != mySession)
         {
            assertThat("Model must not have an edit lock",
                  modelLockManager.getEditLockInfo(mySession, modelId), is(nullValue()));

            assertThat(modelLockManager.isLockedByMe(mySession, modelId), is(false));
            assertThat(modelLockManager.isLockedByOther(mySession, modelId), is(false));
         }

         if (null != otherSession)
         {
            assertThat("Model must not have an edit lock",
                  modelLockManager.getEditLockInfo(otherSession, modelId), is(nullValue()));

            assertThat(modelLockManager.isLockedByMe(otherSession, modelId), is(false));
            assertThat(modelLockManager.isLockedByOther(otherSession, modelId), is(false));
         }


         ModelLockJto lockJto = restController.getEditLockStatus(modelId);
         assertThat(lockJto, is(not(nullValue())));
         assertThat(lockJto.modelId, is(modelId));
         assertThat(lockJto.lockStatus, is(""));
      }
   }

   private void assertModelIsLockedByMe(String... modelIds)
   {
      for (String modelId : modelIds)
      {
         assertThat(mySession, is(not(nullValue())));

         assertThat(modelLockManager.getEditLockInfo(mySession, modelId),
               is(not(nullValue())));

         assertThat(modelLockManager.isLockedByMe(mySession, modelId), is(true));
         assertThat(modelLockManager.isLockedByOther(mySession, modelId), is(false));

         if (null != otherSession)
         {
            assertThat(modelLockManager.isLockedByMe(otherSession, modelId), is(false));
            assertThat(modelLockManager.isLockedByOther(otherSession, modelId), is(true));
         }

         ModelLockJto lockJto = restController.getEditLockStatus(modelId);
         assertThat(lockJto, is(not(nullValue())));
         assertThat(lockJto.modelId, is(modelId));
         assertThat(lockJto.lockStatus, is("lockedByMe"));
      }
   }

   private void assertModelIsLockedByOther(String... modelIds)
   {
      for (String modelId : modelIds)
      {
         assertThat(otherSession, is(not(nullValue())));

         assertThat(modelLockManager.getEditLockInfo(otherSession, modelId),
               is(not(nullValue())));

         assertThat(modelLockManager.isLockedByMe(otherSession, modelId), is(true));
         assertThat(modelLockManager.isLockedByOther(otherSession, modelId), is(false));

         if (null != mySession)
         {
            assertThat(modelLockManager.isLockedByMe(mySession, modelId), is(false));
            assertThat(modelLockManager.isLockedByOther(mySession, modelId), is(true));
         }

         ModelLockJto lockJto = restController.getEditLockStatus(modelId);
         assertThat(lockJto, is(not(nullValue())));
         assertThat(lockJto.modelId, is(modelId));
         assertThat(lockJto.lockStatus, is("lockedByOther"));
      }
   }
}
