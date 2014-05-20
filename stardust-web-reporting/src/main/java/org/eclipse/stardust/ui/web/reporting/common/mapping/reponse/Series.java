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
package org.eclipse.stardust.ui.web.reporting.common.mapping.reponse;

import java.util.*;

public class Series
{
   private TreeSet<ValuesArray> seriesValues;

   public Series()
   {
      seriesValues = new TreeSet<ValuesArray>();
   }

   public void add(ValuesArray values)
   {
      seriesValues.add(values);
   }

   public Collection<ValuesArray> getValues()
   {
      return seriesValues;
   }

   public boolean hasValue(ValuesArray seriesValue)
   {
      return seriesValues.contains(seriesValue);
   }
}
