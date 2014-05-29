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
package org.eclipse.stardust.ui.web.viewscommon.views.document;

import javax.faces.context.FacesContext;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.common.app.PortalApplicationEventScript;
import org.eclipse.stardust.ui.web.common.app.View;
import org.eclipse.stardust.ui.web.common.event.ViewEvent;
import org.eclipse.stardust.ui.web.common.event.ViewEventHandler;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.RepositoryUtility;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.MIMEType;
import org.eclipse.stardust.ui.web.viewscommon.utils.MimeTypesHelper;

/**
 * @author Yogesh.Manware
 * 
 */
public class ReportViewer implements IDocumentViewer, ViewEventHandler
{
   public static final String URL_PARAMETERS = "URL_PARAMETERS";
   private static final String FAVORITE_MARKED = "/plugins/views-common/images/icons/star.png";
   private static final String MARK_FAVORITE = "/plugins/views-common/images/icons/star-empty.png";
   private static final String CONTENT_URL = "/plugins/views-common/views/report/reportViewerFrameAdapter.xhtml";
   private static final String TOOLBAR_URL = "/plugins/views-common/extension/toolbar/reportDocumentViewToolbar.xhtml";
   
   // Report Viewer
   private static final String VIEW_PATH = "/plugins/views-common/views/report/reportViewer.html";
   private static final String ANCHOR_ID = "reportViewerFrameAnchor";
   private static final String KEY_PARAM = "reportViewerName";
   
   private final MIMEType[] mimeTypes = {MimeTypesHelper.BPM_RPT_DESIGN, MimeTypesHelper.BPM_RPT};
   private boolean favoriteReport;
   private MessagesViewsCommonBean propsBean;
   private IDocumentContentInfo documentContentInfo;

   private String reportPath = "";
   private String reportName = "";

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.eclipse.stardust.ui.web.viewscommon.views.document.IDocumentViewer#initialize
    * (org.eclipse.stardust.ui.web.viewscommon.views.document.IDocumentContentInfo,
    * org.eclipse.stardust.ui.web.common.app.View)
    */
   public void initialize(IDocumentContentInfo documentContentInfo, View view)
   {
      propsBean = MessagesViewsCommonBean.getInstance();
      if (documentContentInfo instanceof JCRDocument)
      {
         JCRDocument jcrDocument = (JCRDocument) documentContentInfo;
         reportName = jcrDocument.getName();
         reportPath = jcrDocument.getDocument().getPath();
      }
      else
      {
         // nothing
      }
      this.documentContentInfo = documentContentInfo;
      this.documentContentInfo.setShowDetails(false);

      setFavoriteStatus(documentContentInfo.getId());
   }

   /**
    * archive report
    */
   public void archiveReport()
   {

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
      return CONTENT_URL;
   }

   public MIMEType[] getMimeTypes()
   {
      return mimeTypes;
   }

   public String getToolbarUrl()
   {
      return TOOLBAR_URL;
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

   @Override
   public String getContent()
   {
      // TODO Auto-generated method stub
      return "";
   }

   @Override
   /**
    *
    */
   public void handleEvent(ViewEvent event)
   {
      String pagePath = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath();
      pagePath += VIEW_PATH;

      event.getView().getViewParams().put("name", reportName);
      event.getView().getViewParams().put("path", reportPath);
      if (MimeTypesHelper.detectMimeType(reportName, "").equals(MimeTypesHelper.BPM_RPT))
      {
         event.getView().getViewParams().put("viewMode", "instance");
      }

      String iframeId = "mf_" + event.getView().getIdentityParams();

      switch (event.getType())
      {
      case TO_BE_ACTIVATED:
         Object keyParamValue = (StringUtils.isNotEmpty(KEY_PARAM)) ? event.getView().getViewParams().get(KEY_PARAM) : "";
         PortalApplication.getInstance().addEventScript(
               "InfinityBpm.ProcessPortal.createOrActivateContentFrame('" + iframeId + "', '" + pagePath
                     + event.getView().getParams() + "', {anchorId:'" + ANCHOR_ID
                     + "', anchorYAdjustment:10, zIndex:200, frmAttrs: {displayName: '" + keyParamValue + "'}});");
         fireResizeIframeEvent();

         if (View.ViewState.INACTIVE == event.getView().getViewState())
         {
            changeMouseCursorStyle("default");
         }
         break;

      case TO_BE_DEACTIVATED:
         PortalApplication.getInstance().addEventScript(
               "InfinityBpm.ProcessPortal.deactivateContentFrame('" + iframeId + "');");
         fireResizeIframeEvent();
         break;

      case CLOSED:
         PortalApplication.getInstance().addEventScript(
               "InfinityBpm.ProcessPortal.closeContentFrame('" + iframeId + "');");
         break;

      case LAUNCH_PANELS_ACTIVATED:
      case LAUNCH_PANELS_DEACTIVATED:
      case FULL_SCREENED:
      case RESTORED_TO_NORMAL:
      case PINNED:
      case PERSPECTIVE_CHANGED:
         fireResizeIframeEvent();
         break;
      }
   }

   private void fireResizeIframeEvent()
   {
      PortalApplication.getInstance().addEventScript("InfinityBpm.ProcessPortal.resizeIFrames();");
   }

   /**
    * @param style
    */
   private void changeMouseCursorStyle(String style)
   {
      PortalApplicationEventScript.getInstance().addEventScript(
            "InfinityBpm.Core.changeMouseCursorStyle(\"" + style + "\");");
   }

   @Override
   public boolean isHideToolbar()
   {
      return true;
   }
}