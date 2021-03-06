package org.eclipse.stardust.ui.web.benchmark.portal;

import javax.faces.context.FacesContext;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.common.app.PortalApplicationEventScript;
import org.eclipse.stardust.ui.web.common.app.View;
import org.eclipse.stardust.ui.web.common.event.ViewEvent;
import org.eclipse.stardust.ui.web.common.event.ViewEventHandler;

/**
 *
 * @author Marc.Gille
 *
 */
public class AbstractAdapterView implements ViewEventHandler {
	private String viewPath;
	private String anchorId;
	private String keyParam;

	/**
	 * @param viewPath
	 * @param anchorId
	 * @param keyParam
	 */
	public AbstractAdapterView(String viewPath, String anchorId, String keyParam) {
		super();

		this.viewPath = viewPath;
		this.anchorId = anchorId;
		this.keyParam = keyParam;
	}

	/**
	 *
	 */
	public void handleEvent(ViewEvent event) {
		String pagePath = FacesContext.getCurrentInstance()
				.getExternalContext().getRequestContextPath();
		pagePath += viewPath;
		String iframeId = "mf_" + event.getView().getIdentityParams();

		switch (event.getType()) {
		case TO_BE_ACTIVATED:
			Object keyParamValue = (StringUtils.isNotEmpty(keyParam)) ? event
					.getView().getViewParams().get(keyParam) : "";
			PortalApplication
					.getInstance()
					.addEventScript(
							"InfinityBpm.ProcessPortal.createOrActivateContentFrame('"
									+ iframeId
									+ "', '"
									+ pagePath
									+ event.getView().getParams()
									+ "', {anchorId:'"
									+ anchorId
									+ "', anchorYAdjustment:10, zIndex:200, frmAttrs: {displayName: '"
									+ keyParamValue + "'}});");
			fireResizeIframeEvent();

			if (View.ViewState.INACTIVE == event.getView().getViewState()) {
				changeMouseCursorStyle("default");
			}
			break;

		case TO_BE_DEACTIVATED:
			PortalApplication.getInstance().addEventScript(
					"InfinityBpm.ProcessPortal.deactivateContentFrame('"
							+ iframeId + "');");
			fireResizeIframeEvent();
			break;

		case CLOSED:
			PortalApplication.getInstance().addEventScript(
					"InfinityBpm.ProcessPortal.closeContentFrame('" + iframeId
							+ "');");
			break;

		case LAUNCH_PANELS_ACTIVATED:
		case LAUNCH_PANELS_DEACTIVATED:
		case FULL_SCREENED:
		case RESTORED_TO_NORMAL:
		case PINNED:
		case PERSPECTIVE_CHANGED:
			fireResizeIframeEvent();
			break;

		default:
		   // not relevant
		}
	}

	private void fireResizeIframeEvent() {
		PortalApplication.getInstance().addEventScript(
				"InfinityBpm.ProcessPortal.resizeIFrames();");
	}

	/**
	 * @param style
	 */
	private void changeMouseCursorStyle(String style) {
		PortalApplicationEventScript.getInstance().addEventScript(
				"InfinityBpm.Core.changeMouseCursorStyle(\"" + style + "\");");
	}
}