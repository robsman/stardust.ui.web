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
package org.eclipse.stardust.ui.web.viewscommon.dialogs;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.FacesEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.dto.DepartmentDetails;
import org.eclipse.stardust.engine.api.model.ModelParticipant;
import org.eclipse.stardust.engine.api.model.Organization;
import org.eclipse.stardust.engine.api.model.Participant;
import org.eclipse.stardust.engine.api.model.Role;
import org.eclipse.stardust.engine.api.query.UserGroups;
import org.eclipse.stardust.engine.api.query.Users;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.Department;
import org.eclipse.stardust.engine.api.runtime.DepartmentInfo;
import org.eclipse.stardust.engine.api.runtime.PerformerType;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.ui.web.common.PopupUIComponentBean;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ManagedBeanUtils;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.common.Localizer;
import org.eclipse.stardust.ui.web.viewscommon.common.LocalizerKey;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.ICallbackHandler.EventType;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.IDelegatesProvider.Options;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.participantManagement.ParticipantTree;
import org.eclipse.stardust.ui.web.viewscommon.participantManagement.ParticipantUserObject;
import org.eclipse.stardust.ui.web.viewscommon.user.DelegatesDataProvider;
import org.eclipse.stardust.ui.web.viewscommon.user.ParticipantAutocompleteSelector;
import org.eclipse.stardust.ui.web.viewscommon.user.ParticipantWrapper;
import org.eclipse.stardust.ui.web.viewscommon.utils.ActivityInstanceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.UserUtils;


import com.icesoft.faces.component.ext.RowSelectorEvent;

/**
 * @author Ankita.Patel
 * @version $Revision: $
 */
public class DelegationBean extends PopupUIComponentBean
{
   private static final long serialVersionUID = 2419043753205151128L;
   
   private static final String BEAN_NAME = "delegationBean";
   private static final int ALL_TYPES = 0;
   private static final int USER_TYPE = 1;
   private static final int ROLE_TYPE = 2;
   private static final int ORGANIZATION_TYPE = 3;
   private static final int DEPARTMENT_TYPE = 4;
   private static final String DEPARTMENTS = "Departments";   
   private static final String EMPTY_STRING = "";

   protected static final Logger trace = LogManager.getLogger(DelegationBean.class);
   
   public static final String NOTE_PARAM = "note";
   public static final String NOTE_ENABLED = "noteEnabled";

   private MessagesViewsCommonBean propsBean;
   private List<ActivityInstance> ais;
   private List<ParticipantEntry> searchResult;
   private String notes;
   private int typeFilter;
   private String nameFilter = EMPTY_STRING;
   private List<SelectItem> typeFilters = CollectionUtils.newList();
   private IDelegationHandler delegationHandler;
   private IDelegatesProvider delegatesProvider;
   private IDepartmentProvider deptProvider;
   private ParticipantEntry selectedUser;
   private List<ActivityInstance> delegatedActivities;
   private ICallbackHandler iCallbackHandler;
   private Map<String, ? > activityOutData;
   private String activityContext;
   private ParticipantAutocompleteSelector autoCompleteSelector;   
   private boolean notesEnabled;
   private boolean buildDefaultNotes;
   private boolean limitedSearch = true;
   private boolean oldlimitedSearch = false;
   private boolean limitedSearchEnabled = true;
   private boolean disableAdministrator = false;
   private boolean fireCloseEvent = true;
   private boolean delegateCase = false;
   private ParticipantTree participantTree;
   
   private String id;  
   
   private DELEGATION_MODE delegationMode = DELEGATION_MODE.SEARCH_PARTICIPANTS;
   
   public static enum DELEGATION_MODE {
      PICK_FROM_LIST, SEARCH_PARTICIPANTS, PICK_FROM_TREE
   };

   /**
    * Initializes session context and type filters
    * 
    */
   public DelegationBean()
   {
      propsBean = MessagesViewsCommonBean.getInstance();
      initializeParticipantTree();
   }

