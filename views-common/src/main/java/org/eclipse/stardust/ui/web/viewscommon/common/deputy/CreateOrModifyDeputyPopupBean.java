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
package org.eclipse.stardust.ui.web.viewscommon.common.deputy;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.model.SelectItem;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.dto.UserDetailsLevel;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.engine.api.model.Role;
import org.eclipse.stardust.engine.api.model.RoleInfo;
import org.eclipse.stardust.engine.api.query.FilterAndTerm;
import org.eclipse.stardust.engine.api.query.FilterOrTerm;
import org.eclipse.stardust.engine.api.query.FilterTerm;
import org.eclipse.stardust.engine.api.query.ParticipantAssociationFilter;
import org.eclipse.stardust.engine.api.query.SubsetPolicy;
import org.eclipse.stardust.engine.api.query.UserDetailsPolicy;
import org.eclipse.stardust.engine.api.query.UserQuery;
import org.eclipse.stardust.engine.api.query.Users;
import org.eclipse.stardust.engine.api.runtime.Department;
import org.eclipse.stardust.engine.api.runtime.DeputyOptions;
import org.eclipse.stardust.engine.api.runtime.Grant;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.api.runtime.UserService;
import org.eclipse.stardust.ui.web.common.PopupUIComponentBean;
import org.eclipse.stardust.ui.web.common.autocomplete.IAutocompleteDataProvider;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.common.ModelHelper;
import org.eclipse.stardust.ui.web.viewscommon.common.ValidationMessageBean;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.ICallbackHandler.EventType;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.IParametricCallbackHandler;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.ParametricCallbackHandler;
import org.eclipse.stardust.ui.web.viewscommon.user.UserAutocompleteMultiSelector;
import org.eclipse.stardust.ui.web.viewscommon.user.UserWrapper;
import org.eclipse.stardust.ui.web.viewscommon.utils.DateUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;
import org.eclipse.stardust.ui.web.viewscommon.utils.ParticipantUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ServiceFactoryUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.UserUtils;
import org.eclipse.stardust.ui.web.viewscommon.views.authorization.DualListModel;
import org.eclipse.stardust.ui.web.viewscommon.views.authorization.SelectItemModel;

/**
 * @author Subodh.Godbole
 *
 */
public class CreateOrModifyDeputyPopupBean extends PopupUIComponentBean
{
   private static final long serialVersionUID = 1L;

   public static String SRCH_MODE_SIMILAR_USERS = "SIMILAR_USERS";
   public static String SRCH_MODE_ALL_USERS = "ALL_USERS";

   private boolean modifyMode;
   private DeputyTableEntry deputyTableEntry;
   private User user;
   private DualListModel dualListModel;
   private String searchMode = "SIMILAR_USERS";

   private UserAutocompleteMultiSelector deputySelector;

   List<ModelParticipantInfo> modelParticipantInfoList;
   UserQuery query;

   private ParametricCallbackHandler callbackHandler;
   private ValidationMessageBean validationMessageBean = new ValidationMessageBean();

   /**
    *
    */
   public CreateOrModifyDeputyPopupBean()
   {
      super("deputyManagementView");
   }

   /**
    * @return
    */
   public static CreateOrModifyDeputyPopupBean getInstance()
   {
      return (CreateOrModifyDeputyPopupBean) FacesUtils.getBeanFromContext("createOrModifyDeputyPopupBean");
   }

   /**
    * @param deputyTableEntry
    * @param user
    * @param modifyMode
    * @param callbackHandler
    */
   public void openPopup(DeputyTableEntry deputyTableEntry, User user, ParametricCallbackHandler callbackHandler)
   {
      this.deputyTableEntry = deputyTableEntry;
      this.user = user;
      this.modifyMode = (null != deputyTableEntry) ? true : false;
      this.callbackHandler = callbackHandler;
      modelParticipantInfoList = new ArrayList<ModelParticipantInfo>();
      if (null == this.deputyTableEntry)
      {
         this.deputyTableEntry = new DeputyTableEntry(null, null, null, null);
      }

      initializeModel();

      // For Title
      String userDisplayName = UserUtils.getUserDisplayLabel(user);
      String title = modifyMode
            ? getMessages().getString("dialog.modify.title", userDisplayName)
            : getMessages().getString("dialog.create.title", userDisplayName);
      setTitle(title);

      super.openPopup();
   }

