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

import org.eclipse.stardust.ui.web.viewscommon.common.criticality.CriticalityCategory;

/**
 * @author Yogesh.Manware Note : The class is directly converted to json object.
 *         Modifications to attributes should be avoided.
 */
public class Criticality
{
   private String id;
   private int rangeFrom;
   private int rangeTo;
   private String name;

   public Criticality(CriticalityCategory category, int index)
   {
      id = "id" + String.valueOf(index);
      name = category.getLabel();
      rangeFrom = category.getRangeFrom();
      rangeTo = category.getRangeTo();
   }

   public String getId()
   {
      return id;
   }

   public void setId(String id)
   {
      this.id = id;
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

   public void setName(String name)
   {
      this.name = name;
   }
}