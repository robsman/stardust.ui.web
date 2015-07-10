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
package org.eclipse.stardust.ui.web.rest.service.utils;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.query.DeployedRuntimeArtifactQuery;
import org.eclipse.stardust.engine.api.query.DeployedRuntimeArtifacts;
import org.eclipse.stardust.engine.api.runtime.DeployedRuntimeArtifact;
import org.eclipse.stardust.engine.api.runtime.DmsUtils;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentInfo;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.engine.api.runtime.RuntimeArtifact;
import org.eclipse.stardust.engine.extensions.dms.data.DocumentType;
import org.eclipse.stardust.ui.web.viewscommon.utils.MimeTypesHelper;

/**
 * @author Anoop.Nair
 * @version $Revision: $
 */
@Component
public class DocumentUtils
{
   private static final String CONTENT_TYPE = "text/plain";
   
   @Resource
   private ServiceFactoryUtils serviceFactoryUtils;

   public Document getDocument(String documentId)
   {
      return serviceFactoryUtils.getDocumentManagementService().getDocument(documentId);
   }

   public byte[] getDocumentContents(String documentId)
   {
      return serviceFactoryUtils.getDocumentManagementService().retrieveDocumentContent(documentId);
   }
   
   public Folder getFolder(String path)
   {
      return serviceFactoryUtils.getDocumentManagementService().getFolder(path);
   }
   
   public int getNumPages(Document document)
   {
      int numPages = 0;

      byte[] data = null;

      String contentType = document.getContentType();
      if (MimeTypesHelper.PDF.getType().equals(contentType))
      {
         data = getDocumentManagementService().retrieveDocumentContent(document.getId());
         numPages = 0; // PdfPageCapture.getNumPages(data); // TODO: Complete
      }
      else if (MimeTypesHelper.TIFF.getType().equals(contentType))
      {
         data = getDocumentManagementService().retrieveDocumentContent(document.getId());
         numPages = 0; // TiffReader.getNumPages(data); // TODO: Complete
      }

      return numPages;
   }

   /**
    * @param documentId
    */
   public DocumentType getDocumentType(String documentId)
   {
      return getDocument(documentId).getDocumentType();
   }

   /**
    * @param documentId
    * @param documentTypeId
    * @param schemaLocation
    * @return
    */
   public Document setDocumentType(String documentId, String documentTypeId,
         String schemaLocation)
   {
      DocumentType documentType = null;
      Document document = getDocumentManagementService().getDocument(documentId);

      if (StringUtils.isNotEmpty(documentTypeId))
      {
         documentType = new DocumentType(documentTypeId, schemaLocation);
      }

      document.setDocumentType(documentType);

      Document updatedDocument = getDocumentManagementService().updateDocument(document,
            false, "", "", false);

      return updatedDocument;
   }

   /**
    * 
    * @param folderId
    * @param fileName
    * @param contentType
    * @param byteContents
    * @return
    */
   public Document createDocument(String folderId, String fileName, String contentType, byte[] byteContents)
   {
      DocumentInfo docInfo = DmsUtils.createDocumentInfo(fileName);
      docInfo.setOwner(serviceFactoryUtils.getUserService().getUser().getAccount());
      if (StringUtils.isNotEmpty(contentType))
      {
         docInfo.setContentType(contentType);
      }
      else
      {
         docInfo.setContentType(CONTENT_TYPE);
      }
      Document document = getDocumentManagementService().createDocument(folderId, docInfo, byteContents, null);
      return document;
   }
   
   /**
    * 
    * @param doc
    * @param byteContents
    * @param comments
    * @param overwrite
    * @return
    */
   public Document updateDocument(Document doc, byte[] byteContents, String comments, boolean overwrite)
   {
      Document document = getDocumentManagementService().updateDocument(doc, byteContents, "", !overwrite,
            comments, null, false);
      return  document;
   }
   
   /**
    * 
    * @param doc
    * @param byteContents
    * @param comments
    * @param overwrite
    * @return
    */
   public Document deleteDocument(Document doc, byte[] byteContents, String comments, boolean overwrite)
   {
      Document document = getDocumentManagementService().updateDocument(doc, byteContents, "", !overwrite,
            comments, null, false);
      return  document;
   }
   
   /**
    * 
    * @param oid
    * @param artifact
    * @return
    */
   public DeployedRuntimeArtifact deployBenchmarkDocument(long oid, RuntimeArtifact artifact)
   {
      DeployedRuntimeArtifact runtimeArtifacts = null;
      if(oid > 0)
      {
         
      }
      else
      {
         runtimeArtifacts = serviceFactoryUtils.getAdministrationService().deployRuntimeArtifact(artifact);   
      }
      
      return runtimeArtifacts;
   }
   
   /**
    * 
    * @param query
    * @return
    */
   public DeployedRuntimeArtifacts getDeployedBenchmarkDefinitions(DeployedRuntimeArtifactQuery query)
   {
      DeployedRuntimeArtifacts runtimeArtifacts = serviceFactoryUtils.getQueryService().getRuntimeArtifacts(query);
      return runtimeArtifacts;
   }
   
   /**
    * 
    * @param oid
    * @return
    */
   public RuntimeArtifact getRuntimeArtifacts(long oid)
   {
      return serviceFactoryUtils.getAdministrationService().getRuntimeArtifact(oid);
   }
   
   /**
    * 
    * @param oid
    */
   public void deleteRuntimeArtifacts(long oid)
   {
      serviceFactoryUtils.getAdministrationService().deleteRuntimeArtifact(oid);
   }
   
   /**
    * @return
    */
   private DocumentManagementService getDocumentManagementService()
   {
      return serviceFactoryUtils.getDocumentManagementService();
   }

}
