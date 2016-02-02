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
package org.eclipse.stardust.ui.web.rest.dto.response;

import java.util.List;

import org.eclipse.stardust.ui.web.rest.dto.AbstractDTO;
import org.eclipse.stardust.ui.web.rest.dto.ResourcePolicyDTO;

/**
 * @author Yogesh.Manware
 * @version $Revision: $
 */
public class ResourcePolicyContainerDTO extends AbstractDTO
{
   public List<ResourcePolicyDTO> own; // TODO: sort it based on participant name
   public List<ResourcePolicyDTO> ineherited;// TODO: sort it based on participant name
}
