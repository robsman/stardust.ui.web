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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.stardust.engine.api.dto.UserDetailsLevel;
import org.eclipse.stardust.engine.api.model.ModelParticipant;
import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.engine.api.model.Participant;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.engine.api.query.UserDetailsPolicy;
import org.eclipse.stardust.engine.api.query.UserQuery;
import org.eclipse.stardust.engine.api.query.Users;
import org.eclipse.stardust.engine.api.runtime.Department;
import org.eclipse.stardust.engine.api.runtime.Grant;
import org.eclipse.stardust.engine.api.runtime.ProcessInstancePriority;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.core.query.statistics.api.CriticalExecutionTimePolicy;
import org.eclipse.stardust.engine.core.query.statistics.api.PostponedActivitiesStatistics;
import org.eclipse.stardust.engine.core.query.statistics.api.PostponedActivitiesStatisticsQuery;
import org.eclipse.stardust.engine.core.query.statistics.api.PostponedActivitiesStatistics.PostponedActivities;
import org.eclipse.stardust.ui.web.bcc.ResourcePaths;
import org.eclipse.stardust.ui.web.bcc.WorkflowFacade;
import org.eclipse.stardust.ui.web.bcc.common.configuration.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.bcc.jsf.IQueryExtender;
import org.eclipse.stardust.ui.web.bcc.jsf.PostponedActivitiesCalculator;
import org.eclipse.stardust.ui.web.bcc.jsf.UserItem;
import org.eclipse.stardust.ui.web.bcc.messsages.MessagesBCCBean;
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
import org.eclipse.stardust.ui.web.viewscommon.common.Constants;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.common.ModelHelper;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;



public class PostponedActivitiesBean extends UIComponentBean implements ResourcePaths,ViewEventHandler
{
   private static final long serialVersionUID = 1L;
   
   private final static String QUERY_EXTENDER = "carnotBcPostponedActivities/queryExtender";

   private static final int COLUMN_SIZE = 5;

   private MessagesBCCBean propsBean;

   private PostponedActivitiesStatistics pStat;

   private IQueryExtender queryExtender;

   private SessionContext sessionCtx;

   Set<ModelParticipantInfo> participantList;

   private WorkflowFacade facade;

   private SortableTable<PostponedActivitiesTableEntry> postponedActTable;

   private List<PostponedActivitiesTableEntry> postponedActList;
   

   /**
    * 
    */
   public PostponedActivitiesBean()
   {
      super(V_postponedActivitiesView);
      
   }

   public void handleEvent(ViewEvent event)
   {
      if (ViewEventType.CREATED == event.getType())
      {
         sessionCtx = SessionContext.findSessionContext();
         queryExtender = getQueryExtender();
         facade = WorkflowFacade.getWorkflowFacade();

         propsBean = MessagesBCCBean.getInstance();

         initialize();

      }
   }
   
   /**
    * Refresh the table
    */
   public void update()
   {
      initialize();
   }

