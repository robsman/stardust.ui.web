/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Yogesh.Manware (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.rest.service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.engine.api.model.ParticipantInfo;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.Department;
import org.eclipse.stardust.ui.web.rest.service.dto.NotificationMap;

/**
 * @author Yogesh.Manware
 * @version $Revision: $
 */
public interface IDelegationHandler extends Serializable
{

   public NotificationMap delegateActivities(List<ActivityInstance> activities, Department toDepartment,
         Map<String, Object> params);

   public NotificationMap delegateActivities(List<ActivityInstance> activities, ParticipantInfo toParticipantInfo,
         Map<String, Object> params);
}