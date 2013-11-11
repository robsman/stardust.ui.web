package org.eclipse.stardust.ui.web.viewscommon.common.controller.mashup.service;

import static java.util.Collections.singletonMap;
import static org.eclipse.stardust.common.StringUtils.isEmpty;

import java.net.URI;
import java.util.Map;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.eclipse.stardust.ui.web.viewscommon.common.controller.mashup.MashupContext;
import org.eclipse.stardust.ui.web.viewscommon.common.controller.mashup.MashupContextConfigManager;

public class MashupContextConfigRestController
{
   public static final String CONTEXT_CONFIG_PATH = "mashupContextConfig/";

   public static final String PARAM_CONTEXT_ID = "contextId";

   public static final String PARAM_JSONP_CALLBACK = "callback";

   public static final String IPP_MASHUP_AUTH_PROXY_FILE = "ippMashupAuthProxy.html";

   @Resource
   private final MashupContextConfigManager contextManager = null;

   public String obtainMashupPanelBootstrapUri(String mashupUri,
         Map<String, String> credentials, URI restServicesBaseUri)
   {
      String contextId = contextManager.registerContext(new MashupContext(mashupUri,
            credentials));

      String authProxyUri = deriveAuthProxyUri(URI.create(mashupUri));

      String contextApiUri = restServicesBaseUri.resolve(CONTEXT_CONFIG_PATH + contextId)
            .toString();

      return authProxyUri + "?contextConfigCallback=" + contextApiUri;
   }

   public static String deriveAuthProxyUri(URI uri)
   {
      return uri.resolve(IPP_MASHUP_AUTH_PROXY_FILE).toString();
   }

   @GET
   @Path("testCycle")
   public Response initiateTestCycle(@Context UriInfo uriInfo)
   {
      // TODO remove test data
      String mashupUri = "http://emeafralp083.internal.sungard.corp:8080/sec-redirect/requestTracer";
      Map<String, String> credentials = singletonMap("ipp-auth-token", "top-secret-"
            + System.currentTimeMillis());

      String panelBootstrapUri = obtainMashupPanelBootstrapUri(mashupUri, credentials,
            uriInfo.getRequestUri());
      return Response.seeOther(URI.create(panelBootstrapUri)).build();
   }

   @OPTIONS
   @Path(CONTEXT_CONFIG_PATH + "{" + PARAM_CONTEXT_ID + "}")
   public Response retrieveContextConfigMetadata(
         @PathParam(PARAM_CONTEXT_ID) String contextId)
   {
      ResponseBuilder rb = Response.status(Status.BAD_REQUEST);

      MashupContext mashupContext = (null != contextId) ? contextManager
            .getContext(contextId) : null;

      if (null == mashupContext)
      {
         rb.status(Status.NOT_FOUND);
      }
      else
      {
         URI targetUri = URI.create(mashupContext.uri);
         rb.header("Access-Control-Allow-Origin", domainFromUri(targetUri));

         rb.status(Status.OK);
      }

      return rb.build();
   }

   @GET
   @Path(CONTEXT_CONFIG_PATH + "{" + PARAM_CONTEXT_ID + "}")
   @Produces(MediaType.APPLICATION_JSON)
   public Response retrieveContextConfig(@PathParam(PARAM_CONTEXT_ID) String contextId,
         @QueryParam(PARAM_JSONP_CALLBACK) String jsonpCallback)
   {
      Response metadataResponse = retrieveContextConfigMetadata(contextId);

      ResponseBuilder rb = Response.fromResponse(metadataResponse);

      if (Status.OK.getStatusCode() == metadataResponse.getStatus())
      {
         // indicate response must not be cached anywhere
         CacheControl cacheControl = new CacheControl();
         cacheControl.setNoStore(true);
         cacheControl.setNoCache(true);
         cacheControl.setPrivate(true);
         rb.cacheControl(cacheControl);

         MashupContext mashupContext = contextManager.getContext(contextId);

         String contextConfigJson = generateMashupContextResponse(mashupContext);
         if (!isEmpty(jsonpCallback))
         {
            // cater for JSONP requests (e.g. IE earlier than 10)
            contextConfigJson = jsonpCallback + "(" + contextConfigJson + ");";
         }

         // indicate the context config was delivered to avoid repeated retrieval
         contextManager.consumeContext(contextId, mashupContext);

         rb.entity(contextConfigJson);
      }

      return rb.build();
   }

   private String generateMashupContextResponse(MashupContext mashupContext)
   {
      Gson gson = new GsonBuilder().create();
      JsonObject contextJson = new JsonObject();
      contextJson.addProperty("uri", mashupContext.uri);

      JsonArray cookiesJson = new JsonArray();
      for (String cookieName : mashupContext.credentials.keySet())
      {
         JsonObject cookieJson = new JsonObject();
         cookieJson.addProperty("name", cookieName);
         cookieJson.addProperty("value", mashupContext.credentials.get(cookieName));
         // TODO more cookie attributes like "secure"?

         cookiesJson.add(cookieJson);
      }
      contextJson.add("cookies", cookiesJson);

      return gson.toJson(contextJson);
   }

   private static String domainFromUri(URI uri)
   {
      StringBuilder builder = new StringBuilder(uri.toString().length());

      if (null != uri.getScheme())
      {
         builder.append(uri.getScheme()).append(":");
      }
      builder.append("//").append(uri.getHost());
      if (-1 != uri.getPort())
      {
         builder.append(":").append(uri.getPort());
      }

      return builder.toString();
   }

}
