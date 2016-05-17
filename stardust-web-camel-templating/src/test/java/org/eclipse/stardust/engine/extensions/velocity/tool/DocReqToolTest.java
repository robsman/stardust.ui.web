package org.eclipse.stardust.engine.extensions.velocity.tool;

import org.apache.velocity.VelocityContext;
import org.eclipse.stardust.engine.extensions.templating.enricher.VelocityContextAppenderProcessor;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class DocReqToolTest
{
   private ClassPathXmlApplicationContext ctx;

   private DocReqTool tool;

   @Before
   public void before()
   {
      ctx = new ClassPathXmlApplicationContext(new String[] {
            "classpath:META-INF/spring/templating-application-context.xml"});
      VelocityContext velocityContext=VelocityContextAppenderProcessor.initializeVelocityContext("default-velocity-tools.xml");
      tool = (DocReqTool) velocityContext.get("docreq");

   }

   @After
   public void after()
   {
      ctx.close();
   }

   @Test
   @Ignore("Not yet implemented")
   public void testGetAll()
   {

   }

   @Test
   @Ignore("Not yet implemented")
   public void testGetAccepted()
   {

   }

   @Test
   @Ignore("Not yet implemented")
   public void testGetRequired()
   {

   }

}
