package org.eclipse.stardust.engine.extensions.templating.core;

import java.util.Map;

import org.apache.commons.lang.StringUtils;

public class FieldMetaData
{

   private FieldMetaDataType type;// could be image,textStyling

   private String name;// image template name

   private String location;// classpath://location or repository://location or
   // http:// or file://

   private boolean useImageSize;// use Image size

   private String behavior;// to set NullImageBehaviour:
   // KeepImageTemplate/RemoveImageTemplate

   public FieldMetaData(Map<String, Object> entry)
   {
      this.type = extractType(entry);
      this.name = extractMandatoryStringField("name", entry);
      this.location = extractMandatoryStringField("location", entry);
      this.useImageSize = (entry.get("useImageSize") != null
            && ((Boolean) entry.get("useImageSize")) == true) ? true : false;
      this.behavior = (StringUtils.isEmpty((String) entry.get("behavior")))
            ? (String) entry.get("behavior")
            : null;
   }

   public String getName()
   {
      return name;
   }

   public FieldMetaDataType getType()
   {
      return type;
   }

   public void setType(FieldMetaDataType type)
   {
      this.type = type;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public String getLocation()
   {
      return location;
   }

   public void setLocation(String location)
   {
      this.location = location;
   }

   public boolean isUseImageSize()
   {
      return useImageSize;
   }

   public void setUseImageSize(boolean useImageSize)
   {
      this.useImageSize = useImageSize;
   }

   // public String getBehavior() {
   // return behavior;
   // }
   //
   // public void setBehavior(String behavior) {
   // this.behavior = behavior;
   // }

   private FieldMetaDataType extractType(Map<String, Object> entry)
   {
      String value = (String) entry.get("type");
      if (!StringUtils.isEmpty(value))
      {
         if (value.equalsIgnoreCase("image"))
            return FieldMetaDataType.IMAGE;
         else if (value.equalsIgnoreCase("textstyling"))
            return FieldMetaDataType.TEXT_STYLING;
      }
      return FieldMetaDataType.IMAGE;
   }

   private String extractMandatoryStringField(String fieldName, Map<String, Object> entry)
   {
      String value = (String) entry.get(fieldName);
      if (StringUtils.isEmpty(value))
      {
         throw new MissingFieldException(fieldName + " Field is missing");
      }
      return value;
   }
}