   /**
    * @param excludeThisUser
    * @return
    */
   private UserQuery getUsersWithSimilarGrants(boolean excludeThisUser)
   {
      UserQuery query = UserQuery.findActive();
      User user = null == this.user ? SessionContext.findSessionContext().getUser() : this.user;
      if (user != null)
      {
         FilterTerm filter = query.getFilter().addOrTerm();

         ModelParticipantInfo modelParticipantInfo;
         Model model;
         Role role;
         Department department;

         List<Grant> grants = user.getAllGrants();
         for (Grant grant:grants)
         {
            if (!grant.isOrganization())
            {
               model = ModelCache.findModelCache().getActiveModel(grant);
               role = model.getRole(grant.getId());
               department = grant.getDepartment();

               if (department != null)
               {
                  modelParticipantInfo = department.getScopedParticipant(role);
               }
               else
               {
                  modelParticipantInfo = role;
               }
               modelParticipantInfoList.add(modelParticipantInfo);
               filter.add(ParticipantAssociationFilter.forParticipant(((RoleInfo) modelParticipantInfo)));
            }
         }

         if (excludeThisUser)
         {
            //filter.add(UserQuery.OID.isEqual(user.getOID()));
         }
      }
      else
      {
         query.where(UserQuery.OID.isEqual(0));
      }
      return query;
   }
   /**
    *
    */
   private void initializeModel()
   {
      validationMessageBean.reset();

      deputySelector = new UserAutocompleteMultiSelector(false, true);
      deputySelector.setShowOnlineIndicator(false);
      deputySelector.setDataProvider(new UserDataProvider(deputySelector.getDataProvider()));

      dualListModel = new DualListModel();
      query = getUsersWithSimilarGrants(true);
      // Available Participants
      List<SelectItemModel> assignedList = new ArrayList<SelectItemModel>();
      for (ModelParticipantInfo participantInfo : modelParticipantInfoList)
      {
         if (!containsParticipant(deputyTableEntry.getParticipants(), participantInfo))
         {
            assignedList.add(new SelectItemModel(ModelHelper.getParticipantName(participantInfo), ParticipantUtils
                  .getParticipantUniqueKey(participantInfo), participantInfo,
                  false));
         }
      }
      dualListModel.getSource().addAll(assignedList);

      // Selected Participants
      List<SelectItemModel> assignablesList = new ArrayList<SelectItemModel>();
      for (ModelParticipantInfo participant : deputyTableEntry.getParticipants())
      {
         assignablesList.add(new SelectItemModel(ModelHelper.getParticipantName(participant), ParticipantUtils
               .getParticipantUniqueKey(participant), participant, false));
      }
      dualListModel.getTarget().addAll(assignablesList);
   }

   /**
    * @param participants
    * @param participant
    * @return
    */
   private boolean containsParticipant(Set<ModelParticipantInfo> participants, ModelParticipantInfo participant)
   {
      for (ModelParticipantInfo part : participants)
      {
         if (ParticipantUtils.areEqual(part, participant))
         {
            return true;
         }
      }
      return false;
   }

   @Override
   public void apply()
   {
      try
      {
         validationMessageBean.reset();

         // Validate Deputy User
         if (!modifyMode && null == deputySelector.getSelectedValue())
         {
            validationMessageBean.addError(getMessages().getString("dialog.error.invalidDeputy"), (String[])null);
         }

         // Validate Dates
         if (!DateUtils.validateDateRange(deputyTableEntry.getValidFrom(), deputyTableEntry.getValidTo()))
         {
            validationMessageBean.addError(getMessages().getString("dialog.error.invalidDateRange"), (String[])null);
         }

         // Validate Authorizations
         if (CollectionUtils.isEmpty(dualListModel.getTarget()))
         {
            validationMessageBean.addError(getMessages().getString("dialog.error.noAuthsSelected"), (String[])null);
         }

         if (validationMessageBean.isContainMessages())
         {
            return;
         }

         Set<ModelParticipantInfo> selectedParticipants = new HashSet<ModelParticipantInfo>();
         ModelParticipantInfo participant;
         List<SelectItemModel> selectedAuths = dualListModel.getTarget();
         for (SelectItemModel selectItemModel : selectedAuths)
         {
            participant = (ModelParticipantInfo)selectItemModel.getValueObject();
            selectedParticipants.add(participant);
         }
         deputyTableEntry.setParticipants(selectedParticipants);

         UserService userService = ServiceFactoryUtils.getUserService();
         DeputyOptions deputyOptions = new DeputyOptions(deputyTableEntry.getValidFrom(),
               deputyTableEntry.getValidTo(), deputyTableEntry.getParticipants());

         if (modifyMode)
         {
            userService.modifyDeputy(user, deputyTableEntry.getUser(), deputyOptions);
         }
         else
         {
            deputyTableEntry.setUser(deputySelector.getSelectedValue().getUser());
            userService.addDeputy(user, deputySelector.getSelectedValue().getUser(), deputyOptions);
         }

         if (null != callbackHandler)
         {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("deputyTableEntry", deputyTableEntry);
            params.put("modifyMode", modifyMode);
            callbackHandler.setParameters(params);
            callbackHandler.handleEvent(EventType.APPLY);
         }

         closePopup();
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }
   }

