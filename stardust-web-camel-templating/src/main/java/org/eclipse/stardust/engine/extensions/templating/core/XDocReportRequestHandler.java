package org.eclipse.stardust.engine.extensions.templating.core;

import static org.eclipse.stardust.engine.extensions.templating.core.Util.isClassPathOrRepositoryLocation;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.camel.CamelContext;
import org.apache.velocity.VelocityContext;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;

public class XDocReportRequestHandler implements IRequestHandler
{
   private XDocReportHandler xdocReportEngine;
   private final Logger logger = LogManager.getLogger(XDocReportRequestHandler.class);
  
   public XDocReportRequestHandler(CamelContext camelContext)
   {
      this.xdocReportEngine = new XDocReportHandler(camelContext.getClassResolver());
   }

   /**
    * Propagates the call to XDocReportHandler.
    * 
    * @param TemplatingRequest
    *           request
    * @return byte[] as output of templating engine execution
    */
   @Override
   public byte[] handleRequest(TemplatingRequest request, VelocityContext velocityContext) throws ServiceException
   {
      if(logger.isDebugEnabled())
         logger.debug("-->handleRequest request="+request);
      if (isClassPathOrRepositoryLocation(request))
      {
         return xdocReportEngine.handleClassPathOrRepositoryRequest(
               request.getTemplateUri(), request.getFormat(), request.isConvertToPdf(),
               request.getParameters(), request.getFieldsMetaData(),velocityContext);
      }
      else
      {
         InputStream in = new ByteArrayInputStream(request.getXdocContent());
         return xdocReportEngine.handleInputStreamRequest(in, request.getFormat(),
               request.isConvertToPdf(), request.getParameters(),
               request.getFieldsMetaData(),velocityContext);
      }
   }
}
