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

import java.io.Serializable;
import java.util.Map;

import org.eclipse.stardust.ui.web.rest.service.dto.common.DTOAttribute;
import org.eclipse.stardust.ui.web.rest.service.dto.common.DTOClass;

/**
 * @author Anoop.Nair
 * @version $Revision: $
 */
@DTOClass
public class DocumentDTO extends AbstractDTO
{
   @DTOAttribute("id")
   public String uuid;

   @DTOAttribute("name")
   public String name;

   public String contentType;

   public String path;

   public int numPages;

   @DTOAttribute("getProperties()")
   public Map<String, Serializable> properties;

   public DocumentTypeDTO documentType;

   public DocumentDTO()
   {

   }
}
