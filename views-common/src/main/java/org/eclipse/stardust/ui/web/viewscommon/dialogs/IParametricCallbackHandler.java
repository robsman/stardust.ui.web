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
package org.eclipse.stardust.ui.web.viewscommon.dialogs;

import java.util.Map;

/**
 * @author Yogesh.Manware
 * 
 */
public interface IParametricCallbackHandler extends ICallbackHandler
{

   /**
    * @param parameters
    */
   void setParameters(Map<String, Object> parameters);

   /**
    * @param parameters
    * @return
    */
   Map<String, Object> getParameters();
}
