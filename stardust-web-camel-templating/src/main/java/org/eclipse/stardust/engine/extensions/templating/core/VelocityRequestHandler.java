package org.eclipse.stardust.engine.extensions.templating.core;

import static org.eclipse.stardust.engine.extensions.templating.core.Util.isClassPathOrRepositoryLocation;

import java.io.IOException;

import org.apache.camel.CamelContext;
import org.apache.velocity.VelocityContext;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.extensions.itext.converter.InvalidFormatException;

import com.lowagie.text.DocumentException;

public class VelocityRequestHandler implements IRequestHandler
{
   private final Logger logger = LogManager.getLogger(VelocityRequestHandler.class);
   private VelocityTemplatesHandler templateHandler;

   public VelocityRequestHandler(CamelContext camelContext)
   {
      this.templateHandler = new VelocityTemplatesHandler(
            camelContext.getClassResolver());
   }

   @Override
   public byte[] handleRequest(TemplatingRequest request,VelocityContext velocityContext) throws ServiceException
   {
      if(logger.isDebugEnabled())
         logger.debug("-->handleRequest "+request+", velocityContext:"+velocityContext);
      try
      {
         if (!StringUtils.isEmpty(request.getTemplate()))
         { // Embedded
            return templateHandler.handleEmbeddedTemplate(request.getTemplate(),
                  request.getFormat(), request.isConvertToPdf(), request.getParameters(),velocityContext);
         }
         else if (isClassPathOrRepositoryLocation(request))
         {
            return templateHandler.handleClassPathOrRepositoryRequest(
                  request.getTemplateUri(), request.getFormat(), request.isConvertToPdf(),
                  request.getParameters(),velocityContext);
         }
         else
         {// Data: similar to embedded because it's expected to get the content from the
          // route
            return templateHandler.handleEmbeddedTemplate(request.getTemplate(),
                  request.getFormat(), request.isConvertToPdf(), request.getParameters(),velocityContext);
         }
      }
      catch (DocumentException e)
      {
         logger.error("<--handleRequest",e);
         throw new ServiceException(e);
      }
      catch (IOException e)
      {
         logger.error("<--handleRequest",e);
         throw new ServiceException(e);
      }
      catch (InvalidFormatException e)
      {
         logger.error("<--handleRequest",e);
         throw new ServiceException(e);
      }
   }
}
