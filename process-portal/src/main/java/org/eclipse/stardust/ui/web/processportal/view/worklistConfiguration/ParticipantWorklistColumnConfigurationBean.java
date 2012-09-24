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
package org.eclipse.stardust.ui.web.processportal.view.worklistConfiguration;

import java.util.List;

import org.eclipse.stardust.engine.api.model.ParticipantInfo;
import org.eclipse.stardust.engine.api.model.QualifiedModelParticipantInfo;
import org.eclipse.stardust.engine.api.query.UserGroupQuery;
import org.eclipse.stardust.engine.api.query.UserGroups;
import org.eclipse.stardust.engine.api.runtime.DepartmentInfo;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.UserGroup;
import org.eclipse.stardust.ui.web.common.message.MessageDialog;
import org.eclipse.stardust.ui.web.processportal.common.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.DelegationBean;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.ParametricCallbackHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.ParticipantUtils;

/**
 * @author Yogesh.Manware
 * 
 */
public class ParticipantWorklistColumnConfigurationBean extends WorklistColumnConfigurationBean
{
   private static final String PARTICIPANT_KEY = "participant.";
   private static final String PARTICIPANT_EXIST_KEY = PARTICIPANT_KEY + "exist";
   private static final String SELECT_PARTICIPANT_KEY = PARTICIPANT_KEY + "select";

   List<QualifiedModelParticipantInfo> modelParticipants;
   UserGroups allUserGroups;

   /**
    * default constructor
    */
   public ParticipantWorklistColumnConfigurationBean()
   {
      this(UserPreferencesEntries.P_WORKLIST_PART_CONF);
   }

   /**
    * @param preferenceID
    */
   private ParticipantWorklistColumnConfigurationBean(String preferenceID)
   {
      super(preferenceID);
      initialize();
   }

   @Override
   public void add()
   {
      DelegationBean delegationBean = DelegationBean.getCurrent();
      delegationBean.setDelegateCase(false);
      delegationBean.setSelectedParticipantCase(true);
      delegationBean.setTitle(getMessage(SELECT_PARTICIPANT_KEY));

      delegationBean.setICallbackHandler(new ParametricCallbackHandler()
      {
         public void handleEvent(EventType eventType)
         {
            if (EventType.APPLY == eventType)
            {
               addParticipant(getParameter(DelegationBean.SELECTED_PARTICIPANT));
            }
         }
      });
      delegationBean.openPopup();
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.stardust.ui.web.processportal.view.worklistConfiguration.
    * WorklistColumnConfigurationBean#retrieveandSetConfigurationValues()
    */
   protected void retrieveConfigurations()
   {
      try
      {
         if (null == modelParticipants)
         {
            modelParticipants = ParticipantUtils.fetchAllParticipants(true);
         }

         for (QualifiedModelParticipantInfo participantInfo : modelParticipants)
         {
            WorklistConfigTableEntry confTableEntry = new WorklistConfigTableEntry(participantInfo);
            if (existingConfigurations.contains(confTableEntry.getIdentityKey()))
            {
               continue;
            }
            fetchStoredValues(confTableEntry);
         }

         // set User Groups
         if (null == allUserGroups)
         {
            allUserGroups = getAllUserGroups();
         }

         for (UserGroup userGroup : allUserGroups)
         {
            WorklistConfigTableEntry confTableEntry = new WorklistConfigTableEntry(userGroup);
            if (existingConfigurations.contains(confTableEntry.getIdentityKey()))
            {
               continue;
            }
            fetchStoredValues(confTableEntry);
         }

         columnConfigurationTable.setList(columnConfTableEntries);
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }
   }

   protected String getPropertyKey()
   {
      return PARTICIPANT_KEY;
   }

   /**
    * @param node
    * @return
    */
   private UserGroups getAllUserGroups()
   {
      UserGroupQuery userGroupQuery = UserGroupQuery.findAll();
      return getQryService().getAllUserGroups(userGroupQuery);
   }

   /**
    * @return
    */
   private QueryService getQryService()
   {
      return SessionContext.findSessionContext().getServiceFactory().getQueryService();
   }

   /**
    * @param participant
    */
   private void addParticipant(Object participant)
   {
      if (null != participant)
      {
         if (participant instanceof ParticipantInfo)
         {
            WorklistConfigTableEntry entry = new WorklistConfigTableEntry((ParticipantInfo) participant);
            if (existingConfigurations.contains(entry.getIdentityKey()))
            {
               MessageDialog.addErrorMessage(getMessage(PARTICIPANT_EXIST_KEY));
            }
            else
            {
               addEntry(entry);
            }
         }
         else if (participant instanceof DepartmentInfo)
         {
            WorklistConfigTableEntry entry = new WorklistConfigTableEntry((DepartmentInfo) participant);
            if (existingConfigurations.contains(entry.getIdentityKey()))
            {
               MessageDialog.addErrorMessage(getMessage(PARTICIPANT_EXIST_KEY));
            }
            else
            {
               addEntry(entry);
            }
         }
      }
   }
}