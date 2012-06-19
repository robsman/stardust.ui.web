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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.stardust.ui.web.common.util.FacesUtils;


public class SessionSharedObjectsMap implements Serializable
{
   private static final long serialVersionUID = -2035954718780555432L;
   
   private static final String BEAN_NAME = "sessionSharedObjectsMap";
   private Map<String, Object> objMap = new HashMap<String, Object>();
   
   public static SessionSharedObjectsMap getCurrent()
   {
      return (SessionSharedObjectsMap) FacesUtils.getBeanFromContext(BEAN_NAME);
   }
   
   public Object getObject(String key) {
      return objMap.get(key);
   }
   
   public void removeObject(String key) {
      objMap.remove(key);
   }
   
   public void setObject(String key, Object value) {
      objMap.put(key, value);
   }
}
