package org.eclipse.stardust.ui.web.modeler.xpdl.validation;

import static com.google.common.io.ByteStreams.toByteArray;
import static com.google.common.io.Closeables.closeQuietly;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.InputStream;

import javax.annotation.Resource;

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

import com.google.gson.JsonArray;

import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.api.runtime.UserService;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.ui.web.modeler.edit.ModelingSession;
import org.eclipse.stardust.ui.web.modeler.service.DefaultModelManagementStrategy;
import org.eclipse.stardust.ui.web.modeler.service.ModelService;
import org.eclipse.stardust.ui.web.modeler.service.rest.ModelerSessionRestController;
import org.eclipse.stardust.ui.web.modeler.spi.ModelBinding;
import org.eclipse.stardust.ui.web.modeler.utils.test.MockServiceFactoryLocator;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"../../web-modeler-test-context.xml"})
@FixMethodOrder(MethodSorters.JVM)
public class TestModelValidation
{
   private static final String MODEL_ID = "ModelConversionReferenceModel";

   private static final String OTHER_MODEL_ID = "OffsettingP1";

   @Resource(name = "sflPartition1")
   MockServiceFactoryLocator mockServiceFactoryLocator;

   @Resource
   ModelService modelService;

   @Resource
   private ModelerSessionRestController modelerSessionRestController;

   private ModelingSession mySession;

   @Before
   public void initServiceFactory()
   {
      assertThat(modelService, is(not(nullValue())));

      initializeServiceFactory(mockServiceFactoryLocator, "partition1");
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
      Mockito.when(dmsService.retrieveDocumentContent(xpdlReferenceModel.getId())).thenAnswer(
            new Answer<byte[]>()
            {
               @Override
               public byte[] answer(InvocationOnMock invocation) throws Throwable
               {
                  InputStream isModel = getClass().getResourceAsStream(
                        "../../service/rest/" + xpdlReferenceModel.getName());
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
   public void testXpdlModelCanBeValidated() throws Exception
   {
      EObject model = mySession.modelRepository().findModel(MODEL_ID);

      assertThat(model, is(notNullValue()));
      assertThat(model, is(instanceOf(ModelType.class)));

      ModelBinding<EObject> modelBinding = mySession.modelRepository().getModelBinding(model);

      JsonArray validationInfo = modelBinding.validateModel(model);
      assertThat(validationInfo, is(notNullValue()));
   }

}
