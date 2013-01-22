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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.security.InvalidPasswordException;
import org.eclipse.stardust.engine.api.dto.QualityAssuranceAdminServiceFacade;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.query.PreferenceQuery;
import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.api.runtime.UserExistsException;
import org.eclipse.stardust.engine.api.runtime.UserRealm;
import org.eclipse.stardust.engine.api.runtime.UserService;
import org.eclipse.stardust.engine.core.preferences.PreferenceScope;
import org.eclipse.stardust.engine.core.preferences.Preferences;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.ui.web.common.PopupUIComponentBean;
import org.eclipse.stardust.ui.web.common.configuration.UserPreferencesHelper;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogActionType;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogContentType;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogStyle;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogType;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialogHandler;
import org.eclipse.stardust.ui.web.common.message.MessageDialog;
import org.eclipse.stardust.ui.web.common.util.CollectionUtils;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.common.util.MessagePropertiesBean;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.common.MyPicturePreferenceBean;
import org.eclipse.stardust.ui.web.viewscommon.common.configuration.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.viewscommon.common.validator.DateValidator;
import org.eclipse.stardust.ui.web.viewscommon.core.EMailAddressValidator;
import org.eclipse.stardust.ui.web.viewscommon.core.UserDefinedException;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.ICallbackHandler;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.ICallbackHandler.EventType;
import org.eclipse.stardust.ui.web.viewscommon.login.util.PasswordUtils;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ServiceFactoryUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.UserUtils;



/**
 * @author Yogesh.Manware
 * 
 */
public class UserProfileBean extends PopupUIComponentBean implements ConfirmationDialogHandler
{
   private static final long serialVersionUID = 1L;
   private static final String BEAN_NAME = "userProfileBean";
   private static final String PREFERENCE_ID = "preference";
   private static enum OperationType {
      MODIFY_PROFILE_CONFIGURATION, CREATE_USER, MODIFY_USER, COPY_USER
   }

   private OperationType operationType = OperationType.MODIFY_PROFILE_CONFIGURATION;
   private String headerTitle;
   private String account;
   private String firstName;
   private String lastName;
   // This is defined as 'B'oolean deliberately to get rid of Icefaces issue of (sometimes)
   // sending 'null' to server if the checkbox is disabled
   private Boolean changePassword;  
   private String oldPassword;
   private String password;
   private String confirmPassword;
   private String realmId;
   private String email;
   private Date validFrom;
   private Date validTo;
   private String description;
   private Integer qaOverride;
   private List<SelectItem> displayFormats;
   private String selectedDisplayFormat;

   private MyPicturePreferenceBean myPicturePreference;
   private User user;

   private ICallbackHandler callbackHandler;

   private String validationMsg;
   private String passwordValidationMsg;
   private boolean isInternalAuthentication;
   private int focusIndex = 0;
   private String emailValidationMsg;
   private ConfirmationDialog userProfileConfirmationDlg;

   /**
    * 
    */
   public UserProfileBean()
   {
      user = SessionContext.findSessionContext().getUser();
      focusIndex = 0;
      initializeView();
   }

   /**
    * @return
    */
   public static UserProfileBean getInstance()
   {
      return (UserProfileBean) org.eclipse.stardust.ui.web.common.util.FacesUtils.getBeanFromContext(BEAN_NAME);
   }

   /**
    * Create user dialog
    */
   public void openCreateUserDialog()
   {
      operationType = OperationType.CREATE_USER;
      initializeView();
      openPopup();
   }

   /**
    * Copy user dialog
    * 
    * @param ae
    */
   public void openCopyUserDialog(User user)
   {
      operationType = OperationType.COPY_USER;
      this.user = user;
      initializeView();
      openPopup();
   }

   /**
    * Modify User dialog
    * 
    * @param ae
    */
   public void openModifyUserDialog(ActionEvent ae)
   {
      operationType = OperationType.MODIFY_USER;
      UIComponent source = ae.getComponent();
      Object obj = source.getAttributes().get("user");
      this.user = (User) obj;
      initializeView();
      openPopup();
   }

