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

import org.eclipse.stardust.ui.web.common.column.ColumnPreference;

public class ProcessDocumentColumnPreference extends ColumnPreference
{

   public ProcessDocumentColumnPreference(String columnName, String columnProperty, String columnTitle,
         String columnContentUrl, boolean visible, boolean sortable)
   {
      super(columnName, columnProperty, columnTitle, columnContentUrl, visible, sortable);
   }

   /**
    * 
    */
   private static final long serialVersionUID = 1656496824328282533L;

}