   @Override
   public void initialize()
   {
   }

   public boolean isModifyMode()
   {
      return modifyMode;
   }

   public String getDeputyName()
   {
      return deputyTableEntry.getUserDisplayName();
   }

   public void setDeputyName(String deputyName)
   {
      //
   }

   public Date getValidFrom()
   {
      return deputyTableEntry.getValidFrom();
   }

   public void setValidFrom(Date validFrom)
   {
      deputyTableEntry.setValidFrom(validFrom);
   }

   public Date getValidTo()
   {
      return deputyTableEntry.getValidTo();
   }

   public void setValidTo(Date validTo)
   {
      deputyTableEntry.setValidTo(validTo);
   }

   public String getSearchMode()
   {
      return searchMode;
   }

   public void setSearchMode(String searchMode)
   {
      this.searchMode = searchMode;
   }

   public DeputyTableEntry getDeputyTableEntry()
   {
      return deputyTableEntry;
   }

   public User getUser()
   {
      return user;
   }

   public DualListModel getDualListModel()
   {
      return dualListModel;
   }

   public UserAutocompleteMultiSelector getDeputySelector()
   {
      return deputySelector;
   }

   public ValidationMessageBean getValidationMessageBean()
   {
      return validationMessageBean;
   }

   /**
    * @author Subodh.Godbole
    *
    */
   public class UserDataProvider implements IAutocompleteDataProvider
   {
      IAutocompleteDataProvider mainDataProvider;

      public UserDataProvider(IAutocompleteDataProvider mainDataProvider)
      {
         this.mainDataProvider = mainDataProvider;
      }

      /* (non-Javadoc)
       * @see org.eclipse.stardust.ui.web.common.autocomplete.IAutocompleteDataProvider#getMatchingData(java.lang.String, int)
       */
      public List<SelectItem> getMatchingData(String searchValue, int maxMatches)
      {
         if (SRCH_MODE_ALL_USERS.equals(searchMode))
         {
            // TODO Filter User for whom deputy is being added, also filter already added Deputies
            return mainDataProvider.getMatchingData(searchValue, maxMatches);
         }
         else
         {
            UserQuery userQuery = query;
            userQuery.setPolicy(new SubsetPolicy(maxMatches, false));
            applyFilters(userQuery, searchValue + "%");

            // This would filter the user for whom deputy is being added
            // TODO Also filter already added Deputies
            List<UserWrapper> selData = new ArrayList<UserWrapper>();
            selData.add(new UserWrapper(user, UserAutocompleteMultiSelector.getUserLabel(user, searchValue), true));

            Users users = ServiceFactoryUtils.getQueryService().getAllUsers(userQuery);
            return UserAutocompleteMultiSelector.buildSearchResult(users, selData, searchValue);
         }
      }

      /**
       * @param userQuery
       * @param searchValue
       */
      private void applyFilters(UserQuery userQuery, String searchValue)
      {
         String nameFirstLetterCaseChanged = UserUtils.alternateFirstLetter(searchValue);
         userQuery.setPolicy(new UserDetailsPolicy(UserDetailsLevel.Core));
         
         FilterAndTerm andFilter = userQuery.getFilter().addAndTerm();
         FilterOrTerm filter = andFilter.addOrTerm();
         filter.or(UserQuery.FIRST_NAME.like(searchValue));
         filter.or(UserQuery.FIRST_NAME.like(nameFirstLetterCaseChanged));
         filter.or(UserQuery.LAST_NAME.like(searchValue));
         filter.or(UserQuery.LAST_NAME.like(nameFirstLetterCaseChanged));
         filter.or(UserQuery.ACCOUNT.like(searchValue));
         filter.or(UserQuery.ACCOUNT.like(nameFirstLetterCaseChanged));
         userQuery.where(filter);

         userQuery.orderBy(UserQuery.LAST_NAME).and(UserQuery.FIRST_NAME).and(UserQuery.ACCOUNT);
      }
   }
}
