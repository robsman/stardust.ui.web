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

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.eclipse.stardust.engine.api.runtime.UserGroup;
import org.eclipse.stardust.engine.api.runtime.UserGroupExistsException;
import org.eclipse.stardust.engine.api.runtime.UserService;
import org.eclipse.stardust.ui.web.admin.AdminportalConstants;
import org.eclipse.stardust.ui.web.admin.WorkflowFacade;
import org.eclipse.stardust.ui.web.admin.messages.AdminMessagesPropertiesBean;
import org.eclipse.stardust.ui.web.common.PopupUIComponentBean;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.common.validator.DateValidator;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.ICallbackHandler;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.ICallbackHandler.EventType;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;



/**
 * @author Ankita.Patel
 * @version $Revision: $
 */
public class CreateOrModifyUserGroupBean extends PopupUIComponentBean
{

   public static final String EDIT_NONE = "none";

   public static final String EDIT_ALL = "all";

   private String editMode;

   private boolean modifyMode;

   private UserGroupBean userGroup;

   private UserGroup selectedUserGroup;

   private WorkflowFacade workflowFacade;

   private ICallbackHandler iCallbackHandler;
   
   private AdminMessagesPropertiesBean propsBean;
   
   private String validationMessage;

   /**
    * 
    */
   public CreateOrModifyUserGroupBean()
   {
      workflowFacade = (WorkflowFacade) SessionContext.findSessionContext().lookup(
            AdminportalConstants.WORKFLOW_FACADE);
      propsBean = AdminMessagesPropertiesBean.getInstance();

   }

   /**
    * create or modifies user group as per edit mode and selected user group
    */
   public void apply()
   {
      if(FacesContext.getCurrentInstance().getMessages().hasNext())
         return;
      try
      {
         if (!DateValidator.validInputDate(userGroup.getValidFrom(), userGroup
               .getValidTo()))
         {
            validationMessage = propsBean.getString("views.userGroupMgmt.invalidDate");
            return;
         }
         UserService service = workflowFacade.getServiceFactory().getUserService();
         if (!modifyMode)
         {
            if (service != null && userGroup != null)
            {
               service.createUserGroup(userGroup.getId(), userGroup.getName(), userGroup
                     .getDescription(), userGroup.getValidFrom(), userGroup.getValidTo());
            }
         }
         else
         {
            if (selectedUserGroup != null && userGroup != null)
            {
               selectedUserGroup.setDescription(userGroup.getDescription());
               selectedUserGroup.setName(userGroup.getName());
               selectedUserGroup.setValidFrom(userGroup.getValidFrom());
               selectedUserGroup.setValidTo(userGroup.getValidTo());
               service.modifyUserGroup(selectedUserGroup);
            }
         }
      }
      catch (UserGroupExistsException e)
      {
         validationMessage = propsBean.getParamString("views.userGroupMgmt.notifyUserGroupExistMsg", userGroup.getId());
         return;
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }

      closePopup();
      setICallbackHandler(UserGroupManagementBean.getCurrent());
      if (iCallbackHandler != null)
         iCallbackHandler.handleEvent(EventType.APPLY);
   }

   /**
    * opens user group dialog and gets selected user group data
    * @param ae
    */
   public void openUserGroupDialog(ActionEvent ae)
   {
      UIComponent source = ae.getComponent();
      Object obj = source.getAttributes().get("editable");
      this.editMode = (obj instanceof String) ? (String) obj : EDIT_NONE;

      selectedUserGroup = (UserGroup) source.getAttributes().get("userGroup");
      if (selectedUserGroup != null)
      {
         modifyMode = true;
         this.userGroup = new UserGroupBean(selectedUserGroup);
      }
      else
      {
         modifyMode = false;
         this.userGroup = new UserGroupBean();
      }
      validationMessage = null;
      super.openPopup();
   }

   //********************* Default Getter and Setter Methods *******************
   private void setICallbackHandler(ICallbackHandler callbackHandler)
   {
      iCallbackHandler = callbackHandler;
   }

   public String getId()
   {
      return userGroup.getId();
   }

   public void setId(String id)
   {
      userGroup.setId(id);
   }

   public String getDescription()
   {
      return userGroup.getDescription();
   }

   public void setDescription(String description)
   {
      userGroup.setDescription(description);
   }

   public String getName()
   {
      return userGroup.getName();
   }

   public void setName(String name)
   {
      userGroup.setName(name);
   }

   public Date getValidFrom()
   {
      return userGroup.getValidFrom();
   }

   public void setValidFrom(Date validFrom)
   {
      userGroup.setValidFrom(validFrom);
   }

   public Date getValidTo()
   {
      return userGroup.getValidTo();
   }

   public void setValidTo(Date validTo)
   {
      userGroup.setValidTo(validTo);
   }

   public boolean isGlobalEnable()
   {
      return EDIT_ALL.equals(editMode);
   }

   public boolean isModifyMode()
   {
      return modifyMode;
   }

   @Override
   public void initialize()
   {}
   
   public String getValidationMessage()
   {
      return validationMessage;
   }
}
