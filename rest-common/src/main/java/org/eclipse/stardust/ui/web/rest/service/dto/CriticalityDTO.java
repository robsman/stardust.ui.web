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
package org.eclipse.stardust.ui.web.rest.service.dto;

import org.eclipse.stardust.ui.web.rest.service.dto.common.DTOAttribute;
import org.eclipse.stardust.ui.web.viewscommon.common.criticality.CriticalityConfigurationUtil.ICON_COLOR;

/**
 * 
 * @author Johnson.Quadras
 *
 */
public class CriticalityDTO extends AbstractDTO
{

   public int value;

   @DTOAttribute("iconColor")
   public String color;

   @DTOAttribute("label")
   public String label;

   @DTOAttribute("iconCount")
   public int count;

   @DTOAttribute("rangeFrom")
   public int rangeFrom;

   @DTOAttribute("rangeTo")
   public int rangeTo;

   public void setColor(ICON_COLOR iconColor)
   {
      this.color = iconColor.toString();
   }

}
