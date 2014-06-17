package org.eclipse.stardust.ui.mobile.rest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import javax.activation.DataHandler;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import com.google.gson.JsonObject;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.engine.core.interactions.InteractionRegistry;
import org.eclipse.stardust.ui.mobile.common.LanguageUtil;
import org.eclipse.stardust.ui.mobile.service.ActivitySearchHelper;
import org.eclipse.stardust.ui.mobile.service.DocumentSearchHelper;
import org.eclipse.stardust.ui.mobile.service.MobileWorkflowService;
import org.eclipse.stardust.ui.mobile.service.ProcessSearchHelper;
import org.eclipse.stardust.ui.mobile.service.WorklistHelper;


/**
 * 
 * @author Marc.Gille
 * 
 */
@Path("/")
public class MobileWorkflowResource {
	private MobileWorkflowService mobileWorkflowService;
	private final JsonMarshaller jsonIo = new JsonMarshaller();

	@Context
	private HttpServletRequest httpRequest;

	@Context
	private ServletContext servletContext;
	   
	/**
	 * 
	 * @return
	 */
	public MobileWorkflowService getMobileWorkflowService() {
		return mobileWorkflowService;
	}

	/**
	 * 
	 * @param mobileWorkflowService
	 */
	public void setMobileWorkflowService(
			MobileWorkflowService mobileWorkflowService) {
		this.mobileWorkflowService = mobileWorkflowService;
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("login")
	public Response login(String postedData) {
		try {
			JsonObject json = jsonIo.readJsonObject(postedData);

			return Response.ok(
					getMobileWorkflowService().login(json).toString(),
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
               getMobileWorkflowService().logout().toString(),
               MediaType.APPLICATION_JSON_TYPE).build();
      } catch (Exception e) {
         e.printStackTrace();

         throw new RuntimeException(e);
      }
   }

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("process-definitions")
	public Response getProcessDefinitions(@QueryParam("startable") String startable) {
		try {
			// Initit registry
			getInteractionRegistry();
			return Response.ok(
					getMobileWorkflowService().getProcessDefinitions(Boolean.parseBoolean(startable))
							.toString(), MediaType.APPLICATION_JSON_TYPE)
					.build();
		} catch (Exception e) {
			e.printStackTrace();

			throw new RuntimeException(e);
		}
	}

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("activities")
   public Response getActivities(@QueryParam("processDefinitionIds") String processDefinitionIds) {
      try {
         return Response.ok(
               getMobileWorkflowService().getActivities(processDefinitionIds)
                     .toString(), MediaType.APPLICATION_JSON_TYPE)
               .build();
      } catch (Exception e) {
         e.printStackTrace();

         throw new RuntimeException(e);
      }
   }

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("worklist")
	public Response getWorklist(@QueryParam("sortKey") String sortKey,
         @QueryParam("rowFrom") String rowFrom,
         @QueryParam("pageSize") String pageSize) {
      try
      {
         // Initit registry
         getInteractionRegistry();

         return Response.ok(
               getMobileWorkflowService().getWorklist(
                     WorklistHelper.getWorkslitCriteria(sortKey, rowFrom, pageSize)).toString(),
               MediaType.APPLICATION_JSON_TYPE).build();
      } catch (Exception e) {
			e.printStackTrace();

			throw new RuntimeException(e);
		}
	}

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("worklist/count")
   public Response getWorklistCount() {
      try {
         return Response.ok(
               getMobileWorkflowService().getWorklistCount().toString(),
               MediaType.APPLICATION_JSON_TYPE).build();
      } catch (Exception e) {
         e.printStackTrace();

         throw new RuntimeException(e);
      }
   }
   
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("activity-instances/{oid: \\d+}/delegatees")
   public Response getDelegatees(
		   @PathParam("oid") String activityInstanceOid,
		   @QueryParam("name") String delegateeName){
	   
	   try{
		   return Response.ok(getMobileWorkflowService()
				   .getDelegatees(activityInstanceOid,delegateeName)
				   .toString(),MediaType.APPLICATION_JSON_TYPE)
				   .build();
	   } catch(Exception e){
		   e.printStackTrace();
	       throw new RuntimeException(e);
	   }
	   
   }
   
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("activity-instances")
   public Response getActivityInstances(@QueryParam("startedFromTimestamp") String startedFromTimestamp,
         @QueryParam("startedToTimestamp") String startedToTimestamp,
         @QueryParam("processDefinitionIds") String processDefinitionIds,
         @QueryParam("activityIds") String activityIds,
         @QueryParam("states") String states,
         @QueryParam("sortKey") String sortKey,
         @QueryParam("rowFrom") String rowFrom,
         @QueryParam("pageSize") String pageSize) {
      try
      {
         return Response.ok(
               getMobileWorkflowService().getActivityInstances(
                     ActivitySearchHelper.getActivitySearchCriteria(startedFromTimestamp,
                           startedToTimestamp, processDefinitionIds, activityIds, states,
                           sortKey, rowFrom, pageSize)).toString(),
               MediaType.APPLICATION_JSON_TYPE).build();
      } catch (Exception e) {
         e.printStackTrace();

         throw new RuntimeException(e);
      }
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("activity-instances/states")
   public Response getActivityInstanceStates() {
      try {
         return Response.ok(
               getMobileWorkflowService().getActivityInstanceStates().toString(),
               MediaType.APPLICATION_JSON_TYPE).build();
      } catch (Exception e) {
         e.printStackTrace();

         throw new RuntimeException(e);
      }
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("activity-instances/{oid: \\d+}")
   public Response getActivityInstance(@PathParam("oid") String activityInstanceOid) {
      try {
         return Response.ok(
               getMobileWorkflowService().getActivityInstanceJson(
                     getMobileWorkflowService().getActivityInstance(Long.parseLong(activityInstanceOid))).toString(),
               MediaType.APPLICATION_JSON_TYPE).build();
      } catch (Exception e) {
         e.printStackTrace();

         throw new RuntimeException(e);
      }
   }

   
	@PUT
	@Produces(MediaType.APPLICATION_JSON)
	@Path("activity-instances/{oid: \\d+}/activation")
	public Response activateActivity(@PathParam("oid") String activityInstanceOid) {
		try {
			// TODO @SG - pass the servlet / spring context to service instead of passing it the needed beans.
			return Response.ok(
					getMobileWorkflowService().activateActivity(Long.parseLong(activityInstanceOid))
							.toString(), MediaType.APPLICATION_JSON_TYPE)
					.build();
		} catch (Exception e) {
			e.printStackTrace();

			throw new RuntimeException(e);
		}
	}

   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("process-instances")
   public Response startProcessInstance(String postedData) {
      try {
         JsonObject json = jsonIo.readJsonObject(postedData);

         return Response.ok(
               getMobileWorkflowService().startProcessInstance(json, getInteractionRegistry()).toString(),
               MediaType.APPLICATION_JSON_TYPE).build();
      } catch (Exception e) {
         e.printStackTrace();

         throw new RuntimeException(e);
      }
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("process-instances")
   public Response getProcessInstances(@QueryParam("startedFromTimestamp") String startedFromTimestamp,
         @QueryParam("startedToTimestamp") String startedToTimestamp,
         @QueryParam("processDefinitionIds") String processDefinitionIds,
         @QueryParam("states") String states,
         @QueryParam("sortKey") String sortKey,
         @QueryParam("rowFrom") String rowFrom,
         @QueryParam("pageSize") String pageSize) {
      try
      {
         return Response.ok(
               getMobileWorkflowService().getProcessInstances(
                     ProcessSearchHelper.getProcessSearchCriteria(startedFromTimestamp,
                           startedToTimestamp, processDefinitionIds, states, sortKey,
                           rowFrom, pageSize)).toString(), MediaType.APPLICATION_JSON_TYPE)
               .build();
      } catch (Exception e) {
         e.printStackTrace();

         throw new RuntimeException(e);
      }
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("process-instances/states")
   public Response getProcessInstanceStates() {
      try {
         return Response.ok(
               getMobileWorkflowService().getProcessInstanceStates().toString(),
               MediaType.APPLICATION_JSON_TYPE).build();
      } catch (Exception e) {
         e.printStackTrace();

         throw new RuntimeException(e);
      }
   }

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("process-instances/{oid: \\d+}")
	public Response getProcessInstance(@PathParam("oid") String processOid) {
		try {
			return Response.ok(
					getMobileWorkflowService().getProcessInstanceJson(
					      getMobileWorkflowService().getProcessInstance(Long.parseLong(processOid))).toString(),
					MediaType.APPLICATION_JSON_TYPE).build();
		} catch (Exception e) {
			e.printStackTrace();

			throw new RuntimeException(e);
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("process-instances/{oid: \\d+}/notes")
	public Response getNotes(@PathParam("oid") String processOid) {
		try {
			return Response.ok(
					getMobileWorkflowService().getNotes(
							Long.parseLong(processOid)).toString(),
					MediaType.APPLICATION_JSON_TYPE).build();
		} catch (Exception e) {
			e.printStackTrace();

			throw new RuntimeException(e);
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("process-instances/{oid: \\d+}/notes")
	public Response createNote(String postedData) {
		try {
			JsonObject json = jsonIo.readJsonObject(postedData);

			return Response.ok(
					getMobileWorkflowService().createNote(json).toString(),
					MediaType.APPLICATION_JSON_TYPE).build();
		} catch (Exception e) {
			e.printStackTrace();

			throw new RuntimeException(e);
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("process-instances/{oid: \\d+}/documents")
	public Response getProcessDocuments(@PathParam("oid") String processOid) {
		try {
			return Response.ok(
					getMobileWorkflowService().getProcessInstanceDocuments(
							Long.parseLong(processOid)).toString(),
					MediaType.APPLICATION_JSON_TYPE).build();
		} catch (Exception e) {
			e.printStackTrace();

			throw new RuntimeException(e);
		}
	}

   @POST
   @Path("process-instances/{oid: \\d+}/documents")
   @Consumes(MediaType.MULTIPART_FORM_DATA)
   @Produces(MediaType.APPLICATION_JSON)
   public Response uploadFile(@PathParam("oid")
   String processOid, List<Attachment> attachments, @Context
   HttpServletRequest request)
   {

      JsonObject response = null;
      for (Attachment attachment : attachments)
      {
         DataHandler dataHandler = attachment.getDataHandler();
         try
         {
            InputStream inputStream = dataHandler.getInputStream();
            MultivaluedMap<String, String> headers = attachment.getHeaders();

            FileInfo fileInfo = getFileName(headers);

            String folderPath = Parameters.instance().getString(
                  "Carnot.Portal.FileUploadPath", servletContext.getRealPath("/"));
            // file path
            String filePath = folderPath + fileInfo.name;
            File uploadedFile = new File(filePath);

            OutputStream outputStream = new FileOutputStream(uploadedFile);

            int read = 0;
            byte[] bytes = new byte[1024];
            while ((read = inputStream.read(bytes)) != -1)
            {
               outputStream.write(bytes, 0, read);
            }
            inputStream.close();

            outputStream.flush();
            outputStream.close();

            // Store file in Process Attachments
            getMobileWorkflowService().addProcessAttachment(Long.parseLong(processOid),
                  uploadedFile);

            response = new JsonObject();
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
      }

      if (response != null)
      {
         return Response.ok(response.toString(), MediaType.APPLICATION_JSON_TYPE).build();
      }
      else
      {
         return Response.serverError().build();
      }
   }

   @POST
   @Path("process-instances/{oid: \\d+}")
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public Response updateProcessInstance(@PathParam("oid") String oid, String postedData)
   {
      try
      {
         return Response.ok(
               getMobileWorkflowService().updateProcessInstance(oid,
                     jsonIo.readJsonObject(postedData)).toString()).build();
      }
      catch (Exception e)
      {
         e.printStackTrace();
         throw new RuntimeException(e);
      }
   }
         
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("documents")
   public Response getDocuments(@QueryParam("searchText") String searchText,
         @QueryParam("createFromTimestamp") String createFromTimestamp,
         @QueryParam("createToTimestamp") String createToTimestamp,
         @QueryParam("documentTypeIds") String documentTypeIds,
         @QueryParam("sortKey") String sortKey,
         @QueryParam("rowFrom") String rowFrom,
         @QueryParam("pageSize") String pageSize) {
      try
      {
         return Response.ok(
               getMobileWorkflowService().getDocuments(
                     DocumentSearchHelper.getDocumentSearchCriteria(searchText,
                           createFromTimestamp, createToTimestamp, documentTypeIds,
                           sortKey, rowFrom, pageSize)).toString(),
               MediaType.APPLICATION_JSON_TYPE).build();
      } catch (Exception e) {
         e.printStackTrace();

         throw new RuntimeException(e);
      }
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("documents/{documentId}")
   public Response getDocument(@PathParam("documentId") String documentId) {
      try {
         return Response.ok(
               getMobileWorkflowService().getDocument(documentId).toString(),
               MediaType.APPLICATION_JSON_TYPE).build();
      } catch (Exception e) {
         e.printStackTrace();

         throw new RuntimeException(e);
      }
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("process-instances/{oid: \\d+}/documents/process-attachments/{documentId}")
   public Response getDocument(@PathParam("oid") String processOid, @PathParam("documentId") String documentId) {
      try {
         return Response.ok(
               getMobileWorkflowService().getProcessInstanceDocument(
                     Long.parseLong(processOid), documentId).toString(),
               MediaType.APPLICATION_JSON_TYPE).build();
      } catch (Exception e) {
         e.printStackTrace();

         throw new RuntimeException(e);
      }
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("document-types")
   public Response getDocumentTypes() {
      try {
         return Response.ok(
               getMobileWorkflowService().getDocumentTypes().toString(),
               MediaType.APPLICATION_JSON_TYPE).build();
      } catch (Exception e) {
         e.printStackTrace();

         throw new RuntimeException(e);
      }
   }

   /**
    * @param header
    * @return
    */
   private FileInfo getFileName(MultivaluedMap<String, String> header)
   {
      String[] contentDisposition = header.getFirst("Content-Disposition").split(";");

      FileInfo fileInfo = new FileInfo();
      for (String filename : contentDisposition)
      {
         if ((filename.trim().startsWith("filename")))
         {
            String[] name = filename.split("=");
            fileInfo.name = name[1].trim().replaceAll("\"", "");
         }
      }

      fileInfo.contentType = header.getFirst("Content-Type");

      return fileInfo;
   }

   /**
    * 
    * @author Yogesh.Manware
    * 
    */
   private class FileInfo
   {
      String name;

      String contentType;

      public FileInfo()
      {
         name = "unknown";
         contentType = "unknown";
      }
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("process-instances/{oid: \\d+}/history")
   public Response getProcessHistory(@PathParam("oid") String processOid, @QueryParam("selectedProcessInstanceOid") String strSelectedProcessOid) {
      try {
         System.out.println("startingProcessOid: " + strSelectedProcessOid);
         long selectedProcessInstanceOid = 0; 
         if (strSelectedProcessOid != null && !strSelectedProcessOid.equals(""))
         {
            selectedProcessInstanceOid = Long.parseLong(strSelectedProcessOid);
         }
         return Response.ok(
               getMobileWorkflowService().getProcessHistory(
                     Long.parseLong(processOid), selectedProcessInstanceOid).toString(),
               MediaType.APPLICATION_JSON_TYPE).build();
      } catch (Exception e) {
         e.printStackTrace();

         throw new RuntimeException(e);
      }
   }

   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("activity-instances/{oid: \\d+}/complete")
   public Response completeActivity(@PathParam("oid") String oid, String postedData)
   {
      try
      {
         JsonObject postedDataJson = jsonIo.readJsonObject(postedData);

         return Response.ok(
               getMobileWorkflowService().completeActivity(oid).toString(),
               MediaType.APPLICATION_JSON_TYPE).build();
      }
      catch (Exception e)
      {
         e.printStackTrace();

         throw new RuntimeException(e);
      }
   }
   
   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("activity-instances/{oid : \\d+}/suspend")
   public Response suspendActivity(@PathParam("oid") String oid, String postedData)
   {
      try
      {
         JsonObject postedDataJson = jsonIo.readJsonObject(postedData);

         return Response.ok(
               getMobileWorkflowService().suspendActivity(oid).toString(),
               MediaType.APPLICATION_JSON_TYPE).build();
      }
      catch (Exception e)
      {
         e.printStackTrace();

         throw new RuntimeException(e);
      }
   }   

   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("activity-instances/{oid : \\d+}/suspendAndSave")
   public Response suspendAndSaveActivity(@PathParam("oid") String oid, String postedData)
   {
      try
      {
         JsonObject postedDataJson = jsonIo.readJsonObject(postedData);

         return Response.ok(
               getMobileWorkflowService().suspendAndSaveActivity(oid).toString(),
               MediaType.APPLICATION_JSON_TYPE).build();
      }
      catch (Exception e)
      {
         e.printStackTrace();

         throw new RuntimeException(e);
      }
   }

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("folders/{folderId: .*}")
//   @Path("folders{folderId: \\/{0,1}/|\\/b}")
	public Response getRepositoryFolder(@PathParam("folderId") String folderId) {
		try {
		   System.out.println("folderId: " + folderId);
			return Response.ok(
					getMobileWorkflowService().getRepositoryFolder(folderId).toString(),
					MediaType.APPLICATION_JSON_TYPE).build();
		} catch (Exception e) {
			e.printStackTrace();

			throw new RuntimeException(e);
		}
	}

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("folders/{folderId}/documents/{documentId}")
   public Response getRepositoryDocument(@PathParam("folderId") String folderId, @PathParam("documentId") String documentId) {
      try {
         return Response.ok(
               getMobileWorkflowService().getRepositoryDocument(folderId, documentId).toString(),
               MediaType.APPLICATION_JSON_TYPE).build();
      } catch (Exception e) {
         e.printStackTrace();

         throw new RuntimeException(e);
      }
   }
   
   /**
    * 
    * @return
    */
   private InteractionRegistry getInteractionRegistry() {
	   InteractionRegistry registry = (InteractionRegistry) servletContext.getAttribute(InteractionRegistry.BEAN_ID);
	   if (registry != null) return registry;
	   
	   synchronized (this) {
		   if (null == registry) {
			   registry = new InteractionRegistry();
			   servletContext.setAttribute(InteractionRegistry.BEAN_ID, registry);
		   }
	   }
	   
	   return registry;
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
