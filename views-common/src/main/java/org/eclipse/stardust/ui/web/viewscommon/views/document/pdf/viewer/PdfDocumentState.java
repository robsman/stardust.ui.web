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

package org.eclipse.stardust.ui.web.viewscommon.views.document.pdf.viewer;

import java.awt.Image;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;
import javax.swing.tree.DefaultTreeModel;

import org.eclipse.stardust.ui.web.common.configuration.UserPreferencesHelper;
import org.eclipse.stardust.ui.web.common.views.PortalConfiguration;
import org.eclipse.stardust.ui.web.viewscommon.common.configuration.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler.MessageDisplayMode;
import org.eclipse.stardust.ui.web.viewscommon.views.document.IDocumentContentInfo;
import org.icepdf.core.exceptions.PDFException;
import org.icepdf.core.exceptions.PDFSecurityException;
import org.icepdf.core.pobjects.Document;
import org.icepdf.core.pobjects.Outlines;
import org.icepdf.core.pobjects.PDimension;
import org.icepdf.core.pobjects.Page;
import org.icepdf.core.util.GraphicsRenderingHints;

public class PdfDocumentState
{
   // default rotation factor increment.
   public static final float ROTATION_FACTOR = 90f;
   
   public static List<SelectItem> preDefinedZoomLevels;
   {
      preDefinedZoomLevels = new ArrayList<SelectItem>();
      preDefinedZoomLevels.add(new SelectItem(0.1f, "10%"));
      preDefinedZoomLevels.add(new SelectItem(0.25f, "25%"));
      preDefinedZoomLevels.add(new SelectItem(0.50f, "50%"));
      preDefinedZoomLevels.add(new SelectItem(0.75f, "75%"));
      preDefinedZoomLevels.add(new SelectItem(1.0f, "100%"));
      preDefinedZoomLevels.add(new SelectItem(1.25f, "125%"));
      preDefinedZoomLevels.add(new SelectItem(1.5f, "150%"));
      preDefinedZoomLevels.add(new SelectItem(2.0f, "200%"));
      preDefinedZoomLevels.add(new SelectItem(3.0f, "300%"));
   }

   private final Object documentLock = new Object();

   private IDocumentContentInfo documentContentInfo;

   private Document pdfDocument;

   // Document outline if present

   private DefaultTreeModel outline;

   // Document state parameters.

   private float zoom;

   private float rotation = 0f;

   private int pageCursor = 1;

   private int maxPages;

   private PDimension pageSize;

   // outline default expanded state
   private boolean outlineExpanded;

   // list of zoom levels
   public List<SelectItem> zoomLevels;
   
   /**
    * Create new document state based on the given document path.
    * 
    * @param documentPath
    *           path to PDF document.
    */
   public PdfDocumentState(IDocumentContentInfo documentContentInfo)
   {
      this.documentContentInfo = documentContentInfo;
      initZoomLevels();
      zoom = Float.valueOf(getUserPrefenceHelper().getSingleString(UserPreferencesEntries.V_IMAGE_VIEWER_CONFIG,
            UserPreferencesEntries.F_IMAGE_VIEWER_SELECTED_PDF_ZOOM_LEVEL, "1.0f"));
   }

   public static float getRotationFactor()
   {
      return ROTATION_FACTOR;
   }
   
   /**
    * initialize Zoom levels
    */
   public void initZoomLevels()
   {
      zoomLevels = new ArrayList<SelectItem>();
      zoomLevels.addAll(preDefinedZoomLevels);
   }

   /**
    * Open the PDF document wrapped by this object. If their is already a document
    * assigned to this document it is closed before the current documentPath is loaded.
    * 
    * @throws PDFException
    * @throws IOException
    * @throws PDFSecurityException
    */
   public void openDocument() throws PDFException, IOException, PDFSecurityException
   {
      synchronized (documentLock)
      {
         if (null == pdfDocument)
         {
            pdfDocument = new Document();
            pdfDocument.setInputStream(new ByteArrayInputStream(documentContentInfo.retrieveContent()), "");
         }
         // document length
         maxPages = pdfDocument.getPageTree().getNumberOfPages();

         // page size
         calculatePageImageSize();

         // build swing outlines.
         Outlines outlines = pdfDocument.getCatalog().getOutlines();

         if (null != outlines && null != outlines.getRootOutlineItem())
         {
            // root tree node
            PdfOutlineTreeNode rootItem = new PdfOutlineTreeNode(pdfDocument.getPageTree(), outlines
                  .getRootOutlineItem());

            // expand root node
            ((PdfOutlineTreeNode.NodeUserObject) rootItem.getUserObject()).setExpanded(true);

            outline = new DefaultTreeModel(rootItem);
            // expand document outline.
            outlineExpanded = true;
         }
         else
         {
            outlineExpanded = false;
         }
      }
   }

