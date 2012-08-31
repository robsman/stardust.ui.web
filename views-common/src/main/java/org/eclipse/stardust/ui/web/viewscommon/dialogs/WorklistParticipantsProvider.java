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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.model.OrganizationInfo;
import org.eclipse.stardust.engine.api.model.Participant;
import org.eclipse.stardust.engine.api.model.ParticipantInfo;
import org.eclipse.stardust.engine.api.model.QualifiedModelParticipantInfo;
import org.eclipse.stardust.engine.api.model.RoleInfo;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.Department;
import org.eclipse.stardust.engine.api.runtime.DepartmentInfo;
import org.eclipse.stardust.engine.api.runtime.PerformerType;
import org.eclipse.stardust.ui.web.viewscommon.utils.ParticipantUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ParticipantWorklistCacheManager;
import org.eclipse.stardust.ui.web.viewscommon.utils.RegExUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ServiceFactoryUtils;

/**
 * @author Yogesh.Manware
 * 
 */
public class WorklistParticipantsProvider extends DefaultDelegatesProvider implements IDepartmentProvider
{
   private static final long serialVersionUID = -60843897962877048L;
   private static final String DEPARTMENTS = "Departments";

   private List<QualifiedModelParticipantInfo> worklistParticipants;

   /**
    * 
    */
   public WorklistParticipantsProvider()
   {
      ParticipantWorklistCacheManager.getInstance().reset();
      worklistParticipants = ParticipantUtils.fetchAllParticipants(true);
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.eclipse.stardust.ui.web.viewscommon.dialogs.IDelegatesProvider#findDelegates
    * (java.util.List,
    * org.eclipse.stardust.ui.web.viewscommon.dialogs.IDelegatesProvider.Options)
    */
   public Map<PerformerType, List< ? extends ParticipantInfo>> findDelegates(List<ActivityInstance> activityInstances,
         IDelegatesProvider.Options options)
   {
      Map<PerformerType, List< ? extends ParticipantInfo>> result = CollectionUtils.newMap();
      List<Participant> modelParticipantList = CollectionUtils.newList();

      for (QualifiedModelParticipantInfo participantInfo : worklistParticipants)
      {
         if (participantInfo instanceof RoleInfo)
         {
            if (options.getPerformerTypes().contains(ROLE_TYPE))
            {
               modelParticipantList.add(ParticipantUtils.getParticipant(participantInfo));
            }
         }
         else if (participantInfo instanceof OrganizationInfo)
         {
            if (options.getPerformerTypes().contains(ORGANIZATION_TYPE))
            {
               modelParticipantList.add(ParticipantUtils.getParticipant(participantInfo));
            }
         }
      }

      String filterValue = options.getNameFilter();
      String regex = null;

      if (!StringUtils.isEmpty(filterValue))
      {
         regex = RegExUtils.escape(filterValue.toLowerCase()).replaceAll("\\*", ".*") + ".*";
      }

      List<Participant> matchingModelParticipants = CollectionUtils.newList();
      // filter participants if we search for a string

      if (StringUtils.isNotEmpty(regex))
      {
         for (Participant participant : modelParticipantList)
         {
            if (participant.getName().toLowerCase().matches(regex))
            {
               matchingModelParticipants.add(participant);
            }
         }
         modelParticipantList = matchingModelParticipants;
      }

      result.put(PerformerType.ModelParticipant, modelParticipantList);
      return result;
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.eclipse.stardust.ui.web.viewscommon.dialogs.IDepartmentProvider#findDepartments
    * (java.util.List,
    * org.eclipse.stardust.ui.web.viewscommon.dialogs.IDepartmentProvider.Options)
    */
   public Map<String, Set<DepartmentInfo>> findDepartments(List<ActivityInstance> activityInstances,
         IDepartmentProvider.Options options)
   {
      Set<DepartmentInfo> departments = getAllDepartments();

      // If it is an AutoComplete
      String regex = !StringUtils.isEmpty(options.getNameFilter()) ? options.getNameFilter().replaceAll("\\*", ".*")
            + ".*" : null;

      Set<DepartmentInfo> matchingDepartment = new HashSet<DepartmentInfo>();

      if (StringUtils.isNotEmpty(regex))
      {
         for (DepartmentInfo departmentInfo : departments)
         {
            if (StringUtils.isEmpty(regex) || departmentInfo.getName().matches(regex))
            {
               matchingDepartment.add(departmentInfo);
            }
         }
         departments = matchingDepartment;
      }

      Map<String, Set<DepartmentInfo>> finalList = new HashMap<String, Set<DepartmentInfo>>();
      finalList.put(DEPARTMENTS, departments);
      return finalList;
   }

   private Set<DepartmentInfo> getAllDepartments()
   {
      Set<DepartmentInfo> departmentInfos = findAllDepartments(null, null);
      return departmentInfos;
   }

   /**
    * 
    * @param deptInfo
    * @param orgInfo
    * @return
    */
   private Set<DepartmentInfo> findAllDepartments(DepartmentInfo deptInfo, OrganizationInfo orgInfo)
   {
      List<Department> departments = ServiceFactoryUtils.getQueryService().findAllDepartments(deptInfo, orgInfo);
      Set<DepartmentInfo> departmentInfos = CollectionUtils.newHashSet();
      for (Department dept : departments)
      {
         departmentInfos.add(dept);
         departmentInfos.addAll(findAllDepartments(dept, null));
      }

      return departmentInfos;
   }
}