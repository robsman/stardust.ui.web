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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.util.StringUtils;


/**
 * @author Subodh.Godbole
 *
 */
@SuppressWarnings("unchecked")
public class Reflect
{
   private static final Logger trace = LogManager.getLogger(Reflect.class);

   private Reflect()
   {
   }
   
   /**
    * Returns the Method object from the CARNOT internal stringified representation
    * 
    * @return the Method object found
    * @throws ReflectionException if no match was found.
    */
   public static Method decodeMethod(Class type, String encodedMethod)
   {
      if (StringUtils.isEmpty(encodedMethod))
      {
         throw new ReflectionException("Encoded method is empty.");
      }
      
      MethodDescriptor descriptor = describeEncodedMethod(encodedMethod);
      String name = descriptor.getName();
      Class[] args = descriptor.getArgumentTypeArray();
      try
      {
         Method method = null;
         int currentMatch = 0;
         Method[] methods = type.getMethods();
         for (int i = 0; i < methods.length; i++)
         {
            Method mtd = methods[i];
            Class[] params = mtd.getParameterTypes();
            if (mtd.getName().equals(name) && params.length == args.length)
            {
               int match = match(params, args);
               if (match >= 0)
               {
                  if (method == null || match < currentMatch)
                  {
                     method = mtd;
                     currentMatch = match;
                     if (match == 0)
                     {
                        break;
                     }
                  }
               }
            }
         }

//         Method method = type.getMethod(name, args);
         
//       no need for searching in interfaces. If the method is defined in an
//       interface, then it must be public in the implementing class         
/*         if (!method.isAccessible())
         {
            Class[] interfaces = type.getInterfaces();
            for (int i = 0; i < interfaces.length; i++)
            {
               try
               {
                  Method intfMethod = interfaces[i].getMethod(name, args);
                  if (intfMethod != null)
                  {
                     return intfMethod;
                  }
               }
               catch (Exception ex)
               {
                  // go to the next interface
               }
            }
            // fall back to original method
         }*/
         return method;
      }
      catch (Exception e)
      {
         throw new ReflectionException("Method '" + descriptor + "' in '"
               + type + "' cannot be found or accessed.", e);
      }
   }
   
   public static MethodDescriptor describeEncodedMethod(String encodedMethod)
   {
      if (StringUtils.isEmpty(encodedMethod))
      {
         return null;
      }
      int lparenIndex = encodedMethod.indexOf('(');
      final int rparenIndex;

      if (-1 == lparenIndex)
      {
         lparenIndex = encodedMethod.length();
         rparenIndex = lparenIndex + 1;
      }
      else
      {
         rparenIndex = encodedMethod.indexOf(')', lparenIndex);

         if (-1 == rparenIndex)
         {
            throw new ReflectionException("Syntax error: missing terminating ')' after '(' "
                  + "in encoded method '" + encodedMethod + "'");
         }
      }

      MethodDescriptor method;
      if ((encodedMethod.length() == lparenIndex) || (lparenIndex + 1 == rparenIndex))
      {
         method = new MethodDescriptor(encodedMethod.substring(0, lparenIndex));
      }
      else
      {
         String parameterString = encodedMethod.substring(lparenIndex + 1, rparenIndex);
         List argumentTypes = new ArrayList();

         if (trace.isDebugEnabled())
         {
            trace.debug("Parsing method parameter list encoded as '" + parameterString
                  + "'.");
         }

         Iterator classNamesIter = StringUtils.split(parameterString, ",");

         if (parameterString.indexOf('<') > 0)
         {
            List classNames = getRawParamNames(parameterString);
            classNamesIter = classNames.iterator();
         }

         for (Iterator i = classNamesIter; i.hasNext();)
         {
            String className = ((String) i.next()).trim();

            try
            {
               argumentTypes.add(getClassFromClassName(className));
            }
            catch (ReflectionException e)
            {
               throw new ReflectionException("Class '" + className
                     + "' for parameter not found (" +encodedMethod +").", e);
            }
         }

         String methodName = encodedMethod.substring(0, lparenIndex);
         method = new MethodDescriptor(methodName, argumentTypes);
      }

      return method;
   }

   private static List getRawParamNames(String parameterString)
   {
      boolean endOfMtd = false;
      List /* <String> */classNames = new ArrayList();
      int lsplitIdx = 0;
      int rsplitIdx = parameterString.indexOf(',');
      if (rsplitIdx < 0)
      {
         rsplitIdx = parameterString.length();
      }

      while (!endOfMtd)
      {
         endOfMtd = rsplitIdx == parameterString.length();
         String subString = parameterString.substring(lsplitIdx, rsplitIdx);
         int charCount1 = 0;
         int charCount2 = 0;
         int idx = subString.indexOf('<');
         while (idx > 0)
         {
            charCount1++;
            idx = subString.indexOf('<', idx + 1);
         }
         idx = subString.indexOf('>');
         while (idx > 0)
         {
            charCount2++;
            idx = subString.indexOf('>', idx + 1);
         }
         if (charCount1 == charCount2)
         {
            classNames.add(subString);
            lsplitIdx = rsplitIdx + 1;
            rsplitIdx = parameterString.indexOf(',', rsplitIdx + 1);
         }
         else
         {
            rsplitIdx = parameterString.indexOf(',', rsplitIdx + 1);
         }
         if (rsplitIdx < 0)
         {
            rsplitIdx = parameterString.length();
         }
      }
      return classNames;
   }
   
