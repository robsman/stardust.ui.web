/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.eclipse.stardust.ui.web.rest.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.dto.OrganizationDetails;
import org.eclipse.stardust.engine.api.dto.RoleDetails;
import org.eclipse.stardust.engine.api.dto.UserDetailsLevel;
import org.eclipse.stardust.engine.api.model.ModelParticipant;
import org.eclipse.stardust.engine.api.model.Organization;
import org.eclipse.stardust.engine.api.model.OrganizationInfo;
import org.eclipse.stardust.engine.api.model.Participant;
import org.eclipse.stardust.engine.api.model.ParticipantInfo;
import org.eclipse.stardust.engine.api.model.QualifiedModelParticipantInfo;
import org.eclipse.stardust.engine.api.model.QualifiedOrganizationInfo;
import org.eclipse.stardust.engine.api.model.Role;
import org.eclipse.stardust.engine.api.query.ParticipantAssociationFilter;
import org.eclipse.stardust.engine.api.query.UserDetailsPolicy;
import org.eclipse.stardust.engine.api.query.UserQuery;
import org.eclipse.stardust.engine.api.query.Users;
import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.engine.api.runtime.Department;
import org.eclipse.stardust.engine.api.runtime.DepartmentInfo;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.api.runtime.UserService;
import org.eclipse.stardust.ui.web.rest.service.dto.request.DepartmentDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.response.ParticipantDTO;
import org.eclipse.stardust.ui.web.rest.service.utils.ParticipantManagementUtils;
import org.eclipse.stardust.ui.web.rest.service.utils.ParticipantManagementUtils.ParticipantType;
import org.eclipse.stardust.ui.web.rest.service.utils.ServiceFactoryUtils;
import org.eclipse.stardust.ui.web.viewscommon.common.configuration.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.viewscommon.utils.ParticipantUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.UserUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

/**
 * @author Yogesh.Manware
 * @version $Revision: $
 * 
 */
@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.INTERFACES)
public class ParticipantServiceImpl implements ParticipantService
{
   private static final Logger trace = LogManager.getLogger(ParticipantServiceImpl.class);

   @Resource
   private ServiceFactoryUtils serviceFactoryUtils;

   @Resource
   private ParticipantManagementUtils participantManagementUtils;

   /**
    * @param participantQidIn
    * @return
    */
   public List<ParticipantDTO> getParticipant(String participantQidIn)
   {
      ParticipantContainer participantContainer = getParticipantContainerFromQialifiedId(participantQidIn);
      return getSubParticipants(participantContainer);
   }

   /**
    *
    */
   public ParticipantDTO createDepartment(DepartmentDTO departmentDTO)
   {
      ParticipantContainer participantContainer = getParticipantContainerFromQialifiedId(departmentDTO.parentOrganizationId);

      QualifiedModelParticipantInfo modelParticipant = participantContainer.modelparticipant;
      DepartmentInfo parentDepartment = participantContainer.department;

      AdministrationService adminService = serviceFactoryUtils.getAdministrationService();
      Department department = adminService.createDepartment(departmentDTO.id, departmentDTO.name,
            departmentDTO.description, parentDepartment, (OrganizationInfo) modelParticipant);

      List<ParticipantDTO> participants = new ArrayList<ParticipantDTO>();

      getParticipant(department, participants);

      return (ParticipantDTO) participants;
   }

   /**
    *
    */
   public ParticipantDTO modifyDepartment(DepartmentDTO departmentDTO)
   {
      String parentDepartmentId = ParticipantManagementUtils
            .parseParentDepartmentId(departmentDTO.parentOrganizationId);
      String participantQid = ParticipantManagementUtils.parseParticipantQId(departmentDTO.parentOrganizationId);

      ParticipantContainer participantContainer = getParticipantContainer(participantQid, parentDepartmentId,
            departmentDTO.id);

      DepartmentInfo departmentInfo = participantContainer.department;

      AdministrationService adminService = serviceFactoryUtils.getAdministrationService();
      Department department = adminService.modifyDepartment(departmentInfo.getOID(), departmentDTO.name,
            departmentDTO.description);

      List<ParticipantDTO> participantDTOs = new ArrayList<ParticipantDTO>();

      getSubParticipantsForDepartment(department, participantDTOs);

      return participantDTOs.get(0);
   }

