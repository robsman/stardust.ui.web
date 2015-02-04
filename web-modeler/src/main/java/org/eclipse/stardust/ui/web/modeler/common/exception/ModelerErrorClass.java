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
package org.eclipse.stardust.ui.web.modeler.common.exception;

import org.eclipse.stardust.common.error.ErrorCase;

/**
 * @author Yogesh.Manware
 * 
 */
public class ModelerErrorClass extends ErrorCase
{
   private static final long serialVersionUID = -2758652922569722703L;
   public final static ModelerErrorClass UNABLE_TO_DELETE_REFERENCED_MODEL = new ModelerErrorClass("ModelerError.01001");
   // add other errors

   public ModelerErrorClass(String id)
   {
      super(id);
   }
}