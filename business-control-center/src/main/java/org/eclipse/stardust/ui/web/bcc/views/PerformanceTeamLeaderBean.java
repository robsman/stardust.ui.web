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
package org.eclipse.stardust.ui.web.bcc.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.dto.RoleDetails;
import org.eclipse.stardust.engine.api.dto.RoleInfoDetails;
import org.eclipse.stardust.engine.api.dto.UserDetailsLevel;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.ModelParticipant;
import org.eclipse.stardust.engine.api.model.Organization;
import org.eclipse.stardust.engine.api.model.Participant;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.model.QualifiedRoleInfo;
import org.eclipse.stardust.engine.api.model.Role;
import org.eclipse.stardust.engine.api.query.FilterTerm;
import org.eclipse.stardust.engine.api.query.ParticipantAssociationFilter;
import org.eclipse.stardust.engine.api.query.UserDetailsPolicy;
import org.eclipse.stardust.engine.api.query.UserQuery;
import org.eclipse.stardust.engine.api.query.Users;
import org.eclipse.stardust.engine.api.runtime.DeployedModel;
import org.eclipse.stardust.engine.api.runtime.Grant;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.core.query.statistics.api.UserPerformanceStatistics;
import org.eclipse.stardust.engine.core.query.statistics.api.UserPerformanceStatisticsQuery;
import org.eclipse.stardust.engine.core.query.statistics.api.UserPerformanceStatistics.Contribution;
import org.eclipse.stardust.engine.core.query.statistics.api.UserPerformanceStatistics.PerformanceStatistics;
import org.eclipse.stardust.ui.web.bcc.ResourcePaths;
import org.eclipse.stardust.ui.web.bcc.WorkflowFacade;
import org.eclipse.stardust.ui.web.bcc.common.configuration.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.bcc.jsf.IQueryExtender;
import org.eclipse.stardust.ui.web.bcc.jsf.UserItem;
import org.eclipse.stardust.ui.web.common.UIComponentBean;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference;
import org.eclipse.stardust.ui.web.common.column.DefaultColumnModel;
import org.eclipse.stardust.ui.web.common.column.IColumnModel;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnAlignment;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnDataType;
import org.eclipse.stardust.ui.web.common.columnSelector.TableColumnSelectorPopup;
import org.eclipse.stardust.ui.web.common.event.ViewEvent;
import org.eclipse.stardust.ui.web.common.event.ViewEventHandler;
import org.eclipse.stardust.ui.web.common.event.ViewEvent.ViewEventType;
import org.eclipse.stardust.ui.web.common.filter.TableDataFilterPopup;
import org.eclipse.stardust.ui.web.common.filter.TableDataFilterSearch;
import org.eclipse.stardust.ui.web.common.table.SortableTable;
import org.eclipse.stardust.ui.web.common.table.SortableTableComparator;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDefinitionUtils;



/**
 * @author Ankita.Patel
 * @version $Revision: $
 */
public class PerformanceTeamLeaderBean extends UIComponentBean implements ResourcePaths,ViewEventHandler
{
   private static final long serialVersionUID = 1L;
   private final static String QUERY_EXTENDER = "carnotBcPerformanceTeamleader/queryExtender";   

   private IQueryExtender queryExtender;

   private SessionContext sessionCtx;

   private WorkflowFacade facade;

   private Map teamMap;

   private SortableTable<TeamLeaderTableEntry> teamLeaderTable;

   private UserPerformanceStatistics userStatistics;

   protected static final int TODAY_COL_OFFSET = 0;

   protected static final int WEEK_COL_OFFSET = 1;

   protected static final int MONTH_COL_OFFSET = 2;
   
   private List<ProcessDefinition> processes;  

   private static final int DEFAULT_NUMBER_OF_SELECTED_COLUMNS = 5;
   
   
   /**
    * 
    */
   public PerformanceTeamLeaderBean()
   {
      super(V_performanceTeamleaderView);
      
   }

