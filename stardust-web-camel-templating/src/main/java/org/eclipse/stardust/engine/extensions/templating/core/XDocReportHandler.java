package org.eclipse.stardust.engine.extensions.templating.core;

import static org.eclipse.stardust.engine.extensions.templating.core.Util.composeRepositoryLocationForTemplates;
import static org.eclipse.stardust.engine.extensions.templating.core.Util.getDocumentManagementService;
import static org.eclipse.stardust.engine.extensions.templating.core.Util.getServiceFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.apache.camel.spi.ClassResolver;
import org.apache.camel.util.ResourceHelper;
import org.apache.velocity.VelocityContext;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.extensions.itext.converter.InvalidFormatException;

import com.lowagie.text.DocumentException;

import fr.opensagres.xdocreport.core.XDocReportException;

public class XDocReportHandler
{
   private final Logger logger = LogManager.getLogger(XDocReportHandler.class);
   private ClassResolver resolver;

   public XDocReportHandler(ClassResolver resolver)
   {
      this.resolver = resolver;
   }

   public byte[] handleClassPathOrRepositoryRequest(String templateUri, String format,
         boolean convertToPdf, Map<String, Object> parameters,
         List<FieldMetaData> fieldsMetaData, VelocityContext customVelocityContext) throws ServiceException
   {
      if (templateUri.startsWith("classpath"))
      {
         return handleClassPathTemplate(templateUri, format, convertToPdf, parameters,
               fieldsMetaData,customVelocityContext);
      }
      else if (templateUri.startsWith("repository"))
      {
         return handleRepositoryTemplate(templateUri, format, convertToPdf, parameters,
               fieldsMetaData,customVelocityContext);
      }
      else if (templateUri.startsWith("http"))
      {
         return handleHttpVfsRequest(templateUri, format, convertToPdf, parameters,
               fieldsMetaData);
      }
      return null;
   }

   public byte[] handleInputStreamRequest(InputStream in, String format,
         boolean convertToPdf, Map<String, Object> parameters,
         List<FieldMetaData> fieldsMetaData, VelocityContext customVelocityContext) throws ServiceException
   {
      XDocReportEngineEvaluator xdocReportEngine = new XDocReportEngineEvaluator();
      return xdocReportEngine.evaluate(in, convertToPdf, parameters, fieldsMetaData,customVelocityContext);
   }

   /**
    * 
    * @param templateUri
    * @param format
    * @param convertToPdf
    * @param parameters
    * @return
    * @throws ServiceException
    * @throws DocumentException
    * @throws IOException
    * @throws InvalidFormatException
    * @throws XDocReportException
    */
   private byte[] handleRepositoryTemplate(String templateUri, String format,
         boolean convertToPdf, Map<String, Object> parameters,
         List<FieldMetaData> fieldsMetaData, VelocityContext customVelocityContext) throws ServiceException
   {
      if(logger.isDebugEnabled())
         logger.debug("-->handleRepositoryTemplate templateUri="+templateUri+", format="+format+", convertToPdf="+convertToPdf+", parameters="+parameters);
      byte[] response;
      ServiceFactory sf = getServiceFactory();
      DocumentManagementService dms = getDocumentManagementService(sf);
      String templateLocation = composeRepositoryLocationForTemplates(
            templateUri.replace("repository://", ""));
      byte[] content = dms.retrieveDocumentContent(templateLocation);
      if (content.length == 0)
         throw new RuntimeException("File " + templateLocation + " was not found.");
      response = handleInputStreamRequest(new ByteArrayInputStream(content), format,
            convertToPdf, parameters, fieldsMetaData, customVelocityContext);
      if(logger.isDebugEnabled())
         logger.debug("<--handleRepositoryTemplate");
      return response;
   }

   private byte[] handleClassPathTemplate(String templateUri, String format,
         boolean convertToPdf, Map<String, Object> parameters,
         List<FieldMetaData> fieldsMetaData, VelocityContext customVelocityContext) throws ServiceException
   {
      if(logger.isDebugEnabled())
         logger.debug("-->handleClassPathTemplate templateUri="+templateUri+", format="+format+", convertToPdf="+convertToPdf+", parameters="+parameters);
      byte[] response;
      InputStream is;
      try
      {
         is = ResourceHelper.resolveMandatoryResourceAsInputStream(resolver, templateUri);

         response = handleInputStreamRequest(is, format, convertToPdf, parameters,
               fieldsMetaData,customVelocityContext);
         if(logger.isDebugEnabled())
            logger.debug("<--handleClassPathTemplate");
         return response;
      }
      catch (IOException e)
      {
         logger.error("<--handleRepositoryTemplate",e);
         throw new ServiceException(e);
      }
   }

   private byte[] handleHttpVfsRequest(String templateUri, String format,
         boolean convertToPdf, Map<String, Object> parameters,
         List<FieldMetaData> fieldsMetaData)

   {
      byte[] response = null;
      return response;
   }
}