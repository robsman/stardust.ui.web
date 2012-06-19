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
package org.eclipse.stardust.ui.web.common.reflect;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.stardust.ui.web.common.util.Functor;
import org.eclipse.stardust.ui.web.common.util.StringUtils;
import org.eclipse.stardust.ui.web.common.util.TransformingIterator;


/**
 * @author rsauer
 * @version $Revision: 7281 $
 */
public final class MethodDescriptor
{
   private final String name;
   private final Class returnType;
   private final Class[] argumentTypes;

   public static String encodeMethod(String name, Class[] argumentTypes)
   {
      StringBuffer buffer = new StringBuffer(100);

      buffer.append(name)
            .append('(')
            .append(StringUtils.join(new TransformingIterator(
                  Arrays.asList(argumentTypes).iterator(), new Functor()
                  {
                     public Object execute(Object source)
                     {
                        return ((Class) source).getName();
                     }
                  }), ", "))
            .append(')');

      return buffer.toString();
   }

   public MethodDescriptor(String name)
   {
      this(name, void.class);
   }

   public MethodDescriptor(String name, Class returnType)
   {
      this(name, returnType, Collections.EMPTY_LIST);
   }

   public MethodDescriptor(String name, List argumentTypes)
   {
      this(name, null, argumentTypes);
   }

   public MethodDescriptor(String name, Class returnType, List argTypes)
   {
      this.name = name;
      this.returnType = returnType;
      this.argumentTypes = (Class[]) argTypes.toArray(new Class[argTypes.size()]);
   }

   public String getName()
   {
      return name;
   }

   public Class getReturnType()
   {
      return returnType;
   }

   public List getArgumentTypes()
   {
      return Arrays.asList(argumentTypes);
   }

   public Class[] getArgumentTypeArray()
   {
      return argumentTypes;
   }

   public String toString()
   {
      return encodeMethod(name, argumentTypes);
   }
}