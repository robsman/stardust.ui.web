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
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.faces.model.SelectItem;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.dto.UserDetailsLevel;
import org.eclipse.stardust.engine.api.model.Organization;
import org.eclipse.stardust.engine.api.model.Participant;
import org.eclipse.stardust.engine.api.query.FilterOrTerm;
import org.eclipse.stardust.engine.api.query.UserDetailsPolicy;
import org.eclipse.stardust.engine.api.query.UserQuery;
import org.eclipse.stardust.engine.api.query.Users;
import org.eclipse.stardust.engine.api.runtime.Department;
import org.eclipse.stardust.engine.api.runtime.DepartmentInfo;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.ui.web.common.autocomplete.IAutocompleteDataProvider;
import org.eclipse.stardust.ui.web.viewscommon.common.configuration.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.viewscommon.utils.ServiceFactoryUtils;



/**
 * @author Shrikant.Gangal
 * 
 */
public class ParticipantsDataProvider implements IAutocompleteDataProvider
{
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
      QueryService service = ServiceFactoryUtils.getQueryService();

      UserQuery userQuery = UserQuery.findActive();
      UserDetailsPolicy userPolicy = new UserDetailsPolicy(UserDetailsLevel.Core);
      userPolicy.setPreferenceModules(UserPreferencesEntries.M_ADMIN_PORTAL, UserPreferencesEntries.M_VIEWS_COMMON);
      userQuery.setPolicy(userPolicy);
      
      if (!StringUtils.isEmpty(searchValue))
      {
         String name = searchValue.replaceAll("\\*", "%") + "%";
         String nameFirstLetterCaseChanged = alternateFirstLetter(name);
         FilterOrTerm or = userQuery.getFilter().addOrTerm();
         or.add(UserQuery.LAST_NAME.like(name));
         or.add(UserQuery.LAST_NAME.like(nameFirstLetterCaseChanged));
         or.add(UserQuery.FIRST_NAME.like(name));
         or.add(UserQuery.FIRST_NAME.like(nameFirstLetterCaseChanged));
         or.add(UserQuery.ACCOUNT.like(name));
         or.add(UserQuery.ACCOUNT.like(nameFirstLetterCaseChanged));
      }
      userQuery.orderBy(UserQuery.LAST_NAME).and(UserQuery.FIRST_NAME).and(UserQuery.ACCOUNT);

      Users matchingUsers = service.getAllUsers(userQuery);
      List<ParticipantWrapper> selectedParticipants = new ArrayList<ParticipantWrapper>();
      selectedParticipants.addAll(copyToParticipantWrapperList(matchingUsers));

      List<Participant> rolesAndOrgs = service.getAllParticipants();
      selectedParticipants.addAll(copyToParticipantWrapperList(rolesAndOrgs, searchValue));      
      
      Set<DepartmentInfo> departments = getAllDepartments(rolesAndOrgs);
      selectedParticipants.addAll(copyToParticipantWrapperList(departments, searchValue));
      
      
      List<SelectItem> selectItems = new ArrayList<SelectItem>();
      for (ParticipantWrapper pw : selectedParticipants) {
         selectItems.add(new SelectItem(pw, pw.getText()));
      }
      
      return selectItems;
   }

   /**
    * @param rolesAndOrgs
    * @return
    */
   private Set<DepartmentInfo> getAllDepartments(List<Participant> rolesAndOrgs) {
      QueryService service = ServiceFactoryUtils.getQueryService();
      Set<DepartmentInfo> departmentInfos = CollectionUtils.newHashSet();
      for (Participant p : rolesAndOrgs)
      {
         if (p instanceof Organization)
         {
            departmentInfos.addAll(service.findAllDepartments(null, (Organization) p));
         }
      }
      
      return departmentInfos;
   }

   /**
    * @param departments
    * @param searchValue
    * @return
    */
   private Collection< ? extends ParticipantWrapper> copyToParticipantWrapperList(Set<DepartmentInfo> departments,
         String searchValue)
   {
      List<ParticipantWrapper> selectParticipants = new ArrayList<ParticipantWrapper>();
      String regex = !StringUtils.isEmpty(searchValue) ? searchValue.replaceAll("\\*", ".*") + ".*" : null;
      if (CollectionUtils.isNotEmpty(departments))
      {
         for (DepartmentInfo deptInfo : departments)
         {
            if (deptInfo.getName().matches(regex))
            {
               selectParticipants.add(new ParticipantWrapper((Department) deptInfo));
            }
         }
      }

      return selectParticipants;
   }
   
   /**
    * @param allParticipants
    * @return
    */
   private List<ParticipantWrapper> copyToParticipantWrapperList(Users allParticipants)
   {
      List<ParticipantWrapper> selectParticipants = new ArrayList<ParticipantWrapper>();
      if (allParticipants != null)
      {
         for (Participant participant : allParticipants)
         {
            selectParticipants.add(new ParticipantWrapper(participant));
         }
      }
      return selectParticipants;
   }

   /**
    * @param allParticipants
    * @return
    */
   private List<ParticipantWrapper> copyToParticipantWrapperList(List<Participant> allParticipants, String searchValue)
   {
      List<ParticipantWrapper> selectParticipants = new ArrayList<ParticipantWrapper>();
      String regex = !StringUtils.isEmpty(searchValue) ? searchValue.replaceAll("\\*", ".*") + ".*" : null;
      if (allParticipants != null)
      {
         for (Participant participant : allParticipants)
         {
            if (participant.getName().matches(regex))
            {
               selectParticipants.add(new ParticipantWrapper(participant));
            }
         }
      }
      
      return selectParticipants;
   }
   
   /**
    * Changes the case of the initial letter of the given string.
    * 
    * @param field
    * @return
    */
   private static String alternateFirstLetter(String field)
   {
      String firstLetter = field.substring(0, 1);
      if (firstLetter.compareTo(field.substring(0, 1).toLowerCase()) == 0)
      {
         firstLetter = firstLetter.toUpperCase();
      }
      else
      {
         firstLetter = firstLetter.toLowerCase();
      }
      return firstLetter + field.substring(1);
   }
}
