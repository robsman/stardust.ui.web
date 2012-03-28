/*
 * $Id$
 * (C) 2000 - 2012 CARNOT AG
 */
package org.eclipse.stardust.ui.web.viewscommon.dialogs;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.engine.api.model.Organization;
import org.eclipse.stardust.engine.api.model.OrganizationInfo;
import org.eclipse.stardust.engine.api.model.Participant;
import org.eclipse.stardust.engine.api.model.ParticipantInfo;
import org.eclipse.stardust.engine.api.model.Role;
import org.eclipse.stardust.engine.api.model.RoleInfo;
import org.eclipse.stardust.engine.api.query.FilterOrTerm;
import org.eclipse.stardust.engine.api.query.UserQuery;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.Department;
import org.eclipse.stardust.engine.api.runtime.DeployedModel;
import org.eclipse.stardust.engine.api.runtime.PerformerType;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;
import org.eclipse.stardust.ui.web.viewscommon.utils.ParticipantUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.RegExUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ServiceFactoryUtils;


/**
 * Class is DelegateProvider implementation for Case Process
 * 
 * @author Vikas.Mishra
 * @version $Revision: $
 */
public class CaseDelegateProvider implements IDelegatesProvider, Serializable
{
   private static final long serialVersionUID = -3937163761535955743L;
   public static final CaseDelegateProvider INSTANCE = new CaseDelegateProvider();

   /**
    * private constructor
    */
   private CaseDelegateProvider()
   {

   }

   /**
    * 
    */
   public Map<PerformerType, List< ? extends ParticipantInfo>> findDelegates(List<ActivityInstance> activityInstances,
         Options options)
   {

      Map<PerformerType, List< ? extends ParticipantInfo>> result = CollectionUtils.newHashMap();
      // add users
      if (options.getPerformerTypes().contains(USER_TYPE))
      {
         result.putAll(findUserDelegates(options));
      }
      // add org,role and department
      Collection<ParticipantInfo> participantInfos = getParticipantsFromActiveModels(options);
      result.putAll(findParticipantDelegates(participantInfos, options));

      return result;

   }

   /**
    * 
    * @param options
    * @return
    */
   private Map<PerformerType, List< ? extends ParticipantInfo>> findUserDelegates(Options options)
   {
      Map<PerformerType, List< ? extends ParticipantInfo>> result = CollectionUtils.newHashMap();

      // user filter
      if (options.getPerformerTypes().contains(USER_TYPE))
      {
         UserQuery userQuery = UserQuery.findActive();

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
         userQuery.orderBy(UserQuery.LAST_NAME).and(UserQuery.FIRST_NAME).and(UserQuery.ACCOUNT);

         List< ? extends Participant> matchingUsers = ServiceFactoryUtils.getQueryService().getAllUsers(userQuery);
         // add result for type
         result.put(PerformerType.User, matchingUsers);

      }
      return result;
   }

   /**
    * 
    * @param participantInfos
    * @param options
    */
   private Map<PerformerType, List< ? extends ParticipantInfo>> findParticipantDelegates(
         final Collection<ParticipantInfo> participantInfos, final Options options)
   {
      Map<PerformerType, List< ? extends ParticipantInfo>> result = CollectionUtils.newHashMap();

      String filterValue = options.getNameFilter();
      String regex = null;

      if (!StringUtils.isEmpty(filterValue))
      {
         regex = RegExUtils.escape(filterValue.toLowerCase()).replaceAll("\\*", ".*") + ".*";
      }

      List<ParticipantInfo> matchingModelParticipants = CollectionUtils.newList();
      // filter participants if we search for a string

      for (ParticipantInfo participantInfo : participantInfos)
      {
         String name = participantInfo.getName();
         if (StringUtils.isEmpty(regex) || name.toLowerCase().matches(regex))
         {
            matchingModelParticipants.add(participantInfo);
         }
      }
      result.put(PerformerType.ModelParticipant, matchingModelParticipants);
      return result;
   }

   /**
    * 
    * @return
    */
   private Collection<ParticipantInfo> getParticipantsFromActiveModels(Options options)
   {
      ModelCache modelCache = ModelCache.findModelCache();
      List<DeployedModel> activeModels = modelCache.getActiveModels();
      Map<String, ParticipantInfo> participants = CollectionUtils.newMap();

      for (DeployedModel model : activeModels)
      {
         List<Participant> allParticipants = model.getAllParticipants();
         for (Participant participant : allParticipants)
         {
            if (participant instanceof Role || participant instanceof Organization || participant instanceof Department)
            {
               // add
               if (!filterParticipant(participant, options))
               {
                  participants.put(ParticipantUtils.getParticipantUniqueKey(participant), participant);              
               }

               ModelParticipantInfo mp = (ModelParticipantInfo) participant;
               if (mp.isDepartmentScoped())
               {
                  List<ModelParticipantInfo> runtimeScopes = ParticipantUtils.getRuntimeScopes(mp);

                  for (ModelParticipantInfo participantInfo : runtimeScopes)
                  {
                     // add
                     if (!filterParticipant(participantInfo, options))
                     {
                        participants.put(ParticipantUtils.getParticipantUniqueKey(participantInfo), participantInfo);
                     }
                  }

               }
            }
         }
      }

      Collection<ParticipantInfo> uniqueParticipants = participants.values();
      return uniqueParticipants;
   }

   /**
    * 
    * @param participant
    * @param options
    * @return
    */
   private boolean filterParticipant(ParticipantInfo participant, Options options)
   {
      if (participant instanceof OrganizationInfo && options.getPerformerTypes().contains(ORGANIZATION_TYPE))
      {
         return false;
      }
      // else if (participant instanceof DepartmentInfo &&
      // options.getPerformerTypes().contains(DEPARTMENT_TYPE))
      // {
      // return false;
      // }
      else if (participant instanceof RoleInfo && options.getPerformerTypes().contains(ROLE_TYPE))
      {
         return false;
      }
      return true;
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

}
