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
package org.eclipse.stardust.ui.web.viewscommon.docmgmt;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.stardust.ui.web.viewscommon.dialogs.IParametricCallbackHandler;


/**
 * 
 * @author Yogesh.Manware
 * 
 */
public abstract class ParametricCallbackHandler implements IParametricCallbackHandler
{
   Map<String, Object> parameters;

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.eclipse.stardust.ui.web.viewscommon.dialogs.IParametricCallbackHandler#setParameters
    * (java.util.Map)
    */
   public void setParameters(Map<String, Object> parameters)
   {
      this.parameters = parameters;
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.eclipse.stardust.ui.web.viewscommon.dialogs.IParametricCallbackHandler#getParameters
    * ()
    */
   public Map<String, Object> getParameters()
   {
      return parameters;
   }
   
   /**
    * @param key
    * @param value
    */
   public void setParameter(String key, Object value)
   {
      if (null == parameters)
      {
         this.parameters = new HashMap<String, Object>();
         this.parameters.put(key, value);
      }
   }
   
   /**
    * @param key
    * @return
    */
   public Object getParameter(String key)
   {
      if (null != parameters)
      {
         return this.parameters.get(key);
      }
      return null;
   }
}
