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
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.model.ConditionalPerformer;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.ModelParticipant;
import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.engine.api.model.Organization;
import org.eclipse.stardust.engine.api.model.OrganizationInfo;
import org.eclipse.stardust.engine.api.model.Participant;
import org.eclipse.stardust.engine.api.model.ParticipantInfo;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.model.Role;
import org.eclipse.stardust.engine.api.model.RoleInfo;
import org.eclipse.stardust.engine.api.query.FilterAndTerm;
import org.eclipse.stardust.engine.api.query.FilterOrTerm;
import org.eclipse.stardust.engine.api.query.FilterTerm;
import org.eclipse.stardust.engine.api.query.ParticipantAssociationFilter;
import org.eclipse.stardust.engine.api.query.UserQuery;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.engine.api.runtime.Department;
import org.eclipse.stardust.engine.api.runtime.DepartmentInfo;
import org.eclipse.stardust.engine.api.runtime.DeployedModel;
import org.eclipse.stardust.engine.api.runtime.PerformerType;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.QualityAssuranceUtils.QualityAssuranceState;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ParticipantUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.RegExUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ServiceFactoryUtils;



/**
 * @author sauer
 * @version $Revision: $
 */
public class DefaultDelegatesProvider implements IDelegatesProvider, Serializable
{
   private static final long serialVersionUID = 1L;

   public static final DefaultDelegatesProvider INSTANCE = new DefaultDelegatesProvider();

