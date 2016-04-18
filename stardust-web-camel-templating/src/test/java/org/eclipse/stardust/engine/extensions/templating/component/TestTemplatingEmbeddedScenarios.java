package org.eclipse.stardust.engine.extensions.templating.component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultExchange;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class TestTemplatingEmbeddedScenarios
{
   private static CamelContext camelContext;
   private static ClassPathXmlApplicationContext ctx;
   private static ProducerTemplate producer;
private static Exchange exchange;
   @BeforeClass
   public static void beforeClass()
   {
      ctx = new ClassPathXmlApplicationContext(
            new String[] {"org/eclipse/stardust/engine/extensions/templating/component/templating-embedded-scenarios.xml", "classpath:META-INF/spring/templating-application-context.xml"});
      camelContext = (CamelContext) ctx.getBean("defaultCamelContext");
      producer = camelContext.createProducerTemplate();
      exchange = createExchange();
   }

   private static Exchange createExchange()
   {
      Exchange exchange = new DefaultExchange(camelContext);
      Map<String, Object> ref = new HashMap<String, Object>();
      ref.put("firstName", "John");
      ref.put("lastName", "Smith");
      ref.put("dob", new Date());
      exchange.getIn().setHeader("personInput", ref);
      return exchange;
   }

   /**
    * Source : embedded Format: Text Output Type : Text Convert To PDF: false
    * 
    * REST REQUEST 
    * { 
    *    template: Hello $person.firstName $person.lastName, 
    *    format:text,
    *    outputFormat:text,
    *    pdf=false,
    *    parameters:{ 
    *    "person": { 
    *       "firstname": "abc",
    *       "lastname": "cba" }
    *       }
    * }
    * 
    */
   @Test
   public void testEmbeddedTextText()
   {
      exchange.getIn().setHeader("CamelVelocityTemplate", "Hello $person.firstName $person.lastName");
      producer.send("direct:testEmbeddedTextText", exchange);
   }
}
