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
import org.eclipse.stardust.common.error.PublicException;

/**
 * 
 * @author Yogesh.Manware
 * 
 */
public class ModelerException extends PublicException
{
   private static final long serialVersionUID = -6677900370771286544L;

   /**
    * @param errorCase
    */
   public ModelerException(ErrorCase errorCase)
   {
      super(errorCase);
   }
}
