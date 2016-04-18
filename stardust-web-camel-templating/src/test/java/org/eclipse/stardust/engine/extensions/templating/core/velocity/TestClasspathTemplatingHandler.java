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

import org.eclipse.stardust.engine.extensions.templating.core.RequestHandler;
import org.eclipse.stardust.engine.extensions.templating.core.ServiceException;
import org.eclipse.stardust.engine.extensions.templating.core.TemplatingRequest;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class TestClasspathTemplatingHandler
{
   private static ClassPathXmlApplicationContext ctx;

   private static Date currentDate = null;

   private static TemplatingRequest requestForTestClasspathTextText;

   private static TemplatingRequest requestForTestClasspathTextPdf;

   private static RequestHandler handler;

   @BeforeClass
   public static void beforeClass()
   {
      ctx = new ClassPathXmlApplicationContext(new String[] {
            "classpath:META-INF/spring/templating-application-context.xml"});
      currentDate = new Date();
      requestForTestClasspathTextText = initRequest(false);
      requestForTestClasspathTextPdf = initRequest(true);
      handler=(RequestHandler) ctx.getBean("requestHandler");
   }

   private static TemplatingRequest initRequest(boolean convertToPdf)
   {
      TemplatingRequest request = new TemplatingRequest();
      request.setTemplateUri("classpath://custom/templates/simpleVelocityTemplate.vm");
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
   public void testClasspathTextText() throws ServiceException
   {
      byte[] response = handler.handleRequest(requestForTestClasspathTextText);
      assertNotNull(response != null);
      assertTrue(new String(response).trim().equals("Hello John Smith "
            + new SimpleDateFormat("dd/MM/yyyy").format(currentDate)));
   }

   @SuppressWarnings("resource")
   @Test
   public void testClasspathTextPdf() throws ServiceException, IOException
   {
      byte[] response = handler.handleRequest(requestForTestClasspathTextPdf);
      assertNotNull(response != null);
      File f = new File("./target/testClasspathTextPdf.pdf");
      FileOutputStream fos = new FileOutputStream(f);
      fos.write(response);
   }

}
