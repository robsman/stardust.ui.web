/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Anoop.Nair (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.rest.service.dto;

import org.eclipse.stardust.ui.web.rest.service.dto.common.DTOAttribute;
import org.eclipse.stardust.ui.web.rest.service.dto.common.DTOClass;

/**
 * @author Subodh.Godbole
 * @version $Revision: $
 */
@DTOClass
public class DaemonDTO extends AbstractDTO
{
   @DTOAttribute("type")
   public String type;
   
   @DTOAttribute("startTime.time")
   public Long startTime;
   
   @DTOAttribute("lastExecutionTime.time")
   public Long lastExecutionTime;
   
   @DTOAttribute("running")
   public Boolean running;
   
   @DTOAttribute("acknowledgementState.name")
   public String acknowledgementState;
   
   @DTOAttribute("daemonExecutionState.name")
   public String daemonExecutionState;
}
