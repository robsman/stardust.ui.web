package org.eclipse.stardust.engine.extensions.templating.component;

import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.*;

import org.apache.camel.Exchange;
import org.apache.camel.component.ResourceEndpoint;

public class AbstractEndpoint extends ResourceEndpoint
{
   protected String location;

   protected String format;

   protected boolean convertToPdf;

   protected String template;

   protected String outputName;

   public AbstractEndpoint(String uri, TemplatingComponent component, String resourceUri)
   {
      super(uri, component, resourceUri);
   }

   public String getLocation()
   {
      return location;
   }

   public void setLocation(String location)
   {
      this.location = location;
   }

   public String getFormat()
   {
      return format;
   }

   public void setFormat(String format)
   {
      this.format = format;
   }

   public boolean isConvertToPdf()
   {
      return convertToPdf;
   }

   public void setConvertToPdf(boolean convertToPdf)
   {
      this.convertToPdf = convertToPdf;
   }

   public String getTemplate()
   {
      return template;
   }

   public void setTemplate(String template)
   {
      this.template = template;
   }

   public String getOutputName()
   {
      return outputName;
   }

   public void setOutputName(String outputName)
   {
      this.outputName = outputName;
   }

   public void copyTemplatingHeaders(Exchange exchange, AbstractEndpoint endpoint)
   {
      exchange.getIn().setHeader(TEMPLATING_LOCATION, endpoint.getLocation());
      exchange.getIn().setHeader(TEMPLATING_FORMAT, endpoint.getFormat());
      exchange.getIn().setHeader(TEMPLATING_TEMPLATE, endpoint.getTemplate());
      exchange.getIn().setHeader(TEMPLATING_OUTPUT_NAME, endpoint.getOutputName());
      exchange.getIn().setHeader(TEMPLATING_CONVERT_TO_PDF, endpoint.isConvertToPdf());
   }

}