   @Override
   public void initialize()
   {
      Query query = createQuery();

      ModelCache modelCache = ModelCache.findModelCache();

      facade = WorkflowFacade.getWorkflowFacade();
      participantList = new HashSet<ModelParticipantInfo>();
      if (query.getOrderCriteria().getCriteria().size() == 0)
      {
         query.orderBy(UserQuery.LAST_NAME).and(UserQuery.FIRST_NAME).and(
               UserQuery.ACCOUNT);
      }

      User user = facade.getUser();
      List<Grant> userGrants = user.getAllGrants();

      Participant participant;
      ModelParticipant modelParticipant;
      Department department;
      ModelParticipantInfo modelParticipantInfo;
      for (Grant grant : userGrants)
      {
         participant = facade.getParticipant(grant.getQualifiedId());
         if (participant instanceof ModelParticipant)
         {
            modelParticipant = (ModelParticipant) participant;
            department = grant.getDepartment();
            modelParticipantInfo = (department == null) ? modelParticipant : department.getScopedParticipant(modelParticipant);    
            participantList.add(modelParticipantInfo);
         }
      }

      long totalCount, exceededDurationCount;
      String avgDuration;
      postponedActList = new ArrayList<PostponedActivitiesTableEntry>();
      List<ParticipantsTableEntry> pte;

      Users users = facade.getAllUsers((UserQuery) query);
      List<UserItem> userItems = facade.getAllUsersAsUserItems(users);
      
      for (UserItem userItem : userItems)
      {
         user = userItem.getUser();
         PostponedActivities pActivities = pStat != null ? pStat
               .getPostponedActivities(userItem.getUser().getOID()) : null;

         Collection<PostponedActivities> list = pStat.getPostponedActivities();
         for (PostponedActivities postponedActivities : list)
         {
            if (userItem.getUser().getOID() == postponedActivities.userOid)
            {
               pActivities = postponedActivities;
            }
         }

         pte = new ArrayList<ParticipantsTableEntry>();
         if (pActivities != null)
         {
            PostponedActivitiesCalculator calc = new PostponedActivitiesCalculator(pActivities);
            for (ModelParticipantInfo mp : participantList)
            {
               if (calc != null)
               {
                  if(calc.getTotalCount(mp) != null && calc.getExceededDurationCount(mp) != null)
                  {
                     totalCount = calc.getTotalCount(mp);
                     avgDuration = calc.getAvgDuration(mp);
                     exceededDurationCount = calc.getExceededDurationCount(mp);

                     pte.add(new ParticipantsTableEntry(mp, totalCount, avgDuration, exceededDurationCount));
                  }
                  else
                  {
                     pte.add(new ParticipantsTableEntry(mp, 0, null, 0));
                  }
               }
            }
         }
         else
         {
            for (ModelParticipantInfo mp : participantList)
            {
               totalCount = 0;
               avgDuration = "";
               exceededDurationCount = 0;
               pte.add(new ParticipantsTableEntry(mp, totalCount, avgDuration, exceededDurationCount));
            }

         }
         postponedActList.add(new PostponedActivitiesTableEntry(userItem, userItem
               .getUser().getId(), Long.toString(userItem.getUser().getOID()), pte));
      }

      List<ColumnPreference> fixedCols = new ArrayList<ColumnPreference>();

      ColumnPreference nameCol = new ColumnPreference("TeamMember", "name", this
            .getMessages().getString("column.teamMember"), V_postponedActivitiesColumns,
            new TableDataFilterPopup(new TableDataFilterSearch()), true, true);
      fixedCols.add(nameCol);

      List<ColumnPreference> selectableCols = new ArrayList<ColumnPreference>();
      if (postponedActList.size() > 0)
      {
         List<ParticipantsTableEntry> sampleParticipantList = postponedActList.get(0).getParticipantList();
         int i = 0;
         boolean visible = true;

         for (ParticipantsTableEntry pt : sampleParticipantList)
         {
            if (i >= COLUMN_SIZE)
            {
               visible = false;
            }
            ColumnPreference participantCol = new ColumnPreference("p" + i, ModelHelper
                  .getParticipantName(pt.getModelParticipantInfo()));

            visible = i >= COLUMN_SIZE ? false : true;
            participantCol.setVisible(visible);

            ColumnPreference totalCountCol = new ColumnPreference("Total Count" + i,
                  "participantList[" + i + "].totalCount", ColumnDataType.NUMBER, this
                        .getMessages().getString("column.totalCount"));
            totalCountCol.setColumnAlignment(ColumnAlignment.CENTER);
            participantCol.addChildren(totalCountCol);

            ColumnPreference durationCol = new ColumnPreference("Duration" + i,
                  "participantList[" + i + "].avgDuration", ColumnDataType.STRING,
                  propsBean.getString("views.postponedActivities.column.duration"));
            durationCol.setColumnAlignment(ColumnAlignment.CENTER);
            durationCol.setNoWrap(true);
            participantCol.addChildren(durationCol);

            ColumnPreference durationExceedCol = new ColumnPreference("DurationExceed"
                  + i, "participantList[" + i + "].exceededDurationCount",
                  ColumnDataType.NUMBER, propsBean
                        .getString("views.postponedActivities.column.durationExceed"));
            durationExceedCol.setColumnAlignment(ColumnAlignment.CENTER);
            participantCol.addChildren(durationExceedCol);
            selectableCols.add(participantCol);
            i++;
         }
      }
      IColumnModel columnModel = new DefaultColumnModel(selectableCols, fixedCols, null,
            UserPreferencesEntries.M_BCC, UserPreferencesEntries.V_POSTPONED_ACTIVITIES);
      TableColumnSelectorPopup colSelecpopup = new TableColumnSelectorPopup(columnModel);

      postponedActTable = new SortableTable<PostponedActivitiesTableEntry>(colSelecpopup,
            null,
            new SortableTableComparator<PostponedActivitiesTableEntry>("name", true));
      postponedActTable.setList(postponedActList);
      postponedActTable.initialize();
   }

   /**
    * Returns query which gives users with their statistics
    * 
    * @return query
    */
   private Query createQuery()
   {
      WorkflowFacade facade = WorkflowFacade.getWorkflowFacade();
      PostponedActivitiesStatisticsQuery query = PostponedActivitiesStatisticsQuery
            .forAllUsers();
      query.setPolicy(new CriticalExecutionTimePolicy(Constants
            .getCriticalDurationThreshold(ProcessInstancePriority.LOW, 1.0f), Constants
            .getCriticalDurationThreshold(ProcessInstancePriority.NORMAL, 1.0f),
            Constants.getCriticalDurationThreshold(ProcessInstancePriority.HIGH, 1.0f)));
      pStat = (PostponedActivitiesStatistics) facade.getAllUsers(query);
      UserQuery uQuery = WorkflowFacade.getWorkflowFacade().getTeamQuery(true);
      uQuery.setPolicy(new UserDetailsPolicy(UserDetailsLevel.Core));
      if (queryExtender != null)
      {
         queryExtender.extendQuery(uQuery);
      }
      return uQuery;
   }

   // ********** Modified getter Method **********
   public IQueryExtender getQueryExtender()
   {
      if (queryExtender == null)
      {
         return (IQueryExtender) sessionCtx.lookup(QUERY_EXTENDER);
      }
      return queryExtender;
   }

   // ************ Default Getter setter method *********
   public List<PostponedActivitiesTableEntry> getPostponedActList()
   {
      return postponedActList;
   }

   public SortableTable<PostponedActivitiesTableEntry> getPostponedActTable()
   {
      return postponedActTable;
   }
}