   public Map<PerformerType, List<? extends ParticipantInfo>> findDelegates(
         List<ActivityInstance> activityInstances, Options options)
   {
      Map<PerformerType, List<? extends ParticipantInfo>> result = CollectionUtils.newMap();
      QueryService service = getQueryService();
     

      if ((null != service) && (null != activityInstances))
      {
         // collect models
         Set<Integer> models = CollectionUtils.newSet();   
         
         for (int i = 0; i < activityInstances.size(); ++i)
         {
            ActivityInstance ai = activityInstances.get(i);
            if (!models.contains(new Integer(ai.getModelOID())))
            {
               // collect all model oid's
               models.add(new Integer(ai.getModelOID()));
            }
         }
        

         // user filter
         if (options.getPerformerTypes().contains(USER_TYPE))
         {
            // limited or not
            UserQuery userQuery = (options.isStrictSearch()) ? buildStrictUserQuery(activityInstances) : UserQuery
                  .findActive();

            // filter for user names if selected
            if (!StringUtils.isEmpty(options.getNameFilter()))
            {
               String name = options.getNameFilter().replaceAll("\\*", "%") + "%";
               String nameFirstLetterCaseChanged = alternateFirstLetter(name);
               FilterOrTerm or = userQuery.getFilter().addOrTerm();
               or.add(UserQuery.LAST_NAME.like(name));
               or.add(UserQuery.LAST_NAME.like(nameFirstLetterCaseChanged));
               or.add(UserQuery.FIRST_NAME.like(name));
               or.add(UserQuery.FIRST_NAME.like(nameFirstLetterCaseChanged));
               or.add(UserQuery.ACCOUNT.like(name));
               or.add(UserQuery.ACCOUNT.like(nameFirstLetterCaseChanged));
            }
            userQuery.orderBy(UserQuery.LAST_NAME).and(UserQuery.FIRST_NAME).and(
                  UserQuery.ACCOUNT);

            List<? extends Participant> matchingUsers = service.getAllUsers(userQuery);
            // add result for type
            result.put(PerformerType.User, matchingUsers);
         }

         // model participants (roles, organizations)
         if (options.getPerformerTypes().contains(ROLE_TYPE)
               || options.getPerformerTypes().contains(ORGANIZATION_TYPE))
         {
            Set<String> defaultPerformerSet = CollectionUtils.newSet();
            Set<String> activityPerformerSet;

            for (int i = 0; i < activityInstances.size(); ++i)
            {
               activityPerformerSet = CollectionUtils.newSet();
               ActivityInstance ai = activityInstances.get(i);

               // limited search
               if (options.isStrictSearch())
               {
                  ModelParticipant modelParticipant = getActivityPerformer(ai);                  

                  if (modelParticipant instanceof ConditionalPerformer)
                  {
                     // resolve conditional performer
                     ConditionalPerformer cp = (ConditionalPerformer) modelParticipant;
                     Participant p = cp.getResolvedPerformer();
                     modelParticipant = null;
                     // user and user groups?
                     if (p instanceof ModelParticipant)
                     {
                        modelParticipant = (ModelParticipant) p;
                     }
                  }
                  // at the moment add all to defaultPerformerSet
                  if (modelParticipant instanceof Role)
                  {
                     activityPerformerSet.add(modelParticipant.getId());
                  }
                  // resolve organization
                  if (modelParticipant instanceof Organization)
                  {
                     Organization org = (Organization) modelParticipant;
                     addOrganisations(org, activityPerformerSet);
                  }

                  if (defaultPerformerSet.isEmpty())
                  {
                     if (!activityPerformerSet.isEmpty())
                     {
                        defaultPerformerSet.addAll(activityPerformerSet);
                     }
                  }
                  else
                  {
                     // merge
                     if (!activityPerformerSet.isEmpty())
                     {
                        Set<String> tmpPerformerSet = CollectionUtils.newSet();
                        for (Iterator<String> api = activityPerformerSet.iterator(); api
                              .hasNext();)
                        {
                           String performerId = api.next();
                           if (defaultPerformerSet.contains(performerId))
                           {
                              tmpPerformerSet.add(performerId);
                           }
                        }
                        defaultPerformerSet.clear();
                        if (!tmpPerformerSet.isEmpty())
                        {
                           defaultPerformerSet.addAll(tmpPerformerSet);
                        }
                     }
                  }
               }
            }

            // get all from model and select only the ones we have filtered
            Collection<Participant> candidateParticipants = getCommonParticipantsFromModels(models);
            List<String> roleIds = CollectionUtils.newList();
            List<String> orgIds = CollectionUtils.newList();
            for (Iterator<Participant> i = candidateParticipants.iterator(); i.hasNext();)
            {
               Participant p = i.next();
               if (!options.isStrictSearch() || defaultPerformerSet.contains(p.getId()))
               {
                  if (p instanceof Organization)
                  {
                     if (options.getPerformerTypes().contains(ORGANIZATION_TYPE))
                     {
                        orgIds.add(p.getName());
                     }
                  }
                  else if (p instanceof Role)
                  {
                     if (options.getPerformerTypes().contains(ROLE_TYPE))
                     {
                        if (options.getPerformerTypes().contains(
                              DISABLE_ADMINISTRATOR_ROLE))
                        {
                           if (!p.getId().equals(PredefinedConstants.ADMINISTRATOR_ROLE))
                           {
                              roleIds.add(p.getName());
                           }
                        }
                        else
                        {
                           roleIds.add(p.getName());
                        }
                     }
                  }
               }
            }
            // sort, so we have all in order
            Collections.sort(roleIds);
            Collections.sort(orgIds);
            roleIds.addAll(orgIds);

            String filterValue = options.getNameFilter();
            String regex = null;
            
            if (!StringUtils.isEmpty(filterValue))
            {
               regex = RegExUtils.escape(filterValue.toLowerCase()).replaceAll("\\*", ".*") + ".*";
            }

            List<Participant> matchingModelParticipants = CollectionUtils.newList();
            // filter participants if we search for a string
            for (Iterator<String> i = roleIds.iterator(); i.hasNext();)
            {
               String name = i.next();
               if (StringUtils.isEmpty(regex) || name.toLowerCase().matches(regex))
               {
                  addParticipantToSearchResult(matchingModelParticipants, name,
                        candidateParticipants);
               }
            }
            result.put(PerformerType.ModelParticipant, matchingModelParticipants);
         }

      }
      return result;
   }
   
   private void addOrganisations(Organization org, Set<String> modelParticipants)
   {
      modelParticipants.add(org.getId());
      Iterator<Role> iter = org.getAllSubRoles().iterator();
      while (iter.hasNext())
      {
         Role role = iter.next();
         modelParticipants.add(role.getId());
      }
      
      Iterator<Organization> orgIter = org.getAllSubOrganizations().iterator();
      while (orgIter.hasNext())
      {
         Organization suborg = orgIter.next();
         addOrganisations(suborg, modelParticipants);
      }
   }