   /**
    * @return DelegationBean object
    */
   public static DelegationBean getCurrent()

   {
      return (DelegationBean) FacesContext.getCurrentInstance().getApplication()
            .getVariableResolver().resolveVariable(FacesContext.getCurrentInstance(),
                  BEAN_NAME);
   }

   /**
    * @return
    */
   public String getId()
   {
      if (null == id)
      {
         Random o = new Random();
         id = "DEL" + o.nextInt(10000);
      }
      
      return id;
   }
   /**
    * delegates the current activity to selected participant
    * 
    * @param event
    */
   public void onApply(ActionEvent event)
   {
      Participant participant = null;
      Department department = null;
      Object obj = getSelectedParticipant();
      if (obj instanceof Participant)
      {
         participant = (Participant) obj;
      }
      else if (obj instanceof DepartmentInfo)
      {
         DepartmentInfo deptInfo = (DepartmentInfo) obj;
         department = SessionContext.findSessionContext().getServiceFactory().getAdministrationService().getDepartment(
               deptInfo.getOID());
      }
      Map<String, Object> params = CollectionUtils.newMap();

      params.put(NOTE_ENABLED, Boolean.valueOf(notesEnabled));
      if (notesEnabled)
      {
         if (buildDefaultNotes && StringUtils.isEmpty(notes))
         {
            notes = buildDefaultNotes(participant);
         }
         params.put(NOTE_PARAM, notes);
      }

      IDelegationHandler delHandler = delegationHandler;
      if (null == delHandler)
      {
         delHandler = (IDelegationHandler) ManagedBeanUtils
               .getManagedBean(DelegationHandlerBean.BEAN_ID);
      }

      try
      {
         if (delegatedActivities == null)
         {
            delegatedActivities = CollectionUtils.newList();
         }

         // OutData is only valid when delegating one Activity
         if (activityOutData != null && ais.size() == 1)
         {
            ActivityInstance activityInstance = ais.get(0);

            // Perform Suspend And Save
            ActivityInstance suspendedAi = ActivityInstanceUtils.suspendToUserWorklist(activityInstance, activityContext,
                  activityOutData);
            if (null != suspendedAi)
            {
               ais.set(0, suspendedAi);
            }
            else
            {
               // Suspend Failed
               return;
            }
         }

         List<ActivityInstance> delegatedActivities = null;
         
         for (ActivityInstance ai : ais)
         {
            if (!delegateCase && ActivityInstanceUtils.isDefaultCaseActivity(ai))
            {
               ExceptionHandler.handleException(ExceptionHandler.CLIENT_ID_NONE,
                     propsBean.getString("views.common.activity.abortActivity.failureMsg1"));
               return;
            }
         }
         
         if (participant != null)
         {
            delegatedActivities = delHandler.delegateActivities(ais, participant, params);
         }
         else if (department != null)
         {
            delegatedActivities = delHandler.delegateActivities(ais, department, params);
         }
         else
         {
            // If a participant is selected add a INFO message.
            FacesMessage msg = new FacesMessage();
            msg.setSeverity(FacesMessage.SEVERITY_INFO);
            msg.setSummary(propsBean.getString("delegation.noParticipantSelected.message"));
            msg.setDetail(propsBean.getString("delegation.noParticipantSelected.message"));
            FacesContext.getCurrentInstance().addMessage(null, msg);
         }
         
         if (delegatedActivities != null)
         {
            this.delegatedActivities.addAll(delegatedActivities);
         }

         if (this.delegatedActivities.size() != ais.size())
         {
            //No-op. This is handled by the core framework.
         }
         else
         {
            ICallbackHandler iCH = iCallbackHandler;
            fireCloseEvent = false;
            closePopup();

            if (iCH != null)
               iCH.handleEvent(EventType.APPLY);
         }

      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }
   }

