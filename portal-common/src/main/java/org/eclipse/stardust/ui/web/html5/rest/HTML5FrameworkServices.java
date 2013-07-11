package org.eclipse.stardust.ui.web.html5.rest;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.Iterator;
import java.util.List;

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
import org.eclipse.stardust.ui.web.common.spi.theme.ThemeProvider;
import org.eclipse.stardust.ui.web.common.spi.user.UserProvider;
import org.springframework.core.io.ClassPathResource;


/**
 * TODO: Load info dynamically. Using local server side architecture
 *
 * @author Subodh.Godbole
 *
 */
@Path("/html5/api")
public class HTML5FrameworkServices
{
   @Context
   private ServletContext servletContext;

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("config")
   public Response config(@HeaderParam("Accept-Language") String locale, @Context UriInfo uriInfo)
   {
      String contents = getCodeResource("bpm-ui/templates/config.json");
      contents = StringUtils.replace(contents, "CONTEXT_ROOT", getDeploymentBaseURL(uriInfo, true));

      // TODO: Read from property bundle
      contents = StringUtils.replace(contents, "PORTAL_TITLE", "Infinity Process Platform");
      contents = StringUtils.replace(contents, "LOCALE_ID", "en");
      contents = StringUtils.replace(contents, "SIDEBAR_LABEL", "Navigation");

      return Response.ok(contents, MediaType.APPLICATION_JSON_TYPE).build();
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("navigation")
   public Response navigation(@HeaderParam("Accept-Language") String locale, @Context UriInfo uriInfo)
   {
      String contents = getCodeResource("bpm-ui/templates/navigation.json");
      contents = StringUtils.replace(contents, "CONTEXT_ROOT", getDeploymentBaseURL(uriInfo, true));
      contents = StringUtils.replace(contents, "FULL_PATH", getDeploymentBaseURL(uriInfo, false));
      contents = StringUtils.replace(contents, "LOGGED_IN_USER_LABEL",
            RestControllerUtils.resolveSpringBean(UserProvider.class, servletContext).getUser().getDisplayName());

      return Response.ok(contents, MediaType.APPLICATION_JSON_TYPE).build();
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("messages/{locale}")
   public Response messages(@PathParam("locale") String locale)
   {
      return Response.ok(getCodeResource("bpm-ui/templates/message.json")).build();
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("themes/current")
   public Response themesCurrent(@QueryParam("context") String context, @QueryParam("appStage") String appStage)
   {
      String contents = getCodeResource("bpm-ui/templates/currentTheme.json");

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
      
      contents = StringUtils.replace(contents, "PORAL_SKIN_STYLE_SHEETS", stylesJson);

      return Response.ok(contents, MediaType.APPLICATION_JSON_TYPE).build();
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
         StringBuffer sb = new StringBuffer();
         sb.append(baseUri.getScheme()).append("://");
         sb.append(baseUri.getHost()).append(":");
         sb.append(baseUri.getPort()).append("/");
         sb.append(toReturn);

         toReturn = sb.toString();
      }

      return toReturn;
   }
}
