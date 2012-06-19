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
package org.eclipse.stardust.ui.web.viewscommon.processContextExplorer;

import org.eclipse.stardust.ui.web.common.table.DefaultRowModel;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDescriptor;


/**
 * @author Yogesh.Manware
 * 
 */
public class DescriptorItemTableEntry extends DefaultRowModel
{
   private static final long serialVersionUID = 1L;
   ProcessDescriptor processDescriptor;

   /**
    * @param name
    * @param value
    */
   public DescriptorItemTableEntry(ProcessDescriptor processDescriptor)
   {
      super();
      this.processDescriptor = processDescriptor;
   }

   public String getName()
   {
      return processDescriptor.getKey();
   }

   public String getValue()
   {
      return processDescriptor.getValue();
   }
}
