/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.viewscommon.spi.descriptor;

import org.eclipse.stardust.engine.api.model.DataPath;

/**
 * 
 * @author Yogesh.Manware
 * @version $Revision: $
 */
public class SemanticalDescriptorComparator implements ISemanticalDescriptorComparator
{
   /**
    * 
    * @param dataPath1
    * @param dataPath2
    * @return
    */
   public int compare(DataPath dataPath1, DataPath dataPath2)
   {
      if (null == dataPath1 || null == dataPath2)
      {
         return -1;
      }

      if (dataPath1.getId().equals(dataPath2.getId()) && dataPath1.getMappedType().equals(dataPath2.getMappedType()))
      {
         return 0;
      }

      return -1;
   }
}
