/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *	SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.rest.component.service;

import java.util.List;

import org.eclipse.stardust.ui.web.rest.dto.ResourcePolicyDTO;
import org.eclipse.stardust.ui.web.rest.dto.response.ResourcePolicyContainerDTO;

/**
 * @author Yogesh.Manware
 * @version $Revision: $
 */
public interface ResourcePolicyService
{

   /**
    * @param resourceId
    * @param isFolder
    * @return
    */
   public ResourcePolicyContainerDTO getPolicy(String resourceId, boolean isFolder);

   /**
    * @param resourceId
    * @param resourcePolicies
    * @param isFolder
    */
   public void savePolicy(String resourceId, List<ResourcePolicyDTO> resourcePolicies, boolean isFolder);
}
