package org.eclipse.stardust.ui.web.documenttriage.rest;

import java.util.Enumeration;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.gson.JsonObject;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.ui.web.documenttriage.common.LanguageUtil;
import org.eclipse.stardust.ui.web.documenttriage.service.DocumentTriageService;


@Path("/")
public class DocumentTriageResource {
	private DocumentTriageService documentTriageService;
	private final JsonMarshaller jsonIo = new JsonMarshaller();

	@Context
	private HttpServletRequest httpRequest;

	@Context
	private ServletContext servletContext;
	   
	/**
	 * 
	 * @return
	 */
	public DocumentTriageService getDocumentTriageService() {
		return documentTriageService;
	}

	/**
	 * 
	 * @param documentTriageService
	 */
	public void setDocumentTriageService(
	      DocumentTriageService documentTriageService) {
		this.documentTriageService = documentTriageService;
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("login")
	public Response login(String postedData) {
		try {
			JsonObject json = jsonIo.readJsonObject(postedData);

			return Response.ok(
					getDocumentTriageService().login(json).toString(),
					MediaType.APPLICATION_JSON_TYPE).build();
		} catch (Exception e) {
			e.printStackTrace();

			throw new RuntimeException(e);
		}
	}
	
   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("logout")
   public Response logout(String postedData) {
      try {
         JsonObject json = jsonIo.readJsonObject(postedData);

         return Response.ok(
               getDocumentTriageService().logout().toString(),
               MediaType.APPLICATION_JSON_TYPE).build();
      } catch (Exception e) {
         e.printStackTrace();

         throw new RuntimeException(e);
      }
   }
   
   @GET
   @Produces(MediaType.TEXT_PLAIN)
   @Path("/language")
   public Response getLanguage()
   {
      StringTokenizer tok = new StringTokenizer(httpRequest.getHeader("Accept-language"), ",");
      if (tok.hasMoreTokens())
      {
         return Response.ok(LanguageUtil.getLocale(tok.nextToken()), MediaType.TEXT_PLAIN_TYPE).build();
      }
      return Response.ok("en", MediaType.TEXT_PLAIN_TYPE).build();
   }
   
   /**
    * @param bundleName
    * @param locale
    * @return
    */
   @GET
   @Path("/{bundleName}/{locale}")
   public Response getRetrieve(@PathParam("bundleName") String bundleName,
         @PathParam("locale") String locale) {
      final String POST_FIX = "client-messages";

      if (StringUtils.isNotEmpty(bundleName) && bundleName.endsWith(POST_FIX)) {
         try {
            StringBuffer bundleData = new StringBuffer();
            ResourceBundle bundle = ResourceBundle.getBundle(bundleName,
                  LanguageUtil.getLocaleObject(locale));

            String key;
            Enumeration<String> keys = bundle.getKeys();
            while (keys.hasMoreElements()) {
               key = keys.nextElement();
               bundleData.append(key).append("=")
                     .append(bundle.getString(key)).append("\n");
            }

            return Response.ok(bundleData.toString(),
                  MediaType.TEXT_PLAIN_TYPE).build();
         } catch (MissingResourceException mre) {
            return Response.status(Status.NOT_FOUND).build();
         } catch (Exception e) {
            return Response.status(Status.BAD_REQUEST).build();
         }
      } else {
         return Response.status(Status.FORBIDDEN).build();
      }
   }
}
