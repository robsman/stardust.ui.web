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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.ui.web.viewscommon.common.GenericDataMapping;
import org.eclipse.stardust.ui.web.viewscommon.common.model.AbstractDescriptorFilterModel;


/**
 * @author rsauer
 * @version $Revision: 31067 $
 */
public class GenericDescriptorFilterModel extends AbstractDescriptorFilterModel
{
   private String processId;
   
   private final Descriptor[] descriptors;
   private final static Descriptor[] EMPTY_DESCRIPTORS = new Descriptor[0];

   /**
    * @param descriptors
    * @return
    */
   public static GenericDescriptorFilterModel create(DataPath[] descriptors)
   {
      GenericDescriptorFilterModel result = null;

      if ((null != descriptors) && (0 < descriptors.length))
      {
         result = new GenericDescriptorFilterModel(descriptors);
      }

      return result;
   }

   /**
    * @param descriptors
    * @return
    */
   public static GenericDescriptorFilterModel create(Collection<DataPath> descriptors)
   {
      return create(descriptors.toArray(new DataPath[1]));
   }
   
   public GenericDescriptorFilterModel(DataPath[] descriptors)
   {
      if(descriptors != null)
      { 
         List mappings = new ArrayList(descriptors.length);
         List descriptorList = new ArrayList();
         for (int i = 0; i < descriptors.length; i++ )
         {
            Descriptor descriptor = new Descriptor(descriptors[i], this);
            if(descriptor.isFilterable())
            {
               mappings.add(new GenericDataMapping(descriptors[i]));
            }
            descriptorList.add(descriptor);
         }
         setFilterableData(mappings);
         this.descriptors = (Descriptor[]) 
            descriptorList.toArray(EMPTY_DESCRIPTORS);
      }
      else
      {
         this.descriptors = EMPTY_DESCRIPTORS;
      }
   }

   public String getProcessId()
   {
      return processId;
   }

   public void setProcessId(String processId)
   {
      this.processId = processId;
   }

   public Descriptor[] getDescriptors()
   {
      return descriptors;
   }

   

}
