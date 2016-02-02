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
package org.eclipse.stardust.ui.web.rest.dto;

import org.eclipse.stardust.ui.web.rest.dto.core.DTOAttribute;
import org.eclipse.stardust.ui.web.rest.dto.core.DTOClass;

/**
 * 
 * @author Johnson.Quadras
 *
 */
@DTOClass
public class PasswordRulesDTO extends AbstractDTO
{
   
   public boolean passwordEncrption;
   
   @DTOAttribute("strongPassword")
   public Boolean isStrongPassword;
   
   @DTOAttribute("forcePasswordChange")
   public Boolean isForcePasswordChange;
   
   @DTOAttribute("uniquePassword")
   public Boolean isUniquePassword;
   
   @DTOAttribute("differentCharacters")
   public Integer differentCharacters;

   @DTOAttribute("digits")
   public Integer digits;

   @DTOAttribute("disableUserTime")
   public Integer disableUserTime;

   @DTOAttribute("expirationTime")
   public Integer expirationTime;

   @DTOAttribute("letters")
   public Integer letters;

   @DTOAttribute("minimalPasswordLength")
   public Integer minimalPasswordLength;

   @DTOAttribute("mixedCase")
   public Integer mixedCase;

   @DTOAttribute("notificationMails")
   public Integer notificationMails;

   @DTOAttribute("passwordTracking")
   public Integer passwordTracking;

   @DTOAttribute("punctuation")
   public Integer symbols;
   
   public boolean rulesAvailable;

}
