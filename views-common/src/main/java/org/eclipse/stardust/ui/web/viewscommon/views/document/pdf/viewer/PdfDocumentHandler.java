/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    ICEsoft Technologies Canada, Corp. - initial API and implementation
 *    SunGard CSA LLC                    - additional modifications
 *******************************************************************************/

// Note: This file is derived from http://anonsvn.icefaces.org/repo/icepdf/tags/icepdf-3.0.0/icepdf/examples/icefaces/src/org/icepdf/examples/jsf/viewer/view/DocumentManager.java (r18941)

package org.eclipse.stardust.ui.web.viewscommon.views.document.pdf.viewer;

import java.util.Random;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.ValueChangeEvent;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.common.util.StringUtils;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler.MessageDisplayMode;
import org.eclipse.stardust.ui.web.viewscommon.views.document.IDocumentContentInfo;


import com.icesoft.faces.context.effects.JavascriptContext;

public class PdfDocumentHandler
{
   private static final Logger trace = LogManager.getLogger(PdfDocumentHandler.class);
   private PdfDocumentState currentDocumentState;
   private boolean showOutlinePopup;
   private String documentId;
   private String outlinePopupId;
   private String errorMessage;
   private MessagesViewsCommonBean msgBean;
   
   /**
    * @param docId
    */
   public PdfDocumentHandler(IDocumentContentInfo documentContentInfo)
   {
      super();
      msgBean = MessagesViewsCommonBean.getInstance();
      documentId = documentContentInfo.getId();
      currentDocumentState = new PdfDocumentState(documentContentInfo);
      try
      {
         currentDocumentState.openDocument();
         errorMessage = null;
      }
      catch (Exception e)
      {
         trace.error("Error while initializing the document.", e);
         errorMessage = msgBean.getString("views.documentView.pdfException");
      }
   }

   public PdfDocumentState getCurrentDocumentState()
   {
      return currentDocumentState;
   }

