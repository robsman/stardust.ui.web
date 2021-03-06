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
package org.eclipse.stardust.ui.web.rest.component.util;

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
import org.eclipse.stardust.ui.web.rest.dto.GenericQueryResultDTO;
import org.eclipse.stardust.ui.web.rest.dto.RoleAssignmentDTO;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.common.ModelHelper;
import org.eclipse.stardust.ui.web.viewscommon.common.ParticipantDepartmentPair;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;
import org.eclipse.stardust.ui.web.viewscommon.utils.UserUtils;
import org.springframework.stereotype.Component;

@Component
public class RoleAssignmentUtils
{
   private IQueryExtender queryExtender;

   private final static String QUERY_EXTENDER = "carnotBcRoleAssignment/queryExtender";

   public GenericQueryResultDTO getRoleAssignments()
   {
      Query query = createQuery();
      WorkflowFacade facade = WorkflowFacade.getWorkflowFacade();
      Users users = facade.getAllUsers((UserQuery) query);
      Map<ParticipantDepartmentPair, String> roleNameMap = CollectionUtils.newHashMap();
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
      List<RoleAssignmentDTO> roleAssignmentList = new ArrayList<RoleAssignmentDTO>();
      for (User user : users)
      {
         RoleAssignmentDTO roleAssignmentData = new RoleAssignmentDTO();
         roleAssignmentData.teamMember = UserUtils.getUserDisplayLabel(user);
         roleAssignmentData.userId = user.getId();
         roleAssignmentData.userOid = String.valueOf(user.getOID());

         Map<String, Boolean> columnsValue = CollectionUtils.newHashMap();
         Map<String, String> columnsLabel = CollectionUtils.newHashMap();
         List<ParticipantDepartmentPair> paitList = userPairMap.get(user);
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

            columnsValue.put(roleNameMap.get(participantDepartmentPair), found);
            columnsLabel.put(roleNameMap.get(participantDepartmentPair), participantDepartmentPair.getFirst() + "_"
                  + participantDepartmentPair.getSecond());
         }
         roleAssignmentData.columnsValue = columnsValue;
         roleAssignmentData.columnsLabel = columnsLabel;
         roleAssignmentList.add(roleAssignmentData);
      }

      GenericQueryResultDTO result = new GenericQueryResultDTO();

      if (roleAssignmentList.get(0) != null)
      {
         result.columnsDefinition = roleAssignmentList.get(0).columnsLabel;
         result.columns = roleAssignmentList.get(0).columnsValue.keySet();
         for (int i = 0; i < roleAssignmentList.size(); i++)
         {
            roleAssignmentList.get(i).columnsLabel = null;
         }
         result.list = roleAssignmentList;
         result.totalCount = roleAssignmentList.size();

      }

      userPairMap = null;
      users = null;
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
