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
package org.eclipse.stardust.ui.web.common.filter;

import java.util.ArrayList;

import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.message.MessageDialog;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.common.util.MessagePropertiesBean;
import org.eclipse.stardust.ui.web.common.util.PopupDialog;
import org.eclipse.stardust.ui.web.common.util.StringUtils;


/**
 * @author Subodh.Godbole
 *
 */
public class TableDataFilterPopup extends PopupDialog
{
   private static final long serialVersionUID = 1L;

   private static final Logger trace = LogManager.getLogger(TableDataFilterPopup.class);

   private TableDataFilters dataFilters;
   private ITableDataFilterListener listener;

   private TableDataFilters displayDataFilters;
   
   private ArrayList<String> validationMessags;
   
   private String resetTitle;

   /**
    * @param title
    * @param dataFilters
    * @param listener
    */
   public TableDataFilterPopup(String title, TableDataFilters dataFilters, ITableDataFilterListener listener)
   {
      super(title);
      this.listener= listener;
      this.dataFilters = dataFilters;

      setFireViewEvents(false);

      if(StringUtils.isEmpty(this.getTitle()))
      {
         MessagePropertiesBean propsBean = MessagePropertiesBean.getInstance();
         setTitle( propsBean.getString("common.filterPopup.dataFilterLabel") );
      }
   }

   /**
    * @param dataFilters
    * @param listener
    */
   public TableDataFilterPopup(TableDataFilters dataFilters, ITableDataFilterListener listener)
   {
      this("", dataFilters, listener);
   }
   
   /**
    * @param dataFilters
    */
   public TableDataFilterPopup(TableDataFilters dataFilters)
   {
      this("", dataFilters, null);
   }
   
   /**
    * @param dataFilter
    * @param listener
    */
   public TableDataFilterPopup(ITableDataFilter dataFilter, ITableDataFilterListener listener)
   {
      this(new TableDataFilters(dataFilter), listener);
   }

   /**
    * @param title
    * @param dataFilter
    * @param listener
    */
   public TableDataFilterPopup(String title, ITableDataFilter dataFilter, ITableDataFilterListener listener)
   {
      this(title, new TableDataFilters(dataFilter), listener);
   }

   /**
    * @param title
    * @param dataFilter
    */
   public TableDataFilterPopup(ITableDataFilter dataFilter)
   {
      this("", new TableDataFilters(dataFilter), null);
   }
   
   @Override
   public void apply()
   {
      try
      {
         validationMessags = displayDataFilters.getValidationMessages();
         if(validationMessags.size() == 0) // No validation Errors
         {
            dataFilters.copy(displayDataFilters);
            dataFilters.print();
            setVisible(false);
      
            if(listener != null)
            {
               listener.applyFilter(this.dataFilters);
            }
            else
            {
               trace.debug("TableDataFilterPopup: Listener is NULL can not Apply Filter");
            }
         }
      }
      catch(Exception e)
      {
         MessageDialog.addErrorMessage(MessagePropertiesBean.getInstance().getString("common.unknownError"), e);
      }
   }
   
   @Override
   public void reset()
   {
      displayDataFilters.resetFilters();
   }
   
   @Override
   public void openPopup()
   {
      displayDataFilters = dataFilters.getClone();
      validationMessags = null;
      super.openPopup();
   }
   
   @Override
   public void closePopup()
   {
      super.closePopup();
   // To reset the UI Filter textbox on reset of Form data
      FacesUtils.clearFacesTreeValues();
   }

   /**
    * 
    */
   public void resetAndApply()
   {
      reset();
      apply();
      // To reset the UI Filter textbox on reset of Form data
      FacesUtils.clearFacesTreeValues();
   }
   
   public TableDataFilters getDataFilters()
   {
      return dataFilters;
   }

   public void setListener(ITableDataFilterListener listener)
   {
      this.listener = listener;
   }

   public ITableDataFilterListener getListener()
   {
      return listener;
   }

   public TableDataFilters getDisplayDataFilters()
   {
      return displayDataFilters;
   }
   
   public ArrayList<String> getValidationMessags()
   {
      return validationMessags; 
   }
   
   public String getResetTitle()
   {
      if(StringUtils.isEmpty(resetTitle))
      {
         resetTitle = MessagePropertiesBean.getInstance().getString("common.filterPopup.resetFilter");
      }
      return resetTitle;
   }

   public void setResetTitle(String resetTitle)
   {
      this.resetTitle = resetTitle;
   }
}
