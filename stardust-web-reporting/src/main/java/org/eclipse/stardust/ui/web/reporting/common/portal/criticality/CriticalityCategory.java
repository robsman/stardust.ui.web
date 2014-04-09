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
package org.eclipse.stardust.ui.web.reporting.common.portal.criticality;

import java.util.StringTokenizer;

import javax.faces.event.ActionEvent;

import org.eclipse.stardust.ui.web.reporting.common.portal.criticality.CriticalityConfigurationUtil.ICON_COLOR;


/**
 * @author Shrikant.Gangal
 * @author Yogesh.Manware
 *
 *  Note : The class is directly converted to json object. Modifications to attributes should be avoided.
 */
public class CriticalityCategory implements Comparable<CriticalityCategory>
{
   private String id;
   private int rangeFrom;
   private int rangeTo;
   private String name;
   private ICON_COLOR iconColor;
   private int iconCount;

   public CriticalityCategory()
   {      
   }

   public CriticalityCategory(String stringRep)
   {
      StringTokenizer tok = new StringTokenizer(stringRep, CriticalityConfigurationUtil.COLUMN_SEPARATOR);
      rangeFrom = Integer.parseInt(tok.nextToken());
      rangeTo = Integer.parseInt(tok.nextToken());
      id = name = tok.nextToken();
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
   
   public String getName()
   {
      return name;
   }
   
   public void setName(String label)
   {
      this.id = this.name = label;
   }
   
   public String getIcon()
   {
      return null != CriticalityConfigurationUtil.getIcon(iconColor) ? CriticalityConfigurationUtil.getIcon(iconColor) : CriticalityConfigurationUtil.getIcon(ICON_COLOR.WHITE);
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
   
   /**
    * @param iconCount
    */
   public void setIconCount(int iconCount)
   {
      this.iconCount = iconCount;
   }
   
   public void setIconCount(ActionEvent event)
   {
      this.iconCount = Integer.parseInt((String) event.getComponent().getAttributes().get("displayCount"));
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
         .append(name).append(CriticalityConfigurationUtil.COLUMN_SEPARATOR)
         .append(iconColor).append(CriticalityConfigurationUtil.COLUMN_SEPARATOR)
         .append(iconCount)
         .toString();
   }

   /**
    * Sorts in descending order by default.
    */
   public int compareTo(CriticalityCategory other)
   {
      return other.getRangeFrom() - getRangeFrom();
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + rangeFrom;
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      CriticalityCategory other = (CriticalityCategory) obj;
      if (rangeFrom != other.rangeFrom)
         return false;
      return true;
   }

   public String getId()
   {
      return id;
   }

   public void setId(String id)
   {
      this.id = id;
   }

   
}
