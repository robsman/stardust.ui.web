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
/**
 * @author Johnson.Quadras
 */

package org.eclipse.stardust.ui.web.rest.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.xml.namespace.QName;

import org.apache.commons.collections.CollectionUtils;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.ui.web.bcc.legacy.traffic.TrafficLightViewPropertyProvider;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.rest.service.dto.ColumnDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.SelectItemDTO;
import org.eclipse.stardust.ui.web.rest.service.utils.ProcessDefinitionUtils;
import org.eclipse.stardust.ui.web.rest.service.utils.TrafficLightUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.springframework.stereotype.Component;

@Component
public class TrafficLightService
{
   @Resource
   TrafficLightUtils trafficLightUtils;

   private static final Logger trace = LogManager.getLogger(TrafficLightService.class);

   /**
    * 
    * @return
    */
   public List<SelectItemDTO> getAllProcessesWithTrafficLight()
   {
      List<String> processesFQIds = TrafficLightViewPropertyProvider.getInstance().getAllProcessDefinitionIDs();

      List<SelectItemDTO> processSelectList = new ArrayList<SelectItemDTO>();

      for (int i = 0; i < processesFQIds.size(); i++)
      {
         String processFQId = processesFQIds.get(i);
         ProcessDefinition processDefinition = ProcessDefinitionUtils.getProcessDefinition(QName.valueOf(processFQId)
               .getLocalPart());
         processSelectList.add(new SelectItemDTO(processFQId, I18nUtils.getProcessName(processDefinition)));
      }
      return processSelectList;
   }

   /**
    * 
    * @param processQId
    * @return
    */
   public List< ? > getCateogries(String processQId)
   {
      ProcessDefinition processDefinition = ProcessDefinitionUtils.getProcessDefinition(QName.valueOf(processQId)
            .getLocalPart());

      return trafficLightUtils.getCateogries(processDefinition);
   }

   /**
    * 
    * @param processQId
    * @return
    */
   public List<ColumnDTO> getActivitiyColumns(String processQId)
   {
      List columns = TrafficLightViewPropertyProvider.getInstance()
            .getAllColumnIDs(processQId);
     List<ColumnDTO> activityCols = new ArrayList<ColumnDTO>();
      for (Object object : columns)
      {
         ColumnDTO dto = new ColumnDTO( String.valueOf(object) , String.valueOf(object));
         activityCols.add(dto);
      }
      return activityCols;
   }
   
   
   /**
    * 
    * @param processQId
    * @return
    */
   public List< ? > getTrafficLightData(String processQId)
   {
       processQId = "{QAWorkflowModel}TestParticipants_QAProcess";
      String category = null;
      
      List columns = TrafficLightViewPropertyProvider.getInstance()
            .getAllColumnIDs(processQId);
      List rows = TrafficLightViewPropertyProvider.getInstance()
            .getAllRowIDsAsList(processQId, category);
      
      return null;
   }

}
