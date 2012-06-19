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
package org.eclipse.stardust.ui.web.viewscommon.utils;

import java.io.IOException;

import org.eclipse.stardust.common.Serialization;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;



public final class QueryUtils
{
   private static Logger trace = LogManager.getLogger(QueryUtils.class);
   private static final String LIKE_WILDCARD = "%";
   
   private QueryUtils()
   {
      // Utility class 
   }

   /**
    * @param query
    * @return 
    * @return
    */
   public static Query getClonedQuery(Query query)
   {
      byte[] serializedQuery;
      Query clone = null;
      
      try
      {
         serializedQuery = Serialization.serializeObject(query);
         clone = (Query) Serialization.deserializeObject(serializedQuery);
      }
      catch (IOException e)
      {
         trace.error(e.getMessage(), e);
      }
      catch (ClassNotFoundException e)
      {
         trace.error(e.getMessage(), e);
      }
      
      return clone;
   }
   
   /**
    * return like command compatible string
    * 
    * @param inputString
    * @return
    */
   public static String getFormattedString(String inputString)
   {
      if (StringUtils.isNotEmpty(inputString))
      {
         // prefix
         if (!inputString.startsWith(LIKE_WILDCARD))
         {
            inputString = LIKE_WILDCARD + inputString;
         }
         // postfix
         if (!inputString.endsWith(LIKE_WILDCARD))
         {
            inputString += LIKE_WILDCARD;
         }
      }
      else
      {
         inputString = LIKE_WILDCARD;
      }
      return inputString;
   }
}
