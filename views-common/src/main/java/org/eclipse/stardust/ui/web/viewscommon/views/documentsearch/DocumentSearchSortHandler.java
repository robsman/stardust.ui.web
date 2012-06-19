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
package org.eclipse.stardust.ui.web.viewscommon.views.documentsearch;

import java.util.Iterator;
import java.util.List;

import org.eclipse.stardust.engine.api.query.DocumentQuery;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.ui.web.common.table.SortCriterion;
import org.eclipse.stardust.ui.web.viewscommon.common.table.IppSortHandler;



/**
 * 
 * @author Vikas.Mishra
 */
public class DocumentSearchSortHandler extends IppSortHandler
{
   private static final long serialVersionUID = 1L;

   @Override
   public void applySorting(Query query, List<SortCriterion> sortCriterias)
   {
      Iterator< ? > iterator = sortCriterias.iterator();

      // As per current Architecture, this list will hold only one item
      if (iterator.hasNext())
      {
         SortCriterion sortCriterion = (SortCriterion) iterator.next();

         if (DocumentSearchBean.DOCUMENT_NAME.equals(sortCriterion.getProperty()))
         {
            query.orderBy(DocumentQuery.NAME, sortCriterion.isAscending());
         }
         else if (DocumentSearchBean.AUTHOR.equals(sortCriterion.getProperty()))
         {
            query.orderBy(DocumentQuery.OWNER, sortCriterion.isAscending());
         }
         else if (DocumentSearchBean.FILE_TYPE.equals(sortCriterion.getProperty()))
         {
            query.orderBy(DocumentQuery.CONTENT_TYPE, sortCriterion.isAscending());
         }
         else if (DocumentSearchBean.DATE_CREATED.equals(sortCriterion.getProperty()))
         {
            query.orderBy(DocumentQuery.DATE_CREATED, sortCriterion.isAscending());
         }
         else if (DocumentSearchBean.DATE_LAST_MODIFIED.equals(sortCriterion.getProperty()))
         {
            query.orderBy(DocumentQuery.DATE_LAST_MODIFIED, sortCriterion.isAscending());
         }
         else if (DocumentSearchBean.DOCUMENT_ID.equals(sortCriterion.getProperty()))
         {
            query.orderBy(DocumentQuery.ID, sortCriterion.isAscending());
         }
         else if (DocumentSearchBean.DOCUMENT_TYPE.equals(sortCriterion.getProperty()))
         {
            query.orderBy(DocumentQuery.DOCUMENT_TYPE_ID, sortCriterion.isAscending());
         }
      }
   }

}
