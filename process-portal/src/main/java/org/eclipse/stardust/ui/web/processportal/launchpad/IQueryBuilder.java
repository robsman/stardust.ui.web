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
package org.eclipse.stardust.ui.web.processportal.launchpad;

import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.engine.api.query.QueryResult;

/**
 * With this interface the implementing class is able to recreate a Query. There also is
 * the option to execute a countQuery, which should only count the number of QueryResult
 * items and a getter for the last countQuery.
 * 
 * Launchpanels that show counts of worklist-items or activityInstance-items should use
 * this interface to make their Query recreatable.
 * 
 * @author roland.stamm
 * 
 */
public interface IQueryBuilder
{
   /**
    * Builds a new Query
    * 
    * @return new created Query
    */
   Query createQuery();

   /**
    * Executes a Query that only counts entries
    * 
    * @return result without items
    */
   QueryResult executeCountQuery();

   /**
    * Gets the result from the last <code>executeCountQuery()</code>
    * 
    * @return last fetched QueryResult
    */
   QueryResult getCountQueryResult();
}
