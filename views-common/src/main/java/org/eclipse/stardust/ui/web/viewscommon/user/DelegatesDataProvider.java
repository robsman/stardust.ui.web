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
package org.eclipse.stardust.ui.web.viewscommon.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.model.SelectItem;

import org.eclipse.stardust.engine.api.model.Participant;
import org.eclipse.stardust.engine.api.model.ParticipantInfo;
import org.eclipse.stardust.engine.api.runtime.DepartmentInfo;
import org.eclipse.stardust.engine.api.runtime.PerformerType;
import org.eclipse.stardust.ui.web.common.autocomplete.IAutocompleteDataProvider;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.DefaultDelegatesProvider;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.DepartmentDelegatesProvider;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.IDelegatesProvider;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.IDepartmentProvider;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.ParticipantFilterCriteria;



/**
 * @author Shrikant.Gangal
 * 
 */
public class DelegatesDataProvider implements IAutocompleteDataProvider
{
   private static final String DEPARTMENTS = "Departments";
   private static final int ALL_TYPES = 0;
   private static final int USER_TYPE = 1;
   private static final int ROLE_TYPE = 2;
   private static final int ORGANIZATION_TYPE = 3;
   private static final int DEPARTMENT_TYPE = 4;

   private ParticipantFilterCriteria addnFilterCriteria;
   
   private IDelegatesProvider delegatesProvider;
   
   private IDepartmentProvider departmentProvider;

   /**
    * @param addnFilterCriteria
    */
   public DelegatesDataProvider(final ParticipantFilterCriteria addnFilterCriteria)
   {
      this.addnFilterCriteria = addnFilterCriteria;
   }

   /*
    * Retrieves the matching participants for the given search value and additional filter
    * criteria. The argument 'searchValue' is not used as the additional filter criteria
    * will have this too.
    * 
    * @see
    * org.eclipse.stardust.ui.web.common.autocomplete.IAutocompleteDataProvider#getMatchingData
    * (java.lang.String, int)
    */
   public List<SelectItem> getMatchingData(String searchValue, int maxMatches)
   {      
      if (null == delegatesProvider)
      {
         delegatesProvider = DefaultDelegatesProvider.INSTANCE;
      }


      List<SelectItem> userItems = new ArrayList<SelectItem>();
      // Add default participants
      int typeFilter = addnFilterCriteria.getTypeFilter();
      if (typeFilter == ALL_TYPES || typeFilter == USER_TYPE || typeFilter == ROLE_TYPE
            || typeFilter == ORGANIZATION_TYPE)
      {

         Map<PerformerType, List< ? extends ParticipantInfo>> delegates = delegatesProvider.findDelegates(
               addnFilterCriteria.getActivityInstances(), addnFilterCriteria.getDefaultParticipantOptions());
         
         List<ParticipantWrapper> selectedParticipants = new ArrayList<ParticipantWrapper>();
         selectedParticipants.addAll(copyToParticipantWrapperList(delegates.get(PerformerType.User)));
         selectedParticipants.addAll(copyToParticipantWrapperList(delegates.get(PerformerType.ModelParticipant)));
         selectedParticipants.addAll(copyToParticipantWrapperList(delegates.get(PerformerType.UserGroup)));

         for (ParticipantWrapper participantWrapper : selectedParticipants)
         {
            userItems.add(new SelectItem(participantWrapper, participantWrapper.getText()));
         }
      }
      
      // Add departments
      if (typeFilter == ALL_TYPES || typeFilter == DEPARTMENT_TYPE)
      {
         if (null == departmentProvider)
         {
            departmentProvider = DepartmentDelegatesProvider.INSTANCE;
         }
         
         Map<String, Set<DepartmentInfo>> deptDelegates = departmentProvider.findDepartments(
               addnFilterCriteria.getActivityInstances(), addnFilterCriteria.getDeptParticipantOptions());
         
         Set<DepartmentInfo> selectedDepts = deptDelegates.get(DEPARTMENTS);
         for (DepartmentInfo departmentInfo : selectedDepts)
         {
            ParticipantWrapper participantWrapper = new ParticipantWrapper(departmentInfo);
            userItems.add(new SelectItem(participantWrapper, participantWrapper.getText()));
         }
      }

      return userItems;
   }

   /**
    * @param allParticipants
    * @return
    */
   private List<ParticipantWrapper> copyToParticipantWrapperList(List< ? extends ParticipantInfo> allParticipants)
   {
      List<ParticipantWrapper> selectParticipants = new ArrayList<ParticipantWrapper>();
      if (allParticipants != null)
      {
         for (ParticipantInfo participantInfo : allParticipants)
         {
            if (participantInfo instanceof Participant)
            {
               selectParticipants.add(new ParticipantWrapper((Participant) participantInfo));
            }
         }
      }
      return selectParticipants;
   }

   public IDelegatesProvider getDelegatesProvider()
   {
      return delegatesProvider;
   }

   public void setDelegatesProvider(IDelegatesProvider delegatesProvider)
   {
      this.delegatesProvider = delegatesProvider;
   }

   public void setDepartmentProvider(IDepartmentProvider departmentProvider)
   {
      this.departmentProvider = departmentProvider;
   }
}
