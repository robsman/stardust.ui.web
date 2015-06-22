/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Yogesh.Manware (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.rest.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.dto.UserDetailsLevel;
import org.eclipse.stardust.engine.api.model.DynamicParticipantInfo;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.ModelParticipant;
import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.engine.api.model.Organization;
import org.eclipse.stardust.engine.api.model.OrganizationInfo;
import org.eclipse.stardust.engine.api.model.Participant;
import org.eclipse.stardust.engine.api.model.ParticipantInfo;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.model.QualifiedModelParticipantInfo;
import org.eclipse.stardust.engine.api.model.QualifiedOrganizationInfo;
import org.eclipse.stardust.engine.api.model.Role;
import org.eclipse.stardust.engine.api.model.RoleInfo;
import org.eclipse.stardust.engine.api.query.FilterOrTerm;
import org.eclipse.stardust.engine.api.query.ParticipantAssociationFilter;
import org.eclipse.stardust.engine.api.query.QueryResult;
import org.eclipse.stardust.engine.api.query.UserDetailsPolicy;
import org.eclipse.stardust.engine.api.query.UserQuery;
import org.eclipse.stardust.engine.api.query.Users;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.Department;
import org.eclipse.stardust.engine.api.runtime.DepartmentInfo;
import org.eclipse.stardust.engine.api.runtime.DeployedModel;
import org.eclipse.stardust.engine.api.runtime.PerformerType;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.api.runtime.UserGroup;
import org.eclipse.stardust.ui.web.common.util.GsonUtils;
import org.eclipse.stardust.ui.web.rest.service.dto.DepartmentDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.ModelDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.ModelParticipantDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.ParticipantNodeDetailsDTO.NODE_TYPE;
import org.eclipse.stardust.ui.web.rest.service.dto.QueryResultDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.UserDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.builder.DTOBuilder;
import org.eclipse.stardust.ui.web.rest.service.dto.request.ParticipantSearchRequestDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.response.ParticipantSearchResponseDTO;
import org.eclipse.stardust.ui.web.rest.service.utils.ActivityInstanceUtils;
import org.eclipse.stardust.ui.web.rest.service.utils.ServiceFactoryUtils;
import org.eclipse.stardust.ui.web.viewscommon.common.configuration.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.DefaultDelegatesProvider;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.DepartmentDelegatesProvider;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.IDelegatesProvider;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.IDepartmentProvider;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.RepositoryUtility.NodeType;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ParticipantUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ParticipantUtils.ParticipantType;
import org.eclipse.stardust.ui.web.viewscommon.utils.UserUtils;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;

/**
 * @author Yogesh.Manware
 *
 *         Assist in searching Participants (Specially for Delegation and Worklist
 *         Configuration)
 */
@Component
public class ParticipantSearchComponent
{
   @Resource
   private ActivityInstanceUtils activityInstanceUtils;

   @Resource
   private IDelegatesProvider delegatesProvider;

   @Resource
   private IDepartmentProvider departmentDelegatesProvider;
   
   @Resource
   private ServiceFactoryUtils serviceFactoryUtils;

   private Boolean disabledAdministrator = null;

   private Map<Long, Department> departmentCache = CollectionUtils.newMap();
   
   public enum PerformerTypeUI {
      All(0), Role(1), Organization(2), User(4), Department(8);

      private Integer val;

      private PerformerTypeUI(Integer val)
      {
         this.val = val;
      }

      public Integer getValue()
      {
         return val;
      }
   }

   /**
    * @param request
    * @return
    */
   public String searchParticipants(String request, Integer skip, Integer pageSize)
   {
      // convert request json to request object
      ParticipantSearchRequestDTO delegationDTO = GsonUtils.fromJson(request, ParticipantSearchRequestDTO.class);

      // determine DisabledAdministrator flag
      if (getDisabledAdministrator() == null)
      { // not injected in through configuration
        // check if it exist in request
         if (delegationDTO.getDisableAdministrator() == null)
         {
            delegationDTO.setDisableAdministrator(false); // default is false.
         }
      }
      else
      {
         delegationDTO.setDisableAdministrator(getDisabledAdministrator());
      }

      // search participant
      List<ParticipantSearchResponseDTO> response = getMatchingData(delegationDTO);

      QueryResultDTO result = new QueryResultDTO();
      result.totalCount = response.size();
      result.list = response;
      
      if (skip != null && pageSize != null && skip < result.totalCount && result.totalCount > pageSize) {
         long to = pageSize;
         if (result.totalCount - skip < pageSize) {
            to = result.totalCount;
         }
         result.list = response.subList(skip, (int) to);
      }
      
      // convert response to json string
      return GsonUtils.toJsonHTMLSafeString(result);
   }
   
   
   /**
    * @param searchText
    * @param maxMatches
    * @return
    */
   public String searchAllParticipants(String searchText, int maxMatches)
   {
      return searchAllParticipants(searchText, maxMatches, 15);
   }
   
   /**
    * @param searchText
    * @param maxMatches
    * @param type 
    * @return
    */
   public String searchAllParticipants(String searchText, int maxMatches, int type)
   {
      List<ParticipantSearchResponseDTO> selectedParticipants = new ArrayList<ParticipantSearchResponseDTO>();
      QueryService service = serviceFactoryUtils.getQueryService();

      if (containsUser(type))
      {
         UserQuery userQuery = UserQuery.findActive();
         UserDetailsPolicy userPolicy = new UserDetailsPolicy(UserDetailsLevel.Core);
         userPolicy.setPreferenceModules(UserPreferencesEntries.M_ADMIN_PORTAL, UserPreferencesEntries.M_VIEWS_COMMON);
         userQuery.setPolicy(userPolicy);

         if (!StringUtils.isEmpty(searchText))
         {
            String name = searchText.replaceAll("\\*", "%") + "%";
            String nameFirstLetterCaseChanged = UserUtils.alternateFirstLetter(name);
            FilterOrTerm or = userQuery.getFilter().addOrTerm();
            or.add(UserQuery.LAST_NAME.like(name));
            or.add(UserQuery.LAST_NAME.like(nameFirstLetterCaseChanged));
            or.add(UserQuery.FIRST_NAME.like(name));
            or.add(UserQuery.FIRST_NAME.like(nameFirstLetterCaseChanged));
            or.add(UserQuery.ACCOUNT.like(name));
            or.add(UserQuery.ACCOUNT.like(nameFirstLetterCaseChanged));
         }
         userQuery.orderBy(UserQuery.LAST_NAME).and(UserQuery.FIRST_NAME).and(UserQuery.ACCOUNT);
         Users matchingUsers = service.getAllUsers(userQuery);
         selectedParticipants.addAll(copyToParticipantSearchResponseDTOList(matchingUsers));
      }
      
      //TODO: is there a requirement for separate role and organizations?
      if (containsOrganization(type) && containsRole(type))
      {
         List<Participant> rolesAndOrgs = ParticipantUtils.getAllUnScopedModelParticipant(true);
         selectedParticipants.addAll(copyToParticipantSearchResponseDTOList(rolesAndOrgs, searchText));
         
         if (containsDepartment(type))
         {
            Set<DepartmentInfo> departments = getAllDepartments(rolesAndOrgs, service);
            selectedParticipants.addAll(copyToParticipantSearchResponseDTOList(departments, searchText));
         }
      }
      
      return GsonUtils.toJsonHTMLSafeString(selectedParticipants);
   }

   /**
    * @param participantSReqDTO
    * @return
    */
   public List<ParticipantSearchResponseDTO> getMatchingData(ParticipantSearchRequestDTO participantSReqDTO)
   {
      IDepartmentProvider.Options departmentOptions = null;

      // check department search is required
      if (PerformerTypeUI.Department.name().equalsIgnoreCase(participantSReqDTO.getParticipantType())
            || PerformerTypeUI.All.name().equalsIgnoreCase(participantSReqDTO.getParticipantType()))
      {
         departmentOptions = getDepartmentOptions(participantSReqDTO);
      }

      IDelegatesProvider.Options defaultDelOptions = null;

      if (!PerformerTypeUI.Department.name().equalsIgnoreCase(participantSReqDTO.getParticipantType()))
      {
         defaultDelOptions = getDelegateProviderOptions(participantSReqDTO);
      }

      return getMatchingData(participantSReqDTO.getActivities(), defaultDelOptions, departmentOptions);
   }

   /**
    * @param activities
    * @param defualtOptions
    * @param depOptions
    * @return
    */
   public List<ParticipantSearchResponseDTO> getMatchingData(Long[] activities,
         IDelegatesProvider.Options defualtOptions, IDepartmentProvider.Options depOptions)
   {
      // prepare activities
      List<ActivityInstance> activityInstances = activityInstanceUtils.getActivityInstancesFor(activities);

      List<ParticipantSearchResponseDTO> result = new ArrayList<ParticipantSearchResponseDTO>();

      if (defualtOptions != null)
      {
         result.addAll(getMatchingDataDefault(activityInstances, defualtOptions));
      }

      if (depOptions != null)
      {
         result.addAll(getMatchingDepartment(activityInstances, depOptions));
      }

      return result;
   }

   /**
    * @param activities
    * @param options
    * @return
    */
   private List<ParticipantSearchResponseDTO> getMatchingDataDefault(List<ActivityInstance> activities,
         IDelegatesProvider.Options options)
   {
      if (null == delegatesProvider)
      {
         delegatesProvider = DefaultDelegatesProvider.INSTANCE;
      }

      List<ParticipantSearchResponseDTO> selectedParticipants = new ArrayList<ParticipantSearchResponseDTO>();

      // Add default participants
      Map<PerformerType, List< ? extends ParticipantInfo>> delegates = delegatesProvider.findDelegates(activities,
            options);
      selectedParticipants.addAll(copyToParticipantSearchResponseDTOList(delegates.get(PerformerType.User)));
      selectedParticipants
            .addAll(copyToParticipantSearchResponseDTOList(delegates.get(PerformerType.ModelParticipant)));
      selectedParticipants.addAll(copyToParticipantSearchResponseDTOList(delegates.get(PerformerType.UserGroup)));

      return selectedParticipants;
   }

   /**
    * @param activities
    * @param options
    * @return
    */
   private List<ParticipantSearchResponseDTO> getMatchingDepartment(List<ActivityInstance> activities,
         IDepartmentProvider.Options options)
   {

      List<ParticipantSearchResponseDTO> selectedParticipants = new ArrayList<ParticipantSearchResponseDTO>();

      // Add departments
      if (null == departmentDelegatesProvider)
      {
         departmentDelegatesProvider = DepartmentDelegatesProvider.INSTANCE;
      }

      Map<String, Set<DepartmentInfo>> deptDelegates = departmentDelegatesProvider.findDepartments(activities, options);

      Set<DepartmentInfo> selectedDepts = deptDelegates.get("Departments");

      for (DepartmentInfo departmentInfo : selectedDepts)
      {
         ParticipantSearchResponseDTO participantDTO = new ParticipantSearchResponseDTO(departmentInfo);
         selectedParticipants.add(participantDTO);
      }

      return selectedParticipants;
   }

   /**
    * @param allParticipants
    * @return
    */
   private List<ParticipantSearchResponseDTO> copyToParticipantSearchResponseDTOList(
         List< ? extends ParticipantInfo> allParticipants)
   {
      List<ParticipantSearchResponseDTO> selectParticipants = new ArrayList<ParticipantSearchResponseDTO>();
      if (allParticipants != null)
      {
         for (ParticipantInfo participantInfo : allParticipants)
         {
            if (participantInfo instanceof Participant)
            {
               selectParticipants.add(new ParticipantSearchResponseDTO((Participant) participantInfo));
            }
         }
      }
      return selectParticipants;
   }
   
   /**
    * 
    * @param departmentOID
    * @param modelParticipantJSON
    * @return
    */
   public ModelParticipantDTO removeDepartmentFromParticipant(long departmentOID, JsonObject modelParticipantJSON)
   {
      ModelParticipantDTO modelParticipantDto = null;
      Department dept = serviceFactoryUtils.getAdministrationService().getDepartment(departmentOID);
      
      String modelParticipantType = modelParticipantJSON.get("nodeType").getAsString(); 
      String modelParticipantQualifierId = modelParticipantJSON.get("qualifiedId").getAsString();
      // Remove the department using OID
      serviceFactoryUtils.getAdministrationService().removeDepartment(departmentOID);
      // TODO - convert nodeType to ParticipantTYpe and fetch the participant
      Participant p = ParticipantUtils.getParticipant(modelParticipantQualifierId, ParticipantType.SCOPED_ORGANIZATION);
      if(p instanceof ModelParticipant)
      {
         modelParticipantDto = DTOBuilder.build(p, ModelParticipantDTO.class);
         modelParticipantDto.nodeType = NODE_TYPE.valueOf(modelParticipantType);
         fetchChildNodes((QualifiedModelParticipantInfo)p, modelParticipantDto);
      }
        
      return modelParticipantDto;
   }
   
   /**
    * 
    * @return
    */
   public List<ModelDTO> getParticipantTree()
   {
      List<Organization> topLevelOrganizations = null;
      List<Role> topLevelRoles = null;
      List<DeployedModel> models = ModelUtils.getActiveModels();
      List<ModelDTO> modelList = CollectionUtils.newArrayList();
      for (Model model : models)
      {
         boolean adminRoleAdded = false;
         List<ModelParticipantDTO> modelParticipants = CollectionUtils.newArrayList();
         
         topLevelOrganizations = model.getAllTopLevelOrganizations();
         topLevelRoles = model.getAllTopLevelRoles();
         
         ModelDTO modelDto = DTOBuilder.build(model, ModelDTO.class);
         for(Role role : topLevelRoles)
         {
            // We need to only add the first occurrence of the "Administrator" role 
            if (PredefinedConstants.ADMINISTRATOR_ROLE.equals(role.getId()))
            {
               if (!adminRoleAdded)
               {
                  adminRoleAdded = true;
               }
               else
               {
                  // If "Administrator" role has already been added, skip this element
                  continue;
               }
            }

            if (PredefinedConstants.ADMINISTRATOR_ROLE.equals(role.getId()))
            {
               String modelId = !PredefinedConstants.ADMINISTRATOR_ROLE.equals(role.getId()) ? ModelUtils
                     .extractModelId(role.getQualifiedId()) : null;
               if((modelId == null) || (modelId.equals(model.getId())))
               {
                  ModelParticipantDTO modelParticipantDto = DTOBuilder.build(role, ModelParticipantDTO.class);
                     modelParticipants.add(modelParticipantDto);
                     modelParticipantDto.nodeType = setNodeType(role);
                     
                    fetchChildNodes(role, modelParticipantDto);
               }
            }
         }
         
         // Add all Organizations
         modelParticipants.addAll(createOrganizationTree(topLevelOrganizations, model.getId()));
         
         modelDto.modelParticipants = modelParticipants;
         modelList.add(modelDto);
      }
      return modelList;
   }

   /**
    * Create Organization tree with sub-org and roles
    * 
    * @param orgList
    * @param modelId
    * @return
    */
   private List<ModelParticipantDTO> createOrganizationTree(List<Organization> orgList, String modelId)
   {
      List<ModelParticipantDTO> modelParticipants = CollectionUtils.newArrayList();
      for (Organization organization : orgList)
      {
         String orgModelId = ModelUtils.extractModelId(organization.getQualifiedId());
         if (orgModelId.equals(modelId))
         {
            // create organization object
            ModelParticipantDTO modelParticipantDto = DTOBuilder.build(organization, ModelParticipantDTO.class);
            modelParticipantDto.nodeType = setNodeType(organization);
            // fetch all sub-org, roles and users
            fetchChildNodes(organization, modelParticipantDto);
            modelParticipants.add(modelParticipantDto);
         }
      }
      
      return modelParticipants;
   }
   
   /**
    * 
    * @param participantInfo
    * @param modelParticipantDto
    */
   private void expandOrganizationNode(ModelParticipantInfo participantInfo, ModelParticipantDTO modelParticipantDto)
   {
      // Add all associated Users
      addUsersForParticipant(participantInfo, modelParticipantDto);
      // Add all sub-Org
      addSubOrganizations((QualifiedOrganizationInfo) participantInfo, modelParticipantDto);
      
      //Add all sub-roles
      addSubRoles(participantInfo, modelParticipantDto);
      
   }
   
   /**
    * 
    * @param qualifiedOrganizationInfo
    * @param modelParticipantDto
    */
   private void addSubOrganizations(QualifiedOrganizationInfo qualifiedOrganizationInfo, ModelParticipantDTO modelParticipantDto)
   {
      Department department = getDepartment(qualifiedOrganizationInfo.getDepartment());
      Organization organization = (Organization) ParticipantUtils.getParticipant(qualifiedOrganizationInfo, UserDetailsLevel.Full);
      List<ModelParticipantDTO> subOrganizationList = CollectionUtils.newArrayList();
      @SuppressWarnings("unchecked")
      List<Organization> subOrganizations = organization.getAllSubOrganizations();
      String orgModelId = ModelUtils.extractModelId(qualifiedOrganizationInfo.getQualifiedId());
      for (Organization subOrganization : subOrganizations)
      {
         ModelParticipantInfo modelParticipant = ParticipantUtils.getScopedParticipant(subOrganization, department);
         ModelParticipantDTO subOrgDto = DTOBuilder.build(modelParticipant, ModelParticipantDTO.class);
         subOrgDto.nodeType = setNodeType(subOrganization);
         
         fetchChildNodes(modelParticipant, subOrgDto);
         // TODO - Use current Model ID
         subOrgDto.subOrganizations = createOrganizationTree(subOrganization.getAllSubOrganizations(), orgModelId);
         subOrganizationList.add(subOrgDto);
      }
      modelParticipantDto.subOrganizations = subOrganizationList;
   }
   
   /**
    * 
    * @param qualifiedOrganizationInfo
    * @param modelParticipantDto
    * @return
    */
   private ModelParticipantDTO expandExplicitlyScopedOrganizationNode(QualifiedOrganizationInfo qualifiedOrganizationInfo, ModelParticipantDTO modelParticipantDto)
   {
      QueryService qs = serviceFactoryUtils.getQueryService();
      List<DepartmentDTO> departments = CollectionUtils.newArrayList();
      List<Department> deptList = qs.findAllDepartments(qualifiedOrganizationInfo.getDepartment(),
            qualifiedOrganizationInfo);

      // Add Default Department
      ModelParticipantDTO defaultDepartmentDto = DTOBuilder.build(qualifiedOrganizationInfo, ModelParticipantDTO.class);
      defaultDepartmentDto.nodeType = setNodeType(qualifiedOrganizationInfo);
      
      expandDefaultDepartmentNode(qualifiedOrganizationInfo, modelParticipantDto);
      modelParticipantDto.defaultDepartment = defaultDepartmentDto;
      
      // Add all Departments
      for (Department department : deptList)
      {
         departmentCache.put(department.getOID(), department);
         DepartmentDTO departmentDto = DTOBuilder.build(department, DepartmentDTO.class);
         
         expandDepartmentNode(department, departmentDto);
         departments.add(departmentDto);
         
      }
      modelParticipantDto.scopedDepartments = departments;
      return modelParticipantDto;
   }
   
  /**
   * Add participants, orgs, and Roles for Department
   * 
   * @param department
   * @param departmentDto
   */
   private void expandDepartmentNode(Department department, DepartmentDTO departmentDto)
   {
      QualifiedModelParticipantInfo scopedOrganizationInfo = department.getScopedParticipant(department
            .getOrganization());
      ModelParticipantDTO scopedParticipantDto = DTOBuilder.build(scopedOrganizationInfo, ModelParticipantDTO.class);
      scopedParticipantDto.nodeType = setNodeType(scopedOrganizationInfo);
      
      departmentDto.scopedParticipant = scopedParticipantDto;
      
      // Add all associated Users
      addUsersForParticipant(scopedOrganizationInfo, scopedParticipantDto);

      // Add all sub-Organizations
      addSubOrganizations((QualifiedOrganizationInfo) scopedOrganizationInfo, scopedParticipantDto);

      // Add all sub-Roles
      addSubRoles((QualifiedOrganizationInfo) scopedOrganizationInfo, scopedParticipantDto);
   }

  /**
   * 
   * @param qualifiedOrganizationInfo
   * @param modelParticipantDto
   */
   private void expandDefaultDepartmentNode(QualifiedOrganizationInfo qualifiedOrganizationInfo, ModelParticipantDTO modelParticipantDto)
   {
      Department department = getDepartment(qualifiedOrganizationInfo.getDepartment());

      // Add all associated Users
      addUsersForParticipant(qualifiedOrganizationInfo, modelParticipantDto);
      
      List<ModelParticipantDTO> subOrgs = CollectionUtils.newArrayList();
      // Add all sub-Organizations
      @SuppressWarnings("unchecked")
      List<Organization> subOrganizations = ((Organization) ParticipantUtils.getParticipant(qualifiedOrganizationInfo, UserDetailsLevel.Full))
            .getAllSubOrganizations();
      for (Organization subOrganization : subOrganizations)
      {
         QualifiedOrganizationInfo qualifiedSubOrgInfo = (QualifiedOrganizationInfo) ParticipantUtils
               .getScopedParticipant(subOrganization, department);
         ModelParticipantDTO defaultDepartmentDto = DTOBuilder.build(qualifiedSubOrgInfo, ModelParticipantDTO.class);
         defaultDepartmentDto.nodeType = setNodeType(qualifiedOrganizationInfo);
         subOrgs.add(defaultDepartmentDto);
      }
      modelParticipantDto.subOrganizations = subOrgs;
      // Add all sub-Roles
      addSubRoles(qualifiedOrganizationInfo, modelParticipantDto);
   }
   
   /**
    * 
    * @param modelParticipantInfo
    * @param modelParticipantDto
    */
   private void fetchChildNodes(ModelParticipantInfo modelParticipantInfo,ModelParticipantDTO modelParticipantDto)
   {
      switch (modelParticipantDto.nodeType)
      {
      case ORGANIZATION_UNSCOPED:
      case ORGANIZATON_SCOPED_IMPLICIT:
         expandOrganizationNode((QualifiedOrganizationInfo) modelParticipantInfo, modelParticipantDto);
        break;

      case ORGANIZATON_SCOPED_EXPLICIT:
         expandExplicitlyScopedOrganizationNode((QualifiedOrganizationInfo) modelParticipantInfo, modelParticipantDto);
         break;

      case ROLE_SCOPED:
      case ROLE_UNSCOPED:
         addUsersForParticipant(modelParticipantInfo, modelParticipantDto);
         break;
      // TODO - provide handling for UserGroup
      case USERGROUP:
         /*addUsersForParticipant(node, dynamicParticipantInfo);*/
         break;

      case DEPARTMENT_DEFAULT:
         expandDefaultDepartmentNode((QualifiedOrganizationInfo) modelParticipantInfo, modelParticipantDto);
         break;

      default:
         break;
      }
      
   }
   
   /**
    * Fetch all users for Participant
    * 
    * @param participantInfo
    * @param modelParticipantDto
    */
   private void addUsersForParticipant(ModelParticipantInfo participantInfo, ModelParticipantDTO modelParticipantDto)
   {
         UserQuery userQuery = UserQuery.findAll();
         
         userQuery.getFilter().add(ParticipantAssociationFilter.forParticipant( participantInfo, false));
         UserDetailsPolicy userPolicy = new UserDetailsPolicy(UserDetailsLevel.Full);
         userPolicy.setPreferenceModules(UserPreferencesEntries.M_ADMIN_PORTAL);
         userQuery.setPolicy(userPolicy);
         
         QueryResult<User> users = serviceFactoryUtils.getQueryService().getAllUsers((UserQuery) userQuery);
         modelParticipantDto.users =  (List<UserDTO>) buildAllUsersResult(users).list;
    }
   
   /**
    * 
    * @param users
    * @return
    */
   private QueryResultDTO buildAllUsersResult(QueryResult<User> users)
   {
      List<UserDTO> userDTOList = new ArrayList<UserDTO>();

      for (User user : users)
      {
         UserDTO userDTO = DTOBuilder.build(user, UserDTO.class);
         if (user.getValidFrom() != null)
         {
            userDTO.validFrom = user.getValidFrom().getTime();
         }
         else
         {
            userDTO.validFrom = null;
         }

         if (user.getValidTo() != null)
         {
            userDTO.validTo = user.getValidTo().getTime();
         }
         else
         {
            userDTO.validTo = null;
         }
         userDTO.displayName = UserUtils.getUserDisplayLabel(user);
         userDTOList.add(userDTO);
      }

      QueryResultDTO resultDTO = new QueryResultDTO();
      resultDTO.list = userDTOList;
      resultDTO.totalCount = users.getTotalCount();
      return resultDTO;
   }
   
  /**
   * 
   * @param qualifiedOrganizationInfo
   * @param modelParticipantDto
   * @return
   */
   private ModelParticipantDTO addSubRoles(ModelParticipantInfo qualifiedOrganizationInfo, ModelParticipantDTO modelParticipantDto)
   {
      List<ModelParticipantDTO> rolesList = CollectionUtils.newArrayList();
      Department department = getDepartment(qualifiedOrganizationInfo.getDepartment());
      Organization organization = (Organization) ParticipantUtils.getParticipant(qualifiedOrganizationInfo, UserDetailsLevel.Full);

      @SuppressWarnings("unchecked")
      List<Role> subRoles = organization.getAllSubRoles();
      for (Role subRole : subRoles)
      {
         ModelParticipantDTO roleDto = DTOBuilder.build(subRole, ModelParticipantDTO.class);
         roleDto.nodeType = setNodeType(subRole);
         // load the child nodes for Role
         fetchChildNodes(subRole, roleDto);
         rolesList.add(roleDto);
      }
      modelParticipantDto.subRoles = rolesList;
      return modelParticipantDto;
   }
   
  /**
   * 
   * @param departmentInfo
   * @return
   */
   private Department getDepartment(DepartmentInfo departmentInfo)
   {
      Department department = null;

      if (departmentInfo != null)
      {
         department = departmentCache.get(departmentInfo.getOID());
         if (null == department)
         {
            department = serviceFactoryUtils.getAdministrationService().getDepartment(departmentInfo.getOID());
         }
      }

      return department;
   }
   
   /**
    * 
    * @param modelParticipantInfo
    * @return
    */
   private NODE_TYPE setNodeType(ModelParticipantInfo modelParticipantInfo)
   {
      NODE_TYPE nodeType = null;
      try
      {
         // fetch dynamicParticipantInfo required for UserGroup
         DynamicParticipantInfo dynamicParticipantInfo = null;
         Department department = getDepartment(modelParticipantInfo.getDepartment());
         
         if ((null == modelParticipantInfo) && (null == department) && (null == dynamicParticipantInfo))
         {
            nodeType = NODE_TYPE.MODEL;
         }
         else if ((modelParticipantInfo instanceof OrganizationInfo) && modelParticipantInfo.definesDepartmentScope())
         {
            nodeType = NODE_TYPE.ORGANIZATON_SCOPED_EXPLICIT;
         }
         else if ((modelParticipantInfo instanceof OrganizationInfo) && modelParticipantInfo.isDepartmentScoped()
               && !modelParticipantInfo.definesDepartmentScope())
         {
            nodeType = NODE_TYPE.ORGANIZATON_SCOPED_IMPLICIT;
         }
         else if ((modelParticipantInfo instanceof RoleInfo) && modelParticipantInfo.isDepartmentScoped())
         {
            nodeType = NODE_TYPE.ROLE_SCOPED;
         }
         else if (modelParticipantInfo instanceof OrganizationInfo)
         {
            nodeType = NODE_TYPE.ORGANIZATION_UNSCOPED;
         }
         else if (modelParticipantInfo instanceof RoleInfo)
         {
            nodeType = NODE_TYPE.ROLE_UNSCOPED;
         }
         else if (dynamicParticipantInfo instanceof UserGroup)
         {
            nodeType = NODE_TYPE.USERGROUP;
         }
         else if (dynamicParticipantInfo instanceof User)
         {
            nodeType = NODE_TYPE.USER;
         }
         else if (department != null)
         {
            nodeType = NODE_TYPE.DEPARTMENT;
         }
         
      }catch(Exception e)
      {
         e.printStackTrace();
      }
      return nodeType;
   }

   /**
    * @param ParticipantSearchRequestDTO
    * @return
    */
   private IDelegatesProvider.Options getDelegateProviderOptions(
         final ParticipantSearchRequestDTO ParticipantSearchRequestDTO)
   {
      return new IDelegatesProvider.Options()
      {
         /*
          * (non-Javadoc)
          * 
          * @see
          * org.eclipse.stardust.ui.web.viewscommon.dialogs.IDelegatesProvider.Options
          * #getPerformerTypes()
          */
         public Set<Integer> getPerformerTypes()
         {
            if (PerformerTypeUI.User.name().equals(ParticipantSearchRequestDTO.getParticipantType())
                  || PerformerTypeUI.Role.name().equals(ParticipantSearchRequestDTO.getParticipantType())
                  || PerformerTypeUI.Organization.name().equals(ParticipantSearchRequestDTO.getParticipantType()))
            {
               if (ParticipantSearchRequestDTO.getDisableAdministrator().booleanValue())
               {
                  Set<Integer> result = CollectionUtils.newSet();
                  result.add(PerformerTypeUI.valueOf(ParticipantSearchRequestDTO.getParticipantType()).getValue());
                  result.add(IDelegatesProvider.DISABLE_ADMINISTRATOR_ROLE);

                  return Collections.unmodifiableSet(result);
               }
               return Collections.singleton(new Integer(PerformerTypeUI.valueOf(
                     ParticipantSearchRequestDTO.getParticipantType()).getValue()));
            }
            else
            {
               // ALL_TYPES
               Set<Integer> result = CollectionUtils.newSet();
               if (!ParticipantSearchRequestDTO.isExcludeUserType())
               {
                  result.add(IDelegatesProvider.USER_TYPE);
               }
               result.add(IDelegatesProvider.ROLE_TYPE);
               result.add(IDelegatesProvider.ORGANIZATION_TYPE);

               if (ParticipantSearchRequestDTO.getDisableAdministrator().booleanValue())
               {
                  result.add(IDelegatesProvider.DISABLE_ADMINISTRATOR_ROLE);
               }

               return Collections.unmodifiableSet(result);
            }
         }

         /*
          * (non-Javadoc)
          * 
          * @see
          * org.eclipse.stardust.ui.web.viewscommon.dialogs.IDelegatesProvider.Options
          * #isStrictSearch()
          */
         public boolean isStrictSearch()
         {
            return ParticipantSearchRequestDTO.isLimitedSearch();
         }

         /*
          * (non-Javadoc)
          * 
          * @see
          * org.eclipse.stardust.ui.web.viewscommon.dialogs.IDelegatesProvider.Options
          * #getNameFilter()
          */
         public String getNameFilter()
         {
            return ParticipantSearchRequestDTO.getSearchText();
         }
      };
   }

   /**
    * @param participantSReqDTO
    * @return
    */
   private IDepartmentProvider.Options getDepartmentOptions(final ParticipantSearchRequestDTO participantSReqDTO)
   {
      return new IDepartmentProvider.Options()
      {
         public String getNameFilter()
         {
            return participantSReqDTO.getSearchText();
         }

         public boolean isStrictSearch()
         {
            return participantSReqDTO.isLimitedSearch();
         }
      };
   }
   
   
   /**
    * @param rolesAndOrgs
    * @return
    */
   private Set<DepartmentInfo> getAllDepartments(List<Participant> rolesAndOrgs , QueryService service) {
      Set<DepartmentInfo> departmentInfos = CollectionUtils.newHashSet();
      for (Participant p : rolesAndOrgs)
      {
         if (p instanceof Organization)
         {
            departmentInfos.addAll(service.findAllDepartments(null, (Organization) p));
         }
      }
      
      return departmentInfos;
   }

   /**
    * @param departments
    * @param searchValue
    * @return
    */
   private Collection< ? extends ParticipantSearchResponseDTO> copyToParticipantSearchResponseDTOList(Set<DepartmentInfo> departments,
         String searchValue)
   {
      List<ParticipantSearchResponseDTO> selectParticipants = new ArrayList<ParticipantSearchResponseDTO>();
      String regex = !StringUtils.isEmpty(searchValue) ? searchValue.replaceAll("\\*", ".*") + ".*" : null;
      if (CollectionUtils.isNotEmpty(departments))
      {
         for (DepartmentInfo deptInfo : departments)
         {
            if (deptInfo.getName().matches(regex))
            {
               selectParticipants.add(new ParticipantSearchResponseDTO((Department) deptInfo));
            }
         }
      }

      return selectParticipants;
   }
   
   /**
    * @param allParticipants
    * @return
    */
   private List<ParticipantSearchResponseDTO> copyToParticipantSearchResponseDTOList(Users allParticipants)
   {
      List<ParticipantSearchResponseDTO> selectParticipants = new ArrayList<ParticipantSearchResponseDTO>();
      if (allParticipants != null)
      {
         for (Participant participant : allParticipants)
         {
            selectParticipants.add(new ParticipantSearchResponseDTO(participant));
         }
      }
      return selectParticipants;
   }    

   /**
    * @param allParticipants
    * @return
    */
   private List<ParticipantSearchResponseDTO> copyToParticipantSearchResponseDTOList(List<Participant> allParticipants, String searchValue)
   {
      List<ParticipantSearchResponseDTO> selectParticipants = new ArrayList<ParticipantSearchResponseDTO>();
      String regex = !StringUtils.isEmpty(searchValue) ? searchValue.replaceAll("\\*", ".*") + ".*" : null;
      if (allParticipants != null)
      {
         for (Participant participant : allParticipants)
         {
            if (regex == null || participant.getName().matches(regex))
            {
               selectParticipants.add(new ParticipantSearchResponseDTO(participant));
            }
         }
      }
      
      return selectParticipants;
   }

   public void setDisabledAdministrator(boolean disableAdministrator)
   {
      this.disabledAdministrator = disableAdministrator;
   }

   public Boolean getDisabledAdministrator()
   {
      return disabledAdministrator;
   }

   public IDelegatesProvider getDelegatesProvider()
   {
      return delegatesProvider;
   }

   public void setDelegatesProvider(IDelegatesProvider delegatesProvider)
   {
      this.delegatesProvider = delegatesProvider;
   }

   public IDepartmentProvider getDepartmentDelegatesProvider()
   {
      return departmentDelegatesProvider;
   }

   public void setDepartmentDelegatesProvider(IDepartmentProvider departmentDelegatesProvider)
   {
      this.departmentDelegatesProvider = departmentDelegatesProvider;
   }

   public void setDisabledAdministrator(Boolean disabledAdministrator)
   {
      this.disabledAdministrator = disabledAdministrator;
   }
   
   private boolean containsRole(int input)
   {
      return ((input & PerformerTypeUI.Role.getValue()) == PerformerTypeUI.Role.getValue());
   }
   
   private boolean containsOrganization(int input)
   {
      return ((input & PerformerTypeUI.Organization.getValue()) == PerformerTypeUI.Organization.getValue());
   }
   
   private boolean containsUser(int input)
   {
      return ((input & PerformerTypeUI.User.getValue()) == PerformerTypeUI.User.getValue());
   }
   
   private boolean containsDepartment(int input)
   {
      return ((input & PerformerTypeUI.Department.getValue()) == PerformerTypeUI.Department.getValue());
   }
}
