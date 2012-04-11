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
package org.eclipse.stardust.ui.web.viewscommon.views.documentsearch;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.query.ProcessInstances;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.ui.web.common.table.DefaultRowModel;
import org.eclipse.stardust.ui.web.viewscommon.common.DocumentToolTip;
import org.eclipse.stardust.ui.web.viewscommon.common.ToolTip;
import org.eclipse.stardust.ui.web.viewscommon.core.ResourcePaths;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentMgmtUtility;
import org.eclipse.stardust.ui.web.viewscommon.utils.*;



/**
 * class is type of DefaultRowModel for document search table
 * 
 * @author Vikas.Mishra
 * 
 */
public class DocumentSearchTableEntry extends DefaultRowModel
{
   private static final long serialVersionUID = -364375780924462409L;
   private Date createDate;
   private Date modificationDate;
   private Document document;
   private List<ProcessWrapper> processList = new ArrayList<ProcessWrapper>();
   private String author = "";
   private String containingText;
   private String fileType;
   private String documentType;
   private List<Pair<String, String>> metadata;

   // ~ Instance fields
   // ================================================================================================
   private String documentId;
   private String documentName;
   private String documentPath;
   private User user;
   private boolean popupVisible;
   private long fileSize;
   private boolean selectedRow;
   private String fileSizeLabel;
   private ToolTip documentToolTip;

   private final DocumentProcessesDialog processesDialog;

   // ~ Constructor
   // ================================================================================================

   public DocumentSearchTableEntry()
   {
      processesDialog = new DocumentProcessesDialog();
   }

   public DocumentSearchTableEntry(Document doc, String documentType)
   {
      this();
      this.documentId = doc.getId();
      this.documentName = doc.getName();
      this.fileType = doc.getContentType();
      this.documentType = documentType;
      this.createDate = doc.getDateCreated();
      this.modificationDate = doc.getDateLastModified();
      this.fileSize = doc.getSize();
      this.fileSizeLabel = DocumentMgmtUtility.getHumanReadableFileSize(this.fileSize);
      this.documentPath = getFolderFromFullPath(doc.getPath());
      document = doc;

      user = DocumentMgmtUtility.getOwnerOfDocument(document);
      if (null != user)
      {
         author = FormatterUtils.getUserLabel(user);
      }
      else if (StringUtils.isNotEmpty(document.getOwner()))
      {
         author = document.getOwner();
      }
      
      documentToolTip = new DocumentToolTip(null, document);
      
      metadata = TypedDocumentsUtil.getMetadataAsList(document, true);
      if (metadata.size() > 5) // Requirement is to only show first 5 entries
      {
         metadata = metadata.subList(0, 5);
      }
   }

   public DocumentProcessesDialog getProcessesDialog()
   {
      return processesDialog;
   }

   public final String getAuthor()
   {
      return author;
   }

   public String getContainingText()
   {
      return containingText;
   }

   public String getFileType()
   {
      return fileType;
   }

   public Date getCreateDate()
   {
      return createDate;
   }

   public String getDocumentId()
   {
      return documentId;
   }

   public String getDocumentName()
   {
      return documentName;
   }

   public long getFileSize()
   {
      return fileSize;
   }

   public String getFileSizeLabel()
   {
      return fileSizeLabel;
   }

   public String getIconPath()
   {
      MIMEType mimeType = MimeTypesHelper.detectMimeType(documentName, fileType);
      return ResourcePaths.MIME_TYPE_PATH + mimeType.getIconPath();
   }

   public Date getModificationDate()
   {
      return modificationDate;
   }

   public final List<ProcessWrapper> getProcessList()
   {
      if (processList.isEmpty())
      {
         loadProcessByDocument();
      }
      return processList;
   }

   public static final long getSerialVersionUID()
   {
      return serialVersionUID;
   }

   public final Document getDocument()
   {
      return document;
   }

   public void setAuthor(String author)
   {
      this.author = author;
   }

   public void setContainingText(String containingText)
   {
      this.containingText = containingText;
   }

   public void setFileType(String fileType)
   {
      this.fileType = fileType;
   }

   public void setCreateDate(Date createDate)
   {
      this.createDate = createDate;
   }

   public final void setDocument(Document document)
   {
      this.document = document;
   }

   public String getAuthorFullName()
   {
       if (StringUtils.isNotEmpty(author) && getUser()!=null )
       {
           return FormatterUtils.getUserLabel(getUser());
       }

       return author;
   }
   
   public final User getUser()
   {
      return user;
   }

   public final boolean isPopupVisible()
   {
      return popupVisible;
   }

   public void setDocumentId(String documentId)
   {
      this.documentId = documentId;
   }

   public void setDocumentName(String documentName)
   {
      this.documentName = documentName;
   }

   public void setFileSize(long fileSize)
   {
      this.fileSize = fileSize;
   }

   public void setModificationDate(Date modificationDate)
   {
      this.modificationDate = modificationDate;
   }

   public final void setPopupVisible(boolean popupVisible)
   {
      this.popupVisible = popupVisible;
   }

   public final void setProcessList(List<ProcessWrapper> processList)
   {
      this.processList = processList;
   }

   public final String getDocumentPath()
   {
      return documentPath;
   }

   public final void setDocumentPath(String documentPath)
   {
      this.documentPath = documentPath;
   }

   public boolean isSelectedRow()
   {
      return selectedRow;
   }

   public void setSelectedRow(boolean selectedRow)
   {
      this.selectedRow = selectedRow;
   }

   public String getDocumentType()
   {
      return documentType;
   }

   public void setDocumentType(String documentType)
   {
      this.documentType = documentType;
   }

   public ToolTip getDocumentToolTip()
   {
      return documentToolTip;
   }

   public List<Pair<String, String>> getMetadata()
   {
      return metadata;
   }

   public void setMetadata(List<Pair<String, String>> metadata)
   {
      this.metadata = metadata;
   }

   // ~ Methods
   // ================================================================================================

   public void loadProcessByDocument()
   {
      try
      {
         if (processList.isEmpty())
         {
            ProcessInstances processInstances = DocumentMgmtUtility.findProcessesHavingDocument(document);
            if (CollectionUtils.isNotEmpty(processInstances))
            {
               for (ProcessInstance process : processInstances)
               {
                  processList.add(new ProcessWrapper(process));
               }
            }
         }
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }

   }

   /**
    * method returns folder path from full document path.
    * 
    * @param fullPath
    * @return
    */
   private static String getFolderFromFullPath(String fullPath)
   {
      if (StringUtils.isNotEmpty(fullPath))
      {
         int lastSeperator = fullPath.lastIndexOf("/");
         return fullPath.substring(0, lastSeperator);
      }
      return fullPath;
   }

   /**
    * 
    * @author Vikas.Mishra
    * @version $Revision: $
    */
   public class ProcessWrapper
   {
      private final ProcessInstance processInstance;

      public ProcessWrapper(ProcessInstance processInstance)
      {
         this.processInstance = processInstance;
      }

      public ProcessInstance getProcessInstance()
      {
         return processInstance;
      }

      public String getProcessName()
      {
         ProcessDefinition pd = ProcessDefinitionUtils.getProcessDefinition(processInstance.getModelOID(),
               processInstance.getProcessID());
         return I18nUtils.getProcessName(pd);
      }

   }
}