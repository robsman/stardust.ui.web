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
package org.eclipse.stardust.ui.web.viewscommon.participantspanel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.faces.context.FacesContext;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.dto.UserDetails;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.core.query.statistics.api.UserLoginStatistics;
import org.eclipse.stardust.engine.core.query.statistics.api.UserLoginStatisticsQuery;
import org.eclipse.stardust.engine.core.query.statistics.api.UserLoginStatistics.LoginStatistics;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference;
import org.eclipse.stardust.ui.web.common.column.DefaultColumnModel;
import org.eclipse.stardust.ui.web.common.column.IColumnModel;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnAlignment;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnDataType;
import org.eclipse.stardust.ui.web.common.columnSelector.TableColumnSelectorPopup;
import org.eclipse.stardust.ui.web.common.filter.TableDataFilterPopup;
import org.eclipse.stardust.ui.web.common.filter.TableDataFilterSearch;
import org.eclipse.stardust.ui.web.common.table.SortableTable;
import org.eclipse.stardust.ui.web.common.table.SortableTableComparator;
import org.eclipse.stardust.ui.web.viewscommon.common.UIViewComponentBean;
import org.eclipse.stardust.ui.web.viewscommon.common.configuration.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.viewscommon.core.ResourcePaths;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.process.history.ActivityInstanceHistoryItem;
import org.eclipse.stardust.ui.web.viewscommon.process.history.EventHistoryItem;
import org.eclipse.stardust.ui.web.viewscommon.process.history.IProcessHistoryTableEntry;
import org.eclipse.stardust.ui.web.viewscommon.process.history.ProcessInstanceHistoryItem;
import org.eclipse.stardust.ui.web.viewscommon.utils.DefaultColumnModelEventHandler;



/**
 * @author subodh.godbole
 * 
 */
public class ParticipantsPanelBean extends UIViewComponentBean
{
   private static final Logger trace = LogManager.getLogger(ParticipantsPanelBean.class);
   private static final String BEAN_NAME = "common_participantsPanelBean";

   private boolean showTitle;

   private SortableTable<ParticipantsTableEntry> userTable;
   
   //If set This will be used directly instead of IProcessHistoryDataModel
   private IProcessHistoryTableEntry activityTreeRoot;
   // For case ProcessInstance , show participants for All PI's
   private List<IProcessHistoryTableEntry> caseTreeRootElements;

   /**
    * 
    */
   public ParticipantsPanelBean()
   {
      // ********* Create Table Structure **********
      MessagesViewsCommonBean propsBean = MessagesViewsCommonBean.getInstance();

      ColumnPreference colFirstName = new ColumnPreference("FirstName", "firstName",
            ColumnDataType.STRING, propsBean.getString("participantsTable.firstNameLabel"),
            new TableDataFilterPopup(new TableDataFilterSearch()));
      
      ColumnPreference colLastName = new ColumnPreference("LastName", "lastName",
            ColumnDataType.STRING, propsBean.getString("participantsTable.lastNameLabel"),
            new TableDataFilterPopup(new TableDataFilterSearch()));
         
      ColumnPreference colAccount = new ColumnPreference("Account", "account",
            ColumnDataType.STRING, propsBean.getString("participantsTable.accountLabel"));
      
      /*ColumnPreference colLastLogin = new ColumnPreference("LastLogin", "lastLoginTime",
            ColumnDataType.DATE, propsBean.getString("participantsTable.lastLoginLabel"),
            new TableDataFilterPopup(new TableDataFilterBetween(ITableDataFilter.DataType.DATE)));
      colLastLogin.setColumnConverterType(ColumnConverterType.DATE);*/

      ColumnPreference colStatus = new ColumnPreference("Icon", "online", 
            propsBean.getString("participantsTable.statusLabel"),
            ResourcePaths.VIEW_PARTICIPANTS_PANEL_COLUMNS, true, true);
      colStatus.setColumnAlignment(ColumnAlignment.CENTER);

      ColumnPreference colEmail = new ColumnPreference("Email", "email",
            ColumnDataType.STRING, propsBean.getString("participantsTable.emailLabel"));

      List<ColumnPreference> selectableCols = new ArrayList<ColumnPreference>();

//      if(false) // To Switch between Column Group Mode and Normal Mode
//      {
//         ColumnPreference colFullName = new ColumnPreference("FullName", "Full Name");
//         colFullName.addChildren(colFirstName);
//         colFullName.addChildren(colLastName);
//         
//         selectableCols.add(colFullName);
//      }
//      else
      {
         selectableCols.add(colFirstName);
         selectableCols.add(colLastName);
      }            

      selectableCols.add(colAccount);
      //selectableCols.add(colLastLogin);
      selectableCols.add(colEmail);

      List<ColumnPreference> fixedBeforeCols = new ArrayList<ColumnPreference>();
      fixedBeforeCols.add(colStatus);
      
      DefaultColumnModelEventHandler columnModelListener = new DefaultColumnModelEventHandler();
      IColumnModel columnModel = new DefaultColumnModel(selectableCols, fixedBeforeCols, null,
            UserPreferencesEntries.M_VIEWS_COMMON, UserPreferencesEntries.V_PARTICIPANTS, columnModelListener);
            
      TableColumnSelectorPopup colSelecpopup = new TableColumnSelectorPopup(columnModel);

      userTable = new SortableTable<ParticipantsTableEntry>(colSelecpopup,
            null, new SortableTableComparator<ParticipantsTableEntry>("firstName", true));
      
      columnModelListener.setNeedRefresh(false);
      userTable.initialize();
      columnModelListener.setNeedRefresh(true);
   }
   
