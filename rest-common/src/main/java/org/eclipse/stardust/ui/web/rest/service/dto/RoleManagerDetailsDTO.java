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
 * @author Abhay.Thappan
 */
package org.eclipse.stardust.ui.web.rest.service.dto;

import java.util.List;

import org.eclipse.stardust.ui.web.bcc.jsf.UserItem;
import org.eclipse.stardust.ui.web.rest.service.dto.common.DTOClass;

@DTOClass
public class RoleManagerDetailsDTO extends AbstractDTO
{
   public String roleName;

   public String roleId;

   public String items;

   public String account;

   public String itemsPerUser;

   public boolean roleModifiable;

   public List<RoleManagerDetailUserDTO> assignedUserList;

   public List<RoleManagerDetailUserDTO> assignableUserList;
}
