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

import org.eclipse.stardust.engine.api.runtime.ProcessInstance;

public class ProcessContextCache
{
   
   private final ProcessContextCacheManager cacheManager;
   
   private final ProcessInstance processInstance;
   
   ProcessContextCache(ProcessContextCacheManager cacheManager, ProcessInstance processInstance)
   {
      this.cacheManager = cacheManager;
      this.processInstance = processInstance;
   }

   public ProcessInstance getProcessInstance()
   {
      return processInstance;
   }
   
}
