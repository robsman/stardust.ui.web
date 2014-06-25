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

import org.eclipse.stardust.engine.api.dto.DaemonDetails;
import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.engine.api.runtime.Daemon;
import org.eclipse.stardust.ui.web.admin.AdminportalConstants;
import org.eclipse.stardust.ui.web.admin.ResourcePaths;
import org.eclipse.stardust.ui.web.admin.WorkflowFacade;
import org.eclipse.stardust.ui.web.admin.common.configuration.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.admin.messages.AdminMessagesPropertiesBean;
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
import org.eclipse.stardust.ui.web.common.table.SortableTable;
import org.eclipse.stardust.ui.web.common.table.SortableTableComparator;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.common.PortalException;
import org.eclipse.stardust.ui.web.viewscommon.utils.AuthorizationUtils;



/**
 * @author ankita.patel
 * @version $Revision: $
 */
public class DaemonsBean extends UIComponentBean implements  ViewEventHandler
{
   private static final long serialVersionUID = 1L;

   private static final String MAIL_TRIGGER = "mail.trigger";

   private static final String TIMER_TRIGGER = "timer.trigger";
   
   private static final String REPORTING_DAEMON = "reporting.daemon";

   private AdminMessagesPropertiesBean propsBean;

   private WorkflowFacade workflowFacade;

   private boolean authorizedForDaemonAction;

   private List<DaemonsTableEntry> daemonsList;

   private SortableTable<DaemonsTableEntry> daemonsTable;
   


   /**
    * 
    */
   public DaemonsBean()
   {
      super(ResourcePaths.V_daemons);    
   }

   private void createTable(){

      propsBean = AdminMessagesPropertiesBean.getInstance();

      authorizedForDaemonAction = AuthorizationUtils.canManageDaemons();

      ColumnPreference typeCol = new ColumnPreference("Type", "type",
            ColumnDataType.STRING, this.getMessages().getString("column.type"), true,
            true);

      ColumnPreference startedCol = new ColumnPreference("Started", "startTime",
            ColumnDataType.DATE, this.getMessages().getString("column.started"), true,
            false);
      startedCol.setNoWrap(true);

      ColumnPreference lastTimeCol = new ColumnPreference("LastExecutionTime",
            "lastExecutionTime", ColumnDataType.DATE, this.getMessages().getString(
                  "column.lastTime"), true, false);
      lastTimeCol.setNoWrap(true);

      ColumnPreference statusCol = new ColumnPreference("Status", "statusLabel", this.getMessages()
            .getString("column.status"), ResourcePaths.V_DAEMONCOLUMNS_VIEW, true, false);

      ColumnPreference acknowledgementStateCol = new ColumnPreference(
            "AcknowledgementState", "acknowledgementState", ColumnDataType.STRING, this
                  .getMessages().getString("column.acknowledgeState"), true, false);

      ColumnPreference daemonExecutionStateCol = new ColumnPreference(
            "DaemonExecutionState", "daemonExecutionState", ColumnDataType.STRING, this
                  .getMessages().getString("column.executionState"), true, false);

      List<ColumnPreference> daemonFixedCols = new ArrayList<ColumnPreference>();
      ColumnPreference fixedCol = new ColumnPreference("Actions", "", propsBean
            .getString("views.common.column.actions"),
            ResourcePaths.V_DAEMONCOLUMNS_VIEW, true, false);
      fixedCol.setColumnAlignment(ColumnAlignment.CENTER);
      fixedCol.setExportable(false);
      daemonFixedCols.add(fixedCol);

      List<ColumnPreference> cols = new ArrayList<ColumnPreference>();
      cols.add(typeCol);
      cols.add(startedCol);
      cols.add(lastTimeCol);
      cols.add(statusCol);
      cols.add(acknowledgementStateCol);
      cols.add(daemonExecutionStateCol);

      IColumnModel daemonsColumnModel = new DefaultColumnModel(cols, null,
            daemonFixedCols, UserPreferencesEntries.M_ADMIN, UserPreferencesEntries.V_DAEMONS);
      TableColumnSelectorPopup colSelecPopup = new TableColumnSelectorPopup(
            daemonsColumnModel);
      daemonsTable = new SortableTable<DaemonsTableEntry>(null, colSelecPopup, null,
            new SortableTableComparator<DaemonsTableEntry>("type", true));
      daemonsTable.initialize();
      
   }
   @Override
   public void initialize()
   {
      daemonsList = createDaemons();
      daemonsTable.setList(daemonsList);
      daemonsTable.initialize();
   }
   