   /**
    * @return
    */
   public static ParticipantsPanelBean getCurrent()

   {
      return (ParticipantsPanelBean) FacesContext.getCurrentInstance().getApplication()
            .getVariableResolver().resolveVariable(FacesContext.getCurrentInstance(),
            		BEAN_NAME);
   }

   @Override
   public void initialize()
   {
      trace.debug("Initializing ParticipantsPanelBean");
      if (getCurrentProcessInstance() != null)
      {
         List<ParticipantsTableEntry> userList = retreiveParticipants();
         
        /* if(false) // Add Dyna Attributes for Testing Dynamic Table
         {
            // Add Data
            int iRow = 0;
            ParticipantsTableEntry pte;
            Iterator<ParticipantsTableEntry> userIt = userList.iterator();
            while(userIt.hasNext())
            {
               pte = userIt.next();
               
               List<ParticipantsTableEntryDyna> dynaAttributes = new ArrayList<ParticipantsTableEntryDyna>();
               iRow++;
               dynaAttributes.add(new ParticipantsTableEntryDyna("IPP Dev", "Role" + iRow, "Mgr" + iRow));
               iRow++;
               dynaAttributes.add(new ParticipantsTableEntryDyna("IPP Enable", "Role" + iRow, "Mgr" + iRow));
               
               pte.setDynaAttributes(dynaAttributes);
            }
            
            // Add Columns as per Data
            List<ColumnPreference> selCols = userTable.getColumnModel().getSelectableColumns();
            
            ColumnPreference dynaCol = new ColumnPreference("IPPDev", "IPP Dev");
            dynaCol.addChildren(new ColumnPreference(
                  "IPPDevRole", "dynaAttributes[0].role", ColumnDataType.STRING, "Role", 
                  new TableDataFilterPopup(new TableDataFilterSearch())));
            dynaCol.addChildren(new ColumnPreference(
                  "IPPDevMgr", "dynaAttributes[0].manager", ColumnDataType.STRING, "Manager"));
            selCols.add(dynaCol);
   
            dynaCol = new ColumnPreference("IPPEnable", "IPP Enable");
            dynaCol.addChildren(new ColumnPreference(
                  "IPPEnableRole", "dynaAttributes[1].role", ColumnDataType.STRING, "Role",
                  new TableDataFilterPopup(new TableDataFilterSearch())));
            dynaCol.addChildren(new ColumnPreference(
                  "IPPEnableMgr", "dynaAttributes[1].manager", ColumnDataType.STRING, "Manager"));
            selCols.add(dynaCol);
            
            userTable.getColumnModel().setSelectableColumns(selCols);
         }*/
         
         userTable.setList(userList);         
      }
      else
      {
         trace.debug("Cannot initialize ParticipantsPanelBean");
      }
   }

