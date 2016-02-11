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

package org.eclipse.stardust.ui.web.rest.util;

import java.util.HashSet;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.engine.api.query.DescriptorPolicy;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.ui.web.rest.dto.DataTableOptionsDTO;

/**
 * 
 * @author Johnson.Quadras
 *
 */
public class DescriptorUtils
{
   /**
    * 
    * @param descriptorName
    * @param descriptorNameAndDataPathMap
    * @return
    */
   public static String getDescriptorColumnName(String descriptorName, Map<String, DataPath> descriptorNameAndDataPathMap)
   {
      if (descriptorNameAndDataPathMap.containsKey(descriptorName))
      {
         DataPath columnNameDataPath = (DataPath) descriptorNameAndDataPathMap.get(descriptorName);

         return columnNameDataPath.getData();
      }
      else
         return null;
   }
   
   /**
    * 
    * @param descriptorName
    * @param descriptorNameAndDataPathMap
    * @return
    */
   public static String getXpathName(String descriptorName, Map<String, DataPath> descriptorNameAndDataPathMap)
   {
      if (descriptorNameAndDataPathMap.containsKey(descriptorName))
      {
         DataPath columnNameDataPath = (DataPath) descriptorNameAndDataPathMap.get(descriptorName);

         return columnNameDataPath.getAccessPath();
      }
      else
         return null;
   }
   
   /**
    * @param query
    */
   public static void applyDescriptorPolicy(Query query, DataTableOptionsDTO options)
   {
      if (options.allDescriptorsVisible)
      {
         query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      }
      else if (CollectionUtils.isEmpty(options.visibleDescriptorColumns))
      {
         query.setPolicy(DescriptorPolicy.NO_DESCRIPTORS);
      }
      else
      {
         query.setPolicy(DescriptorPolicy.withIds(new HashSet<String>(options.visibleDescriptorColumns)));
      }
   }
}
