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
package org.eclipse.stardust.ui.web.viewscommon.process.history;

import java.util.List;

import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;


/**
 * @author Yogesh.Manware
 * 
 */
public interface IProcessHistoryDataModel
{
   /**
    * @param activityInstance
    * @param processInstances
    * @param includeEvents
    * @return
    */
   IProcessHistoryTableEntry getProcessHistoryDataModel(ActivityInstance activityInstance,
         List<ProcessInstance> processInstances, boolean includeEvents);

   /**
    * @param processInstance
    * @param processInstances
    * @param includeEvents
    * @return
    */
   IProcessHistoryTableEntry getProcessHistoryDataModel(ProcessInstance processInstance,
         List<ProcessInstance> processInstances, boolean includeEvents);

   /**
    * @param processInstance
    * @param processInstances
    * @param includeEvents
    * @return
    */
   IProcessHistoryTableEntry getActivityDataModel(ProcessInstance processInstance,
         List<ProcessInstance> processInstances, boolean includeEvents);

   /**
    * @param processInstance
    * @param includeEvents
    * @return
    */
   List<ProcessInstance> getAllProcesses(ProcessInstance processInstance, boolean includeEvents);

   /**
    * @param activityInstance
    * @return
    */
   ProcessInstance getCurrentProcessByActivityInstance(ActivityInstance activityInstance);

   /**
    * @return
    */
   boolean isDataModelRestricted();
}