   /**
    * saves userBean
    */
   public void onApply()
   {
      boolean success = true;
      passwordValidationMsg = null;
      validationMsg = null;
      MessagesViewsCommonBean propsBean = MessagesViewsCommonBean.getInstance();
      User newUser = null;
      if (FacesContext.getCurrentInstance().getMessages().hasNext())
         return;
      try
      {
         if (!DateValidator.validInputDate(getValidFrom(), getValidTo()))
         {
            validationMsg = propsBean.getString("views.createUser.invalidDate");
            return;
         }
         if (!validatePassword(getPassword()))
         {
            return;
         }

         if (isCreateMode() || isCopyMode())
         {
         // Validate From email address
            if (StringUtils.isNotEmpty(getEmail()) && !EMailAddressValidator.validateEmailAddress(getEmail()))
            {
               emailValidationMsg = propsBean.getString("views.createUser.invalideEmailAddress");
               success = false;
            }
            else
            {
               newUser = createUser();
               if (isCopyMode())
               {
                  newUser = UserUtils.copyGrantsAndUserGroups(user, newUser);// new user
               }
               updateUserDisplayFormatProperty(newUser);
 
               if (null != qaOverride)
               {
                  UserService userService = UserUtils.getUserService();
                  newUser.setQualityAssuranceProbability(qaOverride);
                  newUser = userService.modifyUser(newUser);
               }
            }
         }
         else if (isModifyMode())
         {
            // Validate email address
            if (StringUtils.isNotEmpty(getEmail()) && !getEmail().equals(user.getEMail())
                  && !EMailAddressValidator.validateEmailAddress(getEmail()))
            {
               emailValidationMsg = propsBean.getString("views.createUser.invalideEmailAddress");
               success = false;
            }
            else
            {
               newUser = modifyUser(user);
               updateUserDisplayFormatProperty(newUser);
            }
         }
         else if (isModifyProfileConfiguration())
         {
            if (!myPicturePreference.isSelectedImageValid())
            {
               return;
            }
            newUser = modifyLoginUser();
            myPicturePreference.save();
            MessageDialog.addInfoMessage(propsBean.getString("common.configuration.saveConfirmation"));
         }
      }
      catch (InvalidPasswordException e)
      {
         success = false;
         String errMessages = PasswordUtils.decodeInvalidPasswordMessage(e, null);
         if (StringUtils.isNotEmpty(errMessages))
         {
            passwordValidationMsg = errMessages;
         }
         else
         {
            passwordValidationMsg = e.toString();
         }
      }
      catch (UserExistsException e)
      {
         success = false;
         validationMsg = propsBean.getParamString("views.createUser.userExistException", getAccount());
      }
      catch (PublicException e)
      {
         success = false;
         validationMsg = ExceptionHandler.getExceptionMessage(e);
      }

      if (success)
      {
         if (!isModifyProfileConfiguration())
         {
            user = newUser;
            closePopup();
         }

         validationMsg = null;
         if (callbackHandler != null)
            callbackHandler.handleEvent(EventType.APPLY);
      }
   }

