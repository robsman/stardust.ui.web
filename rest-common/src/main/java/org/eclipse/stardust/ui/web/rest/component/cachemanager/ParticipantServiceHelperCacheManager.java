/*******************************************************************************
 * Copyright (c) 2016 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.rest.component.cachemanager;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.engine.api.dto.UserDetailsLevel;
import org.eclipse.stardust.engine.api.model.QualifiedOrganizationInfo;
import org.eclipse.stardust.engine.api.query.UserDetailsPolicy;
import org.eclipse.stardust.engine.api.query.UserQuery;
import org.eclipse.stardust.engine.api.runtime.Department;
import org.eclipse.stardust.engine.api.runtime.DepartmentInfo;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.ui.web.rest.component.service.ParticipantService;
import org.eclipse.stardust.ui.web.rest.component.util.ParticipantManagementUtils.ParticipantType;
import org.eclipse.stardust.ui.web.rest.component.util.ServiceFactoryUtils;
import org.eclipse.stardust.ui.web.rest.dto.response.ParticipantDTO;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

/**
 * request scope cache in order to reduce the number of engine calls 
 * while constructing participant tree
 * 
 * @author Yogesh.Manware
 * @version $Revision: $
 */
@Component("participantServiceHelperCacheManager")
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ParticipantServiceHelperCacheManager
{
   @Resource
   private ServiceFactoryUtils serviceFactoryUtils;

   @Resource
   private ParticipantService participantService;

   private Map<String, List<Department>> orgDepartments = new HashMap<String, List<Department>>();

   private Map<Long, Department> oidDepartment = new HashMap<Long, Department>();

   private Map<String, Set<ParticipantDTO>> participantUsersMap = new HashMap<String, Set<ParticipantDTO>>();

   //TODO remove this flag post testing, in next build
   private Boolean cachingOn = false;

   public ParticipantServiceHelperCacheManager()
   {
      cachingOn = !Boolean.valueOf((String)Parameters.instance().get("portal.disableParticipantTreeCache"));
   }

   public void reset()
   {
      oidDepartment.clear();
      orgDepartments.clear();
      participantUsersMap.clear();
   }

   /**
    * @param organization
    * @return
    */
   public List<Department> getDepartments(QualifiedOrganizationInfo organization)
   {
      List<Department> departments = orgDepartments.get(organization.getQualifiedId());

      if (departments == null || !cachingOn)
      {
         departments = serviceFactoryUtils.getQueryService().findAllDepartments(null, organization);
         orgDepartments.put(organization.getQualifiedId(), departments);
         for (Department department : departments)
         {
            oidDepartment.put(department.getOID(), department);
         }
      }
      return departments;
   }

   /**
    * @param organization
    * @param departmentId
    * @return
    */
   public DepartmentInfo getDepartment(QualifiedOrganizationInfo organization, String departmentId)
   {
      List<Department> deptList = getDepartments(organization);
      DepartmentInfo departmentInfo = null;

      for (Department department2 : deptList)
      {
         String deps = getDepartmentsHierarchy(department2, "");
         if (deps.equals(departmentId.trim()))
         {
            departmentInfo = department2;
            break;
         }
      }
      return departmentInfo;
   }

   /**
    * @param department2
    * @param departmentName
    * @return
    */
   public String getDepartmentsHierarchy(Department department2, String departmentName)
   {
      if (department2 == null)
      {
         return departmentName;
      }

      departmentName = department2.getId() + "/" + departmentName;

      if (department2.getParentDepartment() != null)
      {
         return getDepartmentsHierarchy(department2.getParentDepartment(), departmentName);
      }

      return departmentName.substring(0, departmentName.length() - 1);
   }

   /**
    * @param departmentOid
    * @return
    */
   public Department getDepartment(long departmentOid)
   {
      Department department = oidDepartment.get(departmentOid);
      if (department == null || !cachingOn)
      {
         department = serviceFactoryUtils.getAdministrationService().getDepartment(departmentOid);
         oidDepartment.put(departmentOid, department);
      }
      return department;
   }

   /**
    * @param participantInfo
    * @param participantDTOs
    * @return
    */
   private List<User> getUsers()
   {
      UserQuery userQuery = UserQuery.findAll();
      UserDetailsPolicy userPolicy = new UserDetailsPolicy(UserDetailsLevel.Full);
      userPolicy
            .setPreferenceModules(org.eclipse.stardust.ui.web.viewscommon.common.configuration.UserPreferencesEntries.M_ADMIN_PORTAL);
      userQuery.setPolicy(userPolicy);
      return serviceFactoryUtils.getQueryService().getAllUsers(userQuery);
   }

   /**
    * @param allUsers
    */
   private void setAllUsers(Map<User, List<ParticipantDTO>> allUsers)
   {
      for (User user : allUsers.keySet())
      {
         List<ParticipantDTO> participants = allUsers.get(user);
         for (ParticipantDTO participantDTO : participants)
         {
            if (!participantUsersMap.containsKey(participantDTO.uiQualifiedId))
            {
               participantUsersMap.put(participantDTO.uiQualifiedId, new HashSet<ParticipantDTO>());
            }
            ParticipantDTO userDTO = new ParticipantDTO(user);
            userDTO.type = ParticipantType.USER.name();
            participantUsersMap.get(participantDTO.uiQualifiedId).add(userDTO);
         }
      }
   }

   /**
    * @param fullQId
    * @return
    */
   public Collection< ? extends ParticipantDTO> getUsers(String fullQId)
   {
      if (CollectionUtils.isEmpty(participantUsersMap) || !cachingOn)
      {
         List<User> users = getUsers();
         Map<User, List<ParticipantDTO>> userPartiMap = new HashMap<User, List<ParticipantDTO>>();
         for (User user : users)
         {
            userPartiMap.put(user, participantService.getUserGrants(user));
         }
         setAllUsers(userPartiMap);
      }

      if (participantUsersMap.get(fullQId) != null)
      {
         return participantUsersMap.get(fullQId);
      }

      return null;
   }
}
