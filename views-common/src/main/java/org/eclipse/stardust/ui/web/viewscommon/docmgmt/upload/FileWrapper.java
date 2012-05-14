package org.eclipse.stardust.ui.web.viewscommon.docmgmt.upload;

import org.eclipse.stardust.engine.extensions.dms.data.DocumentType;

import com.icesoft.faces.component.inputfile.FileInfo;

/**
 * @author Yogesh.Manware
 * 
 */
public class FileWrapper
{
   private FileInfo fileInfo;
   private String description;
   private String comments;
   private DocumentType documentType;
   private boolean openDocument;

   public FileWrapper()
   {
      super();
   }

   public FileInfo getFileInfo()
   {
      return fileInfo;
   }

   public String getDescription()
   {
      return description;
   }

   public String getComments()
   {
      return comments;
   }

   public DocumentType getDocumentType()
   {
      return documentType;
   }

   public void setFileInfo(FileInfo fileInfo)
   {
      this.fileInfo = fileInfo;
   }

   public void setDescription(String description)
   {
      this.description = description;
   }

   public void setComments(String comments)
   {
      this.comments = comments;
   }

   public void setDocumentType(DocumentType documentType)
   {
      this.documentType = documentType;
   }

   public boolean isOpenDocument()
   {
      return openDocument;
   }

   public void setOpenDocument(boolean openDocument)
   {
      this.openDocument = openDocument;
   }
}