   @Override
   public void initialize()
   {
      processes = ProcessDefinitionUtils.getAllBusinessRelevantProcesses();
      
      teamMap = new HashMap();

      UserQuery query = UserQuery.findActive();
      facade = WorkflowFacade.getWorkflowFacade();
      FilterTerm filter = query.getFilter().addOrTerm();
      Iterator<QualifiedRoleInfo> iter = facade.getTeamleadRoles().iterator();
      ModelCache modelCache = ModelCache.findModelCache();
      while (iter.hasNext())
      {
         Role teamLeadRole = null;
         QualifiedRoleInfo teamLeadRoleInfo = iter.next();
         if (teamLeadRoleInfo instanceof RoleDetails)
         {
            RoleDetails role = (RoleDetails) teamLeadRoleInfo;
            DeployedModel model = modelCache.getModel(role.getModelOID());
            teamLeadRole = (Role) model.getParticipant(teamLeadRoleInfo.getId());

         }
         else if (teamLeadRoleInfo instanceof RoleInfoDetails)
         {
            RoleInfoDetails roleInfo = (RoleInfoDetails) teamLeadRoleInfo;

            for (DeployedModel model : modelCache.getAllModels())
            {
               String modelId = org.eclipse.stardust.ui.web.viewscommon.utils.ModelUtils.extractModelId(roleInfo
                     .getQualifiedId());
               if (model.getId().equals(modelId))
               {
                  teamLeadRole = (Role) model.getParticipant(roleInfo.getId());
                  if (teamLeadRole.getRuntimeElementOID() == roleInfo.getRuntimeElementOID())
                  {
                     break;
                  }
               }
            }
         }

         if (teamLeadRole != null)
         {
            filter.add(ParticipantAssociationFilter.forParticipant(teamLeadRoleInfo));
            Iterator teamIter = teamLeadRole.getTeams().iterator();
            while (teamIter.hasNext())
            {
               Participant team = (Participant) teamIter.next();
               List teams = (List) teamMap.get(teamLeadRole.getQualifiedId());
               if (teams == null)
               {
                  teams = new ArrayList();
                  teamMap.put(teamLeadRole.getQualifiedId(), teams);                 
               }
               teams.add(team);
            }
         }
      }
      query.setPolicy(new UserDetailsPolicy(UserDetailsLevel.Full));
      userStatistics = (UserPerformanceStatistics) facade.getAllUsers(UserPerformanceStatisticsQuery.forAllUsers());
      if (queryExtender != null)
      {
         queryExtender.extendQuery(query);
      }
      createTeamLeaderList(query);

   }

   private void createTable()
   {
      List<ColumnPreference> fixedCols = new ArrayList<ColumnPreference>();
      ColumnPreference nameCol = new ColumnPreference("TeamMember", "name", this.getMessages().getString(
            "column.teamMember"), V_performanceTeamLeaderColumns,
            new TableDataFilterPopup(new TableDataFilterSearch()), true, true);
      fixedCols.add(nameCol);

      List<ColumnPreference> selectableCols = new ArrayList<ColumnPreference>();

      IColumnModel columnModel = new DefaultColumnModel(selectableCols, fixedCols, null, UserPreferencesEntries.M_BCC,
            UserPreferencesEntries.V_PERFORMANCE_TEAM_LEADER);
      TableColumnSelectorPopup colSelecpopup = new TableColumnSelectorPopup(columnModel);

      teamLeaderTable = new SortableTable<TeamLeaderTableEntry>(colSelecpopup, null,
            new SortableTableComparator<TeamLeaderTableEntry>("name", true));
      teamLeaderTable.initialize();
   }

   public void handleEvent(ViewEvent event)
   {
      if (ViewEventType.CREATED == event.getType())
      {
         sessionCtx = SessionContext.findSessionContext();
         queryExtender = getQueryExtender();

         createTable();
         initialize();
      }
   }
   
   /**
    * Refresh the table
    */
   public void update()
   {
      createTable();
      initialize();
   }

