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

import static org.eclipse.stardust.ui.web.viewscommon.utils.ActivityInstanceUtils.isAuxiliaryActivity;
import static org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDefinitionUtils.getAllAccessibleProcessDefinitionsfromAllVersions;
import static org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDefinitionUtils.isAuxiliaryProcess;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Resource;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.dto.DataDetails;
import org.eclipse.stardust.engine.api.model.Activity;
import org.eclipse.stardust.engine.api.model.Data;
import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.engine.api.model.ParameterMapping;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.model.Trigger;
import org.eclipse.stardust.engine.api.runtime.DeployedModel;
import org.eclipse.stardust.engine.extensions.dms.data.DocumentType;
import org.eclipse.stardust.ui.web.common.util.StringUtils;
import org.eclipse.stardust.ui.web.rest.component.util.DocumentUtils;
import org.eclipse.stardust.ui.web.rest.component.util.ModelUtils;
import org.eclipse.stardust.ui.web.rest.component.util.ProcessDefinitionUtils;
import org.eclipse.stardust.ui.web.rest.dto.ActivityDTO;
import org.eclipse.stardust.ui.web.rest.dto.DataPathDTO;
import org.eclipse.stardust.ui.web.rest.dto.DescriptorColumnDTO;
import org.eclipse.stardust.ui.web.rest.dto.DocumentDataDTO;
import org.eclipse.stardust.ui.web.rest.dto.DocumentTypeDTO;
import org.eclipse.stardust.ui.web.rest.dto.ProcessDefinitionDTO;
import org.eclipse.stardust.ui.web.rest.dto.builder.DTOBuilder;
import org.eclipse.stardust.ui.web.rest.dto.builder.DocumentTypeDTOBuilder;
import org.eclipse.stardust.ui.web.rest.util.DescriptorColumnUtils;
import org.eclipse.stardust.ui.web.rest.util.DescriptorColumnUtils.ColumnDataType;
import org.eclipse.stardust.ui.web.viewscommon.descriptors.DescriptorFilterUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;
import org.springframework.stereotype.Component;
/**
 * @author Anoop.Nair
 * @author Johnson.Quadras
 * @version $Revision: $
 */
@Component
public class ProcessDefinitionService
{

   private static final String SCAN_TRIGGER = "scan";
   
	@Resource
	private ProcessDefinitionUtils processDefinitionUtils;

	@Resource
	private ModelUtils modelUtils;
	
	@Resource
	private DocumentUtils documentUtils;


   /**
    * @return
    */
   public List<ProcessDefinitionDTO> getStartableProcesses(String triggerType)
   {
      List<ProcessDefinition> startableProcesses = CollectionUtils.newArrayList();
      if (StringUtils.isNotEmpty(triggerType))
      {
         List<ProcessDefinition> processes = processDefinitionUtils.findStatable(triggerType);
         for (ProcessDefinition procDef : processes)
         {
            List<Trigger> triggers = procDef.getAllTriggers();
            for (Trigger triggerDetails : triggers)
            {
               if (triggerType.equals(triggerDetails.getType()))
               {
                  startableProcesses.add(procDef);
                  break;
               }
            }
         }

      }
      else
      {
         startableProcesses = processDefinitionUtils.getStartableProcesses();
      }

      List<ProcessDefinitionDTO> startableProcessesDTO = buildProcessesDTO(startableProcesses, true);

      return startableProcessesDTO;
   }
   
	/**
	 * @param onlyFilterable
	 * @return
	 */
	public List<DescriptorColumnDTO> getDescriptorColumns(Boolean onlyFilterable)
	{
		Map<String, DataPath> descriptors = processDefinitionUtils.getAllDescriptors(onlyFilterable);
		List<DescriptorColumnDTO> descriptorCols = createDescriptorColumns( descriptors);
		return descriptorCols;
	}


	  /**
	    * creates filterable columns on the provided table
	    * @param table
	    * @param allDescriptors
	    * @return
	    */
	   private static List<DescriptorColumnDTO> createDescriptorColumns( Map<String, DataPath> allDescriptors)
	   {

	      List<DescriptorColumnDTO> descriptorColumns = new ArrayList<DescriptorColumnDTO>();

	      for (Entry<String, DataPath> descriptor : allDescriptors.entrySet())
	      {
	         String descriptorId = descriptor.getKey();
	         DataPath dataPath = descriptor.getValue();

	         Class<?> mappedType = dataPath.getMappedType();

	         ColumnDataType columnType = DescriptorColumnUtils.determineColumnType(mappedType);
	         String type = columnType.toString();
	         
	         String detailedType = type;
	         if(columnType == ColumnDataType.NUMBER ) 
            {
	            detailedType = DescriptorColumnUtils.determineNumberDataType(mappedType) != null ? 
	                  DescriptorColumnUtils.determineNumberDataType(mappedType).toString() : null;
            }
	         
	         // double and float are not sortable
	         boolean sortable = DescriptorFilterUtils.isDataSortable(dataPath);
	         boolean filterable = DescriptorFilterUtils.isDataFilterable(dataPath);
	            
	         DescriptorColumnDTO descriptorColumn = new DescriptorColumnDTO(descriptorId,
	               I18nUtils.getDataPathName(dataPath), type, detailedType, sortable, filterable);
	         descriptorColumns.add(descriptorColumn);
	      }
	      return descriptorColumns;

	   }