   /**
    * 
    */
   private void initializeView()
   {
      MessagesViewsCommonBean propsBean = MessagesViewsCommonBean.getInstance();
      isInternalAuthentication = SecurityProperties.isInternalAuthentication();
      oldPassword = "";
      password = "";
      confirmPassword = "";
      validationMsg = null;
      passwordValidationMsg = null;
      emailValidationMsg = null;
      if (isCreateMode() || isCopyMode())
      {
         if (isCreateMode())
         {
            headerTitle = propsBean.getString("views.createUser.title");
         }
         else
         {
            headerTitle = propsBean.getParamString("views.copyUser.title", I18nUtils.getUserLabel(user));
         }
         changePassword = true;
         account = "";
         firstName = "";
         lastName = "";
         email = "";
         validFrom = null;
         validTo = null;
         description = "";
         QualityAssuranceAdminServiceFacade qualityAssuranceAdminService = ServiceFactoryUtils
               .getQualityCheckAdminServiceFacade();
         qaOverride = qualityAssuranceAdminService.getQualityAssuranceUserDefaultProbability();
         if (isCopyMode() && null != user)
         {
            realmId = user.getRealm().getId();
            qaOverride = user.getQualityAssuranceProbability(); 
         }
         else
         {
            setDefaultRealm();
         }
      }
      else if ((isModifyMode() || isModifyProfileConfiguration()) && null != user)
      {
         UserService userService = ServiceFactoryUtils.getUserService();
         Long userOid = user.getOID();
         if ((userOid != null) && (userService != null))
         {
            user = userService.getUser(userOid.longValue());
         }
         headerTitle = propsBean.getString("views.modifyUser.title");
         changePassword = false;
         account = user.getAccount();
         firstName = user.getFirstName();
         lastName = user.getLastName();
         realmId = user.getRealm().getId();
         email = user.getEMail();
         validFrom = user.getValidFrom();
         validTo = user.getValidTo();
         description = user.getDescription();
         qaOverride = user.getQualityAssuranceProbability();
         if (null != user.getProperty(UserUtils.USER_NAME_DISPLAY_FORMAT_PREF_ID))
         {
            initUserDisplayPreference();
         }
         if (isModifyProfileConfiguration())
         {
            myPicturePreference = new MyPicturePreferenceBean(user);
         }
      }
      
      initDisplayFormats();
   }
   
   /**
    * Read the user display format preference
    */
   private void initUserDisplayPreference()
   {
      Serializable value = null;
      QueryService queryService = SessionContext.findSessionContext().getServiceFactory().getQueryService();
      List<Preferences> prefs = queryService.getAllPreferences(PreferenceQuery.findPreferencesForUsers(user.getRealm()
            .getId(), user.getId(), UserPreferencesEntries.M_ADMIN_PORTAL, PREFERENCE_ID));
      for (Preferences userPref : prefs)
      {
         value = userPref.getPreferences().get(UserUtils.USER_NAME_DISPLAY_FORMAT_PREF_ID);
      }
      if (value != null)
      {
         user.setProperty(UserUtils.USER_NAME_DISPLAY_FORMAT_PREF_ID, value);
      }
   }

   /**
    * sets Default realm id
    */
   private void setDefaultRealm()
   {
      String defaultRealm = Parameters.instance().getString(SecurityProperties.DEFAULT_REALM,
            PredefinedConstants.DEFAULT_REALM_ID);

      UserService userService = ServiceFactoryUtils.getUserService();
      List<UserRealm> realms = userService.getUserRealms();

      if (defaultRealm != null && defaultRealm.trim().length() > 0)
      {
         for (UserRealm userRealm : realms)
         {
            if (userRealm.getId().compareTo(defaultRealm) == 0)
            {
               this.realmId = userRealm.getId();
               break;
            }
         }
      }
   }

   /**
    * Validate the entered password
    * 
    * @param password
    * @return
    */
   private boolean validatePassword(String password)
   {
      boolean success = true;

      if (getChangePassword())
      {
         String passwordConfirmation = getConfirmPassword();
         if (StringUtils.isEmpty(passwordConfirmation) || StringUtils.isEmpty(password))
         {
            passwordValidationMsg = MessagesViewsCommonBean.getInstance().getString("views.createUser.password.empty");
            success = false;
         }
         else if (passwordConfirmation.compareTo(password) != 0)
         {
            passwordValidationMsg = MessagesViewsCommonBean.getInstance().getString(
                  "views.createUser.password.mismatch");
            success = false;
         }
      }
      return success;
   }

   /**
    * @param userBean
    * @return
    */
   private User createUser()
   {
      UserService userService = UserUtils.getUserService();
      if (userService != null)
      {
         User user = userService.createUser(getRealmId(), getAccount(), getFirstName(), getLastName(),
               getDescription(), getPassword(), getEmail(), getValidFrom(), getValidTo());
         return user;
      }
      return null;
   }

