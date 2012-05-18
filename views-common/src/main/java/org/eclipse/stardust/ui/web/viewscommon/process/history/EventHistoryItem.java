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
package org.eclipse.stardust.ui.web.viewscommon.process.history;

import java.util.Date;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.dto.DepartmentDetails;
import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.engine.api.model.Participant;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.DepartmentInfo;
import org.eclipse.stardust.engine.api.runtime.HistoricalEvent;
import org.eclipse.stardust.engine.api.runtime.HistoricalEventDescriptionDelegation;
import org.eclipse.stardust.engine.api.runtime.HistoricalEventDescriptionStateChange;
import org.eclipse.stardust.engine.api.runtime.HistoricalEventType;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.ui.web.viewscommon.common.Localizer;
import org.eclipse.stardust.ui.web.viewscommon.common.LocalizerKey;
import org.eclipse.stardust.ui.web.viewscommon.common.ModelHelper;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;


public class EventHistoryItem extends AbstractProcessHistoryTableEntry
{
   /*
    * The following fields set the type of the history event item It is mapped as an icon
    * identifier in the GUI e.g "delegate" is mapped to .AFProcessHistoryDelegateIcon in
    * the skin definition file (css).
    */
   public static final String DELEGATION_TYPE = "Delegate";
   public static final String EXCEPTION_TYPE = "Exception";
   public static final String NOTE_TYPE = "Note";
   public static final String RESUBMISSION_TYPE = "Resubmission";
   public static final String ACTIVITY_ACTIVE_TYPE = "ActivityActive";
   public static final String SUSPENDED_TYPE = "ActivitySuspended";
   public static final String ABORTED_TYPE = "ActivityAborted";
   public static final String ABORTING_TYPE = "AbortingActivity";
   public static final String INTERRUPTED_TYPE = "ActivityInterrupted";
   public static final String COMPLETED_TYPE = "ActivityCompleted";

   private String type;
   private String name;
   private String detail;
   private String fullDetail;
   private Date eventTime;
   private String performer;
   private HistoricalEventType eventType;
   private User user;

   /**
    * @param event
    */
   public EventHistoryItem(HistoricalEvent event)
   {
      super(null, null);
      this.eventType = event.getEventType();
      switch (eventType.getValue())
      {
      case HistoricalEventType.DELEGATION:
         type = DELEGATION_TYPE;
         name = Localizer.getString(LocalizerKey.PH_DELEGATION_TYPE);
         HistoricalEventDescriptionDelegation delDescr = (HistoricalEventDescriptionDelegation) event.getDetails();
         Participant from = delDescr.getFromPerformer();
         Participant to = delDescr.getToPerformer();
         StringBuffer buffer = new StringBuffer();
         if (from != null)
         {
            buffer.append(ModelHelper.getParticipantName(from)).append(" ");
         }
         if (to != null)
         {
            buffer.append("-> ").append(ModelHelper.getParticipantName(to));
         }
         fullDetail = buffer.toString();
         break;

      case HistoricalEventType.EXCEPTION:
         type = EXCEPTION_TYPE;
         name = Localizer.getString(LocalizerKey.PH_EXCEPTION_TYPE);
         fullDetail = (String) event.getDetails();
         break;

      case HistoricalEventType.NOTE:
         type = NOTE_TYPE;
         name = Localizer.getString(LocalizerKey.PH_NOTE_TYPE);
         fullDetail = (String) event.getDetails();
         break;

      case HistoricalEventType.STATE_CHANGE:
         HistoricalEventDescriptionStateChange stateDescr = (HistoricalEventDescriptionStateChange) event.getDetails();
         ActivityInstanceState toState = stateDescr.getToState();
         Participant toPerformer = null;
         switch (toState.getValue())
         {
         case ActivityInstanceState.HIBERNATED:
            name = Localizer.getString(LocalizerKey.PH_RESUBMISSION_TYPE);
            type = RESUBMISSION_TYPE;
            break;
         case ActivityInstanceState.APPLICATION:
            name = Localizer.getString(LocalizerKey.PH_ACTIVITY_ACTIVE_TYPE);
            type = ACTIVITY_ACTIVE_TYPE;
            break;
         case ActivityInstanceState.SUSPENDED:
            name = Localizer.getString(LocalizerKey.PH_SUSPENDED_TYPE);
            type = SUSPENDED_TYPE;
            toPerformer = stateDescr.getToPerformer();
            if (toPerformer != null)
            {
               fullDetail = "-> " + I18nUtils.getParticipantName(toPerformer);
            }
            break;
         case ActivityInstanceState.ABORTED:
            name = Localizer.getString(LocalizerKey.PH_ABORTED_TYPE);
            type = ABORTED_TYPE;
            break;
         case ActivityInstanceState.ABORTING:
            name = Localizer.getString(LocalizerKey.PH_ABORTING_TYPE);
            type = ABORTING_TYPE;
            break;
         case ActivityInstanceState.INTERRUPTED:
            name = Localizer.getString(LocalizerKey.PH_INTERRUPTED_TYPE);
            type = INTERRUPTED_TYPE;
            break;
         case ActivityInstanceState.COMPLETED:
            name = Localizer.getString(LocalizerKey.PH_COMPLETED_TYPE);
            type = COMPLETED_TYPE;
            break;
         }
         break;

      default:
         break;
      }

      if (fullDetail != null && fullDetail.length() > 30)
      {
         detail = fullDetail.substring(0, 29) + " ... ";
      }
      else
      {
         detail = fullDetail;
         fullDetail = null;
      }

      eventTime = event.getEventTime();
      user = event.getUser();
      performer = user != null ? I18nUtils.getUserLabel(user) : null;
   }

   public String getName()
   {
      return name;
   }

   public String getRuntimeObjectType()
   {
      return type;
   }

   protected void runtimeObjectChanged()
   {}

   public Date getStartTime()
   {
      return eventTime;
   }

   public Date getLastModificationTime()
   {
      return eventTime;
   }

   public String getPerformer()
   {
      return performer;
   }

   public String getDetails()
   {
      return detail;
   }

   public String getState()
   {
      return null;
   }

   public boolean isMoreDetailsAvailable()
   {
      return fullDetail != null && !StringUtils.isEmpty(detail);
   }

   public String getFullDetails()
   {
      return fullDetail;
   }

   public HistoricalEventType getEventType()
   {
      return eventType;
   }

   /**
    * @return the user
    */
   public User getUser()
   {
      return user;
   }
}