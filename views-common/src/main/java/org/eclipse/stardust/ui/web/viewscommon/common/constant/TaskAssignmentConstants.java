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
package org.eclipse.stardust.ui.web.viewscommon.common.constant;

import org.eclipse.stardust.engine.api.model.PredefinedConstants;

/**
 * @author rsauer
 * @version $Revision$
 */
public interface TaskAssignmentConstants
{
   String TASK_ASSIGNMENT_SCOPE = PredefinedConstants.ENGINE_SCOPE + "tasks:assignment:";
   
   String ASSIGNMENT_MODE = TASK_ASSIGNMENT_SCOPE + "mode";

   String WORK_MODE_WORKSHOP = "workshop";
   String WORK_MODE_ASSEMBLY_LINE = "assemblyLine";
   
   
   String WORKLIST_RELEVANCY = TASK_ASSIGNMENT_SCOPE + "worklistRelevancy";

   String WORKLIST_RELEVANCY_NONE = "none";
   String WORKLIST_RELEVANCY_ACTIVITY_CONTAINER = "activityContainer";
   String WORKLIST_RELEVANCY_ACTIVITY_ROOT = "activityRoot";
}
