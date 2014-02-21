package org.eclipse.stardust.ui.mobile.rest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.activation.DataHandler;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.ui.mobile.service.MobileWorkflowService;

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

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("startable-processes")
	public Response getStartableProcesses() {
		try {
			return Response.ok(
					getMobileWorkflowService().getStartableProcesses()
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

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("activity/activate")
	public Response activateActivity(String postedData) {
		try {
			JsonObject json = jsonIo.readJsonObject(postedData);

			return Response.ok(
					getMobileWorkflowService().activateActivity(json)
							.toString(), MediaType.APPLICATION_JSON_TYPE)
					.build();
		} catch (Exception e) {
			e.printStackTrace();

			throw new RuntimeException(e);
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("process-instances/{oid}")
	public Response getProcessInstance(@PathParam("oid") String processOid) {
		try {
			return Response.ok(
					getMobileWorkflowService().getProcessInstance(
							Long.parseLong(processOid)).toString(),
					MediaType.APPLICATION_JSON_TYPE).build();
		} catch (Exception e) {
			e.printStackTrace();

			throw new RuntimeException(e);
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("process-instances/{oid}/notes")
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
	@Path("process-instances/{oid}/notes/create")
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
	@Path("process-instances/{oid}/documents")
	public Response getDocuments(@PathParam("oid") String processOid) {
		try {
			return Response.ok(
					getMobileWorkflowService().getDocuments(
							Long.parseLong(processOid)).toString(),
					MediaType.APPLICATION_JSON_TYPE).build();
		} catch (Exception e) {
			e.printStackTrace();

			throw new RuntimeException(e);
		}
	}

   @POST
   @Path("process-instances/{oid}/documents")
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

   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("activity-instances/{oid}/complete")
   public Response completeActivity(String postedData)
   {
      try
      {
         JsonObject postedDataJson = jsonIo.readJsonObject(postedData);

         return Response.ok(
               getMobileWorkflowService().completeActivity(
                     postedDataJson.getAsJsonObject("activityInstance"),
                     postedDataJson.getAsJsonObject("outData")),
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
	@Path("folders/{id}")
	public Response getFolders(@PathParam("id") String folderId) {
		try {
			return Response.ok(
					getMobileWorkflowService().getFolders(folderId).toString(),
					MediaType.APPLICATION_JSON_TYPE).build();
		} catch (Exception e) {
			e.printStackTrace();

			throw new RuntimeException(e);
		}
	}
}
