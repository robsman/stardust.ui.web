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

package org.eclipse.stardust.ui.web.processportal.interaction.iframe;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.engine.api.model.DataMapping;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.engine.extensions.dms.data.DocumentType;
import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.common.app.View;
import org.eclipse.stardust.ui.web.common.event.ViewDataEvent;
import org.eclipse.stardust.ui.web.common.event.ViewDataEventHandler;
import org.eclipse.stardust.ui.web.common.util.StringUtils;
import org.eclipse.stardust.ui.web.processportal.interaction.Interaction;
import org.eclipse.stardust.ui.web.processportal.view.manual.RawDocument;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentMgmtUtility;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentViewUtil;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.FileStorage;
import org.eclipse.stardust.ui.web.viewscommon.utils.MIMEType;
import org.eclipse.stardust.ui.web.viewscommon.utils.MimeTypesHelper;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelUtils;
import org.eclipse.stardust.ui.web.viewscommon.views.document.AbstractDocumentContentInfo;
import org.eclipse.stardust.ui.web.viewscommon.views.document.JCRDocument;

/**
 * 
 * @author Yogesh.Manware
 * 
 */
public class ManualActivityDocumentController
{
   private static final long serialVersionUID = -4288981777329121017L;

   protected static final String DOC_PATH = "../../plugins/views-common/images/icons/";

   private AbstractDocumentContentInfo document;

   private Interaction interaction;

   private DataMapping dataMapping;

   private String docInteractionId;

   private DocumentType documentType;

   private List<AbstractDocumentContentInfo> docsTobeDeleted = new ArrayList<AbstractDocumentContentInfo>();

   private RawDocument rawDocument;
   
   private FileStorage fileStorage;

   /**
    * 
    * @param document
    * @param dm
    * @param interaction
    */
   public ManualActivityDocumentController(Document document, DataMapping dm, Interaction interaction)
   {
      docInteractionId = interaction.getId() + "_" + dm.getDataId();
      this.dataMapping = dm;
      this.interaction = interaction;

      if (document != null)
      {
         JCRDocument jcrDocument = new JCRDocument(document,
               Direction.IN == dm.getDirection());
         
         // pre-initialize mimetype so that it can be called from Rest API
         jcrDocument.getMimeType();

         this.document = jcrDocument;
      }

      this.documentType = ModelUtils.getDocumentTypeFromData(interaction.getModel(),
            interaction.getModel().getData(dm.getDataId()));
      
      fileStorage = FileStorage.getInstance();
   }

   /**
    * 
    * @return
    */
   public AbstractDocumentContentInfo getDocument()
   {
      return this.document;
   }

   /**
    * This method would be invoked only when document is a JCR document and from server
    * side
    * 
    * @param nested
    * @return
    */
   public View openDocument(boolean nested)
   {
      if (this.document != null)
      {
         Map<String, Object> params = CollectionUtils.newMap();
         params.put("processInstance", interaction.getActivityInstance()
               .getProcessInstance());
         params.put("dataPathId", dataMapping.getDataPath());
         params.put("dataId", dataMapping.getDataId());
         params.put("disableAutoDownload", true);

         View documentView = DocumentViewUtil.openDataMappingDocument(
               interaction.getActivityInstance().getProcessInstance(),
               dataMapping.getDataId(), getDocument(), params);

         PortalApplication.getInstance().registerViewDataEventHandler(documentView,
               new ViewDataEventHandler()
               {
                  public void handleEvent(ViewDataEvent event)
                  {
                     setDocument((AbstractDocumentContentInfo) event.getPayload());
                  }
               });

         return documentView;
      }
      return null;
   }

   /**
    * 
    * @return
    */
   public String getViewKey()
   {
      StringBuffer viewKey = new StringBuffer();
      if (isJCRDocument())
      {
         viewKey.append("documentOID=").append(document.getId());
      }
      else
      {
         viewKey.append("documentOID=").append(""); // should never come here
      }

      return viewKey.toString();
   }

