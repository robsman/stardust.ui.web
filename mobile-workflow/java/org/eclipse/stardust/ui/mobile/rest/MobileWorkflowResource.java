package org.eclipse.stardust.ui.mobile.rest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.activation.DataHandler;
import javax.annotation.Resource;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.engine.core.interactions.InteractionRegistry;
import org.eclipse.stardust.ui.mobile.service.MobileWorkflowService;
import org.springframework.context.ApplicationContext;

import com.google.gson.JsonObject;


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

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("process-definitions")
	public Response getProcesses(@QueryParam("startable") String startable) {
		try {
			return Response.ok(
					getMobileWorkflowService().getProcesses(Boolean.parseBoolean(startable))
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
	public Response getWorklist() {
		try {
			return Response.ok(
					getMobileWorkflowService().getWorklist().toString(),
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
               getMobileWorkflowService().startProcessInstance(json).toString(),
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
}
