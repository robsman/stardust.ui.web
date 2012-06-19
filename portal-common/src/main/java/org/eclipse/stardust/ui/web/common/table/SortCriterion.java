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

/**
 * To be used in connection with ISortHandler
 * This class replaces the previous Trinidad-based implementation: org.apache.myfaces.trinidad.model.SortCriterion
 * 
 * @author Subodh.Godbole
 */
public class SortCriterion implements Serializable
{
   private static final long serialVersionUID = 1L;
   
   private String property;
   private boolean ascending;
   
   /**
    * @param property
    * @param ascending
    */
   public SortCriterion(String property, boolean ascending)
   {
      super();
      this.property = property;
      this.ascending = ascending;
   }

   @Override
   public String toString()
   {
      return property + ":" + ascending;
   }

   public String getProperty()
   {
      return property;
   }
   
   public void setProperty(String property)
   {
      this.property = property;
   }
   
   public boolean isAscending()
   {
      return ascending;
   }
   
   public void setAscending(boolean ascending)
   {
      this.ascending = ascending;
   }
}
