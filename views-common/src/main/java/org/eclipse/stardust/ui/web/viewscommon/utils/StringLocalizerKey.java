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
import org.eclipse.stardust.ui.web.viewscommon.common.LocalizerKey;

/**
 * @author Subodh.Godbole
 *
 */
public class StringLocalizerKey extends LocalizerKey
{
   private static final long serialVersionUID = 1L;

   /**
    * @param key
    * @param model
    */
   public StringLocalizerKey(String key, Model model)
   {
      super(ModelElementUtils.getBundleName(model.getModelOID()), key);
   }
   
   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.viewscommon.common.LocalizerKey#isMandatory()
    */
   public boolean isMandatory()
   {
      return false;
   }
}
