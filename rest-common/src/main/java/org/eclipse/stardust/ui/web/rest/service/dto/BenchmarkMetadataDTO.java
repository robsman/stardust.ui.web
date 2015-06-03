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
package org.eclipse.stardust.ui.web.rest.service.dto;

import org.eclipse.stardust.ui.web.rest.service.dto.common.DTOAttribute;

/**
 * 
 * @author Sidharth.Singh
 * @version $Revision: $
 */
public class BenchmarkMetadataDTO extends AbstractDTO
{
   @DTOAttribute("owner")
   public String author;

   @DTOAttribute("dateLastModified.time")
   public long lastModifiedDate;

   public String getAuthor()
   {
      return author;
   }

   public void setAuthor(String author)
   {
      this.author = author;
   }

   public long getLastModifiedDate()
   {
      return lastModifiedDate;
   }

   public void setLastModifiedDate(long lastModifiedDate)
   {
      this.lastModifiedDate = lastModifiedDate;
   }

}
