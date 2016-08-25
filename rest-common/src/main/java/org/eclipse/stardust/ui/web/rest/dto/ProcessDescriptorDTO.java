/*
 * $Id$
 * (C) 2000 - 2016 CARNOT AG
 */
package org.eclipse.stardust.ui.web.rest.dto;

import org.eclipse.stardust.ui.web.rest.dto.core.DTOAttribute;

public class ProcessDescriptorDTO
{
   @DTOAttribute("id")
   public String id;
   
   @DTOAttribute("type")
   public String type;
   
   @DTOAttribute("changedValue")
   public Object changedValue;
   
   @DTOAttribute("hideTime")
   public boolean hideTime;
   
   @DTOAttribute("useServerTimeZone")
   public boolean useServerTimeZone;
}