   /**
    * @param userBean
    * @param userToModify
    * @return
    */
   private User modifyUser(User userToModify)
   {
      User modifiedUser = null;
      UserService userService = UserUtils.getUserService();
      if (null != userToModify && null != userService)
      {
         SessionContext ctx = SessionContext.findSessionContext();
         User loggedInUser = ctx != null ? ctx.getUser() : null;
         boolean passwordChanged = this.getChangePassword();
         if (passwordChanged)
         {
            userToModify.setPassword(getPassword());
         }
         userToModify.setFirstName(getFirstName());
         userToModify.setLastName(getLastName());
         userToModify.setDescription(getDescription());
         userToModify.setEMail(getEmail());
         userToModify.setValidFrom(getValidFrom());
         userToModify.setValidTo(getValidTo());
         userToModify.setQualityAssuranceProbability(getQaOverride());
         modifiedUser = userService.modifyUser(userToModify);
         if (modifiedUser != null && modifiedUser.equals(loggedInUser))
         {
            if (passwordChanged)
            {
               UserUtils.updateServiceFactory(getAccount(), getPassword());
            }
            UserUtils.updateLoggedInUser();
         }
      }
      return modifiedUser;
   }

   private User modifyLoginUser()
   {
      UserService userService = UserUtils.getUserService();
      User modifiedUser = null;
      if (null != userService)
      {
         SessionContext ctx = SessionContext.findSessionContext();
         User loggedInUser = ctx != null ? ctx.getUser() : null;
         String newPassword = getChangePassword() ? getPassword() : getOldPassword();
         modifiedUser = userService.modifyLoginUser(getOldPassword(), getFirstName(), getLastName(), newPassword,
               getEmail());
         if (modifiedUser != null && modifiedUser.equals(loggedInUser))
         {
            if (getChangePassword())
            {
               UserUtils.updateServiceFactory(getAccount(), getPassword());
            }
            UserUtils.updateLoggedInUser();
         }
      }
      return modifiedUser;
   }

   // *********************** Modified Getter and Setter Methods
   public SelectItem[] getAllRealms()
   {
      UserService userService = ServiceFactoryUtils.getUserService();
      List<UserRealm> realms = userService.getUserRealms();
      SelectItem[] allRealms = new SelectItem[realms.size() + 1];
      allRealms[0] = new SelectItem("");
      int count = 1;
      for (UserRealm realm : realms)
      {
         allRealms[count] = new SelectItem(realm.getId());
         count++;
      }
      return allRealms;
   }

   public boolean isPasswordEnabled()
   {
      if ((isModifyMode() && isUserEditable()) || isModifyProfileConfiguration())
      {
         return true;
      }
      else
      {
         return false;
      }
   }

   public boolean isModifyProfileConfiguration()
   {
      if (operationType.equals(OperationType.MODIFY_PROFILE_CONFIGURATION))
      {
         return true;
      }
      return false;
   }

   public boolean isCreateMode()
   {
      if (operationType == OperationType.CREATE_USER)
      {
         return true;
      }
      else
      {
         return false;
      }
   }

   public boolean isCopyMode()
   {
      if (operationType == OperationType.COPY_USER)
      {
         return true;
      }
      else
      {
         return false;
      }
   }

   public boolean isModifyMode()
   {
      if (operationType == OperationType.MODIFY_USER)
      {
         return true;
      }
      else
      {
         return false;
      }
   }

   public boolean isUserEditable()
   {
      return isInternalAuthentication;
   }

   /**
    * 
    */
   public void resetConfiguration()
   {
      FacesUtils.clearFacesTreeValues();
      initializeView();
      myPicturePreference.reset();
      MessageDialog.addInfoMessage(MessagesViewsCommonBean.getInstance().getString("views.common.config.reset"));
   }
   
