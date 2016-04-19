package org.eclipse.stardust.engine.extensions.templating.core;

import static org.eclipse.stardust.engine.extensions.templating.core.Util.isClassPathOrRepositoryLocation;

import java.io.IOException;

import org.apache.camel.CamelContext;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.extensions.itext.converter.InvalidFormatException;
import org.eclipse.stardust.engine.extensions.templating.enricher.VelocityContextAppenderProcessor;

import com.lowagie.text.DocumentException;

public class VelocityRequestHandler implements IRequestHandler
{
   private VelocityTemplatesHandler templateHandler;

   private VelocityContextAppenderProcessor appender;

   public VelocityRequestHandler(CamelContext camelContext,
         VelocityContextAppenderProcessor appender)
   {
      this.templateHandler = new VelocityTemplatesHandler(
            camelContext.getClassResolver());
      this.appender = appender;
   }

   @Override
   public byte[] handleRequest(TemplatingRequest request) throws ServiceException
   {
      templateHandler.setVelocityContext(appender.getVelocityContext());
      
      try
      {
         if (!StringUtils.isEmpty(request.getTemplate()))
         { // Embedded
            return templateHandler.handleEmbeddedTemplate(request.getTemplate(),
                  request.getFormat(), request.isConvertToPdf(), request.getParameters());
         }
         else if (isClassPathOrRepositoryLocation(request))
         {
            return templateHandler.handleClassPathOrRepositoryRequest(
                  request.getTemplateUri(), request.getFormat(), request.isConvertToPdf(),
                  request.getParameters());
         }
         else
         {// Data: similar to embedded because it's expected to get the content from the
          // route
            return templateHandler.handleEmbeddedTemplate(request.getTemplate(),
                  request.getFormat(), request.isConvertToPdf(), request.getParameters());
         }
      }
      catch (DocumentException e)
      {
         throw new ServiceException(e);
      }
      catch (IOException e)
      {
         throw new ServiceException(e);
      }
      catch (InvalidFormatException e)
      {
         throw new ServiceException(e);
      }
   }
}
