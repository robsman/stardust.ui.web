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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.eclipse.stardust.engine.api.dto.QualityAssuranceAdminServiceFacade;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.ui.web.admin.messages.AdminMessagesPropertiesBean;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialogHandler;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogActionType;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogContentType;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogStyle;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogType;
import org.eclipse.stardust.ui.web.common.message.MessageDialog;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.common.util.MessagePropertiesBean;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.utils.ServiceFactoryUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.UserUtils;



/**
 * @author Shrikant.Gangal
 * 
 */
public class ParticipantMgmtConfigurationBean implements ConfirmationDialogHandler
{
   private List<SelectItem> displayFormats;

   private User user = SessionContext.findSessionContext().getUser();

   private String selectedDisplayFormat;

   private AdminMessagesPropertiesBean msgBean = AdminMessagesPropertiesBean.getInstance();

   private Integer qaOverride = null;

   private QualityAssuranceAdminServiceFacade qualityAssuranceAdminService;
   
   private ConfirmationDialog participantConfirmationDilaog;

   /**
    * 
    */
   public ParticipantMgmtConfigurationBean()
   {
      init();
   }

   /**
    * 
    */
   public void init()
   {
      String i18FirstName = msgBean.getString("views.createUser.basicPanel.firstName.label");
      String i18LastName = msgBean.getString("views.createUser.basicPanel.lastName.label");
      String i18AccId = msgBean.getString("views.createUser.basicPanel.id.label");
      displayFormats = new ArrayList<SelectItem>();
      displayFormats.add(new SelectItem(UserUtils.USER_NAME_DISPLAY_FORMAT_0, UserUtils.formatUserName(i18FirstName,
            i18LastName, i18AccId, UserUtils.USER_NAME_DISPLAY_FORMAT_0)));
      displayFormats.add(new SelectItem(UserUtils.USER_NAME_DISPLAY_FORMAT_1, UserUtils.formatUserName(i18FirstName,
            i18LastName, i18AccId, UserUtils.USER_NAME_DISPLAY_FORMAT_1)));
      displayFormats.add(new SelectItem(UserUtils.USER_NAME_DISPLAY_FORMAT_2, UserUtils.formatUserName(i18FirstName,
            i18LastName, i18AccId, UserUtils.USER_NAME_DISPLAY_FORMAT_2)));

      selectedDisplayFormat = UserUtils.getDefaultUserNameDisplayFormat();
      
      qualityAssuranceAdminService = ServiceFactoryUtils.getQualityCheckAdminServiceFacade();
      qaOverride = qualityAssuranceAdminService.getQualityAssuranceUserDefaultProbability();
   }

   /**
    * @return
    */
   public List<SelectItem> getDisplayFormats()
   {
      return displayFormats;
   }

   /**
    * @return
    */
   public String getSelectedDisplayFormat()
   {
      return selectedDisplayFormat;
   }

   /**
    * @param item
    */
   public void setSelectedDisplayFormat(String item)
   {
      selectedDisplayFormat = item;
   }

   /**
    * @return
    */
   public String getFormattedCurrentUserName()
   {
      return UserUtils.formatUserName(user, selectedDisplayFormat);
   }

   /**
    * 
    */
   public void save()
   {
      UserUtils.saveDefaultUserNameDisplayFormat(getSelectedDisplayFormat());
      qualityAssuranceAdminService.setQualityAssuranceUserDefaultProbability(qaOverride);

      MessageDialog.addInfoMessage(msgBean.getString("views.participantManagement.configuration.saveMsg"));
   }

   /**
    * 
    */
   public void reset()
   {
      qualityAssuranceAdminService.setQualityAssuranceUserDefaultProbability(null);
      FacesUtils.clearFacesTreeValues();
      init();
      MessageDialog.addInfoMessage(msgBean.getString("views.participantManagement.configuration.resetMsg"));
   }
   
   /**
    * Open confirmation dialog prior to reset value.
    */
   public void openConfirmationDialog()
   {
      FacesContext facesContext = FacesContext.getCurrentInstance();
      Iterator<String> facesMessageIds = facesContext.getClientIdsWithMessages();
      while (facesMessageIds.hasNext())
      {
         // Clears the Error messages on the Page
         FacesUtils.clearFacesComponentValues(FacesUtils.matchComponentInHierarchy(FacesContext.getCurrentInstance()
               .getViewRoot(), facesMessageIds.next()));
      }
      participantConfirmationDilaog = new ConfirmationDialog(DialogContentType.WARNING, DialogActionType.YES_NO,
            DialogType.NORMAL, DialogStyle.COMPACT, this);
      MessagePropertiesBean propsBean = MessagePropertiesBean.getInstance();
      participantConfirmationDilaog.setTitle(propsBean.getString("common.configurationPanel.confirmResetTitle"));
      participantConfirmationDilaog.setMessage(propsBean.getParamString("common.configurationPanel.confirmReset",
            AdminMessagesPropertiesBean.getInstance().getString("views.participantMgmt.label")));
      participantConfirmationDilaog.openPopup();
   }
   
   /**
    * 
    */
   public boolean accept()
   {
      reset();
      participantConfirmationDilaog = null;
      return true;
   }

   /**
    * 
    */
   public boolean cancel()
   {
      participantConfirmationDilaog = null;
      return true;
   }
   
   public Integer getQaOverride()
   {
      return qaOverride;
   }

   public void setQaOverride(Integer qaOverride)
   {
      this.qaOverride = qaOverride;
   }

   public ConfirmationDialog getParticipantConfirmationDilaog()
   {
      return participantConfirmationDilaog;
   }
   
   
}
