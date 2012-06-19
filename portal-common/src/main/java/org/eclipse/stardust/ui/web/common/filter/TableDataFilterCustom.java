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

/**
 * @author Subodh.Godbole
 *
 */
public abstract class TableDataFilterCustom extends TableDataFilter implements ITableDataFilterCustom
{
   private static final long serialVersionUID = 1L;

   protected String contentUrl;
   
   /**
    * @param name
    * @param property
    * @param title
    * @param visible
    * @param contentUrl
    */
   public TableDataFilterCustom(String name, String property, String title,
         boolean visible, String contentUrl)
   {
      super(name, property, title, DataType.NONE, FilterCriteria.NONE, visible);
      this.contentUrl = contentUrl;
   }

   /**
    * @param contentUrl
    */
   public TableDataFilterCustom(String contentUrl)
   {
      this("", "", "", true, contentUrl);
   }
   
   public String getContentUrl()
   {
      return contentUrl;
   }
}
