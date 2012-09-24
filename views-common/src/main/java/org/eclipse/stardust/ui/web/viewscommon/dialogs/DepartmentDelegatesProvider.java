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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.engine.api.model.Organization;
import org.eclipse.stardust.engine.api.model.OrganizationInfo;
import org.eclipse.stardust.engine.api.model.Participant;
import org.eclipse.stardust.engine.api.model.ParticipantInfo;
import org.eclipse.stardust.engine.api.model.Role;
import org.eclipse.stardust.engine.api.model.RoleInfo;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.engine.api.runtime.Department;
import org.eclipse.stardust.engine.api.runtime.DepartmentInfo;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ServiceFactoryUtils;



/**
 * @author ankita.patel
 * @version $Revision: $
 */
public class DepartmentDelegatesProvider implements Serializable, IDepartmentProvider
{

   private static final long serialVersionUID = 2762028882465821463L;

   public static final DepartmentDelegatesProvider INSTANCE = new DepartmentDelegatesProvider();

   private static final String DEPARTMENTS = "Departments";
   
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

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.viewscommon.dialogs.IDepartmentProvider#findDepartments(java.util.List, org.eclipse.stardust.ui.web.viewscommon.dialogs.IDepartmentProvider.Options)
    */
   public Map<String, Set<DepartmentInfo>> findDepartments(List<ActivityInstance> activityInstances, Options options)
   {
      if (CollectionUtils.isEmpty(activityInstances))
      {
         return findDepartments(options);
      }
      else
      {
         return findDepartments_(activityInstances, options);
      }
   }

   /**
    * @param options
    * @return
    */
   private Map<String, Set<DepartmentInfo>> findDepartments(IDepartmentProvider.Options options)
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

   /**
    * @param activityInstances
    * @param options
    * @return
    */
   private Map<String, Set<DepartmentInfo>> findDepartments_(
         List<ActivityInstance> activityInstances, Options options)
     {
      
      Map<String, Set<DepartmentInfo>> finalList = new HashMap<String, Set<DepartmentInfo>>();
      Set<DepartmentInfo> finalDeptList = null;
      for (int i = 0; i < activityInstances.size(); ++i)
      {
         ActivityInstance ai = (ActivityInstance) activityInstances.get(i);

         // Get the current performer for the AI
         ParticipantInfo currentPerformer = ai.getCurrentPerformer();
         Department department = null;
         Department parentDepartment = null;
         
         if (ai.getProcessInstance().isCaseProcessInstance())
         {
            finalDeptList=getAllDepartments();
         }
         else if (currentPerformer instanceof ModelParticipantInfo)
         {
            // Get department and parent department for current participant performer
            ModelParticipantInfo modelParticipantInfo = (ModelParticipantInfo) currentPerformer;
            DepartmentInfo departmentInfo = modelParticipantInfo.getDepartment();
            if (departmentInfo != null)
            {
               department = getAdministrationService().getDepartment(
                     departmentInfo.getOID());
               if (department != null)
               {
                  parentDepartment = department.getParentDepartment();
               }
            }

            if (currentPerformer instanceof OrganizationInfo)
            {
               OrganizationInfo org = (OrganizationInfo) currentPerformer;
               // Find all sibling departments (departments at same level)
               List<Department> candidateDepartments = getQueryService()
                     .findAllDepartments(parentDepartment, org);
               // Add "common" departments to list
               finalDeptList = intersection(finalDeptList, candidateDepartments);
            }
            else if (currentPerformer instanceof RoleInfo)
            {
               RoleInfo roleInfo = (RoleInfo) currentPerformer;
               Participant participant = ModelUtils.getModelCache().getParticipant(
                     roleInfo.getId());
               if (participant instanceof Role)
               {
                  Role role = (Role) participant;
                  List<Organization> organizations = role.getAllSuperOrganizations();
                  for (Organization org : organizations)
                  {
                     // Find all sibling departments (departments at same level)
                     List<Department> candidateDepartments = getQueryService()
                           .findAllDepartments(parentDepartment, org);
                     // Add "common" departments to list
                     finalDeptList = intersection(finalDeptList, candidateDepartments);
                  }
               }
            }
         }

      }
      if (finalDeptList == null)
      {
         finalDeptList = CollectionUtils.newHashSet();
      }
      String regex = !StringUtils.isEmpty(options.getNameFilter()) ? options
            .getNameFilter().replaceAll("\\*", ".*") + ".*" : null;
      Set<DepartmentInfo> matchingDepartment = new HashSet<DepartmentInfo>();
      if (regex != null)
      {
         for (DepartmentInfo departmentInfo : finalDeptList)
         {
            if (StringUtils.isEmpty(regex) || departmentInfo.getName().matches(regex))
            {
               matchingDepartment.add(departmentInfo);
            }
         }
         finalDeptList = matchingDepartment;
      }

      finalList.put(DEPARTMENTS, finalDeptList);

      return finalList;
   }
   
   /**
    * Retains the elements in lhs which also exist in rhs. lhs is modified. If lhs is
    * null, a new set is created
    * 
    * @param lhs
    * @param rhs
    * @return the modified lhs
    */
   private static Set<DepartmentInfo> intersection(Set<DepartmentInfo> lhs,
         List<Department> rhs)
   {
      if (lhs == null)
      {
         lhs = new HashSet<DepartmentInfo>(rhs);
      }
      else
      {
         lhs.retainAll(rhs);
      }

      return lhs;
   }

   private static QueryService getQueryService()
   {
      return ServiceFactoryUtils.getQueryService();
   }

   private static AdministrationService getAdministrationService()
   {
      return ServiceFactoryUtils.getAdministrationService();
   }
}
