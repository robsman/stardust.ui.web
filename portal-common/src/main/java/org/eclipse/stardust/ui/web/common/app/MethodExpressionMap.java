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
package org.eclipse.stardust.ui.web.common.app;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.el.ELContext;
import javax.el.MethodExpression;
import javax.el.MethodInfo;

/**
 * Not thread safe.
 * 
 * @author Pierre Asselin
 */
public class MethodExpressionMap implements Map<String, MethodExpression>
{

   MethodExpression methodExpression;

   public MethodExpressionMap()
   {
      methodExpression = new MethodExpression()
      {
         MethodInfo methodInfo = new MethodInfo("openView", Void.TYPE, null);

         @Override
         public MethodInfo getMethodInfo(ELContext elcontext)
         {
            return methodInfo;
         }

         @Override
         public Object invoke(ELContext elcontext, Object[] aobj)
         {
            return null;
         }

         @Override
         public boolean equals(Object obj)
         {
            return false;
         }

         @Override
         public String getExpressionString()
         {
            return null;
         }

         @Override
         public int hashCode()
         {
            return 0;
         }

         @Override
         public boolean isLiteralText()
         {
            return false;
         }

      };
   }

   public void clear()
   {
   // ignored
   }

   public boolean containsKey(Object key)
   {
      return false;
   }

   public boolean containsValue(Object value)
   {
      return false;
   }

   public Set<java.util.Map.Entry<String, MethodExpression>> entrySet()
   {
      return null;
   }

   public MethodExpression get(Object view)
   {
      return null;
   }

   public boolean isEmpty()
   {
      return false;
   }

   public Set<String> keySet()
   {
      return null;
   }

   public MethodExpression put(String key, MethodExpression value)
   {
      return null;
   }

   public void putAll(Map< ? extends String, ? extends MethodExpression> m)
   {
   }

   public MethodExpression remove(Object key)
   {
      return null;
   }

   public int size()
   {
      return 0;
   }

   public Collection<MethodExpression> values()
   {
      return null;
   }
}