   /**
    * Confirmation Dialog before reseting Config data.
    */
   public void openConfirmationDialog()
   {
      userProfileConfirmationDlg = new ConfirmationDialog(DialogContentType.WARNING, DialogActionType.YES_NO,
            DialogType.NORMAL, DialogStyle.COMPACT, this);
      MessagePropertiesBean propsBean = MessagePropertiesBean.getInstance();
      userProfileConfirmationDlg.setTitle(propsBean.getString("common.configurationPanel.confirmResetTitle"));
      userProfileConfirmationDlg.setMessage(propsBean.getParamString("common.configurationPanel.confirmReset",
            MessagesViewsCommonBean.getInstance().getString("views.userProfileView.labelTitle")));
      userProfileConfirmationDlg.openPopup();
   }

   /**
    * 
    */
   public boolean accept()
   {
      resetConfiguration();
      userProfileConfirmationDlg = null;
      return false;
   }

   /**
    * 
    */
   public boolean cancel()
   {
      userProfileConfirmationDlg = null;
      return false;
   }
   
   // ***************** Default Getter and Setter Methods ***********************
   public String getHeaderTitle()
   {
      return headerTitle;
   }

   public User getUser()
   {
      return user;
   }

   public String getValidationMsg()
   {
      return validationMsg;
   }

   public void setValidationMsg(String validationMsg)
   {
      this.validationMsg = validationMsg;
   }

   public String getPasswordValidationMsg()
   {
      return passwordValidationMsg;
   }

   public MyPicturePreferenceBean getMyPicturePreference()
   {
      return myPicturePreference;
   }

   public void setICallbackHandler(ICallbackHandler callbackHandler)
   {
      this.callbackHandler = callbackHandler;
   }

   public String getAccount()
   {
      return account;
   }

   public void setAccount(String account)
   {
      if (isCreateMode() || isCopyMode())
      {
         this.account = account;
      }
   }

   public String getFirstName()
   {
      return firstName;
   }

   public void setFirstName(String firstName)
   {
      this.firstName = firstName;
   }

   public String getLastName()
   {
      return lastName;
   }

   public void setLastName(String lastName)
   {
      this.lastName = lastName;
   }

   public void nameChangeListener(ValueChangeEvent event)
   {
      if (!event.getPhaseId().equals(javax.faces.event.PhaseId.INVOKE_APPLICATION))
      {
         event.setPhaseId(javax.faces.event.PhaseId.INVOKE_APPLICATION);
         event.queue();
         return;
      }
      else
      {
         initDisplayFormats();
      }
   }
   
   public String getPassword()
   {
      return password;
   }

   public void setPassword(String password)
   {
      this.password = password;
   }

   public String getConfirmPassword()
   {
      return confirmPassword;
   }

   public void setConfirmPassword(String confirmPassword)
   {
      this.confirmPassword = confirmPassword;
   }

   public String getEmail()
   {
      return email;
   }

   public void setEmail(String email)
   {
      this.email = email;
   }

   public Date getValidFrom()
   {
      return validFrom;
   }

   public void setValidFrom(Date validFrom)
   {
      this.validFrom = validFrom;
   }

   public Date getValidTo()
   {
      return validTo;
   }

   public void setValidTo(Date validTo)
   {
      this.validTo = validTo;
   }

   public String getDescription()
   {
      return description;
   }

   public void setDescription(String description)
   {
      this.description = description;
   }

   public String getOldPassword()
   {
      return oldPassword;
   }

   public void setOldPassword(String oldPassword)
   {
      this.oldPassword = oldPassword;
   }

   public Boolean getChangePassword()
   {
      return changePassword;
   }

   public void setChangePassword(Boolean changePassword)
   {
      if (isPasswordEnabled())
      {
         this.changePassword = changePassword;
      }
   }

   public List<SelectItem> getDisplayFormats()
   {
      return displayFormats;
   }

