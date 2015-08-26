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
package org.eclipse.stardust.ui.web.rest.service.dto.response;

import java.util.List;

import org.eclipse.stardust.ui.web.rest.service.dto.AbstractDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.DocumentDTO;

/**
 * @author Yogesh.Manware
 * 
 * @version $Revision: $
 */
public class FolderDTO extends AbstractDTO
{
   public String uuid;
   public String name;
   public String path;
   public boolean hasChildren = false;

   public List<FolderDTO> folders;
   public List<DocumentDTO> documents;
   public UiPropertiesDTO uiProperties;
   public CorrespondenceMetaDataDTO correspondenceMetaDataDTO;

   public FolderDTO()
   {

   }
}