   public List<DocumentDataDTO> getAllDocumentData(String processDefinitionId)
   {
      List<DocumentDataDTO> allDocumentData = CollectionUtils.newArrayList();
      Set<String> dataIds = CollectionUtils.newHashSet();
      ProcessDefinition processDefinition = processDefinitionUtils.getProcessDefinition(processDefinitionId);
      List<Trigger> triggers = processDefinition.getAllTriggers();
      for (Trigger triggerDetails : triggers)
      {
         if (SCAN_TRIGGER.equals(triggerDetails.getType()))
         {
            for (ParameterMapping mapping : triggerDetails.getAllParameterMappings())
            {
               dataIds.add(mapping.getDataId());
            }
         }
      }

      if(CollectionUtils.isNotEmpty(dataIds))
      {
         DeployedModel model = ModelCache.findModelCache().getModel(processDefinition.getModelOID());
         List<Data> allData =  model.getAllData();
         
         for (Data data : allData)
         {
            if (dataIds.contains(data.getId()))
            {
               DataDetails dataDetails = (DataDetails) data;
               DocumentType documentType =  org.eclipse.stardust.ui.web.viewscommon.utils.ModelUtils.getDocumentTypeFromData(model, dataDetails);
               DocumentDataDTO documentDataDTO = DTOBuilder.build(dataDetails, DocumentDataDTO.class);
               DocumentTypeDTO documentTypeDTO = DocumentTypeDTOBuilder.build(documentType);
               documentDataDTO.documentType = documentTypeDTO;
               allDocumentData.add(documentDataDTO);
            }
         }
      }
      
      return allDocumentData;
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
			processDTO.dataPaths = buildDataPathDTO(processDefinition);
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
			 activityDTO.runtimeElementOid = activity.getRuntimeElementOID();
			 activitiesDTO.add(activityDTO);
		}
		return activitiesDTO;
	}

   /**
    * Return a process Definition DTO for a process Definition
    * @param processDefinition
    * @return
    */
	private ProcessDefinitionDTO buildProcessDTO(ProcessDefinition processDefinition)
   {
      String modelName = I18nUtils.getModelName(modelUtils.getModel(processDefinition.getModelOID()));

      ProcessDefinitionDTO processDTO = DTOBuilder.build(processDefinition, ProcessDefinitionDTO.class);
      processDTO.auxillary = isAuxiliaryProcess(processDefinition);
      processDTO.modelName = modelName;
      processDTO.name = I18nUtils.getProcessName(processDefinition);
      
      return processDTO;
   }

	private List<DataPathDTO> buildDataPathDTO(ProcessDefinition processDefinition)
	{
	   List<DataPath> dataPaths = processDefinition.getAllDataPaths();
	   List<DataPathDTO> dataPathsDTO = CollectionUtils.newArrayList();
	      for(DataPath dataPath : dataPaths)
	      {
	         DataPathDTO dataPathDTO = DTOBuilder.build(dataPath, DataPathDTO.class);
	         dataPathsDTO.add(dataPathDTO);
	      }
	      return dataPathsDTO;
	}
   /**
    * @param procDefIDs
    * @param onlyFilterable
    */
   public List<DescriptorColumnDTO> getCommonDescriptors(List<String> procDefIDs, Boolean onlyFilterable)
   {
      List<ProcessDefinition> processDefinitions = ProcessDefinitionUtils.getProcessDefinitions(procDefIDs);
      DataPath[] descriptors = ProcessDefinitionUtils.getCommonDescriptors(processDefinitions, onlyFilterable);
      Map<String, DataPath> commonDescriptors = ProcessDefinitionUtils.getDataPathMap(descriptors);
      List<DescriptorColumnDTO> descriptorCols = createDescriptorColumns(commonDescriptors);
      return descriptorCols;
   }

   /**
    * Returns all the accessible Processes
    * 
    * @return
    */
   public List<ProcessDefinitionDTO> getAllUniqueProcess(boolean excludeActivities)
   {
      List<ProcessDefinition> allAccessibleProcesses = org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDefinitionUtils
            .getAllAccessibleProcessDefinitions();
      return buildProcessesDTO(allAccessibleProcesses, excludeActivities);
   }

   /**
    * Returns all the accessible Processes
    * 
    * @return
    */
   public List<ProcessDefinitionDTO> getAllBusinessRelevantProcesses(boolean excludeActivities)
   {
      List<ProcessDefinition> allAccessibleProcesses = org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDefinitionUtils
            .getAllBusinessRelevantProcesses();
      return buildProcessesDTO(allAccessibleProcesses, excludeActivities);
   }
}