   /**
    * Updates the current document state page cursor to point to the next logical page in
    * the document. Nothing happens if there is now documents loaded or if the page cursor
    * is at the end of the document.
    * 
    * @param event
    *           jsf action event.
    */
   public void nextPage(ActionEvent event)
   {
      try
      {
         // if their is is a currentDocument then go to the next page.
         if (null != getCurrentDocumentState())
         {
            int totalPages = getCurrentDocumentState().getDocumentLength();
            int currentPage = getCurrentDocumentState().getPageCursor();

            currentPage++;

            if (currentPage > totalPages)
            {
               currentPage = totalPages;
            }
            getCurrentDocumentState().setPageCursor(currentPage);
            refreshDocumentState();
         }
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e, msgBean.getString("common.unableToPerformAction"),
               MessageDisplayMode.ONLY_CUSTOM_MSG);
      }
   }

   /**
    * Updates the current document state page cursor to point to the previous logical page
    * in the document. Nothing happens if there is now documents loaded or if the page
    * cursor is at the begining of the document.
    * 
    * @param event
    *           jsf action event.
    */
   public void previousPage(ActionEvent event)
   {
      try
      {
         // if their is is a currentDocument then go to the next page.
         if (null != getCurrentDocumentState())
         {
            int currentPage = getCurrentDocumentState().getPageCursor();
            currentPage--;
            if (currentPage < 1)
            {
               currentPage = 1;
            }
            getCurrentDocumentState().setPageCursor(currentPage);

            // refresh current page state.
            refreshDocumentState();
         }
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e, msgBean.getString("common.unableToPerformAction"),
               MessageDisplayMode.ONLY_CUSTOM_MSG);
      }
   }

   /**
    * Rotate the current document state by 90 degrees.
    * 
    * @param event
    *           jsf action event.
    */
   public void rotateDocumentRight(ActionEvent event)
   {
      try
      {
         if (null != getCurrentDocumentState())
         {
            float viewRotation = getCurrentDocumentState().getRotation();
            viewRotation -= PdfDocumentState.ROTATION_FACTOR;

            if (viewRotation < 0)
            {
               viewRotation += 360;
            }
            getCurrentDocumentState().setRotation(viewRotation);

            // refresh current page state.
            refreshDocumentState();
         }
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e, msgBean.getString("common.unableToPerformAction"),
               MessageDisplayMode.ONLY_CUSTOM_MSG);
      }
   }

   /**
    * Rotate the current document state by -90 degrees.
    * 
    * @param event
    *           jsf action event.
    */
   public void rotateDocumentLeft(ActionEvent event)
   {
      try
      {
         if (null != getCurrentDocumentState())
         {
            float viewRotation = getCurrentDocumentState().getRotation();
            viewRotation += PdfDocumentState.ROTATION_FACTOR;
            viewRotation %= 360;

            getCurrentDocumentState().setRotation(viewRotation);

            // refresh current page state.
            refreshDocumentState();
         }
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e, msgBean.getString("common.unableToPerformAction"),
               MessageDisplayMode.ONLY_CUSTOM_MSG);
      }
   }

   /**
    * Go to the page number specifed by the current document state. If the page number is
    * not in the range of the documents pages it is altered to the nearest bound.
    * 
    * @param event
    *           jsf action event.
    */
   public void goToPage(ActionEvent event)
   {
      try
      {
         if (null != getCurrentDocumentState())
         {
            int totalPages = getCurrentDocumentState().getDocumentLength();
            int currentPage = getCurrentDocumentState().getPageCursor();

            if (currentPage > totalPages)
            {
               getCurrentDocumentState().setPageCursor(totalPages);
            }
            if (currentPage < 1)
            {
               getCurrentDocumentState().setPageCursor(1);
            }

            // refresh current page state.
            refreshDocumentState();
         }
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e, msgBean.getString("common.unableToPerformAction"),
               MessageDisplayMode.ONLY_CUSTOM_MSG);
      }
   }

   private void refreshDocumentState()
   {
      if (null != getCurrentDocumentState())
      {
         getCurrentDocumentState().calculatePageImageSize();
      }
   }

   public void documentZoomLevelChange(ValueChangeEvent event)
   {
      if (event.getPhaseId() != PhaseId.INVOKE_APPLICATION)
      {
         event.setPhaseId(PhaseId.INVOKE_APPLICATION);
         event.queue();
      }
      refreshDocumentState();
   }

   public void goToDestination(ActionEvent event)
   {

      FacesContext context = FacesContext.getCurrentInstance();

      int pageNumber = Integer.parseInt((String) context.getExternalContext().getRequestParameterMap()
            .get("pageNumber"));
      currentDocumentState.setPageCursor(pageNumber + 1);

      // refresh current page state.
      refreshDocumentState();

   }

   public boolean isShowOutlinePopup()
   {
      return showOutlinePopup;
   }

   public void toggleShowOutline()
   {
      showOutlinePopup = false;
      addPopupCenteringScript();
   }

   public void showOutlinePopup(ActionEvent event)
   {
      showOutlinePopup = true;
      addPopupCenteringScript();
   }

   public void goToFirstPage(ActionEvent event)
   {
      try
      {
         if (null != getCurrentDocumentState())
         {
            getCurrentDocumentState().setPageCursor(1);
            refreshDocumentState();
         }
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e, msgBean.getString("common.unableToPerformAction"),
               MessageDisplayMode.ONLY_CUSTOM_MSG);
      }
   }

   public void goToLastPage(ActionEvent event)
   {
      try
      {
         if (null != getCurrentDocumentState())
         {

            int totalPages = getCurrentDocumentState().getDocumentLength();
            getCurrentDocumentState().setPageCursor(totalPages);
            refreshDocumentState();
         }
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e, msgBean.getString("common.unableToPerformAction"),
               MessageDisplayMode.ONLY_CUSTOM_MSG);
      }
   }

   public void zoomIn()
   {
      if (3.0f >= getCurrentDocumentState().getZoom())
      {
         getCurrentDocumentState().setZoom(getCurrentDocumentState().getZoom() + 0.1f);
      }
      refreshDocumentState();
   }

   public void zoomOut()
   {
      if (0.1f < getCurrentDocumentState().getZoom())
      {
         getCurrentDocumentState().setZoom(getCurrentDocumentState().getZoom() - 0.1f);
      }
      refreshDocumentState();
   }

   public boolean isFirstPage()
   {
      try
      {
         if (null != getCurrentDocumentState())
         {
            if (getCurrentDocumentState().getPageCursor() == 1)
               return true;
         }
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e, msgBean.getString("common.unableToPerformAction"),
               MessageDisplayMode.ONLY_CUSTOM_MSG);
      }
      return false;
   }

   public boolean isLastPage()
   {
      try
      {
         if (null != getCurrentDocumentState())
         {
            if (getCurrentDocumentState().getPageCursor() == getCurrentDocumentState().getDocumentLength())
               return true;
         }
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e, msgBean.getString("common.unableToPerformAction"),
               MessageDisplayMode.ONLY_CUSTOM_MSG);
      }
      return false;
   }

   public boolean isMaxZoom()
   {
      if (null != getCurrentDocumentState())
         if (getCurrentDocumentState().getZoom() >= 3.0f)
            return true;
      return false;
   }

   public boolean isMinZoom()
   {
      if (null != getCurrentDocumentState())
         if (getCurrentDocumentState().getZoom() <= 0.1f)
            return true;
      return false;
   }

   public boolean isHavingOutline()
   {
      if (null != getCurrentDocumentState())
         if (null != getCurrentDocumentState().getOutline())
            return true;
      return false;
   }
   
   /**
    * 
    */
   private void addPopupCenteringScript()
   {
      if (showOutlinePopup)
      {
         String positionPopupScript = "InfinityBpm.Core.positionMessageDialog('" + getOutlinePopupId() + "');";
         JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), positionPopupScript);
         PortalApplication.getInstance().addEventScript(positionPopupScript);
      }
   }

   public String getDocumentId()
   {
      return documentId;
   }
   
   /**
    * @return
    */
   public String getOutlinePopupId()
   {
      if(StringUtils.isEmpty(outlinePopupId))
      {
         Random o = new Random();
         outlinePopupId = "UIC" + o.nextInt(10000);
      }
      return outlinePopupId;
   }

   public String getErrorMessage()
   {
      return errorMessage;
   }
}