   public void initDisplayFormats()
   {
      this.displayFormats = new ArrayList<SelectItem>();
      if (StringUtils.isNotEmpty(getFirstName()) && StringUtils.isNotEmpty(getLastName())) {
         displayFormats.add(new SelectItem(UserUtils.USER_NAME_DISPLAY_FORMAT_0, UserUtils.formatUserName(getFirstName(), getLastName(), getAccount(), UserUtils.USER_NAME_DISPLAY_FORMAT_0)));
         displayFormats.add(new SelectItem(UserUtils.USER_NAME_DISPLAY_FORMAT_1, UserUtils.formatUserName(getFirstName(), getLastName(), getAccount(), UserUtils.USER_NAME_DISPLAY_FORMAT_1)));
         displayFormats.add(new SelectItem(UserUtils.USER_NAME_DISPLAY_FORMAT_2, UserUtils.formatUserName(getFirstName(), getLastName(), getAccount(), UserUtils.USER_NAME_DISPLAY_FORMAT_2)));
      }
      initNameDisplayFormat(user);
   }

   public String getSelectedDisplayFormat()
   {      
      return selectedDisplayFormat;
   }

   public void setSelectedDisplayFormat(String selectedDisplayFormat)
   {
      this.selectedDisplayFormat = selectedDisplayFormat;
   }

   public String getRealmId()
   {
      return realmId;
   }

   public void setRealmId(String realmId)
   {
      if (isCreateMode() || isCopyMode())
      {
         this.realmId = realmId;
      }
   }

   public int getFocusIndex()
   {
      return focusIndex;
   }

   public void setFocusIndex(int index)
   {
      focusIndex = index;
   }

   public String getEmailValidationMsg()
   {
      return emailValidationMsg;
   }
   
   @Override
   public void initialize()
   {}
   
   /**
    * Updates the 'username display format' preference for the given user.
    *  
    * @param userToModify
    */
   private void updateUserDisplayFormatProperty(User userToModify)
   {
      QueryService qService = SessionContext.findSessionContext().getServiceFactory().getQueryService();
      List<Preferences> prefs = qService.getAllPreferences(PreferenceQuery.findPreferencesForUsers(userToModify
            .getRealm().getId(), userToModify.getId(), UserPreferencesEntries.M_ADMIN_PORTAL, PREFERENCE_ID));
      if (CollectionUtils.isEmpty(prefs))
      {
         Map<String, Serializable> prefMap = new HashMap<String, Serializable>();
         prefMap.put(UserUtils.USER_NAME_DISPLAY_FORMAT_PREF_ID, selectedDisplayFormat);
         Preferences newPref = new Preferences(PreferenceScope.USER, UserPreferencesEntries.M_ADMIN_PORTAL,
               PREFERENCE_ID, prefMap);
         newPref.setRealmId(userToModify.getRealm().getId());
         newPref.setUserId(userToModify.getId());
         prefs.add(newPref);
      }
      else
      {
         for (Preferences pref : prefs)
         {
            Map<String, Serializable> pMap = pref.getPreferences();
            if (CollectionUtils.isEmpty(pMap))
            {
               pMap = new HashMap<String, Serializable>();
            }
            pMap.put(UserUtils.USER_NAME_DISPLAY_FORMAT_PREF_ID, selectedDisplayFormat);
            pref.setPreferences(pMap);
         }
      }

      AdministrationService adminService = SessionContext.findSessionContext().getServiceFactory()
            .getAdministrationService();
      adminService.savePreferences(prefs);
   }
   
   /**
    * @param user
    */
   private void initNameDisplayFormat(User user)
   {
      if (isCreateMode() || isCopyMode())
      {
         selectedDisplayFormat = UserUtils.getDefaultUserNameDisplayFormat();
      }
      else
      {
         if (null != user.getProperty(UserUtils.USER_NAME_DISPLAY_FORMAT_PREF_ID))
         {
            selectedDisplayFormat = (String) user.getProperty(UserUtils.USER_NAME_DISPLAY_FORMAT_PREF_ID);
         }
         else
         {
            selectedDisplayFormat = UserUtils.getDefaultUserNameDisplayFormat();
         }
      }
   }
   
   public Integer getQaOverride()
   {
      return qaOverride;
   }

   public void setQaOverride(Integer qaOverride)
   {
      this.qaOverride = qaOverride;
   }

   public ConfirmationDialog getUserProfileConfirmationDlg()
   {
      return userProfileConfirmationDlg;
   }
   
   

}