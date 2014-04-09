package org.eclipse.stardust.ui.web.reporting.common.mapping.request;

import java.util.ArrayList;
import java.util.List;

public class ReportFilterMetaData
{
   private boolean process_filter_auxiliary = false;
   private boolean activity_filter_auxiliary = false;
   private boolean activity_filter_interactive = false;
   private boolean activity_filter_nonInteractive = false;
   private boolean isDescriptor = false;
   private boolean isStructuredType = false;
   private String xPath;
   private String javaType;
   private Number rangeFrom;
   private Number rangeTo;
   private List<String> selectedProcesses = new ArrayList<String>();

   public boolean isProcess_filter_auxiliary()
   {
      return process_filter_auxiliary;
   }

   public boolean isActivity_filter_auxiliary()
   {
      return activity_filter_auxiliary;
   }

   public boolean isActivity_filter_interactive()
   {
      return activity_filter_interactive;
   }

   public boolean isActivity_filter_nonInteractive()
   {
      return activity_filter_nonInteractive;
   }

   public List<String> getSelectedProcesses()
   {
      return selectedProcesses;
   }

   public boolean isDescriptor()
   {
      return isDescriptor;
   }

   public boolean isStructuredType()
   {
      return isStructuredType;
   }

   public String getXPath()
   {
      return xPath;
   }

   public String getJavaType()
   {
      return javaType;
   }

   public Number getRangeFrom()
   {
      return rangeFrom;
   }

   public Number getRangeTo()
   {
      return rangeTo;
   }
}