   /**
    * fires on clicking Cancel button refresh the page
    */
   public void onCancel()
   {
      closePopup();
   }

   @Override
   public void initialize()
   {
      typeFilters.add(new SelectItem(new Integer(0), propsBean.getString("delegation.allTypes")));
      typeFilters.add(new SelectItem(new Integer(1), propsBean.getString("delegation.users")));
      typeFilters.add(new SelectItem(new Integer(2), propsBean.getString("delegation.roles")));
      typeFilters.add(new SelectItem(new Integer(3), propsBean.getString("delegation.orgs")));
      typeFilters.add(new SelectItem(new Integer(4), propsBean.getString("delegation.departments")));

      if (autoCompleteSelector == null)
      {
         autoCompleteSelector = new ParticipantAutocompleteSelector(new DelegatesDataProvider(
               new ParticipantFilterCriteria()
               {

                  public List<ActivityInstance> getActivityInstances()
                  {
                     return ais;
                  }

                  public boolean isLimitedSearchEnabled()
                  {
                     return limitedSearch;
                  }

                  public Options getDefaultParticipantOptions()
                  {
                     return getDelegateProviderOptions();
                  }

                  public IDepartmentProvider.Options getDeptParticipantOptions()
                  {
                     return getDepartmentOptions();
                  }

                  public int getTypeFilter()
                  {
                     return typeFilter;
                  }
               }), null);
      }
   }

   /**
    * fires on selecting data table row gets the current selected ParticipantEntry object
    * 
    * @param re
    */
   public void onRowSelection(RowSelectorEvent re)
   {
      this.selectedUser = searchResult.get(re.getRow());
   }

   private void initializeParticipantTree()
   {
      participantTree = new ParticipantTree();
      participantTree.setShowUserNodes(false);
      participantTree.setShowUserGroupNodes(false);
      participantTree.setHighlightUserFilterEnabled(false);
      participantTree.initialize();
   }
   
   /**
    * Cleanup resources
    */
   private void cleanup()
   {
      ais = null;
      activityOutData = null;
      activityContext = null;
      searchResult = null;
      notes = null;
      typeFilter = ALL_TYPES;
      nameFilter = EMPTY_STRING;
      typeFilters = CollectionUtils.newList();
      selectedUser = null;
      delegatedActivities = null;
      iCallbackHandler = null;
      fireCloseEvent = true;
      limitedSearch = true;
      delegationMode = DELEGATION_MODE.SEARCH_PARTICIPANTS;
      autoCompleteSelector.setSearchValue(EMPTY_STRING);
   }

   /**
    * @return selected participant
    */
   private Object getSelectedParticipant()
   {
      if (DELEGATION_MODE.PICK_FROM_LIST.equals(delegationMode))
      {
         if (selectedUser != null)
         {
            if (selectedUser.getParticipant() != null)
            {
               return selectedUser.getParticipant();
            }
            else if (selectedUser.getDepartment() != null)
            {
               return selectedUser.getDepartment();
            }
         }
      }
      else if (DELEGATION_MODE.SEARCH_PARTICIPANTS.equals(delegationMode))
      {
         ParticipantWrapper participantWrapper = autoCompleteSelector.getSelectedValue();
         if (null != participantWrapper)
         {
            return participantWrapper.getObject();
         }
      }
      else if (DELEGATION_MODE.PICK_FROM_TREE.equals(delegationMode))
      {
         ParticipantUserObject userObj = participantTree.getSelectedUserObject();
         if (null != userObj)
         {
            if (null != userObj.getDynamicParticipantInfo())
            {
               return userObj.getDynamicParticipantInfo();
            }
            else if (null != userObj.getQualifiedModelParticipantInfo())
            {
               return userObj.getQualifiedModelParticipantInfo();
            }
            else if (null != userObj.getDepartment())
            {
               return userObj.getDepartment();
            }
         }
      }
      return null;
   }

