package org.eclipse.stardust.engine.extensions.velocity.tool;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.velocity.tools.Scope;
import org.apache.velocity.tools.config.DefaultKey;
import org.apache.velocity.tools.config.InvalidScope;
import org.apache.velocity.tools.generic.SafeConfig;
import org.apache.velocity.tools.generic.ValueParser;

@DefaultKey("docreq")
@InvalidScope({Scope.APPLICATION, Scope.SESSION})
public class DocReqTool extends SafeConfig
{

   private static final String ACCEPTED = "Accepted";

   private static final String REQUIRED = "Required";

   private Map<String, Object> documentRequest;

   private String format;

   public void setFormat(String format)
   {
      if (format == null)
         this.format = "text";
      this.format = format;
   }

   public void setDocumentRequest(Map<String, Object> documentRequest)
   {
      this.documentRequest = documentRequest;
   }

   public void configure(ValueParser parser)
   {}

   public DocReqTool()
   {}

   @SuppressWarnings("unchecked")
   public String getAll()
   {
      List<Map<String, Object>> documents = (List<Map<String, Object>>) this.documentRequest
            .get("Documents");
      return processAll(this.format, documents);
   }

   @SuppressWarnings("unchecked")
   public String getAll(Object format, Object o)
   {
      if (o == null)
         return null;
      List<Map<String, Object>> documents = (List<Map<String, Object>>) o;
      return processAll((String) format, documents);
   }

   @SuppressWarnings("unchecked")
   private String processAll(String format, List<Map<String, Object>> documents)
   {
      StringBuilder response = new StringBuilder();
      if (StringUtils.isNotEmpty(format) && format.equalsIgnoreCase("html"))
         response.append("<table>");
      for (Object entry : documents)
      {
         Map<String, Object> document = (Map<String, Object>) entry;
         if (StringUtils.isNotEmpty(format) && !format.equalsIgnoreCase("html"))
         {
            response.append(getText(document));
         }
         else
         {
            response.append(getHtml(document));
         }
      }
      if (StringUtils.isNotEmpty(format) && format.equalsIgnoreCase("html"))
         response.append("</table>");
      return response.toString();
   }

   @SuppressWarnings("unchecked")
   public String getAccepted()
   {
      List<Map<String, Object>> documents = (List<Map<String, Object>>) this.documentRequest
            .get("Documents");
      return accepted(this.format, documents);
   }

   @SuppressWarnings("unchecked")
   public String getAccepted(String format, Object o)
   {
      if (o == null)
         return null;
      List<Map<String, Object>> documents = (List<Map<String, Object>>) o;
      return accepted(format, documents);
   }

   /**
    * should pass in $CORRESPONDENCE.Documents
    * 
    * @param o
    * @return
    */
   @SuppressWarnings("unchecked")
   public String accepted(Object o)
   {
      if (o == null)
         return null;
      List<Map<String, Object>> documents = (List<Map<String, Object>>) o;
      return accepted(this.format, documents);
   }

   @SuppressWarnings("unchecked")
   private String accepted(String format, List<Map<String, Object>> documents)
   {
      StringBuilder response = new StringBuilder();
      if (StringUtils.isNotEmpty(format) && format.equalsIgnoreCase("html"))
         response.append("<table>");
      for (Object entry : documents)
      {
         Map<String, Object> document = (Map<String, Object>) entry;
         if ((Boolean) document.get(ACCEPTED) )
         {
            if (StringUtils.isNotEmpty(format) && !format.equalsIgnoreCase("html"))
            {
               response.append(getText(document));
            }
            else
            {
               response.append(getHtml(document));
            }
         }
      }
      if (StringUtils.isNotEmpty(format) && format.equalsIgnoreCase("html"))
         response.append("</table>");
      return response.toString();
   }

   /**
    * The output would list (name, comment) all documents that are required and not
    * existing yet.
    * 
    * @return
    */
   @SuppressWarnings("unchecked")
   public String getRequired()
   {
      List<Map<String, Object>> documents = (List<Map<String, Object>>) this.documentRequest
            .get("Documents");
      return required(this.format, documents);
   }

   public String getRequired(Object format, Object o)
   {
      if (o == null)
         return null;
      List<Map<String, Object>> documents = (List<Map<String, Object>>) o;
      return required((String) format, documents);
   }

   /**
    * should pass in $CORRESPONDENCE.Documents
    * 
    * @param o
    * @return
    */
   @SuppressWarnings("unchecked")
   public String required(Object o)
   {
      if (o == null)
         return null;
      List<Map<String, Object>> documents = (List<Map<String, Object>>) o;
      return required(this.format, documents);
   }

   private String required(String format, List<Map<String, Object>> documents)
   {
      StringBuilder response = new StringBuilder();
      if (StringUtils.isNotEmpty(format) && format.equalsIgnoreCase("html"))
         response.append("<table>");
      for (Object entry : documents)
      {
         Map<String, Object> document = (Map<String, Object>) entry;
         if ((Boolean) document.get(REQUIRED))
         {
            if (StringUtils.isNotEmpty(format) && !format.equalsIgnoreCase("html"))
            {
               response.append(getText(document));
            }
            else
            {
               response.append(getHtml(document));
            }
         }
      }
      if (StringUtils.isNotEmpty(format) && format.equalsIgnoreCase("html"))
         response.append("</table>");
      return response.toString();
   }

   private StringBuilder getHtml(Map<String, Object> document)
   {
      StringBuilder response = new StringBuilder();
      response.append("<tr>");
      response.append("<td>Document :</td>");
      response.append("<td>" + document.get("Name") + "</td>");
      response.append("</tr>");
      response.append("<tr>");
      response.append("<td>Comment :</td>");
      response.append("<td>" + document.get("Comment") + "</td>");
      response.append("</tr>");
      return response;
   }

   private StringBuilder getText(Map<String, Object> document)
   {
      StringBuilder response = new StringBuilder();
      response.append("Document : " + document.get("Name"));
      response.append("\n");
      response.append("Comment : " + document.get("Comment"));
      response.append("\n");
      return response;
   }
}
