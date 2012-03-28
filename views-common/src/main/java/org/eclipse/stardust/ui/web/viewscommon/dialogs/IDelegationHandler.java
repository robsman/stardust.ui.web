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
package org.eclipse.stardust.ui.web.viewscommon.dialogs;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.faces.FacesException;
import org.eclipse.stardust.engine.api.model.ParticipantInfo;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.Department;


public interface IDelegationHandler extends Serializable
{

   public List<ActivityInstance> delegateActivities(List<ActivityInstance> activities, Department toDepartment,
         Map<String, Object> params) throws FacesException;

   public List<ActivityInstance> delegateActivities(List<ActivityInstance> activities,
         ParticipantInfo toParticipantInfo, Map<String, Object> params) throws FacesException;
}