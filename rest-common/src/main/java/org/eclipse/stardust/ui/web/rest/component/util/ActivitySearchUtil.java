package org.eclipse.stardust.ui.web.rest.component.util;

import static org.eclipse.stardust.common.StringUtils.isEmpty;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.dto.UserDetailsLevel;
import org.eclipse.stardust.engine.api.query.ActivityFilter;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.ActivityInstances;
import org.eclipse.stardust.engine.api.query.FilterAndTerm;
import org.eclipse.stardust.engine.api.query.FilterOrTerm;
import org.eclipse.stardust.engine.api.query.PerformingUserFilter;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.engine.api.query.SubsetPolicy;
import org.eclipse.stardust.engine.api.query.UserDetailsPolicy;
import org.eclipse.stardust.engine.api.query.UserQuery;
import org.eclipse.stardust.engine.api.query.Users;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.ui.web.rest.dto.UserDTO;
import org.eclipse.stardust.ui.web.rest.dto.response.ActivitySearchDTO;
import org.eclipse.stardust.ui.web.viewscommon.common.configuration.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.viewscommon.common.spi.IFilterProvider;
import org.eclipse.stardust.ui.web.viewscommon.utils.FilterProviderUtil;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ResubmissionUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ResubmissionUtils.ModelResubmissionActivity;
import org.springframework.stereotype.Component;

@Component
public class ActivitySearchUtil
{
   private static final Logger trace = LogManager.getLogger(ActivitySearchUtil.class);

   @Resource
   private ServiceFactoryUtils serviceFactoryUtils;

   public ActivitySearchDTO getAllResubmissionActivityInstances()
   {
      ActivityInstances activityInstances = getActivityInstances_Resubmission();
      return buildActivitySearchDTO(activityInstances);

   }

   public ActivitySearchDTO getAllActivityInstances()
   {
      ActivityInstances activityInstances = getActivityInstances_anyActivatable();
      return buildActivitySearchDTO(activityInstances);
   }

   private ActivitySearchDTO buildActivitySearchDTO(ActivityInstances activityInstances)
   {
      ActivitySearchDTO activitySearchDTO = null;
      if (activityInstances != null)
      {
         activitySearchDTO = new ActivitySearchDTO();
         activitySearchDTO.totalCount = activityInstances.getTotalCount();
      }
      return activitySearchDTO;
   }

   private ActivityInstances getActivityInstances_Resubmission()
   {
      ActivityInstanceQuery query = ActivityInstanceQuery.findInState(ActivityInstanceState.Hibernated);
      query.getFilter().addOrTerm().or(PerformingUserFilter.CURRENT_USER).or(new PerformingUserFilter(0));

      List<ModelResubmissionActivity> resubmissionActivities = CollectionUtils.newList();
      ResubmissionUtils.fillListWithResubmissionActivities(resubmissionActivities);

      if (resubmissionActivities.isEmpty())
      {
         query.getFilter().add(ActivityInstanceQuery.ACTIVITY_OID.isNull());
      }
      else
      {
         FilterOrTerm or = query.getFilter().addOrTerm();
         for (Iterator<ModelResubmissionActivity> as = resubmissionActivities.iterator(); as.hasNext();)
         {
            ModelResubmissionActivity activity = as.next();
            or.add(ActivityFilter.forProcess(activity.getActivityId(), activity.getProcessId(),// TODO:check
                                                                                               // FQID
                                                                                               // change
                  activity.getModelOids(), false));
         }
      }

      applyFilterProviders(query);

      return serviceFactoryUtils.getQueryService().getAllActivityInstances((ActivityInstanceQuery) query);

   }

   public ActivityInstances getActivityInstances_anyActivatable()
   {
      ActivityInstanceQuery query = ActivityInstanceQuery.findInState(new ActivityInstanceState[] {
            ActivityInstanceState.Hibernated, ActivityInstanceState.Application, ActivityInstanceState.Suspended});
      // only evaluate count
      query.setPolicy(new SubsetPolicy(0, true));

      applyFilterProviders(query);

      return serviceFactoryUtils.getQueryService().getAllActivityInstances(query);
   }

   /**
    * @param firstName
    * @param lastName
    * @return
    */
   public List<UserDTO> getUsers_anyLike(String firstName, String lastName)
   {
      UserQuery query = UserQuery.findAll();
      String[] prefModules = {UserPreferencesEntries.M_ADMIN_PORTAL};
      UserDetailsPolicy userPolicy = new UserDetailsPolicy(UserDetailsLevel.Core);
      userPolicy.setPreferenceModules(prefModules);
      query.setPolicy(userPolicy);
      //limiting the user search to 20
      query.setPolicy(new SubsetPolicy(20, false));
      
      FilterAndTerm and = query.getFilter().addAndTerm();
      if (!isEmpty(firstName))
      {
         String first = firstName.replace('*', '%');
         first = "%" + first + "%";
         and.add(UserQuery.FIRST_NAME.like(first));
      }
      if (!isEmpty(lastName))
      {
         String last = lastName.replace('*', '%');
         last = "%" + last + "%";
         and.add(UserQuery.LAST_NAME.like(last));
      }
      Users users = serviceFactoryUtils.getQueryService().getAllUsers(query);
      return buildUserList(users);
   }

   public ActivitySearchDTO getWorklistForUser(long userOID)
   {
      ActivityInstances activityInstances = getActivityInstances_forUser(userOID);
      return buildActivitySearchDTO(activityInstances);
   }

   /**
    * @param userOid
    * @return
    */
   private ActivityInstances getActivityInstances_forUser(long userOid)
   {
      ActivityInstanceQuery query = ActivityInstanceQuery.findAll();
      // only evaluate count
      query.setPolicy(new SubsetPolicy(0, true));
      FilterOrTerm or = query.getFilter().addOrTerm();
      or.add(ActivityInstanceQuery.CURRENT_USER_PERFORMER_OID.isEqual(userOid));
      return serviceFactoryUtils.getQueryService().getAllActivityInstances(query);
   }

   /**
    * 
    * @param users
    * @return
    */
   private List<UserDTO> buildUserList(Users users)
   {
      List<UserDTO> usersList = new ArrayList<UserDTO>();
      for (User user : users)
      {
         UserDTO userDTO = new UserDTO();
         userDTO.displayName = I18nUtils.getUserLabel(user);
         userDTO.oid = user.getOID();
         userDTO.id = user.getId();
         usersList.add(userDTO);
      }
      return usersList;

   }

   public static void applyFilterProviders(Query query)
   {

      List<IFilterProvider> filterProviders = FilterProviderUtil.getInstance().getFilterProviders();

      if (trace.isDebugEnabled())
      {
         trace.debug("Applying Filter Providers = " + filterProviders.size());
      }

      for (IFilterProvider filterProvider : filterProviders)
      {
         filterProvider.applyFilter(query);
      }
   }

}
