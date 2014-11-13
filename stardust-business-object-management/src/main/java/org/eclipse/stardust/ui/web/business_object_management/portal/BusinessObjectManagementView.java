package org.eclipse.stardust.ui.web.business_object_management.portal;

import org.eclipse.stardust.ui.web.common.event.ViewEvent;

/**
 *
 * @author Marc.Gille
 *
 *         TODO Use AbstractAdapterView with Spring Bean Properties?
 */
public class BusinessObjectManagementView extends AbstractAdapterView {
	/**
   *
   */
	public BusinessObjectManagementView() {
		super(
				"/plugins/business-object-management/views/businessObjectManagementView.html",
				"businessObjectManagementViewFrameAnchor", "businessObjectName");
	}

	@Override
	public void handleEvent(ViewEvent event) {
		super.handleEvent(event);

		switch (event.getType()) {
		case CREATED:
			event.getView()
					.setIcon(
							"/plugins/business-object-management/css/images/business-object.png");
			break;

		default:
			// not relevant
		}
	}
}
