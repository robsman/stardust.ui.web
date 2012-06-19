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
package org.eclipse.stardust.ui.web.bcc;

import java.util.MissingResourceException;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.ui.web.bcc.messsages.MessagesBCCBean;



/**
 * Special handling of labels of archived reports and user report designs. Fallback to the standard BCC
 * message bean if no reporting URL detected.
 */
public class CategoryTreeMessageBean extends MessagesBCCBean {

	public CategoryTreeMessageBean() {
	}
	
   public boolean hasKey(String key)
   {
      try
      {
         String value = get(key);
         if (value != null && value.startsWith("%"))
         {
            return false;
         }
         return true;
      }
      catch (MissingResourceException mre)
      {
         return false;
      }
   }


	public String get(Object key)
	{
		String stringKey = (String)key;
		
		if ( !StringUtils.isEmpty(stringKey) && stringKey.indexOf("reportUri") != -1)
		{
			// this is a dynamic view, need to derive the label and description from the key
			int i = stringKey.indexOf(".archived.");
			if (i == -1)
			{
				i = stringKey.indexOf(".designs.");
				if (i == -1)
				{
					// assume standard reports
					String [] s = stringKey.split("\\.");
					String fileName = "";
					if (s.length > 2)
					{
						fileName = s[s.length-3];
					}
					return super.get("views.categoryTree.standardReportDesignsPrefix")+" "+fileName;
				}
				else
				{
					String fileName = stringKey.substring(i+".designs.".length(), stringKey.lastIndexOf('.'));
					return super.get("views.categoryTree.userDefinedReportDesignsPrefix")+" "+fileName;
				}
			}
			else
			{
				String fileName = stringKey.substring(i+".archived.".length(), stringKey.lastIndexOf('.'));
				return super.get("views.categoryTree.userReportsPrefix")+" "+fileName;
			}
		}
		else
		{
			// fallback to default behaviour
			return super.get(key);	
		}
	}

}