   /**
    * As per Query creates the team leader roles and participants list
    * 
    * @param query
    */
   private void createTeamLeaderList(UserQuery query)
   {    
      List data = new ArrayList();//remove this variable
      Users teamleader = null;
      if (teamMap.size() > 0)
      {
         facade = WorkflowFacade.getWorkflowFacade();
         if (query.getOrderCriteria().getCriteria().size() == 0)
         {
            query.orderBy(UserQuery.LAST_NAME).and(UserQuery.FIRST_NAME).and(
                  UserQuery.ACCOUNT);
         }
         teamleader = facade.getAllUsers((UserQuery) query);
         List userItems = facade.getAllUsersAsUserItems(teamleader);
         Iterator uIter = userItems.iterator();
         Set teamleaders = CollectionUtils.newSet();
         ModelCache modelCache = ModelCache.findModelCache();
         while (uIter.hasNext())
         {
            UserItem userItem = (UserItem) uIter.next();
            User user = userItem.getUser();
            Iterator gIter = user.getAllGrants().iterator();
            while (gIter.hasNext())
            {
               Grant grant = (Grant) gIter.next();
               List teams = (List) teamMap.get(grant.getQualifiedId());
               Model model = modelCache.getActiveModel(grant);
               Participant p = model != null ? model.getParticipant(grant.getId()) : null;
               Role teamleaderRole = null;
               if (p instanceof Role)
               {
                  teamleaderRole = (Role) p;
               }
               if (teams != null && teamleaderRole != null)
               {
                  Iterator teamIter = teams.iterator();
                  while (teamIter.hasNext())
                  {
                     p = (Participant) teamIter.next();
                     if (p instanceof Organization)
                     {
                        teamleaders.add(new Teamleader(userItem, teamleaderRole,
                              (Organization) p));
                     }
                  }
               }
            }
         }
         Map tStatistics = getTeamStatistics(teamleaders);
         Iterator tlIter = tStatistics.entrySet().iterator();

         while (tlIter.hasNext())
         {
            Entry entry = (Entry) tlIter.next();
            Map objectData = (Map) entry.getValue();

            Iterator itr = objectData.keySet().iterator();
            while (itr.hasNext())
            {
               String pId = (String) itr.next();
               List<CompletedActivityStatistics> tempList = new ArrayList<CompletedActivityStatistics>();
               if (objectData.get(pId) instanceof CompletedActivityStatistics)
               {
                  CompletedActivityStatistics cas = (CompletedActivityStatistics) objectData
                        .get(pId);
                  tempList.add(new CompletedActivityStatistics(cas.getCountToday(), cas
                        .getCountWeek(), cas.getCountMonth(), pId));
               }

            }
            data.add(entry.getKey());

         }

      }
      

   }

