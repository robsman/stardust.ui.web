package org.eclipse.stardust.engine.extensions.templating.core.xdocreport;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.engine.extensions.templating.core.FieldMetaData;
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
   private static RequestHandler handler;
   private static TemplatingRequest requestForTestClasspathPlainDocx;

   private static TemplatingRequest requestForTestClasspathDocxPdf;
   @BeforeClass
   public static void beforeClass()
   {
      ctx = new ClassPathXmlApplicationContext(
            new String[] {"classpath:META-INF/spring/templating-application-context.xml"});
      currentDate = new Date();
      requestForTestClasspathPlainDocx=initRequest(false);
      requestForTestClasspathDocxPdf=initRequest(true);
      handler=(RequestHandler) ctx.getBean("requestHandler");
   }
   private static TemplatingRequest initRequest(boolean convertToPdf)
   {
      TemplatingRequest request = new TemplatingRequest();
      request.setTemplateUri("classpath://custom/templates/simpleDocxTemplate.docx");
      request.setConvertToPdf(convertToPdf);
      request.setFormat("docx");
      Map<String, Object> parameters = new HashMap<String, Object>();
      Map<String, Object> ref = new HashMap<String, Object>();
      ref.put("firstName", "John");
      ref.put("lastName", "Smith");
      ref.put("dob", currentDate);
      parameters.put("personInput", ref);
      request.setParameters(parameters);
      return request;
   }
   
   @SuppressWarnings("resource")
   @Test
   public void testClasspathPlainDocx() throws ServiceException, IOException
   {
      byte[] response = handler.handleRequest(requestForTestClasspathPlainDocx);
      File f = new File("./target/testClasspathPlainDocx.docx");
      FileOutputStream fos = new FileOutputStream(f);
      fos.write(response);
   }
   
   @SuppressWarnings("resource")
   @Test
   public void testClasspathDocxPdf() throws ServiceException, IOException
   {
      byte[] response = handler.handleRequest(requestForTestClasspathDocxPdf);
      File f = new File("./target/testClasspathDocxPdf.pdf");
      FileOutputStream fos = new FileOutputStream(f);
      fos.write(response);
   }
   @SuppressWarnings("resource")
   @Test(expected=java.lang.RuntimeException.class)
   public void testForCRNT36345() throws ServiceException, IOException
   {
      TemplatingRequest request = new TemplatingRequest();
      request.setTemplateUri("classpath://custom/templates/SUNChoice Letterhead.docx");
      request.setConvertToPdf(true);
      request.setFormat("docx");
      Map<String, Object> parameters = new HashMap<String, Object>();

      Map<String, Object> Follow_UpLetterDetails = new HashMap<String, Object>();
      Follow_UpLetterDetails.put("Accomodations","abc");
      
      parameters.put("Follow_UpLetterDetails", Follow_UpLetterDetails);
      request.setParameters(parameters);
      
      byte[] response = handler.handleRequest(request);
      File f = new File("./target/SUNChoice Letterhead updated.pdf");
      FileOutputStream fos = new FileOutputStream(f);
      fos.write(response);
   }

   @SuppressWarnings("resource")
   @Test
   public void testTemplateWithImage() throws ServiceException, IOException
   {
      TemplatingRequest request = new TemplatingRequest();
      request.setTemplateUri("classpath://custom/templates/user-greetings-with-signature.docx");
      request.setConvertToPdf(false);
      request.setFormat("docx");
      Map<String, Object> parameters = new HashMap<String, Object>();
      Map<String, Object> person = new HashMap<String, Object>();
      person.put("firstName", "abc");
      person.put("lastName", "abc");
      parameters.put("person", person);
      
      List<FieldMetaData> fieldsMetaData=new ArrayList<FieldMetaData>();
      Map<String, Object> entry = new HashMap<String, Object>();
      entry.put("name","signature");
      entry.put("location","classpath://org/eclipse/stardust/engine/extensions/templating/core/xdocreport/sungard.png");
      fieldsMetaData.add(new FieldMetaData(entry));
      request.setFieldsMetaData(fieldsMetaData);

      request.setParameters(parameters);
      
      byte[] response = handler.handleRequest(request);
      File f = new File("./target/user-greetings-with-signature.docx");
      FileOutputStream fos = new FileOutputStream(f);
      fos.write(response);
   }
   @SuppressWarnings("resource")
   @Test
   public void testTemplateWithImageAndConvertToPDF() throws ServiceException, IOException  
   {
      TemplatingRequest request = new TemplatingRequest();
      request.setTemplateUri("classpath://custom/templates/user-greetings-with-signature.docx");
      request.setConvertToPdf(true);
      request.setFormat("docx");
      Map<String, Object> parameters = new HashMap<String, Object>();
      Map<String, Object> person = new HashMap<String, Object>();
      person.put("firstName", "abc");
      person.put("lastName", "abc");
      parameters.put("person", person);
      
      List<FieldMetaData> fieldsMetaData=new ArrayList<FieldMetaData>();
      Map<String, Object> entry = new HashMap<String, Object>();
      entry.put("name","signature");
      entry.put("location","classpath://org/eclipse/stardust/engine/extensions/templating/core/xdocreport/sungard.png");
      fieldsMetaData.add(new FieldMetaData(entry));
      request.setFieldsMetaData(fieldsMetaData);

      request.setParameters(parameters);
      
      byte[] response = handler.handleRequest(request);
      File f = new File("./target/user-greetings-with-signature.pdf");
      FileOutputStream fos = new FileOutputStream(f);
      fos.write(response);
   }

   @SuppressWarnings("resource")
   @Test(expected=java.lang.RuntimeException.class)
   public void tes2tForCRNT36345() throws ServiceException, IOException
   {
      TemplatingRequest request = new TemplatingRequest();
      request.setTemplateUri("classpath://custom/templates/docxLetter.docx");
      request.setConvertToPdf(true);
      request.setFormat("docx");
      Map<String, Object> parameters = new HashMap<String, Object>();

      Map<String, Object> person = new HashMap<String, Object>();
      person.put("FirstName","abc");
      person.put("LastName","efg");
      person.put("city","Paris");
      person.put("date",new Date());
      
      parameters.put("Person", person);
      request.setParameters(parameters);
      
      byte[] response = handler.handleRequest(request);
      File f = new File("./target/docxLetter.pdf");
      FileOutputStream fos = new FileOutputStream(f);
      fos.write(response);
   }
   
   @SuppressWarnings("resource")
   @Test
   public void tes3tForCRNT36345() throws ServiceException, IOException
   {
      TemplatingRequest request = new TemplatingRequest();
      request.setTemplateUri("classpath://custom/templates/CD008-CAL_Final_liability.docx");
      request.setConvertToPdf(true);
      request.setFormat("docx");
      Map<String, Object> parameters = new HashMap<String, Object>();

      Map<String, Object> Scheme = new HashMap<String, Object>();
      Scheme.put("Name","abc");
      Scheme.put("Number","123");
      Map<String, Object> Member = new HashMap<String, Object>();
      Member.put("FirstName","abc");
      Member.put("Surname","efg");
      
     
      Map<String, Object> claim = new HashMap<String, Object>();
      claim.put("Scheme",Scheme);
      claim.put("Member",Member);
      

      parameters.put("claim", claim);
      request.setParameters(parameters);
      
      byte[] response = handler.handleRequest(request);
      File f = new File("./target/CD008-CAL_Final_liability.pdf");
      FileOutputStream fos = new FileOutputStream(f);
      fos.write(response);
   }
   @SuppressWarnings("resource")
   @Test
   public void tesTemplateWithBackgroundImages() throws ServiceException, IOException
   {
      TemplatingRequest request = new TemplatingRequest();
      request.setTemplateUri("classpath://custom/templates/1my-template-background-images.docx");
      request.setConvertToPdf(true);
      request.setFormat("docx");
      Map<String, Object> parameters = new HashMap<String, Object>();
      parameters.put("companyName","FIS");
      parameters.put("address","main street");
      request.setParameters(parameters);
      
      byte[] response = handler.handleRequest(request);
      File f = new File("./target/my-template-background-images1.pdf");
      FileOutputStream fos = new FileOutputStream(f);
      fos.write(response);
   }
   @SuppressWarnings("resource")
   @Test
   public void testDocxResume() throws ServiceException, IOException
   {
      TemplatingRequest request = new TemplatingRequest();
      request.setTemplateUri("classpath://custom/templates/DocxResume.docx");
      request.setConvertToPdf(true);
      request.setFormat("docx");
      Map<String, Object> parameters = new HashMap<String, Object>();
      parameters.put("firstName","FIS");
      parameters.put("lastName","FIS");
      request.setParameters(parameters);
      
      byte[] response = handler.handleRequest(request);
      File f = new File("./target/DocxResume.pdf");
      FileOutputStream fos = new FileOutputStream(f);
      fos.write(response);
   }
   
}
