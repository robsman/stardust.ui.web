package org.eclipse.stardust.ui.web.reporting.common.mapping.request;

import java.util.ArrayList;
import java.util.List;

public class ReportFilterMetaData
{
   private boolean process_filter_auxiliary = false;
   private boolean activity_filter_auxiliary = false;
   private boolean activity_filter_interactive = false;
   private boolean activity_filter_nonInteractive = false;
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
}
