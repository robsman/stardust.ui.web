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

import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.model.DataMapping;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.ui.common.form.jsf.DocumentInputController;
import org.eclipse.stardust.ui.common.form.jsf.DocumentPath;
import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.common.app.View;
import org.eclipse.stardust.ui.web.common.app.View.ViewState;
import org.eclipse.stardust.ui.web.common.event.ViewDataEvent;
import org.eclipse.stardust.ui.web.common.event.ViewDataEventHandler;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.util.StringUtils;
import org.eclipse.stardust.ui.web.viewscommon.core.ResourcePaths;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.ICallbackHandler;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentMgmtUtility;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentViewUtil;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.ActivityInstanceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.MIMEType;
import org.eclipse.stardust.ui.web.viewscommon.utils.MimeTypesHelper;
import org.eclipse.stardust.ui.web.viewscommon.views.doctree.CommonFileUploadDialog;
import org.eclipse.stardust.ui.web.viewscommon.views.document.FileSystemJCRDocument;
import org.eclipse.stardust.ui.web.viewscommon.views.document.IDocumentContentInfo;
import org.eclipse.stardust.ui.web.viewscommon.views.document.JCRDocument;



/**
 * @author Subodh.Godbole
 *
 */
public class IppDocumentInputController extends DocumentInputController implements ViewDataEventHandler
{
   private static final Logger trace = LogManager.getLogger(IppDocumentInputController.class);

   private View documentView; 
   private ActivityInstance activityInstance;
   private DataMapping dataMapping;

   /**
    * @param path
    * @param activityInstance
    * @param dataMapping
    */
   public IppDocumentInputController(DocumentPath path, ActivityInstance activityInstance, DataMapping dataMapping)
   {
      super(path);
      this.activityInstance = activityInstance;
      this.dataMapping = dataMapping;

      setDeleteIcon("/plugins/views-common/images/icons/page_white_delete.png");
      setDeleteLabel(MessagesViewsCommonBean.getInstance().getString("views.genericRepositoryView.treeMenuItem.delete"));
   }

   @Override
   public void setValue(Object object)
   {
      super.setValue(object);

      // It's observed that sometimes document is not received as null
      // So check with obvious attributes (id or name), if any of them are empty then consider document as null 
      // Scenario is - 
      // - Open Activity where document is already assigned to Data Mapping.
      // - Delete Document And save the Activity
      // - Now open Activity again - It returns non null document, but actually document should be null 
      if (null != document && (StringUtils.isEmpty(document.getId()) || StringUtils.isEmpty(document.getName())))
      {
         document = null;
      }

      if (null != document)
      {
         setOpenLabel(document.getName());
         MIMEType mimeType = MimeTypesHelper.detectMimeType(document.getName(), document.getContentType());
         setOpenIcon(mimeType.getCompleteIconPath());
      }
      else
      {
         setOpenLabel(MessagesViewsCommonBean.getInstance().getString("views.common.document.noDocument"));
         setOpenIcon(ResourcePaths.I_EMPTY_CORE_DOCUMENT);
      }
   }

