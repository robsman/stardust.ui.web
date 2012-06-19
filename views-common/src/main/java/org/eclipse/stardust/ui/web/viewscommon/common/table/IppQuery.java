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
import org.eclipse.stardust.ui.web.common.table.IQuery;
import org.eclipse.stardust.ui.web.viewscommon.utils.QueryUtils;



/**
 * @author Subodh.Godbole
 *
 */
public class IppQuery implements IQuery
{
   private static final long serialVersionUID = 1L;

   private Query query;
   
   /**
    * @param ippQuery
    */
   public IppQuery(Query query)
   {
      this.query = query;
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.table.paginator.spi.IQuery#getClone()
    */
   public IQuery getClone()
   {
      return new IppQuery(QueryUtils.getClonedQuery(query));
   }

   public Query getQuery()
   {
      return query;
   }

   public void setQuery(Query query)
   {
      this.query = query;
   }
}
