package org.eclipse.stardust.engine.extensions.templating.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TemplatingRequest
{

   @SerializedName("templateUri")
   private String templateUri; // TODO: rename this parameter to templateFilePath in the
                               // JSON request

   @SerializedName("template")
   private String template; // TODO: rename this parameter to templateContent

   private String format;

   @SerializedName("processOid")
   private Long processOid;

   @SerializedName("accountId")
   private String accountId;

   @SerializedName("pdf")
   private boolean convertToPdf;

   private Map<String, Object> parameters;

   private Map<String, Object> output;

   private @Expose(serialize = false, deserialize = false) byte[] xdocContent;

   private @Expose(serialize = false, deserialize = false) List<FieldMetaData> fieldsMetaData;

   public String getTemplateUri()
   {
      return templateUri;
   }

   public void setTemplateUri(String templateUri)
   {
      this.templateUri = templateUri;
   }

   public String getTemplate()
   {
      return template;
   }

   public void setTemplate(String template)
   {
      this.template = template;
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

   public Map<String, Object> getParameters()
   {
      if (parameters == null)
         parameters = new HashMap<String, Object>();
      return parameters;
   }

   public void setParameters(Map<String, Object> parameters)
   {
      this.parameters = parameters;
   }

   public Map<String, Object> getOutput()
   {
      return output;
   }

   public void setOutput(Map<String, Object> output)
   {
      this.output = output;
   }

   public byte[] getXdocContent()
   {
      return xdocContent;
   }

   public void setXdocContent(byte[] xdocContent)
   {
      this.xdocContent = xdocContent;
   }

   public Long getProcessOid()
   {
      return processOid;
   }

   public void setProcessOid(Long processOid)
   {
      this.processOid = processOid;
   }

   public String getAccountId()
   {
      return accountId;
   }

   public void setAccountId(String accountId)
   {
      this.accountId = accountId;
   }

   public List<FieldMetaData> getFieldsMetaData()
   {
      return fieldsMetaData;
   }

   private List<FieldMetaData> lookupFieldsMetaDataFromParameters()
   {
      List<FieldMetaData> response = new ArrayList<FieldMetaData>();
      List<Map<String, Object>> fields = (List<Map<String, Object>>) ((Map<String, Object>) this.parameters
            .get("fieldsMetaData")).get("fields");
      for (Map<String, Object> entry : fields)
      {

         response.add(new FieldMetaData(entry));
      }
      return response;
   }

   public void setFieldsMetaData(List<FieldMetaData> fieldsMetaData)
   {
      this.fieldsMetaData = fieldsMetaData;
   }

   @Override
   public String toString()
   {
      return "TemplatingRequest [templateUri=" + templateUri + ", template=" + template
            + ", processOid=" + processOid + ", format=" + format + ", convertToPdf="
            + convertToPdf + ", parameters=" + parameters + ", output=" + output + "]";
   }

   @SuppressWarnings("unchecked")
   public void fromMap(Map<String, Object> input)
   {
      if (input.containsKey("templateUri"))
         this.templateUri = (String) input.get("templateUri");
      if (input.containsKey("template"))
         this.template = (String) input.get("template");
      if (input.containsKey("accountId"))
         this.accountId = (String) input.get("accountId");
      if (input.containsKey("processOid"))
      {
         if (input.get("processOid") instanceof String
               && !StringUtils.isEmpty((String) input.get("processOid")))
         {
            this.processOid = Long.parseLong((String) input.get("processOid"));
         }
         if (input.get("processOid") instanceof Integer)
            this.processOid = ((Integer) input.get("processOid")).longValue();
      }
      if (input.containsKey("format"))
         this.format = (String) input.get("format");
      if (input.containsKey("pdf"))
         this.convertToPdf = (Boolean) input.get("pdf");
      if (input.containsKey("parameters"))
      {
         this.parameters = (Map<String, Object>) input.get("parameters");
         if (this.parameters != null && !this.parameters.isEmpty())
         {
            if (this.parameters.containsKey("fieldsMetaData"))
               this.fieldsMetaData = lookupFieldsMetaDataFromParameters();
         }
      }
      if (input.containsKey("output"))
         this.output = (Map<String, Object>) input.get("output");
   }

   public Map<String, Object> toMap()
   {
      Map<String, Object> topLevel = new HashMap<String, Object>();
      topLevel.put("templateUri", this.templateUri);
      topLevel.put("template", this.template);
      topLevel.put("processOid", this.processOid);
      topLevel.put("accountId", this.accountId);
      topLevel.put("format", this.format);
      topLevel.put("pdf", this.convertToPdf);
      topLevel.put("parameters", this.parameters);
      topLevel.put("output", this.output);
      return topLevel;
   }

}
