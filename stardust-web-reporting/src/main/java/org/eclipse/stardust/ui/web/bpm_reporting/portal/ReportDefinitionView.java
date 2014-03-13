package org.eclipse.stardust.ui.web.bpm_reporting.portal;

import org.eclipse.stardust.ui.web.common.event.ViewEvent;

/**
 * 
 * @author Marc.Gille
 * 
 *         TODO Use AbstractAdapterView with Spring Bean Properties?
 */
public class ReportDefinitionView extends AbstractAdapterView
{
   /**
   *
   */
   public ReportDefinitionView()
   {
      super("/plugins/bpm-reporting/views/reportDefinitionView.html", "reportDefinitionFrameAnchor",
            "reportDefinitionName");
   }

   @Override
   public void handleEvent(ViewEvent event)
   {
      super.handleEvent(event);

      switch (event.getType())
      {
      case CREATED:
         event.getView().setIcon("/plugins/bpm-reporting/images/icons/report-definition.png");
         break;
      }
   }
}
