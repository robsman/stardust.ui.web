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
/**
 * 
 */
package org.eclipse.stardust.ui.web.processportal.launchpad;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.query.ActivityInstances;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.engine.api.query.QueryResult;
import org.eclipse.stardust.engine.api.query.Users;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.ui.web.common.uielement.AbstractLaunchPanel;
import org.eclipse.stardust.ui.web.processportal.common.PPUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;



/**
 * @author roland.stamm
 * 
 */
public class ActivitySearchPanelBean extends AbstractLaunchPanel
      implements InitializingBean, DisposableBean, IActivitySearchUserSearchHandler
{

   private static final long serialVersionUID = 569448206007135385L;

   private static final String ID_USER_WORKLIST_SEARCH = "userWorklistSearch";

   private static final String ID_ALL_ACTIVITY_INSTANCES = "allActivityInstances";
   
   private static final String ID_ALL_RESUBMISSION_ACTIVITY_INSTANCES = "allResubmissionInstances";

   private static final int SEARCH_RESULT_MAP_SIZE = 3;

   private AllAvailableActivityQueryBuilder allActivityQueryBuilder;
   
   private AllResubmissionActivity allResubmissionActivity;
   
   private Map<String, ActivitySearchModel> lastSearchItems;

   private List<ActivitySearchUserModel> users;   

   private boolean userWorklistSearchPanelVisible;

   private String firstNameFilter;

   private String lastNameFilter;

   public ActivitySearchPanelBean()
   {
      super("activitySearch");
      // TODO Auto-generated constructor stub
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
    */
   public void afterPropertiesSet() throws Exception
   {
      this.lastSearchItems = new LinkedHashMap<String, ActivitySearchModel>(); // TODO
      // find
      // proper
      // Linked tab;

      // (SEARCH_RESULT_MAP_SIZE);
      this.users = CollectionUtils.newArrayList();
      this.allActivityQueryBuilder = new AllAvailableActivityQueryBuilder();
      this.allResubmissionActivity = new AllResubmissionActivity();
      update();
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.springframework.beans.factory.DisposableBean#destroy()
    */
   public void destroy() throws Exception
   {
   }
   
   /**
    * @return
    */
   public String searchAllActivityInstancesAction()
   {
      searchAllActivityInstances();
      return null;
   }

   /**
    * @return
    */
   public String clearAction()
   {
      clear();
      return null;
   }

   /**
    * 
    */
   private void clear()
   {
      lastSearchItems.clear();
   }
   
   public String searchAllResubmissionActivityInstancesAction()
   {
      searchAllResubmissionActivityInstances();
      return null;
   }
   
   private void searchAllResubmissionActivityInstances()
   {
      
      allResubmissionActivity.executeCountQuery();
      if (allResubmissionActivity.getCountQueryResult() != null)
      {
         Map<String, Object> params = CollectionUtils.newTreeMap();
         params.put(Query.class.getName(), allResubmissionActivity.createQuery());
         String name = this.getMessages().getString("resubmission");
         String id = ID_ALL_RESUBMISSION_ACTIVITY_INSTANCES;
         params.put("id", id);
         params.put("name", name);
         params.put("showResubmitLink", true);
         PPUtils.openWorklistView("id=" + id, params);

         PPUtils.selectWorklist(null);

         //add to SearchResult Map
         if(lastSearchItems != null)
         {
            lastSearchItems.put(id, new ActivitySearchModel(id, name, allResubmissionActivity));
         }
      }
   }

   /**
    * 
    */
   private void searchAllActivityInstances()
   {
      allActivityQueryBuilder.executeCountQuery();
      if (allActivityQueryBuilder.getCountQueryResult() != null)
      {
         Map<String, Object> params = CollectionUtils.newTreeMap();
         params.put(Query.class.getName(), allActivityQueryBuilder.createQuery());
         String name = this.getMessages().getString("allActivities");
         String id = ID_ALL_ACTIVITY_INSTANCES;
         params.put("id", id);
         params.put("name", name);

         PPUtils.openWorklistView("id=" + id, params);

         PPUtils.selectWorklist(null);
         if (lastSearchItems.containsKey(id))
            lastSearchItems.remove(id);

         // add to SearchResult Map
         lastSearchItems.put(id, new ActivitySearchModel(id, name, allActivityQueryBuilder));
      }
   }

   /**
    * 
    */
   public void update()
   {
   // TODO maybe update all searchResults?
   // updating all searchResults might be to costly
   }

   public List<ActivitySearchModel> getItems()
   {
      List<ActivitySearchModel> items = CollectionUtils.newArrayList(lastSearchItems.values());
      Collections.reverse(items);
      return items;
   }

   public List<ActivitySearchUserModel> getUsers()
   {
      return users;
   }

   private class AllAvailableActivityQueryBuilder implements IQueryBuilder
   {
      private ActivityInstances countQueryResult;

      public Query createQuery()
      {
         executeCountQuery();
         return countQueryResult.getQuery();
      }

      public ActivityInstances executeCountQuery()
      {
         countQueryResult = PPUtils.getActivityInstances_anyActivatable();
         return countQueryResult;
      }

      public ActivityInstances getCountQueryResult()
      {
         return countQueryResult;
      }
   }
   
   private class AllResubmissionActivity implements IQueryBuilder
   {
      private ActivityInstances countQueryResult;  
      public Query createQuery()
      {
         executeCountQuery();
         return countQueryResult.getQuery();
      }

      public QueryResult executeCountQuery()
      {
         countQueryResult = PPUtils.getActivityInstances_Resubmission();
         return countQueryResult;
      }

      public QueryResult getCountQueryResult()
      {
         return countQueryResult;
      }
      
   }

   // ************************ Worklist Search ********************************

   public String searchUserWorklistAction()
   {
      showFilteredUserSelection();
      return null;
   }

   private void showFilteredUserSelection()
   {
      userWorklistSearchPanelVisible = !userWorklistSearchPanelVisible;
   }

   public String clearUsersAction()
   {
      clearUsers();
      return null;
   }

   private void clearUsers()
   {
      setFirstNameFilter("");
      setLastNameFilter("");
      users.clear();
   }

   public String queryForUsersAction()
   {
      filterUsers();
      return null;
   }

   private void filterUsers()
   {
      users.clear();
     
      Users qusers = PPUtils.getUsers_anyLike(getFirstNameFilter(), getLastNameFilter());
      for (Iterator<User> iterator = qusers.iterator(); iterator.hasNext();)
      {
         User user = iterator.next();
         users.add(new ActivitySearchUserModel(user, this));         
      }
   }

   public boolean isUserWorklistSearchPanelVisible()
   {
      return userWorklistSearchPanelVisible;
   }

   public void setUserWorklistSearchPanelVisible(boolean userWorklistSearchPanelVisible)
   {
      this.userWorklistSearchPanelVisible = userWorklistSearchPanelVisible;
   }

   public String getFirstNameFilter()
   {
      return firstNameFilter;
   }

   public String getLastNameFilter()
   {
      return lastNameFilter;
   }

   public void setLastNameFilter(String lastNameFilter)
   {
      this.lastNameFilter = lastNameFilter;
   }

   public void setFirstNameFilter(String firstNameFilter)
   {
      this.firstNameFilter = firstNameFilter;
   }

   public void searchWorklistFor(User user)
   {
      userWorklistSearchPanelVisible = false;

      UserWorklistQueryBuilder userWorklistQueryBuilder = new UserWorklistQueryBuilder(user);

      userWorklistQueryBuilder.executeCountQuery();
      if (userWorklistQueryBuilder.getCountQueryResult() != null)
      {
         Map<String, Object> params = CollectionUtils.newTreeMap();
         params.put(Query.class.getName(), userWorklistQueryBuilder.createQuery());
         String name = I18nUtils.getUserLabel(user);
         String id = ID_USER_WORKLIST_SEARCH + user.getOID();
         params.put("id", id);
         params.put("name", name);

         PPUtils.openWorklistView("id=" + id, params);

         PPUtils.selectWorklist(null);

         if (lastSearchItems.containsKey(id))
         {
            lastSearchItems.remove(id);
         }

         // add to SearchResult Map
         lastSearchItems.put(id, new ActivitySearchModel(id, name, userWorklistQueryBuilder));
      }
   }

   private class UserWorklistQueryBuilder implements IQueryBuilder
   {

      private ActivityInstances countQueryResult;

      private User user;

      public UserWorklistQueryBuilder(User user)
      {
         this.user = user;
      }

      public Query createQuery()
      {
         executeCountQuery();
         return countQueryResult.getQuery();
      }

      public ActivityInstances executeCountQuery()
      {
         countQueryResult = PPUtils.getActivityInstances_forUser(user.getOID());
         return countQueryResult;
      }

      public ActivityInstances getCountQueryResult()
      {
         return countQueryResult;
      }
   }
}
