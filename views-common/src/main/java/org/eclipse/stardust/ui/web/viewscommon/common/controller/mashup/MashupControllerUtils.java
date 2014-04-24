package org.eclipse.stardust.ui.web.viewscommon.common.controller.mashup;

import static org.eclipse.stardust.common.CollectionUtils.newHashMap;
import static org.eclipse.stardust.ui.web.viewscommon.common.controller.UriEncodingUtils.encodeURIComponent;

import java.net.URI;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.stardust.common.Functor;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.TransformingIterator;
import org.eclipse.stardust.common.config.Parameters;

public class MashupControllerUtils
{
   public static final String IPP_MASHUP_AUTH_PROXY_FILE = "ippMashupAuthProxy.html";

   public static final String USER_INFO_RESOURCE_PATH = "rest/views-common/userInfo";

   public static boolean isEnabled()
   {
      String featureGuardConfig = Parameters.instance().getString(
            "org.eclipse.stardust.ui.web.feature.mashupCredentialsPropagation",
            "withSessionTokens");

      return "withSessionTokens".equals(featureGuardConfig)
            || "always".equals(featureGuardConfig);
   }

   public static boolean isAlwaysEnabled()
   {
      String featureGuardConfig = Parameters.instance().getString(
            "org.eclipse.stardust.ui.web.feature.mashupCredentialsPropagation",
            "withSessionTokens");

      return "always".equals(featureGuardConfig);
   }

   public static Map<String, String> obtainMashupPanelBootstrapParams(MashupContextConfigManager contextManager, URI mashupUri,
         Map<String, String> credentials, URI restServicesBaseUri)
   {
      String contextId = contextManager.registerContext(new MashupContext(mashupUri.toString(),
            credentials));
      long contextExpiresIn = Math
            .max(-1, //
                  (contextManager.getContextExpiry(contextId) - System
                        .currentTimeMillis()) / 1000L);

      String authProxyUri = deriveAuthProxyUri(mashupUri);

      URI userInfoUri = restServicesBaseUri.resolve(USER_INFO_RESOURCE_PATH);

      Map<String, String> params = newHashMap();
      params.put("auth_proxy_uri", authProxyUri);
      params.put("user_info_uri", userInfoUri.toString());
      params.put("access_token", contextId);
      params.put("token_type", "Bearer");
      params.put("expires_in", Long.toString(contextExpiresIn));

      return params;
   }

   public static URI buildMashupBootstrapUri(final Map<String, String> params,
         URI portalBaseUri)
   {
      URI mashupLoaderUri = portalBaseUri
            .resolve("plugins/views-common/common/controller/mashup/ippMashupLoader.html");

      String panelBootsrapHash = StringUtils.join(
            transformation(params.keySet(), new Functor<String, String>()
            {
               @Override
               public String execute(String key)
               {
                  return key + "=" + encodeURIComponent(params.get(key));
               }
            }), "&");

      return URI.create(mashupLoaderUri + "#" + panelBootsrapHash);
   }

   public static <S, T> Iterator<T> transformation(Iterable< ? extends S> source,
         Functor<S, T> transformation)
   {
      return new TransformingIterator<S, T>(source.iterator(), transformation);
   }

   public static String deriveAuthProxyUri(URI uri)
   {
      return uri.resolve(IPP_MASHUP_AUTH_PROXY_FILE).toString();
   }


   private MashupControllerUtils()
   {
      // utility class
   }
}
