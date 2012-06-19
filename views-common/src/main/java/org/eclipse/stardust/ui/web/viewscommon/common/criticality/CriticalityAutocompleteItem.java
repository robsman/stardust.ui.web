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
package org.eclipse.stardust.ui.web.viewscommon.common.criticality;

import java.util.StringTokenizer;

import javax.faces.event.ActionEvent;

import org.eclipse.stardust.ui.web.common.table.DefaultRowModel;
import org.eclipse.stardust.ui.web.viewscommon.common.criticality.CriticalityConfigurationUtil.ICON_COLOR;


/**
 * @author Shrikant.Gangal
 *
 */
public class CriticalityAutocompleteItem extends DefaultRowModel
{
   /**
    * 
    */
   private static final long serialVersionUID = 1L;
   private int rangeFrom;
   private int rangeTo;
   private String label;
   private ICON_COLOR iconColor;
   private int iconCount;
   private CriticalityAutocompleteSelector criticalityAutocompleteSelector;
   private double PORTAL_CRITICALITY_MUL_FACTOR = 1000;

   public CriticalityAutocompleteItem()
   {}

   public CriticalityAutocompleteItem(CriticalityCategory cCat)
   {
      rangeFrom = cCat.getRangeFrom();
      rangeTo = cCat.getRangeTo();
      label = cCat.getLabel();
      iconColor = cCat.getIconColor();
      iconCount = cCat.getIconCount();
   }

   public CriticalityAutocompleteItem(String stringRep)
   {
      StringTokenizer tok = new StringTokenizer(stringRep, CriticalityConfigurationUtil.COLUMN_SEPARATOR);
      rangeFrom = Integer.parseInt(tok.nextToken());
      rangeTo = Integer.parseInt(tok.nextToken());
      label = tok.nextToken();
      iconColor = ICON_COLOR.valueOf(tok.nextToken());
      iconCount = tok.hasMoreTokens() ? Integer.parseInt(tok.nextToken()) : 1;
   }

   public int getRangeFrom()
   {
      return rangeFrom;
   }

   public void setRangeFrom(int rangeFrom)
   {
      this.rangeFrom = rangeFrom;
   }

   public int getRangeTo()
   {
      return rangeTo;
   }

   public void setRangeTo(int rangeTo)
   {
      this.rangeTo = rangeTo;
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
      return null != CriticalityConfigurationUtil.getIcon(iconColor)
            ? CriticalityConfigurationUtil.getIcon(iconColor)
            : CriticalityConfigurationUtil.getIcon(ICON_COLOR.WHITE);
   }

   public ICON_COLOR getIconColor()
   {
      return iconColor;
   }

   public void setIconColor(ICON_COLOR iconColor)
   {
      this.iconColor = iconColor;
   }

   public int getIconCount()
   {
      return iconCount;
   }

   public void setIconCount(int iconCount)
   {
      this.iconCount = iconCount;
   }

   public void setIconCount(ActionEvent event)
   {
      this.iconCount = Integer.parseInt((String) event.getComponent().getAttributes().get("displayCount"));
   }

   public double getRangeFromDouble()
   {
      return (getRangeFrom() / PORTAL_CRITICALITY_MUL_FACTOR);
   }

   public double getRangeToDouble()
   {
      return (getRangeTo() / PORTAL_CRITICALITY_MUL_FACTOR);
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#toString()
    * 
    * TODO - Handle empty strings (if needed)
    */
   public String toString()
   {
      return new StringBuffer().append(rangeFrom).append(CriticalityConfigurationUtil.COLUMN_SEPARATOR).append(rangeTo)
            .append(CriticalityConfigurationUtil.COLUMN_SEPARATOR).append(label)
            .append(CriticalityConfigurationUtil.COLUMN_SEPARATOR).append(iconColor)
            .append(CriticalityConfigurationUtil.COLUMN_SEPARATOR).append(iconCount).toString();
   }

   public CriticalityAutocompleteSelector getCriticalityAutocompleteSelector()
   {
      return criticalityAutocompleteSelector;
   }

   public void setCriticalityAutocompleteSelector(CriticalityAutocompleteSelector criticalityAutocompleteSelector)
   {
      this.criticalityAutocompleteSelector = criticalityAutocompleteSelector;
   }

   public void removeFromList()
   {
      criticalityAutocompleteSelector.removeCriticality(this);
   }

   @Override
   public boolean equals(Object equateTo)
   {
      if (null != equateTo && equateTo instanceof CriticalityAutocompleteItem)
      {
         CriticalityAutocompleteItem eTo = (CriticalityAutocompleteItem) equateTo;
         if (eTo.getLabel().equals(getLabel()))
         {
            return true;
         }
      }

      return false;
   }
}
