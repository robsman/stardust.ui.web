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

import java.util.Map;

import org.eclipse.stardust.engine.extensions.dms.data.DocumentType;
import org.eclipse.stardust.engine.extensions.dms.data.annotations.printdocument.DocumentAnnotations;
import org.eclipse.stardust.ui.web.viewscommon.utils.MIMEType;


/**
 * @author Yogesh.Manware
 * 
 */
public abstract class AbstractDocumentContentInfo implements IDocumentContentInfo
{
   protected String name = "";
   protected String author = null;
   protected String id;
   protected String idLabel;
   protected DocumentType documentType;
   protected boolean modifyPrivilege;

   protected String description = "";
   protected String comments = "";
   protected DocumentAnnotations annotations;
   protected Map<String, Object> properties = null;

   protected byte[] content = null;
   protected MIMEType mimeType = null;
   protected String url = null;

   protected boolean supportVersioning;
   protected JCRVersionTracker versionTracker;
   
   protected boolean contentEditable = true;
   protected boolean metaDataEditable = true;
   
   public IDocumentContentInfo saveFile(String filePath)
   {
      return null;
   }
   
   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public String getAuthor()
   {
      return author;
   }

   public String getDescription()
   {
      return description;
   }

   public String getComments()
   {
      return comments;
   }

   public DocumentAnnotations getAnnotations()
   {
      return annotations;
   }

   public void setDescription(String desc)
   {
      this.description = desc;
   }

   public void setComments(String comments)
   {
      this.comments = comments;
   }

   public void setAnnotations(DocumentAnnotations annotations)
   {
      this.annotations = annotations;
   }

   public String getURL()
   {
      return url;
   }

   public boolean isContentEditable()
   {
      return contentEditable;
   }

   public boolean isMetaDataEditable()
   {
      return metaDataEditable;
   }

   public boolean isSupportsVersioning()
   {
      return supportVersioning;
   }

   public IVersionTracker getVersionTracker()
   {
      return versionTracker;
   }

   public Map<String, Object> getProperties()
   {
      return properties;
   }

   public MIMEType getMimeType()
   {
      return mimeType;
   }

   public String getIcon()
   {
      return mimeType.getCompleteIconPath();
   }

   public DocumentType getDocumentType()
   {
      return documentType;
   }
   
   public boolean isModifyPrivilege()
   {
      return modifyPrivilege;
   }

   public String getId()
   {
      return id;
   }

   public String getIdLabel()
   {
      return idLabel;
   }
}
