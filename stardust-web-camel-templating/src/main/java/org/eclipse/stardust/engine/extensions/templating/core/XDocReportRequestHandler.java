package org.eclipse.stardust.engine.extensions.templating.core;

import static org.eclipse.stardust.engine.extensions.templating.core.Util.isClassPathOrRepositoryLocation;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.camel.CamelContext;
import org.eclipse.stardust.engine.extensions.templating.enricher.VelocityContextAppenderProcessor;

public class XDocReportRequestHandler implements IRequestHandler
{
   private XDocReportHandler xdocReportEngine;

   private VelocityContextAppenderProcessor appender;

   public XDocReportRequestHandler(CamelContext camelContext,
         VelocityContextAppenderProcessor appender)
   {
      this.xdocReportEngine = new XDocReportHandler(camelContext.getClassResolver());
      this.appender = appender;
   }

   /**
    * Propagates the call to XDocReportHandler.
    * 
    * @param TemplatingRequest
    *           request
    * @return byte[] as output of templating engine execution
    */
   @Override
   public byte[] handleRequest(TemplatingRequest request) throws ServiceException
   {
      xdocReportEngine.setCustomVelocityContextAppender(appender);
      if (isClassPathOrRepositoryLocation(request))
      {
         return xdocReportEngine.handleClassPathOrRepositoryRequest(
               request.getTemplateUri(), request.getFormat(), request.isConvertToPdf(),
               request.getParameters(), request.getFieldsMetaData());
      }
      else
      {
         InputStream in = new ByteArrayInputStream(request.getXdocContent());
         return xdocReportEngine.handleInputStreamRequest(in, request.getFormat(),
               request.isConvertToPdf(), request.getParameters(),
               request.getFieldsMetaData());
      }
   }
}
