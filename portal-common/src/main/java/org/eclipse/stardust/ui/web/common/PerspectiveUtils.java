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
package org.eclipse.stardust.ui.web.common;

import static org.eclipse.stardust.ui.web.common.util.StringUtils.isEmpty;

import java.util.List;

/**
 * @author robert.sauer
 * @version $Revision: $
 */
public class PerspectiveUtils
{

   public static//
   <E extends UiElement, X extends UiExtension<E>> //
   void mergeExtensions(List<E> elements, List<X> extensions)
   {
      for (X extension : extensions)
      {
         int idxAfter = elements.size();

         if ( !isEmpty(extension.getAfter()) && !"*".equals(extension.getAfter()))
         {
            for (E def : elements)
            {
               if (extension.getAfter().equals(def.getName()))
               {
                  idxAfter = elements.indexOf(def) + 1;
                  break;
               }
            }
         }
         else if ( !isEmpty(extension.getBefore()))
         {
            if ("*".equals(extension.getBefore()))
            {
               idxAfter = 0;
            }
            else
            {
               for (E def : elements)
               {
                  if (extension.getBefore().equals(def.getName()))
                  {
                     idxAfter = elements.indexOf(def);
                     break;
                  }
               }
            }
         }

         // TODO handle before

         for (E entry : extension.getElements())
         {
            elements.add(idxAfter++ , entry);
         }
      }
   }
}
