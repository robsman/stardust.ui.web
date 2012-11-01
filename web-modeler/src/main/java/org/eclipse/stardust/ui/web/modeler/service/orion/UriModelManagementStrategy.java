package org.eclipse.stardust.ui.web.modeler.service.orion;

import static org.eclipse.stardust.common.CollectionUtils.newArrayList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javax.annotation.Resource;

import org.apache.http.Consts;
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
import org.springframework.context.ApplicationContext;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.model.xpdl.builder.strategy.AbstractModelManagementStrategy;
import org.eclipse.stardust.model.xpdl.builder.utils.XpdlModelIoUtils;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.ui.web.modeler.spi.ModelPersistenceHandler;
import org.eclipse.stardust.ui.web.modeler.xpdl.XpdlPersistenceHandler;

/**
 * 
 * @author Marc.Gille
 * 
 */
public class UriModelManagementStrategy extends AbstractModelManagementStrategy
{
   private static final Logger trace = LogManager.getLogger(UriModelManagementStrategy.class);

   /**
	 *
	 */
   private Map<String, String> modelFileNameMap = new HashMap<String, String>();

   private final List<ModelPersistenceHandler> persistenceHandlers;

   private String fileUri;

   private static final int timeOut = 100000;

   /**
    * Manages remote model files.
    */
   public UriModelManagementStrategy()
   {
      this.persistenceHandlers = newArrayList();
      persistenceHandlers.add(new XpdlPersistenceHandler(this));

      // // TODO migrate to Spring based discovery?
      // // see also ModelRepository
      // try
      // {
      // String fqcnBpmn2Handler =
      // "org.eclipse.stardust.ui.web.modeler.bpmn2.Bpmn2PersistenceHandler";
      // @SuppressWarnings("unchecked")
      // Class<? extends ModelPersistenceHandler> clsBpmn2Handler =
      // Reflect.getClassFromClassName(fqcnBpmn2Handler, false);
      // if (null != clsBpmn2Handler)
      // {
      // ModelPersistenceHandler bpmn2Handler = (ModelPersistenceHandler)
      // Reflect.createInstance(clsBpmn2Handler, null, null);
      // persistenceHandlers.add(bpmn2Handler);
      // trace.info("Registered BPMN2 persistence handler.");
      // }
      // else
      // {
      // trace.info("Could not load BPMN2 persistence handler, BPMN2 support will not be available.");
      // }
      // }
      // catch (Exception e)
      // {
      // trace.warn("Failed loading BPMN2 persistence handler.", e);
      // }
   }

   /**
   *
   */
   public void setFileUri(String fileUri)
   {
      this.fileUri = fileUri;
   }

   /**
	 *
	 */
   public List<ModelDescriptor> loadModels()
   {
      List<ModelDescriptor> models = newArrayList();

      String modelFileContent = retrieveModelFileString("http://localhost:9090" + fileUri);

      // deflate, if necesarily

      // if ("gzip".equals(uc.getContentEncoding()))
      // is = new GZIPInputStream(is);

      // for (Document modelDocument : candidateModelDocuments)
      // {
      // ModelType xpdlModel = null;
      // EObject model = null;
      //
      // byte[] modelContent = readModelContext(modelDocument);
      // for (ModelPersistenceHandler persistenceHandler : persistenceHandlers)
      // {
      // ByteArrayInputStream baos = new ByteArrayInputStream(modelContent);
      // ModelPersistenceHandler.ModelDescriptor descriptor =
      // persistenceHandler.loadModel(
      // modelDocument.getName(), baos);
      // if (null != descriptor)
      // {
      // model = descriptor.model;
      // if (descriptor.model instanceof ModelType)
      // {
      // xpdlModel = (ModelType) descriptor.model;
      // }
      // else
      // {
      // // use just the most basic XPDL representation, rest will be handled
      // // directly from native format (e.g. BPMN2)
      // xpdlModel = BpmModelBuilder.newBpmModel()
      // .withIdAndName(descriptor.id,
      // !isEmpty(descriptor.name) ? descriptor.name : descriptor.id)
      // .build();
      //
      // break;
      // }
      // }
      // }
      //
      // if (null != xpdlModel)
      // {
      // // TODO - This method needs to move to some place where it will be called only
      // // once for
      // loadEObjectUUIDMap(xpdlModel);
      // mapModelFileName(xpdlModel, modelDocument.getName());
      //
      // models.add(new ModelDescriptor(xpdlModel.getId(), modelDocument.getName(),
      // model, xpdlModel));
      // }
      // }

      return models;
   }