   private static QueryService getQueryService()
   {
      SessionContext sessionContext = SessionContext.findSessionContext();
      if (sessionContext != null && sessionContext.isSessionInitialized())
      {
         return sessionContext.getServiceFactory().getQueryService();
      }
      return null;
   }

   private static String alternateFirstLetter(String field)
   {
      String firstLetter = field.substring(0, 1);
      if (firstLetter.compareTo(field.substring(0, 1).toLowerCase()) == 0)
      {
         firstLetter = firstLetter.toUpperCase();
      }
      else
      {
         firstLetter = firstLetter.toLowerCase();
      }
      return firstLetter + field.substring(1);
   }

   // user filter
   private static UserQuery buildStrictUserQuery(List<ActivityInstance> ais)
   {
      UserQuery userQuery = UserQuery.findActive();

      FilterAndTerm userAndTerm = userQuery.getFilter();
      if (!CollectionUtils.isEmpty(ais))
      {
         for (int i = 0; i < ais.size(); ++i)
         {
            ActivityInstance ai = ais.get(i);
            if (ai.getState() != ActivityInstanceState.Completed
                  && ai.getState() != ActivityInstanceState.Aborted)
            {
               // Get the default performer for the AI
               ModelParticipant defaultPerformer = getActivityPerformer(ai);                 

               // Get the current performer for the AI
               ParticipantInfo currentPerformer = ai.getCurrentPerformer();

               // Get department for current participant performer
               Department department = null;
               if (currentPerformer instanceof ModelParticipantInfo)
               {
                  DepartmentInfo departmentInfo = ((ModelParticipantInfo) currentPerformer).getDepartment();
                  if (departmentInfo != null)
                  {
                     department = getAdministrationService().getDepartment(departmentInfo.getOID());
                  }
               }
               
               if (defaultPerformer instanceof ConditionalPerformer)
               {
                  ConditionalPerformer condPerf = (ConditionalPerformer) defaultPerformer;
                  Participant participant = condPerf.getResolvedPerformer();
                  if (participant instanceof ModelParticipant)
                  {
                     defaultPerformer = (ModelParticipant) participant;
                  }
               }
               if (defaultPerformer instanceof Organization)
               {
                  FilterTerm orTerm = userAndTerm.addOrTerm();
                  Organization org = (Organization) defaultPerformer;

                  Organization currentPerformerOrg = getCurrentPerformerOrganization(currentPerformer);
                  
                  // If the AI has been delegated downwards to a scoped sub-Organization or its Roles, ...
                  // ... we use the current Performer Organization; else we use the model default Performer Organization
                  boolean useCurrentPerformer = false;
                  if ((currentPerformerOrg != null)
                        && !currentPerformerOrg.getId().equals(org.getId()) // current Performer Organization != default Performer Organization
                        && currentPerformerOrg.isDepartmentScoped())
                  {
                     useCurrentPerformer = true;
                  }
                  
                  List<ModelParticipantInfo> subParticipants;
                  if (!useCurrentPerformer)
                  {
                     // Get sub-participants for the model default organization to add to user query filter
                     subParticipants = ParticipantUtils.getSubParticipants(department, org);
                  }
                  else
                  {
                     // Get sub-participants for the current performer's organization ... 
                     // ... (if current performer is model participant) to add to user query filter
                     subParticipants = ParticipantUtils.getSubParticipants(department, currentPerformerOrg);
                  }
                  
                  // Iterate over sub-participants and add ParticipantAssociationFilter to user query  
                  for (ModelParticipantInfo modelParticipantInfo : subParticipants)
                  {
                     orTerm.add(ParticipantAssociationFilter.forParticipant(modelParticipantInfo, false));
                  }
               }
               else if (defaultPerformer != null)
               {
                  ModelParticipantInfo modelParticipantInfo = ParticipantUtils.getScopedParticipant(defaultPerformer, department);
                  userAndTerm.and(ParticipantAssociationFilter.forParticipant(modelParticipantInfo, false));
               }
            }
         }
      }
      return userQuery;
   }

   

