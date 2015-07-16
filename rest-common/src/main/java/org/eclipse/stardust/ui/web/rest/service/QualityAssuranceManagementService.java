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
 * 
 * @author Johnson.Quadras
 *
 */
package org.eclipse.stardust.ui.web.rest.service;

import static org.eclipse.stardust.ui.web.viewscommon.utils.ActivityUtils.filterAccessibleActivities;

import java.util.List;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.dto.DepartmentInfoDetails;
import org.eclipse.stardust.engine.api.dto.QualityAssuranceAdminServiceFacade;
import org.eclipse.stardust.engine.api.model.Activity;
import org.eclipse.stardust.engine.api.model.ModelParticipant;
import org.eclipse.stardust.engine.api.model.OrganizationInfo;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.runtime.Department;
import org.eclipse.stardust.engine.api.runtime.DepartmentInfo;
import org.eclipse.stardust.engine.api.runtime.DeployedModel;
import org.eclipse.stardust.engine.api.runtime.DeployedModelDescription;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.ui.web.rest.service.dto.QualityAssuranceActivityDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.QualityAssuranceDepartmentDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.builder.DTOBuilder;
import org.eclipse.stardust.ui.web.rest.service.dto.request.QualityAssuranceRequestDTO;
import org.eclipse.stardust.ui.web.rest.service.utils.ActivityUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ParticipantUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDefinitionUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.QualityAssuranceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ServiceFactoryUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class QualityAssuranceManagementService
{
   private QualityAssuranceAdminServiceFacade qualityAssuranceAdminService = null;
   private static final Logger trace = LogManager.getLogger(QualityAssuranceManagementService.class);
   /**
    * 
    * @param processQualifiedId
    * @param activityQualiedId
    * @return
    */
   public List<QualityAssuranceDepartmentDTO> getDepartments(String processQId, String activityQId)
   {

      Activity activity = ActivityUtils.getActivity(processQId, activityQId);
      ModelParticipant participant = activity.getDefaultPerformer();
      List<QualityAssuranceDepartmentDTO> departments = CollectionUtils.newList();

      // detect if the participant is scoped
      if (participant.isDepartmentScoped() && participant instanceof OrganizationInfo)
      {
         OrganizationInfo orgInfo = (OrganizationInfo) participant;
         List<Department> deptList;
         QueryService qs = ServiceFactoryUtils.getQueryService();
         deptList = qs.findAllDepartments(orgInfo.getDepartment(), orgInfo);

         for (Department department : deptList)
         {
            QualityAssuranceDepartmentDTO dto = DTOBuilder.build(department, QualityAssuranceDepartmentDTO.class);
            Integer qaPercentage = qualityAssuranceAdminService.getQualityAssuranceParticipantProbability(activity,
                  department);
            dto.orgRuntimeId = department.getOrganization().getRuntimeElementOID();
            if (qaPercentage != null)
            {
               dto.qaPercentage = QualityAssuranceUtils.getStringValueofQAProbability(qaPercentage);
            }
            departments.add(dto);
         }
      }
      return departments;

   }

   /**
    * 
    * @param showObsoleteActivities
    * @return
    */
   public List<QualityAssuranceActivityDTO> getQaActivities(Boolean showObsoleteActivities)
   {

      if (null == qualityAssuranceAdminService)
      {
         qualityAssuranceAdminService = ServiceFactoryUtils.getQualityCheckAdminServiceFacade();
      }

      List<QualityAssuranceActivityDTO> activityEntries = CollectionUtils.newList();
      trace.debug("Getting QA activities for obsolete Activities :" + showObsoleteActivities);
      if (showObsoleteActivities) // Show old model's activities
      {
         activityEntries = getQAActivityEntries();
      }
      else
      {
         activityEntries = getQaActivitiesForActiveModels();
      }
      return activityEntries;
   }

   /**
    * 
    * @param request
    * @throws Exception
    */
   public void updateQaProbabilities(QualityAssuranceRequestDTO request) throws Exception
   {

      // save activity level QA percentage
      for (QualityAssuranceActivityDTO activityEntry : request.activities)
      {

         Activity activity = ActivityUtils.getActivity(activityEntry.processQualifiedId,
               activityEntry.activityQualifiedId);
         qualityAssuranceAdminService.setQualityAssuranceParticipantProbability(activity, null,
               QualityAssuranceUtils.getIntegerValueofQAProbability(activityEntry.qaPercentage));
      }

      // save department level QA percentage
      for (QualityAssuranceDepartmentDTO departmentEnrty : request.departments)
      {
         DepartmentInfo departmentInfo = new DepartmentInfoDetails(departmentEnrty.oid, departmentEnrty.id,
               departmentEnrty.name, departmentEnrty.orgRuntimeId);
         Activity activity = ActivityUtils.getActivity(departmentEnrty.processQualifiedId,
               departmentEnrty.activityQualifiedId);
         qualityAssuranceAdminService.setQualityAssuranceParticipantProbability(activity, departmentInfo,
               QualityAssuranceUtils.getIntegerValueofQAProbability(departmentEnrty.qaPercentage));
      }

   }

   /**
    * returns all activities across all model versions without having duplicate
    * activity-performer pair
    * 
    * @param ws
    * @param processQualifiedId
    * @param doFilterAccess
    * @return
    */
   private List<QualityAssuranceActivityDTO> getQAActivityEntries()
   {
      List<QualityAssuranceActivityDTO> activityEntries = CollectionUtils.newList();
      WorkflowService workflowService = ServiceFactoryUtils.getWorkflowService();
      // get all models
      List<DeployedModel> models = CollectionUtils.newArrayList(ModelUtils.getAllModels());
      List<ProcessDefinition> processes;
      List<Activity> activities, filteredActivities;

      for (DeployedModel model : models)
      {
         // get all process definitions from the model
         processes = model.getAllProcessDefinitions();
         for (ProcessDefinition processDefinition : processes)
         {
            // get all activities from process
            activities = processDefinition.getAllActivities();
            filteredActivities = filterAccessibleActivities(workflowService, activities);
            // search quality assured activities
            for (Activity activity : filteredActivities)
            {
               if (activity.isQualityAssuranceEnabled())
               {
                  QualityAssuranceActivityDTO entry = buildQaActivityDTO(activity);
                  if (!model.isActive())
                  {
                     entry.oldModel = true;
                  }
                  activityEntries.add(entry);
               }
            }

         }
      }
      return activityEntries;
   }

   private QualityAssuranceActivityDTO buildQaActivityDTO(Activity activity)
   {

      DeployedModelDescription model = ModelUtils.getModel(activity.getModelOID());
      QualityAssuranceActivityDTO entry = DTOBuilder.build(activity, QualityAssuranceActivityDTO.class);
      entry.modelName = I18nUtils.getModelName(model);
      entry.processName = I18nUtils.getProcessName(ProcessDefinitionUtils.getProcessDefinition(activity
            .getProcessDefinitionId()));
      entry.activityName = I18nUtils.getActivityName(activity);
      entry.processQualifiedId = ProcessDefinitionUtils.getProcessDefinition(activity.getProcessDefinitionId())
            .getQualifiedId();
      entry.defaultPerformer = I18nUtils.getParticipantName(activity.getDefaultPerformer());
      Integer qaProbability = qualityAssuranceAdminService.getQualityAssuranceParticipantProbability(activity, null);
      if (qaProbability != null)
      {
         entry.qaPercentage = QualityAssuranceUtils.getStringValueofQAProbability(qaProbability);
      }
      entry.performerType = ParticipantUtils.getParticipantType(activity.getDefaultPerformer()).toString();
      return entry;
   }

   /**
    * 
    * @return
    */
   private List<QualityAssuranceActivityDTO> getQaActivitiesForActiveModels()
   {
      // show only latest activities
      List<ProcessDefinition> allProcesses = ProcessDefinitionUtils.getAllProcessDefinitionsOfActiveModels();
      List<QualityAssuranceActivityDTO> activityEntries = CollectionUtils.newList();
      List<Activity> activities;
      for (ProcessDefinition processDefinition : allProcesses)
      {
         activities = processDefinition.getAllActivities();
         for (Activity activity : activities)
         {
            if (activity.isQualityAssuranceEnabled())
            {
               QualityAssuranceActivityDTO entry = buildQaActivityDTO(activity);
               activityEntries.add(entry);
            }
         }
      }
      return activityEntries;
   }

}
