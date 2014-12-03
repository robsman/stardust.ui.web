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
import org.eclipse.stardust.engine.api.model.ModelElement;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelUtils;

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
   public int compare2(DataPath dataPath1, DataPath dataPath2)
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

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.viewscommon.spi.descriptor.ISemanticalDescriptorComparator#compare(org.eclipse.stardust.engine.api.model.ModelElement, org.eclipse.stardust.engine.api.model.ModelElement)
    */
   public int compare(ModelElement modelElement1, ModelElement modelElement2)
   {
      if (null == modelElement1 || null == modelElement2)
      {
         return -1;
      }
      int result = Integer.valueOf(modelElement1.getElementOID()).compareTo(modelElement2.getElementOID());
      if (result == 0)
      {
         String modelId1 = ModelUtils.extractModelId(modelElement1.getQualifiedId());
         String modelId2 = ModelUtils.extractModelId(modelElement2.getQualifiedId());
         result = modelId1.compareTo(modelId2);
      }
      return result;
   }

}
