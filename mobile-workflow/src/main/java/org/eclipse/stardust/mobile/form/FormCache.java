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

package org.eclipse.stardust.mobile.form;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.stardust.engine.api.model.ApplicationContext;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;

/**
 * To avoid duplicate generation of forms in one session.
 * 
 * @author Ellie.Sepehri
 *
 */
public class FormCache {
	public static Map<String, ManualActivityForm> formMap = new HashMap<String, ManualActivityForm>();

	/**
	 * 
	 * @param activityInstance
	 * @param applicationContext
	 * @param binding
	 * @param workflowService
	 * @return
	 */
	public ManualActivityForm getForm(ActivityInstance activityInstance, ApplicationContext applicationContext, String binding, WorkflowService workflowService)
	{
		ManualActivityForm form = formMap.get(activityInstance.getActivity().getId());
		
		if (form == null)
		{
//			formMap.put(activityInstance.getActivity().getId(), form = new ManualActivityForm(new AjaxFormGenerator(null, binding), workflowService,
//				applicationContext));
		}
		
		return form;
	}
}
