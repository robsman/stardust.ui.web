package org.eclipse.stardust.ui.web.html5.rest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.ui.web.common.Constants;
import org.eclipse.stardust.ui.web.common.IPerspectiveDefinition;
import org.eclipse.stardust.ui.web.common.PerspectiveDefinition;
import org.eclipse.stardust.ui.web.common.ViewDefinition;
import org.eclipse.stardust.ui.web.common.app.PerspectiveAuthorizationProxy;
import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.spi.env.RuntimeEnvironmentInfoProvider;
import org.eclipse.stardust.ui.web.common.spi.theme.ThemeProvider;
import org.eclipse.stardust.ui.web.common.spi.user.IAuthorizationProvider;
import org.eclipse.stardust.ui.web.common.spi.user.UserProvider;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.common.util.MessagePropertiesBean;
import org.eclipse.stardust.ui.web.common.util.UserUtils;
import org.eclipse.stardust.ui.web.plugin.support.ServiceLoaderUtils;
import org.eclipse.stardust.ui.web.plugin.support.resources.PluginResourceUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.google.gson.JsonObject;

/**
 * TODO: Load info dynamically. Using local server side architecture
 *
 * @author Subodh.Godbole
 *
 */
@Path("/html5/api")
public class HTML5FrameworkServices
{
   private static final Logger trace = LogManager.getLogger(HTML5FrameworkServices.class);
   private final String COMMON_MENU_KEY_SEPERATOR = "=";
   private final String NEWLINE_TAB = "\n\t";
   private final String htmlViewJSON = ",\n{\n\t\"label\": \"{label}\",\n\t\"id\": \"Int/VIEW_NAME/:id\",\n\t\"iconBase\":\"viewIconBase :icon\",\n\t\"icon\":\"viewIconBase tabIcon-generic\",\n\t\"module\": \"bpm-ui\",\n\t\"controller\": \"bpm-ui.InternalPageCtrl\",\n\t\"partial\": \"/CONTEXT_ROOTVIEW_PATH\"\n\t}";

   @Context
   private ServletContext servletContext;

   private ApplicationContext appContext;

