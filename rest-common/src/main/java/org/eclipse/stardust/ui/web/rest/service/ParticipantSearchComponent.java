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
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.dto.UserDetailsLevel;
import org.eclipse.stardust.engine.api.model.Organization;
import org.eclipse.stardust.engine.api.model.Participant;
import org.eclipse.stardust.engine.api.model.ParticipantInfo;
import org.eclipse.stardust.engine.api.model.QualifiedModelParticipantInfo;
import org.eclipse.stardust.engine.api.query.FilterOrTerm;
import org.eclipse.stardust.engine.api.query.UserDetailsPolicy;
import org.eclipse.stardust.engine.api.query.UserQuery;
import org.eclipse.stardust.engine.api.query.Users;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.Department;
import org.eclipse.stardust.engine.api.runtime.DepartmentInfo;
import org.eclipse.stardust.engine.api.runtime.PerformerType;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.ui.web.common.util.GsonUtils;
import org.eclipse.stardust.ui.web.rest.service.dto.QueryResultDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.request.ParticipantSearchRequestDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.response.ParticipantDTO;
import org.eclipse.stardust.ui.web.rest.service.utils.ActivityInstanceUtils;
import org.eclipse.stardust.ui.web.rest.service.utils.ServiceFactoryUtils;
import org.eclipse.stardust.ui.web.viewscommon.common.configuration.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.DefaultDelegatesProvider;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.DepartmentDelegatesProvider;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.IDelegatesProvider;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.IDepartmentProvider;
import org.eclipse.stardust.ui.web.viewscommon.utils.ParticipantUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.UserUtils;
import org.springframework.stereotype.Component;

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
      List<ParticipantDTO> response = getMatchingData(delegationDTO);

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
      return searchAllParticipants(searchText, maxMatches, 15, true, true);
   }
   
   /**
    * @param searchText
    * @param maxMatches
    * @param type 
    * @return
    */
   public String searchAllParticipants(String searchText, int maxMatches, int type, boolean filterPredefinedModel, boolean filterScopedParticipant)
   {
      List<ParticipantDTO> selectedParticipants = new ArrayList<ParticipantDTO>();
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
         selectedParticipants.addAll(copyToParticipantDTOList(matchingUsers));
      }
      
      //TODO: is there a requirement for separate role and organizations?
      if (containsOrganization(type) && containsRole(type))
      {
         List<Participant> rolesAndOrgs = null;
         if(filterScopedParticipant)
         {
            rolesAndOrgs = ParticipantUtils.getAllUnScopedModelParticipant(filterPredefinedModel);   
         }
         else
         {
            List<QualifiedModelParticipantInfo> allParticipants = ParticipantUtils.getAllModelParticipants(filterPredefinedModel);
            rolesAndOrgs = CollectionUtils.newArrayList();
            for (QualifiedModelParticipantInfo participant : allParticipants)
            {
               if (participant instanceof Participant)
               {
                  rolesAndOrgs.add((Participant) participant);
               }
            }
         }
         
         selectedParticipants.addAll(copyToParticipantDTOList(rolesAndOrgs, searchText));
         
         if (containsDepartment(type))
         {
            Set<DepartmentInfo> departments = getAllDepartments(rolesAndOrgs, service);
            selectedParticipants.addAll(copyToParticipantDTOList(departments, searchText));
         }
      }
      
      return GsonUtils.toJsonHTMLSafeString(selectedParticipants);
   }

   /**
    * @param participantSReqDTO
    * @return
    */
   private List<ParticipantDTO> getMatchingData(ParticipantSearchRequestDTO participantSReqDTO)
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
   private List<ParticipantDTO> getMatchingData(Long[] activities,
         IDelegatesProvider.Options defualtOptions, IDepartmentProvider.Options depOptions)
   {
      // prepare activities
      List<ActivityInstance> activityInstances = activityInstanceUtils.getActivityInstancesFor(activities);

      List<ParticipantDTO> result = new ArrayList<ParticipantDTO>();

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
   private List<ParticipantDTO> getMatchingDataDefault(List<ActivityInstance> activities,
         IDelegatesProvider.Options options)
   {
      if (null == delegatesProvider)
      {
         delegatesProvider = DefaultDelegatesProvider.INSTANCE;
      }

      List<ParticipantDTO> selectedParticipants = new ArrayList<ParticipantDTO>();

      // Add default participants
      Map<PerformerType, List< ? extends ParticipantInfo>> delegates = delegatesProvider.findDelegates(activities,
            options);
      selectedParticipants.addAll(copyToParticipantDTOList(delegates.get(PerformerType.User)));
      selectedParticipants
            .addAll(copyToParticipantDTOList(delegates.get(PerformerType.ModelParticipant)));
      selectedParticipants.addAll(copyToParticipantDTOList(delegates.get(PerformerType.UserGroup)));

      return selectedParticipants;
   }

   /**
    * @param activities
    * @param options
    * @return
    */
   private List<ParticipantDTO> getMatchingDepartment(List<ActivityInstance> activities,
         IDepartmentProvider.Options options)
   {

      List<ParticipantDTO> selectedParticipants = new ArrayList<ParticipantDTO>();

      // Add departments
      if (null == departmentDelegatesProvider)
      {
         departmentDelegatesProvider = DepartmentDelegatesProvider.INSTANCE;
      }

      Map<String, Set<DepartmentInfo>> deptDelegates = departmentDelegatesProvider.findDepartments(activities, options);

      Set<DepartmentInfo> selectedDepts = deptDelegates.get("Departments");

      for (DepartmentInfo departmentInfo : selectedDepts)
      {
         ParticipantDTO participantDTO = new ParticipantDTO(departmentInfo);
         selectedParticipants.add(participantDTO);
      }

      return selectedParticipants;
   }

   /**
    * @param allParticipants
    * @return
    */
   private List<ParticipantDTO> copyToParticipantDTOList(
         List< ? extends ParticipantInfo> allParticipants)
   {
      List<ParticipantDTO> selectParticipants = new ArrayList<ParticipantDTO>();
      if (allParticipants != null)
      {
         for (ParticipantInfo participantInfo : allParticipants)
         {
            if (participantInfo instanceof Participant)
            {
               selectParticipants.add(new ParticipantDTO((Participant) participantInfo));
            }
         }
      }
      return selectParticipants;
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
   private Collection< ? extends ParticipantDTO> copyToParticipantDTOList(Set<DepartmentInfo> departments,
         String searchValue)
   {
      List<ParticipantDTO> selectParticipants = new ArrayList<ParticipantDTO>();
      String regex = !StringUtils.isEmpty(searchValue) ? searchValue.replaceAll("\\*", ".*") + ".*" : null;
      if (CollectionUtils.isNotEmpty(departments))
      {
         for (DepartmentInfo deptInfo : departments)
         {
            if (deptInfo.getName().matches(regex))
            {
               selectParticipants.add(new ParticipantDTO(deptInfo));
            }
         }
      }

      return selectParticipants;
   }
   
   /**
    * @param allParticipants
    * @return
    */
   private List<ParticipantDTO> copyToParticipantDTOList(Users allParticipants)
   {
      List<ParticipantDTO> selectParticipants = new ArrayList<ParticipantDTO>();
      if (allParticipants != null)
      {
         for (Participant participant : allParticipants)
         {
            selectParticipants.add(new ParticipantDTO(participant));
         }
      }
      return selectParticipants;
   }    

   /**
    * @param allParticipants
    * @return
    */
   private List<ParticipantDTO> copyToParticipantDTOList(List<Participant> allParticipants, String searchValue)
   {
      List<ParticipantDTO> selectParticipants = new ArrayList<ParticipantDTO>();
      String regex = !StringUtils.isEmpty(searchValue) ? searchValue.replaceAll("\\*", ".*") + ".*" : null;
      if (allParticipants != null)
      {
         for (Participant participant : allParticipants)
         {
            if (regex == null || participant.getName().matches(regex))
            {
               selectParticipants.add(new ParticipantDTO(participant));
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
