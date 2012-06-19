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
package org.eclipse.stardust.ui.web.admin.views;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.event.ActionEvent;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.dto.LogEntryDetails;
import org.eclipse.stardust.engine.api.query.LogEntryQuery;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.engine.api.query.QueryResult;
import org.eclipse.stardust.engine.api.runtime.LogEntry;
import org.eclipse.stardust.ui.web.admin.AdminportalConstants;
import org.eclipse.stardust.ui.web.admin.ResourcePaths;
import org.eclipse.stardust.ui.web.admin.WorkflowFacade;
import org.eclipse.stardust.ui.web.admin.common.configuration.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.admin.messages.AdminMessagesPropertiesBean;
import org.eclipse.stardust.ui.web.common.PopupUIComponentBean;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference;
import org.eclipse.stardust.ui.web.common.column.DefaultColumnModel;
import org.eclipse.stardust.ui.web.common.column.IColumnModel;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnDataType;
import org.eclipse.stardust.ui.web.common.columnSelector.TableColumnSelectorPopup;
import org.eclipse.stardust.ui.web.common.event.ViewEvent;
import org.eclipse.stardust.ui.web.common.event.ViewEventHandler;
import org.eclipse.stardust.ui.web.common.event.ViewEvent.ViewEventType;
import org.eclipse.stardust.ui.web.common.table.DataTableSortModel;
import org.eclipse.stardust.ui.web.common.table.IUserObjectBuilder;
import org.eclipse.stardust.ui.web.common.table.PaginatorDataTable;
import org.eclipse.stardust.ui.web.common.table.SortCriterion;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.common.PortalException;
import org.eclipse.stardust.ui.web.viewscommon.common.table.IppSearchHandler;
import org.eclipse.stardust.ui.web.viewscommon.common.table.IppSortHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.UserUtils;



/**
 * @author Ankita.Patel
 * @version $Revision: $
 */
