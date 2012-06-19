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
package org.eclipse.stardust.ui.client.model;

import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.struct.StructuredDataConstants;


public class DescriptorFilterInfo
{
   private String dataId;
   private String typeId;
   private String accessPath;
   
   public DescriptorFilterInfo(String dataId, String dataType, String accessPath)
   {
      this.dataId = dataId;
      this.typeId = dataType;
      this.accessPath = accessPath;
   }

   public boolean isFilterable()
   {
      if (StructuredDataConstants.STRUCTURED_DATA.equals(typeId))
      {
         return !StringUtils.isEmpty(accessPath);
      }
      if (PredefinedConstants.PRIMITIVE_DATA.equals(typeId))
      {
         return StringUtils.isEmpty(accessPath);
      }
      // unsupported type
      return false;
   }

   public boolean matches(String dataId, String typeId, String accessPath)
   {
      return CompareHelper.areEqual(this.dataId, dataId)
          && CompareHelper.areEqual(this.typeId, typeId)
          && CompareHelper.areEqual(this.accessPath, accessPath);
   }

   public boolean matches(DescriptorFilterInfo info)
   {
      return matches(info.dataId, info.typeId, info.accessPath);
   }
}
