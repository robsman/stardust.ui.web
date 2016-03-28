/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.eclipse.stardust.ui.web.rest.component.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.ws.rs.core.Response.Status;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.InvalidArgumentException;
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
import org.eclipse.stardust.engine.api.query.UserGroups;
import org.eclipse.stardust.engine.api.query.UserQuery;
import org.eclipse.stardust.engine.api.query.Users;
import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.engine.api.runtime.Department;
import org.eclipse.stardust.engine.api.runtime.DepartmentInfo;
import org.eclipse.stardust.engine.api.runtime.Grant;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.api.runtime.UserGroup;
import org.eclipse.stardust.engine.api.runtime.UserService;
import org.eclipse.stardust.ui.web.rest.component.util.ParticipantManagementUtils;
import org.eclipse.stardust.ui.web.rest.component.util.ParticipantManagementUtils.ParticipantType;
import org.eclipse.stardust.ui.web.rest.component.util.ServiceFactoryUtils;
import org.eclipse.stardust.ui.web.rest.component.util.UserGroupUtils;
import org.eclipse.stardust.ui.web.rest.dto.DataTableOptionsDTO;
import org.eclipse.stardust.ui.web.rest.dto.ModelDTO;
import org.eclipse.stardust.ui.web.rest.dto.request.DepartmentDTO;
import org.eclipse.stardust.ui.web.rest.dto.response.ParticipantDTO;
import org.eclipse.stardust.ui.web.rest.exception.PortalRestException;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.common.configuration.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentMgmtUtility;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.ParticipantUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
   public static final String CASE_PERFORMER = "{PredefinedModel}CasePerformer";
   private static final Logger trace = LogManager.getLogger(ParticipantServiceImpl.class);

   @Resource
   private ServiceFactoryUtils serviceFactoryUtils;

   @Resource
   private ParticipantManagementUtils participantManagementUtils;

   @Autowired
   private ModelServiceBean modelService;
   
   @Resource
   private UserGroupUtils userGroupUtils;
   
   /**
    * @param participantQidIn
    * @return
    * @throws Exception 
    */
   public List<ParticipantDTO> getParticipantTree(boolean lazyLoad)
   {
      // get Active Models along with top level Participants
      List<ModelDTO> models = modelService.getModelParticipants();
      List<ParticipantDTO> allParticipantDTOs = new ArrayList<ParticipantDTO>();

      for (ModelDTO modelDTO : models)
      {
         // convert to ParticipantDTO to keep the response uniform
         ParticipantDTO modelParticipantDTO = new ParticipantDTO(modelDTO);
         allParticipantDTOs.add(modelParticipantDTO);

         Iterator<ParticipantDTO> participantIterator = modelParticipantDTO.children.iterator();
         while (participantIterator.hasNext())
         {
            ParticipantDTO participantDTO = participantIterator.next();
            if (CASE_PERFORMER.equals(participantDTO.qualifiedId))
            {
               participantIterator.remove();
               if (lazyLoad)
               {
                  break;
               }
               else
               {
                  continue;
               }
            }
            if (!lazyLoad)
            {
               populateChildrenRecursively(participantDTO);
            }
         }
      }
      
      //get User Groups
      allParticipantDTOs.add(getUserGroupDTOs());
      Collections.sort(allParticipantDTOs);
      
      return allParticipantDTOs;
   }
   
   /**
    *
    */
   public List<ParticipantDTO> getUserGrants(String account)
   {
      User user = null;
      if (StringUtils.isNotEmpty(account))
      {
         user = serviceFactoryUtils.getUserService().getUser(account);
      }
      else
      {
         user = SessionContext.findSessionContext().getUser();
         // get the true copy of user, above fetches the user from session
         user = serviceFactoryUtils.getUserService().getUser(user.getAccount());
      }
      return getUserGrants(user);
   }

   /**
    * @param user
    * @return
    */
   private List<ParticipantDTO> getUserGrants(User user)
   {
      List<ParticipantDTO> grants = new ArrayList<ParticipantDTO>();
      List<Grant> roleOrgReportDefinitionsGrants = DocumentMgmtUtility.getRoleOrgReportDefinitionsGrants(user);

      for (Grant grant : roleOrgReportDefinitionsGrants)
      {
         ParticipantDTO participantDTO = null;
         QualifiedModelParticipantInfo modelParticipant = ParticipantUtils.getModelParticipant(grant.getQualifiedId());

         // organization or department
         if (grant.isOrganization())
         {
            if (grant.getDepartment() != null)
            {
               // specific department
               if (grant.getQualifiedId().equals(grant.getDepartment().getOrganization().getQualifiedId()))
               {
                  participantDTO = getParticipant(grant.getDepartment());
               }
               // default department under specific department
               else
               {
                  if (modelParticipant.definesDepartmentScope())
                  {
                     participantDTO = getParticipant(modelParticipant, grant.getDepartment(), true);
                  }
                  else
                  {
                     // implicit organization
                     participantDTO = getParticipant(modelParticipant, grant.getDepartment(), false);
                  }
               }
            }
            else
            {
               // default department
               if (modelParticipant.isDepartmentScoped())
               {
                  participantDTO = getParticipant(modelParticipant, null, true);
               }
               // un-scoped organization
               else
               {
                  participantDTO = getParticipant(modelParticipant, null, false);
               }
            }
         }
         else
         {
            // role 
            participantDTO = getParticipant(modelParticipant, grant.getDepartment(), false);
         }

         grants.add(participantDTO);
      }

      return grants;
   }
   
   /**
    * @return
    */
   private ParticipantDTO getUserGroupDTOs()
   {
      UserGroups userGroups = userGroupUtils.getAllUserGroups(new DataTableOptionsDTO());
      ParticipantDTO userGroupDTOs = new ParticipantDTO();
      userGroupDTOs.id = "_internal_user_groups_";
      userGroupDTOs.name = MessagesViewsCommonBean.getInstance().get("views.participantTree.userGroup.label");
      userGroupDTOs.children = new ArrayList<ParticipantDTO>();
      userGroupDTOs.type = "USERGROUPS";
      for (UserGroup userGroup : userGroups)
      {
         ParticipantDTO userGroupDTO = new ParticipantDTO(userGroup);
         userGroupDTO.uiQualifiedId = userGroupDTO.id;
         userGroupDTOs.children.add(userGroupDTO);

         List<ParticipantDTO> userDTOs = new ArrayList<ParticipantDTO>();
         getUsers(userGroup, userDTOs);
         userGroupDTO.children = userDTOs;
         Collections.sort(userGroupDTO.children);
      }
      Collections.sort(userGroupDTOs.children);
      return userGroupDTOs;
   }
   
   
   /**
    * @param participantDTO
    */
   private void populateChildrenRecursively(ParticipantDTO participantDTO)
   {
      String fullId = participantDTO.uiQualifiedId;
      if (StringUtils.isEmpty(fullId))
      {
         fullId = participantDTO.qualifiedId;
      }
      ParticipantContainer participantContainer = getParticipantContainerFromQialifiedId(fullId);
      participantDTO.children = getSubParticipants(participantContainer);
      for (ParticipantDTO childParticipantDTO : participantDTO.children)
      {
         if (!ParticipantType.USER.name().equals(childParticipantDTO.type))
         {
            populateChildrenRecursively(childParticipantDTO);
         }
      }
   }
   
   /**
    * @param participantQidIn
    * @return
    */
   public List<ParticipantDTO> getSubParticipants(String participantQidIn, boolean lazyLoad)
   {
      ParticipantContainer participantContainer = getParticipantContainerFromQialifiedId(participantQidIn);
      return getSubParticipants(participantContainer, lazyLoad);
   }
   
   /**
    * @param participantQidIn
    * @return
    */
   @Override
   public List<ParticipantDTO> getParticipantDTOFromQualifiedId(List<String> participantIds)
   {
      List<ParticipantDTO> participantDTOs = new ArrayList<ParticipantDTO>();

      for (String participantQidIn : participantIds)
      {
         ParticipantContainer participantContainer = getParticipantContainerFromQialifiedId(participantQidIn);
         QualifiedModelParticipantInfo modelparticipant = participantContainer.modelparticipant;
         Department department = participantContainer.department;
         String pTypeStr = participantContainer.participantType;

         ParticipantType pType = ParticipantType.valueOf(pTypeStr);

         ParticipantDTO participantDTO = null;

         switch (pType)
         {
         case ORGANIZATION_UNSCOPED:
         case ORGANIZATON_SCOPED_IMPLICIT:
         case ORGANIZATON_SCOPED_EXPLICIT:
         case ROLE_SCOPED:
         case ROLE_UNSCOPED:
            participantDTO = getParticipant(modelparticipant, department, false);
            break;

         case DEPARTMENT:
         case DEPARTMENT_DEFAULT:
            participantDTO = getParticipant(department);
            break;

         case USERGROUP:
            participantDTO = new ParticipantDTO(
                  userGroupUtils.getUserGroup(participantContainer.dynamicParticipantInfoId));
            break;

         default:
            if (trace.isDebugEnabled())
            {
               trace.debug("Not supported to expand: " + pTypeStr);
            }
            break;
         }

         participantDTOs.add(participantDTO);
      }
      return participantDTOs;
   }

   /**
    * @param participantContainer
    * @param lazyLoad
    * @return
    */
   private List<ParticipantDTO> getSubParticipants(ParticipantContainer participantContainer, boolean lazyLoad)
   {
      List<ParticipantDTO> participantDTOs = getSubParticipants(participantContainer);
      if (!lazyLoad)
      {
         for (ParticipantDTO childParticipantDTO : participantDTOs)
         {
            if (!ParticipantType.USER.name().equals(childParticipantDTO.type))
            {
               populateChildrenRecursively(childParticipantDTO);
            }
         }
      }
      return participantDTOs;
   }
   
   /**
    * create or modify department
    * 
    */
   public ParticipantDTO createModifyDepartment(DepartmentDTO departmentDTO, boolean lazyLoad)
   {
      ParticipantContainer participantContainer = getParticipantContainerFromQialifiedId(departmentDTO.uiQualifiedId);
      QualifiedModelParticipantInfo modelParticipant = participantContainer.modelparticipant;
      DepartmentInfo parentDepartment = participantContainer.department;

      AdministrationService adminService = serviceFactoryUtils.getAdministrationService();

      if (ParticipantType.ORGANIZATON_SCOPED_EXPLICIT.name().equals(participantContainer.participantType))
      {
         // create department
         // check if department with same id already exist
         DepartmentInfo department = participantManagementUtils.getDepartment(
               (QualifiedOrganizationInfo) modelParticipant, departmentDTO.id);
         // we should not re-create the department
         if (department != null)
         {
            throw new PortalRestException(Status.BAD_REQUEST, MessagesViewsCommonBean.getInstance().getString(
                  "views.participantTree.duplicateDepartment.error"));
         }
         Department updatedDepartment = adminService.createDepartment(departmentDTO.id, departmentDTO.name,
               departmentDTO.description, parentDepartment, (OrganizationInfo) modelParticipant);
         ParticipantDTO participantDTO = getParticipant(updatedDepartment);
         if (!lazyLoad)
         {
            populateChildrenRecursively(participantDTO);
         }
         return participantDTO;
      }
      else if (ParticipantType.DEPARTMENT.name().equals(participantContainer.participantType))
      {
         // modify department
         DepartmentInfo departmentInfo = participantContainer.department;
         Department updatedDepartment = adminService.modifyDepartment(departmentInfo.getOID(), departmentDTO.name,
               departmentDTO.description);
         return getParticipant(updatedDepartment);
      }
      
      return null;
   }

   /**
    * @param departmentQualifiedId
    * @return
    */
   public boolean deleteDepartment(String departmentQualifiedId)
   {
      try
      {
         ParticipantContainer participantContainer = getParticipantContainerFromQialifiedId(departmentQualifiedId);
         serviceFactoryUtils.getAdministrationService().removeDepartment(participantContainer.department.getOID());
      }
      catch (InvalidArgumentException aex)
      {
         throw new PortalRestException(Status.BAD_REQUEST, MessagesViewsCommonBean.getInstance().getString(
               "views.participantTree.deleteDepartment.error.inUse"));
      }
      catch (Exception ex)
      {
         throw new PortalRestException(Status.BAD_REQUEST, MessagesViewsCommonBean.getInstance().getString(
               "views.participantTree.deleteDepartment.error.generic"), ex);
      }

      return true;
   }

   /**
    * TODO: userId should also contain realm in future
    */
   @Override
   public Map<String, List<ParticipantDTO>> modifyParticipant(HashSet<String> participant, HashSet<String> add,
         HashSet<String> remove)
   {
      HashSet<String> selectedUsers = new HashSet<String>();

      if (CollectionUtils.isNotEmpty(add))
      {
         selectedUsers.addAll(add);
      }
      if (CollectionUtils.isNotEmpty(remove))
      {
         selectedUsers.addAll(remove);
      }

      Set<User> users = getAllSelectedUsers(selectedUsers);

      Map<String, List<ParticipantDTO>> participantsMap = new HashMap<String, List<ParticipantDTO>>();

      Map<String, ParticipantContainer> participantContainers = new HashMap<String, ParticipantContainer>();

      for (String participantQId : participant)
      {
         ParticipantContainer participantContainer = getParticipantContainerFromQialifiedId(participantQId);
         if (CollectionUtils.isNotEmpty(add))
         {
            for (String userId : add)
            {
               User user = getUser(users, userId);
               if (participantContainer.participantType.equals(ParticipantType.DEPARTMENT.name()))
               {
                  user.addGrant(participantContainer.department
                        .getScopedParticipant((ModelParticipant) participantContainer.modelparticipant));
               }
               else if (participantContainer.participantType.equals(ParticipantType.USERGROUP.name()))
               {
                  user.joinGroup(participantContainer.dynamicParticipantInfoId);
               }
               else
               {
                  user.addGrant(participantContainer.modelparticipant);
               }
            }
         }

         if (CollectionUtils.isNotEmpty(remove))
         {
            for (String userId : remove)
            {
               User user = getUser(users, userId);
               if (participantContainer.participantType.equals(ParticipantType.DEPARTMENT.name()))
               {
                  user.removeGrant(participantContainer.department
                        .getScopedParticipant((ModelParticipant) participantContainer.modelparticipant));
               }
               else if (participantContainer.participantType.equals(ParticipantType.USERGROUP.name()))
               {
                  user.leaveGroup(participantContainer.dynamicParticipantInfoId);
               }
               else
               {
                  user.removeGrant(participantContainer.modelparticipant);
               }
            }
         }
         participantContainers.put(participantQId, participantContainer);
      }
      
      updateSelectedUsers(users);

      for (String pId : participantContainers.keySet())
      {
         List<ParticipantDTO> participantDTOs = new ArrayList<ParticipantDTO>();
         List<ParticipantDTO> newUsers = new ArrayList<ParticipantDTO>();
         participantDTOs.addAll(getSubParticipants(participantContainers.get(pId)));
         for (ParticipantDTO participantDTO : participantDTOs)
         {
            if (ParticipantType.USER.name().equals(participantDTO.type))
            {
               newUsers.add(participantDTO);
            }
         }
         participantsMap.put(pId, newUsers);
      }
      return participantsMap;
   }

   /**
    * @param users
    * @param userId
    * @return
    */
   private User getUser(Set<User> users, String userId)
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
    * @param usersList
    * @return
    */
   private Set<User> getAllSelectedUsers(HashSet<String> allUsers)
   {
      // TODO: consider realmId
      Users usersList = UserUtils.getUsers(allUsers, null, UserDetailsLevel.Minimal);
      Set<User> users = new HashSet<User>();
      UserService userService = serviceFactoryUtils.getUserService();
      for (User user2 : usersList)
      {
         users.add(userService.getUser(user2.getOID()));
      }
      return users;
   }

   /**
    * @param users
    */
   private void updateSelectedUsers(Set<User> users)
   {
      UserService userService = serviceFactoryUtils.getUserService();
      for (User user2 : users)
      {
         userService.modifyUser(user2);
      }
   }
   
   /**
    * 
    * @param participantQidIn
    * @return
    */
   public ParticipantContainer getParticipantContainerFromQialifiedId(String participantQidIn)
   {
      String departmentId = ParticipantManagementUtils.parseDepartmentId(participantQidIn);
      String parentDepartmentId = ParticipantManagementUtils.parseParentDepartmentId(participantQidIn);
      String participantQid = ParticipantManagementUtils.parseParticipantQId(participantQidIn);

      // assumed it is UserGroup (dynamic participant)
      if (StringUtils.isEmpty(participantQid))
      {
         ParticipantContainer participantContainer = new ParticipantContainer();
         participantContainer.dynamicParticipantInfoId = participantQidIn;
         participantContainer.participantType = ParticipantManagementUtils.ParticipantType.USERGROUP.name();
         return participantContainer;
      }
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
            departmentInfo = getDepartmentInfoFromSuperOrganizations(superOrganizations, parentDepartmentId);
            
            if (departmentInfo != null)
            {
               modelParticipant = getDepartment(departmentInfo.getOID()).getScopedParticipant(
                     (ModelParticipant) modelParticipant);
            }
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
    * retrieve department by traversing through hierarchy of parent organizations
    * 
    * @param superOrganizations
    * @param parentDepartmentId
    * @return
    */
   private DepartmentInfo getDepartmentInfoFromSuperOrganizations(List<Organization> superOrganizations,
         String parentDepartmentId)
   {
      DepartmentInfo departmentInfo = null;
      for (Organization organization : superOrganizations)
      {
         departmentInfo = participantManagementUtils.getDepartment(organization, parentDepartmentId);
         if (departmentInfo != null)
         {
            return departmentInfo;
         }
      }

      for (Organization organization : superOrganizations)
      {
         return getDepartmentInfoFromSuperOrganizations(organization.getAllSuperOrganizations(), parentDepartmentId);
      }

      return null;
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

      case USERGROUP:
         getUsers(userGroupUtils.getUserGroup(participantContainer.dynamicParticipantInfoId), participantDTOs);
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
      Collections.sort(participantDTOs);
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
      participantDTOs.add(getParticipant(qualifiedOrganizationInfo, parentDepartment, true));

      // Add all Departments
      for (Department department : deptList)
      {
         participantDTOs.add(getParticipant(department));
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
         participantDTOs.add(getParticipant(
               (QualifiedOrganizationInfo) ParticipantUtils.getScopedParticipant(subOrganization, department),
               department, true));
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
         participantDTOs.add(getParticipant(ParticipantUtils.getScopedParticipant(subOrganization, department),
               department, false));
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
         participantDTOs.add(getParticipant(ParticipantUtils.getScopedParticipant(subRole, department), department,
               false));
      }
   }

   /**
    * @param department
    * @param participantDTOs
    */
   private ParticipantDTO getParticipant(Department department)
   {
      ParticipantDTO participantDTO = new ParticipantDTO(department);
      participantDTO.type = ParticipantType.DEPARTMENT.name();

      if (department.getParentDepartment() != null)
      {
         participantDTO.uiQualifiedId = getDepartmentId(department.getParentDepartment())
               + department.getOrganization().getQualifiedId();
         participantDTO.parentDepartmentName = department.getParentDepartment().getName();
      }
      else
      {
         participantDTO.uiQualifiedId = department.getOrganization().getQualifiedId();
      }

      participantDTO.uiQualifiedId += "[" + department.getId() + "]";
      
      return participantDTO;
   }

   /**
    * @param qualifiedParticipantInfo
    * @param participantDTOs
    * @param parentDepartment
    * @return
    */
   private ParticipantDTO getParticipant(QualifiedModelParticipantInfo qualifiedParticipantInfo,
         Department parentDepartment, boolean defaultDepartment)
   {
      Participant participant = ParticipantUtils.getParticipant(qualifiedParticipantInfo);
      ParticipantDTO participantDTO = new ParticipantDTO(participant);
      participantDTO.type = ParticipantManagementUtils.getParticipantType(qualifiedParticipantInfo).name();
      participantDTO.uiQualifiedId = "";
      if (parentDepartment != null)
      {
         participantDTO.uiQualifiedId = getDepartmentId(parentDepartment);
         participantDTO.parentDepartmentName = parentDepartment.getName();
      }

      if (defaultDepartment)
      {
         participantDTO.uiQualifiedId += qualifiedParticipantInfo.getQualifiedId() + "[]";
         participantDTO.type = ParticipantType.DEPARTMENT_DEFAULT.name();
         participantDTO.name = participantDTO.name + " "
               + MessagesViewsCommonBean.getInstance().getString("views.participantTree.default"); 
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

   public static class ParticipantContainer
   {
      QualifiedModelParticipantInfo modelparticipant;
      String dynamicParticipantInfoId; //used only for userGroups
      Department department;
      String participantType;
   }
}