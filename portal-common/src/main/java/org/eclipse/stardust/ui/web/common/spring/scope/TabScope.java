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
package org.eclipse.stardust.ui.web.common.spring.scope;

import java.util.Map;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;

/**
 * @author Robert.Sauer
 * @version $Revision: $
 */
public class TabScope implements Scope
{
   /**
    * @deprecated replaced by request bound tab scope manager
    */
   private TabScopeManager tabScopeManager;

   /**
    * @deprecated replaced by request bound tab scope manager
    */
   public void setTabScopeManager(TabScopeManager tabScopeManager)
   {
      this.tabScopeManager = tabScopeManager;
   }

   public Object get(String name, ObjectFactory objectFactory)
   {
      // tab scope bound to request should have preference
      TabScopeManager manager = TabScopeUtils.getCurrentTabScope();

      // TODO remove this branch
      if (null == manager)
      {
         // fall back to currently active tab (single active tab supported only)
         manager = tabScopeManager;
      }
      
      Object bean = null;

      if (null != manager)
      {
         Map<String, Object> currentTabScope = manager.getCurrentTabScope();
         if (null != currentTabScope)
         {
            bean = currentTabScope.get(name);
            if (null == bean)
            {
               bean = objectFactory.getObject();
               if (null != bean)
               {
                  currentTabScope.put(name, bean);
               }
            }
         }
      }
      
      return bean;
   }

   public String getConversationId()
   {
      // TODO delegate to tab scope manager
      return null;
   }

   public void registerDestructionCallback(String name, Runnable callback)
   {
      // tab scope bound to request should have preference
      TabScopeManager manager = TabScopeUtils.getCurrentTabScope();

      // TODO remove this branch
      if (null == manager)
      {
         // fall back to currently active tab (single active tab supported only)
         manager = tabScopeManager;
      }
      
      if (null != manager)
      {
         Map<String, Runnable> destructionCallbacks = manager.getCurrentTabScopeDestructionCallbacks();
         if (null != destructionCallbacks)
         {
            destructionCallbacks.put(name, callback);
         }
      }
   }

   public Object remove(String name)
   {
      // tab scope bound to request should have preference
      TabScopeManager manager = TabScopeUtils.getCurrentTabScope();

      // TODO remove this branch
      if (null == manager)
      {
         // fall back to currently active tab (single active tab supported only)
         manager = tabScopeManager;
      }
      
      Object bean = null;
      
      if (null != manager)
      {
         Map<String, Object> currentTabScope = manager.getCurrentTabScope();
         if (null != currentTabScope)
         {
            bean = currentTabScope.get(name);
            if (null != bean)
            {
               currentTabScope.remove(name);

               // remove destruction callback (see #registerDestructionCallback contract)
               Map<String, Runnable> destructionCallbacks = manager.getCurrentTabScopeDestructionCallbacks();
               if (null != destructionCallbacks)
               {
                  destructionCallbacks.remove(name);
               }
            }
         }
      }

      return bean;
   }
}
