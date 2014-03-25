package org.eclipse.stardust.ui.web.reporting.core.mapping;

import com.google.gson.JsonElement;

public class ReportDefinition
{
   private String name;
   private String description;
   private ReportStorage storage;
   private ReportDataSet dataSet;

   //TODO: - implement wrapper object when its clear how the final json
   //will look like
   private JsonElement parameters;
   private JsonElement layout;
   private ReportScheduling scheduling;
}