   /**
    * 
    * @param uri
    * @return
    */
   public static String retrieveModelFileString(String uri)
   {
      try
      {
         URL url = new URL(uri);

         URLConnection urlConnection = url.openConnection();
         urlConnection.setConnectTimeout(1000);
         urlConnection.setReadTimeout(1000);
         BufferedReader breader = new BufferedReader(new InputStreamReader(
               urlConnection.getInputStream()));

         StringBuilder stringBuilder = new StringBuilder();

         String line;

         while ((line = breader.readLine()) != null)
         {
            stringBuilder.append(line);
         }

         return stringBuilder.toString();
      }
      catch (MalformedURLException e)
      {
         throw new RuntimeException(e);
      }
      catch (IOException e)
      {
         throw new RuntimeException(e);
      }
   }

   /**
     *
     */
   public ModelType loadModel(String id)
   {
      // Folder folder = documentManagementService.getFolder(MODELS_DIR);
      // List<Document> candidateModelDocuments = folder.getDocuments();
      // for (Document modelDocument : candidateModelDocuments) {
      // if (modelDocument.getName().endsWith(".xpdl"))
      // {
      // if(modelDocument.getName().equals(id))
      // {
      // ModelType model = XpdlModelIoUtils
      // .loadModel(readModelContext(modelDocument), this);
      // loadEObjectUUIDMap(model);
      // mapModelFileName(model, modelDocument.getName());
      //
      // return model;
      // }
      // }
      // }

      return null;
   }

   /**
	 *
	 */
   public ModelType attachModel(String id)
   {
      ModelType model = null;

      // XpdlModelIoUtils
      // .loadModel(readModelContext(getDocumentManagementService()
      // .getDocument(MODELS_DIR + id + ".xpdl")), this);
      // loadEObjectUUIDMap(model);
      //
      // getModels().put(id, model);

      return model;
   }

   /**
	 *
	 */
   public void saveModel(ModelType model)
   {
      String modelContent = new String(XpdlModelIoUtils.saveModel(model));
   }

   /**
    * 
    * @param model
    */
   public void deleteModel(ModelType model)
   {
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
      return modelFileNameMap.get(modelUUID);
   }

   /**
    * 
    * @param model
    */
   public String getModelFilePath(ModelType model)
   {
      String modelUUID = uuidMapper().getUUID(model);

      return "" + modelFileNameMap.get(modelUUID);
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
      modelFileNameMap.put(modelUUID, fileName);
   }

   /**
    * @param model
    */
   private void removeModelFileNameMapping(ModelType model)
   {
      String modelUUID = uuidMapper().getUUID(model);
      modelFileNameMap.remove(modelUUID);
   }

   public static void main(String[] args) throws Exception
   {
      HttpHost targetHost = new HttpHost("localhost", 9090, "http");
      DefaultHttpClient httpclient = new DefaultHttpClient();
    
      try
      {
         httpclient.getCredentialsProvider().setCredentials(
               new AuthScope(targetHost.getHostName(), targetHost.getPort()),
               new UsernamePasswordCredentials("motu", "motu"));

         // Create AuthCache instance
         AuthCache authCache = new BasicAuthCache();
         // Generate BASIC scheme object and add it to the local
         // auth cache
         BasicScheme basicAuth = new BasicScheme();
         authCache.put(targetHost, basicAuth);

         // Add AuthCache to the execution context
         BasicHttpContext localcontext = new BasicHttpContext();
         localcontext.setAttribute(ClientContext.AUTH_CACHE, authCache);

         // HttpGet httpget = new HttpGet("/file/motu/Test/CustomerOnboarding.xpdl");
         HttpGet httpget = new HttpGet("/file/motu/Test/test.xpdl");
         //HttpGet httpget = new HttpGet("/");

         System.out.println("executing request: " + httpget.getRequestLine());
         System.out.println("to target: " + targetHost);

         HttpResponse response = httpclient.execute(targetHost, httpget, localcontext);
         HttpEntity entity = response.getEntity();

         System.out.println("----------------------------------------");
         System.out.println(response.getStatusLine());
         if (entity != null)
         {
            System.out.println("Response content length: " + entity.getContentLength());
            System.out.println("Response content type: " + entity.getContentType());
            System.out.println("Response content: " + convertStreamToString(entity.getContent()));
         }

         EntityUtils.consume(entity);
      }
      finally
      {
         // When HttpClient instance is no longer needed,
         // shut down the connection manager to ensure
         // immediate deallocation of all system resources
         httpclient.getConnectionManager().shutdown();
      }
   }
   
   public static String convertStreamToString(java.io.InputStream is) {
      java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
      return s.hasNext() ? s.next() : "";
   }
}