public class OverviewBean extends PopupUIComponentBean
      implements IUserObjectBuilder<LogTableEntry> , ViewEventHandler
{
   private static final long serialVersionUID = 1L;
   private static final Logger trace = LogManager.getLogger(OverviewBean.class);

   private WorkflowFacade workflowFacade;

   private AdminMessagesPropertiesBean propsBean;

   private PaginatorDataTable<LogTableEntry, LogEntry> logEntriesTable;

   private LogTableEntry log;   


   /**
    * 
    */
   public OverviewBean()
   {
      super(ResourcePaths.V_overview);
      
   }

   @Override
   public void initialize()
   {
      ColumnPreference timeStampCol = new ColumnPreference("TimeStamp", "timeStamp",
            ColumnDataType.DATE, this.getMessages()
                  .getString("logTable.column.timeStamp"), true, true);
      timeStampCol.setNoWrap(true);

      ColumnPreference typeCol = new ColumnPreference("Type", "type",
            ColumnDataType.STRING, this.getMessages().getString("logTable.column.type"),
            true, false);

      ColumnPreference codeCol = new ColumnPreference("Code", "code",
            ColumnDataType.STRING, this.getMessages().getString("logTable.column.code"),
            true, false);

      ColumnPreference contextCol = new ColumnPreference("Context", "context",
            ColumnDataType.STRING, this.getMessages()
                  .getString("logTable.column.context"), true, false);

      ColumnPreference subjectCol = new ColumnPreference("Subject", "toolTipSubject", this
            .getMessages().getString("logTable.column.subject"),
            ResourcePaths.V_OVERVIEWCOLUMNS_VIEW, true, false);

      ColumnPreference accountCol = new ColumnPreference("Account", "user", this
            .getMessages().getString("logTable.column.account"),
            ResourcePaths.V_OVERVIEWCOLUMNS_VIEW, true, false);

      List<ColumnPreference> overviewCols = new ArrayList<ColumnPreference>();
      overviewCols.add(timeStampCol);
      overviewCols.add(typeCol);
      overviewCols.add(codeCol);
      overviewCols.add(contextCol);
      overviewCols.add(subjectCol);
      overviewCols.add(accountCol);

      IColumnModel overviewColumnModel = new DefaultColumnModel(overviewCols, null, null,
            UserPreferencesEntries.M_ADMIN, UserPreferencesEntries.V_OVERVIEW);
      TableColumnSelectorPopup colSelecPopup = new TableColumnSelectorPopup(
            overviewColumnModel);

      logEntriesTable = new PaginatorDataTable<LogTableEntry, LogEntry>(colSelecPopup,
            new OverviewTableSearchHandler(), null, new OverviewTableSortHandler(), this,
            new DataTableSortModel<LogTableEntry>("timeStamp", false));
      logEntriesTable.initialize();
      logEntriesTable.refresh(true);
   }

   public void handleEvent(ViewEvent event)
   {
       if (ViewEventType.CREATED == event.getType())
       {         
           workflowFacade = (WorkflowFacade) SessionContext.findSessionContext().lookup(
                 AdminportalConstants.WORKFLOW_FACADE);
           propsBean = AdminMessagesPropertiesBean.getInstance();

           initialize();
       }
   }
   
   /**
    * Updates the changes
    */
   public void update()
   {
      initialize();
      workflowFacade.reset();
     
   }

   /**
    * gets selected row data and opens inspectLogEntry popup
    * 
    * @param ae
    */
   public void openLogEntryDialog(ActionEvent ae)
   {
      UIComponent source = ae.getComponent();
      Object obj = source.getAttributes().get("log");

      if (obj instanceof LogTableEntry)
      {
         this.log = (LogTableEntry) obj;
      }
      super.openPopup();
   }

   /**
    * Creates LogTableEntry User object
    */
   public LogTableEntry createUserObject(Object resultRow)
   {
      if (resultRow instanceof LogEntryDetails)
      {
         try
         {
            LogEntryDetails logEntry = (LogEntryDetails) resultRow;
            String accountName = UserUtils.getUserDisplayLabel(logEntry.getUser());
            if (StringUtils.isNotEmpty(accountName))
            {
               int charIndex = accountName.indexOf(":");
               accountName = accountName.substring(charIndex + 1, accountName.length());
            }
    
   
               return new LogTableEntry(logEntry.getTimeStamp(), logEntry.getType(), logEntry.getCode(), logEntry
                  .getContext(), logEntry.getSubject(), accountName, logEntry.getUserOID());
         }
         catch (Exception e)
         {
            trace.error(e);
            LogTableEntry logTableEntry = new LogTableEntry();
            logTableEntry.setCause(e);
            return logTableEntry;
         }

      }
      return null;
   }

   // ****************** Modified Getter methods************************
   public long getTotalProcessInstancesCount() throws PortalException
   {
      return workflowFacade.getTotalProcessInstancesCount();
   }

   public long getActiveProcessInstancesCount() throws PortalException
   {
      return workflowFacade.getActiveProcessInstancesCount();
   }

   public long getInterruptedProcessInstancesCount() throws PortalException
   {
      return workflowFacade.getInterruptedProcessInstancesCount();
   }

   public long getCompletedProcessInstancesCount() throws PortalException
   {
      return workflowFacade.getCompletedProcessInstancesCount();
   }

   public long getAbortedProcessInstancesCount() throws PortalException
   {
      return workflowFacade.getAbortedProcessInstancesCount();
   }

   public long getTotalActivityInstancesCount() throws PortalException
   {
      return workflowFacade.getTotalActivityInstancesCount();
   }

   public long getActiveActivityInstancesCount() throws PortalException
   {
      return workflowFacade.getActiveActivityInstancesCount();
   }

   public long getPendingActivityInstancesCount() throws PortalException
   {
      return workflowFacade.getPendingActivityInstancesCount();
   }

   public long getAbortedActivityInstancesCount() throws PortalException
   {
      return workflowFacade.getAbortedActivityInstancesCount();
   }

   public long getCompletedActivityInstancesCount() throws PortalException
   {
      return workflowFacade.getCompletedActivityInstancesCount();
   }

   public long getTotalUsersCount() throws PortalException
   {
      return workflowFacade.getTotalUsersCount();
   }

   public long getActiveUsersCount() throws PortalException
   {
      return workflowFacade.getActiveUsersCount();
   }

   // *************** Default Getter & Setter Methods ****************************
   public PaginatorDataTable<LogTableEntry, LogEntry> getLogEntriesTable()
   {
      return logEntriesTable;
   }

   public void setLogEntriesTable(PaginatorDataTable<LogTableEntry, LogEntry> logEntriesTable)
   {
      this.logEntriesTable = logEntriesTable;
   }

   public LogTableEntry getLog()
   {
      return log;
   }

   public void setLog(LogTableEntry log)
   {
      this.log = log;
   }

   /**
    * @author Subodh.Godbole
    *
    */
   public class OverviewTableSearchHandler extends IppSearchHandler<LogEntry>
   {
      private static final long serialVersionUID = 1L;

      @Override
      public Query createQuery()
      {
         LogEntryQuery query = new LogEntryQuery();
         // setFilter(query);
         return query;
      }

      @Override
      public QueryResult<LogEntry> performSearch(Query query)
      {
         try
         {
            return workflowFacade.getAllLogEntries((LogEntryQuery) query);
         }
         catch (Exception e)
         {
            // Method is invoked in the render response phase.
            // As a result no messages would be inserted at the moment.
            ExceptionHandler.handleException(ExceptionHandler.CLIENT_ID_NONE, e);
         }
         return null;
      }
   }
   
   
   
   /**
    * @author Subodh.Godbole
    *
    */
   public class OverviewTableSortHandler extends IppSortHandler
   {
      private static final long serialVersionUID = 1L;

      @Override
      public void applySorting(Query query, List<SortCriterion> sortCriteriaList)
      {
         Iterator< ? > iterator = sortCriteriaList.iterator();

         // As per current Architecture, this list will hold only one item
         if (iterator.hasNext())
         {
            SortCriterion sortCriterion = (SortCriterion) iterator.next();
            if ("timeStamp".equals(sortCriterion.getProperty()))
            {
               query.orderBy(LogEntryQuery.STAMP, sortCriterion.isAscending());
            }
         }
      }
   }
}
