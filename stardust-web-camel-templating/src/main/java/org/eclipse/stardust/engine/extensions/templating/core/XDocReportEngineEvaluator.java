package org.eclipse.stardust.engine.extensions.templating.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.xwpf.converter.core.IXWPFConverter;
import org.apache.poi.xwpf.converter.pdf.PdfConverter;
import org.apache.poi.xwpf.converter.pdf.PdfOptions;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.velocity.VelocityContext;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.extensions.templating.imageprovider.HttpImageProvider;
import org.eclipse.stardust.engine.extensions.templating.imageprovider.JCRImageProvider;
import org.eclipse.stardust.engine.extensions.templating.imageprovider.LocalImageProvider;
import org.eclipse.stardust.engine.extensions.velocity.tool.UserServiceTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.opensagres.xdocreport.core.XDocReportException;
import fr.opensagres.xdocreport.document.IXDocReport;
import fr.opensagres.xdocreport.document.images.ByteArrayImageProvider;
import fr.opensagres.xdocreport.document.images.ClassPathImageProvider;
import fr.opensagres.xdocreport.document.images.IImageProvider;
import fr.opensagres.xdocreport.document.registry.XDocReportRegistry;
import fr.opensagres.xdocreport.template.IContext;
import fr.opensagres.xdocreport.template.TemplateEngineKind;
import fr.opensagres.xdocreport.template.formatter.FieldsMetadata;
import fr.opensagres.xdocreport.template.formatter.NullImageBehaviour;

public class XDocReportEngineEvaluator
{
   protected final Logger log = LoggerFactory.getLogger(getClass());

   private static XDocReportRegistry docRegistry;
   private static IXWPFConverter<PdfOptions> pdfConverter;
   private static PdfOptions options;
   {
      docRegistry = XDocReportRegistry.getRegistry();
      pdfConverter= PdfConverter.getInstance();
      options = PdfOptions.create();
   }

   private IContext loadContext(VelocityContext customVelocityContext)
   {
      IContext context = null;
      Map<String, Object> contextMap = new HashMap<String, Object>();

      for (Object key : customVelocityContext.getKeys())
      {
         contextMap.put(((String) key), customVelocityContext.get(((String) key)));
      }
      context = new org.eclipse.stardust.engine.extensions.templating.component.XDocVelocityContext(
            customVelocityContext);
      return context;
   }

   public byte[] evaluate(InputStream in, boolean convertToPdf,
         Map<String, Object> parameters, List<FieldMetaData> fieldsMetaData, VelocityContext customVelocityContext)
               throws ServiceException
   // throws IOException, XDocReportException
   {
      try
      {
         IContext context = loadContext(customVelocityContext);

         ByteArrayOutputStream out = new ByteArrayOutputStream();
         TemplateEngineKind kind = TemplateEngineKind.Velocity;
         IXDocReport report;

         report = docRegistry.loadReport(in, kind);

         FieldsMetadata metadata = report.createFieldsMetadata();
         if (fieldsMetaData != null && !fieldsMetaData.isEmpty())
         {
            for (FieldMetaData entry : fieldsMetaData)
            {
               metadata.addFieldAsImage(entry.getName(),
                     NullImageBehaviour.RemoveImageTemplate, entry.isUseImageSize());
               context.put(entry.getName(), getImageProvider(entry));
            }
         }

         for (String key : parameters.keySet())
         {
            if (context.get(key) != null)
            {
               if (log.isDebugEnabled())
                  log.debug("entry <" + key
                        + "> exits alreay in the context and will be overriden.");
            }
            context.put(key, parameters.get(key));
         }

         UserServiceTool userService = (UserServiceTool) context.get("user");
         if (userService != null)
         {
            try
            {
               byte[] signatureContent = userService.getSignature();
               metadata.addFieldAsImage(userService.getSignatureKey(),
                     NullImageBehaviour.RemoveImageTemplate, true);
               ByteArrayImageProvider imageProvider = new ByteArrayImageProvider(
                     signatureContent);
               imageProvider.setUseImageSize(true);
               context.put(userService.getSignatureKey(), imageProvider);
            }
            catch (RuntimeException re)
            {
               log.warn("Failed to retrieve or initialize user context.", re);
            }
         }

         report.process(context, out);

         if (convertToPdf)
         {
            InputStream pdfIs = new ByteArrayInputStream(
                  ((ByteArrayOutputStream) out).toByteArray());
            ByteArrayOutputStream pdfOut = new ByteArrayOutputStream();
            XWPFDocument document = new XWPFDocument(pdfIs);
            pdfConverter.convert(document, pdfOut, options);
            pdfIs.close();
            pdfOut.close();
            return pdfOut.toByteArray();
         }
         // out.close();
         return out.toByteArray();
      }
      catch (IOException e)
      {
         throw new ServiceException(e);
      }
      catch (XDocReportException e)
      {
         throw new ServiceException(e);
      }
   }

   private IImageProvider getImageProvider(FieldMetaData entry) throws IOException
   {
      if (StringUtils.isNotEmpty(entry.getLocation()) && entry.getLocation().startsWith("classpath://"))
         return new ClassPathImageProvider(this.getClass().getClassLoader(),
               entry.getLocation().replace("classpath://", ""), true);
      if (StringUtils.isNotEmpty(entry.getLocation()) 
            && entry.getLocation().startsWith("repository://"))
         return new JCRImageProvider(entry.getLocation().replace("repository://", ""));
      if (StringUtils.isNotEmpty(entry.getLocation()) 
            && entry.getLocation().startsWith("file://"))
         return new LocalImageProvider(entry.getLocation().replace("file://", ""));
      if (StringUtils.isNotEmpty(entry.getLocation()) 
            && (entry.getLocation().startsWith("http://")
                  || entry.getLocation().startsWith("https://")))
         return new HttpImageProvider(entry.getLocation());
      return null;

   }
}
