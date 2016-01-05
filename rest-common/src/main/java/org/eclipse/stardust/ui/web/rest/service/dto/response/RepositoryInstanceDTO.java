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
package org.eclipse.stardust.ui.web.rest.service.dto.response;

import org.eclipse.stardust.ui.web.rest.service.dto.AbstractDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.common.DTOAttribute;

/**
 * @author Yogesh.Manware
 * @version $Revision: $
 */
public class RepositoryInstanceDTO extends AbstractDTO
{
   @DTOAttribute("getProviderId()")
   public String providerId;

   @DTOAttribute("getRepositoryId()")
   public String id;

   @DTOAttribute("getRepositoryName()")
   public String name;

   @DTOAttribute("getRepositoryVersion()")
   public String version;

   @DTOAttribute("getRepositoryType()")
   public String type;

   public boolean isDefualt = false;

   public RepositoryInstanceDTO()
   {}
}
