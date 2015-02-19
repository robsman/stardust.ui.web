/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Anoop.Nair (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.rest;

import java.util.List;

import org.eclipse.stardust.ui.web.rest.service.dto.WorklistFilterDTO;

/**
 * @author Subodh.Godbole
 * @version $Revision: $
 */
public class Options
{
   public int pageSize;
   public int skip ;
   public String orderBy;
   public boolean asc = true;
   public WorklistFilterDTO filter;
   public List<String> visibleDescriptorColumns;
   public boolean  allDescriptorsVisible;
  
   
   public Options(){
      
   }
   /**
    * @param pageSize
    * @param skip
    */
   public Options(int pageSize, int skip, String orderBy, boolean asc )
   {
      super();
      this.pageSize = pageSize;
      this.skip = skip;
      this.orderBy = orderBy;
      this.asc = asc;
   }
}
