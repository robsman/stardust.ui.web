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
package org.eclipse.stardust.ui.web.rest.dto;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.ui.web.rest.dto.core.DTOAttribute;
import org.eclipse.stardust.ui.web.viewscommon.utils.FormatterUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.UserUtils;

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
   
   public long runtimeOid;
   
   public void setAuthor(String owner)
   {
      if (StringUtils.isNotEmpty(owner))
      {
         User user = UserUtils.getUser(owner);
         if (null != user)
         {
            author = FormatterUtils.getUserLabel(user);
         }
         else
         {
            author = owner;
         }
      }
   }
}
