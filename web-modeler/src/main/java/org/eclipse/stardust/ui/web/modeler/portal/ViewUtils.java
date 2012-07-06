package org.eclipse.stardust.ui.web.modeler.portal;

import java.util.Map;

import javax.faces.FactoryFinder;
import javax.faces.context.FacesContext;
import javax.faces.context.FacesContextFactory;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.lifecycle.LifecycleFactory;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.common.util.AbstractMessageBean;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;

public class ViewUtils {

	public static void openView(String modelId, String processId,
			ServletContext servletContext, HttpServletRequest req, HttpServletResponse resp) {
		System.out.println("ServletContext : " + servletContext);
		System.out.println("ServletRequest : " + req);
		System.out.println("ServletResponse : " + resp);

        FacesContextFactory contextFactory = (FacesContextFactory)FactoryFinder.getFactory(FactoryFinder.FACES_CONTEXT_FACTORY);
        LifecycleFactory lifecycleFactory = (LifecycleFactory)FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY);
        Lifecycle lifecycle = lifecycleFactory.getLifecycle(LifecycleFactory.DEFAULT_LIFECYCLE);

        FacesContext facesContext = contextFactory.getFacesContext(servletContext, req, resp, lifecycle);

        PortalApplication portalApp = (PortalApplication) FacesUtils.getBeanFromContext(facesContext, "ippPortalApp");

		Map<String, Object> params = CollectionUtils.newHashMap();
		params.put("modelId", modelId);
		params.put("processId", processId);

		String key = "processId=" + processId;

		portalApp.openViewById("modelerView", key, params, null, true);
	}

	public static void openViewById(String id, String key, Map<String, Object> params,
			ServletContext servletContext, HttpServletRequest req, HttpServletResponse resp) {

        FacesContextFactory contextFactory = (FacesContextFactory)FactoryFinder.getFactory(FactoryFinder.FACES_CONTEXT_FACTORY);
        LifecycleFactory lifecycleFactory = (LifecycleFactory)FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY);
        Lifecycle lifecycle = lifecycleFactory.getLifecycle(LifecycleFactory.DEFAULT_LIFECYCLE);

        FacesContext facesContext = contextFactory.getFacesContext(servletContext, req, resp, lifecycle);

        PortalApplication portalApp = (PortalApplication) FacesUtils.getBeanFromContext(facesContext, "ippPortalApp");

		portalApp.openViewById(id, key, params, (AbstractMessageBean) FacesUtils.getBeanFromContext(facesContext,"bpmModelerMessages"), true);
	}
}
