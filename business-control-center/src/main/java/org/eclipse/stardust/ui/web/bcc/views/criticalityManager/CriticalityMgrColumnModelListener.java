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
package org.eclipse.stardust.ui.web.bcc.views.criticalityManager;

import org.eclipse.stardust.ui.web.common.column.IColumnModel;
import org.eclipse.stardust.ui.web.viewscommon.utils.DefaultColumnModelEventHandler;


/**
 * @author Shrikant.Gangal
 * 
 */
public class CriticalityMgrColumnModelListener extends DefaultColumnModelEventHandler
{
   ActivityCriticalityManagerBean acBean;

   public CriticalityMgrColumnModelListener(ActivityCriticalityManagerBean bean)
   {
      acBean = bean;
   }

   @Override
   public void columnsRearranged(IColumnModel columnModel)
   {
      acBean.initializeMissingCriticaliyStatistics();
      super.columnsRearranged(columnModel);
   }
}
