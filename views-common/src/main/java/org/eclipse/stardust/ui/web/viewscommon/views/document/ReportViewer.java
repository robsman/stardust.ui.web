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


import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.ui.web.common.app.View;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentMgmtUtility;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.RepositoryUtility;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.ResourceNotFoundException;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.MIMEType;
import org.eclipse.stardust.ui.web.viewscommon.utils.ServiceFactoryUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.UserUtils;


/**
 * @author Yogesh.Manware
 * 
 */
public class ReportViewer implements IDocumentViewer
{
   public static final String URL_PARAMETERS="URL_PARAMETERS";
   private static final String FAVORITE_MARKED = "/plugins/views-common/images/icons/star.png";
   private static final String MARK_FAVORITE = "/plugins/views-common/images/icons/star-empty.png";
   private final String contentUrl = "/plugins/views-common/views/report/reportViewer.xhtml";
   private final String toolbarUrl = "/plugins/views-common/extension/toolbar/reportDocumentViewToolbar.xhtml";
   private final MIMEType[] mimeTypes = {};

   private String sourceURI;
   private String reportUri;
   private boolean favoriteReport;
   private MessagesViewsCommonBean propsBean;
   private View view;
   private IDocumentContentInfo documentContentInfo;

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.stardust.ui.web.viewscommon.views.document.IDocumentViewer#initialize(org.eclipse.stardust.ui.web.viewscommon.views.document.IDocumentContentInfo, org.eclipse.stardust.ui.web.common.app.View)
    */
   public void initialize(IDocumentContentInfo documentContentInfo, View view)
   {
      this.view = view;
      propsBean = MessagesViewsCommonBean.getInstance();
      if (documentContentInfo instanceof JCRDocument)
      {
         JCRDocument jcrDocument = (JCRDocument) documentContentInfo;
         try
         {
            reportUri = getJCRReportUri(jcrDocument.getDocument().getId());
         }
         catch (Exception e)
         {
            ExceptionHandler.handleException(e);
         }
      }
      else if (documentContentInfo instanceof FileSystemDocument)
      {
         reportUri = documentContentInfo.getId();
      }
      this.documentContentInfo = documentContentInfo;
      setFavoriteStatus(documentContentInfo.getId());
      String queryString = getQueryString();
      sourceURI = DocumentMgmtUtility.getReportingBaseURL() + "/" + getPartitionID() + "?__report=" + reportUri + queryString + "&realmId="
            + UserUtils.getRealmId() + "&workflowUserSessionId=" + ServiceFactoryUtils.getWorkflowUserSessionId();
   }
   
   /**
    * mark or unmark favorite reports
    */
   public void updateFavoriteStatus()
   {
      String id = "";
      String name = "";
      if (documentContentInfo instanceof FileSystemDocument)
      {
         id = documentContentInfo.getId();
         name = documentContentInfo.getName();
      }
      else if (documentContentInfo instanceof JCRDocument)
      {
         id = documentContentInfo.getId();
         name = documentContentInfo.getName();
      }

      if (favoriteReport)
      {
         RepositoryUtility.removeFromFavorite(id);
         favoriteReport = false;
      }
      else
      {
         RepositoryUtility.addToFavorite(name, id);
         favoriteReport = true;
      }
   }

   /**
    */
   private String getQueryString()
   {
      if (CollectionUtils.isNotEmpty(view.getViewParams()))
      {
         String modelId = (String) view.getViewParams().get("ModelID");
         String modelOId = (String) view.getViewParams().get("ModelOID");
         StringBuilder strBuilder = new StringBuilder();
         if (StringUtils.isNotEmpty(modelId) && StringUtils.isNotEmpty(modelOId))
         {
            strBuilder.append("&").append("ModelID").append("=").append(modelId);
            strBuilder.append("&").append("ModelOID").append("=").append(modelOId);
         }
         return strBuilder.toString();
      }
      return "";
   }
   
   /**
    * @return
    */
   private String getPartitionID()
   {
      return "frameset/" + UserUtils.getPartitionID();
   }
   
   /**
    * @param documentPath
    * @return
    * @throws ResourceNotFoundException
    */
   private String getJCRReportUri(String documentOID) throws ResourceNotFoundException
   {
      return FacesUtils.getServerBaseURL() + "/dms-content/"
            + DocumentMgmtUtility.getDocumentManagementService().requestDocumentContentDownload(documentOID);
   }

   /**
    * @param documentId
    */
   private void setFavoriteStatus(String documentId)
   {
      // check if the report is favorite
      if (RepositoryUtility.isFavoriteReport(documentId))
      {
         favoriteReport = true;
      }
      else
      {
         favoriteReport = false;
      }
   }

   public void setContent(String content)
   {}

   /**
    * Returns content url
    */
   public String getContentUrl()
   {
      return contentUrl;
   }

   /**
    * returns the resource uri
    */
   public String getContent()
   {
      return sourceURI;
   }

   public MIMEType[] getMimeTypes()
   {
      return mimeTypes;
   }

   public String getToolbarUrl()
   {
      return toolbarUrl;
   }

   public String getFavoriteIcon()
   {
      if (favoriteReport)
      {
         return FAVORITE_MARKED;
      }
      else
      {
         return MARK_FAVORITE;
      }
   }

   public String getFavoriteTitle()
   {
      if (favoriteReport)
      {
         return propsBean.getString("views.myReportsView.removeFromFavorite.title");
      }
      else
      {
         return propsBean.getString("views.myReportsView.addToFavorite.title");
      }
   }

   public void closeDocument()
   {}
}