   /**
    * @param departmentQualifiedId
    * @return
    */
   public boolean deleteDepartment(String departmentQualifiedId)
   {
      ParticipantContainer participantContainer = getParticipantContainerFromQialifiedId(departmentQualifiedId);
      serviceFactoryUtils.getAdministrationService().removeDepartment(participantContainer.department.getOID());
      return true;
   }

   /**
    * TODO: userId should also contain realm in future
    */
   @Override
   public Map<String, List<ParticipantDTO>> modifyParticipant(HashSet<String> participant, HashSet<String> add,
         HashSet<String> remove)
   {
      HashSet<String> allUsers = new HashSet<String>();
      if (CollectionUtils.isNotEmpty(add))
      {
         allUsers.addAll(add);
      }
      if (CollectionUtils.isNotEmpty(remove))
      {
         allUsers.addAll(remove);
      }

      List<User> users = UserUtils.getUsers(allUsers, null, UserDetailsLevel.Minimal);

      Map<String, List<ParticipantDTO>> participantsMap = new HashMap<String, List<ParticipantDTO>>();

      for (String participantQId : participant)
      {
         ParticipantContainer participantContainer = getParticipantContainerFromQialifiedId(participantQId);
         for (String userId : add)
         {
            // TODO: consider realmId
            User user = getUser(users, userId);
            if (participantContainer.participantType.equals(ParticipantType.DEPARTMENT.name()))
            {
               addUserToModelParticipant(user,
                     participantContainer.department
                           .getScopedParticipant((ModelParticipant) participantContainer.modelparticipant));
            }
            else
            {
               addUserToModelParticipant(user, participantContainer.modelparticipant);
            }
         }
         List<ParticipantDTO> participantDTOs = new ArrayList<ParticipantDTO>();
         participantDTOs.addAll(getSubParticipants(participantContainer));
         participantsMap.put(participantQId, participantDTOs);
      }
      return participantsMap;
   }

   /**
    * @param users
    * @param userId
    * @return
    */
   private User getUser(List<User> users, String userId)
   {
      // TODO: consider realmId

      for (User user : users)
      {
         if (user.getId().equals(userId))
         {
            return user;
         }
      }
      return null;
   }

   /**
    * @param user
    * @param qualifiedParticipantInfo
    */
   private void addUserToModelParticipant(User user, QualifiedModelParticipantInfo qualifiedParticipantInfo)
   {
      UserService userService = serviceFactoryUtils.getUserService();

      User userToModify = userService.getUser(user.getOID());
      userToModify.addGrant(qualifiedParticipantInfo);
      userService.modifyUser(userToModify);
   }

   /**
    * 
    * @param participantQidIn
    * @return
    */
   private ParticipantContainer getParticipantContainerFromQialifiedId(String participantQidIn)
   {
      String departmentId = ParticipantManagementUtils.parseDepartmentId(participantQidIn);
      String parentDepartmentId = ParticipantManagementUtils.parseParentDepartmentId(participantQidIn);
      String participantQid = ParticipantManagementUtils.parseParticipantQId(participantQidIn);
      return getParticipantContainer(participantQid, parentDepartmentId, departmentId);
   }

