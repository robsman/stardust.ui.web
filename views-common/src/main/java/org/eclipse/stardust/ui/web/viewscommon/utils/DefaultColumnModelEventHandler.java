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
package org.eclipse.stardust.ui.web.viewscommon.utils;

import org.eclipse.stardust.ui.web.common.column.IColumnModel;
import org.eclipse.stardust.ui.web.common.column.IColumnModelListener;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;

/**
 * A Default implementation for <code>IColumnModelListener</code>
 * <P>
 * It takes needRefresh boolean value and if it is true then on listener call, it refreshes page.
 * @see FacesUtils#refreshPage()
 * </P>
 *
 * @author Vikas.Mishra
 * @version $Revision: $
 */
public class DefaultColumnModelEventHandler implements IColumnModelListener
{
   private boolean needRefresh;

   /**
    * Method columnsRearranged.
    * @param columnModel IColumnModel
    * @see org.eclipse.stardust.ui.web.common.column.IColumnModelListener#columnsRearranged(IColumnModel)
    */
   public void columnsRearranged(IColumnModel columnModel)
   {
      if (needRefresh)
      {
         FacesUtils.refreshPage();
      }
   }

   /**
    * Method setNeedRefresh.
    * @param needRefresh boolean
    */
   public void setNeedRefresh(boolean needRefresh)
   {
      this.needRefresh = needRefresh;
   }

   /**
    * Method isNeedRefresh.
    * @return boolean
    */
   public boolean isNeedRefresh()
   {
      return needRefresh;
   }
   

}
