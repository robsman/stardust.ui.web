/*******************************************************************************
 * Copyright (c) 2013 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.stardust.ui.web.reporting.beans.spring.portal;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

/**
 * @author Yogesh.Manware
 * 
 */
@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class SearchHandlerChain
{
   @Autowired
   private List<ISearchHandler> searchHandlers;

   /**
    * @param serviceName
    * @param searchValue
    * @return
    */
   public String handleRequest(String serviceName, String searchValue)
   {
      String result = null;
      for (ISearchHandler searchHandler : searchHandlers)
      {
         result = searchHandler.handle(serviceName, searchValue);
         if (result != null)
         {
            return result;
         }
      }
      return "";
   }

}
