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
package org.eclipse.stardust.ui.web.viewscommon.common;

import org.eclipse.stardust.ui.web.common.table.DefaultRowModel;

/**
 * 
 * @author Sidharth.Singh
 * @version $Revision: $
 * @since 7.0
 */
public class PriorityAutoCompleteItem extends DefaultRowModel
{
   /**
    * 
    */
   private static final long serialVersionUID = 1L;
   private String label;
   private String icon;
   private int priority;

   private PriorityAutocompleteSelector priorityAutocompleteSelector;

   public PriorityAutoCompleteItem()
   {}

   public PriorityAutoCompleteItem(String label, String icon, int priority)
   {
      this.label = label;
      this.icon = icon;
      this.priority = priority;
   }

   public String getLabel()
   {
      return label;
   }

   public void setLabel(String label)
   {
      this.label = label;
   }

   public String getIcon()
   {
      return icon;
   }

   public void setIcon(String icon)
   {
      this.icon = icon;
   }

   public int getPriority()
   {
      return priority;
   }

   public void setPriority(int priority)
   {
      this.priority = priority;
   }

   public PriorityAutocompleteSelector getPriorityAutocompleteSelector()
   {
      return priorityAutocompleteSelector;
   }

   public void setPriorityAutocompleteSelector(PriorityAutocompleteSelector priorityAutocompleteSelector)
   {
      this.priorityAutocompleteSelector = priorityAutocompleteSelector;
   }

   public void removeFromList()
   {
      priorityAutocompleteSelector.removePriority(this);
   }
   
   @Override
   public boolean equals(Object equateTo)
   {
      if (null != equateTo && equateTo instanceof PriorityAutoCompleteItem)
      {
         PriorityAutoCompleteItem eTo = (PriorityAutoCompleteItem) equateTo;
         if (eTo.getLabel().equals(getLabel()))
         {
            return true;
         }
      }

      return false;
   }

}
