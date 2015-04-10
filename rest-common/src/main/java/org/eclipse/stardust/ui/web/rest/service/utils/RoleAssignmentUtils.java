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
 * @author Abhay.Thappan
 */
package org.eclipse.stardust.ui.web.rest.service.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.dto.UserDetailsLevel;
import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.engine.api.model.Organization;
import org.eclipse.stardust.engine.api.model.QualifiedModelParticipantInfo;
import org.eclipse.stardust.engine.api.model.Role;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.engine.api.query.UserDetailsPolicy;
import org.eclipse.stardust.engine.api.query.UserQuery;
import org.eclipse.stardust.engine.api.query.Users;
import org.eclipse.stardust.engine.api.runtime.DeployedModel;
import org.eclipse.stardust.engine.api.runtime.Grant;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.ui.web.bcc.WorkflowFacade;
import org.eclipse.stardust.ui.web.bcc.jsf.IQueryExtender;
import org.eclipse.stardust.ui.web.rest.service.dto.DescriptorsDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.GrantsAssignmentDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.QueryResultDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.RoleAssignmentDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.RoleAssignmentDataDTO;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.common.ModelHelper;
import org.eclipse.stardust.ui.web.viewscommon.common.ParticipantDepartmentPair;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;
import org.springframework.stereotype.Component;

@Component
public class RoleAssignmentUtils
{
   private IQueryExtender queryExtender;

   private final static String QUERY_EXTENDER = "carnotBcRoleAssignment/queryExtender";

   public QueryResultDTO getRoleAssignments()
   {
      Query query = createQuery();
      WorkflowFacade facade = WorkflowFacade.getWorkflowFacade();
      Users users = facade.getAllUsers((UserQuery) query);
      Map<ParticipantDepartmentPair, String> roleNameMap = CollectionUtils.newHashMap();
      List<RoleAssignmentDTO> roleEntries = CollectionUtils.newArrayList();

      Map<User, List<ParticipantDepartmentPair>> userPairMap = CollectionUtils.newHashMap();

      for (User user : users)
      {
         List<Grant> grantsTempList = user.getAllGrants();
         QualifiedModelParticipantInfo modelParticipantInfo = null;
         List<ParticipantDepartmentPair> pairList = CollectionUtils.newArrayList();

         // iterate to create ParticipantDepartmentPair
         for (Grant grant : grantsTempList)
         {
            modelParticipantInfo = getGrantParticipant(grant);
            ParticipantDepartmentPair participantDepartmentPair = ParticipantDepartmentPair
                  .getParticipantDepartmentPair(modelParticipantInfo);
            if (participantDepartmentPair != null)
            {
               // add role to pair list
               pairList.add(participantDepartmentPair);

               // add unique role to roleNameMap
               if (!roleNameMap.containsKey(participantDepartmentPair))
               {
                  roleNameMap.put(participantDepartmentPair, ModelHelper.getParticipantName(modelParticipantInfo));
               }
            }
         }
         // add pairList to userPair Map
         userPairMap.put(user, pairList);
      }
      // Now iterate users
      for (User user : users)
      {
         List<ParticipantDepartmentPair> paitList = userPairMap.get(user);

         List<GrantsAssignmentDTO> grantsEntries = CollectionUtils.newArrayList();
         Set<ParticipantDepartmentPair> roles = roleNameMap.keySet();
         for (ParticipantDepartmentPair participantDepartmentPair : roles)
         {
            boolean found = false;
            for (ParticipantDepartmentPair key : paitList)
            {
               if (participantDepartmentPair.equals(key))
               {
                  found = true;
                  break;
               }
            }

            grantsEntries.add(new GrantsAssignmentDTO(participantDepartmentPair, found));
         }
         roleEntries.add(new RoleAssignmentDTO(user, grantsEntries));
      }

      List<RoleAssignmentDataDTO> roleAssignmentDataList = new ArrayList<RoleAssignmentDataDTO>();
      if (CollectionUtils.isNotEmpty(roleEntries))
      {
         for (RoleAssignmentDTO roleEntry : roleEntries)
         {
            RoleAssignmentDataDTO roleAssignmentData = new RoleAssignmentDataDTO();
            roleAssignmentData.teamMember = roleEntry.name;
            List<DescriptorsDTO> descriptors = CollectionUtils.newArrayList();
            Map<String, Boolean> descriptorsValues = CollectionUtils.newHashMap();
            for (GrantsAssignmentDTO grantAssignment : roleEntry.grants)
            {
               DescriptorsDTO descriptor = new DescriptorsDTO(
                     roleNameMap.get(grantAssignment.participantDepartmentPair));
               descriptorsValues.put(roleNameMap.get(grantAssignment.participantDepartmentPair),
                     grantAssignment.userInRole);
               descriptors.add(descriptor);
            }
            roleAssignmentData.descriptors = descriptors;
            roleAssignmentData.descriptorsValues = descriptorsValues;
            roleAssignmentDataList.add(roleAssignmentData);
         }
      }

      userPairMap = null;
      users = null;

      QueryResultDTO result = new QueryResultDTO();
      result.list = roleAssignmentDataList;
      result.totalCount = roleAssignmentDataList.size();
      return result;
   }

   /**
    * Creates the query to get User Details
    * 
    * @return query
    */
   private Query createQuery()
   {
      WorkflowFacade facade = WorkflowFacade.getWorkflowFacade();
      UserQuery query = facade.getTeamQuery(true);
      query.setPolicy(new UserDetailsPolicy(UserDetailsLevel.Full));

      getQueryExtender();
      if (queryExtender != null)
      {
         queryExtender.extendQuery(query);
      }
      return query;
   }

   /**
    * @return IQueryExtender
    */
   private IQueryExtender getQueryExtender()
   {
      if (queryExtender == null)
      {
         SessionContext sessionCtx = SessionContext.findSessionContext();
         return (IQueryExtender) sessionCtx.lookup(QUERY_EXTENDER);
      }
      return queryExtender;
   }

   /**
    * @param grant
    * @return
    */
   private QualifiedModelParticipantInfo getGrantParticipant(Grant grant)
   {
      ModelParticipantInfo modelParticipantInfo = null;

      DeployedModel deployedModel = ModelCache.findModelCache().getActiveModel(grant);
      // Organization grant
      if (grant.isOrganization())
      {
         if (grant.getDepartment() != null)
         {
            Organization organization = deployedModel.getOrganization(grant.getId());
            modelParticipantInfo = grant.getDepartment().getScopedParticipant(organization);
         }
         else
         {
            modelParticipantInfo = deployedModel.getOrganization(grant.getId());
         }
      }
      // Role grant
      else
      {
         if (grant.getDepartment() != null)
         {
            Role role = deployedModel.getRole(grant.getId());
            modelParticipantInfo = grant.getDepartment().getScopedParticipant(role);
         }
         else
         {
            modelParticipantInfo = deployedModel.getRole(grant.getId());
         }
      }

      return (QualifiedModelParticipantInfo) modelParticipantInfo;
   }

}
