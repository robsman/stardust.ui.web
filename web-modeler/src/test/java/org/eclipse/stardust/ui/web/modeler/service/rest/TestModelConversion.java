package org.eclipse.stardust.ui.web.modeler.service.rest;

import static com.google.common.io.ByteStreams.toByteArray;
import static com.google.common.io.Closeables.closeQuietly;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.InputStream;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.gson.JsonArray;
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
import org.eclipse.stardust.ui.web.modeler.service.ModelerSessionController;
import org.eclipse.stardust.ui.web.modeler.utils.test.MockServiceFactoryLocator;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"../../web-modeler-test-context.xml"})
public class TestModelConversion
{
   @Resource
   MockServiceFactoryLocator mockServiceFactoryLocator;

   @Resource
   JsonMarshaller jsonIo;

   @Resource
   ModelService modelService;

   @Resource
   private ModelerSessionController restController;

   @Before
   public void initServiceFactory()
   {
      mockServiceFactoryLocator.init();

      User mockUser = Mockito.mock(User.class);
      Mockito.when(mockUser.getAccount()).thenReturn("motu");

      UserService userService = mockServiceFactoryLocator.get().getUserService();
      Mockito.when(userService.getUser()).thenReturn(mockUser);

      Document bpmn2Model = Mockito.mock(Document.class);
      Mockito.when(bpmn2Model.getId()).thenReturn("StatementP1");
      Mockito.when(bpmn2Model.getName()).thenReturn("StatementP1.bpmn");

      Document xpdlModel = Mockito.mock(Document.class);
      Mockito.when(xpdlModel.getId()).thenReturn("OffsettingP1");
      Mockito.when(xpdlModel.getName()).thenReturn("OffsettingP1.xpdl");

      Document xpdlReferenceModel = Mockito.mock(Document.class);
      Mockito.when(xpdlReferenceModel.getId()).thenReturn("ModelConversionReferenceModel");
      Mockito.when(xpdlReferenceModel.getName()).thenReturn("ModelConversionReferenceModel.xpdl");

      Folder modelsFolder = Mockito.mock(Folder.class);
      Mockito.when(modelsFolder.getDocuments()).thenReturn(asList(bpmn2Model, xpdlModel, xpdlReferenceModel));

      DocumentManagementService dmsService = mockServiceFactoryLocator.get()
            .getDocumentManagementService();

      Mockito.when(dmsService.getFolder(DefaultModelManagementStrategy.MODELS_DIR))
            .thenReturn(modelsFolder);
      Mockito.when(dmsService.retrieveDocumentContent(bpmn2Model.getId())).thenAnswer(
            new Answer<byte[]>()
            {
               @Override
               public byte[] answer(InvocationOnMock invocation) throws Throwable
               {
                  InputStream isModel = getClass().getResourceAsStream(
                        "StatementP1.bpmn");
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
      Mockito.when(dmsService.retrieveDocumentContent(xpdlModel.getId())).thenAnswer(
            new Answer<byte[]>()
            {
               @Override
               public byte[] answer(InvocationOnMock invocation) throws Throwable
               {
                  InputStream isModel = getClass().getResourceAsStream(
                        "OffsettingP1.xpdl");
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
                        "ModelConversionReferenceModel.xpdl");
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

   @Test
   public void testToXpdlModelConversion() throws Exception
   {
      // model ID of "Statement P1" BPMN2 test model
      final String srcModelId = "35258023-f7c1-4c12-a0ce-19de63b1d733";

      assertThat(modelService, is(not(nullValue())));

      assertThat(restController, is(not(nullValue())));

      RequestExecutor requestExecutor = new BeanInvocationExecutor(jsonIo, modelService,
            restController);

      JsonObject cmdJson = new JsonObject();

      cmdJson.addProperty("commandId", "model.clone");
      cmdJson.addProperty("modelId", srcModelId);
      cmdJson.add("changeDescriptions", new JsonArray());
      ((JsonArray) cmdJson.get("changeDescriptions")).add(new JsonObject());


      JsonObject changeJson = new JsonObject();
      changeJson.addProperty("targetFormat", "xpdl");
      ((JsonObject) ((JsonArray) cmdJson.get("changeDescriptions")).get(0)).add(
            "changes", changeJson);

      JsonObject cloneModelResult = requestExecutor.applyChange(cmdJson);

      String newModelId = cloneModelResult.getAsJsonObject("changes")
            .getAsJsonArray("added").get(0).getAsJsonObject()
            .get(ModelerConstants.ID_PROPERTY).getAsString();

      assertThat(newModelId, is(notNullValue()));

      DocumentManagementService dmsService = mockServiceFactoryLocator.get()
            .getDocumentManagementService();
      Mockito.when(
            dmsService.createDocument(Mockito.eq(DefaultModelManagementStrategy.MODELS_DIR),
                  Mockito.<Document> any(), Mockito.<byte[]> any(), Mockito.anyString()))
            .thenAnswer(new Answer<Document>()
            {
               @Override
               public Document answer(InvocationOnMock invocation) throws Throwable
               {
                  // trace the generated model
                  System.out.println(new String((byte[]) invocation.getArguments()[2]));
                  return null;
               }
            });

      modelService.saveModel(newModelId);
   }

   @Test
   public void testFromXpdlModelConversion() throws Exception
   {
      // model ID of "ModelConversionReferenceModel" XPDL test model
      final String srcModelId = "ModelConversionReferenceModel";

      assertThat(modelService, is(not(nullValue())));

      assertThat(restController, is(not(nullValue())));

      RequestExecutor requestExecutor = new BeanInvocationExecutor(jsonIo, modelService,
            restController);

      JsonObject cmdJson = new JsonObject();

      cmdJson.addProperty("commandId", "model.clone");
      cmdJson.addProperty("modelId", srcModelId);
      cmdJson.add("changeDescriptions", new JsonArray());
      ((JsonArray) cmdJson.get("changeDescriptions")).add(new JsonObject());


      JsonObject changeJson = new JsonObject();
      changeJson.addProperty("targetFormat", "bpmn2");
      ((JsonObject) ((JsonArray) cmdJson.get("changeDescriptions")).get(0)).add(
            "changes", changeJson);

      JsonObject cloneModelResult = requestExecutor.applyChange(cmdJson);

      String newModelId = cloneModelResult.getAsJsonObject("changes")
            .getAsJsonArray("added").get(0).getAsJsonObject()
            .get(ModelerConstants.ID_PROPERTY).getAsString();

      assertThat(newModelId, is(notNullValue()));

      DocumentManagementService dmsService = mockServiceFactoryLocator.get()
            .getDocumentManagementService();
      Mockito.when(
            dmsService.createDocument(Mockito.eq(DefaultModelManagementStrategy.MODELS_DIR),
                  Mockito.<Document> any(), Mockito.<byte[]> any(), Mockito.anyString()))
            .thenAnswer(new Answer<Document>()
            {
               @Override
               public Document answer(InvocationOnMock invocation) throws Throwable
               {
                  // trace the generated model
                  System.out.println(new String((byte[]) invocation.getArguments()[2]));
                  return null;
               }
            });

      modelService.saveModel(newModelId);
   }

}
