/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.common.column;

import java.util.Comparator;

/**
 * @author Aditya.Gaikwad
 * @version $Revision: $
 */
public class ColumnPreferenceComparator implements Comparator<ColumnPreference>
{
   @Override
   public int compare(ColumnPreference o1, ColumnPreference o2)
   {
      return o1.getColumnTitle().compareToIgnoreCase(o2.getColumnTitle());
   }
}
