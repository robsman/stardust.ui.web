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
package org.eclipse.stardust.ui.web.rest.dto;

import java.util.HashMap;
import java.util.List;

import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.ui.web.rest.dto.core.DTOAttribute;
import org.eclipse.stardust.ui.web.rest.dto.core.DTOClass;

/**
 * @author Anoop.Nair
 * @author Yogesh.Manware
 * 
 * @version $Revision: $
 */
@DTOClass
public class DocumentDTO extends AbstractDTO
{
   @DTOAttribute("id")
   public String uuid;

   @DTOAttribute("name")
   public String name;

   @DTOAttribute("size")
   public long size;
   
   @DTOAttribute("revisionComment")
   public String comment;
   
   @DTOAttribute("description")
   public String description;
   
   @DTOAttribute("contentType")
   public String contentType;

   @DTOAttribute("encoding")
   public String encoding;
   
   @DTOAttribute("revisionId")
   public String revisionId;
   
   @DTOAttribute("revisionName")
   public String revisionName;
   
   @DTOAttribute("versionLabels")
   public List<String> versionLabels;
 
   @DTOAttribute("owner")
   public String owner;
   
   public UserDTO ownerDetails;
   
   @DTOAttribute("dateCreated.time")
   public Long dateCreated;
   
   @DTOAttribute("dateLastModified.time")
   public Long dateLastModified;
   
   @DTOAttribute("path")
   public String path;
   
   @DTOAttribute("repositoryId")
   public String repositoryId;

   @DTOAttribute("getProperties()")
   public HashMap<String, Object> properties;

   public String downloadToken;
   
   public DocumentTypeDTO documentType;

   public List<Pair<String, String>> documentData;
   
   public String attachmentType = "process"; // or activity
   
   public DocumentDTO()
   {

   }
}