   public DocumentType getDocumentType()
   {
      return documentType;
   }

   /**
    * 
    * @return
    */
   public boolean delete(String fileStorageUUID)
   {
      if (isJCRDocument())
      {
         // to be deleted from MAIframeController
         docsTobeDeleted.add(this.document);
         this.document = null;
         return true;
      }
      else if (StringUtils.isNotEmpty(fileStorageUUID))
      {
         String path = fileStorage.pullPath(fileStorageUUID);
         if (StringUtils.isNotEmpty(path))
         {
            File file = new File(path);
            if (file.exists())
            {
               file.delete();
            }
         }
      }
      return false;
   }

   public void setRawDocument(RawDocument rawDocument)
   {
      this.rawDocument = rawDocument;
   }

   public Document createJCRDocumentFromUUID(String uuid)
   {
      // pull physical path
      FileStorage fileStorage = FileStorage.getInstance();
      String path = fileStorage.pullPath(uuid);

      byte[] contentBytes = DocumentMgmtUtility.getFileSystemDocumentContent(path);

      String typedDocFolderPath = DocumentMgmtUtility.getTypedDocumentsFolderPath(interaction.getActivityInstance()
            .getProcessInstance());
      Folder typedDocFolder = DocumentMgmtUtility.createFolderIfNotExists(typedDocFolderPath);

      Document concreteDocument = null;
      String fileName = rawDocument.getName();

      // CHECK if the file with same name already exist in Specific Documents folder
      Document existingDocument = DocumentMgmtUtility.getDocument(typedDocFolderPath,
            fileName);

      if (null != existingDocument)
      {
         fileName = DocumentMgmtUtility.appendTimeStamp(fileName);
      }

      MIMEType mimeType = MimeTypesHelper.detectMimeType(fileName, "");

      // Create Document with Properties
      concreteDocument = DocumentMgmtUtility.createDocument(typedDocFolder.getId(),
            fileName, contentBytes, getDocumentType(), mimeType.getType(),
            rawDocument.getDescription(), rawDocument.getComments(), null,
            new HashMap<String, Object>());

      return concreteDocument;
   }

   /**
    * 
    * @return
    */
   public boolean isJCRDocument()
   {
      if (this.document != null && this.document instanceof JCRDocument)
      {
         return true;
      }
      return false;
   }

   /**
    * 
    * @param document
    */
   public void setDocument(AbstractDocumentContentInfo document)
   {
      this.document = document;
   }

   /**
    * 
    * @return
    */
   public Document getJCRDocument()
   {
      if (isJCRDocument())
      {
         return ((JCRDocument) this.document).getDocument();
      }
      return null;
   }

   public String getIconPath()
   {
      if (isJCRDocument())
      {
         return DOC_PATH + "mime-types/" + getDocument().getMimeType().getIconPath();
      }
      return DOC_PATH + "page_white_error.png";
   }

   /**
    * 
    * @return
    */
   public List<AbstractDocumentContentInfo> getDocsTobeDeleted()
   {
      return docsTobeDeleted;
   }

   public DataMapping getDataMapping()
   {
      return dataMapping;
   }

   public String getDocInteractionId()
   {
      return docInteractionId;
   }

   public static class DOCUMENT
   {
      public static final String TYPE_ID = "docTypeId";

      public static final String TYPE_NAME = "docTypeName";

      public static final String ID = "docId";

      public static final String NAME = "docName";

      public static final String ICON = "docIcon";

      public static final String FILE_NAME = "fileName";

      public static final String CONTENT_TYPE = "contentType";

      public static final String DESCRIPTION = "fileDescription";

      public static final String VERSION_COMMENT = "versionComment";

      public static final String DOC_INTERACTION_ID = "docInteractionId";

      public static final String PROCESS_INSTANCE_OID = "processInstanceOId";

      public static final String DATA_PATH_ID = "dataPathId";

      public static final String DATA_ID = "dataId";
   }
}