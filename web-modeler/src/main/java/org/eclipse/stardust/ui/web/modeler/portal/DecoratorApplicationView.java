package org.eclipse.stardust.ui.web.modeler.portal;

import org.eclipse.stardust.ui.web.common.event.ViewEvent;

public class DecoratorApplicationView extends AbstractAdapterView {
	   public DecoratorApplicationView()
	   {
	      super("/plugins/bpm-modeler/views/modeler/decoratorApplicationView.html", "decoratorApplicationFrameAnchor",
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