   /**
    * Ignore requestLocale and use the locale set at the time of login
    * 
    * @param requestLocale
    * @return
    */
   public static String getLocaleCode(String requestLocale, ServletContext servletContext)
   {
      String localeCode = "en";

      try
      {
         MessagePropertiesBean messageBean = (MessagePropertiesBean) RestControllerUtils.resolveSpringBean(
               MessagePropertiesBean.class, servletContext);

         localeCode = messageBean.getLocaleObject().getLanguage();
         if (StringUtils.isNotEmpty(messageBean.getLocaleObject().getCountry()))
         {
            localeCode += ("_" + messageBean.getLocaleObject().getCountry());
         }
      }
      catch (Exception e)
      {
         trace.error("Unexpected error in getting local", e);
      }

      return localeCode;
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("config")
   public Response config(@HeaderParam("Accept-Language") String locale, @Context UriInfo uriInfo)
   {
      trace.debug("Retrieving config");

      String contents = getCodeResource("META-INF/xhtml/html5/templates/config.json");
      contents = StringUtils.replace(contents, "CONTEXT_ROOT", getDeploymentBaseURL(uriInfo, true));
      contents = StringUtils.replace(contents, "RANDOM_VALUE", getRandomValue());

      MessagePropertiesBean messageBean = (MessagePropertiesBean) RestControllerUtils.resolveSpringBean(
            MessagePropertiesBean.class, servletContext);

      String headerKey = servletContext.getInitParameter(Constants.LOGIN_HEADING);
      String bundleBasName = servletContext.getInitParameter(Constants.COMMON_MESSAGE_BUNDLE);
      
      // TODO: Parameterise this into context parameter
      contents = StringUtils.replace(contents, "DEFAULT_LOCALE", "en");

      contents = StringUtils.replace(contents, "PORTAL_TITLE",
            FacesUtils.getPortalTitle(headerKey, bundleBasName, messageBean.getLocaleObject()));

      contents = StringUtils.replace(contents, "SIDEBAR_LABEL",
            messageBean.getString("portalFramework.config.SIDEBAR_LABEL"));
      
      String commonMenuConfigStr = (String) RestControllerUtils.resolveSpringBean("commonMenuConfig", servletContext);
      if (StringUtils.isNotEmpty(commonMenuConfigStr))
      {
         contents = StringUtils.replace(contents, "COMMON_MENU", parseCommonMenuString(commonMenuConfigStr));
      }
      else
      {
         contents = StringUtils.replace(contents, "COMMON_MENU", "");
      }
      return Response.ok(contents, MediaType.APPLICATION_JSON_TYPE).build();
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("navigation")
   public Response navigation(@HeaderParam("Accept-Language") String locale, @Context UriInfo uriInfo)
   {
      trace.debug("Retrieving navigation");

      themesCurrent(null, null);
      
      String contents = getCodeResource("META-INF/xhtml/html5/templates/navigation.json");
      contents = StringUtils.replace(contents, "FULL_PATH", getDeploymentBaseURL(uriInfo, false));
      contents = StringUtils.replace(contents, "LOGGED_IN_USER_LABEL",
            RestControllerUtils.resolveSpringBean(UserProvider.class, servletContext).getUser().getDisplayName());
      
      String version = "";
    
      MessagePropertiesBean messageBean = (MessagePropertiesBean) RestControllerUtils.resolveSpringBean(
            MessagePropertiesBean.class, servletContext);
      
      contents = StringUtils.replace(contents, "PORTAL_LABEL",
            messageBean.getString("portalFramework.navigation.PORTAL_LABEL"));
      contents = StringUtils.replace(contents, "CONFIGURATION_LABEL",
            messageBean.getString("portalFramework.navigation.CONFIGURATION_LABEL"));
      contents = StringUtils.replace(contents, "ALERTS_LABEL",
            messageBean.getString("portalFramework.navigation.ALERTS_LABEL"));
      contents = StringUtils.replace(contents, "SIGN_OUT_LABEL",
            messageBean.getString("portalFramework.navigation.SIGN_OUT_LABEL"));
      
      contents = addHTMLViewsToNavigationJSON(contents);
      
      // Replacing CONTEXT_ROOT should be called after the above call to add html views 
      contents = StringUtils.replace(contents, "CONTEXT_ROOT", getDeploymentBaseURL(uriInfo, true));
      
      try
      {
         RuntimeEnvironmentInfoProvider envInfoProvider = RestControllerUtils.resolveSpringBean(RuntimeEnvironmentInfoProvider.class, servletContext);
         version = (null != envInfoProvider) ? envInfoProvider.getVersion().getCompleteString() : "";
      }
      catch (Exception e)
      { 
         version = "dev";
         trace.error("Could not retrieve Version Information", e);
      }
      contents = StringUtils.replace(contents, "BUILD_INFO", version);

      return Response.ok(contents, MediaType.APPLICATION_JSON_TYPE).build();
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("messages/{locale}")
   public Response messages(@PathParam("locale") String locale)
   { 
      String localeCode = getLocaleCode(locale, servletContext);

      JsonObject messages = new JsonObject();
      messages.add(localeCode, new JsonObject());

      JsonObject translation = new JsonObject();
      messages.getAsJsonObject(localeCode).add("translation", translation);

      String key, value, component;
      int index;

      MessagePropertiesBean messageBean = (MessagePropertiesBean) RestControllerUtils.resolveSpringBean(
            MessagePropertiesBean.class, servletContext);

      Map<String, String> bundleValues = loadBundle("html5-framework-messages", messageBean.getLocaleObject());
      bundleValues.putAll(getPluginMessages());

      for (Entry<String, String> entry : bundleValues.entrySet())
      {
         index = entry.getKey().indexOf(".");
         if (index > 0)
         {
            key = entry.getKey();
            value = entry.getValue();

            component = key.substring(0, index);
            key = key.substring(index + 1);
            key = key.replace(".", "-"); // "." is special char in HTML5 Framework
            
            if(null == translation.get(component))
            {
               translation.add(component, new JsonObject());  
            }
            translation.getAsJsonObject(component).addProperty(key, value);
         }
         else
         {
            translation.addProperty(entry.getKey(), entry.getValue());
         }
      }

      return Response.ok(messages.toString()).build();
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("themes/current")
   public Response themesCurrent(@QueryParam("context") String context, @QueryParam("appStage") String appStage)
   {
      String contents = getCodeResource("META-INF/xhtml/html5/templates/currentTheme.json");

      String stylesJson = "";
      ThemeProvider themeProvider = RestControllerUtils.resolveSpringBean(ThemeProvider.class, servletContext);
      List<String> styleSheets = themeProvider.getStyleSheets();
      if (CollectionUtils.isNotEmpty(styleSheets))
      {
         StringBuffer sb = new StringBuffer();
         Iterator<String> it = styleSheets.iterator();
         while(it.hasNext())
         {
            String css = it.next();
            sb.append(",\n\t\"").append(css.substring(1)).append("\"");
         }
         stylesJson = sb.toString();
      }
      
      contents = addPluginViewIconStyleSheets(contents);
      contents = StringUtils.replace(contents, "PORAL_SKIN_STYLE_SHEETS", stylesJson);

      return Response.ok(contents, MediaType.APPLICATION_JSON_TYPE).build();
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("themes/current/custom")
   public Response themesCustom(@QueryParam("context") String context, @QueryParam("appStage") String appStage)
   {
      String stylesJson = "{\"stylesheets\": [";
      ThemeProvider themeProvider = RestControllerUtils.resolveSpringBean(ThemeProvider.class, servletContext);
      List<String> styleSheets = themeProvider.getStyleSheets();
      if (CollectionUtils.isNotEmpty(styleSheets))
      {
         StringBuffer sb = new StringBuffer();
         Iterator<String> it = styleSheets.iterator();
         while(it.hasNext())
         {
            String css = it.next();
            sb.append(",\"").append(css.substring(1)).append("\"");
         }
         sb.deleteCharAt(0);
         stylesJson += sb.toString();
      }
      stylesJson += "]}";
      return Response.ok(stylesJson, MediaType.APPLICATION_JSON_TYPE).build();
   }

   /**
    * @param contents
    * @return
    */
   public String addPluginViewIconStyleSheets(String contents)
   {
      final String STYLE_FILE_POSTFIX = "-icons.css";
      final String STYLES_FILE_PATH = "css/*-icons.css";
      StringBuffer pluginIconsStyles = new StringBuffer("");

      try
      {
         Set<String> cssFileNames = PluginResourceUtils.getMatchingFileNames(getAppContext(), STYLES_FILE_PATH);
         
         for (String fileName : cssFileNames)
         {
            String pluginName = (fileName.substring(0, fileName.indexOf(STYLE_FILE_POSTFIX)));
            pluginIconsStyles.append(",\n\t\"plugins/" + pluginName + "/css/"
                  + fileName + "\"");
         }
      }
      catch (IOException e)
      {
         trace.warn(e);
      }

      contents = StringUtils.replace(contents, "PLUGIN_VIEW_ICON_STYLE_SHEETS",
            pluginIconsStyles.toString());

      return contents;
   }
   
   /**
    * @param contents
    */
   private String addHTMLViewsToNavigationJSON(String contents)
   {
      List<IPerspectiveDefinition> perspectiveDefs = getAllPerspectives();
      StringBuffer allHtmlViews = new StringBuffer("");
      for (IPerspectiveDefinition perspectiveDef : perspectiveDefs)
      {
         List<ViewDefinition> viewDefs = perspectiveDef.getViews();
         for (ViewDefinition viewDef : viewDefs)
         {
            if (viewDef.getInclude().endsWith(".html"))
            {
               String str = StringUtils.replace(htmlViewJSON, "VIEW_NAME",
                     viewDef.getName());
               str = StringUtils.replace(str, "VIEW_PATH", viewDef.getInclude());
               allHtmlViews.append(str);
            }
         }
      }

      contents = StringUtils.replace(contents, "NATIVE_VIEW_DEFS",
            allHtmlViews.toString());
      
      return contents;
   }

   /**
    * @param file
    * @return
    */
   private String getCodeResource(final String file)
   {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      try
      {
         IOUtils.copy(new ClassPathResource(file).getInputStream(), baos);
      }
      catch (Exception exception)
      {
         System.out.println("error trying to read file " + file + exception);
      }

      return new String(baos.toByteArray());
   }

   /**
    * @param uriInfo
    * @return
    */
   private String getDeploymentBaseURL(UriInfo uriInfo, boolean onlyContext)
   {
      URI baseUri = uriInfo.getBaseUri();

      // E.g. path = /<context-root>/services/rest/common
      String path = baseUri.getPath();
      path = path.substring(1);

      String toReturn;
      int index = path.indexOf("/");
      if (index != -1)
      {
         toReturn = path.substring(0, index);
      }
      else
      {
         toReturn = path; // Just a fall-back. Control will never come here
      }

      if (!onlyContext)
      {
         String portalBaseURL = servletContext.getInitParameter("InfinityBpm.PortalBaseUri");
         StringBuffer sb = new StringBuffer();
         if(StringUtils.isNotEmpty(portalBaseURL))
         {
            if (portalBaseURL.contains("/${request.contextPath}"))
            {
               portalBaseURL = portalBaseURL.replace("${request.contextPath}/", toReturn);
               sb.append(portalBaseURL);
            }
         }
         else
         {
         sb.append(baseUri.getScheme()).append("://");
         sb.append(baseUri.getHost());
         if (baseUri.getPort() > 0){
            sb.append(":");
            sb.append(baseUri.getPort());
         }
         sb.append("/");
         sb.append(toReturn);
         }
         toReturn = sb.toString();
      }

      return toReturn;
   }

   /**
    * @return
    */
   private String getRandomValue()
   {
      Random random = new Random();
      return String.valueOf(random.nextInt(10000));
   }
   
   /**
    * 
    * @param commonMenuString
    * @return
    */
   private String parseCommonMenuString(String commonMenuString)
   {
      String arr[] = commonMenuString.split(",");
      StringBuffer commonMenuJsonStr = new StringBuffer();
      commonMenuJsonStr.append("," +NEWLINE_TAB);
      commonMenuJsonStr.append(splitAndParseStr(arr[0], COMMON_MENU_KEY_SEPERATOR));
      commonMenuJsonStr.append("," + NEWLINE_TAB);
      commonMenuJsonStr.append(splitAndParseStr(arr[1], COMMON_MENU_KEY_SEPERATOR));
      return commonMenuJsonStr.toString();
   }

   /**
    * 
    * @param strVal
    * @param seperator
    * @return
    */
   private String splitAndParseStr(String strVal, String seperator)
   {
      StringBuffer buffer = new StringBuffer();
      if (StringUtils.isNotEmpty(strVal))
      {
         String params[] = strVal.split(seperator);
         buffer.append("\"" + params[0] + "\"");
         buffer.append(":");
         buffer.append("\"" + params[1] + "\"");
      }
      return buffer.toString();
   }
   
   
   /**
    * TODO - this function needs to be moved to some utility class after the TODO
    * mentioned inside the method is resolved
    * 
    * @return
    */
   private List<IPerspectiveDefinition> getAllPerspectives() {
      Map<String, PerspectiveDefinition> systemPerspectives = getAppContext().getBeansOfType(PerspectiveDefinition.class);
      List<IPerspectiveDefinition> allPerspectives = new ArrayList<IPerspectiveDefinition>();
      Map<String, IPerspectiveDefinition> perspectives = new HashMap<String, IPerspectiveDefinition>();
      for (String key : systemPerspectives.keySet())
      {
         PerspectiveDefinition pd = systemPerspectives.get(key);
         if (isAuthorized(pd))
         {
            perspectives.put(key, PerspectiveAuthorizationProxy.newInstance(pd,
                  (PortalApplication) getAppContext().getBean("ippPortalApp")));            
         }
      }
      
      for (IPerspectiveDefinition perspectiveDef : perspectives.values())
      {
         allPerspectives.add(perspectiveDef);
      }
      
      return allPerspectives;
   }

   /**
    * @param perspectiveDef
    * @return
    * 
    */
   private boolean isAuthorized(PerspectiveDefinition perspectiveDef)
   {
      Boolean isAuthorized = isAuthorized_(perspectiveDef.getName());

      if (null != isAuthorized)
      {
         return isAuthorized;
      }
      else
      {
         return UserUtils.isAuthorized(
               ((UserProvider) getAppContext().getBean("userProvider")).getUser(),
               perspectiveDef.getRequiredRolesSet(), perspectiveDef.getExcludeRolesSet());
      }
   }
   

   /**
    * (Portal UI Authorization)
    * @param elementName
    * @return
    */
   private Boolean isAuthorized_(String elementName)
   {
      IAuthorizationProvider authorizationProvider = getAuthorizationProvider();
      Boolean isAuthorized = null;

      if (null != authorizationProvider)
      {
         isAuthorized = authorizationProvider.isAuthorized(((UserProvider) getAppContext().getBean("userProvider")).getUser(), elementName);
      }

      return isAuthorized;
   }
   
   /**
    * @return
    */
   private IAuthorizationProvider getAuthorizationProvider()
   {
      Iterator<IAuthorizationProvider.Factory> serviceProviders = ServiceLoaderUtils.searchProviders(IAuthorizationProvider.Factory.class);

      IAuthorizationProvider.Factory factory = null;

      if (null != serviceProviders)
      {
         while (serviceProviders.hasNext())
         {
            factory = serviceProviders.next();
            if (null != factory)
            {
               return factory.getAuthorizationProvider();
            }
         }
      }
      return null;
   }

   /**
    * @return
    */
   private Map<String, String> getPluginMessages()
   {
      Set<String> bundles = new HashSet<String>();
      Map<String, String> values = new LinkedHashMap<String, String>();

      MessagePropertiesBean messageBean = (MessagePropertiesBean) RestControllerUtils.resolveSpringBean(
            MessagePropertiesBean.class, servletContext);

      List<IPerspectiveDefinition> perspectiveDefs = getAllPerspectives();
      for (IPerspectiveDefinition perspectiveDef : perspectiveDefs)
      {
         bundles.addAll(perspectiveDef.getMessageBundlesSet());
      }

      trace.info("Loading Plugin Bundles: " + bundles);
      for (String bundle : bundles)
      {
         values.putAll(loadBundle(bundle, messageBean.getLocaleObject()));
      }

      return values;
   }

   /**
    * @param baseName
    * @param locale
    * @return
    */
   private Map<String, String> loadBundle(String baseName, Locale locale)
   {
      Map<String, String> values = new LinkedHashMap<String, String>();

      if (null != baseName)
      {
         ResourceBundle bundle = ResourceBundle.getBundle(baseName, locale);
         for (String key : Collections.list(bundle.getKeys()))
         {
            values.put(key, bundle.getString(key));
         }
      }

      return values;
   }

   /**
    * @return
    */
   private ApplicationContext getAppContext()
   {
      if (null == appContext)
      {
         appContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
      }

      return appContext;
   }
}
