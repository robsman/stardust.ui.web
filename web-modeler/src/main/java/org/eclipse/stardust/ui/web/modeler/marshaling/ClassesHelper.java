package org.eclipse.stardust.ui.web.modeler.marshaling;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.eclipse.stardust.common.annotations.ParameterName;
import org.eclipse.stardust.common.annotations.ParameterNames;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.model.xpdl.carnot.DirectionType;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public final class ClassesHelper
{

   private static final Map<String, Class<? >> PRIMITIVE_NAME_TYPE_MAP = new HashMap<String, Class<? >>();

   static
   {
      PRIMITIVE_NAME_TYPE_MAP.put("boolean", Boolean.TYPE);
      PRIMITIVE_NAME_TYPE_MAP.put("byte", Byte.TYPE);
      PRIMITIVE_NAME_TYPE_MAP.put("char", Character.TYPE);
      PRIMITIVE_NAME_TYPE_MAP.put("short", Short.TYPE);
      PRIMITIVE_NAME_TYPE_MAP.put("int", Integer.TYPE);
      PRIMITIVE_NAME_TYPE_MAP.put("long", Long.TYPE);
      PRIMITIVE_NAME_TYPE_MAP.put("float", Float.TYPE);
      PRIMITIVE_NAME_TYPE_MAP.put("double", Double.TYPE);
   }

   public static Class<? > getPrimitiveTypeForName(final String name)
   {
      return (Class<? >) PRIMITIVE_NAME_TYPE_MAP.get(name);
   }

   private static final Class<? >[] PRIMITIVE_WRAPPER_CLASSES_MAP = {
         Boolean.TYPE, Boolean.class, Byte.TYPE, Byte.class, Character.TYPE,
         Character.class, Double.TYPE, Double.class, Float.TYPE, Float.class,
         Integer.TYPE, Integer.class, Long.TYPE, Long.class, Short.TYPE, Short.class,};

   public static Class<? > getPrimitiveWrapperClass(final Class<? > type)
   {
      if ( !type.isPrimitive())
      {
         throw new IllegalArgumentException("type is not a primitive class");
      }

      for (int i = 0; i < PRIMITIVE_WRAPPER_CLASSES_MAP.length; i += 2)
      {
         if (type.equals(PRIMITIVE_WRAPPER_CLASSES_MAP[i]))
            return PRIMITIVE_WRAPPER_CLASSES_MAP[i + 1];
      }

      return null;
   }

   public static boolean isPrimitiveWrapperClass(final Class<? > type)
   {
      for (int i = 0; i < PRIMITIVE_WRAPPER_CLASSES_MAP.length; i += 2)
      {
         if (type.equals(PRIMITIVE_WRAPPER_CLASSES_MAP[i + 1]))
         {
            return true;
         }
      }

      return false;
   }

   public static boolean isPrimitiveType(final Class<? > type)
   {
      if (type.isPrimitive() || isPrimitiveWrapperClass(type))
      {
         return true;
      }

      return false;
   }

   public static boolean isPrimitiveType(final String type)
   {
      return PRIMITIVE_NAME_TYPE_MAP.containsKey(type);
   }

   public static Class<? > loadClass(String className, ClassLoader classLoader)
         throws ClassNotFoundException
   {

      // ClassLoader.loadClass() does not handle primitive types:

      if (className.length() == 1)
      {
         char type = className.charAt(0);
         if (type == 'B')
            return Byte.TYPE;
         if (type == 'C')
            return Character.TYPE;
         if (type == 'D')
            return Double.TYPE;
         if (type == 'F')
            return Float.TYPE;
         if (type == 'I')
            return Integer.TYPE;
         if (type == 'J')
            return Long.TYPE;
         if (type == 'S')
            return Short.TYPE;
         if (type == 'Z')
            return Boolean.TYPE;
         if (type == 'V')
            return Void.TYPE;

         throw new ClassNotFoundException(className);
      }

      if (isPrimitiveType(className) == true)
         return (Class<? >) ClassesHelper.PRIMITIVE_NAME_TYPE_MAP.get(className);

      if (className.charAt(0) == 'L' && className.charAt(className.length() - 1) == ';')
         return classLoader.loadClass(className.substring(1, className.length() - 1));

      try
      {
         return classLoader.loadClass(className);
      }
      catch (ClassNotFoundException e)
      {
         if (className.charAt(0) != '[')
            throw e;
      }

      int arrayDimension = 0;
      while (className.charAt(arrayDimension) == '[')
         arrayDimension++ ;

      Class<? > componentType = loadClass(className.substring(arrayDimension),
            classLoader);

      return Array.newInstance(componentType, new int[arrayDimension]).getClass();
   }

   public static void addReturnTypeAccessPoint(JsonArray accessPointsJson, Method method)
   {
      JsonObject accessPointJson;
      accessPointJson = new JsonObject();

      accessPointsJson.add(accessPointJson);
      accessPointJson.addProperty(ModelerConstants.ID_PROPERTY, "returnValue");
      accessPointJson.addProperty(ModelerConstants.NAME_PROPERTY, "returnValue");
      accessPointJson.addProperty(ModelerConstants.DIRECTION_PROPERTY,
            DirectionType.OUT_LITERAL.toString());
      accessPointJson.addProperty(ModelerConstants.DATA_TYPE_SIMPLENAME,
            method.getReturnType().getSimpleName());

      if (method.getReturnType() != null)
      {
         if (method.getReturnType().isPrimitive())
         {
            accessPointJson.addProperty(ModelerConstants.DATA_TYPE_PROPERTY,
                  PredefinedConstants.PRIMITIVE_DATA);
            accessPointJson.addProperty(ModelerConstants.PRIMITIVE_DATA_TYPE_PROPERTY,
                  method.getReturnType().getName());
         }
         else
         {
            accessPointJson.addProperty(ModelerConstants.DATA_TYPE_PROPERTY,
                  PredefinedConstants.SERIALIZABLE_DATA);
            accessPointJson.addProperty("javaClass", method.getReturnType().getName());
         }
      }
   }

   public static void addParameterAccessPoints(JsonArray accessPointsJson, Method method)
   {
      ParameterName[] values = getParameterLabels(method);
      JsonObject accessPointJson;
      for (int n = 0; n < method.getParameterTypes().length; ++n)
      {
         accessPointJson = new JsonObject();

         accessPointsJson.add(accessPointJson);
         accessPointJson.addProperty(ModelerConstants.DIRECTION_PROPERTY,
               DirectionType.IN_LITERAL.toString());

         Class<? > parameterType = method.getParameterTypes()[n];
         String paramName = parameterType.getSimpleName();
         String paramId = paramName.toLowerCase().charAt(0) + "Param" + (n + 1);
         String paramLabel = getParameterLabel(method, n, values, paramId);

         accessPointJson.addProperty(ModelerConstants.ID_PROPERTY, paramId);
         accessPointJson.addProperty(ModelerConstants.NAME_PROPERTY, paramLabel);
         accessPointJson.addProperty(ModelerConstants.DATA_TYPE_SIMPLENAME,
               parameterType.getSimpleName());

         if (parameterType.isPrimitive())
         {
            accessPointJson.addProperty(ModelerConstants.DATA_TYPE_PROPERTY,
                  ModelerConstants.PRIMITIVE_DATA_TYPE_KEY);
            accessPointJson.addProperty(ModelerConstants.PRIMITIVE_DATA_TYPE_PROPERTY,
                  PredefinedConstants.PRIMITIVE_DATA);
         }
         else
         {
            accessPointJson.addProperty(ModelerConstants.DATA_TYPE_PROPERTY,
                  PredefinedConstants.SERIALIZABLE_DATA);
            accessPointJson.addProperty("javaClass",
                  method.getParameterTypes()[n].getName());
         }
      }
   }

   private static <T extends Annotation> T[] getParameterLabels(Method method)
   {
      for (Annotation a : method.getAnnotations())
      {
         if (matchName("ParameterNames", a))
         {
            return getValue(a);
         }
      }
      return null;
   }

   private static <T> T getValue(Annotation a)
   {
      try
      {
         Method m = a.getClass().getMethod("value");
         @SuppressWarnings("unchecked")
         T result = (T) m.invoke(a);
         return result;
      }
      catch (Exception e)
      {
         // (fh) do nothing because either:
         // - the method do not exist or
         // - we don't have access to it
         // - it returns a different type than expected
      }
      return null;
   }

   private static boolean matchName(String name, Annotation a)
   {
      return name.equals(a.annotationType().getSimpleName());
   }

   private static String getParameterLabel(Method method, int n, ParameterName[] values, String paramId)
   {
      Annotation nameAnnotation = findParameterName(method, n);
      String paramLabel = nameAnnotation == null ? null : ClassesHelper.<String>getValue(nameAnnotation);
      if (StringUtils.isEmpty(paramLabel) && values != null && n < values.length)
      {
         paramLabel = values[n].value();
      }
      return StringUtils.isEmpty(paramLabel) ? paramId : paramLabel;
   }

   private static Annotation findParameterName(Method method, int parameterIndex)
   {
      Annotation[] paramAnnotations = method.getParameterAnnotations()[parameterIndex];
      for (Annotation annotation : paramAnnotations)
      {
         if (matchName("ParameterName", annotation))
         {
            return annotation;
         }
      }
      return null;
   }

   public static Method getMethodBySignature(ClassLoader classLoader, String className,
         String methodSignature)
   {
      Method method = null;
      try
      {

         Class<? > type = classLoader.loadClass(className);

         String methodName = methodSignature.split("\\(")[0];

         String signature = methodSignature.split("\\(")[1];

         signature = signature.substring(0, signature.length() - 1);

         String[] parameterClassNames;

         Class<? >[] parameterClasses = new Class<? >[]{};

         if ( !StringUtils.isEmpty(signature))
         {
            signature = removeErasures(signature);

            parameterClassNames = signature.split(",");

            parameterClasses = new Class[parameterClassNames.length];

            for (int n = 0; n < parameterClassNames.length; ++n)
            {
               if (parameterClassNames[n].indexOf("[") > -1)
               {
                  parameterClassNames[n] = getArrayName(parameterClassNames[n]);
               }
               parameterClasses[n] = ClassesHelper.loadClass(parameterClassNames[n],
                     classLoader);
            }

         }

         method = type.getMethod(methodName, parameterClasses);

      }
      catch (Throwable t)
      {
         t.printStackTrace();
      }

      return method;
   }

   private static String removeErasures(String signature)
   {
      signature = signature.replace("[", "#");
      signature = signature.replace("]", "&");
      signature = signature.replaceAll("<[^\\<]*\\>", "");
      signature = signature.replace("&", "]");
      signature = signature.replace("#", "[");
      return signature;
   }

   public static String getArrayName(String className) throws ClassNotFoundException
   {
      Pattern arrayPattern = Pattern.compile("([\\w\\.]*)\\[\\]");
      Matcher m = arrayPattern.matcher(className);
      if (m.find())
      {
         String elementName = m.group(1);
         return "[L" + elementName + ";";
      }
      return null;
   }
}
