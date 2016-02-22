package org.eclipse.stardust.ui.web.benchmark.portal;

import org.eclipse.stardust.ui.web.common.event.ViewEvent;

/**
 *
 * @author Marc.Gille
 *
 *         TODO Use AbstractAdapterView with Spring Bean Properties?
 */
public class GanttChartView extends AbstractAdapterView {
	/**
   *
   */
	public GanttChartView() {
		super("/plugins/benchmark/views/ganttChartView.html",
				"ganttChartViewFrameAnchor", "name");
	}

	@Override
	public void handleEvent(ViewEvent event) {
		super.handleEvent(event);

		switch (event.getType()) {
		case CREATED:
			event.getView().setIcon(
					"/plugins/simple-modeler/css/images/simpleModel.png");

			break;

      default:
         // not relevant
		}
	}
}
