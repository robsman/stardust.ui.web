package org.eclipse.stardust.ui.web.reporting.common.mapping.request;

import com.google.gson.JsonElement;

import org.eclipse.stardust.ui.web.reporting.common.validation.IValidateAble;
import org.eclipse.stardust.ui.web.reporting.common.validation.annotations.NotNull;

public class ReportDefinition implements IValidateAble
{
   @NotNull
   private String name;

   private String description;

   private ReportStorage storage;

   private ReportDataSet dataSet;

   // TODO: - implement wrapper object when its clear how the final json
   // will look like
   private JsonElement parameters;

   private JsonElement layout;

   private ReportScheduling scheduling;

   public String getName()
   {
      return name;
   }

   public String getDescription()
   {
      return description;
   }

   public ReportStorage getStorage()
   {
      return storage;
   }

   public ReportDataSet getDataSet()
   {
      return dataSet;
   }

   public JsonElement getParameters()
   {
      return parameters;
   }

   public JsonElement getLayout()
   {
      return layout;
   }

   public ReportScheduling getScheduling()
   {
      return scheduling;
   }
}
