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

import java.io.Serializable;

/**
 * @author rsauer
 * @version $Revision$
 */
public interface IFilterModel extends Serializable
{
   IFilterModel ALWAYS_ON = new IFilterModel()
   {
      public boolean isFilterEnabled()
      {
         return true;
      }

      public void setFilterEnabled(boolean isEnabled)
      {
         // IGNORE
      }
   };

   boolean isFilterEnabled();

   void setFilterEnabled(boolean isEnabled);
}
