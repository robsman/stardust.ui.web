package org.eclipse.stardust.ui.web.html5.rest;

import java.io.ByteArrayOutputStream;
import java.net.URI;

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

import org.apache.commons.io.IOUtils;
import org.eclipse.stardust.common.StringUtils;
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
   public Response config(@Context UriInfo uriInfo)
   {
      String contents = getCodeResource("bpm-ui/staticTest/config.json");
      contents = StringUtils.replace(contents, "CONTEXT_ROOT", getDeploymentBaseURL(uriInfo, true));

      return Response.ok(contents, MediaType.APPLICATION_JSON_TYPE).build();
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("navigation")
   public Response navigation(@HeaderParam("Accept-Language") String locale, @Context UriInfo uriInfo)
   {
      String contents = getCodeResource("bpm-ui/staticTest/navigation-en.json");
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
      return Response.ok(getCodeResource("bpm-ui/staticTest/message-en.json")).build();
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("themes")
   public Response themes(@QueryParam("appStage") String appStage)
   {
      return Response.ok(getCodeResource("bpm-ui/staticTest/themes.json"), MediaType.APPLICATION_JSON_TYPE).build();
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("themes/current")
   public Response themesCurrent(@QueryParam("context") String context, @QueryParam("appStage") String appStage)
   {
      return Response.ok(getCodeResource("bpm-ui/staticTest/theme1.json"), MediaType.APPLICATION_JSON_TYPE).build();
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