   /**
    * Disposed of the ICEpdf document object freeing up server resources.
    */
   public void closeDocument()
   {
      synchronized (documentLock)
      {
         try
         {
            if (null != pdfDocument)
            {
               pdfDocument.dispose();
            }
            pdfDocument = null;
            outline = null;
            maxPages = -1;
         }
         catch (Exception e)
         {
            ExceptionHandler.handleException(e,
                  MessagesViewsCommonBean.getInstance().getString("common.unableToPerformAction"),
                  MessageDisplayMode.ONLY_CUSTOM_MSG);
         }
      }
   }

   /**
    * Gets the total number of pages in the pdfDocument.
    * 
    * @return number of pages in pdfDocument, -1 if the number of pages could not be
    *         determined.
    */
   public int getDocumentLength()
   {
      return maxPages;
   }

   /**
    * Gets the image associated with the current document state.
    * 
    * @return image represented by the pageCursor, rotation and zoom.
    */
   public Image getPageImage()
   {
      synchronized (documentLock)
      {
         if (null != pdfDocument)
         {
            // Check page bounds
            if (pageCursor < 1)
            {
               pageCursor = 1;
            }
            else if (pageCursor > pdfDocument.getPageTree().getNumberOfPages())
            {
               pageCursor = pdfDocument.getPageTree().getNumberOfPages();
            }
            return pdfDocument.getPageImage(pageCursor - 1, GraphicsRenderingHints.SCREEN, Page.BOUNDARY_CROPBOX,
                  rotation, zoom);
         }
         return null;
      }
   }

   /**
    * Gets the page size associated with the current pdfDocument state.
    * 
    * @return page sized specified by the attributes pageCursor, rotation and zoom.
    */
   public void calculatePageImageSize()
   {
      synchronized (documentLock)
      {
         if (null != pdfDocument && null != pdfDocument.getCatalog())
         {
            pageSize = pdfDocument.getPageDimension(pageCursor - 1, rotation, zoom);
         }
         else
         {
            pageSize = new PDimension(1f, 1f);
         }
      }
   }

   public int getPageWidth()
   {
      return (int) pageSize.getWidth();
   }

   public int getPageHeight()
   {
      return (int) pageSize.getHeight();
   }

   /**
    * Invalidates the current page content stream so that
    */
   public void invalidate()
   {
      synchronized (documentLock)
      {
         if (null != pdfDocument)
         {
            Page page;
            // quickly invalidate content streams so we can swap font
            // implementations.
            int max = getDocumentLength();
            for (int i = 0; i < max; i++)
            {
               page = pdfDocument.getPageTree().getPage(i);
               if (page.isInitiated())
               {
                  page.getLibrary().disposeFontResources();
               }
            }
         }
      }
   }

   public float getZoom()
   {
      return zoom;
   }

   public void setZoom(float zoom)
   {
      if (this.zoom != zoom)
      {
         // populate new zoomLevel list
         populateZoomLevel(zoom);
      }
      else
         this.zoom = zoom;
   }

   /**
    * populateZoomLevel
    * 
    * @param zoom
    */
   public void populateZoomLevel(float zoom)
   {
      int zoomInt = Math.round(zoom * 100);

      // clear previous list
      zoomLevels.clear();
      zoomLevels.addAll(preDefinedZoomLevels);

      if (!contains(zoomInt * 1f / 100))
      {
         zoomLevels.add(0, new SelectItem((zoomInt * 1f / 100), zoomInt + "%"));
      }

      this.zoom = zoomInt * 1f / 100;
   }

   /**
    * @param zoomValue
    * @return
    */
   private boolean contains(float zoomValue)
   {
      for (SelectItem zoomLevel : preDefinedZoomLevels)
      {
         Float zValue = (Float) zoomLevel.getValue();
         if (zValue.equals(zoomValue))
         {
            return true;
         }
      }
      return false;
   }
   
   /**
    * @return
    */
   private static UserPreferencesHelper getUserPrefenceHelper()
   {
      return UserPreferencesHelper.getInstance(UserPreferencesEntries.M_VIEWS_COMMON, PortalConfiguration.getInstance()
            .getPrefScopesHelper().getSelectedPreferenceScope());
   }
   public float getRotation()
   {
      return rotation;
   }

   public void setRotation(float rotation)
   {
      this.rotation = rotation;
   }

   public int getPageCursor()
   {
      return pageCursor;
   }

   public void setPageCursor(int pageCursor)
   {
      this.pageCursor = pageCursor;
   }

   public List<SelectItem> getZoomLevels()
   {
      return zoomLevels;
   }

   public DefaultTreeModel getOutline()
   {
      return outline;
   }

   public boolean isOutlineExpanded()
   {
      return outlineExpanded;
   }

   public void setOutlineExpanded(boolean outlineExpanded)
   {
      this.outlineExpanded = outlineExpanded;
   }
}
