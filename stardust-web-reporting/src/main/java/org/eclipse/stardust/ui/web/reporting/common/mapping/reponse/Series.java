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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Series
{
   private List<ValuesArray> seriesValues;

   public Series()
   {
      seriesValues = new ArrayList<ValuesArray>();
   }

   public void add(ValuesArray values)
   {
      seriesValues.add(values);
   }

   public void sort()
   {
      Collections.sort(seriesValues);
   }

   public List<ValuesArray> getValues()
   {
      return seriesValues;
   }
}
