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

import org.eclipse.stardust.engine.api.model.Data;
import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.ui.web.common.util.StringUtils;
import org.eclipse.stardust.ui.web.viewscommon.descriptors.DescriptorFilterUtils;

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

      int result = -1;

      if (null == dataPath1 || null == dataPath2)
      {
         return result;
      }

      Data data1 = DescriptorFilterUtils.getData(dataPath1);
      Data data2 = DescriptorFilterUtils.getData(dataPath2);

      if (data1 == null || data2 == null)
      {
         return result;
      }

      // Identical Data path ID
      // Identical fully qualified Data
      // Identical data path i.e. access path
      if (dataPath1.getId().equals(dataPath2.getId()))
      {
         if (data1.getQualifiedId().equals(data2.getQualifiedId()))
         {
            //primitives don't access paths!
            if (StringUtils.isEmpty(dataPath1.getAccessPath()) && StringUtils.isEmpty(dataPath2.getAccessPath()))
            {
               result = 0;
            }
            if (dataPath1.getAccessPath() != null && dataPath1.getAccessPath().equals(dataPath2.getAccessPath()))
            {
               result = 0;
            }
         }
      }
      return result;
   }
}