   @Override
   public void viewDocument()
   {
      if (null != document)
      {
         unregisterHandler();

         IDocumentContentInfo docInfo = (document instanceof RawDocument) ? getFileSystemDocument() : getJCRDocument();
         Map<String, Object> params = CollectionUtils.newMap();
         params.put("processInstance", activityInstance.getProcessInstance());
         params.put("dataPathId", dataMapping.getDataPath());
         params.put("dataId", dataMapping.getDataId());
         documentView = DocumentViewUtil.openDataMappingDocument(activityInstance.getProcessInstance(),
               dataMapping.getDataId(), docInfo, params);
         PortalApplication.getInstance().registerViewDataEventHandler(documentView, this);
         refreshPortalSession();
      }
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.common.form.jsf.DocumentInputController#saveDocument()
    */
   public boolean saveDocument()
   {
      // Save Document only if it's Raw/Unsaved Document
      if (document instanceof RawDocument)
      {
         FileSystemJCRDocument fsDoc = getFileSystemDocument();
         JCRDocument jcrDoc = (JCRDocument)fsDoc.save(fsDoc.retrieveContent());
         setValue(jcrDoc.getDocument());

         return true;
      }
      
      return false;
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.common.form.jsf.DocumentInputController#closeDocument()
    */
   public void closeDocument()
   {
      if (isDocumentViewerOpened())
      {
         PortalApplication.getInstance().closeView(documentView);
         unregisterHandler();
         refreshPortalSession();
      }
   }

   @Override
   protected void uploadDocument()
   {
      MessagesViewsCommonBean propsBean = MessagesViewsCommonBean.getInstance();

      CommonFileUploadDialog fileUploadDialog = CommonFileUploadDialog.getCurrent();
      fileUploadDialog.initialize();

      fileUploadDialog.setDocumentType(getPath().getDocumentType());
      fileUploadDialog.setHeaderMessage(propsBean.getParamString(
            "views.genericRepositoryView.specificDocument.uploadFile", label));
      fileUploadDialog.setTitle(propsBean.getString("common.fileUpload"));
      fileUploadDialog.setOpenDocument(true);

      fileUploadDialog.setICallbackHandler(new ICallbackHandler()
      {
         public void handleEvent(EventType eventType)
         {
            if (eventType == EventType.APPLY)
            {
               CommonFileUploadDialog fileUploadDialog = CommonFileUploadDialog.getCurrent();
               RawDocument rawDocument = new RawDocument(fileUploadDialog.getFileInfo());
               rawDocument.setDescription(fileUploadDialog.getDescription());
               rawDocument.setComments(fileUploadDialog.getComments());
               rawDocument.setDocumentType(fileUploadDialog.getDocumentType());

               setValue(rawDocument);

               if (fileUploadDialog.isOpenDocument())
               {
                  viewDocument();
               }
            }
         }
      });
      fileUploadDialog.openPopup();
      refreshPortalSession();
   }

   @Override
   public void deleteDocument()
   {
      setValue(null);
      closeDocument();
   }

   @Override
   public void destroy()
   {
      super.destroy();
      unregisterHandler();
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.event.ViewDataEventHandler#handleEvent(org.eclipse.stardust.ui.web.common.event.ViewDataEvent)
    */
   public void handleEvent(ViewDataEvent event)
   {
      switch (event.getType())
      {
      case DATA_MODIFIED:
         Object payload = event.getPayload();
         if (payload instanceof JCRDocument)
         {
            setValue(((JCRDocument)payload).getDocument());
         }
         else
         {
            // Ideally, this should not happen
            trace.error("Received Document is not instanceof JCRDocument");
         }
         break;
      }
   }

   /**
    * @return
    */
   public boolean isDocumentViewerOpened()
   {
      return (null != documentView && ViewState.CLOSED != documentView.getViewState()) ? true : false;
   }

   /**
    * @return
    */
   private FileSystemJCRDocument getFileSystemDocument()
   {
      if (document instanceof RawDocument)
      {
         RawDocument rawDocument = (RawDocument) document;
         String parentFolder = DocumentMgmtUtility.getTypedDocumentsFolderPath(activityInstance.getProcessInstance());
         return new FileSystemJCRDocument(rawDocument.getFileInfo().getPhysicalPath(), document.getDocumentType(),
               parentFolder, rawDocument.getDescription(), rawDocument.getComments());
      }

      return null;
   }

   /**
    * @return
    */
   private JCRDocument getJCRDocument()
   {
      return new JCRDocument(document, getPath().isReadonly());
   }

   /**
    * 
    */
   private void unregisterHandler()
   {
      if (null != documentView)
      {
         PortalApplication.getInstance().unregisterViewDataEventHandler(documentView, this);
      }
   }

   /**
    * 
    */
   private void refreshPortalSession()
   {
      if (ActivityInstanceUtils.isIframeBased(activityInstance))
      {
         PortalApplication.getInstance().renderPortalSession();
      }
   }
}
