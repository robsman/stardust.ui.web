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
package org.eclipse.stardust.ui.web.rest.dto.response;

import org.eclipse.stardust.ui.web.rest.dto.AbstractDTO;
import org.eclipse.stardust.ui.web.rest.dto.core.DTOAttribute;

/**
 * @author Yogesh.Manware
 * @version $Revision: $
 */
public class RepositoryProviderDTO extends AbstractDTO
{
   @DTOAttribute("getProviderId()")
   public String id;
   
   @DTOAttribute("getProviderName()")
   public String name;
   
   @DTOAttribute("isWriteSupported()")
   public boolean isWriteSupported;
   
   @DTOAttribute("isFullTextSearchSupported()")
   public boolean isFullTextSearchSupported;
   
   @DTOAttribute("isMetaDataSearchSupported()")
   public boolean isMetaDataSearchSupported;
   
   @DTOAttribute("isMetaDataWriteSupported()")
   public boolean isMetaDataWriteSupported;
   
   @DTOAttribute("isTransactionSupported()")
   public boolean isTransactionSupported;
   
   @DTOAttribute("isVersioningSupported()")
   public boolean isVersioningSupported;
   
   @DTOAttribute("isAccessControlPolicySupported()")
   public boolean isAccessControlPolicySupported;
   
   public RepositoryProviderDTO()
   {}
}
