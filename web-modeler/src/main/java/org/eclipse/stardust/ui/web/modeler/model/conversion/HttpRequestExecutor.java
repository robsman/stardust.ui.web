package org.eclipse.stardust.ui.web.modeler.model.conversion;

import java.io.InputStream;
import java.io.InputStreamReader;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

//import org.apache.cxf.jaxrs.client.WebClient;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class HttpRequestExecutor extends RequestExecutor
{
   public static final String SERVICE_PREFIX = "services/rest/bpm-modeler/modeler/1234567890/";

   public static final String SERVICE_ALL_MODELS = "models";

   public static final String SERVICE_APPLY_CHANGE = "sessions/changes";

   private final String sessionCookie;

   /**
    * This is a stub implementation of CXF's WebClient API, to enable compilation. To
    * really use this adapter the original import needs to be re-enabled and this stub
    * class removed.
    */
   private static class WebClient
   {
      public static WebClient create(String baseUri, boolean threadSafe)
      {
         throw new UnsupportedOperationException(
               "Please uncomment the org.apache.cxf.jaxrs.client.WebClient import and recompile.");
      }

      public WebClient path(String path)
      {
         return null;
      }

      public WebClient header(String name, Object... values)
      {
         return null;
      }

      public WebClient query(String name, Object... values)
      {
         return null;
      }

      public Response get()
      {
         return null;
      }

      public Response post(Object body)
      {
         return null;
      }
   }

   public WebClient webClient = WebClient.create("http://localhost:8080/pepper-test/",
         false);

   public HttpRequestExecutor(String sessionCookie)
   {
      this.sessionCookie = sessionCookie;
   }

   @Override
   public JsonObject loadAllModels()
   {
      Response allModels = webClient.path(SERVICE_PREFIX + SERVICE_ALL_MODELS)
            .header("Cookie", "JSESSIONID=" + sessionCookie).query("reload", false).get();

      assert (allModels.getEntity() instanceof InputStream);

      JsonObject allModelsJson = new JsonParser().parse(
            new InputStreamReader((InputStream) allModels.getEntity())).getAsJsonObject();

      return allModelsJson;
   }

   @Override
   public JsonObject loadProcessDiagram(String modelId, String processId)
   {
      // load associated diagram
      Response respDiagram = webClient
            .path(SERVICE_PREFIX + SERVICE_ALL_MODELS + "/" + modelId + "/process/"
                  + processId + "/loadModel")
            .header("Cookie", "JSESSIONID=" + sessionCookie).get();

      assert (respDiagram.getEntity() instanceof InputStream);

      JsonObject diagramJson = new JsonParser().parse(
            new InputStreamReader((InputStream) respDiagram.getEntity()))
            .getAsJsonObject();

      return diagramJson;
   }

   @Override
   public JsonObject applyChange(JsonObject cmdJson)
   {
      Response createModelResponse = webClient
            .path(SERVICE_PREFIX + SERVICE_APPLY_CHANGE)
            .header("Cookie", "JSESSIONID=" + sessionCookie)
            .header("Content-Type", "application/json").post(cmdJson.toString());

      assert (createModelResponse.getEntity() instanceof InputStream);

      if (Status.CREATED.getStatusCode() == createModelResponse.getStatus()
            || Status.OK.getStatusCode() == createModelResponse.getStatus())
      {
         JsonElement createModelJson = new JsonParser().parse(new InputStreamReader(
               (InputStream) createModelResponse.getEntity()));

         return ((JsonObject) createModelJson);
      }
      else
      {
         throw new WebApplicationException(createModelResponse);
      }
   }
}
