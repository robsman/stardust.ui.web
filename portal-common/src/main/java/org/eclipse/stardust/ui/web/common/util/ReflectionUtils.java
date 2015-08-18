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
package org.eclipse.stardust.ui.web.common.util;

import static java.util.Collections.synchronizedMap;
import static org.eclipse.stardust.common.CollectionUtils.newConcurrentHashMap;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.stardust.ui.web.common.reflect.Reflect;
import org.eclipse.stardust.ui.web.common.reflect.ReflectionException;
import org.eclipse.stardust.ui.web.common.util.StringUtils;


/**
 * @author Subodh.Godbole
 *
 */
public class ReflectionUtils
{
   private static final Object[] NO_ARGS = new Object[0];

   private static Map<Class< ? >, ConcurrentMap<String, Method>> propertyAccessorCache = synchronizedMap(new WeakHashMap<Class< ? >, ConcurrentMap<String, Method>>());

   /**
    * @param obj
    * @param methodName
    * @param params
    * @return
    */
   @SuppressWarnings("rawtypes")
   public static Object getMethod(Object obj, String methodName, Object... params)
   {
      if(obj != null && !StringUtils.isEmpty(methodName))
      {
         Class[] paramTypes = null;
         
         if(params != null)
         {
            paramTypes = new Class[params.length];
            int i = 0;
            for (Object object : params)
            {
               paramTypes[i++] = object.getClass();
            }
         }      
         
         try
         {
            return obj.getClass().getMethod(methodName, paramTypes);
         }
         catch (Exception e)
         {
            //NOP
         }
      }
      
      return null;
   }

   /**
    * @param obj
    * @param methodName
    * @param params
    * @param paramTypes
    * @return
    * @throws Exception
    */
   @SuppressWarnings("rawtypes")
   public static Object invokeMethod(Object obj, String methodName, Object[] params, Class[] paramTypes) throws Exception
   {
      if(obj != null && !StringUtils.isEmpty(methodName))
      {
         Method method = obj.getClass().getMethod(methodName, paramTypes);
         Object ret = method.invoke(obj, params);
   
         return ret;
      }
      
      return null;
   }

   /**
    * @param obj
    * @param methodName
    * @param params
    * @return
    * @throws Exception
    */
   @SuppressWarnings("rawtypes")
   public static Object invokeMethod(Object obj, String methodName, Object... params) throws Exception
   {
      if(obj != null && !StringUtils.isEmpty(methodName))
      {
         Class[] paramTypes = null;
         
         if(params != null)
         {
            paramTypes = new Class[params.length];
            int i = 0;
            for (Object object : params)
            {
               paramTypes[i++] = object.getClass();
            }
         }      
         
         Method method = obj.getClass().getMethod(methodName, paramTypes);
         Object ret = method.invoke(obj, params);
   
         return ret;
      }
      
      return null;
   }

   /**
    * @param obj
    * @param methodName
    * @return
    * @throws Exception
    */
   public static Object invokeMethod(Object obj, String methodName) throws Exception
   {
      return invokeMethod(obj, methodName, NO_ARGS);
   }

   /**
    * @param obj
    * @param fieldName
    * @return
    * @throws Exception
    */
   public static Object invokeGetterMethod(Object obj, String fieldName) throws Exception
   {
      Class<?> objClass = obj.getClass();
      ConcurrentMap<String, Method> cachedAccessors = propertyAccessorCache.get(objClass);
      if (null == cachedAccessors)
      {
         cachedAccessors = newConcurrentHashMap();
      }

      if (!cachedAccessors.containsKey(fieldName))
      {
         // perform cost of method lookup only once, caching the result for reuse
         Class<?>[] getterArgs = new Class[0];
         Method getterMethod;
         try
         {
            getterMethod = objClass.getMethod("get" + toSentenseCase(fieldName), getterArgs);
         }
         catch (NoSuchMethodException nsme1)
         {
            try
            {
               getterMethod = objClass.getMethod("is" + toSentenseCase(fieldName), getterArgs);
            }
            catch(NoSuchMethodException nsme2)
            {
               throw new NoSuchMethodException("No Getter Method for Object: " +
                     objClass.getName() + ", On Field: " + fieldName);
            }
         }

         cachedAccessors.putIfAbsent(fieldName, getterMethod);
      }

      if (!propertyAccessorCache.containsKey(objClass))
      {
         propertyAccessorCache.put(objClass, cachedAccessors);
      }

      Method getterMethod = cachedAccessors.get(fieldName);

      try
      {
         return getterMethod.invoke(obj, NO_ARGS);
      }
      catch (InvocationTargetException ite)
      {
         if (ite.getCause() instanceof Exception)
         {
            throw (Exception) ite.getCause();
         }
         else
         {
            throw ite;
         }
      }
   }

