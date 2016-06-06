package org.eclipse.stardust.engine.extensions.templating.core.velocity;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.camel.impl.DefaultCamelContext;
import org.eclipse.stardust.engine.extensions.templating.core.RequestHandler;
import org.eclipse.stardust.engine.extensions.templating.core.ServiceException;
import org.eclipse.stardust.engine.extensions.templating.core.TemplatingRequest;
import org.eclipse.stardust.engine.extensions.templating.enricher.VelocityContextAppenderProcessor;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class TestEmbeddedTemplatingHandler
{
   private static ClassPathXmlApplicationContext ctx;

   private static Date currentDate = null;

   private static TemplatingRequest requestForTestEmbeddedTextText;

   private static TemplatingRequest requestForTestEmbeddedTextPdf;

   private static RequestHandler handler=new RequestHandler(new DefaultCamelContext());

   @BeforeClass
   public static void beforeClass()
   {
      ctx = new ClassPathXmlApplicationContext(new String[] {
            "classpath:META-INF/spring/templating-application-context.xml"});
      currentDate = new Date();
      requestForTestEmbeddedTextText = initRequest(false);
      requestForTestEmbeddedTextPdf = initRequest(true);
   }

   private static TemplatingRequest initRequest(boolean convertToPdf)
   {
      TemplatingRequest request = new TemplatingRequest();
      request.setTemplate(
            "Hello $personInput.firstName $personInput.lastName $date.format('dd/MM/yyyy',$personInput.dob)");
      request.setConvertToPdf(convertToPdf);
      request.setFormat("text");
      Map<String, Object> parameters = new HashMap<String, Object>();
      Map<String, Object> ref = new HashMap<String, Object>();
      ref.put("firstName", "John");
      ref.put("lastName", "Smith");
      ref.put("dob", currentDate);
      parameters.put("personInput", ref);
      request.setParameters(parameters);
      return request;
   }

   @Test
   public void testEmbeddedTextText() throws ServiceException
   {
      byte[] response = handler.handleRequest(requestForTestEmbeddedTextText, VelocityContextAppenderProcessor.initializeVelocityContext("default-velocity-tools.xml"));
      assertNotNull(response != null);
      assertTrue(new String(response).equalsIgnoreCase("Hello John Smith "
            + new SimpleDateFormat("dd/MM/yyyy").format(currentDate)));
   }

   @SuppressWarnings("resource")
   @Test
   public void testEmbeddedTextPdf() throws ServiceException, IOException
   {
      byte[] response = handler.handleRequest(requestForTestEmbeddedTextPdf,VelocityContextAppenderProcessor.initializeVelocityContext("default-velocity-tools.xml"));
      assertNotNull(response != null);
      File f = new File("./target/testEmbeddedTextPdf.pdf");
      FileOutputStream fos = new FileOutputStream(f);
      fos.write(response);
   }
}
