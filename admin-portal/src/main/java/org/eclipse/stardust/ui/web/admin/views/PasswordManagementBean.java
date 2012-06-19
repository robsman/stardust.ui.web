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
import java.util.List;

import javax.faces.model.SelectItem;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.engine.api.dto.PasswordRulesDetails;
import org.eclipse.stardust.engine.api.runtime.PasswordRules;
import org.eclipse.stardust.ui.web.admin.AdminportalConstants;
import org.eclipse.stardust.ui.web.admin.ResourcePaths;
import org.eclipse.stardust.ui.web.admin.WorkflowFacade;
import org.eclipse.stardust.ui.web.common.UIComponentBean;
import org.eclipse.stardust.ui.web.common.event.ViewEvent;
import org.eclipse.stardust.ui.web.common.event.ViewEventHandler;
import org.eclipse.stardust.ui.web.common.event.ViewEvent.ViewEventType;
import org.eclipse.stardust.ui.web.common.message.MessageDialog;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.GenericPopup;



/**
 * @author Ankita.Patel
 * @version $Revision: $
 */
public class PasswordManagementBean extends UIComponentBean implements  ViewEventHandler
{
   private static final long serialVersionUID = 1L;
   public static int EXPIRATION_TIME_MIN = 1;
   public static int EXPIRATION_TIME_MAX = 999;
   public static int EXPIRATION_TIME_DEFAULT = 90;

   public static int SEND_NOTIFICATION_MAILS_MIN = 0;
   public static int SEND_NOTIFICATION_MAILS_MAX = 999;
   public static int SEND_NOTIFICATION_MAILS_DEFAULT = 3;

   public int DISABLE_ACCOUNT_MIN = -1;
   public int DISABLE_ACCOUNT_MAX = 999;

   private static int MINIMUM_PASSWORD_LENGTH_DEFAULT = 6;

   private int lowerCaseLetters = 0;
   private int upperCaseLetters = 0;

   private List<SelectItem> minPwdLengths = new ArrayList<SelectItem>();
   private List<SelectItem> allLetters = new ArrayList<SelectItem>();
   private List<SelectItem> mixedCaseLetters = new ArrayList<SelectItem>();
   private List<SelectItem> previousPwds = new ArrayList<SelectItem>();
   private List<SelectItem> minCharDifferences = new ArrayList<SelectItem>();

   private PasswordRules passwordRules;

   /**
    * 
    */
   public PasswordManagementBean()
   {
      super("passwordMgmt");
     
   }

   @Override
   public void initialize()
   {
      retrievePasswordRules();

      for (int i = 4; i <= 32; i++)
      {
         minPwdLengths.add(new SelectItem(new Integer(i), new String("" + i + "")));
      }
      
      for(int i=0; i<= 10; i++)
      {
         previousPwds.add(new SelectItem(new Integer(i), new String("" + i + "")));
      }

      calculateMinCharDiff();
      
      for(int i=0; i<=6; i++)
      {
         allLetters.add(new SelectItem(new Integer(i), new String("" + i + "")));
      }
      
      for(int i=0; i<=6; i++)
      {
         mixedCaseLetters.add(new SelectItem(new Integer(i), new String("" + i + "")));
      }
   }

   public void handleEvent(ViewEvent event)
   {
       if (ViewEventType.CREATED == event.getType())
       {        
           initialize();
           
     
       }
   }
   
   public void save()
   {
      WorkflowFacade workflowFacade = (WorkflowFacade) SessionContext
            .findSessionContext().lookup(AdminportalConstants.WORKFLOW_FACADE);
      if (isPasswordEncrypted())
      {
         GenericPopup genericPopup = GenericPopup.getCurrent();
         genericPopup.setIncludePath(ResourcePaths.V_ConfirmEncyprtPwd);
         genericPopup.openPopup();
      }
      workflowFacade.getServiceFactory().getAdministrationService().setPasswordRules(
            passwordRules);

      MessageDialog.addInfoMessage(getMessages().getString("saveSuccess"));
   }

