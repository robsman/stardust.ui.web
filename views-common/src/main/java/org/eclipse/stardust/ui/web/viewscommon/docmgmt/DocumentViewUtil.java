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
package org.eclipse.stardust.ui.web.viewscommon.docmgmt;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.core.spi.dms.RepositoryIdUtils;
import org.eclipse.stardust.engine.extensions.dms.data.DmsPrivilege;
import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.common.app.View;
import org.eclipse.stardust.ui.web.viewscommon.utils.DMSHelper;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.views.document.FileSystemDocument;
import org.eclipse.stardust.ui.web.viewscommon.views.document.FileSystemJCRDocument;
import org.eclipse.stardust.ui.web.viewscommon.views.document.IDocumentContentInfo;
import org.eclipse.stardust.ui.web.viewscommon.views.document.JCRDocument;

import com.icesoft.util.encoding.Base64;



/**
 * Assist in opening different types of documents
 * 
 * @author Yogesh.Manware
 * 
 */
public class DocumentViewUtil
{
   /**
    * @param viewKey
    * @param documentContentInfo
    * @param viewParams
    * @return
    */
   public static View openDocument(String viewKey, IDocumentContentInfo documentContentInfo,
         Map<String, Object> viewParams)
   {
      Map<String, Object> params = CollectionUtils.newTreeMap();
      params.put("documentInfo", documentContentInfo);
      if (CollectionUtils.isNotEmpty(viewParams))
      {
         params.putAll(viewParams);
      }
      // Again Encode id with Bas64 to handle special char ':' in viewKey
      // ex: {urn:repositoryId:repo1}{jcrUuid}
      viewKey = Base64.encode(viewKey);
      return PortalApplication.getInstance().openViewById("documentView", viewKey, params, null, true);
   }

   /**
    * @param viewKey
    * @param documentContentInfo
    * @return
    */
   public static View openDocument(String viewKey, IDocumentContentInfo documentContentInfo)
   {
      return openDocument(viewKey, documentContentInfo, null);
   }

   /**
    * @param resourcePath
    * @param name
    * @return
    */
   public static View openFileSystemDocument(String resourcePath, String name, boolean editable)
   {
      FileSystemDocument fileSystemDocument = new FileSystemDocument(resourcePath, null, editable);
      if (StringUtils.isNotEmpty(name))
      {
         fileSystemDocument.setName(name);
      }
      return openFileSystemDocument(null, fileSystemDocument);
   }

   /**
    * @param viewKey
    * @param fileSystemDocument
    * @return
    */
   public static View openFileSystemDocument(String viewKey, FileSystemDocument fileSystemDocument)
   {
      if (StringUtils.isEmpty(viewKey))
      {
         viewKey = "documentOID=" + fileSystemDocument.getName();
      }

      return openDocument(viewKey, fileSystemDocument, null);
   }

   /**
    * @param documentId
    * @return
    */
   public static View openJCRDocument(String documentId)
   {
      return openJCRDocument(documentId, null, null);
   }

   /**
    * @param document
    * @return
    */
   public static View openJCRDocument(Document document)
   {
      return openJCRDocument(document, null, null);
   }

   /**
    * @param viewKey
    * @param document
    * @return
    */
   public static View openJCRDocument(String viewKey, Document document)
   {
      return openJCRDocument(viewKey, document, null, null);
   }

   /**
    * @param documentId
    * @param name
    * @return
    */
   public static View openJCRDocument(String documentId, String name)
   {
      return openJCRDocument(documentId, name, null);
   }

   /**
    * @param documentId
    * @param viewParams
    * @return
    */
   public static View openJCRDocument(String documentId, Map<String, Object> viewParams)
   {
      return openJCRDocument(documentId, null, viewParams);
   }

   /**
    * @param document
    * @param viewParams
    * @return
    */
   public static View openJCRDocument(Document document, Map<String, Object> viewParams)
   {
      return openJCRDocument(document, null, viewParams);
   }

   /**
    * @param documentId
    * @param name
    * @param viewParams
    * @return
    */
   public static View openJCRDocument(String documentId, String name, Map<String, Object> viewParams)
   {
      try
      {
         return openJCRDocument(DocumentMgmtUtility.getDocument(documentId), name, viewParams);
      }
      catch (ResourceNotFoundException e)
      {
         ExceptionHandler.handleException(e);
      }
      return null;
   }

   /**
    * @param viewKey
    * @param document
    * @param name
    * @param viewParams
    * @return
    */
   public static View openJCRDocument(String viewKey, Document document, String name, Map<String, Object> viewParams)
   {
      if (!DMSHelper.hasPrivilege(document.getId(), DmsPrivilege.READ_PRIVILEGE))
      {
         RepositoryUtility.showErrorPopup("common.authorization.msg", null, null);
         return null;
      }

      JCRDocument documentContentInfo;
      documentContentInfo = new JCRDocument(document);

      if (StringUtils.isNotEmpty(name))
      {
         documentContentInfo.setName(name);
      }

      // create viewKey
      if (StringUtils.isEmpty(viewKey))
      {
         try
         {
            // TODO review by portal team
            // documentId can contain ':' or other special characters that need to be encoded. e.g. {urn:repositoryId:<repositoryId}<id>
            viewKey = "documentOID=" + URLEncoder.encode(document.getId(), "UTF-8");
         }
         catch (UnsupportedEncodingException e)
         {
            // if encoding fails, fallback to old ID format
            viewKey = "documentOID=" + RepositoryIdUtils.stripRepositoryId(document.getId());
         }
      }

      return openDocument(viewKey, documentContentInfo, viewParams);
   }

   /**
    * @param document
    * @param name
    * @param viewParams
    * @return
    */
   public static View openJCRDocument(Document document, String name, Map<String, Object> viewParams)
   {
      return openJCRDocument(null, document, name, viewParams);
   }

   /**
    * @param reportPath
    * @param modelID
    * @param urlParamMap
    * @return
    */
   public static View openActiveModelReport(String reportPath, String modelID, Map<String, Object> urlParamMap)
   {
      FileSystemDocument fileSystemDocument = new FileSystemDocument(reportPath, null, false);

      // create viewKey
      String viewKey = "documentOID=";
      viewKey += fileSystemDocument.getName();
      // if it is a model report
      if (StringUtils.isNotEmpty(modelID))
      {
         viewKey += modelID;
         fileSystemDocument.setName(fileSystemDocument.getName() + " (" + modelID + ")");
      }
      return openDocument(viewKey, fileSystemDocument, urlParamMap);
   }

   /**
    * @param processInstance
    * @param dataId
    * @param documentContentInfo
    * @param viewParams
    * @return
    */
   public static View openDataMappingDocument(ProcessInstance processInstance, String dataId,
         IDocumentContentInfo documentContentInfo, Map<String, Object> viewParams)
   {
      StringBuffer viewKey = new StringBuffer();
      if (documentContentInfo instanceof FileSystemJCRDocument)
      {
         viewKey.append("documentOID=").append(documentContentInfo.getId().hashCode());
      }
      else
      {
         viewKey.append("documentOID=").append(documentContentInfo.getId());
      }
      return openDocument(viewKey.toString(), documentContentInfo, viewParams);
   }
}