   /**
    * @return
    */
   private List<ParticipantsTableEntry> retreiveParticipants()
   {
	   List<ParticipantsTableEntry> list = new ArrayList<ParticipantsTableEntry>();
	      List<User> users = getParticipantsForProcessInstance();

	      QueryService queryService = getQueryService();
	      UserLoginStatistics userLoginStatistics = (UserLoginStatistics) queryService
	            .getAllUsers(UserLoginStatisticsQuery.forAllUsers());
	      for (int n = 0; n < users.size(); ++n)
	      {
	         UserDetails user = (UserDetails) users.get(n);
	         LoginStatistics loginStatistics = userLoginStatistics.getLoginStatistics(user
	               .getOID());
	         if (loginStatistics != null)
	         {
	            list.add(new ParticipantsTableEntry(user.getFirstName(), user.getLastName(),
	                  user.getAccount(), loginStatistics.currentlyLoggedIn, new Date(),
	                  user.getEMail(), user.getValidFrom(), user.getValidTo()));
	         }
	         else
	         {
	            list.add(new ParticipantsTableEntry(user.getFirstName(), user.getLastName(),
	                  user.getAccount(), false, new Date()));
	         }
	      }

	      return list;
   }

   
   /*
    * Returns the all Participants for the ProcessInstance including Subprocess
    */
   private List<User> getParticipantsForProcessInstance()
   {
      if (getCurrentProcessInstance() == null)
      {
         return Collections.EMPTY_LIST;
      }
      if (CollectionUtils.isNotEmpty(caseTreeRootElements))
      {
         List<User> userList = CollectionUtils.newArrayList();
         for (IProcessHistoryTableEntry caseRoot : caseTreeRootElements)
         {
            for(User user:getListOfPerformers((ActivityInstanceHistoryItem) caseRoot))
            {
               if(!userList.contains(user))
               {
                  userList.add(user);
               }
            }
         }
         return userList;
      }
      else
      {
         return getListOfPerformers((ActivityInstanceHistoryItem) activityTreeRoot);
      }
   }

   /**
    * @param activityInstHistItem
    * @return
    */
   private List<User> getListOfPerformers(ActivityInstanceHistoryItem activityInstHistItem)
   {
      List<User> eventPerformers = new ArrayList<User>();
      EventHistoryItem eventHistoryItem;
      Set<Long> userOids = new HashSet<Long>();

      if (activityInstHistItem != null)
      {
         List<IProcessHistoryTableEntry> activities = activityInstHistItem.getChildren();
         for (IProcessHistoryTableEntry activityHistoryItem : activities)
         {
            if (activityHistoryItem instanceof ActivityInstanceHistoryItem)
            {
               // for activityEvent
               if (activityHistoryItem.getChildren() != null)
               {
                  List events = activityHistoryItem.getChildren();
                  for (Object eventHistoryItemObj : events)
                  {
                     if (eventHistoryItemObj instanceof EventHistoryItem)
                     {
                        eventHistoryItem = (EventHistoryItem) eventHistoryItemObj;
                        if ( !eventHistoryItem.isNodePathToActivityInstance())
                        {
                           User user = eventHistoryItem.getUser();
                           if (eventHistoryItem.getUser() != null
                                 && !userOids.contains(user.getOID()))
                           {
                              eventPerformers.add(user);
                              userOids.add(user.getOID());
                           }
                        }
                     }
                  }
               }
            }
         }
      }
      return eventPerformers;
   }
   
   // ************* DEFAULT GETTER SETTER METHODS **********************

   public SortableTable<ParticipantsTableEntry> getUserTable()
   {
      return userTable;
   }
   
   public boolean isShowTitle()
   {
      return showTitle;
   }

   public void setShowTitle(boolean showTitle)
   {
      this.showTitle = showTitle;
   }

   public void setActivityTreeRoot(IProcessHistoryTableEntry activityTreeRoot)
   {
      this.activityTreeRoot = activityTreeRoot;
   }

   public void setCaseTreeRootElements(List<IProcessHistoryTableEntry> caseTreeRootElements)
   {
      this.caseTreeRootElements = caseTreeRootElements;
   }
   
}