   /**
    * @param participantQid
    * @param parentDepartmentId
    * @param departmentId
    * @return
    */
   private ParticipantContainer getParticipantContainer(String participantQid, String parentDepartmentId,
         String departmentId)
   {
      DepartmentInfo departmentInfo = null;

      QualifiedModelParticipantInfo modelParticipant = ParticipantUtils.getModelParticipant(participantQid);

      String pType = ParticipantManagementUtils.getParticipantType(modelParticipant).name();

      if (StringUtils.isNotEmpty(parentDepartmentId))
      {
         // scoped participant
         List<Organization> superOrganizations = null;
         if (modelParticipant instanceof OrganizationDetails)
         {
            // Organization
            OrganizationDetails org = (OrganizationDetails) modelParticipant;

            if (StringUtils.isNotEmpty(departmentId))
            {
               // department
               pType = ParticipantType.DEPARTMENT.name();
               parentDepartmentId += "/" + departmentId;
               departmentInfo = participantManagementUtils.getDepartment(org, parentDepartmentId);
            }
            else
            {
               // organization under department
               superOrganizations = org.getAllSuperOrganizations();
            }
         }
         else if (modelParticipant instanceof RoleDetails)
         {
            // Role
            RoleDetails role = (RoleDetails) modelParticipant;
            superOrganizations = role.getAllSuperOrganizations();
         }

         if (superOrganizations != null)
         {
            for (Organization organization : superOrganizations)
            {
               departmentInfo = participantManagementUtils.getDepartment(organization, parentDepartmentId);
            }
            modelParticipant = getDepartment(departmentInfo.getOID()).getScopedParticipant(
                  (ModelParticipant) modelParticipant);
            pType = ParticipantManagementUtils.getParticipantType(modelParticipant).name();
         }
      }

      if (departmentId != null)
      {
         // department
         if (StringUtils.isEmpty(departmentId))
         {
            // default department
            pType = ParticipantType.DEPARTMENT_DEFAULT.name();
         }
         else if (departmentInfo == null)
         {
            // department
            pType = ParticipantType.DEPARTMENT.name();
            departmentInfo = participantManagementUtils.getDepartment((QualifiedOrganizationInfo) modelParticipant,
                  departmentId);
         }
      }

      Department department = null;
      if (departmentInfo != null)
      {
         department = getDepartment(departmentInfo.getOID());
      }

      ParticipantContainer participantContainer = new ParticipantContainer();
      participantContainer.modelparticipant = modelParticipant;
      participantContainer.department = department;
      participantContainer.participantType = pType;

      return participantContainer;
   }

   /**
    * @param modelparticipant
    * @param department
    * @param pTypeStr
    * @return
    */
   private List<ParticipantDTO> getSubParticipants(ParticipantContainer participantContainer)
   {
      QualifiedModelParticipantInfo modelparticipant = participantContainer.modelparticipant;
      Department department = participantContainer.department;
      String pTypeStr = participantContainer.participantType;

      ParticipantType pType = ParticipantType.valueOf(pTypeStr);

      List<ParticipantDTO> participantDTOs = new ArrayList<ParticipantDTO>();

      switch (pType)
      {
      case ORGANIZATION_UNSCOPED:
      case ORGANIZATON_SCOPED_IMPLICIT:
         getSubParticipantsForOrganization((QualifiedOrganizationInfo) modelparticipant, participantDTOs);
         break;
      case ORGANIZATON_SCOPED_EXPLICIT:
         getSubParticipantsForExplicitelyScopedOrganization((QualifiedOrganizationInfo) modelparticipant,
               participantDTOs, department);
         break;

      case ROLE_SCOPED:
      case ROLE_UNSCOPED:
         getUsers(modelparticipant, participantDTOs);
         break;

      case DEPARTMENT:
         getSubParticipantsForDepartment(department, participantDTOs);
         break;

      case DEPARTMENT_DEFAULT:
         getSubParticipantsForDefaultDepartment((QualifiedOrganizationInfo) modelparticipant, participantDTOs);
         break;

      default:
         if (trace.isDebugEnabled())
         {
            trace.debug("Not supported to expand: " + pTypeStr);
         }
         break;
      }

      return participantDTOs;
   }

   /**
    * @param qualifiedOrganizationInfo
    * @param participantDTOs
    */
   private void getSubParticipantsForOrganization(QualifiedOrganizationInfo qualifiedOrganizationInfo,
         List<ParticipantDTO> participantDTOs)
   {
      // Add all associated Users
      getUsers(qualifiedOrganizationInfo, participantDTOs);

      // Add all sub-Organizations
      getSubOrganizations(qualifiedOrganizationInfo, participantDTOs);

      // Add all sub-Roles
      getSubRoles(qualifiedOrganizationInfo, participantDTOs);
   }

