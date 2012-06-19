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

import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.engine.api.query.QueryResult;
import org.eclipse.stardust.engine.api.query.SubsetPolicy;
import org.eclipse.stardust.ui.web.common.table.IQuery;
import org.eclipse.stardust.ui.web.common.table.IQueryResult;
import org.eclipse.stardust.ui.web.common.table.ISearchHandler;



/**
 * @author Subodh.Godbole
 *
 */
public abstract class IppSearchHandler<E> implements ISearchHandler<E>
{
   private static final long serialVersionUID = 1L;

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.table.paginator.spi.ISearchHandler#buildQuery()
    */
   public IQuery buildQuery()
   {
      return new IppQuery(createQuery());
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.table.paginator.spi.ISearchHandler#performSearch(org.eclipse.stardust.ui.web.common.table.paginator.spi.IQuery, int, int)
    */
   public IQueryResult<E> performSearch(IQuery iQuery, int startRow, int pageSize)
   {
      Query query = ((IppQuery) iQuery).getQuery();
      SubsetPolicy newPolicy = new SubsetPolicy(pageSize, startRow, true);
      query.setPolicy(newPolicy);

      return new IppQueryResult<E>(performSearch(query));
   }

   public abstract Query createQuery();
   public abstract QueryResult<E> performSearch(Query query);
}
