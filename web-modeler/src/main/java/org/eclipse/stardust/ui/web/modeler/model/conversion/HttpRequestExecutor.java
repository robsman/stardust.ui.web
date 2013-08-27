package org.eclipse.stardust.ui.web.modeler.model.conversion;

import static junit.framework.Assert.assertTrue;

import java.io.InputStream;
import java.io.InputStreamReader;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.cxf.jaxrs.client.WebClient;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.eclipse.stardust.common.error.PublicException;

public class HttpRequestExecutor extends RequestExecutor
{
   public static final String SERVICE_PREFIX = "services/rest/bpm-modeler/modeler/1234567890/";

   public static final String SERVICE_ALL_MODELS = "models";

   public static final String SERVICE_APPLY_CHANGE = "sessions/changes";

   private final String sessionCookie;

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

      assertTrue(allModels.getEntity() instanceof InputStream);

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

      assertTrue(respDiagram.getEntity() instanceof InputStream);

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

      assertTrue(createModelResponse.getEntity() instanceof InputStream);

      if (Status.CREATED.getStatusCode() == createModelResponse.getStatus()
            || Status.OK.getStatusCode() == createModelResponse.getStatus())
      {
         JsonElement createModelJson = new JsonParser().parse(new InputStreamReader(
               (InputStream) createModelResponse.getEntity()));

         return ((JsonObject) createModelJson);
      }
      else
      {
         throw new PublicException("Failed applying change: "
               + new JsonParser().parse(new InputStreamReader((InputStream) createModelResponse
                     .getEntity())).getAsString());
      }
   }
}
