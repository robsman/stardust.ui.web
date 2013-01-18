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

import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;

import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.common.app.PortalUiController;
import org.eclipse.stardust.ui.web.common.app.View;
import org.eclipse.stardust.ui.web.common.event.ViewEvent.ViewEventType;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.viewscommon.views.document.IDocumentEventListener.DocumentEventType;


/**
 * helps to pop-out the document in new window
 * 
 * @author yogesh.manware
 * 
 */
public class ExternalDocumentViewerBean
{
   private static final String BEAN_NAME = "externalDocumentViewerBean";
   private static final String VIEWER = "/plugins/views-common/views/document/externalDocumentView.iface";
   private String documentId;
   private String icon;
   private String label;
   private IDocumentViewer contentHandler;
   private boolean opened;
   
   private PortalUiController portalUiController;
   private View view;

   public static ExternalDocumentViewerBean getInstance()
   {
      return (ExternalDocumentViewerBean) FacesUtils.getBeanFromContext(BEAN_NAME);
   }

   /**
    * open popup
    * 
    * @param documentId
    */
   public void openDocument(DocumentHandlerBean documentHandlerBean, View view)
   {      
      initialize(documentHandlerBean, view);
      openDocument();
   }

   private void initialize(DocumentHandlerBean documentHandlerBean, View view)
   {
      this.documentId = documentHandlerBean.getDocumentContentInfo().getId();
      this.view = view;
      contentHandler = documentHandlerBean.getContentHandler();
      label = documentHandlerBean.getLabel();
      icon = documentHandlerBean.getDocumentContentInfo().getIcon();
   }

   /**
    * open popup
    * 
    * @param documentId
    */
   public void openDocument()
   {
      if (null != documentId && !opened)
      {
         String contextPath = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath();
         String url = contextPath + VIEWER;
         String jsFunction = "openWindow('" + url + "');";
         //JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), jsFunction);
         PortalApplication.getInstance().addEventScript(jsFunction);
         setOpened(true);
      }
      else
      {
         PortalApplication.getInstance().renderPortalSession();
      }
   }

   /**
    * closing the window from outside window
    */
   public void closePopupDocument()
   {
      PortalApplication.getInstance().addEventScript("closeWindow();");
      setOpened(false);
   }

   /**
    * This method gets invoked when the window is being unloaded (includes window closure
    * event)
    * 
    * @param event
    */
   public void closeWindowListener(ValueChangeEvent event)
   {
      if ("true".equals(event.getNewValue()))
      {
         PortalApplication.getInstance().renderPortalSession();
         setOpened(false);         
         getPortalUiController().broadcastNonVetoableViewEvent(view, ViewEventType.POPPED_IN);
      }
   }

   /**
    * 
    * @return
    */
   public String getPortalHeader()
   {
      return FacesUtils.getPortalTitle();
   }
  
   public boolean isOpened()
   {
      return opened;
   }

   public void setOpened(boolean opened)
   {
      this.opened = opened;
      if (!this.opened && (contentHandler instanceof IDocumentEventListener))
      {
         ((IDocumentEventListener) contentHandler).handleEvent(DocumentEventType.POPPED_IN);
      }
   }

   public String getIcon()
   {
      return icon;
   }

   public String getLabel()
   {
      return label;
   }

   public IDocumentViewer getContentHandler()
   {
      return contentHandler;
   }

   public String getDocumentId()
   {
      return documentId;
   }

   public PortalUiController getPortalUiController()
   {
      return portalUiController;
   }

   public void setPortalUiController(PortalUiController portalUiController)
   {
      this.portalUiController = portalUiController;
   }   
}