   public void handleEvent(ViewEvent event)
   {
       if (ViewEventType.CREATED == event.getType())
       {
           workflowFacade = (WorkflowFacade) SessionContext.findSessionContext().lookup(
                 AdminportalConstants.WORKFLOW_FACADE);
           createTable();
           initialize();
           
     
       }
   }

   /**
    * starts selected daemon
    * 
    * @param ae
    * @throws PortalException
    */
   public void startDaemon(ActionEvent ae) throws PortalException
   {
      UIComponent source = ae.getComponent();
      Object obj = source.getAttributes().get("daemon");

      if (obj instanceof Daemon)
      {

         AdministrationService service = workflowFacade.getServiceFactory()
               .getAdministrationService();
         Daemon daemon = (Daemon) obj;
         if (daemon != null)
         {
            service.startDaemon(daemon.getType(), true);
            initialize();
         }

      }
   }

   /**
    * stops selected daemon
    * 
    * @param ae
    * @throws PortalException
    */
   public void stopDaemon(ActionEvent ae) throws PortalException
   {
      UIComponent source = ae.getComponent();
      Object obj = source.getAttributes().get("daemon");

      if (obj instanceof Daemon)
      {
         AdministrationService service = workflowFacade.getServiceFactory()
               .getAdministrationService();
         Daemon daemon = (Daemon) obj;
         if (daemon != null)
         {
            service.stopDaemon(daemon.getType(), true);
            initialize();
         }

      }
   }

   /**
    * creates daemons
    * 
    * @return daemonList
    */
   private List<DaemonsTableEntry> createDaemons()
   {
      daemonsList = new ArrayList<DaemonsTableEntry>();
      List<DaemonDetails> daemons = workflowFacade.getAllDaemons();
      for (Iterator<DaemonDetails> iterator = daemons.iterator(); iterator.hasNext();)
      {
         Object object = (Object) iterator.next();
         if (object instanceof DaemonDetails)
         {
            DaemonDetails dd = (DaemonDetails) object;
            String type = null;
            if (AdministrationService.EVENT_DAEMON.equals(dd.getType()))
               type = propsBean.getString("views.daemons.eventDaemon.label");
            else if (MAIL_TRIGGER.equals(dd.getType()))
               type = propsBean.getString("views.daemons.mailDaemon.label");
            else if (TIMER_TRIGGER.equals(dd.getType()))
               type = propsBean.getString("views.daemons.timeDaemon.label");
            else if (AdministrationService.SYSTEM_DAEMON.equals(dd.getType()))
               type = propsBean.getString("views.daemons.systemDaemon.label");
            else if (AdministrationService.CRITICALITY_DAEMON.equals(dd.getType()))
               type = propsBean.getString("views.daemons.criticalityDaemon.label");
            else if (REPORTING_DAEMON.equals(dd.getType()))
               type = propsBean.getString("views.daemons.reportingDaemon.label");
            else 
               type = dd.getType();

            daemonsList.add(new DaemonsTableEntry(dd, type, dd.getStartTime(), dd
                  .getLastExecutionTime(), dd.isRunning(), dd.getAcknowledgementState(), dd.getDaemonExecutionState()));
         }

      }
      return daemonsList;
   }


   // *************** Default Getter & Setter Methods *******************
   public boolean isAuthorizedForDaemonAction()
   {
      return authorizedForDaemonAction;
   }

   public SortableTable<DaemonsTableEntry> getDaemonsTable()
   {
      return daemonsTable;
   }

   public void setDaemonsTable(SortableTable<DaemonsTableEntry> daemonsTable)
   {
      this.daemonsTable = daemonsTable;
   }

}
