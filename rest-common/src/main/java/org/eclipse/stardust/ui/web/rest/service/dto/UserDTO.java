/*******************************************************************************
 * Copyright (c) 2013 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
/**
 * @author Abhay.Thappan
 */
package org.eclipse.stardust.ui.web.rest.service.dto;

import java.util.List;

import org.eclipse.stardust.ui.web.rest.service.dto.common.DTOAttribute;

/**
 * 
 * @author Johnson.Quadras
 *
 */
public class UserDTO extends AbstractDTO
{
   @DTOAttribute("id")
   public String id;

   @DTOAttribute("qualifiedId")
   public String qualifiedId;

   @DTOAttribute("name")
   public String displayName;

   public Long validFrom;

   public Long validTo;

   @DTOAttribute("description")
   public String description;

   @DTOAttribute("eMail")
   public String eMail;

   @DTOAttribute("realm.name")
   public String realm;

   public String userImageURI;

   @DTOAttribute("firstName")
   public String firstName;

   @DTOAttribute("lastName")
   public String lastName;

   @DTOAttribute("OID")
   public Long oid;

   @DTOAttribute("realm.id")
   public String realmId;

   @DTOAttribute("administrator")
   public Boolean isAdministrator;

   @DTOAttribute("account")
   public String account;

   public boolean isInternalAuthentication;

   public String oldPassword;

   public String password;

   public String confirmPassword;

   public boolean changePassword;

   public Integer qaOverride;

   public List<SelectItemDTO> allRealms;

   public String selectedDisplayFormat;

}