   /**
    * @param qualifiedOrganizationInfo
    * @param participantDTOs
    * @param parentDepartment
    */
   private void getSubParticipantsForExplicitelyScopedOrganization(QualifiedOrganizationInfo qualifiedOrganizationInfo,
         List<ParticipantDTO> participantDTOs, Department parentDepartment)
   {
      List<Department> deptList = serviceFactoryUtils.getQueryService().findAllDepartments(
            qualifiedOrganizationInfo.getDepartment(), qualifiedOrganizationInfo);

      // Add Default Department
      ParticipantDTO participant = getParticipant(qualifiedOrganizationInfo, participantDTOs, parentDepartment, true);
      participant.type = ParticipantType.DEPARTMENT_DEFAULT.name();

      // Add all Departments
      for (Department department : deptList)
      {
         getParticipant(department, participantDTOs);
      }
   }

   /**
    * @param department
    * @param participantDTOs
    */
   private void getSubParticipantsForDepartment(Department department, List<ParticipantDTO> participantDTOs)
   {
      QualifiedModelParticipantInfo scopedOrganizationInfo = department.getScopedParticipant(department
            .getOrganization());

      // Add all associated Users
      getUsers(scopedOrganizationInfo, participantDTOs);

      // Add all sub-Organizations
      getSubOrganizations((QualifiedOrganizationInfo) scopedOrganizationInfo, participantDTOs);

      // Add all sub-Roles
      getSubRoles((QualifiedOrganizationInfo) scopedOrganizationInfo, participantDTOs);
   }

   /**
    * @param qualifiedOrganizationInfo
    * @param participantDTOs
    */
   private void getSubParticipantsForDefaultDepartment(QualifiedOrganizationInfo qualifiedOrganizationInfo,
         List<ParticipantDTO> participantDTOs)
   {
      Department department = getDepartmentSafely(qualifiedOrganizationInfo);

      // Add all associated Users
      getUsers(qualifiedOrganizationInfo, participantDTOs);

      // Add all sub-Organizations
      @SuppressWarnings("unchecked")
      List<Organization> subOrganizations = ((Organization) ParticipantUtils.getParticipant(qualifiedOrganizationInfo,
            UserDetailsLevel.Full)).getAllSubOrganizations();
      for (Organization subOrganization : subOrganizations)
      {
         ParticipantDTO participant = getParticipant(
               (QualifiedOrganizationInfo) ParticipantUtils.getScopedParticipant(subOrganization, department),
               participantDTOs, department, true);
         participant.type = ParticipantType.DEPARTMENT_DEFAULT.name();
      }

      // Add all sub-Roles
      getSubRoles(qualifiedOrganizationInfo, participantDTOs);
   }

   /**
    * @param participantInfo
    * @param participantDTOs
    */
   private void getUsers(ParticipantInfo participantInfo, List<ParticipantDTO> participantDTOs)
   {
      UserQuery userQuery = UserQuery.findAll();
      userQuery.getFilter().add(ParticipantAssociationFilter.forParticipant(participantInfo, false));
      UserDetailsPolicy userPolicy = new UserDetailsPolicy(UserDetailsLevel.Minimal);
      userPolicy.setPreferenceModules(UserPreferencesEntries.M_ADMIN_PORTAL);
      userQuery.setPolicy(userPolicy);
      Users allUsers = serviceFactoryUtils.getQueryService().getAllUsers(userQuery);
      for (User user : allUsers)
      {
         ParticipantDTO participantDTO = new ParticipantDTO(user);
         participantDTO.type = ParticipantType.USER.name();
         participantDTOs.add(participantDTO);
      }
   }

