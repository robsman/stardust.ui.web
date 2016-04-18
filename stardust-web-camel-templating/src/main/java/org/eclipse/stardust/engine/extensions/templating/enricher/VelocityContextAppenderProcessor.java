package org.eclipse.stardust.engine.extensions.templating.enricher;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.util.ExchangeHelper;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.tools.ToolManager;

/**
 * Used to initialize VelocityContext.
 * 
 */
public class VelocityContextAppenderProcessor implements org.apache.camel.Processor
{
   private String toolsConfigFilePath;

   private VelocityContext velocityContext;

   public VelocityContextAppenderProcessor(String toolsConfigFilePath)
   {
      this.toolsConfigFilePath = toolsConfigFilePath;
      this.velocityContext = initializeVelocityContext(this.toolsConfigFilePath);
   }

   private static ToolManager initializeToolManager(String toolsConfigFilePath)
   {
      ToolManager velocityToolManager = new ToolManager();
      velocityToolManager.configure(toolsConfigFilePath);
      return velocityToolManager;
   }

   private static VelocityContext initializeVelocityContext(String toolsConfigFilePath)
   {
      ToolManager velocityToolManager = initializeToolManager(toolsConfigFilePath);
      VelocityContext velocityContext = new VelocityContext(
            velocityToolManager.createContext());
      return velocityContext;
   }

   public VelocityContext getVelocityContext()
   {
      if (velocityContext == null)
         this.velocityContext = initializeVelocityContext(this.toolsConfigFilePath);
      return velocityContext;
   }

   @Override
   public void process(Exchange exchange) throws Exception
   {
      this.velocityContext = initializeVelocityContext(toolsConfigFilePath);
      // remove all headers that contains : in the identifier since it's not a valid
      // velocity identifier
      Map<String, Object> filteredHeaders = new HashMap<String, Object>();
      for (String key : exchange.getIn().getHeaders().keySet())
      {
         if (!key.contains(":"))
            filteredHeaders.put(key, exchange.getIn().getHeader(key));
      }
      exchange.getIn().setHeaders(filteredHeaders);

      Map<String, Object> variableMap = ExchangeHelper.createVariableMap(exchange);
      for (String key : variableMap.keySet())
      {
         velocityContext.put(key, variableMap.get(key));
      }
      exchange.getIn().setHeader("CamelVelocityContext", velocityContext);
   }

}
