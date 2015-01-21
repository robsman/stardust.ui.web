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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.model.Participant;
import org.eclipse.stardust.engine.api.model.ParticipantInfo;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.DepartmentInfo;
import org.eclipse.stardust.engine.api.runtime.PerformerType;
import org.eclipse.stardust.ui.web.rest.service.dto.request.ParticipantSearchRequestDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.response.ParticipantSearchResponseDTO;
import org.eclipse.stardust.ui.web.rest.service.utils.ActivityInstanceUtils;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.DefaultDelegatesProvider;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.DepartmentDelegatesProvider;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.IDelegatesProvider;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.IDepartmentProvider;
import org.springframework.stereotype.Component;

/**
 * @author Yogesh.Manware
 *
 */
@Component
public class DelegationComponent
{
   @Resource
   private ActivityInstanceUtils activityInstanceUtils;

   private IDelegatesProvider delegatesProvider;
   private IDepartmentProvider departmentProvider;

   public enum ParticipantType {
      All(0), User(1), Role(2), Organization(3), Department(4);

      private Integer val;

      private ParticipantType(Integer val)
      {
         this.val = val;
      }

      public Integer getValue()
      {
         return val;
      }
   }

   /**
    * @param participantSReqDTO
    * @return
    */
   public List<ParticipantSearchResponseDTO> getMatchingData(ParticipantSearchRequestDTO participantSReqDTO)
   {
      IDepartmentProvider.Options departmentOptions = null;

      // check department search is required
      if (ParticipantType.Department.name().equalsIgnoreCase(participantSReqDTO.getParticipantType())
            || ParticipantType.All.name().equalsIgnoreCase(participantSReqDTO.getParticipantType()))
      {
         departmentOptions = getDepartmentOptions(participantSReqDTO);
      }

      IDelegatesProvider.Options defaultDelOptions = null;

      if (!ParticipantType.Department.name().equalsIgnoreCase(participantSReqDTO.getParticipantType()))
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
      List<ActivityInstance> activityInstances = getActivityInstancesFor(activities);

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
      if (null == departmentProvider)
      {
         departmentProvider = DepartmentDelegatesProvider.INSTANCE;
      }

      Map<String, Set<DepartmentInfo>> deptDelegates = departmentProvider.findDepartments(activities, options);

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
    * @param activities
    * @return
    */
   private List<ActivityInstance> getActivityInstancesFor(Long[] activities)
   {
      List<ActivityInstance> activityInstances = new ArrayList<ActivityInstance>();

      if (activities == null)
      {
         return activityInstances;
      }
      for (Long activityInstanceOid : activities)
      {
         ActivityInstance ai = activityInstanceUtils.getActivityInstance(activityInstanceOid.longValue());
         if (ai != null)
         {
            activityInstances.add(ai);
         }
      }
      return activityInstances;
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
            if (ParticipantType.User.name().equals(ParticipantSearchRequestDTO.getParticipantType())
                  || ParticipantType.Role.name().equals(ParticipantSearchRequestDTO.getParticipantType())
                  || ParticipantType.Organization.name().equals(ParticipantSearchRequestDTO.getParticipantType()))
            {
               if (ParticipantSearchRequestDTO.isDisableAdministrator())
               {
                  Set<Integer> result = CollectionUtils.newSet();
                  result.add(ParticipantType.valueOf(ParticipantSearchRequestDTO.getParticipantType()).getValue());
                  result.add(IDelegatesProvider.DISABLE_ADMINISTRATOR_ROLE);

                  return Collections.unmodifiableSet(result);
               }
               return Collections.singleton(new Integer(ParticipantType.valueOf(
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

               if (ParticipantSearchRequestDTO.isDisableAdministrator())
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
}
