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
package org.eclipse.stardust.ui.web.viewscommon.core;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;


public class HtmlCleanerUtility implements HtmlFormatter
{
   public static final Logger logger = LogManager.getLogger(HtmlCleanerUtility.class);

   public String cleanAndFormatHtml(String content)
   {

      String cleanedXml = "";
      HtmlCleaner cleaner = new HtmlCleaner();
      CleanerProperties props = cleaner.getProperties();

      // customize cleaner's property setters
      props.setRecognizeUnicodeChars(false);

      // removing id attributes from the input xml. Need to check this behavior while
      // test.

      try
      {
         TagNode node = cleaner.clean(content);

         TagNode[] divNodes = node.getAllElements(true);
         for (int i = 0; i < divNodes.length; i++)
         {
            TagNode divNode = (divNodes[i]);
            divNode.removeAttribute("id");
         }

         cleanedXml = cleaner.getInnerHtml(node);

      }
      catch (ArrayIndexOutOfBoundsException ae)
      {
         logger.debug("Array out of bounds" + ae.getMessage());
      }
      catch (Exception e)
      {
         logger.debug("Error in cleaning the input xml from the rich Text Editor"
               + e.getMessage());
      }

      return cleanedXml;

   }

}
