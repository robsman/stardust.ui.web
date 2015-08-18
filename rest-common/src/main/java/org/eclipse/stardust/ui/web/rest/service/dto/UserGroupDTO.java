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

package org.eclipse.stardust.ui.web.rest.service.dto;

import org.eclipse.stardust.ui.web.rest.service.dto.common.DTOAttribute;
import org.eclipse.stardust.ui.web.rest.service.dto.common.DTOClass;

/**
 * @author Aditya.Gaikwad
 * @version $Revision: $
 */

@DTOClass
public class UserGroupDTO extends AbstractDTO
{

   @DTOAttribute("id")
   public String id;

   @DTOAttribute("OID")
   public long oid;

   @DTOAttribute("name")
   public String name;

   @DTOAttribute("validFrom.time")
   public Long validFrom;

   @DTOAttribute("validTo.time")
   public Long validTo;

   @DTOAttribute("description")
   public String description;

}
