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
public class DataPathDTO extends AbstractDTO
{
   @DTOAttribute("id")
   public String id;
   
   @DTOAttribute("qualifiedId")
   public String qualifiedId;

   @DTOAttribute("direction.id")
   public String direction;

   @DTOAttribute("mappedType.name")
   public String type;

   @DTOAttribute("isDescriptor")
   public Boolean isDescriptor;

   @DTOAttribute("isKeyDescriptor")
   public Boolean isKeyDescriptor;

   @DTOAttribute("accessPath")
   public String accessPath;

   @DTOAttribute("data")
   public String data;
   
   public String name;
}