   /**
    * Retrieves the list of participants as per the filter criteria.
    */
   private void retrieveParticipants()
   {
      try
      {
         selectedUser = null;
         searchResult = CollectionUtils.newList();
         if (typeFilter == ALL_TYPES || typeFilter == USER_TYPE || typeFilter == ROLE_TYPE
               || typeFilter == ORGANIZATION_TYPE)
         {
            retrieveDefaultParticipants();
         }
         if (typeFilter == ALL_TYPES || typeFilter == DEPARTMENT_TYPE)
         {
            retrieveDepartments();
         }
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }
   }

   /**
    * Retrieves departments
    */
   private void retrieveDepartments()
   {
      IDepartmentProvider departmentDelegatesProvider = getDepartmentDelegatesProvider();
      if (null == departmentDelegatesProvider)
      {
         departmentDelegatesProvider = DepartmentDelegatesProvider.INSTANCE;
         trace.info("Using DEFAULT department delegates provider to retrieve departments");
      }

      Map<String, Set<DepartmentInfo>> deptsList = departmentDelegatesProvider.findDepartments(ais,
            getDepartmentOptions());

      appendDepartmentToSearch((Set<DepartmentInfo>) deptsList.get(DEPARTMENTS));
   }

   /**
    * @param deptsList
    */
   private void appendDepartmentToSearch(Set<DepartmentInfo> deptsList)
   {
      List<ParticipantEntry> participantList = CollectionUtils.newArrayList();
      for (DepartmentInfo departmentInfo : deptsList)
      {
         participantList.add(new ParticipantEntry(departmentInfo));
      }
      Collections.sort(participantList);
      searchResult.addAll(participantList);
   }

   /**
    * Retrieves participants
    */
   private void retrieveDefaultParticipants()
   {
      IDelegatesProvider delegatesProvider = getDelegatesProvider();
      if (null == delegatesProvider)
      {
         delegatesProvider = DefaultDelegatesProvider.INSTANCE;
         trace.info("Using DEFAULT delegates provider to retrieve participants");
      }

      Map<PerformerType, List<? extends Participant>> delegates = delegatesProvider.findDelegates(ais,
            getDelegateProviderOptions());

      if (!CollectionUtils.isEmpty(delegates))
      {
         // handle users
         List<? extends Participant> users = delegates.get(PerformerType.User);

         if ((users instanceof Users) && ((Users) users).hasMore())
         {
            // TODO handle large result sets
         }

         if (!CollectionUtils.isEmpty(users))
         {
            appendParticipantsToSearchResult(users);
         }

         // handle model participants
         List<? extends Participant> modelParticipants = delegates.get(PerformerType.ModelParticipant);

         if (!CollectionUtils.isEmpty(modelParticipants))
         {
            appendParticipantsToSearchResult(modelParticipants);
         }

         // handle user groups
         List<? extends Participant> userGroups = delegates.get(PerformerType.UserGroup);

         if ((userGroups instanceof UserGroups) && ((UserGroups) userGroups).hasMore())
         {
            // TODO handle large result sets
         }

         if (!CollectionUtils.isEmpty(userGroups))
         {
            appendParticipantsToSearchResult(userGroups);
         }
      }

   }

   /**
    * appends the Participant to search Result list
    * 
    * @param delegates
    */
   private void appendParticipantsToSearchResult(List<? extends Participant> delegates)
   {
      for (int i = 0; i < delegates.size(); ++i)
      {
         Participant delegate = delegates.get(i);
         searchResult.add(new ParticipantEntry(delegate, false));
      }
   }

