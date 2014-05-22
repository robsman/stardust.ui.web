/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.viewscommon.views.document;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import org.eclipse.stardust.engine.extensions.dms.data.DocumentType;
import org.eclipse.stardust.engine.extensions.dms.data.annotations.printdocument.DocumentAnnotations;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.ResourceNotFoundException;
import org.eclipse.stardust.ui.web.viewscommon.utils.MIMEType;


/**
 * @author Yogesh.Manware
 * 
 */
public interface IDocumentContentInfo extends Serializable
{
   String getId();
   String getName();
   String getAuthor();
   Date getDateLastModified();
   Date getDateCreated();
   long getSize();
   DocumentType getDocumentType();
   void setDocumentType(DocumentType documentType);

   byte[] retrieveContent();
   MIMEType getMimeType();
   String getURL();
   String getIcon();

   String getDescription();
   String getComments();
   DocumentAnnotations getAnnotations();
   Map<String, Object> getProperties();
   
   void setDescription(String desc);
   void setComments(String comments);
   void setAnnotations(DocumentAnnotations annotations);
   
   boolean isModifyPrivilege();
   boolean isContentEditable();
   boolean isMetaDataEditable();

   IDocumentContentInfo save(byte[] contentBytes);
   IDocumentContentInfo saveFile(String filePath);
   IDocumentContentInfo reset() throws ResourceNotFoundException;

   boolean isSupportsVersioning();
   IVersionTracker getVersionTracker();
   void setShowDetails(boolean showDetails);
   boolean isShowDetails();
}