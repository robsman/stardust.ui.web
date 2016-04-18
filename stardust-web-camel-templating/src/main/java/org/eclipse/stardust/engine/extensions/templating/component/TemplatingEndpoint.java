package org.eclipse.stardust.engine.extensions.templating.component;

import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.TEMPLATING_CONVERT_TO_PDF;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.TEMPLATING_FORMAT;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.TEMPLATING_LOCATION;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.TEMPLATING_OUTPUT_NAME;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.TEMPLATING_REQUEST_METADATA;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.TEMPLATING_TEMPLATE;
import static org.eclipse.stardust.engine.extensions.camel.CamelConstants.TEMPLATING_TEMPLATE_CONTENT;
import static org.eclipse.stardust.engine.extensions.camel.RouteHelper.parseSimpleExpression;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.component.ResourceEndpoint;
import org.apache.camel.util.CamelContextHelper;
import org.apache.commons.lang.StringUtils;
import org.eclipse.stardust.engine.extensions.templating.core.FieldMetaData;
import org.eclipse.stardust.engine.extensions.templating.core.RequestHandler;
import org.eclipse.stardust.engine.extensions.templating.core.ServiceException;
import org.eclipse.stardust.engine.extensions.templating.core.TemplatingRequest;

public class TemplatingEndpoint extends ResourceEndpoint
{
   private String location;

   private String formatExpression;

   private String convertToPdfExpression;

   private String templateExpression;

   private String outputNameExpression;

   public void copyTemplatingHeaders(Exchange exchange)
   {
      exchange.getIn().setHeader(TEMPLATING_LOCATION, location);
      exchange.getIn().setHeader(TEMPLATING_FORMAT, evaluateFormat(exchange));
      exchange.getIn().setHeader(TEMPLATING_TEMPLATE, evaluateTemplate(exchange));
      exchange.getIn().setHeader(TEMPLATING_OUTPUT_NAME, evaluateOutputName(exchange));
      exchange.getIn().setHeader(TEMPLATING_CONVERT_TO_PDF,
            evaluateConvertToPdf(exchange));
   }

   public TemplatingEndpoint(String uri, TemplatingComponent component,
         String resourceUri)
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

   public String evaluateFormat(Exchange exchange)
   {
      if (formatExpression != null)
         return parseSimpleExpression(formatExpression).evaluate(exchange, String.class);
      else
      {
         return (String) exchange.getIn().getHeader(TEMPLATING_FORMAT);
      }
   }

   public String getFormatExpression()
   {
      return formatExpression;
   }

   public void setFormatExpression(String formatExpression)
   {
      this.formatExpression = formatExpression;
   }

   public Boolean evaluateConvertToPdf(Exchange exchange)
   {
      boolean returnValue = false;

      if (convertToPdfExpression != null)
      {
         returnValue = parseSimpleExpression(convertToPdfExpression).evaluate(exchange,
               Boolean.class);
      }
      else
      {
         returnValue = exchange.getIn().getHeader(TEMPLATING_CONVERT_TO_PDF, false,
               Boolean.class);
      }
      return returnValue;
   }

   public void setConvertToPdfExpression(String convertToPdfExpression)
   {
      this.convertToPdfExpression = convertToPdfExpression;
   }

   public String getConvertToPdfExpression()
   {
      return convertToPdfExpression;
   }

   public String evaluateTemplate(Exchange exchange)
   {
      if (templateExpression != null)
      {
         return parseSimpleExpression(templateExpression).evaluate(exchange,
               String.class);
      }
      else
      {
         return exchange.getIn().getHeader(TEMPLATING_TEMPLATE, String.class);
      }
   }

   public void setTemplateExpression(String templateExpression)
   {
      this.templateExpression = templateExpression;
   }

   public String getTemplateExpression()
   {
      return templateExpression;
   }

   public String evaluateOutputName(Exchange exchange)
   {
      if (outputNameExpression != null)
         return parseSimpleExpression(outputNameExpression).evaluate(exchange,
               String.class);
      else
         return exchange.getIn().getHeader(TEMPLATING_OUTPUT_NAME, String.class);
   }