   /**
    * if buildDefaultNotes parameter is set to true,it creates default note on delegation
    * 
    * @param participant
    * @return
    */
   private String buildDefaultNotes(Participant participant)
   {
      SessionContext sessionCtx = SessionContext.findSessionContext();
      User loginUser = sessionCtx != null ? sessionCtx.getUser() : null;
      if (loginUser != null)
      {
         String account = loginUser.getAccount();
         Calendar cal = Calendar.getInstance();
         DateFormat formater = new SimpleDateFormat();
         String timestamp = formater.format(cal.getTime());
         String defaultNote = Localizer.getString(LocalizerKey.DELEGATE_NOTES, "DATE",
               timestamp);

         defaultNote = StringUtils.replace(defaultNote, "FROMUSER", account + " ("
               + I18nUtils.getUserLabel(loginUser) + ")");
         if (participant instanceof User)
         {
            User user = (User) participant;
            defaultNote = StringUtils.replace(defaultNote, "TOPARTICIPANT", user
                  .getAccount());
         }
         else if (participant instanceof ModelParticipant)
         {
            defaultNote = StringUtils.replace(defaultNote, "TOPARTICIPANT", participant
                  .getName());
         }
         return defaultNote;
      }
      return null;
   }

   // ************* Modified Getter and Setter Methods ****************

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.PopupUIComponentBean#openPopup()
    */
   public void openPopup()
   {
      super.openPopup();
      initialize();
      participantTree.resetPreviousSelection();
     // retrieveParticipants();
     // FacesUtils.refreshPage();
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.PopupUIComponentBean#closePopup()
    */
   public void closePopup()
   {
      ICallbackHandler iCH = iCallbackHandler;
      boolean fireClose = fireCloseEvent;

      cleanup();
      super.closePopup();
      
      if (iCH != null && fireClose)
         iCH.handleEvent(EventType.CANCEL);
      
      //For IE - In IE the delegation didn't work the second time
      FacesUtils.refreshPage();
   }

   /**
    * 
    * @return
    */
   public String getDialogTitle()
   {
      if (delegateCase)
      {
         return propsBean.getString("delegation.case.title");
      }
      else
      {
         return propsBean.getString("delegation.title");
      }
   }
   
   /**
    * @return
    */
   public List<ParticipantEntry> getSearchResult()
   {

      return searchResult;
   }

   /**
    * @param type
    */
   public void setTypeFilter(int type)
   {
      typeFilter = type;
   }
   
   /**
    * Value change listener for typeFilter.
    * @param event
    */
   public void typeFilterValueChangeListener(final ValueChangeEvent event)
   {
      handleFilterCriteriaChangeEvent(event);
   }

   /**
    * @param nameFilter
    */
   public void setNameFilter(String nameFilter)
   {
      this.nameFilter = nameFilter;
   }
   
   /**
    * Value change listener for nameFilter.
    * 
    * @param event
    */
   public void nameFilterValueChangeListener(final ValueChangeEvent event)
   {
      handleFilterCriteriaChangeEvent(event);
   }

   // ************* Default Getter and Setter Methods ***********
   /**
    * @return
    */
   public String getNotes()
   {
      return notes;
   }

   /**
    * @param notes
    */
   public void setNotes(String notes)
   {
      this.notes = notes;
   }

   /**
    * @return
    */
   public boolean isApplyButtonDisabled()
   {
      return false;
   }

   /**
    * @return
    */
   public boolean isLimitedSearch()
   {
      return limitedSearch;
   }

   /**
    * @return
    */
   public boolean isAllUserRoles()
   {
      return !limitedSearch;
   }

   /**
    * @param allUserRoles
    */
   public void setAllUserRoles(boolean allUserRoles)
   {
      this.limitedSearch = !allUserRoles;
   }

   /**
    * Value change listener for 'show all users' checkbox.
    * @param event
    */
   public void showAllUserRolesValueChangeListener(final ValueChangeEvent event)
   {
      handleFilterCriteriaChangeEvent(event);
   }
   
   /**
    * @param limitedSearch
    */
   public void setLimitedSearch(boolean limitedSearch)
   {
      this.limitedSearch = limitedSearch;
   }

   /**
    * @return
    */
   public boolean isLimitedSearchEnabled()
   {
      return limitedSearchEnabled;
   }
   
   public boolean isDelegateCase()
   {
      return delegateCase;
   }

   /**
    * @param limitedSearchEnabled
    */
   public void setLimitedSearchEnabled(boolean limitedSearchEnabled)
   {
      this.limitedSearchEnabled = limitedSearchEnabled;
   }

   /**
    * @return
    */
   public boolean isDisableAdministrator()
   {
      return disableAdministrator;
   }

   /**
    * @param disableAdministrator
    */
   public void setDisableAdministrator(boolean disableAdministrator)
   {
      this.disableAdministrator = disableAdministrator;
   }

   /**
    * @return
    */
   public int getTypeFilter()
   {
      return typeFilter;
   }

   /**
    * @return
    */
   public String getNameFilter()
   {
      return nameFilter;
   }

   /**
    * @param delegationHandler
    */
   public void setDefaultDelegationHandler(IDelegationHandler delegationHandler)
   {
      this.delegationHandler = delegationHandler;
   }

   /**
    * @return
    */
   public boolean isNotesEnabled()
   {
      return notesEnabled;
   }

   /**
    * @param notesEnabled
    */
   public void setNotesEnabled(boolean notesEnabled)
   {
      this.notesEnabled = notesEnabled;
   }

   /**
    * @return
    */
   public boolean isBuildDefaultNotes()
   {
      return buildDefaultNotes;
   }

   /**
    * @param buildDefaultNotes
    */
   public void setBuildDefaultNotes(boolean buildDefaultNotes)
   {
      this.buildDefaultNotes = buildDefaultNotes;
   }

   /**
    * @return
    */
   public IDelegatesProvider getDelegatesProvider()
   {
      return delegatesProvider;
   }

   /**
    * @param setStrictMode
    */
   public void setStrictMode(boolean setStrictMode)
   {
      limitedSearch = setStrictMode;
   }

   /**
    * @param delegatesProvider
    */
   public void setDelegatesProvider(IDelegatesProvider delegatesProvider)
   {
      this.delegatesProvider = delegatesProvider;
   }

   /**
    * @return
    */
   public IDepartmentProvider getDepartmentDelegatesProvider()
   {
      return deptProvider;
   }

   /**
    * @param deptProvider
    */
   public void setDepartmentDelegatesProvider(IDepartmentProvider deptProvider)
   {
      this.deptProvider = deptProvider;
   }

   /**
    * @param searchResult
    */
   public void setSearchResult(List<ParticipantEntry> searchResult)
   {
      this.searchResult = searchResult;
   }

   /**
    * @return
    */
   public List<SelectItem> getTypeFilters()
   {
      return typeFilters;
   }

   /**
    * @return
    */
   public List<ActivityInstance> getAis()
   {
      return ais;
   }

   /**
    * @param ais
    */
   public void setAis(List<ActivityInstance> ais)
   {
      this.ais = ais;
   }

   /**
    * @param ai
    */
   public void setAi(ActivityInstance ai)
   {
      if (ai != null)
      {
         this.ais = CollectionUtils.newList();
         this.ais.add(ai);
      }
   }

   /**
    * @param activityOutData
    */
   public void setActivityOutData(Map<String, Serializable> activityOutData)
   {
      this.activityOutData = activityOutData;
   }

   /**
    * @param activityContext
    */
   public void setActivityContext(String activityContext)
   {
      this.activityContext = activityContext;
   }

   /**
    * @return
    */
   public ParticipantEntry getSelectedUser()
   {
      return selectedUser;
   }

   /**
    * @param selectedUser
    */
   public void setSelectedUser(ParticipantEntry selectedUser)
   {
      this.selectedUser = selectedUser;
   }

   /**
    * @param callbackHandler
    */
   public void setICallbackHandler(ICallbackHandler callbackHandler)
   {
      iCallbackHandler = callbackHandler;
   }
   
   /**
    * 
    * @param delegateCase
    */
   public void setDelegateCase(boolean delegateCase)
   {
      this.delegateCase = delegateCase;
   }

   /**
    * ParticipantEntry user object class
    */
   public static class ParticipantEntry implements Serializable,Comparable<ParticipantEntry>
   {
      private final static long serialVersionUID = 1l;

      private Participant participant;

      private DepartmentInfo department;

      private boolean isRole;

      private boolean isOrganization;

      private boolean selected;

      private String type;
      
      private String label;

      public ParticipantEntry(Participant participant)
      {
         this.participant = participant;
         if (participant instanceof Role)
         {
            isRole = true;
            type = "delegation.role";
         }
         else if (participant instanceof Organization)
         {
            isOrganization = true;
            type = "delegation.organization";
         }
         else
         {
            type = "delegation.user";
         }
         this.label = getLabel();
      }

      public ParticipantEntry(Participant participant, boolean select)
      {
         this(participant);
         this.selected = select;      
      }

      public ParticipantEntry(DepartmentInfo dept)
      {
         this.department = dept;
         this.type = "delegation.department";
         this.label = getLabel();
      }

      public boolean isReferencingParticipant()
      {
         return participant != null;
      }

      public boolean isReferencingDepartment()
      {
         return department != null;
      }

      public DepartmentInfo getDepartment()
      {
         return department;
      }

      public void setDepartment(DepartmentInfo department)
      {
         this.department = department;
      }

      public boolean isRole()
      {
         return isRole;
      }

      public boolean isOrganization()
      {
         return isOrganization;
      }

      public Participant getParticipant()
      {
         return participant;
      }
      
      public String getLabel()
      {
         if (isReferencingParticipant())
         {
            if (isRole() || isOrganization())
            {
               return I18nUtils.getParticipantName(participant);
            }
            else if (participant instanceof User)
            {
               return UserUtils.getUserDisplayLabel((User) participant);
            }
         }
         else if (isReferencingDepartment())
         {
            if (department instanceof DepartmentDetails)
            {
               DepartmentDetails deptDetail = (DepartmentDetails) department;
               if (null != deptDetail.getOrganization() && deptDetail.getOrganization().isDepartmentScoped())
               {
                  StringBuilder departmentName = new StringBuilder();
                  departmentName.append(I18nUtils.getParticipantName(deptDetail.getOrganization()));
                  return departmentName.append(" - ").append(department.getName()).toString();
               }

            }
            return department.getName();
         }
         return "";
      }

      public String getName()
      {
        return label;
      }

      public boolean isSelected()
      {
         return selected;
      }

      public void setSelected(boolean selected)
      {
         this.selected = selected;
      }

      public String getType()
      {
         return type;
      }

      public void setType(String type)
      {
         this.type = type;
      }
      
      public int compareTo(ParticipantEntry other)
      {
         return getLabel().compareTo(other.getLabel());
      }

      
   }
   
   /**
    * @return
    */
   public boolean isSelectVisible()
   {
      return isVisible() && DELEGATION_MODE.PICK_FROM_LIST.equals(delegationMode);
   }
   
   /**
    * @return
    */
   public boolean isSearchVisible()
   {
      return isVisible() && DELEGATION_MODE.SEARCH_PARTICIPANTS.equals(delegationMode);
   }
   
   /**
    * @return
    */
   public boolean isTreeVisible()
   {
      return isVisible() && DELEGATION_MODE.PICK_FROM_TREE.equals(delegationMode);
   }
   
   public void setSelectMode()
   {
      delegationMode = DELEGATION_MODE.PICK_FROM_LIST;
   }
   
   public void setSearchMode()
   {
      delegationMode = DELEGATION_MODE.SEARCH_PARTICIPANTS;
   }
   
   public void setPickFromTreeMode()
   {
      delegationMode = DELEGATION_MODE.PICK_FROM_TREE;
   }

   public ParticipantTree getParticipantTree()
   {
      return participantTree;
   }

   /**
    * @return
    */
   public ParticipantAutocompleteSelector getAutoCompleteSelector()
   {
      return autoCompleteSelector;
   }

   /**
    * Checks of the given event is in 'Invoke application' phase. If not, sets the phase
    * Id to invoke application. If present, then invokes the retrieveFilteredData
    * function.
    * 
    * @param event
    *           - given event
    */
   private void handleFilterCriteriaChangeEvent(final FacesEvent event)
   {
      if (!event.getPhaseId().equals(javax.faces.event.PhaseId.INVOKE_APPLICATION))
      {
         event.setPhaseId(javax.faces.event.PhaseId.INVOKE_APPLICATION);
         event.queue();
         return;
      }

      if ((DELEGATION_MODE.PICK_FROM_LIST.equals(delegationMode)) || oldlimitedSearch != limitedSearch)
      {
         retrieveParticipants();         
         oldlimitedSearch=limitedSearch;
      }
   }
   
   /**
    * @return
    */
   private Options getDelegateProviderOptions()
   {
      return new IDelegatesProvider.Options()
      {
         /*
          * (non-Javadoc)
          * 
          * @see org.eclipse.stardust.ui.web.viewscommon.dialogs.IDelegatesProvider.Options#getPerformerTypes()
          */
         public Set<Integer> getPerformerTypes()
         {
            if (USER_TYPE == typeFilter || ROLE_TYPE == typeFilter || ORGANIZATION_TYPE == typeFilter)
            {
               if (DelegationBean.this.isDisableAdministrator())
               {
                  Set<Integer> result = CollectionUtils.newSet();
                  result.add(new Integer(typeFilter));
                  result.add(IDelegatesProvider.DISABLE_ADMINISTRATOR_ROLE);

                  return Collections.unmodifiableSet(result);
               }
               return Collections.singleton(new Integer(typeFilter));
            }
            else
            {
               // ALL_TYPES

               // TODO provide constant for this
               Set<Integer> result = CollectionUtils.newSet();
               result.add(IDelegatesProvider.USER_TYPE);
               result.add(IDelegatesProvider.ROLE_TYPE);
               result.add(IDelegatesProvider.ORGANIZATION_TYPE);

               if (DelegationBean.this.isDisableAdministrator())
               {
                  result.add(IDelegatesProvider.DISABLE_ADMINISTRATOR_ROLE);
               }

               return Collections.unmodifiableSet(result);
            }
         }

         /*
          * (non-Javadoc)
          * 
          * @see org.eclipse.stardust.ui.web.viewscommon.dialogs.IDelegatesProvider.Options#isStrictSearch()
          */
         public boolean isStrictSearch()
         {
            return limitedSearch;
         }

         /*
          * (non-Javadoc)
          * 
          * @see org.eclipse.stardust.ui.web.viewscommon.dialogs.IDelegatesProvider.Options#getNameFilter()
          */
         public String getNameFilter()
         {
            if (DELEGATION_MODE.PICK_FROM_LIST.equals(delegationMode))
            {
               return nameFilter;
            }
            else
            {
               return autoCompleteSelector.getSearchValue();
            }
         }
      };
   }

   /**
    * @return
    */
   private IDepartmentProvider.Options getDepartmentOptions()
   {
      return new IDepartmentProvider.Options()
      {

         public String getNameFilter()
         {
            if (DELEGATION_MODE.PICK_FROM_LIST.equals(delegationMode))
            {
               return nameFilter;
            }
            else
            {
               return autoCompleteSelector.getSearchValue();
            }
         }

         public boolean isStrictSearch()
         {
            return limitedSearch;
         }
      };
   }  
}
