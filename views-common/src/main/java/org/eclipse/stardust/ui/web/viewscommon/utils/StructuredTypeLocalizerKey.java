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

import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.core.struct.TypedXPath;
import org.eclipse.stardust.ui.web.viewscommon.common.LocalizerKey;

/**
 * @author Subodh.Godbole
 *
 */
public class StructuredTypeLocalizerKey extends LocalizerKey
{
   private static final long serialVersionUID = 1L;

   public final static int KEY_NAME = 1;
   public final static int KEY_DESC = 2;
   
   public StructuredTypeLocalizerKey(TypedXPath typedXPath, Model model, int mode)
   {
      super(ModelElementUtils.getBundleName(model.getModelOID()), buildKey(typedXPath, mode));
   }
   
   /**
    * @param typedXPath
    * @param mode
    * @return
    */
   private static String buildKey(TypedXPath typedXPath, int mode)
   {
      String val = null;
      val = "StructuredType." + getFullXPath(typedXPath);
      switch(mode)
      {
         case KEY_DESC:
            val += ".Description";
            break;
         case KEY_NAME:
         default:
            val += ".Name";
            break;
      }
      return val;
   }

   public boolean isMandatory()
   {
      return false;
   }

   /**
    * @param typedXPath
    * @return
    */
   private static String getFullXPath(TypedXPath typedXPath)
   {
      StringBuffer sb = new StringBuffer(typedXPath.getId());
      while (null != typedXPath.getParentXPath())
      {
         typedXPath = typedXPath.getParentXPath();
         sb.insert(0, typedXPath.getId() + ".");
      }

      return sb.toString();
   }
}
