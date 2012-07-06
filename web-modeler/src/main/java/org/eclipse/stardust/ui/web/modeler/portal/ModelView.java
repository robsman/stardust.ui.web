/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.stardust.ui.web.modeler.portal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.common.event.ViewEvent;
import org.eclipse.stardust.ui.web.common.event.ViewEventHandler;
import org.eclipse.stardust.ui.web.modeler.service.ModelService;
import org.springframework.stereotype.Component;

@Component
public class ModelView implements ViewEventHandler {
	private ModelService modelService;
	private ModelType model;

	public ModelService getModelService() {
		return modelService;
	}

	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}

	public ModelType getModel() {
		return model;
	}

	public void setModel(ModelType model) {
		this.model = model;
	}

	/**
	 * 
	 * @return
	 */
	public List getVersions()
	{
		List list = new ArrayList();
		
		return list;
	}
	
	/**
	 * 
	 * @return
	 */
	public List getNotes()
	{
		List list = new ArrayList();
		
		return list;
	}

	/**
	 * 
	 * @return
	 */
	public List getProblems()
	{
		List list = new ArrayList();

		Map map = new HashMap();

		list.add(map);

		map.put("componentType", "Process Definition");
		map.put("componentName", "OMNI Platform Services/GetPlan");
		map.put("Synopsis", "No start activity defined");

		map = new HashMap();
		
		list.add(map);
		
		map.put("componentType", "Message Transformation Application");
		map.put("componentName", "OMNI Platform Services/GetPlan Input Mapping");
		map.put("Synopsis", "Undefined identified \"planNumb\"");
		
		return list;
	}

	/**
	 * 
	 * @return
	 */
	public List getWorkItems()
	{
		List list = new ArrayList();
		
		return list;
	}

	public void handleEvent(ViewEvent event) {
		String modelId = event.getView().getParamValue("modelId");

		switch (event.getType())
		{
		case TO_BE_ACTIVATED:
			setModel(getModelService().getModel(modelId));
			//PortalApplication.getInstance().getPortalUiController().loadPerspective("ippBpmModeler");

			break;
		case TO_BE_DEACTIVATED:
			break;
		case CLOSED:
			break;
		case LAUNCH_PANELS_ACTIVATED:
		case LAUNCH_PANELS_DEACTIVATED:
		case FULL_SCREENED:
		case RESTORED_TO_NORMAL:
			break;
		}
	}
}
