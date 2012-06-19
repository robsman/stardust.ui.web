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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.StringTokenizer;

import javax.faces.model.SelectItem;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.ui.web.common.util.AbstractMessageBean;



/**
 * @author Yogesh.Manware
 * 
 */
public class IceComponentUtil
{
   
   public static final Comparator<SelectItem> SELECT_ITEM_ORDER = new Comparator<SelectItem>()
   {
      public int compare(SelectItem item1, SelectItem item2)
      {
         return item1.getLabel().compareTo(item2.getLabel());
      }
   };
   /**
    * @param prefix
    * @param keys
    * @param msgBean
    * @return
    */
   public static SelectItem[] buildSelectItemArray(String prefix, String[] keys, AbstractMessageBean msgBean)
   {
      SelectItem[] selectItems = new SelectItem[keys.length];
      int i = 0;
      for (String partialKey : keys)
      {
         String key = prefix + partialKey;
         selectItems[i++] = buildSelectItem(key, msgBean);
      }
      return selectItems;
   }

   /**
    * @param key
    * @param msgBean
    * @return
    */
   public static SelectItem buildSelectItem(String key, AbstractMessageBean msgBean)
   {
      return new SelectItem(key, msgBean.getString(key));
   }

   /**
    * parses the message to be displayed UI, recognizes \n character and adds new line
    * accordingly
    * 
    * @param message
    * @return
    */
   public static List<String> parseMessage(String message)
   {
      List<String> messageLines = new ArrayList<String>();
      if (StringUtils.isNotEmpty(message))
      {
         StringTokenizer st = new StringTokenizer(message, "\n");
         while (st.hasMoreTokens())
         {
            messageLines.add(st.nextToken());
         }
      }
      return messageLines;
   }
}