   /**
    * Creates the list to be displayed for users, their statistics and process definitions
    * 
    * @param teamleaders
    * @return
    * 
    */
   //TODO: code cleanup /optimization
   private Map getTeamStatistics(Set teamleaders)
   {
      // The following map should have a predictable iteration order
      // so that the ordering of the teamleader list is not destroyed
      Map tStatistics = new LinkedHashMap();
      List<TeamLeaderTableEntry> teamLeaderList = new ArrayList<TeamLeaderTableEntry>();

      List<ColumnPreference> selCols = teamLeaderTable.getColumnModel()
            .getSelectableColumns();
      if (teamleaders != null)
      {
         // ****** DATA
        
         Iterator tlIter = teamleaders.iterator();
         while (tlIter.hasNext())
         {
            List<CompletedActivityStatisticsTableEntry> activityStatisticsList =null;
            Teamleader tl = (Teamleader) tlIter.next();
            Map pStatistics = new HashMap();
            tStatistics.put(tl, pStatistics);
            Iterator teamIter = getTeamMember(tl).iterator();   
          
            while (teamIter.hasNext())
            { 
               activityStatisticsList = new ArrayList<CompletedActivityStatisticsTableEntry>();   
               User user = (User) teamIter.next();
               setCompletedProcessStatisticsForUser(pStatistics, user,
                     activityStatisticsList);
            }
            
            if (CollectionUtils.isEmpty(activityStatisticsList))
            {
               activityStatisticsList = new ArrayList<CompletedActivityStatisticsTableEntry>();   
               for (ProcessDefinition process : processes)
               {
                  activityStatisticsList.add(new CompletedActivityStatisticsTableEntry(I18nUtils
                        .getProcessName(process), 0, 0, 0));
               }

            }
            teamLeaderList.add(new TeamLeaderTableEntry(tl, tl.getUser().getId(), Long.toString(tl.getUser().getOID()),
                  activityStatisticsList));
         }

         // ****** DEFINE COL MODEL AS PER DATA
         if (!teamLeaderList.isEmpty())
         {
            
            int i = 0;
            String propertyMapping = "";
            for (ProcessDefinition process : processes)
            {
               ColumnPreference dynaProcessCol = new ColumnPreference("PD" + i, I18nUtils.getProcessName(process));

               dynaProcessCol.setVisible((i < DEFAULT_NUMBER_OF_SELECTED_COLUMNS) ? true : false);

               propertyMapping = "statisticsList[" + i + "].countToday";
               ColumnPreference countTodayCol = new ColumnPreference("Today" + i,
                     propertyMapping, ColumnDataType.NUMBER, this.getMessages()
                           .getString("column.today"), null, true, false);
               countTodayCol.setColumnAlignment(ColumnAlignment.CENTER);
               dynaProcessCol.addChildren(countTodayCol);

               propertyMapping = "statisticsList[" + i + "].countWeek";
               ColumnPreference countWeekCol = new ColumnPreference("Week" + i,
                     propertyMapping, ColumnDataType.NUMBER, this.getMessages()
                           .getString("column.week"), null, true, false);
               countWeekCol.setColumnAlignment(ColumnAlignment.CENTER);
               dynaProcessCol.addChildren(countWeekCol);

               propertyMapping = "statisticsList[" + i + "].countMonth";
               ColumnPreference countMonthCol = new ColumnPreference("Month" + i,
                     propertyMapping, ColumnDataType.NUMBER, this.getMessages()
                           .getString("column.month"), null, true, false);
               countMonthCol.setColumnAlignment(ColumnAlignment.CENTER);
               dynaProcessCol.addChildren(countMonthCol);

               selCols.add(dynaProcessCol);
               i++;
            }
            teamLeaderTable.getColumnModel().setDefaultSelectableColumns(selCols);
         }
         teamLeaderTable.setList(teamLeaderList);
         teamLeaderTable.initialize();
      }
      return tStatistics;
   }

   /**
    * Calculates the complete activity statistics of User for different process
    * definitions it belongs
    * 
    * @param pStatistics
    * @param user
    * @param activityStatisticsList
    */
   private void setCompletedProcessStatisticsForUser(Map pStatistics, User user,
         List<CompletedActivityStatisticsTableEntry> activityStatisticsList)
   {
      if (userStatistics != null && processes!=null)
      {
        
         for (ProcessDefinition process:processes)
         {  
            PerformanceStatistics performanceStatistics = userStatistics
                  .getStatisticsForUserAndProcess(user.getOID(), process.getQualifiedId());

            CompletedActivityStatistics pStat = (CompletedActivityStatistics) pStatistics.get(process.getQualifiedId());
            if (pStat == null)
            {
               pStat = new CompletedActivityStatistics();
               pStatistics.put(process.getQualifiedId(), pStat);
            }
            
            if (performanceStatistics != null)
            {
               
               List contributions = performanceStatistics.contributions;
               for (int i = 0; i < contributions.size(); ++i)
               {
                  Contribution con = (Contribution) contributions.get(i);
                  pStat.countToday += con.performanceToday.nAisCompleted;
                  pStat.countWeek += con.performanceThisWeek.nAisCompleted;
                  pStat.countMonth += con.performanceThisMonth.nAisCompleted;
               }

            }            
            activityStatisticsList.add(new CompletedActivityStatisticsTableEntry(
                  I18nUtils.getProcessName(process), pStat.countToday, pStat.countWeek,
                  pStat.countMonth));

         }
      }
   }

