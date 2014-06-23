package org.eclipse.stardust.ui.mobile.service;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.engine.api.model.ConditionalPerformer;
import org.eclipse.stardust.engine.api.model.ModelParticipant;
import org.eclipse.stardust.engine.api.model.Organization;
import org.eclipse.stardust.engine.api.model.Participant;
import org.eclipse.stardust.engine.api.model.Role;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.FilterAndTerm;
import org.eclipse.stardust.engine.api.query.FilterOrTerm;
import org.eclipse.stardust.engine.api.query.QueryResult;
import org.eclipse.stardust.engine.api.query.UserQuery;
import org.eclipse.stardust.engine.api.query.Users;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.ui.web.viewscommon.common.spi.user.impl.IppUser;

/**
 * @author Shrikant.Gangal
 *
 */
public class DelegationHelper
{
   QueryService queryService;

   WorkflowService workflowService;

   public DelegationHelper(QueryService queryService, WorkflowService workflowService)
   {
      this.queryService = queryService;
      this.workflowService = workflowService;
   }

   public JsonObject getMatchingDelegates(String activityInstanceOid, String delegateName)
   {

      ActivityInstanceQuery query = ActivityInstanceQuery.findAll();
      FilterAndTerm filter = query.getFilter().addAndTerm();
      filter.and(ActivityInstanceQuery.OID.isEqual(Long.parseLong(activityInstanceOid)));
      QueryResult<ActivityInstance> activityInstances = queryService.getAllActivityInstances(query);

      JsonObject resultJson = new JsonObject();
      if (activityInstances.size() == 1)
      {

         // TODO - use faces independent model cache once moved to portal-common.
         // DeployedModel model = ModelCache.findModelCache().getModel(
         // activityInstances.get(0).getModelOID());

         // List<Participant> participants = serviceFactory.getQueryService()
         // .getAllParticipants(activityInstances.get(0).getModelOID());
         // System.out.println("@@@@@@@@@@@@@@@@@@@@@ getDefaultPerformerID " +
         // activityInstances.get(0).getActivity().getDefaultPerformer().getId());
         // // DeployedModel model = serviceFactory.getQueryService().getModel(
         // // activityInstances.get(0).getModelOID(), false);
         // // List<Participant> participants = model.getAllParticipants();
         // String regex = "";
         // if ( !StringUtils.isEmpty(delegateeName))
         // {
         // regex = RegExUtils.escape(delegateeName.toLowerCase())
         // .replaceAll("\\*", ".*") + ".*";
         // }
         // Set<String> roles = new HashSet<String>();
         // for (Participant participant : participants)
         // {
         //
         // if (participant instanceof Role)
         // {
         // System.out.println("@@@@@@@@@@@ role: " + participant.getName());
         // roles.add(participant.getId());
         // }
         // else
         // {
         // System.out.println("@@@@@@@@@@@ Not role: " + participant.getName());
         // }
         //
         // if (StringUtils.isEmpty(regex)
         // || participant.getName().toLowerCase().matches(regex))
         // {
         // JsonObject participantJSON = new JsonObject();
         // participantJSON.addProperty("name", participant.getName());
         // participantJSON.addProperty("id", participant.getId());
         // // participantJSON.addProperty("id", participant inst);
         // userInstancesJson.add(participantJSON);
         // }
         // }

         JsonArray userInstancesJson = new JsonArray();

         ModelParticipant modelParticipant = activityInstances.get(0)
               .getActivity()
               .getDefaultPerformer();
         Set<String> roles = new HashSet<String>();

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

            roles.add(modelParticipant.getQualifiedId());

            if (mathesSearchString(modelParticipant.getId(), delegateName)
                  || mathesSearchString(modelParticipant.getName(), delegateName))
            {
               JsonObject paJSON = new JsonObject();
               paJSON.addProperty("name", modelParticipant.getName());
               paJSON.addProperty("id", modelParticipant.getId());
               paJSON.addProperty("type", "role");
               userInstancesJson.add(paJSON);
            }
         }
         // at the moment add all to defaultPerformerSet
         if (modelParticipant instanceof Role)
         {
            roles.add(modelParticipant.getQualifiedId());

            if (mathesSearchString(modelParticipant.getId(), delegateName)
                  || mathesSearchString(modelParticipant.getName(), delegateName))
            {
               JsonObject paJSON = new JsonObject();
               paJSON.addProperty("name", modelParticipant.getName());
               paJSON.addProperty("id", modelParticipant.getId());
               paJSON.addProperty("type", "role");
               userInstancesJson.add(paJSON);
            }
         }
         // resolve organization
         if (modelParticipant instanceof Organization)
         {
            Organization org = (Organization) modelParticipant;
            addOrganisations(org, roles, userInstancesJson, delegateName);
         }

         roles.add(modelParticipant.getQualifiedId());

         UserQuery userQuery = new UserQuery();
         // FilterAndTerm userAndFilter = userQuery.getFilter();
         if ( !StringUtils.isEmpty(delegateName))
         {
            FilterOrTerm or = userQuery.getFilter().addOrTerm();
            or.add(UserQuery.LAST_NAME.like(delegateName));
            or.add(UserQuery.LAST_NAME.like(alternateFirstLetter(delegateName)));
            or.add(UserQuery.FIRST_NAME.like(delegateName));
            or.add(UserQuery.FIRST_NAME.like(alternateFirstLetter(delegateName)));
            or.add(UserQuery.ACCOUNT.like(delegateName));
            or.add(UserQuery.ACCOUNT.like(alternateFirstLetter(delegateName)));
         }
         Users matchingUsers = queryService.getAllUsers(userQuery);
         for (User participant : matchingUsers)
         {
            System.out.println("@@@@@@@@@@@@ participant " + participant.getName()
                  + " grants " + participant.getAllGrants() + " in roles ? " + roles);
            if (org.eclipse.stardust.ui.web.common.util.UserUtils.isAuthorized(
                  new IppUser(participant), roles, new HashSet<String>()))
            {
               JsonObject participantJSON = new JsonObject();
               participantJSON.addProperty("name", participant.getName());
               participantJSON.addProperty("id", participant.getId());
               participantJSON.addProperty("type", "user");
               // participantJSON.addProperty("id", participant inst);
               userInstancesJson.add(participantJSON);
            }
         }

         resultJson.add("data", userInstancesJson);
      }