   /**
    * If password encrypted is selected
    */
   public void enablePasswordEncrypt()
   {
      GenericPopup genericPopup = GenericPopup.getCurrent();
      genericPopup.closePopup();
      WorkflowFacade workflowFacade = (WorkflowFacade) SessionContext
            .findSessionContext().lookup(AdminportalConstants.WORKFLOW_FACADE);
      workflowFacade.getServiceFactory().getAdministrationService().setPasswordRules(
            passwordRules);
   }

   public void closePopup()
   {
      GenericPopup genericPopup = GenericPopup.getCurrent();
      genericPopup.closePopup();
   }

   /**
    * 
    */
   private void retrievePasswordRules()
   {
      WorkflowFacade workflowFacade = (WorkflowFacade) SessionContext
            .findSessionContext().lookup(AdminportalConstants.WORKFLOW_FACADE);
      if (workflowFacade.getServiceFactory().getAdministrationService()
            .getPasswordRules() != null)
      {
         passwordRules = workflowFacade.getServiceFactory().getAdministrationService()
               .getPasswordRules();
      }
      else
      {
         passwordRules = new PasswordRulesDetails();
         setDefaults();
      }
   }

   /**
    * 
    */
   private void setDefaults()
   {
      setMinimalPasswordLength(MINIMUM_PASSWORD_LENGTH_DEFAULT);
      setPasswordTracking(0);
      setDifferentCharacters(0);
      setDigits(0);
      setLetters(0);
      setMixedCase(0);
      setPunctuations(0);
      setExpirationTime(EXPIRATION_TIME_DEFAULT);
      setSendNotificationMails(SEND_NOTIFICATION_MAILS_DEFAULT);
   }

   /**
    * 
    */
   private void calculateMinCharDiff()
   {
      minCharDifferences = new ArrayList<SelectItem>();

      for (int i = 0; i <= getMinimalPasswordLength(); i++)
      {
         minCharDifferences.add(new SelectItem(new Integer(i), new String("" + i + "")));
      }
   }

   // **************** Getter & Setter Methods********************
   public boolean isPasswordEncrypted()
   {
      // Uncomment the line,Once the Security.Password.Encryption property will be added
      // in carnot.properties.
      /*
       * return Parameters.instance().getString("Security.Password.Encryption")
       * .equalsIgnoreCase("true") ? true : false;
       */
      return Parameters.instance().getBoolean("Security.Password.Encryption", false);
   }

   public boolean isStrongPassword()
   {
      return passwordRules.isStrongPassword();
   }

   public void setStrongPassword(boolean strongPassword)
   {
      passwordRules.setStrongPassword(strongPassword);
   }

   public int getMinimalPasswordLength()
   {
      return passwordRules.getMinimalPasswordLength();
   }

   public void setMinimalPasswordLength(int minimalPasswordLength)
   {
      passwordRules.setMinimalPasswordLength(minimalPasswordLength);
      calculateMinCharDiff();
   }

   public int getLetters()
   {
      return passwordRules.getLetters();
   }

   public void setLetters(int letters)
   {
      passwordRules.setLetters(letters);
   }

   public List<SelectItem> getMinPwdLengths()
   {
      return minPwdLengths;
   }

   public void setMinPwdLengths(List<SelectItem> minPwdLengths)
   {
      this.minPwdLengths = minPwdLengths;
   }

   public List<SelectItem> getAllLetters()
   {
      return allLetters;
   }

   public void setAllLetters(List<SelectItem> allLetters)
   {
      this.allLetters = allLetters;
   }

   public int getLowerCaseLetters()
   {
      return lowerCaseLetters;
   }
   
   public List<SelectItem> getMixedCaseLetters()
   {
      return mixedCaseLetters;
   }

   public void setMixedCaseLetters(List<SelectItem> mixedCaseLetters)
   {
      this.mixedCaseLetters = mixedCaseLetters;
   }

   public void setLowerCaseLetters(int lowerCaseLetters)
   {
      this.lowerCaseLetters = lowerCaseLetters;
   }

