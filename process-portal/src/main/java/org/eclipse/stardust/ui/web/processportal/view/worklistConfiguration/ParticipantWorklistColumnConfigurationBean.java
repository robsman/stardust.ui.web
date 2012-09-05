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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.model.ParticipantInfo;
import org.eclipse.stardust.engine.api.runtime.DepartmentInfo;
import org.eclipse.stardust.engine.api.runtime.PerformerType;
import org.eclipse.stardust.ui.web.common.message.MessageDialog;
import org.eclipse.stardust.ui.web.processportal.common.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.DelegationBean;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.IDelegatesProvider;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.IDelegatesProvider.Options;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.IDepartmentProvider;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.WorklistParticipantsProvider;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.ParametricCallbackHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;

/**
 * @author Yogesh.Manware
 * 
 */
public class ParticipantWorklistColumnConfigurationBean extends WorklistColumnConfigurationBean
{
   WorklistParticipantsProvider provider;

   public ParticipantWorklistColumnConfigurationBean()
   {
      this(UserPreferencesEntries.V_WORKLIST_PART_CONF);
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
      WorklistParticipantsProvider provider = getProvider();
      delegationBean.setDelegatesProvider((IDelegatesProvider) provider);
      delegationBean.setDepartmentDelegatesProvider((IDepartmentProvider) provider);

      delegationBean.setICallbackHandler(new ParametricCallbackHandler()
      {
         public void handleEvent(EventType eventType)
         {
            if (EventType.APPLY == eventType)
            {
               addParticipant(getParameters().get("selectedParticipant"));
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
   protected void retrieveandSetConfigurationValues()
   {
      try
      {
         Map<PerformerType, List< ? extends ParticipantInfo>> delegates = getProvider().findDelegates(null,
               getDelegateProviderOptions());

         List< ? extends ParticipantInfo> modelParticipants = delegates.get(PerformerType.ModelParticipant);

         columnConfTableEntries = new ArrayList<WorklistConfigTableEntry>();

         // set default configuration
         defaultConf = WorklistConfigurationUtil
               .getStoredValues(WorklistConfigurationUtil.DEFAULT, columnConfiguration);
         WorklistConfigTableEntry defaultEntry = new WorklistConfigTableEntry(WorklistConfigurationUtil.DEFAULT);
         defaultEntry.setConfiguration(defaultConf);
         columnConfTableEntries.add(defaultEntry);

         for (ParticipantInfo participantInfo : modelParticipants)
         {
            WorklistConfigTableEntry confTableEntry = new WorklistConfigTableEntry(participantInfo);
            if (existingoIds.contains(confTableEntry.getElementOID()))
            {
               continue;
            }
            fetchStoredValues(confTableEntry);
         }
         Set<DepartmentInfo> departments = getProvider().findDepartments(null, getDepartmentOptions()).get(
               "Departments");
         for (DepartmentInfo departmentInfo : departments)
         {
            WorklistConfigTableEntry confTableEntry = new WorklistConfigTableEntry(departmentInfo);
            fetchStoredValues(confTableEntry);
         }

         columnConfigurationTable.setList(columnConfTableEntries);
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }
   }

   /**
    * @return provider
    */
   private WorklistParticipantsProvider getProvider()
   {
      if (null == provider)
      {
         provider = new WorklistParticipantsProvider();
      }
      return provider;
   }

   /**
    * @param participant
    */
   private void addParticipant(Object participant)
   {
      if (participant instanceof ParticipantInfo)
      {
         WorklistConfigTableEntry entry = new WorklistConfigTableEntry((ParticipantInfo) participant);
         if (existingoIds.contains(entry.getElementOID()))
         {
            MessageDialog.addErrorMessage(getMessage("views.worklistPanelConfiguration.error.participantExist"));
         }
         else
         {
            addEntry(entry);
         }
      }
      else if (participant instanceof DepartmentInfo)
      {
         WorklistConfigTableEntry entry = new WorklistConfigTableEntry((DepartmentInfo) participant);
         if (existingoIds.contains(entry.getElementOID()))
         {
            MessageDialog.addErrorMessage(getMessage("views.worklistPanelConfiguration.error.departmentExist"));
         }
         else
         {
            addEntry(entry);
         }
      }
   }

   /**
    * @return DepartmentOptions
    */
   private IDepartmentProvider.Options getDepartmentOptions()
   {
      return new IDepartmentProvider.Options()
      {

         public String getNameFilter()
         {
            return "";
         }

         public boolean isStrictSearch()
         {
            return false;
         }
      };
   }

   /**
    * @return delegateProviderOptions
    */
   private Options getDelegateProviderOptions()
   {
      return new IDelegatesProvider.Options()
      {
         /*
          * (non-Javadoc)
          * 
          * @see
          * org.eclipse.stardust.ui.web.viewscommon.dialogs.IDelegatesProvider.Options
          * #getPerformerTypes()
          */
         public Set<Integer> getPerformerTypes()
         {
            Set<Integer> result = CollectionUtils.newSet();
            result.add(IDelegatesProvider.ROLE_TYPE);
            result.add(IDelegatesProvider.ORGANIZATION_TYPE);
            result.add(IDelegatesProvider.DEPARTMENT_TYPE);
            return Collections.unmodifiableSet(result);
         }

         /*
          * (non-Javadoc)
          * 
          * @see
          * org.eclipse.stardust.ui.web.viewscommon.dialogs.IDelegatesProvider.Options
          * #isStrictSearch()
          */
         public boolean isStrictSearch()
         {
            return true;
         }

         /*
          * (non-Javadoc)
          * 
          * @see
          * org.eclipse.stardust.ui.web.viewscommon.dialogs.IDelegatesProvider.Options
          * #getNameFilter()
          */
         public String getNameFilter()
         {
            return "";
         }
      };
   }
}