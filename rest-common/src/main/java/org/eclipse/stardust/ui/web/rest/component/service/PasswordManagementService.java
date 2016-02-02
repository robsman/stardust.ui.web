/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
/**
 * @author Johnson.Quadras
 */
package org.eclipse.stardust.ui.web.rest.component.service;

import javax.annotation.Resource;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.engine.api.dto.PasswordRulesDetails;
import org.eclipse.stardust.engine.api.runtime.PasswordRules;
import org.eclipse.stardust.ui.web.rest.component.util.ServiceFactoryUtils;
import org.eclipse.stardust.ui.web.rest.dto.PasswordRulesDTO;
import org.eclipse.stardust.ui.web.rest.dto.builder.DTOBuilder;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class PasswordManagementService
{

   @Resource
   private ServiceFactoryUtils serviceFactoryUtils;

   /**
    * Get the password Rules
    * 
    * @return
    */
   public PasswordRulesDTO getPasswordRules()
   {
      PasswordRules passwordRules = serviceFactoryUtils.getAdministrationService().getPasswordRules();
      PasswordRulesDTO passwordRulesDTO = new PasswordRulesDTO();
      if (null != passwordRules)
      {
         passwordRulesDTO = DTOBuilder.build(passwordRules, PasswordRulesDTO.class);
         passwordRulesDTO.rulesAvailable = true;

      }
      passwordRulesDTO.passwordEncrption = Parameters.instance().getBoolean("Security.Password.Encryption", false);
      return passwordRulesDTO;
   }

   /**
    * Save the password Rules
    * 
    * @param passwordRulesDTO
    */
   public void savePasswordRules(PasswordRulesDTO passwordRulesDTO)
   {
      PasswordRules passwordRules = getPasswordRules(passwordRulesDTO);
      serviceFactoryUtils.getAdministrationService().setPasswordRules(passwordRules);
   }

   /**
    * 
    * @param passwordRulesDTO
    * @return
    */
   private PasswordRules getPasswordRules(PasswordRulesDTO passwordRulesDTO)
   {
      PasswordRules passwordRules = new PasswordRulesDetails();
      passwordRules.setDifferentCharacters(passwordRulesDTO.differentCharacters);
      passwordRules.setDigits(passwordRulesDTO.digits);
      passwordRules.setDisableUserTime(passwordRulesDTO.disableUserTime);
      passwordRules.setExpirationTime(passwordRulesDTO.expirationTime);
      passwordRules.setForcePasswordChange(passwordRulesDTO.isForcePasswordChange);
      passwordRules.setLetters(passwordRulesDTO.letters);
      passwordRules.setMinimalPasswordLength(passwordRulesDTO.minimalPasswordLength);
      passwordRules.setMixedCase(passwordRulesDTO.mixedCase);
      passwordRules.setNotificationMails(passwordRulesDTO.notificationMails);
      passwordRules.setPasswordTracking(passwordRulesDTO.passwordTracking);
      passwordRules.setPunctuation(passwordRulesDTO.symbols);
      passwordRules.setStrongPassword(passwordRulesDTO.isStrongPassword);
      passwordRules.setUniquePassword(passwordRulesDTO.isUniquePassword);
      return passwordRules;
   }
}
