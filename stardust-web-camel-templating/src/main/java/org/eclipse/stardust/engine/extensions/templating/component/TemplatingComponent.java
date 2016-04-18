package org.eclipse.stardust.engine.extensions.templating.component;

import java.util.Map;

import org.apache.camel.Endpoint;
import org.apache.camel.impl.DefaultComponent;

public class TemplatingComponent extends DefaultComponent
{

   @Override
   protected Endpoint createEndpoint(String uri, String remaining,
         Map<String, Object> parameters) throws Exception
   {
      // Templating parameters
      String location = remaining;
      String format = getAndRemoveParameter(parameters, "format", String.class);
      String convertToPdf = getAndRemoveParameter(parameters, "convertToPdf",
            String.class);
      String template = getAndRemoveParameter(parameters, "template", String.class);
      String outputName = getAndRemoveParameter(parameters, "outputName", String.class);

      TemplatingEndpoint endpoint = new TemplatingEndpoint(uri, this, remaining);
      endpoint.setLocation(location);
      endpoint.setFormatExpression(format);
      endpoint.setConvertToPdfExpression(convertToPdf);
      endpoint.setTemplateExpression(template);
      endpoint.setOutputNameExpression(outputName);
      return endpoint;
   }

}
