package org.eclipse.stardust.engine.extensions.templating.core;

import static org.eclipse.stardust.engine.extensions.templating.core.Util.*;

import java.io.*;
import java.util.List;
import java.util.Map;

import org.apache.camel.spi.ClassResolver;
import org.apache.camel.util.ResourceHelper;

import com.lowagie.text.DocumentException;

import fr.opensagres.xdocreport.core.XDocReportException;
import fr.opensagres.xdocreport.template.IContext;
import fr.opensagres.xdocreport.template.formatter.FieldsMetadata;

import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.extensions.itext.converter.InvalidFormatException;
import org.eclipse.stardust.engine.extensions.templating.enricher.VelocityContextAppenderProcessor;

public class XDocReportHandler
{
   private ClassResolver resolver;

   private XDocReportEngineEvaluator xdocReportEngine = new XDocReportEngineEvaluator();

   public XDocReportHandler(ClassResolver resolver)
   {
      this.resolver = resolver;
   }

   public void setCustomContext(IContext customContext)
   {
      this.xdocReportEngine.setCustomContext(customContext);
   }

   public void setCustomVelocityContextAppender(
         VelocityContextAppenderProcessor customVelocityContextAppender)
   {
      this.xdocReportEngine
            .setCustomVelocityContextAppender(customVelocityContextAppender);
   }

   public void setFieldsMetaData(FieldsMetadata fieldsMetaData)
   {
      this.xdocReportEngine.setFieldsMetaData(fieldsMetaData);
   }

   public byte[] handleClassPathOrRepositoryRequest(String templateUri, String format,
         boolean convertToPdf, Map<String, Object> parameters,
         List<FieldMetaData> fieldsMetaData) throws ServiceException
   {
      if (templateUri.startsWith("classpath"))
      {
         return handleClassPathTemplate(templateUri, format, convertToPdf, parameters,
               fieldsMetaData);
      }
      else if (templateUri.startsWith("repository"))
      {
         return handleRepositoryTemplate(templateUri, format, convertToPdf, parameters,
               fieldsMetaData);
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
         List<FieldMetaData> fieldsMetaData) throws ServiceException
   {
      return this.xdocReportEngine.evaluate(in, convertToPdf, parameters, fieldsMetaData);
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
         List<FieldMetaData> fieldsMetaData) throws ServiceException
   {
      byte[] response;
      ServiceFactory sf = getServiceFactory();
      DocumentManagementService dms = getDocumentManagementService(sf);
      String templateLocation = composeRepositoryLocationForTemplates(
            templateUri.replace("repository://", ""));
      byte[] content = dms.retrieveDocumentContent(templateLocation);
      if (content.length == 0)
         throw new RuntimeException("File " + templateLocation + " was not found.");
      response = handleInputStreamRequest(new ByteArrayInputStream(content), format,
            convertToPdf, parameters, fieldsMetaData);
      return response;
   }

   private byte[] handleClassPathTemplate(String templateUri, String format,
         boolean convertToPdf, Map<String, Object> parameters,
         List<FieldMetaData> fieldsMetaData) throws ServiceException
   {
      byte[] response;
      InputStream is;
      try
      {
         is = ResourceHelper.resolveMandatoryResourceAsInputStream(resolver, templateUri);

         response = handleInputStreamRequest(is, format, convertToPdf, parameters,
               fieldsMetaData);
         return response;
      }
      catch (IOException e)
      {
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