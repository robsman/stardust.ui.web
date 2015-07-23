package org.eclipse.stardust.ui.web.modeler.portal;

import org.eclipse.stardust.ui.web.common.event.ViewEvent;

public class TemplateApplicationView extends AbstractAdapterView {
	   public TemplateApplicationView()
	   {
	      super("/plugins/bpm-modeler/views/modeler/templateApplicationView.html", "templateApplicationFrameAnchor",
	            "applicationName");
	   }

	   @Override
	   public void handleEvent(ViewEvent event)
	   {
	      super.handleEvent(event);

	      switch (event.getType())
	      {
	      case CREATED:
	         event.getView().setIcon(
	               "/plugins/bpm-modeler/images/icons/application-c-ext-web.png");
	         break;
	      }
	   }
	}