   @SuppressWarnings("unchecked")
   public List<Map<String, Object>> evaluateFieldsMetaData(Exchange exchange)
   {
      Map<String, Object> headers = exchange.getIn().getHeaders();
      if (headers.containsKey(TEMPLATING_REQUEST_METADATA))
      {
         Map<String, Object> data = (Map<String, Object>) exchange.getIn()
               .getHeader(TEMPLATING_REQUEST_METADATA);
         return (List<Map<String, Object>>) data.get("fields");
      }
      return null;
   }

   public void setOutputNameExpression(String outputNameExpression)
   {
      this.outputNameExpression = outputNameExpression;
   }

   public String getOutputNameExpression()
   {
      return outputNameExpression;
   }

   /**
    * @throws ServiceException
    * 
    * 
    */
   @Override
   protected void onExchange(Exchange exchange) throws ServiceException
   {
      copyTemplatingHeaders(exchange);
      TemplatingRequest request = createTemplatingRequestFromExchange(exchange);
      RequestHandler handler = (RequestHandler) CamelContextHelper
            .mandatoryLookup(exchange.getContext(), "requestHandler");
      byte[] out = handler.handleRequest(request);
      Message outMessage = exchange.getOut();
      outMessage.setBody(out);
      outMessage.setHeaders(exchange.getIn().getHeaders());
      outMessage.setAttachments(exchange.getIn().getAttachments());
   }

   /**
    * Return an instance of TemplatingRequest that will be used by templating core
    * service.
    * 
    * @param exchange
    * @return
    */
   private TemplatingRequest createTemplatingRequestFromExchange(Exchange exchange)
   {
      TemplatingRequest request = new TemplatingRequest();
      request.setConvertToPdf(evaluateConvertToPdf(exchange));
      request.setFormat(evaluateFormat(exchange));
      request.setOutput(toOutput("name", evaluateOutputName(exchange)));
      request.setParameters(exchange.getIn().getHeaders());

      if (location.equalsIgnoreCase("embedded"))
      {
         request.setTemplate(
               exchange.getIn().getHeader(TEMPLATING_TEMPLATE_CONTENT, String.class));
      }
      else if (location.equalsIgnoreCase("classpath")
            || location.equalsIgnoreCase("repository"))
      {
         request.setTemplateUri(toTemplateUri(location, evaluateTemplate(exchange)));
      }
      else
      { // Data
         if (!request.getFormat().equals("docx"))
         {
            request.setTemplate(
                  exchange.getIn().getHeader(TEMPLATING_TEMPLATE_CONTENT, String.class));
         }
         else
         {
            request.setXdocContent(
                  exchange.getIn().getHeader(TEMPLATING_TEMPLATE_CONTENT, byte[].class));
         }
      }

      List<Map<String, Object>> requestMetaData = evaluateFieldsMetaData(exchange);
      if (requestMetaData != null)
         request.setFieldsMetaData(extractFieldsMetaData(requestMetaData));

      return request;
   }

   private Map<String, Object> toOutput(String key, String value)
   {
      Map<String, Object> output = null;
      if (StringUtils.isNotEmpty(value))
      {
         output = new HashMap<String, Object>();
         output.put(key, value);
      }
      return output;
   }

   /**
    * return a string in the following formation location://providedTemplate
    * 
    * @param location
    * @param providedTemplate
    * @return
    */
   private String toTemplateUri(String location, String providedTemplate)
   {
      // if (location.equalsIgnoreCase("classpath"))
      return location + "://" + providedTemplate;
      // else
      // return location + "://" + providedTemplate;
   }

   /**
    * 
    * @param requestMetaData
    * @return
    */
   private List<FieldMetaData> extractFieldsMetaData(
         List<Map<String, Object>> requestMetaData)
   {
      List<FieldMetaData> response = new ArrayList<FieldMetaData>();
      for (Map<String, Object> entry : requestMetaData)
      {
         FieldMetaData field = new FieldMetaData(entry);
         response.add(field);
      }
      return response;
   }
}
