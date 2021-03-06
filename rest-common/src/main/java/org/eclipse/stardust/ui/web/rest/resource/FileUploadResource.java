package org.eclipse.stardust.ui.web.rest.resource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.activation.DataHandler;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.eclipse.stardust.ui.web.html5.rest.RestControllerUtils;
import org.eclipse.stardust.ui.web.rest.dto.FileInfoDTO;
import org.eclipse.stardust.ui.web.rest.util.FileUploadUtils;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.FileStorage;
import org.eclipse.stardust.ui.web.viewscommon.utils.MIMEType;
import org.eclipse.stardust.ui.web.viewscommon.utils.MimeTypesHelper;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * 
 * @author Nikhil.Gahlot
 *
 */
@Path("/file-upload")
public class FileUploadResource
{

   private static final String DOC_PATH = "../../plugins/views-common/images/icons/";
   
   @Context
   protected ServletContext servletContext;

   @POST
   @Path("upload")
   @Consumes(MediaType.MULTIPART_FORM_DATA)
   public Response uploadFile(List<Attachment> attachments, @Context HttpServletRequest request)
   {
      JsonArray response = new JsonArray();
      for (Attachment attachment : attachments)
      {
         DataHandler dataHandler = attachment.getDataHandler();
         try
         {
            InputStream inputStream = dataHandler.getInputStream();
            MultivaluedMap<String, String> headers = attachment.getHeaders();

            FileInfoDTO fileInfo = FileUploadUtils.getFileInfo(headers);

            FileStorage fileStorage = (FileStorage) RestControllerUtils
                  .resolveSpringBean("fileStorage", servletContext);

            // file path
            String fileName = fileInfo.name;
            
            if(fileName.lastIndexOf("\\") > 0 ){
               fileName = fileName.substring(fileName.lastIndexOf("\\") + 1, fileName.length()); 
            }
                
            
            String filePath = fileStorage.getStoragePath(servletContext) + fileName;
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

            // Store file path in session
            String uuid = fileStorage.pushPath(filePath);

            // get document icon
            MimeTypesHelper mimeTypesHelper = (MimeTypesHelper) RestControllerUtils.resolveSpringBean(
                  "ippMimeTypesHelper", servletContext);
            MIMEType mType = mimeTypesHelper.detectMimeTypeI(fileInfo.name, fileInfo.contentType);

            String docIcon = DOC_PATH + "mime-types/" + mType.getIconPath();

            JsonObject jsonObj = new JsonObject();
            jsonObj.add("uuid", new JsonPrimitive(uuid));
            jsonObj.add("contentType", new JsonPrimitive(fileInfo.contentType));
            jsonObj.add("fileName", new JsonPrimitive(fileInfo.name));
            jsonObj.add("docIcon", new JsonPrimitive(docIcon));
            
            response.add(jsonObj);
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
      }

      return Response.ok(response.toString()).build();
   }
   

}
