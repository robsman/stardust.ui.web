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
package org.eclipse.stardust.ui.web.viewscommon.common.table;

import java.util.List;

import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.ui.web.common.table.IQuery;
import org.eclipse.stardust.ui.web.common.table.ISortHandler;
import org.eclipse.stardust.ui.web.common.table.SortCriterion;



/**
 * @author Subodh.Godbole
 *
 */
public abstract class IppSortHandler implements ISortHandler
{
   private static final long serialVersionUID = 1L;

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.table.paginator.spi.ISortHandler#applySorting(org.eclipse.stardust.ui.web.common.table.paginator.spi.IQuery, java.util.List)
    */
   public void applySorting(IQuery iQuery, List<SortCriterion> sortCriterias)
   {
      applySorting(((IppQuery)iQuery).getQuery(), sortCriterias);
   }

   /**
    * @param query
    * @param sortCriterias
    */
   public abstract void applySorting(Query query, List<SortCriterion> sortCriterias);
}
