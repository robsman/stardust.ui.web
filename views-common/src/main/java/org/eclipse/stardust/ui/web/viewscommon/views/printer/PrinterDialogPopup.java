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
package org.eclipse.stardust.ui.web.viewscommon.views.printer;

import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.eclipse.stardust.ui.web.common.PopupUIComponentBean;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.viewscommon.views.document.pdf.PdfResource;
import org.eclipse.stardust.ui.web.viewscommon.views.document.pdf.PrintingPreferences;


/**
 * @author Yogesh.Manware
 * 
 */
public class PrinterDialogPopup extends PopupUIComponentBean
{
   private static final long serialVersionUID = 1L;
   private static final String BEAN_NAME = "printerDialogPopup";
   private PdfResource pdfResource;
   private PrintingPreferences printingPreferences = new PrintingPreferences();

   public PrinterDialogPopup()
   {
      super("correspondenceView");
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.stardust.ui.web.common.UIComponentBean#initialize()
    */
   public void initialize()
   {}

   /**
    * @return current instance
    */
   public static PrinterDialogPopup getCurrent()
   {
      return (PrinterDialogPopup) FacesUtils.getBeanFromContext(BEAN_NAME);
   }

   public SelectItem[] getMargin()
   {
      return printingPreferences.getMargin();
   }

   public void orientationChanged(ValueChangeEvent vce)
   {
      printingPreferences.setOrientation((String) vce.getNewValue());
   }

   public void pageSizeChanged(ValueChangeEvent vce)
   {
      printingPreferences.setPageSize((String) vce.getNewValue());
   }

   public void leftMarginChanged(ValueChangeEvent vce)
   {
      printingPreferences.setLeft(Float.parseFloat(((String) vce.getNewValue())));
   }

   public void rightMarginChanged(ValueChangeEvent vce)
   {
      printingPreferences.setRight(Float.parseFloat(((String) vce.getNewValue())));
   }

   public void topMarginChanged(ValueChangeEvent vce)
   {
      printingPreferences.setTop(Float.parseFloat(((String) vce.getNewValue())));
   }

   public void bottomMarginChanged(ValueChangeEvent vce)
   {
      printingPreferences.setBottom(Float.parseFloat(((String) vce.getNewValue())));
   }

   public void setPdfResource(PdfResource pdfResource)
   {
      this.pdfResource = pdfResource;
   }

   public PdfResource getPdfResource()
   {
      return this.pdfResource;
   }

   public PrintingPreferences getPrintingPreferences()
   {
      return printingPreferences;
   }

   public void setPrintingPreferences(PrintingPreferences printingPreferences)
   {
      this.printingPreferences = printingPreferences;
   }
}
