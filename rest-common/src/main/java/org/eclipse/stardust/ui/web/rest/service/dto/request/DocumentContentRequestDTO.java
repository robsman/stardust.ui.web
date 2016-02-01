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

import org.eclipse.stardust.engine.extensions.dms.data.DocumentType;
import org.eclipse.stardust.ui.web.rest.service.dto.DocumentDTO;

/**
 * @author Yogesh.Manware
 * @version $Revision: $
 */
public class DocumentContentRequestDTO extends DocumentDTO
{
   public String dataPathId;
   public String parentFolderPath;
   public DocumentType documentType;
   
   public byte[] contentBytes;
   
   public String content;
   public String contentBase64; // kept as alternate to send data
   
   public boolean createVersion = true; // to indicate user wants to create a version if
                                        // the document already exist with the same name
                                        // and when he/she actually intends to create a
                                        // document
   public boolean createNewRevision = true; // to indicate user want create revision on
                                            // document update, in case of just rename
                                            // user may not want to create revision so it
                                            // is recommended to make it false
   
}