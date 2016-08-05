package org.eclipse.stardust.engine.extensions.templating.rest;

import static org.eclipse.stardust.engine.extensions.templating.core.Util.getServiceFactory;

import java.util.HashMap;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.camel.CamelContext;
import org.apache.commons.lang.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.extensions.camel.util.DmsFileArchiver;
import org.eclipse.stardust.engine.extensions.templating.core.RequestHandler;
import org.eclipse.stardust.engine.extensions.templating.core.ServiceException;
import org.eclipse.stardust.engine.extensions.templating.core.TemplatingRequest;
import org.eclipse.stardust.engine.extensions.templating.core.ValidationException;
import org.eclipse.stardust.engine.extensions.templating.enricher.VelocityContextAppenderProcessor;
import org.eclipse.stardust.engine.extensions.json.GsonHandler;

@Path("/")
public class TemplatingRestlet
{
   private final Logger logger = LogManager.getLogger(TemplatingRestlet.class);
   private String velocityToolsPath;
   private CamelContext camelContext;
   
   public void setVelocityToolsPath(String velocityToolsPath)
   {
      this.velocityToolsPath = velocityToolsPath;
   }

   public void setCamelContext(CamelContext camelContext)
   {
      this.camelContext = camelContext;
   }
   
   @POST
   @Path("/")
   @Consumes({"application/json"})
   public Response processRequest(TemplatingRequest request) throws ServiceException
   {
      try
      {
         if(logger.isDebugEnabled()){
            logger.debug("-->processRequest: "+request);
         }
         validateRequest(request);
         RequestHandler requestHadnler=new RequestHandler(this.camelContext);
         byte[] content = requestHadnler.handleRequest(request, VelocityContextAppenderProcessor.initializeVelocityContext(this.velocityToolsPath));
         Document createdDocument = storeDocument(request, content);
         if (createdDocument != null){
            if(logger.isDebugEnabled()){
               logger.debug("<--processRequest");
            }
            return Response.ok(createdDocument.getId()).build();
         }else{
            GsonHandler gson = new GsonHandler();
            if(logger.isDebugEnabled()){
               logger.debug("<--processRequest");
            }
            return Response.ok(gson.toJson(new String(content))).build();
         }
      }
      catch (ValidationException ve)
      {
         logger.error("<--processRequest: "+ve.getMessage());
         return Response.status(ve.getStatusCode()).entity(ve.getMessage()).build();
      }
      catch (RuntimeException re)
      {
         logger.error("<--processRequest: "+re.getMessage());
         return Response.status(Status.INTERNAL_SERVER_ERROR).entity(re.getMessage())
               .build();
      }
   }

   /**
    * Creates a Document in the repository according to the provided Output configuration.
    * Null otherwise.
    * 
    * @param request
    * @param content
    * @return
    */
   private Document storeDocument(TemplatingRequest request, byte[] content)
   {
      if(logger.isDebugEnabled()){
         logger.debug("-->storeDocument: "+request);
      }
      Document createdDocument = null;
      if (request.getOutput() == null)
         request.setOutput(new HashMap<String, Object>());

      String outputFileName = (String) request.getOutput().get("name");
      String outputFileLocation = (String) request.getOutput().get("path");

      if (!StringUtils.isEmpty(outputFileLocation)
            && !StringUtils.isEmpty(outputFileName))
         createdDocument = storeDocumentinSpecificLocation(getServiceFactory(), content,
               outputFileName, outputFileLocation);
      if(logger.isDebugEnabled()){
         logger.debug("<--storeDocument, created documentId:"+createdDocument);
      }
      return createdDocument;
   }

   /**
    * Perform request validation
    * 
    * @param request
    */
   private void validateRequest(TemplatingRequest request)
   {
      if(logger.isDebugEnabled()){
         logger.debug("-->validateRequest: "+request);
      }
      if (request.getOutput() != null && !request.getOutput().isEmpty())
      {
         if (!request.getOutput().containsKey("name"))
            throw new ValidationException(Status.BAD_REQUEST.getStatusCode(),
                  "name parameter is mandatory.");

         if (request.isConvertToPdf()
               && !((String) request.getOutput().get("name")).endsWith(".pdf"))
         {
            throw new ValidationException(Status.BAD_REQUEST.getStatusCode(),
                  "name should end with .pdf");

         }
         else if (((String) request.getOutput().get("name")).endsWith(".pdf")
               && !request.isConvertToPdf())
         {
            throw new ValidationException(Status.BAD_REQUEST.getStatusCode(),
                  "Invalid value provided for Name parameter.");
         }
      }
      if(logger.isDebugEnabled()){
         logger.debug("<--validateRequest");
      }
   }

   private Document storeDocumentinSpecificLocation(ServiceFactory sf, byte[] content,
         String fileName, String path)
   {
      DmsFileArchiver dmsFileArchiver = new DmsFileArchiver(sf);
      Document newDocument = dmsFileArchiver.archiveFile(content, fileName, path);
      return newDocument;
   }
}