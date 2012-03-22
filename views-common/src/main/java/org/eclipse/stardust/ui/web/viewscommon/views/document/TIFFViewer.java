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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.extensions.dms.data.annotations.printdocument.DocumentAnnotations;
import org.eclipse.stardust.engine.extensions.dms.data.annotations.printdocument.Identifiable;
import org.eclipse.stardust.engine.extensions.dms.data.annotations.printdocument.PageOrientation;
import org.eclipse.stardust.engine.extensions.dms.data.annotations.printdocument.PrintDocumentAnnotationsImpl;
import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.common.app.View;
import org.eclipse.stardust.ui.web.common.event.ViewEvent;
import org.eclipse.stardust.ui.web.common.event.ViewEventHandler;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.core.SessionSharedObjectsMap;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentMgmtUtility;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.ResourceNotFoundException;
import org.eclipse.stardust.ui.web.viewscommon.utils.MIMEType;
import org.eclipse.stardust.ui.web.viewscommon.utils.MimeTypesHelper;
import org.eclipse.stardust.ui.web.viewscommon.views.document.tiff.TIFFDocumentHolder;

import com.icesoft.faces.context.effects.JavascriptContext;


/**
 * @author Shrikant.Gangal
 * 
 */
public class TIFFViewer implements IDocumentViewer, ICustomDocumentSaveHandler, IDocumentEventListener, ViewEventHandler
{
   private static final String imgConfigMapKey = "IMAGE_VIEWER_CONFIGURATION";
   private static final Logger trace = LogManager.getLogger(TIFFViewer.class);

   private final String contentUrl = "/plugins/views-common/views/document/tiffDocumentDetails.xhtml";
   
   private final String customSaveDialogURL = "/plugins/views-common/views/document/tiffCustomSaveDialog.xhtml";

   private Map<String, TIFFDocumentWrapper> DOC_ID_VS_DOC_MAP; 

   private boolean contentChanged = false;

   private final MIMEType[] mimeTypes = {MimeTypesHelper.TIFF};

   private TIFFDocumentHolder docHolder;

   private String pageImageURL;

   private String docId;
   
   private String frameId;
   
   private ProcessInstance processInstance;
   
   private IDocumentContentInfo documentInfo;
   
   private View view;
   
   private boolean poppedOut;
   
   private boolean descriptionChanged;
   
