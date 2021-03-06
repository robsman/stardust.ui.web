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

package org.eclipse.stardust.ui.web.modeler.portal;

import java.util.ArrayList;
import java.util.List;

import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.ui.web.common.PopupUIComponentBean;
import org.eclipse.stardust.ui.web.modeler.service.ModelService;

public class InviteParticipantsDialog extends PopupUIComponentBean
{
   private static final long serialVersionUID = 1L;

   private ModelService modelService;

   private String filterString;

   private boolean notifyViaEmail;

   private boolean notifyViaAlert;

   private List<String> selectedUserAccounts;
   
   private List<SelectItem> notInvitedUsers;

   public InviteParticipantsDialog()
   {
      super();
      initialize();
   }

   @Override
   public void openPopup()
   {
      List<User> userList = getModelService().getNotInvitedUsers();
      notInvitedUsers = CollectionUtils.newArrayList();
      for (User user : userList)
      {
         notInvitedUsers.add(new SelectItem(user.getAccount(), user.getFirstName() + " " + user.getLastName() + " ("
               + user.getAccount() + ")"));
      }
      super.openPopup();
   }
   public ModelService getModelService()
   {
      return modelService;
   }

   public void setModelService(ModelService modelService)
   {
      this.modelService = modelService;
   }

   /**
    *
    * @return
    */
   public String getFilterString()
   {
      return filterString;
   }

   /**
    *
    * @param filterString
    */
   public void setFilterString(String filterString)
   {
      this.filterString = filterString;
   }

   /**
    *
    * @return
    */
   public List<SelectItem> getNotInvitedUsers()
   {
     return notInvitedUsers;
   }

   /**
    *
    * @param event
    */
   public void userChanged(ValueChangeEvent event)
   {
      selectedUserAccounts.clear();

      for (String account : (String[]) event.getNewValue())
      {
         selectedUserAccounts.add(account);
      }
   }

   /**
    *
    * @return
    */
   public boolean isNotifyViaEmail()
   {
      return notifyViaEmail;
   }

   /**
    *
    * @param notifyViaEmail
    */
   public void setNotifyViaEmail(boolean notifyViaEmail)
   {
      this.notifyViaEmail = notifyViaEmail;
   }

   /**
    *
    * @return
    */
   public boolean isNotifyViaAlert()
   {
      return notifyViaAlert;
   }

   /**
    *
    * @param notifyViaAlert
    */
   public void setNotifyViaAlert(boolean notifyViaAlert)
   {
      this.notifyViaAlert = notifyViaAlert;
   }

   @Override
   public void initialize()
   {
      selectedUserAccounts = new ArrayList<String>();
      notInvitedUsers = new ArrayList<SelectItem>();
   }
}
