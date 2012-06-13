/*
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * "The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations under
 * the License.
 *
 * The Original Code is ICEfaces 1.5 open source software code, released
 * November 5, 2006. The Initial Developer of the Original Code is ICEsoft
 * Technologies Canada, Corp. Portions created by ICEsoft are Copyright (C)
 * 2004-2006 ICEsoft Technologies Canada, Corp. All Rights Reserved.
 *
 * Contributor(s): _____________________.
 *
 * Alternatively, the contents of this file may be used under the terms of
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"
 * License), in which case the provisions of the LGPL License are
 * applicable instead of those above. If you wish to allow use of your
 * version of this file only under the terms of the LGPL License and not to
 * allow others to use your version of this file under the MPL, indicate
 * your decision by deleting the provisions above and replace them with
 * the notice and other provisions required by the LGPL License. If you do
 * not delete the provisions above, a recipient may use your version of
 * this file under either the MPL or the LGPL License."
 *
 */

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