   private int iframeDelay;

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.stardust.ui.web.viewscommon.views.document.IDocumentViewer#initialize
    * ()
    */
   public void initialize(IDocumentContentInfo documentContentInfo, View view)
   {
      this.view = view;
      processInstance = (ProcessInstance) view.getViewParams().get("processInstance");
      setDocumentId(documentContentInfo);
      
      //Temporary code
      documentInfo = documentContentInfo;
      
      frameId = "tiff_frame" + docId;
      
      try
      {
         SessionSharedObjectsMap sessionMap = SessionSharedObjectsMap.getCurrent();
         Object obj = sessionMap.getObject(docId);
         DOC_ID_VS_DOC_MAP = (Map<String, TIFFDocumentWrapper>) sessionMap.getObject("DOC_ID_VS_DOC_MAP");
         if (null == DOC_ID_VS_DOC_MAP)
         {
            DOC_ID_VS_DOC_MAP = new HashMap<String, TIFFDocumentWrapper>();
            sessionMap.setObject("DOC_ID_VS_DOC_MAP", DOC_ID_VS_DOC_MAP);
         }
         if (null == sessionMap.getObject("VIEWER_STATE_SHARE_MAP"))
         {
            sessionMap.setObject("VIEWER_STATE_SHARE_MAP", new HashMap<String, String>());
         }
         //TODO; should handle generic IDocumentContentInfo
         updateDocIdVsDocMap(documentContentInfo);
         if (null != obj && obj instanceof TIFFDocumentHolder)
         {
            docHolder = (TIFFDocumentHolder) obj;
            docHolder.initialize(documentInfo);
            docHolder.setProcessInstance(processInstance);
         }
         else
         {
            docHolder = new TIFFDocumentHolder(documentInfo);
            docHolder.setEditable(documentInfo.isContentEditable());
            docHolder.setProcessInstance(processInstance);
            sessionMap.setObject(docId, docHolder);
            sessionMap.setObject("SESSION_CONTEXT", SessionContext.findSessionContext());
            sessionMap.setObject("DOCUMENT_MANAGEMENT_SERVICE", SessionContext.findSessionContext().getServiceFactory()
                  .getDocumentManagementService());
         }

         FacesUtils.refreshPage();
      }
      catch (Exception e)
      {
         trace.error(e);
         throw new RuntimeException(e);
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.stardust.ui.web.viewscommon.views.document.IDocumentViewer#getContent
    * ()
    */
   public String getContent()
   {
      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.eclipse.stardust.ui.web.viewscommon.views.document.IDocumentViewer#getContentUrl()
    */
   public String getContentUrl()
   {
      return contentUrl;
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.eclipse.stardust.ui.web.viewscommon.views.document.IDocumentViewer#getMimeTypes()
    */
   public MIMEType[] getMimeTypes()
   {
      return mimeTypes;
   }

   /**
    * @return
    */
   public String getPageImageURL()
   {

      return docHolder.getPageImageURL();
   }

   /**
    * @param pageImageURL
    */
   public void setPageImageURL(String pageImageURL)
   {
      this.pageImageURL = pageImageURL;
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.eclipse.stardust.ui.web.viewscommon.views.document.IDocumentViewer#setContent(java
    * .lang.String)
    */
   public void setContent(String content)
   {
	   //Do nothing
   }

   /**
    * @return
    */
   public String getDocId()
   {

      return docId;
   }

   /**
    * @return
    */
   public TIFFDocumentHolder getTiffDocumentHolder()
   {
      return docHolder;
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.eclipse.stardust.ui.web.common.event.ViewEventHandler#handleEvent(com.sungard.framework
    * .ui.event.ViewEvent)
    */
   public void handleEvent(ViewEvent event)
   {
      switch (event.getType())
      {
      case TO_BE_ACTIVATED:
         activate();
         break;
      case ACTIVATED:
         break;
      case TO_BE_DEACTIVATED:
         deActivateIframe();
         //restoreParentIframeSize();
         break;
      case CLOSED:
         close();
         cleanViewerStateShareMap();
         break;
      case LAUNCH_PANELS_ACTIVATED:
         restoreTiffIframe();
         break;
      case LAUNCH_PANELS_DEACTIVATED:
         restoreTiffIframe();
         break;
      case FULL_SCREENED:
         restoreTiffIframe();
         break;
      case RESTORED_TO_NORMAL:
         restoreTiffIframe();
         break;
      case POPPED_IN:
         if (PortalApplication.getInstance().getFocusView() == event.getView()
               || PortalApplication.getInstance().getActiveView() == event.getView())
         {
            String publishReinitializeMsg = "window.parent.EventHub.events.publish('RE_INITIALIZE_VIEWER');";
            JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), publishReinitializeMsg);
            PortalApplication.getInstance().addEventScript(publishReinitializeMsg);
            activate();
         }
         break;
      case TO_BE_PINNED:
         restoreTiffIframe();
         break;
      }
   }
   
   public void handleEvent(DocumentEventType eventType)
   {
      switch (eventType)
      {
      case DETAILS_PANEL_COLLAPSED:
         restoreTiffIframe();
         break;
      case DETAILS_PANEL_EXPANDED:
         restoreTiffIframe();
         break;
      case POPPED_OUT:
         poppedOut = true;
         deActivateIframe();
         break;
      case POPPED_IN:
         poppedOut = false;
         break;
      case SHOW_PREVIOUS_VERSION_TO_BE_INVOKED:
         close();
         break;
      case SHOW_PREVIOUS_VERSION_INVOKED:
         activate();
         break;
      case SHOW_NEXT_VERSION_TO_BE_INVOKED:
         close();
         break;
      case SHOW_NEXT_VERSION_INVOKED:
         activate();
         break;
      case REFRESH_VIWER_TO_BE_INVOKED:
         close();
         break;
      case REFRESH_VIWER_INVOKED:
         activate();
         break;
      case DOCUMENT_DELETED:
         closeIframe();
         break;
      }
   }
   
   /**
    * 
    */
   public void activate()
   {
      if (!isPoppedOut())
      {
         String pagePath = docHolder.getDefaultPagePath();
         pagePath += "&isEditable=" + documentInfo.isContentEditable();
         Map<String, String> imageViewerConfigOptions = readImageViewerConfiguration();
         SessionSharedObjectsMap.getCurrent().setObject(imgConfigMapKey, imageViewerConfigOptions);
         docHolder.setShowHideFlag(Boolean.valueOf(imageViewerConfigOptions.get("showSidePanel")));
         FacesContext facesContext = FacesContext.getCurrentInstance();

         restoreTiffIframe();
         String anchor = "tiffViewerIframe";
         PortalApplication.getInstance().addEventScript(
               "window.setTimeout(function() {InfinityBpm.ProcessPortal.createOrActivateContentFrame('" + frameId
                     + "', '" + pagePath + "' + '&canvasWidth=' + docWidth + '&canvasHeight=' + docHeight, {anchorId:'"
                     + anchor + "'});},"+this.iframeDelay+");");
      }
   }

   /**
    * 
    */
   public void close()
   {      
      String publishSaveMsg = "window.parent.EventHub.events.publish('VIEW_CLOSING', '" + docId + "');";
      JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), publishSaveMsg);
      PortalApplication.getInstance().addEventScript(publishSaveMsg);
      DOC_ID_VS_DOC_MAP.remove(docId);
      closeIframe();
   }
   
