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
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils;

/**
 * 
 * @author Johnson.Quadras
 *
 */
public class PriorityDTO extends AbstractDTO
{

   @DTOAttribute("processInstance.priority")
   public int value;

   @DTOAttribute("processInstance.priority")
   public String name;

   @DTOAttribute("processInstance.priority")
   public String label;

   public void setName(Integer value)
   {
      this.name = ProcessInstanceUtils.getPriorityValue(value);
   }

   public void setLabel(Integer value)
   {
      this.label = ProcessInstanceUtils.getPriorityLabel(value);
   }
}
