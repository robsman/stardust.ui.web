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
package org.eclipse.stardust.ui.web.viewscommon.user;

import org.eclipse.stardust.ui.web.common.filter.ITableDataFilter;
import org.eclipse.stardust.ui.web.common.filter.TableDataFilterCustom;
import org.eclipse.stardust.ui.web.common.util.StringUtils;
import org.eclipse.stardust.ui.web.viewscommon.core.ResourcePaths;


/**
 * @author Shrikant.Gangal
 *
 */
public class TableDataFilterUserName extends TableDataFilterCustom
{
   private static final long serialVersionUID = 1L;
   private String firstName;
   private String lastName;
   public static final int MAX_SUMMARY_LENGTH = 35;
   
   public TableDataFilterUserName(String name, String property, String title, boolean visible)
   {
      super(name, property, title, visible, ResourcePaths.V_USER_NAME_TABLE_FILTER);
   }

   public TableDataFilterUserName()
   {
      this("", "", "", true);
   }
   
   public boolean isFilterSet()
   {
      if (StringUtils.isEmpty(firstName) && StringUtils.isEmpty(lastName))
      {
         return false;
      }
      
      return true;
   }

   public void resetFilter()
   {
      firstName = null;
      lastName = null;
   }

   public String getFilterSummaryTitle()
   {
      String ln = StringUtils.isEmpty(getLastName()) ? "" : getLastName();
      String del = StringUtils.isEmpty(getLastName()) || StringUtils.isEmpty(getFirstName()) ? "" : ", ";
      String fn = StringUtils.isEmpty(getFirstName()) ? "" : getFirstName();

      String str = ln + del + fn;
      if (str.length() > MAX_SUMMARY_LENGTH)
      {
         str = str.substring(0, MAX_SUMMARY_LENGTH);
         str += "...";
      }

      return str;
   }

   public boolean contains(Object compareValue)
   {
      return true;
   }

   public ITableDataFilter getClone()
   {
      TableDataFilterUserName cFilter = new TableDataFilterUserName(getName(), getProperty(), getTitle(), isVisible());
      cFilter.setFirstName(getFirstName());
      cFilter.setLastName(getLastName());
      
      return cFilter;
   }

   public void copyValues(ITableDataFilter dataFilterToCopy)
   {
      if (dataFilterToCopy instanceof TableDataFilterUserName)
      {
         setFirstName(((TableDataFilterUserName) dataFilterToCopy).getFirstName());
         setLastName(((TableDataFilterUserName) dataFilterToCopy).getLastName());
      }
   }

   public String getFirstName()
   {
      return firstName;
   }

   public void setFirstName(String firstName)
   {
      this.firstName = firstName;
   }

   public String getLastName()
   {
      return lastName;
   }

   public void setLastName(String lastName)
   {
      this.lastName = lastName;
   }
}