   /**
    * Retrieves a class object from the class name. For "normal" Java classes,
    * the return value is the result of <code>Class.forName()</code>. For
    * primitive types <code>int</code>, <code>float</code>, ... their pseudo
    * class object e.g. <code>Integer.TYPE</code> is returned.
    *
    * @throws ReflectionException If the lookup fails.
    *
    * @see #getClassFromAbbreviatedName(java.lang.String)
    */
   public static Class getClassFromClassName(String className)
   {
      return getClassFromClassName(className, true);
   }

   /**
    * Retrieves a class object from the class name. For "normal" Java classes,
    * the return value is the result of <code>Class.forName()</code>. For
    * primitive types <code>int</code>, <code>float</code>, ... their pseudo
    * class object e.g. <code>Integer.TYPE</code> is returned.
    * If <code>isMandatory</code> is set to false then <code>null</code> will 
    * be returned instead of throwing an <code>ReflectionException</code>.  
    *
    * @throws ReflectionException If the lookup fails and lenientLookup is enabled.
    *
    * @see #getClassFromAbbreviatedName(java.lang.String)
    */
   public static Class getClassFromClassName(String className, boolean isMandatory)
   {
      Class clazz = null;
      if ( !StringUtils.isEmpty(className))
      {
         className = getRawClassName(className);
         try
         {
            if (className.equals(Boolean.TYPE.getName()))
            {
               return Boolean.TYPE;
            }
            else if (className.equals(Character.TYPE.getName()))
            {
               return Character.TYPE;
            }
            else if (className.equals(Byte.TYPE.getName()))
            {
               return Byte.TYPE;
            }
            else if (className.equals(Short.TYPE.getName()))
            {
               return Short.TYPE;
            }
            else if (className.equals(Integer.TYPE.getName()))
            {
               return Integer.TYPE;
            }
            else if (className.equals(Long.TYPE.getName()))
            {
               return Long.TYPE;
            }
            else if (className.equals(Float.TYPE.getName()))
            {
               return Float.TYPE;
            }
            else if (className.equals(Double.TYPE.getName()))
            {
               return Double.TYPE;
            }
            else if (className.equals(Void.TYPE.getName()))
            {
               return Void.TYPE;
            }
            else
            {
               ClassLoader classLoader = getContextClassLoader();
               if (null != classLoader)
               {
                  clazz = Class.forName(className, true, classLoader);
               }
               else
               {
                  clazz = Class.forName(className);
               }
            }
         }
         catch (Exception x)
         {
            if (isMandatory)
            {
               throw new ReflectionException("Cannot retrieve class from class name '"
                     + className + "'.", x);
            }
         }
      }
      else
      {
         if (isMandatory)
         {
            // @todo (france, ub): throwing an exception here is experimental
            throw new ReflectionException("Empty class name.");
         }
      }
      
      return clazz;
   }

   /**
    * @param clazz
    * @param name
    * @param value
    */
   public static void setStaticFieldValue(Class clazz, String name, Object value)
   {
      Field field = getField(clazz, name);
      if (null != field)
      {
         field.setAccessible(true);
         try
         {
            field.set(null, value);
         }
         catch (Exception e)
         {
            throw new RuntimeException(e);
         }
      }
      else
      {
         throw new RuntimeException("Field '" + name + "' for '" + clazz.getName() + "' not found");
      }
   }

   /**
    * @param clazz
    * @param name
    * @return
    */
   public static Field getField(Class clazz, String name)
   {
      Field field = null;

      if (null != clazz)
      {
         try
         {
            field = clazz.getDeclaredField(name);
            field.setAccessible(true);
         }
         catch (NoSuchFieldException e)
         {
            // ignore, bubble up to super class
         }
         catch (SecurityException e)
         {
            throw new RuntimeException(e);
         }
         if (null == field)
         {
            field = getField(clazz.getSuperclass(), name);
         }
      }
      return field;
   }

   /**
    * @return the context classloader. May be null if it is not set or usage of 
    *         this classloader is disabled by configuration (default behavior).  
    */
   private static ClassLoader getContextClassLoader()
   {
      ClassLoader classLoader = null;
      classLoader = Thread.currentThread().getContextClassLoader();
   
      return classLoader;
   }

   private static String getRawClassName(String className)
   {
      // strip type parameters info if present
      int ix = className.indexOf('<');
      if (ix > 0)
      {
         className = className.substring(0, ix);
      }
      return className;
   }

   private static int match(Class[] params, Class[] args)
   {
      int match = 0;
      for (int i = 0; i < params.length; i++)
      {
         if (params[i].equals(Object.class))
         {
            if (!args[i].equals(Object.class))
            {
               match++;
            }
         }
         else if (!params[i].equals(args[i]))
         {
            return -1;
         }
      }
      return match;
   }
}