   private boolean allSaveOptionsChecked(TIFFCustomSaveDialog saveOptions)
   {
      boolean allChecked = true;
      if (!saveOptions.isSaveAnnotations() || !saveOptions.isSaveBookmarks() || !saveOptions.isSavePageOrder() || !saveOptions.isSavePageRotation())
      {
         allChecked = false;
      }
      
      return allChecked;
   }
   
   private List<String> getIds(Iterator<? extends Identifiable> iter)
   {
      List<String> ids = new ArrayList<String>();
      
      while (iter.hasNext())
      {
         ids.add(iter.next().getId());
      }
      
      return ids;
   }
   
   private List<Integer> getPgNos(Iterator<PageOrientation> iter)
   {
      List<Integer> pgNos = new ArrayList<Integer>();
      
      while (iter.hasNext())
      {
         pgNos.add(iter.next().getPageNumber());
      }
      
      return pgNos;
   }
   
   /**
    * 
    */
   public void toggleShowHideFlag()
   {
      getTiffDocumentHolder().toggleShowHideFlag();
      restoreTiffIframe();
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.eclipse.stardust.ui.web.viewscommon.views.document.IDocumentViewer#getToolbarUrl()
    */
   public String getToolbarUrl()
   {
      // TODO Auto-generated method stub
      return null;
   }

   /**
    * 
    */
   public void restoreTiffIframe()
   {
      if (!isPoppedOut())
      {
         calculateOptimalDivSize();
         String anchor = "tiffViewerIframe";
         String restoreTiffIframe = "if (document.getElementById('tiffViewerIframe')) {"
         	   + "try{document.getElementById('tiffViewerIframe').style.width = (docWidth + 10) + 'px';"
               + "document.getElementById('tiffViewerIframe').style.height = (docHeight + 70) + 'px';"
               + "window.parent.EventHub.events.publish('CANVAS_RESIZED', docWidth, docHeight);}catch(e){}"
               + "InfinityBpm.ProcessPortal.resizeContentFrame('" + frameId + "', {anchorId:'" + anchor + "'});}";

         JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), restoreTiffIframe);
         PortalApplication.getInstance().addEventScript(restoreTiffIframe);
      }
   }
   
   private void updateDocIdVsDocMap(IDocumentContentInfo docInfo)
   {
      if (null == docInfo.getAnnotations())
      {
         docInfo.setAnnotations((DocumentAnnotations) new PrintDocumentAnnotationsImpl());
      }
      
      DOC_ID_VS_DOC_MAP.put(docId, new TIFFDocumentWrapper(docInfo));
   }
   
   /**
    * 
    */
   private void calculateOptimalDivSize()
   {
      String calculateOptimalDivSizeJS = "var divObj = document.getElementById('tiffViewerIframe');"
            + "var topValue= 0,leftValue= 0;" + "while(divObj) {" + "leftValue+= divObj.offsetLeft;"
            + "topValue+= divObj.offsetTop;" + "divObj = divObj.offsetParent;" + "}"
            + "var windowWidth = document.body.clientWidth, windowHeight = document.body.clientHeight;"
            + "var docWidth = parseInt((document.body.clientWidth - leftValue)) - 30;"
            + "var docHeight = parseInt(docWidth * 1.4);";

      JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), calculateOptimalDivSizeJS);
      PortalApplication.getInstance().addEventScript(calculateOptimalDivSizeJS);
   }

   /**
    * 
    */
   public void deActivateIframe()
   {
      String deActivateIframeJS = "window.setTimeout(function() {InfinityBpm.ProcessPortal.deactivateContentFrame('" + frameId + "');},"+this.iframeDelay+");";
      JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), deActivateIframeJS);
      PortalApplication.getInstance().addEventScript(deActivateIframeJS);
   }

   private void closeIframe()
   {
      String closeIframeJS = "InfinityBpm.ProcessPortal.closeContentFrame('" + frameId + "');";
      JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), closeIframeJS);
      PortalApplication.getInstance().addEventScript(closeIframeJS);
   }
   
   private Map<String, String> readImageViewerConfiguration()
   {
      Map<String, String> imageConfig = new HashMap<String, String>();
      ImageViewerConfigurationBean imgConfigBean = ImageViewerConfigurationBean.getCurrent();
      imageConfig.put("zoomLevel", imgConfigBean.getSelectedDisplayZoomLevelEnum());
      imageConfig.put("showSidePanel", String.valueOf(imgConfigBean.isShowSidePanel()));
      imageConfig.put("isInverted", String.valueOf(imgConfigBean.isInvertImage()));
      imageConfig.put("noteColour", imgConfigBean.getStickyNoteColour());
      imageConfig.put("isBold", String.valueOf(imgConfigBean.isBoldSelected()));
      imageConfig.put("isItalic", String.valueOf(imgConfigBean.isItalicSelected()));
      imageConfig.put("isUnderlined", String.valueOf(imgConfigBean.isUnderlineSelected()));
      imageConfig.put("noteFontSize", imgConfigBean.getSelectedNoteFontSize());
      imageConfig.put("highlighterColour", imgConfigBean.getHighlighterColour());
      imageConfig.put("showAnnotations", String.valueOf(imgConfigBean.isShowAnnotations()));
      imageConfig.put("selectedStamp", imgConfigBean.getSelectedStamp());
      imageConfig.put("isDatanameIncludedInTarget", String.valueOf(imgConfigBean.isDatanameInTargetIncluded()));
      imageConfig.put("isHighlightDataFieldEnabled", String.valueOf(imgConfigBean.isHighlightDataFieldsEnabled()));
      imageConfig.put("dataHighlightColour", imgConfigBean.getDataFieldHighlightColour());
      imageConfig.put("dataHighlightOpacity", imgConfigBean.getDataFieldHighlightOpacity());
      imageConfig.put("magnifyFields", String.valueOf(imgConfigBean.isMagnifyFields()));
      
      return imageConfig;
   }
   
   private void cleanViewerStateShareMap()
   {
      Map<String, String> stateShareMap = (Map<String, String>) SessionSharedObjectsMap.getCurrent().getObject("VIEWER_STATE_SHARE_MAP");
      if (null != stateShareMap)
      {
         stateShareMap.remove(docId);
      }
   }
   
   public ProcessInstance getProcessInstance()
   {
      return processInstance;
   }

   public IDocumentContentInfo save() throws ResourceNotFoundException
   {
      TIFFCustomSaveDialog saveOptions = TIFFCustomSaveDialog.getCurrent();
      IDocumentContentInfo docInfoFreshCopy = null;

      // Get a fresh document object if a partial save is needed
      // Else us the updated document object
      if (allSaveOptionsChecked(saveOptions))
      {
         docInfoFreshCopy = DOC_ID_VS_DOC_MAP.get(docId).getDocInfo();
      }
      else
      {
         PrintDocumentAnnotationsImpl updatedMetadata = (PrintDocumentAnnotationsImpl) DOC_ID_VS_DOC_MAP.get(docId)
               .getDocInfo().getAnnotations();
         docInfoFreshCopy = documentInfo.reset();
         PrintDocumentAnnotationsImpl saveableMetadata = (PrintDocumentAnnotationsImpl) docInfoFreshCopy
               .getAnnotations();
         if (null == saveableMetadata)
         {
            saveableMetadata = new PrintDocumentAnnotationsImpl();
            docInfoFreshCopy.setAnnotations(saveableMetadata);
         }

         if (saveOptions.isSaveAnnotations())
         {
            saveableMetadata.setHighlights(updatedMetadata.getHighlights());
            saveableMetadata.setNotes(updatedMetadata.getNotes());
            saveableMetadata.setStamps(updatedMetadata.getStamps());
         }
         if (saveOptions.isSaveBookmarks())
         {
            saveableMetadata.setBookmarks(updatedMetadata.getBookmarks());
            if (null != updatedMetadata.getDefaultBookmark())
            {
               saveableMetadata.setDefaultBookmark(updatedMetadata.getDefaultBookmark().getId());
            }
            // It would be better to have the API enhanced to reset the default
            // bookmark rather than setting it to empty string.
            else if (null == updatedMetadata.getDefaultBookmark() && null != saveableMetadata.getDefaultBookmark())
            {
               saveableMetadata.setDefaultBookmark("");
            }
         }
         if (saveOptions.isSavePageOrder())
         {
            saveableMetadata.setPageSequence(updatedMetadata.getPageSequence());
         }
         if (saveOptions.isSavePageRotation())
         {
            saveableMetadata.setPageOrientations(updatedMetadata.getPageOrientations());
         }
      }

      return docInfoFreshCopy;
   
   }

   public boolean isModified()
   {
      TIFFDocumentWrapper statusWrapper = DOC_ID_VS_DOC_MAP.get(docId);
      return (statusWrapper.isPageSequenceChanged() || statusWrapper.isBookmarkChanged()
            || statusWrapper.isBookmarkChanged() || statusWrapper.isRotationChanged() || statusWrapper
            .isAnnotationChanged());
   }

   public boolean usesCustomSaveDialog()
   {
      return true;
   }

   public boolean isDescriptionChanged()
   {
      return descriptionChanged;
   }

   public void setDescriptionChanged(boolean descriptionChanged)
   {
      this.descriptionChanged = descriptionChanged;
   }

   public boolean isPoppedOut()
   {
      return poppedOut;
   }

   public int getIframeDelay()
   {
      return iframeDelay;
   }

   public void setIframeDelay(int iframeDelay)
   {
      this.iframeDelay = iframeDelay;
   }
   
   public void setCustomSaveDialogOptions()
   {
      TIFFCustomSaveDialog saveDialog = TIFFCustomSaveDialog.getCurrent();
      if (documentInfo instanceof FileSystemJCRDocument)
      {
         saveDialog.setFileSystemDocument(true);
      }
      TIFFDocumentWrapper statusWrapper = DOC_ID_VS_DOC_MAP.get(docId);
      statusWrapper.setDocDetailsChanged(isDescriptionChanged());
      saveDialog.setTiffDocument(true);
      saveDialog.setSavePageOrder(statusWrapper.isPageSequenceChanged());
      saveDialog.setShowSavePageOrder(statusWrapper.isPageSequenceChanged());
      saveDialog.setSaveBookmarks(statusWrapper.isBookmarkChanged());
      saveDialog.setShowSaveBookmarks(statusWrapper.isBookmarkChanged());
      saveDialog.setSavePageRotation(statusWrapper.isRotationChanged());
      saveDialog.setShowSavePageRotation(statusWrapper.isRotationChanged());
      saveDialog.setSaveAnnotations(statusWrapper.isAnnotationChanged());
      saveDialog.setShowSaveAnnotations(statusWrapper.isAnnotationChanged());
      saveDialog.setSaveDocumentData(statusWrapper.isDocDetailsChanged());
      saveDialog.setShowSaveDocumentData(statusWrapper.isDocDetailsChanged());
   }

   public String getCustomDialogURL()
   {
      return customSaveDialogURL;
   }

   public CustomDialogPosition getDialogPosition()
   {
      return CustomDialogPosition.ADD_AFTER;
   }

   public void closeDocument()
   {}   

   /**
    * @return
    */
   private void setDocumentId(IDocumentContentInfo docInfo)
   {
      docId = (docInfo instanceof FileSystemDocument)
            ? DocumentMgmtUtility.stripOffSpecialCharacters(docInfo.getId())
            : docInfo.getId();
   }
}
