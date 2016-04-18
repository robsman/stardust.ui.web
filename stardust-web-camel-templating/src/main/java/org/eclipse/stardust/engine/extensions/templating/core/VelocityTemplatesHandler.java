package org.eclipse.stardust.engine.extensions.templating.core;

import static org.eclipse.stardust.engine.extensions.templating.core.Util.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Map;

import org.apache.camel.spi.ClassResolver;
import org.apache.camel.util.IOHelper;
import org.apache.camel.util.ResourceHelper;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lowagie.text.DocumentException;

import com.sungard.infinity.integration.itext.converter.InvalidFormatException;
import com.sungard.infinity.integration.itext.converter.StringToPdfConverter;

import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;

public class VelocityTemplatesHandler
{
   protected final Logger log = LoggerFactory.getLogger(getClass());

   private VelocityEngineEvaluator engine;

   private ClassResolver resolver;

   private VelocityContext velocityContext;

   public void setVelocityContext(VelocityContext velocityContext)
   {
      this.velocityContext = velocityContext;
   }

   public VelocityTemplatesHandler(ClassResolver resolver)
   {
      this.engine = new VelocityEngineEvaluator(resolver);
      this.resolver = resolver;
   }

   public byte[] handleClassPathOrRepositoryRequest(String templateUri, String format,
         boolean convertToPdf, Map<String, Object> parameters)
               throws DocumentException, IOException, InvalidFormatException
   {
      if (templateUri.startsWith("classpath"))
      {
         return handleClassPathTemplate(templateUri, format, convertToPdf, parameters);
      }
      else if (templateUri.startsWith("repository"))
      {
         return handleRepositoryTemplate(templateUri, format, convertToPdf, parameters);
      }
      else if (templateUri.startsWith("http"))
      {
         return handleHttpVfsRequest(templateUri, format, convertToPdf, parameters);
      }
      return null;
   }

   /**
    * Handle requests containing the template location as http://...
    * 
    * @param templateUri
    * @param format
    * @param convertToPdf
    * @param parameters
    * @return
    * @throws DocumentException
    * @throws IOException
    * @throws InvalidFormatException
    */
   private byte[] handleHttpVfsRequest(String templateUri, String format,
         boolean convertToPdf, Map<String, Object> parameters)
               throws DocumentException, IOException, InvalidFormatException
   {
      byte[] response = null;
      registerParametersinVelocityContext(parameters);
      return response;
   }

   /**
    * 
    * @param templateUri
    * @param format
    * @param convertToPdf
    * @param parameters
    * @return
    * @throws DocumentException
    * @throws IOException
    * @throws InvalidFormatException
    */
   private byte[] handleRepositoryTemplate(String templateUri, String format,
         boolean convertToPdf, Map<String, Object> parameters)
               throws DocumentException, IOException, InvalidFormatException
   {
      byte[] response = null;
      registerParametersinVelocityContext(parameters);
      ServiceFactory sf = getServiceFactory();
      DocumentManagementService dms = sf.getDocumentManagementService();
      String templateLocation = composeRepositoryLocationForTemplates(
            templateUri.replace("repository://", ""));
      byte[] content = dms.retrieveDocumentContent(templateLocation);
      if (content.length == 0)
         throw new RuntimeException("File " + templateLocation + " is not found.");

      StringWriter buffer = engine.evaluate(
            appendCustomMacros(new String(content)).toString(), velocityContext);
      if (convertToPdf)
         response = StringToPdfConverter.convertToPdf(format,
               buffer.toString().getBytes());
      else
         response = buffer.toString().getBytes();
      return response;
   }

   /**
    * 
    * @param content
    *           : template content.
    * @param format
    *           : text|html|xml
    * @param convertToPdf
    *           : true|flase
    * @param parameters
    *           :
    * @throws InvalidFormatException
    * @throws IOException
    * @throws DocumentException
    */
   private byte[] handleClassPathTemplate(String templateUri, String format,
         boolean convertToPdf, Map<String, Object> parameters)
               throws DocumentException, IOException, InvalidFormatException
   {
      byte[] response = null;

      registerParametersinVelocityContext(parameters);
      InputStream is = ResourceHelper.resolveMandatoryResourceAsInputStream(resolver,
            templateUri);
      StringWriter buffer = engine.evaluate(
            appendCustomMacros(IOHelper.loadText(is)).toString(), velocityContext);
      if (convertToPdf)
         response = StringToPdfConverter.convertToPdf(format,
               buffer.toString().getBytes());
      else
         response = buffer.toString().getBytes();
      return response;
   }

   /**
    * 
    * @param content
    *           : template content.
    * @param format
    *           : text|html|xml
    * @param convertToPdf
    *           : true|flase
    * @param parameters
    *           :
    * @throws InvalidFormatException
    * @throws IOException
    * @throws DocumentException
    */
   public byte[] handleEmbeddedTemplate(String content, String format,
         boolean convertToPdf, Map<String, Object> parameters)
               throws DocumentException, IOException, InvalidFormatException
   {
      byte[] response;

      registerParametersinVelocityContext(parameters);
      StringWriter buffer = engine.evaluate(appendCustomMacros(content).toString(),
            velocityContext);
      if (convertToPdf)
         response = StringToPdfConverter.convertToPdf(format,
               buffer.toString().getBytes());
      else
         response = buffer.toString().getBytes();
      return response;
   }

   private void registerParametersinVelocityContext(Map<String, Object> parameters)
   {
      for (String key : parameters.keySet())
      {
         velocityContext.put(key, parameters.get(key));
         if (log.isDebugEnabled())
            log.debug("registering " + key + " in velocity context.");
      }
   }

   /**
    * getInputs and setOutputs macros will generate variables according to the provided
    * parameters.
    * 
    * @param content
    * @return
    */
   private StringBuilder appendCustomMacros(String content)
   {
      StringBuilder templateContent = new StringBuilder();
      if (!StringUtils.isEmpty(content))
      {
         templateContent.append("#parse(\"commons.vm\")\n");
         templateContent.append("#getInputs()\n");
         templateContent.append(content);
         templateContent.append("#setOutputs()");
      }
      if (log.isDebugEnabled())
         log.debug("Template content: " + templateContent);
      return templateContent;
   }
}