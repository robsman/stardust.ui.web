/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Anoop.Nair (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.rest.service;

import static org.eclipse.stardust.ui.web.viewscommon.utils.ActivityInstanceUtils.isAuxiliaryActivity;
import static org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDefinitionUtils.getAllAccessibleProcessDefinitionsfromAllVersions;
import static org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDefinitionUtils.isAuxiliaryProcess;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.model.Activity;
import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference;
import org.eclipse.stardust.ui.web.rest.service.dto.ActivityDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.DescriptorColumnDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.ProcessDefinitionDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.builder.DTOBuilder;
import org.eclipse.stardust.ui.web.rest.service.utils.ModelUtils;
import org.eclipse.stardust.ui.web.rest.service.utils.ProcessDefinitionUtils;
import org.eclipse.stardust.ui.web.viewscommon.descriptors.DescriptorColumnUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.springframework.stereotype.Component;
/**
 * @author Anoop.Nair
 * @author Johnson.Quadras
 * @version $Revision: $
 */
@Component
public class ProcessDefinitionService
{

	@Resource
	private ProcessDefinitionUtils processDefinitionUtils;
	
	@Resource
	private ModelUtils modelUtils;

	/**
	 * @return
	 */
	public List<ProcessDefinitionDTO> getStartableProcesses()
	{
		List<ProcessDefinition> startableProcesses = processDefinitionUtils
				.getStartableProcesses();

		List<ProcessDefinitionDTO> startableProcessesDTO = buildProcessesDTO( startableProcesses, true);
		
		return startableProcessesDTO;
	}

	/**
	 * @param onlyFilterable
	 * @return
	 */
	public List<DescriptorColumnDTO> getDescriptorColumns(Boolean onlyFilterable)
	{
		Map<String, DataPath> descriptors = processDefinitionUtils.getAllDescriptors(onlyFilterable);
		List<ColumnPreference> descriptorCols = DescriptorColumnUtils.createDescriptorColumns(null, descriptors);

		return DTOBuilder.buildList(descriptorCols, DescriptorColumnDTO.class);
	}


	/**
	 * Returns all the accessible Processes
	 * @return
	 */
	public List<ProcessDefinitionDTO> getAllProcesses(boolean excludeActivities)
	{
		List<ProcessDefinition> allAccessibleProcesses = getAllAccessibleProcessDefinitionsfromAllVersions();
		return buildProcessesDTO( allAccessibleProcesses, excludeActivities);
	}

	/**
	 * Build the process DTO list from the process definitions 
	 * @param allProcesses
	 * @return
	 */
	private List<ProcessDefinitionDTO> buildProcessesDTO(
			List<ProcessDefinition> allProcesses, boolean excludeActivities) {
		
		List<ProcessDefinitionDTO> processDTOList = CollectionUtils.newArrayList();
		
		for (ProcessDefinition processDefinition : allProcesses) {
			
			ProcessDefinitionDTO processDTO = buildProcessDTO(processDefinition);
			if(!excludeActivities){
				List<ActivityDTO> activitiesDTO = buildActivitiesDTO(processDefinition);
				processDTO.activities = activitiesDTO;
			}
			processDTOList.add(processDTO);
		}
		return processDTOList;
	}

	/**
	 * Builds a list of Activities DTO from Process Definition
	 * @param processDefinition
	 * @return List<ActivityDTO>
	 */
	private List<ActivityDTO> buildActivitiesDTO(ProcessDefinition processDefinition) {
		List<ActivityDTO> activitiesDTO = CollectionUtils.newArrayList();
		for (Object activityObj : processDefinition.getAllActivities()) {
			
			Activity activity = (Activity) activityObj;
			 ActivityDTO activityDTO = DTOBuilder.build(activity, ActivityDTO.class);
			 activityDTO.auxillary = isAuxiliaryActivity(activity);
			 activityDTO.name = I18nUtils.getActivityName(activity);
			 activitiesDTO.add(activityDTO);
		}
		return activitiesDTO;
	}

	/**
	 * Return a process Definition DTO for a process Definition
	 * @param processDefinition
	 * @return
	 */
	private ProcessDefinitionDTO buildProcessDTO(
			ProcessDefinition processDefinition) {
		String modelName = I18nUtils.getModelName(modelUtils.getModel(processDefinition.getModelOID()));
		
		ProcessDefinitionDTO processDTO = DTOBuilder.build(processDefinition, ProcessDefinitionDTO.class);
		processDTO.auxillary = isAuxiliaryProcess(processDefinition);
		processDTO.modelName = modelName;
		processDTO.name = I18nUtils.getProcessName(processDefinition);
		return processDTO;
	}


}
