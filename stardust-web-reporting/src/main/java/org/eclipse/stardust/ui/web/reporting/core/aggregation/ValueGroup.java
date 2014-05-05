/*******************************************************************************
* Copyright (c) 2014 SunGard CSA LLC and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Holger.Prause (SunGard CSA LLC) - initial API and implementation and/or initial documentation
*******************************************************************************/
package org.eclipse.stardust.ui.web.reporting.core.aggregation;

import java.util.ArrayList;
import java.util.List;

public class ValueGroup<T>
{
   public List<T> values;

   public ValueGroup()
   {
      values = new ArrayList<T>();
   }

   public List<T> getValues()
   {
      return values;
   }

   public void add(T value)
   {
      values.add(value);
   }

   public int getSize()
   {
      return values.size();
   }
}
