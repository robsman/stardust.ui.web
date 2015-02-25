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
 * @author Subodh.Godbole
 * @version $Revision: $
 */
@DTOClass
public class DescriptorColumnDTO extends AbstractDTO
{
   @DTOAttribute("columnName")
   public String id;
   
   @DTOAttribute("columnTitle")
   public String title;

   @DTOAttribute("columnDataType.name()")
   public String type;

   @DTOAttribute("sortable")
   public Boolean sortable;

   public Boolean filterable;

   public DescriptorColumnDTO(String id, String title, String type, Boolean sortable,
         Boolean filterable)
   {
      super();
      this.id = id;
      this.title = title;
      this.type = type;
      this.sortable = sortable;
      this.filterable = filterable;
   }

}
