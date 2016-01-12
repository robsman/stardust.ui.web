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
package org.eclipse.stardust.ui.web.rest.service.dto.request;

import java.io.Serializable;
import java.util.Map;

import org.eclipse.stardust.engine.extensions.dms.data.DocumentType;

/**
 * @author Yogesh.Manware
 * @version $Revision: $
 */
public class DocumentInfoDTO
{
   public String name;
   public String comments;
   public String description;
   public byte[] content;
   public String contentType;
   
   public String parentFolderPath;
   
   public DocumentType documentType; // evaluated on server side considering modelId and
                                     // typedDocumentId
   
   public Map<String, Serializable> properties; // used to store documentType values
   
   public String dataPathId;
}