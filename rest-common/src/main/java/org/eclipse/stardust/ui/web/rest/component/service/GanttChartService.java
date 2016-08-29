/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.rest.component.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.rest.dto.ActivityInstanceDTO;
import org.eclipse.stardust.ui.web.rest.dto.GanttChartDTO;
import org.eclipse.stardust.ui.web.rest.dto.ProcessInstanceDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Johnson.Quadras
 * @version $Revision: $
 */
@Component
public class GanttChartService
{
   private static final Logger trace = LogManager.getLogger(GanttChartService.class);

   @Resource
   private org.eclipse.stardust.ui.web.rest.component.util.ProcessInstanceUtils processInstanceUtilsREST;

   @Autowired
   private ActivityInstanceService activityInstanceService;

   @Autowired
   private ProcessInstanceService processInstanceService;

   /**
    * 
    * @param processOid
    * @param findParent
    * @param findAllChildren
    * @return
    */
   public GanttChartDTO getGanttChart(Long processOid, boolean fetchRootProcess, boolean findAllChildren)
   {
      ProcessInstance originalProcess =  processInstanceUtilsREST.getProcessInstance(processOid, false, false);
      ProcessInstance process = originalProcess;
     
      if(fetchRootProcess && originalProcess.getRootProcessInstanceOID() != originalProcess.getOID()) {
         process = processInstanceUtilsREST.getProcessInstance(originalProcess.getRootProcessInstanceOID(), false, false);
      }
      
      ProcessInstanceDTO processDTO = processInstanceUtilsREST.buildProcessInstanceDTO(process);
      GanttChartDTO processGanttChartInfo  = new GanttChartDTO(processDTO);

      // Get child info
      addChildren(findAllChildren, processGanttChartInfo);
      
      return processGanttChartInfo;
   }

   /**
    * 
    * @param findAllChildren
    * @param processGanttChartInfo
    * @param activities
    */
   private void addChildren(boolean findAllChildren,
         GanttChartDTO processGanttChartInfo)
   {
      List<ActivityInstanceDTO> activities = activityInstanceService.getByProcessOid(processGanttChartInfo.oid);
      for (ActivityInstanceDTO activityInstanceDTO : activities)
      {
         if(activityInstanceDTO.activity.implementationTypeId == "Subprocess")
         {
            ProcessInstanceDTO subProcess = (ProcessInstanceDTO) processInstanceService.findByStartingActivityOid(activityInstanceDTO.activityOID);
            GanttChartDTO subProcessDTO = null;
            if(findAllChildren) 
            {
                subProcessDTO  = getGanttChart(subProcess.oid, false, true); 
            }
            else 
            {
               subProcessDTO  = new GanttChartDTO(subProcess);
            }
            processGanttChartInfo.addChildren(subProcessDTO);
         }
         else
         {
            GanttChartDTO activityInfo  = new GanttChartDTO(activityInstanceDTO);
            processGanttChartInfo.addChildren(activityInfo);
         }
      }
   }
}