   /**
    * @param modelParticipantInfo
    * @param participantDTOs
    */
   private void getSubOrganizations(QualifiedModelParticipantInfo modelParticipantInfo,
         List<ParticipantDTO> participantDTOs)
   {
      Department department = getDepartmentSafely(modelParticipantInfo);

      Organization organization = (Organization) ParticipantUtils.getParticipant(modelParticipantInfo,
            UserDetailsLevel.Full);

      @SuppressWarnings("unchecked")
      List<Organization> subOrganizations = organization.getAllSubOrganizations();
      for (Organization subOrganization : subOrganizations)
      {
         getParticipant(ParticipantUtils.getScopedParticipant(subOrganization, department), participantDTOs,
               department, false);
      }
   }

   /**
    * @param participantDTOs
    * @param modelParticipantInfo
    */
   private void getSubRoles(QualifiedModelParticipantInfo modelParticipantInfo, List<ParticipantDTO> participantDTOs)
   {
      Department department = getDepartmentSafely(modelParticipantInfo);

      Organization organization = (Organization) ParticipantUtils.getParticipant(modelParticipantInfo,
            UserDetailsLevel.Full);

      @SuppressWarnings("unchecked")
      List<Role> subRoles = organization.getAllSubRoles();
      for (Role subRole : subRoles)
      {
         getParticipant(ParticipantUtils.getScopedParticipant(subRole, department), participantDTOs, department, false);
      }
   }

   /**
    * @param department
    * @param participantDTOs
    */
   private void getParticipant(Department department, List<ParticipantDTO> participantDTOs)
   {
      ParticipantDTO participantDTO = new ParticipantDTO(department);
      participantDTO.type = ParticipantType.DEPARTMENT.name();
      participantDTOs.add(participantDTO);

      if (department.getParentDepartment() != null)
      {
         participantDTO.uiQualifiedId = getDepartmentId(department.getParentDepartment())
               + department.getOrganization().getQualifiedId();
      }
      else
      {
         participantDTO.uiQualifiedId = department.getOrganization().getQualifiedId();
      }

      participantDTO.uiQualifiedId += "[" + department.getId() + "]";
   }

   /**
    * @param qualifiedParticipantInfo
    * @param participantDTOs
    * @param parentDepartment
    * @return
    */
   private ParticipantDTO getParticipant(QualifiedModelParticipantInfo qualifiedParticipantInfo,
         List<ParticipantDTO> participantDTOs, Department parentDepartment, boolean defaultDepartment)
   {
      Participant participant = ParticipantUtils.getParticipant(qualifiedParticipantInfo);
      ParticipantDTO participantDTO = new ParticipantDTO(participant);
      participantDTO.type = ParticipantManagementUtils.getParticipantType(qualifiedParticipantInfo).name();
      participantDTOs.add(participantDTO);
      participantDTO.uiQualifiedId = "";
      if (parentDepartment != null)
      {
         participantDTO.uiQualifiedId = getDepartmentId(parentDepartment);
      }

      if (defaultDepartment)
      {
         participantDTO.uiQualifiedId += qualifiedParticipantInfo.getQualifiedId() + "[]";
      }
      else
      {
         participantDTO.uiQualifiedId += qualifiedParticipantInfo.getQualifiedId();
      }

      return participantDTO;
   }

   /**
    * @param parentDepartment
    * @return
    */
   private String getDepartmentId(Department parentDepartment)
   {
      return "[" + ParticipantManagementUtils.getDepartmentsHierarchy(parentDepartment, "") + "]";
   }

   /**
    * @param modelParticipantInfo
    * @return
    */
   private Department getDepartmentSafely(QualifiedModelParticipantInfo modelParticipantInfo)
   {
      if (modelParticipantInfo.getDepartment() != null)
      {
         return getDepartment(modelParticipantInfo.getDepartment().getOID());
      }
      return null;
   }

   /**
    * @param departmentOid
    * @return
    */
   private Department getDepartment(long departmentOid)
   {
      Department department = serviceFactoryUtils.getAdministrationService().getDepartment(departmentOid);
      return department;
   }

   static class ParticipantContainer
   {
      QualifiedModelParticipantInfo modelparticipant;
      Department department;
      String participantType;
   }
}