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

import org.eclipse.stardust.engine.api.model.ModelElement;
import org.eclipse.stardust.ui.web.viewscommon.common.LocalizerKey;



public class ModelElementLocalizerKey extends LocalizerKey
{
   public final static int KEY_NAME = 1;
   public final static int KEY_DESC = 2;
   
   public ModelElementLocalizerKey(ModelElement modelElement, int mode)
   {
      super(ModelElementUtils.getBundleName(modelElement), 
            buildKey(modelElement, mode));
   }
   
   private static String buildKey(ModelElement modelElement, int mode)
   {
      String val = null;
      val = ModelElementUtils.getNLSPrefix(modelElement);
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
}
