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

import java.io.Serializable;
import java.util.List;

import org.eclipse.stardust.engine.api.query.Query;


/**
 * @author rsauer
 * @version $Revision$
 */
public interface ISortHandler extends Serializable
{
   boolean isSortableColumn(String propertyName);
   
   void applySorting(Query query, List sortCriteria);
}
