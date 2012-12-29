package org.eclipse.stardust.ui.web.modeler.service;

import static org.eclipse.stardust.common.CollectionUtils.newArrayList;
import static org.eclipse.stardust.common.StringUtils.isEmpty;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.eclipse.emf.ecore.EObject;
import org.springframework.beans.factory.annotation.Autowired;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.model.xpdl.builder.BpmModelBuilder;
import org.eclipse.stardust.model.xpdl.builder.strategy.AbstractModelManagementStrategy;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.ui.web.modeler.common.ModelPersistenceService;
import org.eclipse.stardust.ui.web.modeler.spi.ModelPersistenceHandler;
import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.http.cookie.Cookie;

import com.google.gson.JsonObject;

/**
 * Support insertion of model files received from the client and allows to download
 * changed model files. Used e.g. for integration with Orion.
 * 
 * @author Marc.Gille
 * 
 */
public class ClientModelManagementStrategy extends AbstractModelManagementStrategy
{
   private static final Logger trace = LogManager.getLogger(ClientModelManagementStrategy.class);

   private List<ModelDescriptor> loadedModels = newArrayList();

   private final ModelPersistenceService persistenceService;

   /**
    * Manages remote model files.
    */
   @Autowired
   public ClientModelManagementStrategy(ModelPersistenceService persistenceService)
   {
      this.persistenceService = persistenceService;
   }

   /**
   *
   */
   public JsonObject addModelFile(String filePath, String content)
   {
      trace.info("Caching model file " + filePath + " with content of length " + content.length());
      
      byte[] modelContent = content.getBytes();

      trace.info("Load Model " + filePath);

      ModelPersistenceHandler.ModelDescriptor<? > descriptor = null;

      try
      {
         descriptor = persistenceService.loadModel(filePath, new ByteArrayInputStream(
               modelContent));
      }
      catch (Exception x)
      {
         x.printStackTrace();

         throw new RuntimeException(x);
      }

      ModelType xpdlModel = null;
      EObject model = null;

      if (null != descriptor)
      {
         trace.info("Descriptor is not null");

         model = descriptor.model;

         if (descriptor.model instanceof ModelType)
         {
            xpdlModel = (ModelType) descriptor.model;

            trace.info("Built XPDL model");
         }
         else
         {
            // use just the most basic XPDL representation, rest will be handled
            // directly from native format (e.g. BPMN2)
            xpdlModel = BpmModelBuilder.newBpmModel()
                  .withIdAndName(descriptor.id,
                        !isEmpty(descriptor.name) ? descriptor.name : descriptor.id)
                  .build();
            trace.info("Built BPMN model");
         }
      }
      else
      {            
         throw new RuntimeException("Model cannot be obtained.");
      }

      if (null != xpdlModel)
      {
         // TODO - This method needs to move to some place where it will be called only
         // once for
         loadEObjectUUIDMap(xpdlModel);
         mapModelFileName(xpdlModel, filePath);

         trace.info("Adding model " + xpdlModel.getId() + " " + filePath);

         ModelDescriptor modelDescriptor = new ModelDescriptor(xpdlModel.getId(), filePath, model, xpdlModel); 
         
         loadedModels.add(modelDescriptor);

         JsonObject modelDescriptorJson = new JsonObject();
         
         modelDescriptorJson.addProperty("id", xpdlModel.getId());
         modelDescriptorJson.addProperty("uuid", uuidMapper().getUUID(xpdlModel));
         modelDescriptorJson.addProperty("name", xpdlModel.getName());
         
         return modelDescriptorJson;
      }
      else
      {            
         throw new RuntimeException("Model cannot be obtained.");
      }
   }

   /**
	 *
	 */
   public List<ModelDescriptor> loadModels()
   {
      return loadedModels;
   }

   /**
     *
     */
   public ModelType loadModel(String id)
   {
      trace.info("Load invoked on id " + id);

      return null;
   }

   /**
	 *
	 */
   public ModelType attachModel(String id)
   {
      return null;
   }

   /**
	 *
	 */
   public void saveModel(ModelType model)
   {
      // Do nothing as store is initiated from the client
   }

   /**
    * 
    * @param model
    */
   public void deleteModel(ModelType model)
   {
      // TODO Delete from cache
   }

   /**
    * @param fileName
    * @param fileContent
    * @param createNewVersion
    * @return
    */
   @Override
   public ModelUploadStatus uploadModelFile(String fileName, byte[] fileContent,
         boolean createNewVersion)
   {
      return ModelUploadStatus.NEW_MODEL_CREATED;
   }

   /**
	 *
	 */
   public void versionizeModel(ModelType model)
   {
   }

   /**
    * 
    * @param model
    */
   public String getModelFileName(ModelType model)
   {
      String modelUUID = uuidMapper().getUUID(model);
      return "IMPLEMENT ME!"; // modelFileNameMap.get(modelUUID);
   }

   /**
    * 
    * @param model
    */
   public String getModelFilePath(ModelType model)
   {
      String modelUUID = uuidMapper().getUUID(model);

      return "IMPLEMENT ME!";// + modelFileNameMap.get(modelUUID);
   }

   /**
    * @param model
    */
   private void mapModelFileName(ModelType model)
   {
      mapModelFileName(model, model.getId() + ".xpdl");
   }

   /**
    * @param model
    */
   private void mapModelFileName(ModelType model, String fileName)
   {
      String modelUUID = uuidMapper().getUUID(model);
      // modelFileNameMap.put(modelUUID, fileName);
   }

   /**
    * @param model
    */
   private void removeModelFileNameMapping(ModelType model)
   {
      String modelUUID = uuidMapper().getUUID(model);
      // modelFileNameMap.remove(modelUUID);
   }
}