   // collect all participants
   private static Collection<Participant> getCommonParticipantsFromModels(
         Set<Integer> models)
   {
      ModelCache modelCache = ModelCache.findModelCache();
      Map<String, Participant> participants = CollectionUtils.newMap();
      List<String> commonParticipants = CollectionUtils.newList();

      List<Participant> result = CollectionUtils.newList();

      if (modelCache != null && models != null)
      {
         Iterator<DeployedModel> modelIter = modelCache.getAllModels().iterator();
         while (modelIter.hasNext())
         {
            Model model = modelIter.next();
            if (models.contains(new Integer(model.getModelOID())))
            {
               if (participants.isEmpty())
               {
                  for (Iterator<Participant> pIter = model.getAllParticipants().iterator(); pIter
                        .hasNext();)
                  {
                     Participant participant = pIter.next();
                     String participantId = participant.getId();

                     // TODO what about organizations?
                     if (participant instanceof Role
                           || participant instanceof Organization)
                     {
                        participants.put(participantId, participant);
                        commonParticipants.add(participantId);
                     }
                  }
               }
               else
               {
                  List<String> modelParticipants = CollectionUtils.newList();

                  for (Iterator<Participant> pIter = model.getAllParticipants().iterator(); pIter
                        .hasNext();)
                  {
                     Participant participant = pIter.next();
                     modelParticipants.add(participant.getId());
                  }
                  commonParticipants.retainAll(modelParticipants);
               }
            }
         }
      }

      for (Iterator<Participant> pIter = participants.values().iterator(); pIter.hasNext();)
      {
         Participant participant = pIter.next();
         if (commonParticipants.contains(participant.getId()))
         {
            result.add(participant);
         }
      }

      return result;
   }

   private void addParticipantToSearchResult(List<Participant> searchResult, String name,
         Collection<Participant> candidateParticipants)
   {
      for (Iterator<Participant> userGroupIter = candidateParticipants.iterator(); userGroupIter
            .hasNext();)
      {
         Participant p = userGroupIter.next();
         if (CompareHelper.areEqual(p.getName(), name))
         {
            searchResult.add(p);
         }
      }
   }

   /**
    * Get the Organization for the Current Performer (if Current Performer is a ModelParticipant) 
    * @param currentPerformer
    * @return
    */
   private static Organization getCurrentPerformerOrganization(ParticipantInfo currentPerformer)
   {
      Organization currentPerformerOrg = null;
      if (currentPerformer instanceof ModelParticipantInfo)
      {
         if (currentPerformer instanceof RoleInfo)
         {
            RoleInfo roleInfo = (RoleInfo) currentPerformer;
            Role role = (Role) ModelUtils.getModelCache().getParticipant(roleInfo.getId(), Role.class);
            List<Organization> orgs = role.getAllSuperOrganizations();
            if (!CollectionUtils.isEmpty(orgs))
            {
               currentPerformerOrg = orgs.get(0);
            }
         }
         else if (currentPerformer instanceof OrganizationInfo)
         {
            OrganizationInfo organizationInfo = (OrganizationInfo) currentPerformer;
            currentPerformerOrg = (Organization) ModelUtils.getModelCache().getParticipant(organizationInfo.getId(), 
                  Organization.class);
         }
      }
      
      return currentPerformerOrg;
   }

   private static AdministrationService getAdministrationService()
   {
      return ServiceFactoryUtils.getAdministrationService();
   }
   
   /**
    * @param ai
    * @return
    */
   private static ModelParticipant getActivityPerformer(ActivityInstance ai)
   {
      ModelParticipant performer = null;
      if (QualityAssuranceState.IS_QUALITY_ASSURANCE.equals(ai.getQualityAssuranceState()))
      {
         performer = ai.getActivity().getQualityAssurancePerformer();
      }
      else
      {
         performer = ai.getActivity().getDefaultPerformer();
      }
      return performer;
   }
}