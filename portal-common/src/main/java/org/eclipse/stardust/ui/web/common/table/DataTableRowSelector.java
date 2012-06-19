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
package org.eclipse.stardust.ui.web.common.table;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.common.util.ReflectionUtils;

import com.icesoft.faces.component.ext.ClickActionEvent;
import com.icesoft.faces.component.ext.RowSelectorEvent;


/**
 * @author Subodh.Godbole
 *
 */
public class DataTableRowSelector implements Serializable
{
   private static final long serialVersionUID = 1L;

   private static final Logger trace = LogManager.getLogger(DataTableRowSelector.class);

   private String rowProperty;
   private boolean multiple;
   private boolean enhancedMultiple;
   private int dblClickDelay = 200;
   private boolean preStyleOnSelection;
   private int clickedRow = -1;

   private EventListener eventListener;

   /**
    * @param rowProperty
    */
   public DataTableRowSelector(String rowProperty)
   {
      this(rowProperty, false);
   }

   /**
    * @param rowProperty
    * @param multiple
    */
   public DataTableRowSelector(String rowProperty, boolean multiple)
   {
      this(rowProperty, null, multiple);
   }

   /**
    * @param rowProperty
    * @param eventListener
    * @param multiple
    */
   public DataTableRowSelector(String rowProperty, EventListener eventListener, boolean multiple)
   {
      this.rowProperty = rowProperty;
      this.eventListener = eventListener;
      this.multiple = multiple;
      
      this.enhancedMultiple = multiple ? true : false;
   }

   /**
    * @param event
    */
   public void rowClicked(ClickActionEvent event)
   {
      if (null != eventListener)
      {
         eventListener.rowClicked(event);
      }
   }

   /**
    * @param event
    */
   public void rowSelected(RowSelectorEvent event)
   {
      if (null != eventListener)
      {
         eventListener.rowSelected(event);
      }
   }

   /**
    * Resets the clicked row / selected row as per data provided
    */
   public void resetSelectedRow(List< ? > list)
   {
      // This applies only to single select mode
      if (!multiple && null != list)
      {
         try
         {
            Boolean selected;
            Map<String, Object> result;
            
            int size = list.size();
            for (int i = 0; i < size; i++)
            {
               result = FacesUtils.getObjectPropertyMapping(list.get(i), rowProperty);
               selected = (Boolean) ReflectionUtils.invokeGetterMethod(result.get("object"),
                     (String) result.get("property"));

               if (selected)
               {
                  clickedRow = i;
                  return;
               }
            }

            clickedRow = -1;
         }
         catch (Exception e)
         {
            trace.error("Failed in resetSelectedRow()", e);
         }
      }
   }
   
   public String getRowProperty()
   {
      return rowProperty;
   }

   public void setRowProperty(String rowProperty)
   {
      this.rowProperty = rowProperty;
   }

   public boolean isMultiple()
   {
      return multiple;
   }

   public void setMultiple(boolean multiple)
   {
      this.multiple = multiple;
   }

   public boolean isEnhancedMultiple()
   {
      return enhancedMultiple;
   }

   public void setEnhancedMultiple(boolean enhancedMultiple)
   {
      this.enhancedMultiple = enhancedMultiple;
   }

   public int getDblClickDelay()
   {
      return dblClickDelay;
   }

   public void setDblClickDelay(int dblClickDelay)
   {
      this.dblClickDelay = dblClickDelay;
   }

   public boolean isPreStyleOnSelection()
   {
      return preStyleOnSelection;
   }

   public void setPreStyleOnSelection(boolean preStyleOnSelection)
   {
      this.preStyleOnSelection = preStyleOnSelection;
   }

   public int getClickedRow()
   {
      return clickedRow;
   }

   public void setClickedRow(int clickedRow)
   {
      this.clickedRow = clickedRow;
   }

   public EventListener getEventListener()
   {
      return eventListener;
   }

   public void setEventListener(EventListener eventListener)
   {
      this.eventListener = eventListener;
   }

   /**
    * @author Subodh.Godbole
    *
    */
   public interface EventListener
   {
      public void rowClicked(ClickActionEvent event);
      public void rowSelected(RowSelectorEvent event);
   }
}