   public int getUpperCaseLetters()
   {
      return upperCaseLetters;
   }

   public void setUpperCaseLetters(int upperCaseLetters)
   {
      this.upperCaseLetters = upperCaseLetters;
   }

   public void setMixedCase(int length)
   {
      passwordRules.setMixedCase(length);
   }

   public int getMixedCase()
   {
      return passwordRules.getMixedCase();
   }

   public int getDigits()
   {
      return passwordRules.getDigits();
   }

   public void setDigits(int digits)
   {
      passwordRules.setDigits(digits);
   }

   public int getPunctuations()
   {
      return passwordRules.getPunctuation();
   }

   public void setPunctuations(int punctuations)
   {
      passwordRules.setPunctuation(punctuations);
   }

   public boolean isUniquePassword()
   {
      return passwordRules.isUniquePassword();
   }

   public void setUniquePassword(boolean uniquePassword)
   {
      passwordRules.setUniquePassword(uniquePassword);
   }

   public int getDifferentCharacters()
   {
      return passwordRules.getDifferentCharacters();
   }

   public void setDifferentCharacters(int differentCharacters)
   {
      passwordRules.setDifferentCharacters(differentCharacters);
   }

   public int getPasswordTracking()
   {
      return passwordRules.getPasswordTracking();
   }

   public void setPasswordTracking(int passwordTracking)
   {
      passwordRules.setPasswordTracking(passwordTracking);
   }

   public boolean isPeriodicPwdChange()
   {
      return passwordRules.isForcePasswordChange();
   }

   public void setPeriodicPwdChange(boolean periodicPwdChange)
   {
      passwordRules.setForcePasswordChange(periodicPwdChange);
   }

   public Integer getSendNotificationMails()
   {
      return passwordRules.getNotificationMails();
   }

   public void setSendNotificationMails(Integer sendNotificationMails)
   {
      if (sendNotificationMails != null)
      {
         passwordRules.setNotificationMails(sendNotificationMails);
      }
      else
      // When null set to Min Value
      {
         passwordRules.setNotificationMails(0);
      }
   }

   public Integer getExpirationTime()
   {
      return passwordRules.getExpirationTime();
   }

   public void setExpirationTime(Integer expirationTime)
   {
      if (expirationTime != null)
      {
         passwordRules.setExpirationTime(expirationTime);
      }
      else
      // When null set to Min Value
      {
         passwordRules.setExpirationTime(1);
      }
   }

   public void setDisableUserTime(Integer days)
   {
      if (days != null)
      {
         passwordRules.setDisableUserTime(days);
      }
      else
      {
         passwordRules.setDisableUserTime(-1);
      }
   }

   public Integer getDisableUserTime()
   {
      return passwordRules.getDisableUserTime();
   }

   public List<SelectItem> getPreviousPwds()
   {
      return previousPwds;
   }

   public void setPreviousPwds(List<SelectItem> previousPwds)
   {
      this.previousPwds = previousPwds;
   }

   public int getEXPIRATION_TIME_MIN()
   {
      return EXPIRATION_TIME_MIN;
   }

   public int getEXPIRATION_TIME_MAX()
   {
      return EXPIRATION_TIME_MAX;
   }

   public int getSEND_NOTIFICATION_MAILS_MIN()
   {
      return SEND_NOTIFICATION_MAILS_MIN;
   }

   public int getSEND_NOTIFICATION_MAILS_MAX()
   {
      return SEND_NOTIFICATION_MAILS_MAX;
   }

   public int getDISABLE_ACCOUNT_MIN()
   {
      return DISABLE_ACCOUNT_MIN;
   }

   public int getDISABLE_ACCOUNT_MAX()
   {
      return DISABLE_ACCOUNT_MAX;
   }

   public List<SelectItem> getMinCharDifferences()
   {
      return minCharDifferences;
   }

   public void setMinCharDifferences(List<SelectItem> minCharDifferences)
   {
      this.minCharDifferences = minCharDifferences;
   }
   

}
