package org.eclipse.stardust.ui.web.modeler.portal;

import java.util.Arrays;

import javax.faces.context.FacesContext;

import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.common.event.ViewEvent;
import org.eclipse.stardust.ui.web.common.event.ViewEventHandler;
import org.eclipse.stardust.ui.web.modeler.service.ModelService;
import org.springframework.stereotype.Component;

import com.icesoft.faces.context.effects.JavascriptContext;


@Component
public class XsdStructuredDataTypeView implements ViewEventHandler {
	private ModelService modelService;


	public ModelService getModelService() {
		return modelService;
	}

	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}

	public void handleEvent(ViewEvent event) {
		String pagePath = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath();
		pagePath += "/plugins/bpm-modeler/views/modeler/xsdStructuredDataTypePanel.xhtml";
		String iframeId = "mf_" + event.getView().getIdentityParams();

		switch (event.getType())
		{
		case TO_BE_ACTIVATED:
			FacesContext facesContext = FacesContext.getCurrentInstance();

			if (!Arrays.asList(JavascriptContext.getIncludedLibs(facesContext)).contains(
					"/plugins/processportal/IppProcessPortal.js")) {
				JavascriptContext.includeLib("/plugins/processportal/IppProcessPortal.js",
						facesContext);
			}

			PortalApplication.getInstance().addEventScript("InfinityBpm.ProcessPortal.createOrActivateContentFrame('" + iframeId + "', '" + pagePath + event.getView().getParams() + "', {anchorId:'xsdDataStructurePanelAnchor', width:1000, height:800, maxWidth:1000, maxHeight:1000, anchorYAdjustment:10, zIndex:200});");

			break;

		case TO_BE_DEACTIVATED:
			PortalApplication.getInstance().addEventScript("InfinityBpm.ProcessPortal.deactivateContentFrame('" + iframeId + "');");
			break;

		case CLOSED:
			PortalApplication.getInstance().addEventScript(
					"InfinityBpm.ProcessPortal.closeContentFrame('" + iframeId + "');");
			break;

		case LAUNCH_PANELS_ACTIVATED:
		case LAUNCH_PANELS_DEACTIVATED:
		case FULL_SCREENED:
		case RESTORED_TO_NORMAL:
			PortalApplication.getInstance().addEventScript(
					"InfinityBpm.ProcessPortal.resizeContentFrame('"
					+ iframeId + "');");
			break;
		}

	}

	/**
	 *
	 */
	public void save()
	{
		//getModelService().saveModel();
	}
}
