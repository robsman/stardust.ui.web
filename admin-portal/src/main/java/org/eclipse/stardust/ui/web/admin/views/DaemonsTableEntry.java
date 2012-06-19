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

import java.util.Date;

import org.eclipse.stardust.engine.api.runtime.AcknowledgementState;
import org.eclipse.stardust.engine.api.runtime.Daemon;
import org.eclipse.stardust.engine.api.runtime.DaemonExecutionState;
import org.eclipse.stardust.ui.web.admin.messages.AdminMessagesPropertiesBean;
import org.eclipse.stardust.ui.web.common.table.DefaultRowModel;



/**
 * @author ankita.patel
 * @version $Revision: $
 */
public class DaemonsTableEntry extends DefaultRowModel
{
   private static final String ACK_STATE="views.daemons.ackState.";
   private static final String DAEMON_EXEC_STATE="views.daemons.daemonExecState.";
   private Daemon daemon;

   private String type;

   private Date startTime;

   private Date lastExecutionTime;

   private boolean running;

   private String acknowledgementState;

   private String daemonExecutionState;
   
   private String statusLabel;

   /**
    * @param daemon
    * @param type
    * @param startTime
    * @param lastExecutionTime
    * @param running
    * @param acknowledgementState
    * @param daemonExecutionState
    */
   public DaemonsTableEntry(Daemon daemon, String type, Date startTime,
         Date lastExecutionTime, boolean running, AcknowledgementState acknowledgementState,
         DaemonExecutionState daemonExecutionState)
   {
      super();
      this.daemon = daemon;
      this.type = type;
      this.startTime = startTime;
      this.lastExecutionTime = lastExecutionTime;
      this.running = running;
      AdminMessagesPropertiesBean messageBean = AdminMessagesPropertiesBean.getInstance();
      this.statusLabel = this.running ? messageBean.getString("views.daemons.status.column.running") : messageBean
            .getString("views.daemons.status.column.stopped");
      this.acknowledgementState = messageBean.getString(ACK_STATE + acknowledgementState.getValue());
      this.daemonExecutionState = messageBean.getString(DAEMON_EXEC_STATE + daemonExecutionState.getValue());
   }

   /**
    * 
    */
   public DaemonsTableEntry()
   {
   // TODO Auto-generated constructor stub
   }

   public Daemon getDaemon()
   {
      return daemon;
   }

   public String getType()
   {
      return type;
   }

   public Date getStartTime()
   {
      return startTime;
   }

   public Date getLastExecutionTime()
   {
      return lastExecutionTime;
   }

   public boolean isRunning()
   {
      return running;
   }

   public String getAcknowledgementState()
   {
      return acknowledgementState;
   }

   public String getDaemonExecutionState()
   {
      return daemonExecutionState;
   }

   public String getStatusLabel()
   {
      return statusLabel;
   }
   
}
