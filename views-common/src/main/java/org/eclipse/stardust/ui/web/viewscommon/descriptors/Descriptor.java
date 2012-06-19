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
package org.eclipse.stardust.ui.web.viewscommon.descriptors;

import java.io.Serializable;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.ui.web.viewscommon.common.spi.IDescriptorFilterModel;
import org.eclipse.stardust.ui.web.viewscommon.common.spi.IGenericInputField;
import org.eclipse.stardust.ui.web.viewscommon.utils.CommonDescriptorUtils;



public class Descriptor implements Serializable
{  
   private static final long serialVersionUID = 1L;
   private final boolean sortable;
   private String sortProperty;
   private final boolean filterable;
   private final DataPath dataPath;
   
   public Descriptor(DataPath dataPath, IDescriptorFilterModel filterModel)
   {
      this.dataPath = dataPath;
      DescriptorFilterUtils.DescriptorFlags flags = DescriptorFilterUtils.getSortableAndFilterableFlags(dataPath);
      filterable = flags.isFilterable();
      sortable = flags.isSortable();
      if(sortable)
      {
         sortProperty = "carnot-descriptor:" + dataPath.getData();
         if(CommonDescriptorUtils.isStructuredData(dataPath) 
               && !StringUtils.isEmpty(dataPath.getAccessPath()))
         {
            sortProperty = sortProperty + ";xPath:" + dataPath.getAccessPath();
         }
      }      
    
      // don't initialize the descriptorField in the constructor
      // because you cannot be sure if the filterModel is in a final state
   }
   
   
   
   public DataPath getDataPath()
   {
      return dataPath;
   }
   
   public boolean isSortable()
   {
      return sortable;
   }
   
   public boolean isFilterable()
   {
      return filterable;
   }
   
   public String getSortProperty()
   {
      return sortProperty;
   }
   
  
}
