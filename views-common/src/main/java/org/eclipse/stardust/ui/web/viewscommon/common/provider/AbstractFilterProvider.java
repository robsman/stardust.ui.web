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
package org.eclipse.stardust.ui.web.viewscommon.common.provider;

import java.io.Serializable;
import java.util.Set;

import org.eclipse.stardust.ui.web.viewscommon.common.spi.IFilterModel;
import org.eclipse.stardust.ui.web.viewscommon.common.spi.IFilterProvider;

/**
 * @author rsauer
 * @version $Revision$
 */
public abstract class AbstractFilterProvider implements IFilterProvider, Serializable
{
   private String filterId;
   private IFilterModel filterModel;

   public String getFilterId()
   {
      return filterId;
   }

   public void setFilterId(String id)
   {
      this.filterId = id;
   }
   
   public String[] getParticipantDomain(IFilterModel abstractFilterModel)
   {
      return null;
   }
   
   public Set<String> getProcessDomain(IFilterModel abstractFilterModel)
   {
      return null;
   }

   public void setFilterModel(IFilterModel filterModel)
   {
      this.filterModel = filterModel;
   }
   
   public IFilterModel getFilterModel()
   {
      return filterModel;
   }
}
