package org.eclipse.stardust.engine.extensions.templating.core;

import static org.eclipse.stardust.engine.extensions.templating.core.Util.getServiceFactory;
import static org.eclipse.stardust.engine.extensions.templating.core.Util.isDocx;
import static org.eclipse.stardust.engine.extensions.templating.core.Util.isValidDocumentRepositoryId;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response.Status;

import org.apache.camel.CamelContext;
import org.apache.velocity.VelocityContext;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;

public class RequestHandler
{
   private CamelContext camelContext;

   private final Logger logger = LogManager.getLogger(RequestHandler.class);

   public RequestHandler(CamelContext camelContext)
   {
      this.camelContext = camelContext;
   }

   /**
    * Method Used to register IN DataPath as Input parameters. if the same ID is used as
    * In Data Mapping and IN DataPath; then the data Mapping value will be used.
    * 
    * @param request
    * @param dataPaths
    */
   private void registerDataPaths(TemplatingRequest request,
         Map<String, Serializable> dataPaths)
   {
      if (request.getParameters() == null)
      {
         request.setParameters(new HashMap<String, Object>());
      }

      for (String key : dataPaths.keySet())
      {
         if (!request.getParameters().containsKey(key))
            request.getParameters().put(key, dataPaths.get(key));
         else
         {
            if (logger.isDebugEnabled())
               logger.debug("Duplicate IDs detected for entry " + key
                     + ". The Data Path value will not be skipped.");
         }
      }
   }

   public byte[] handleRequest(TemplatingRequest request, VelocityContext velocityContext) throws ServiceException
   {
      if(logger.isDebugEnabled())
         logger.debug("-->handleRequest"+ request);
      
      ServiceFactory sf = getServiceFactory();
      validate(request);
      if (request.getProcessOid() != null)
      {
         if (logger.isDebugEnabled())
            logger.debug(
                  "Registering data paths for process OID <" + request.getProcessOid()+">.");
         Map<String, Serializable> dataPaths = sf.getWorkflowService()
               .getInDataPaths(request.getProcessOid(), null);
         registerDataPaths(request, dataPaths);
      }
      if(logger.isDebugEnabled())
         logger.debug("<--handleRequest");
      return dispatch(request, velocityContext);
   }

   private byte[] dispatch(TemplatingRequest request, VelocityContext velocityContext) throws ServiceException
   {
      if(logger.isDebugEnabled())
         logger.debug("-->dispatch: "+request);
      IRequestHandler handler;
      if (isDocx(request))
      {
         handler = new XDocReportRequestHandler(camelContext);
         if(logger.isDebugEnabled())
            logger.debug("The request is dispatched to XDocReportRequestHandler");
      }
      else
      {
         handler = new VelocityRequestHandler(camelContext);
         if(logger.isDebugEnabled())
            logger.debug("The request is dispatched to VelocityRequestHandler");
      }
      if(logger.isDebugEnabled())
         logger.debug("<--dispatch");
      return handler.handleRequest(request,velocityContext);
   }

   protected void validate(TemplatingRequest request)
   {
      if(logger.isDebugEnabled())
         logger.debug("-->validate: "+request);
      if (request == null)
         throw new ValidationException(Status.BAD_REQUEST.getStatusCode(),
               "The request cannot be null");

      if (StringUtils.isEmpty(request.getFormat()))
         throw new ValidationException(Status.BAD_REQUEST.getStatusCode(),
               "format cannot be empty, please provide one of the following values text|html|xml|docx.");
      else
      {
         if ((!request.getFormat().equals("text"))
               && (!request.getFormat().equals("html"))
               && (!request.getFormat().equals("xml"))
               && (!request.getFormat().equals("docx")))
            throw new ValidationException(Status.BAD_REQUEST.getStatusCode(),
                  "Invalid value for Format field. please provide one of the following values text|html|xml|docx.");
      }
      if (StringUtils.isNotEmpty(request.getTemplateUri())
            && !isValidDocumentRepositoryId(request.getTemplateUri()))
      {
         if (request.getFormat().equals("docx")
               && !request.getTemplateUri().endsWith(".docx"))
         {
            throw new ValidationException(Status.BAD_REQUEST.getStatusCode(),
                  "Invalid template Name. please provide a valid MS Word File.");
         }
         if (!request.getFormat().equals("docx")
               && request.getTemplateUri().endsWith(".docx"))
         {
            throw new ValidationException(Status.BAD_REQUEST.getStatusCode(),
                  "Invalid template configuration. please provide a valid format.");
         }

         if (!StringUtils.isEmpty(request.getTemplate())
               && !StringUtils.isEmpty(request.getTemplateUri()))
            throw new ValidationException(Status.BAD_REQUEST.getStatusCode(),
                  "Invalid request. The request contains a Template URI and Template content");

         if (!StringUtils.isEmpty(request.getTemplateUri()))
            if ((!request.getTemplateUri().startsWith("classpath://"))
                  && (!request.getTemplateUri().startsWith("repository://"))
                  && (!request.getTemplateUri().startsWith("http://")))
               throw new ValidationException(Status.BAD_REQUEST.getStatusCode(),
                     "templateUri should start with classpath|repository|http .");
      }
      if(logger.isDebugEnabled())
         logger.debug("<--validate");
   }
}
