/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
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
 * @author Subodh.Godbole
 * @version $Revision: $
 *
 */
@DTOClass
public class LaunchPanelDTO
{
   @DTOAttribute("name")
   public String name;

   @DTOAttribute("include")
   public String include;

   @DTOAttribute("definedIn")
   public String definedIn;

   @DTOAttribute("global")
   public boolean global;
}
