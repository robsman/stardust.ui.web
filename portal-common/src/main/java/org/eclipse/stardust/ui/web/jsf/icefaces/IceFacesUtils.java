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
package org.eclipse.stardust.ui.web.jsf.icefaces;

import javax.faces.context.FacesContext;

import com.icesoft.faces.context.BridgeFacesContext;

/**
 * @author sauer
 * @version $Revision: $
 */
public class IceFacesUtils
{

   public static boolean isIceFaces(FacesContext facesContext)
   {
      return (facesContext instanceof BridgeFacesContext);
   }
   
}