      return resultJson;
   }

   /**
    * @param ai
    * @param delegateId
    * @return
    */
   public ActivityInstance delegateActivity(ActivityInstance ai, String delegateId)
   {
      try
      {
         Participant p = queryService.getParticipant(delegateId);
         return workflowService.delegateToParticipant(ai.getOID(), p.getId());
      }
      catch (ObjectNotFoundException e)
      {
         UserQuery userQuery = new UserQuery();
         FilterAndTerm and = userQuery.getFilter().addAndTerm();
         and.add(UserQuery.ACCOUNT.isEqual(delegateId));
         Users u = queryService.getAllUsers(userQuery);
         return workflowService.delegateToUser(ai.getOID(), u.get(0).getOID());
      }
   }

   /**
    * @param field
    * @return
    */
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

   /**
    * @param org
    * @param modelParticipants
    */
   private void addOrganisations(Organization org, Set<String> modelParticipants,
         JsonArray userInstancesJson, String delegateName)
   {
      modelParticipants.add(org.getQualifiedId());

      if (mathesSearchString(org.getId(), delegateName)
            || mathesSearchString(org.getName(), delegateName))
      {
         JsonObject paJSON = new JsonObject();
         paJSON.addProperty("name", org.getName());
         paJSON.addProperty("id", org.getId());
         paJSON.addProperty("type", "organization");
         userInstancesJson.add(paJSON);
      }
      Iterator<Role> iter = org.getAllSubRoles().iterator();
      while (iter.hasNext())
      {
         Role role = iter.next();
         modelParticipants.add(role.getQualifiedId());

         if (mathesSearchString(role.getId(), delegateName)
               || mathesSearchString(role.getName(), delegateName))
         {
            JsonObject roleJSON = new JsonObject();
            roleJSON.addProperty("name", role.getName());
            roleJSON.addProperty("id", role.getId());
            roleJSON.addProperty("type", "role");
            userInstancesJson.add(roleJSON);
         }
      }

      Iterator<Organization> orgIter = org.getAllSubOrganizations().iterator();
      while (orgIter.hasNext())
      {
         Organization suborg = orgIter.next();
         addOrganisations(suborg, modelParticipants, userInstancesJson, delegateName);
      }
   }

   /**
    * @param name
    * @param searchString
    * @return
    */
   private boolean mathesSearchString(String name, String searchString)
   {
      if (StringUtils.isNotEmpty(searchString))
      {
         if (searchString.equals(name) || alternateFirstLetter(searchString).equals(name))
         {
            return true;
         }
         else
         {
            return false;
         }
      }

      return true;
   }
}
