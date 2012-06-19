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
package org.eclipse.stardust.ui.web.viewscommon.views.document.tiff.extract;

import java.util.List;

import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.runtime.DeployedModel;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.extensions.dms.data.annotations.printdocument.PrintDocumentAnnotationsImpl;
import org.eclipse.stardust.ui.web.common.table.DefaultRowModel;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelUtils;



/**
 * 
 * @author vikas.mishra
 * @since 7.0
 */
public class ExtractPageTableEntry extends DefaultRowModel
{
   private static final long serialVersionUID = 1L;
   public static final String PROCESS_ATTACHMENT = "process-attachment";

   private final ProcessInstance sourceProcessInstance;
   private final MessagesViewsCommonBean COMMON_MESSAGE_BEAN = MessagesViewsCommonBean.getInstance();

   private boolean select;
   private int pageTo;
   private int pageFrom;
   private String spawnProcessName;
   private String spawnProcessFQID;
   private String docTypeId;
   private String dataId;
   private boolean deletePages;
   private boolean copyImageData = true;
   private boolean copyProcessData = true;
   private String docDecription;
   private String versionComment;   
   private List<SelectItem> dataItems;
   private List<SelectItem> docTypeItems;
   private byte[] content;
   private PrintDocumentAnnotationsImpl docMetadata;
   private String docId;

   /**
    * 
    * @param defaultProcessFQID
    * @param sourceDocId
    * @param copyProcessData
    */
   public ExtractPageTableEntry(final String sourceDocId, final String defaultProcessFQID,
         final boolean copyProcessData, final ProcessInstance sourceProcessInstance)
   {
      this.copyProcessData = copyProcessData;
      spawnProcessFQID = defaultProcessFQID;
      this.sourceProcessInstance = sourceProcessInstance;
      this.docId = sourceDocId;
      versionComment = COMMON_MESSAGE_BEAN.getParamString("views.extractPageDialog.targetDocumentVersion.comment",
            sourceDocId);
   }

   /**
    * 
    * @param sourceDocId
    * @param copyProcessData
    * @param defaultProcessFQID
    * @param pageFrom
    * @param pageTo
    */
   public ExtractPageTableEntry(String sourceDocId, String defaultProcessFQID, boolean copyProcessData, int pageFrom,
         int pageTo, final ProcessInstance sourceProcessInstance)
   {
      this(sourceDocId, defaultProcessFQID, copyProcessData, sourceProcessInstance);
      this.pageFrom = pageFrom;
      this.pageTo = pageTo;
   }

   public PrintDocumentAnnotationsImpl getDocMetadata()
   {
      return docMetadata;
   }

   public void setDocMetadata(PrintDocumentAnnotationsImpl docMetadata)
   {
      this.docMetadata = docMetadata;
   }

   public List<SelectItem> getDataItems()
   {
      return dataItems;
   }

   public List<SelectItem> getDocTypeItems()
   {
      return docTypeItems;
   }

   public void setDataItems(List<SelectItem> dataItems)
   {
      this.dataItems = dataItems;
   }

   public void setDocTypeItems(List<SelectItem> docTypeItems)
   {
      this.docTypeItems = docTypeItems;
   }

   public byte[] getContent()
   {
      return content;
   }

   public void setContent(byte[] content)
   {
      this.content = content;
   }

   public ProcessInstance getRootProcessInstance()
   {
      return sourceProcessInstance;
   }

   public String getDocId()
   {
      return docId;
   }

   public void setDocId(String docId)
   {
      this.docId = docId;
   }

   public DeployedModel getModel()
   {
      DeployedModel model = null;

      if (null != sourceProcessInstance)
      {
         model = ModelUtils.getModel(sourceProcessInstance.getModelOID());
      }
      else
      {
         String modelId = ModelUtils.extractModelId(spawnProcessFQID);
         model = ModelUtils.getActiveModel(modelId);
      }

      return model;
   }

   /**
    * 
    * @return
    */
   public boolean isContainsDataId()
   {
      return StringUtils.isNotEmpty(dataId);
   }

   public int getPageTo()
   {
      return pageTo;
   }

   public void setPageTo(int pageTo)
   {
      this.pageTo = pageTo;
   }

   public int getPageFrom()
   {
      return pageFrom;
   }

   public void setPageFrom(int pageFrom)
   {
      this.pageFrom = pageFrom;
   }

   public String getDocTypeId()
   {
      return docTypeId;
   }

   public void setDocTypeId(String docTypeId)
   {
      this.docTypeId = docTypeId;
   }

   public String getDataId()
   {
      return dataId;
   }

   public void setDataId(String dataId)
   {
      this.dataId = dataId;
   }

   public boolean isDeletePages()
   {
      return deletePages;
   }

   public void setDeletePages(boolean deletePages)
   {
      this.deletePages = deletePages;
   }

   public boolean isCopyImageData()
   {
      return copyImageData;
   }

   public void setCopyImageData(boolean copyImageData)
   {
      this.copyImageData = copyImageData;
   }

   public boolean isCopyProcessData()
   {
      return copyProcessData;
   }

   public void setCopyProcessData(boolean copyProcessData)
   {
      this.copyProcessData = copyProcessData;
   }

   public boolean isSelect()
   {
      return select;
   }

   public void setSelect(boolean select)
   {
      this.select = select;
   }

   public String getDocDecription()
   {
      return docDecription;
   }

   public void setDocDecription(String docDecription)
   {
      this.docDecription = docDecription;
   }

   public String getVersionComment()
   {
      return versionComment;
   }

   public void setVersionComment(String versionComment)
   {
      this.versionComment = versionComment;
   }

   public void toggleDeletePages(ActionEvent event)
   {
      deletePages = !deletePages;
   }

   public void toggleCopyImageData(ActionEvent event)
   {
      copyImageData = !copyImageData;
   }

   public void toggleCopyProcessData(ActionEvent event)
   {
      copyProcessData = !copyProcessData;
   }  

   public String getSpawnProcessName()
   {
      return spawnProcessName;
   }

   public void setSpawnProcessName(String spawnProcessName)
   {
      this.spawnProcessName = spawnProcessName;
   }

   public String getSpawnProcessFQID()
   {
      return spawnProcessFQID;
   }

   public void setSpawnProcessFQID(String spawnProcessFQID)
   {
      this.spawnProcessFQID = spawnProcessFQID;
   }

   public boolean isProcessAttachmentDataSelected()
   {
      return (PROCESS_ATTACHMENT.equals(dataId));
   }
}
