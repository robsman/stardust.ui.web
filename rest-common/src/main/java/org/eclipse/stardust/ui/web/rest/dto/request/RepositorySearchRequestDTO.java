/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *	SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.rest.dto.request;

import java.util.Date;
import java.util.List;

import org.eclipse.stardust.ui.web.rest.dto.AbstractDTO;
import org.eclipse.stardust.ui.web.rest.dto.DataTableOptionsDTO;

/**
 * @author Yogesh.Manware
 * @version $Revision: $
 */
public class RepositorySearchRequestDTO extends AbstractDTO
{
   public enum Search_Type {
      Both, Document, Folder
   }

   public String id;
   public String name = "";

   public DataTableOptionsDTO documentDataTableOption;

   public String searchType = Search_Type.Both.name();

   // Document search Options
   public Date dateCreatedFrom;
   public Date dateCreateTo;
   public Date dateLastModifiedFrom;
   public Date dateLastModifiedTo;
   public String owner;
   public List<String> contentTypeIn;
   public String contentTypeLike;
   public List<String> documentTypeIdIn;
   public List<String> repositoryIn;
   public String contentLike;
   public String metaDataLike;
}
