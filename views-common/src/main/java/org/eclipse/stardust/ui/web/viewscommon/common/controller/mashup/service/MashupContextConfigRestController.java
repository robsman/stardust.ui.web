package org.eclipse.stardust.ui.web.viewscommon.common.controller.mashup.service;

import static java.util.Collections.singletonMap;
import static org.eclipse.stardust.common.CollectionUtils.newHashMap;
import static org.eclipse.stardust.common.StringUtils.isEmpty;
import static org.eclipse.stardust.ui.web.viewscommon.common.controller.UriEncodingUtils.encodeURIComponent;

import java.net.URI;
import java.util.Map;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.Path;
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

import org.eclipse.stardust.ui.web.viewscommon.common.controller.UriEncodingUtils;
import org.eclipse.stardust.ui.web.viewscommon.common.controller.mashup.MashupContext;
import org.eclipse.stardust.ui.web.viewscommon.common.controller.mashup.MashupContextConfigManager;
import org.eclipse.stardust.ui.web.viewscommon.common.controller.mashup.MashupControllerUtils;

public class MashupContextConfigRestController
{
   public static final String USER_INFO_BASE_PATH = "userInfo";

   public static final String PARAM_ACCESS_TOKEN = "access_token";

   public static final String PARAM_JSONP_CALLBACK = "callback";

   public static final String IPP_MASHUP_AUTH_PROXY_FILE = "ippMashupAuthProxy.html";

   @Resource
   private final MashupContextConfigManager contextManager = null;

   public Map<String, String> obtainMashupPanelBootstrapParams(String mashupUri,
         Map<String, String> credentials, URI restServicesBaseUri)
   {
      String contextId = contextManager.registerContext(new MashupContext(mashupUri,
            credentials));
      long contextExpiresIn = Math
            .max(-1, //
                  (contextManager.getContextExpiry(contextId) - System
                        .currentTimeMillis()) / 1000L);

      String authProxyUri = deriveAuthProxyUri(URI.create(mashupUri));

      String userInfoUri = restServicesBaseUri.resolve(USER_INFO_BASE_PATH).toString();

      Map<String, String> params = newHashMap();
      params.put("auth_proxy_uri", authProxyUri);
      params.put("user_info_uri", userInfoUri);
      params.put("access_token", contextId);
      params.put("token_type", "Bearer");
      params.put("expires_in", Long.toString(contextExpiresIn));

      return params;
   }

   public String obtainMashupPanelBootstrapUri(String mashupUri,
         Map<String, String> credentials, URI restServicesBaseUri, URI portalBaseUri)
   {
      Map<String, String> params = obtainMashupPanelBootstrapParams(mashupUri,
            credentials, restServicesBaseUri);

      return params.get("auth_proxy_uri") //
            + "#user_info_uri=" + params.get("user_info_uri") //
            + "&access_token=" + params.get("access_token") //
            + "&token_type=" + params.get("token_type") //
            + "&expires_in=" + params.get("expires_in");
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
      URI mashupUri = URI
            .create("http://emeafralp083.internal.sungard.corp:8080/sec-redirect/requestTracer");
      Map<String, String> creds = singletonMap("ipp-auth-token",
            "top-secret-" + System.currentTimeMillis());

      URI portalBaseUri = uriInfo.getRequestUri().resolve("../../../");
      URI servicesBaseUri = portalBaseUri.resolve("services/");

      Map<String, String> bootstrapParams = MashupControllerUtils
            .obtainMashupPanelBootstrapParams(contextManager, mashupUri, creds,
                  servicesBaseUri);

      URI bootstrapUri = MashupControllerUtils.buildMashupBootstrapUri(bootstrapParams,
            portalBaseUri);

      return Response.seeOther(bootstrapUri).build();
   }

   @OPTIONS
   @Path(USER_INFO_BASE_PATH)
   public Response retrieveUserInfoMetadata(
         @HeaderParam("Authorization") String accessTokenAuth)
   {
      ResponseBuilder rb = Response.ok();

      rb.header("Access-Control-Allow-Origin", "*");
      rb.header("Access-Control-Allow-Headers", "Authorization");

      return rb.build();
   }

   @GET
   @Path(USER_INFO_BASE_PATH)
   @Produces(MediaType.APPLICATION_JSON)
   public Response retrieveUserInfo(@HeaderParam("Authorization") String accessTokenAuth,
         @QueryParam(PARAM_ACCESS_TOKEN) String accessToken,
         @QueryParam(PARAM_JSONP_CALLBACK) String jsonpCallback)
   {
      String contextId = null;
      if ( !isEmpty(accessTokenAuth) && accessTokenAuth.startsWith("Bearer "))
      {
         contextId = accessTokenAuth.substring("Bearer ".length());
      }
      else if ( !isEmpty(accessToken))
      {
         contextId = accessToken;
      }

      ResponseBuilder rb = Response.status(Status.BAD_REQUEST);

      MashupContext mashupContext = !isEmpty(contextId) ? contextManager
            .getContext(contextId) : null;
      if (null != mashupContext)
      {
         rb.header("Access-Control-Allow-Origin", domainFromUri(URI.create(mashupContext.uri)));
         rb.header("Access-Control-Allow-Headers", "Authorization");

         // indicate response must not be cached anywhere
         CacheControl cacheControl = new CacheControl();
         cacheControl.setNoStore(true);
         cacheControl.setNoCache(true);
         cacheControl.setPrivate(true);
         rb.cacheControl(cacheControl);
         rb.header("Pragma", "no-cache");

         String contextConfigJson = generateMashupContextResponse(mashupContext);
         if (!isEmpty(jsonpCallback))
         {
            // cater for JSONP requests (e.g. IE earlier than 10)
            contextConfigJson = jsonpCallback + "(" + contextConfigJson + ");";
         }

         // indicate the context config was delivered to avoid repeated retrieval
         contextManager.consumeContext(contextId, mashupContext);

         rb.entity(contextConfigJson);
         rb.status(Status.OK);
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
      contextJson.add("ipp_session_credentials", cookiesJson);

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