   /**
    * As per the TeamLeader returns the uesrs within
    * 
    * @param tl
    * @return list of teamLeadersRole
    */
   private List getTeamMember(Teamleader tl)
   {
      UserQuery query = UserQuery.findAll();
      query.getFilter().add(
            ParticipantAssociationFilter.forTeamLeader(tl.getTeamleaderRole()));
      query.setPolicy(new UserDetailsPolicy(UserDetailsLevel.Core));
      WorkflowFacade facade = WorkflowFacade.getWorkflowFacade();
      return facade.getAllUsers(query);
   }

   /**
    * @return
    */
   public IQueryExtender getQueryExtender()
   {
      if (queryExtender == null)
      {
         return (IQueryExtender) sessionCtx.lookup(QUERY_EXTENDER);
      }
      return queryExtender;
   }

   /**
    * @return
    */
   public SortableTable<TeamLeaderTableEntry> getTeamLeaderTable()
   {
      return teamLeaderTable;
   }

   public static class CompletedActivityStatistics
   {
      public CompletedActivityStatistics()
      {
         super();
      }

      private long countToday;

      private long countWeek;

      private long countMonth;

      private String processDefinitionName;

      public CompletedActivityStatistics(long countToday, long countWeek,
            long countMonth, String processDefinitionName)
      {
         super();
         this.countToday = countToday;
         this.countWeek = countWeek;
         this.countMonth = countMonth;
         this.processDefinitionName = processDefinitionName;
      }

      public long getCountToday()
      {
         return countToday;
      }

      public void setCountToday(long countToday)
      {
         this.countToday = countToday;
      }

      public long getCountWeek()
      {
         return countWeek;
      }

      public void setCountWeek(long countWeek)
      {
         this.countWeek = countWeek;
      }

      public long getCountMonth()
      {
         return countMonth;
      }

      public void setCountMonth(long countMonth)
      {
         this.countMonth = countMonth;
      }

      public String getProcessDefinitionName()
      {
         return processDefinitionName;
      }

      public void setProcessDefinitionName(String processDefinitionName)
      {
         this.processDefinitionName = processDefinitionName;
      }
   }

   public static class Teamleader
   {
      private UserItem user;

      private ModelParticipant team;

      private Role teamleaderRole;

      private String teamName;

      private List<CompletedActivityStatistics> statisticsList;

      protected Teamleader(UserItem user, Role teamleaderRole, ModelParticipant team)
      {
         this.user = user;
         this.team = team;
         teamName = I18nUtils.getParticipantName(team);
         this.teamleaderRole = teamleaderRole;
      }

      public User getUser()
      {
         return user.getUser();
      }

      public UserItem getUserItem()
      {
         return user;
      }

      public String getTeamname()
      {
         return teamName;
      }

      public ModelParticipant getTeam()
      {
         return team;
      }

      public Role getTeamleaderRole()
      {
         return teamleaderRole;
      }

      public boolean equals(Object obj)
      {
         if (obj instanceof Teamleader)
         {
            Teamleader tl = (Teamleader) obj;
            return user.getUser().getOID() == tl.user.getUser().getOID()
                  && user.getUser().getRealmOID() == tl.user.getUser().getRealmOID()
                  && team.getRuntimeElementOID() == tl.team.getRuntimeElementOID();
         }
         return false;
      }

      public int hashCode()
      {
         return (int) user.getUser().getOID() | (int) user.getUser().getRealmOID()
               | (int) team.getRuntimeElementOID();
      }

      public List<CompletedActivityStatistics> getStatisticsList()
      {
         return statisticsList;
      }

      public void setStatisticsList(List<CompletedActivityStatistics> statisticsList)
      {
         this.statisticsList = statisticsList;
      }
   }

}
