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
package org.eclipse.stardust.ui.web.processportal.view.manual;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.extensions.dms.data.DocumentType;
import org.eclipse.stardust.engine.extensions.dms.data.annotations.printdocument.DocumentAnnotations;

import com.icesoft.faces.component.inputfile.FileInfo;



/**
 * @author Subodh.Godbole
 *
 */
public class RawDocument implements Document
{
   private static final long serialVersionUID = 1L;

   private FileInfo fileInfo;
   private Map<String, Serializable> properties;
   private String description;
   private String comments;
   private DocumentType documentType;

   /**
    * @param fileInfo
    */
   public RawDocument(FileInfo fileInfo)
   {
      this.fileInfo = fileInfo;
      properties = new HashMap<String, Serializable>();
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.api.runtime.DocumentInfo#getContentType()
    */
   public String getContentType()
   {
      return (null != fileInfo) ? fileInfo.getContentType() : null;
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.api.runtime.ResourceInfo#getName()
    */
   public String getName()
   {
      return (null != fileInfo) ? fileInfo.getFileName() : null;
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.api.runtime.Document#getSize()
    */
   public long getSize()
   {
      return (null != fileInfo) ? fileInfo.getSize() : 0;
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.api.runtime.ResourceInfo#getProperties()
    */
   @SuppressWarnings("rawtypes")
   public Map getProperties()
   {
      return properties;
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.api.runtime.ResourceInfo#getProperty(java.lang.String)
    */
   public Serializable getProperty(String property)
   {
      return properties.get(property);
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.api.runtime.ResourceInfo#setProperties(java.util.Map)
    */
   @SuppressWarnings({"rawtypes", "unchecked"})
   public void setProperties(Map props)
   {
      properties.clear();
      if (null != props)
      {
         properties.putAll(props);
      }
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.api.runtime.ResourceInfo#setProperty(java.lang.String, java.io.Serializable)
    */
   public void setProperty(String property, Serializable value)
   {
      properties.put(property, value);
   }

   public FileInfo getFileInfo()
   {
      return fileInfo;
   }

   public String getDescription()
   {
      return description;
   }

   public void setDescription(String description)
   {
      this.description = description;
   }

   public String getComments()
   {
      return comments;
   }

   public void setComments(String comments)
   {
      this.comments = comments;
   }

   public DocumentType getDocumentType()
   {
      return documentType;
   }

   public void setDocumentType(DocumentType documentType)
   {
      this.documentType = documentType;
   }

   public String getId()
   {
      return "not-yet-set";
   }

   /**************** Unsupported Operations ****************/

   public DocumentAnnotations getDocumentAnnotations()
   {
      throw new UnsupportedOperationException();
   }

   public void setContentType(String arg0)
   {
      throw new UnsupportedOperationException();
   }

   public void setDocumentAnnotations(DocumentAnnotations arg0)
   {
      throw new UnsupportedOperationException();
   }

   public Date getDateCreated()
   {
      throw new UnsupportedOperationException();
   }

   public Date getDateLastModified()
   {
      throw new UnsupportedOperationException();
   }

   public String getOwner()
   {
      throw new UnsupportedOperationException();
   }

   public void setName(String arg0)
   {
      throw new UnsupportedOperationException();
   }

   public void setOwner(String arg0)
   {
      throw new UnsupportedOperationException();
   }

   public String getPath()
   {
      throw new UnsupportedOperationException();
   }

   public String getRepositoryId()
   {
      throw new UnsupportedOperationException();
   }

   public String getEncoding()
   {
      throw new UnsupportedOperationException();
   }

   public String getRevisionId()
   {
      throw new UnsupportedOperationException();
   }

   public String getRevisionName()
   {
      throw new UnsupportedOperationException();
   }

   public List getVersionLabels()
   {
      throw new UnsupportedOperationException();
   }

   public String getRevisionComment()
   {
      throw new UnsupportedOperationException();
   }
}
