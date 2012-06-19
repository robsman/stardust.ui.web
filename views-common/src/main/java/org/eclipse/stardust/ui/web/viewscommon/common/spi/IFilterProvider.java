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
package org.eclipse.stardust.ui.web.viewscommon.common.spi;

import java.util.Set;

import org.eclipse.stardust.engine.api.query.Query;


public interface IFilterProvider
{
   String getFilterId();
   
   void setFilterId(String filterId);
   
   Set<String> getProcessDomain(IFilterModel abstractFilterModel);
   
   String[] getParticipantDomain(IFilterModel abstractFilterModel);
   
   void applyFilter(Query query);
   
   IFilterModel getFilterModel();
   
   void setFilterModel(IFilterModel filterModel);
}
