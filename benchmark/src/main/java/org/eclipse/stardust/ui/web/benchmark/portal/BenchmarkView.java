package org.eclipse.stardust.ui.web.benchmark.portal;

import org.eclipse.stardust.ui.web.common.event.ViewEvent;

/**
 *
 * @author Marc.Gille
 *
 *         TODO Use AbstractAdapterView with Spring Bean Properties?
 */
public class BenchmarkView extends AbstractAdapterView {
	/**
   *
   */
	public BenchmarkView() {
		super(
				"/plugins/benchmark/views/benchmarkView.html",
				"benchmarkViewFrameAnchor", "businessObjectName");
	}

	@Override
	public void handleEvent(ViewEvent event) {
		super.handleEvent(event);

		switch (event.getType()) {
		case CREATED:
			event.getView()
					.setIcon(
							"/plugins/benchmark/css/images/business-object.png");
			break;

		default:
			// not relevant
		}
	}
}
