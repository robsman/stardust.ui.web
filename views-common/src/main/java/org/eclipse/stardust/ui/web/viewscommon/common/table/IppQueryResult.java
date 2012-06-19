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

import org.eclipse.stardust.engine.api.query.QueryResult;
import org.eclipse.stardust.ui.web.common.table.IQueryResult;



/**
 * @author Subodh.Godbole
 *
 * @param <E>
 */
public class IppQueryResult<E> implements IQueryResult<E>
{
   private static final long serialVersionUID = 1L;

   private QueryResult<E> queryResult;
   
   /**
    * @param ippQueryResult
    */
   public IppQueryResult(QueryResult<E> ippQueryResult)
   {
      this.queryResult = ippQueryResult;
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.table.paginator.spi.IQueryResult#getTotalCount()
    */
   public long getTotalCount() throws UnsupportedOperationException
   {
      return queryResult.getTotalCount();
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.table.paginator.spi.IQueryResult#getData()
    */
   public List<E> getData()
   {
      return queryResult;
   }

   public QueryResult<E> getQueryResult()
   {
      return queryResult;
   }

   public void setQueryResult(QueryResult<E> queryResult)
   {
      this.queryResult = queryResult;
   }
}
