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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.query.CustomOrderCriterion;
import org.eclipse.stardust.engine.api.query.DataOrder;
import org.eclipse.stardust.engine.api.query.FilterableAttribute;
import org.eclipse.stardust.engine.api.query.OrderCriterion;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.ui.web.common.table.SortCriterion;



public abstract class AbstractSortHandler implements ISortHandler
{
   private Map sortableProperties;
   
   public AbstractSortHandler()
   {
      this(new FilterableAttribute[0]);
   }
   
   public AbstractSortHandler(FilterableAttribute[] attributes)
   {
      sortableProperties = new HashMap();
      if(attributes != null && attributes.length > 0)
      {
         for (int i = 0; i < attributes.length; i++)
         {
            addSortableAttribute(attributes[i]);
         }
      }
   }
   
   public void applySorting(Query query, List sortCriteria)
   {
      if(query != null && sortCriteria != null)
      {
         Iterator scIter = sortCriteria.iterator();
         while (scIter.hasNext())
         {
            SortCriterion sortCriterion = (SortCriterion) scIter.next();
            Iterator propIter = StringUtils.split(sortCriterion.getProperty(), ",");
            while(propIter.hasNext())
            {
               String prop = (String)propIter.next();
               Object attribute = sortableProperties.get(prop);
               if(attribute instanceof FilterableAttribute)
               {
                  handleFilterableAttribute(query, (FilterableAttribute)attribute,
                        sortCriterion.isAscending());
               }
               else if (attribute instanceof OrderCriterion)
               {
                  handleOrderCriterion(query, (OrderCriterion)attribute,
                        sortCriterion.isAscending());
               }
            }
         }
      }
   }

   public boolean isSortableColumn(String propertyName)
   {
      if(propertyName != null && 
            -1 == propertyName.indexOf(","))
      {
         return sortableProperties.containsKey(propertyName);
      }
      Iterator propIter = StringUtils.split(propertyName, ",");
      boolean sortable = false;
      while(propIter.hasNext())
      {
         String prop = (String)propIter.next();
         sortable = sortableProperties.containsKey(prop.trim());
         if(!sortable)
         {  // all attributes must be sortable...
            break;
         }
      }
      return sortable;
   }
   
   protected void addSortableAttribute(FilterableAttribute attribute)
   {
      addSortableAttribute(
            attribute != null ? attribute.getAttributeName() : null,
            attribute);
   }
   
   protected void addSortableAttribute(String propertyName, 
         FilterableAttribute attribute)
   {
      if(!StringUtils.isEmpty(propertyName) && attribute != null)
      {
         sortableProperties.put(propertyName, attribute);
      }
   }
   
   protected void addSortableAttribute(String propertyName, 
         OrderCriterion attribute)
   {
      if(!StringUtils.isEmpty(propertyName) && attribute != null)
      {
         sortableProperties.put(propertyName, attribute);
      }
   }
   
   protected void handleFilterableAttribute(Query query, FilterableAttribute attr, boolean ascending)
   {
      query.orderBy(attr, ascending);
   }
   
   protected void handleOrderCriterion(Query query, OrderCriterion attr, boolean ascending)
   {
      if(attr instanceof CustomOrderCriterion && !ascending)
      {
         attr = ((CustomOrderCriterion)attr).ascendig(false);
      }
      else if (attr instanceof DataOrder && !ascending)
      {
         attr = new DataOrder(((DataOrder) attr).getDataID(), ((DataOrder) attr)
               .getAttributeName(), false);
      }
      query.orderBy(attr);
   }
}