   /**
    * @param <R>
    * @param targetClassName
    * @param methodName
    * @param args
    * @return
    */
   @SuppressWarnings("unchecked")
   public static <R> R invokeStaticMethod(String targetClassName, String methodName, Object... args)
   {
      Class<?> clsTarget = Reflect.getClassFromClassName(targetClassName);
      Method mthd = Reflect.decodeMethod(clsTarget, methodName);
      
      try
      {
         return (R) mthd.invoke(null, args);
      }
      catch (InvocationTargetException e)
      {
         if (e.getTargetException() instanceof RuntimeException)
         {
            throw (RuntimeException) e.getTargetException();
         }
         else
         {
            throw new ReflectionException("Failed invoking " + targetClassName + "." + methodName, e.getTargetException());
         }
      }
      catch (Exception e)
      {
         throw new ReflectionException("Failed invoking " + targetClassName + "." + methodName, e);
      }
   }

   /**
    * @param <R>
    * @param targetClassName
    * @param methodName
    * @param target
    * @param args
    * @return
    */
   @SuppressWarnings("unchecked")
   public static <R> R invokeMethod(String targetClassName, String methodName, Object target, Object... args)
   {
      Class<?> clsTarget = Reflect.getClassFromClassName(targetClassName);
      Method mthd = Reflect.decodeMethod(clsTarget, methodName);
      
      try
      {
         return (R) mthd.invoke(target, args);
      }
      catch (InvocationTargetException e)
      {
         if (e.getTargetException() instanceof RuntimeException)
         {
            throw (RuntimeException) e.getTargetException();
         }
         else
         {
            throw new ReflectionException("Failed invoking " + targetClassName + "." + methodName, e.getTargetException());
         }
      }
      catch (Exception e)
      {
         throw new ReflectionException("Failed invoking " + targetClassName + "." + methodName, e);
      }
   }

   /**
    * @param str
    * @return
    */
   public static String toSentenseCase(final String str)
   {
      String retStr = null;
      if(!StringUtils.isEmpty(str))
      {
         String firstChar = "" + str.charAt(0);
         retStr = firstChar.toUpperCase() + str.substring(1);
      }

      return retStr;
   }
   
   /**
    * @param className
    * @param loader
    * @return
    */
   @SuppressWarnings("unchecked")
   public static Object createInstance(String className, ClassLoader loader)
   {
      try
      {
         className = getRawClassName(className);
         Class clazz = Class.forName(className, true, loader);
         return clazz.newInstance();
      }
      catch (Exception e)
      {
         throw new RuntimeException("Cannot instantiate class '" + className + "'.", e);
      }
   }
   
   /**
    * @param className
    * @return
    */
   public static Object createInstance(String className)
   {
      return createInstance(className, null, null);
   }

   /**
    * @param className
    * @param argTypes
    * @param args
    * @return
    */
   @SuppressWarnings("unchecked")
   public static Object createInstance(String className, Class[] argTypes, Object[] args)
   {
      Class clazz = getClassFromClassName(className);
      
      return createInstance(clazz, argTypes, args);
   }

   /**
    * @param clazz
    * @param argTypes
    * @param args
    * @return
    */
   @SuppressWarnings("unchecked")
   public static Object createInstance(Class clazz, Class[] argTypes, Object[] args)
   {
      try
      {
         if (null == argTypes)
         {
            return clazz.newInstance();
         }
         else
         {
            Constructor ctor = clazz.getConstructor(argTypes);
            return ctor.newInstance(args);
         }
      }
      catch (Exception e)
      {
         throw new RuntimeException("Cannot instantiate class '" + clazz.getName()
               + "'.", e);
      }
   }
   
   /**
    * @param className
    * @return
    */
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
   
   /**
    * @param className
    * @return
    */
   @SuppressWarnings("unchecked")
   public static Class getClassFromClassName(String className)
   {
      return getClassFromClassName(className, true);
   }
   
   /**
    * @param className
    * @param isMandatory
    * @return
    */
   @SuppressWarnings("unchecked")
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
               throw new RuntimeException("Cannot retrieve class from class name '"
                     + className + "'.", x);
            }
         }
      }
      else
      {
         if (isMandatory)
         {
            // @todo (france, ub): throwing an exception here is experimental
            throw new RuntimeException("Empty class name.");
         }
      }
      
      return clazz;
   }

   /**
    * @return
    */
   private static ClassLoader getContextClassLoader()
   {
      ClassLoader classLoader = null;
      classLoader = Thread.currentThread().getContextClassLoader();
      return classLoader;
   }
   
   /**
    * check given class(fully qualified Class name) in classPath
    * 
    * @param className
    * @return boolean
    */
   public static boolean isClassInClassPath(String className)
   {
      try
      {
         // initialize parameter is false to avoid Class initialization
         Class.forName(className, false, getContextClassLoader());
         return true;
      }
      catch (ClassNotFoundException e)
      {
      }
      return false;

   }
}
