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
import org.eclipse.stardust.ui.web.common.filter.ITableDataFilter;
import org.eclipse.stardust.ui.web.common.table.IFilterHandler;
import org.eclipse.stardust.ui.web.common.table.IQuery;



/**
 * @author Subodh.Godbole
 *
 */
public abstract class IppFilterHandler implements IFilterHandler
{
   private static final long serialVersionUID = 1L;

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.table.IFilterHandler#applyFiltering(org.eclipse.stardust.ui.web.common.table.paginator.spi.IQuery, java.util.List)
    */
   public void applyFiltering(IQuery iQuery, List<ITableDataFilter> filters)
   {
      applyFiltering(((IppQuery)iQuery).getQuery(), filters);
   }
   
   /**
    * @param query
    * @param filters
    */
   public abstract void applyFiltering(Query query, List<ITableDataFilter> filters);
}
