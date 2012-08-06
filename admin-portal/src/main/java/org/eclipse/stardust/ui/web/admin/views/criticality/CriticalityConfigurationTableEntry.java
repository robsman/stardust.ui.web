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
package org.eclipse.stardust.ui.web.admin.views.criticality;

import java.util.StringTokenizer;

import javax.faces.event.ActionEvent;

import org.eclipse.stardust.ui.web.common.table.DefaultRowModel;
import org.eclipse.stardust.ui.web.viewscommon.common.criticality.CriticalityConfigurationUtil;
import org.eclipse.stardust.ui.web.viewscommon.common.criticality.CriticalityConfigurationUtil.ICON_COLOR;

/**
 * @author Shrikant.Gangal
 *
 */
public class CriticalityConfigurationTableEntry extends DefaultRowModel
{
   private static final long serialVersionUID = 1L;

   private static final String DEFAULT_ICON = CriticalityConfigurationUtil.getIcon(ICON_COLOR.WHITE);
   
   private int rangeFrom;
   private int rangeTo;
   private String label;
   private ICON_COLOR iconColor;
   private int iconCount;
   private boolean editable;
   private boolean selected;
   private CriticalityIconsSelectorPopup criticalityIconsSelectorPopup = new CriticalityIconsSelectorPopup("CriticalityIconsSelector");

   public CriticalityConfigurationTableEntry()
   {      
   }
   
   public CriticalityConfigurationTableEntry(int rangeFrom, int rangeTo, String label, ICON_COLOR iconColor, int iconCount)
   {
      this.rangeFrom = rangeFrom;
      this.rangeTo = rangeTo;
      this.label = label;
      this.iconColor = iconColor;
      this.iconCount = iconCount;
   }
   
   public CriticalityConfigurationTableEntry(String stringRep)
   {
      StringTokenizer tok = new StringTokenizer(stringRep, CriticalityConfigurationUtil.COLUMN_SEPARATOR);
      rangeFrom = Integer.parseInt(tok.nextToken());
      rangeTo = Integer.parseInt(tok.nextToken());
      label = tok.nextToken();
      iconColor = ICON_COLOR.valueOf(tok.nextToken());
      iconCount = tok.hasMoreTokens() ? Integer.parseInt(tok.nextToken()) : 1;
   }

   /**
    * 
    */
   private void validateCriticalityConfiguration()
   {
      CriticalityConfigurationBean.getInstance().validate();
   }

   public int getRangeFrom()
   {
      return rangeFrom;
   }
   
   public void setRangeFrom(int rangeFrom)
   {
      this.rangeFrom = rangeFrom;
      validateCriticalityConfiguration();
   }
   
   public int getRangeTo()
   {
      return rangeTo;
   }
   
   public void setRangeTo(int rangeTo)
   {
      this.rangeTo = rangeTo;
      validateCriticalityConfiguration();
   }
   
   public String getLabel()
   {
      return label;
   }
   
   public void setLabel(String label)
   {
      this.label = label;
      validateCriticalityConfiguration();
   }
   
   public String getIcon()
   {
      return null != CriticalityConfigurationUtil.getIcon(iconColor) ? CriticalityConfigurationUtil.getIcon(iconColor) : CriticalityConfigurationUtil.getIcon(ICON_COLOR.WHITE);
   }
   
   public String getDefaultIcon()
   {
      return DEFAULT_ICON;
   }
   
   public ICON_COLOR getIconColor()
   {
      return iconColor;
   }
   
   public void setIconColor(ICON_COLOR iconColor)
   {
      this.iconColor = iconColor;
      validateCriticalityConfiguration();
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
   
   public boolean isEditable()
   {
      return editable;
   }
   
   public void setEditable(boolean editable)
   {
      this.editable = editable;
   }
   
   public boolean isSelected()
   {
      return selected;
   }
   
   public void setSelected(boolean selected)
   {
      this.selected = selected;
   }   
   
   public CriticalityIconsSelectorPopup getCriticalityIconsSelectorPopup()
   {
      return criticalityIconsSelectorPopup;
   }
   
   public void openIconSelector(ActionEvent event)
   {
      CriticalityConfigurationBean.getInstance().closeAllIconSelectors();
      criticalityIconsSelectorPopup.showIconSelector(event);
   }
   
   
   public void setSelectedIcon(ActionEvent event)
   {
      String selectedColor = (String) event.getComponent().getAttributes().get("selectedColor");
      setIconColor(ICON_COLOR.valueOf(selectedColor));
      criticalityIconsSelectorPopup.setVisible(false);
   }
   
   
   public void closeIconSelectionPopup(ActionEvent event)
   {
      criticalityIconsSelectorPopup.setVisible(false);
   }
   
   /* (non-Javadoc)
    * @see java.lang.Object#toString()
    * 
    * TODO - Handle empty strings (if needed)
    */
   public String toString()
   {
      return new StringBuffer()
         .append(rangeFrom).append(CriticalityConfigurationUtil.COLUMN_SEPARATOR)
         .append(rangeTo).append(CriticalityConfigurationUtil.COLUMN_SEPARATOR)
         .append(label).append(CriticalityConfigurationUtil.COLUMN_SEPARATOR)
         .append(iconColor).append(CriticalityConfigurationUtil.COLUMN_SEPARATOR)
         .append(iconCount)
         .toString();
   }
}
