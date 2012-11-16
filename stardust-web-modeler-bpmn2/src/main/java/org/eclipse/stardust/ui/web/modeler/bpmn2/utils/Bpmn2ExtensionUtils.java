package org.eclipse.stardust.ui.web.modeler.bpmn2.utils;

import static java.util.Collections.singleton;
import static org.eclipse.stardust.common.CollectionUtils.isEmpty;
import static org.eclipse.stardust.common.CollectionUtils.newArrayList;
import static org.eclipse.stardust.common.CollectionUtils.newHashSet;
import static org.eclipse.stardust.common.StringUtils.isEmpty;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.bpmn2.BaseElement;
import org.eclipse.bpmn2.Bpmn2Factory;
import org.eclipse.bpmn2.Bpmn2Package;
import org.eclipse.bpmn2.ExtensionAttributeValue;
import org.eclipse.bpmn2.util.XmlExtendedMetadata;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.impl.EAttributeImpl;
import org.eclipse.emf.ecore.impl.EStructuralFeatureImpl;
import org.eclipse.emf.ecore.util.ExtendedMetaData;
import org.eclipse.emf.ecore.util.FeatureMap;
import org.eclipse.emf.ecore.xml.type.AnyType;
import org.eclipse.emf.ecore.xml.type.XMLTypeFactory;
import org.eclipse.emf.ecore.xml.type.XMLTypePackage;
import org.eclipse.emf.ecore.xml.type.impl.AnyTypeImpl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * @author Robert Sauer
 */
public class Bpmn2ExtensionUtils
{
   public static final String NS_URI_STARDUST = "http://www.eclipse.org/stardust";

   public static final String NS_PREFIX_STARDUST = "stardust";

   public static EObject getExtensionElement(BaseElement object, String tag)
   {
      ExtensionAttributeValue extensionAttributes = !object.getExtensionValues()
            .isEmpty() //
            ? object.getExtensionValues().get(0)
            : null;
      if (null != extensionAttributes)
      {
         for (FeatureMap.Entry extension : extensionAttributes.getValue())
         {
            if (isInFilter(extension.getEStructuralFeature(), tag))
            {
               Object extensionValue = extension.getValue();

               if (extensionValue instanceof EObject)
               {
                  return (EObject) extensionValue;
               }
            }
         }
      }

      return null;
   }

   /**
    * Returns extensions for a given object
    *
    * @param object
    * @return
    */
   public static List<Map<String, Object>> getExtensionAttributes(BaseElement object,
         String tag)
   {
      List<Map<String, Object>> extensions = newArrayList();

      ExtensionAttributeValue extensionAttributes = getOrCreate(
            ExtensionAttributeValue.class, object.getExtensionValues());
      if (null != extensionAttributes)
      {
         for (FeatureMap.Entry extension : extensionAttributes.getValue())
         {
            if (isInFilter(extension.getEStructuralFeature(), tag))
            {
               Map<String, Object> tagAttributes = null;
               Object extensionValue = extension.getValue();

               if (extensionValue instanceof BaseElement)
               {
                  BaseElement baseElement = (BaseElement) extensionValue;
                  tagAttributes = findExtensionAttributes(baseElement.getAnyAttribute(),
                        null);
               }
               else if (extensionValue instanceof AnyType)
               {
                  AnyType anyType = (AnyType) extensionValue;
                  tagAttributes = findExtensionAttributes(anyType.getAnyAttribute(), null);
               }
               else
               {
                  throw new IllegalArgumentException("Fail to parse extension value "
                        + extensionValue);
               }

               extensions.add(tagAttributes);
            }
            else
            {
               continue;
            }
         }
      }

      return extensions;
   }

   public static void setExtensionAttributes(BaseElement object,
         String tag, Map<String, ? extends Object> attributeValues)
   {
      Set<String> updatedAttribs = newHashSet();
      Set<String> obsoleteAttribs = newHashSet();

      ExtensionAttributeValue extensionAttributes = getOrCreate(
            ExtensionAttributeValue.class, object.getExtensionValues());
      if (null != extensionAttributes)
      {
         for (Iterator<FeatureMap.Entry> i = extensionAttributes.getValue().iterator(); i.hasNext(); )
         {
            FeatureMap.Entry extension = i.next();
            if (isInFilter(extension.getEStructuralFeature(), tag))
            {
               Map<String, Object> tagAttributes = null;
               Object extensionValue = extension.getValue();

               if (extensionValue instanceof BaseElement)
               {
                  BaseElement baseElement = (BaseElement) extensionValue;
                  tagAttributes = findAllExtensionAttributes(baseElement.getAnyAttribute());
               }
               else if (extensionValue instanceof AnyType)
               {
                  AnyType anyType = (AnyType) extensionValue;
                  tagAttributes = findAllExtensionAttributes(anyType.getAnyAttribute());
               }
               else
               {
                  throw new IllegalArgumentException("Fail to parse extension value "
                        + extensionValue);
               }

               for (Iterator<String> j = tagAttributes.keySet().iterator(); j.hasNext(); )
               {
                  String attribName = j.next();
                  if (attributeValues.containsKey(attribName))
                  {
                     updatedAttribs.add(attribName);
                     // TODO update value
                  }
                  else
                  {
                     obsoleteAttribs.add(attribName);
                     j.remove();
                  }
               }
               if (tagAttributes.isEmpty())
               {
                  i.remove();
               }
               if (extensionAttributes.getValue().isEmpty())
               {
                  // TODO remove extension elements container
               }
            }
            else
            {
               continue;
            }
         }
      }

      if ( !isEmpty(attributeValues.keySet()))
      {
         doSetExtensionAttributes(object, tag, attributeValues);
      }
   }

   public static void doSetExtensionAttributes(BaseElement object, String tag,
         Map<String, ? extends Object> newAttributes)
   {
      ExtendedMetaData metadata = XmlExtendedMetadata.INSTANCE;

      ExtensionAttributeValue extensionAttributes = getOrCreate(
            ExtensionAttributeValue.class, object.getExtensionValues());
      FeatureMap extensions = extensionAttributes.getValue();

      // create extension element type
      EStructuralFeature extensionElementType = metadata.demandFeature(NS_URI_STARDUST,
            tag, true, true);

      extensionElementType.setChangeable(true);

      AnyType extensionElement = (AnyTypeImpl) XMLTypeFactory.eINSTANCE.createAnyType();

      // add extension element to extensions
      extensions.add(extensionElementType, extensionElement);

      // create extension attributes
      for (Map.Entry<String, ?> attribute : newAttributes.entrySet())
      {
         String ns = null;

         String name = attribute.getKey();
         Object value = attribute.getValue();

         // name can be (namespace):name to assign a namespace to it
         Pattern pattern = Pattern.compile("^\\((.*)\\):(.*)$");
         Matcher matcher = pattern.matcher(name);

         // name can be (namespace):name to assign a namespace to it
         if (matcher.matches())
         {
            ns = matcher.group(1);
            name = matcher.group(2);
         }

         // single extension attribute
         EAttributeImpl extensionAttributeType = (EAttributeImpl) metadata.demandFeature(
               ns, name, false, false);

         // add it to extension element
         extensionElement.getAnyAttribute().add(
               new EStructuralFeatureImpl.SimpleFeatureMapEntry(extensionAttributeType,
                     value));
      }
   }

   public static String getExtensionAttribute(BaseElement element, String name)
   {
      Object value = null;
      for (Iterator<FeatureMap.Entry> i = element.getAnyAttribute().iterator(); i.hasNext(); )
      {
         FeatureMap.Entry extension = i.next();
         if (isInFilter(extension.getEStructuralFeature(), name))
         {
            value = extension.getValue();
            break;
         }
      }

      return (String) ((value instanceof String) ? value : null);
   }

   public static void setExtensionAttribute(BaseElement element, String name, String value)
   {
      EStructuralFeature attributeAccessor = null;
      Object oldValue = null;
      for (Iterator<FeatureMap.Entry> i = element.getAnyAttribute().iterator(); i.hasNext(); )
      {
         FeatureMap.Entry extension = i.next();
         if (isInFilter(extension.getEStructuralFeature(), name))
         {
            attributeAccessor = extension.getEStructuralFeature();
            oldValue = extension.getValue();
            break;
         }
      }

      if (null == attributeAccessor)
      {
         attributeAccessor = XmlExtendedMetadata.INSTANCE.demandFeature(NS_URI_STARDUST, name, false, false);
         attributeAccessor.setChangeable(true);
      }

      if (isEmpty(value))
      {
         if (null != oldValue)
         {
            element.getAnyAttribute().list(attributeAccessor).remove(oldValue);
         }
      }
      else
      {
         element.getAnyAttribute().set(attributeAccessor, value);
      }
   }

   public static String getExtensionClob(BaseElement object, String tag)
   {
      String clob = "";

      ExtensionAttributeValue extensionAttributes = !object.getExtensionValues()
            .isEmpty() //
            ? object.getExtensionValues().get(0)
            : null;
      if (null != extensionAttributes)
      {
         for (FeatureMap.Entry extension : extensionAttributes.getValue())
         {
            if (isInFilter(extension.getEStructuralFeature(), tag))
            {
               Object extensionValue = extension.getValue();

               if (extensionValue instanceof AnyType)
               {
                  AnyType anyType = (AnyType) extensionValue;
                  clob = getCDataString(anyType.getMixed());
                  break;
               }
               else
               {
                  throw new IllegalArgumentException("Fail to parse extension value "
                        + extensionValue);
               }
            }
         }
      }

      return clob;
   }

   public static void setExtensionClob(BaseElement object, String tag, String clob)
   {
      ExtendedMetaData metadata = XmlExtendedMetadata.INSTANCE;

      ExtensionAttributeValue extensionAttributes = getOrCreate(
            ExtensionAttributeValue.class, object.getExtensionValues());
      FeatureMap extensions = extensionAttributes.getValue();

      AnyType clobHolder = null;
      for (FeatureMap.Entry extension : extensionAttributes.getValue())
      {
         if (isInFilter(extension.getEStructuralFeature(), tag))
         {
            Object extensionValue = extension.getValue();

            if (extensionValue instanceof AnyType)
            {
               clobHolder = (AnyType) extensionValue;
               break;
            }
         }
      }

      // create extension element type
      EStructuralFeature extensionElementType = metadata.demandFeature(NS_URI_STARDUST,
            tag, true, true);
      extensionElementType.setChangeable(true);

      if (isEmpty(clob))
      {
         if (null != clobHolder)
         {
            extensions.list(extensionElementType).remove(clobHolder);
         }

         if (extensions.isEmpty())
         {
            object.getExtensionValues().remove(extensionAttributes);
         }
      }
      else
      {
         if (null == clobHolder)
         {
            clobHolder = (AnyTypeImpl) XMLTypeFactory.eINSTANCE.createAnyType();
            extensions.add(extensionElementType, clobHolder);
         }
         setCDataString(clobHolder.getMixed(), clob);
      }
   }

   public static JsonObject getExtensionAsJson(BaseElement object, String tag)
   {
      String clobValue = getExtensionClob(object, tag);

      JsonObject payload = null;
      if (null != clobValue)
      {
         JsonElement parsedClob = new JsonParser().parse(clobValue);
         if (parsedClob.isJsonObject())
         {
            payload = parsedClob.getAsJsonObject();
         }
         else
         {
            // TODO coerce values somehow?
         }
      }

      return (null != payload) ? payload : new JsonObject();
   }

   public static void setExtensionFromJson(BaseElement object, String tag, JsonObject payload)
   {
      String clobValue = null;
      if ((null != payload) && !payload.entrySet().isEmpty())
      {
         clobValue = payload.toString();
      }

      setExtensionClob(object, tag, clobValue);
   }

   protected static String getCDataString(FeatureMap featureMap)
   {
      String result = null;

      Collection<?> parts = (Collection<?>) featureMap.get(
            XMLTypePackage.eINSTANCE.getXMLTypeDocumentRoot_CDATA(), false);
      if (isEmpty(parts))
      {
         parts = (Collection<?>) featureMap.get(
               XMLTypePackage.eINSTANCE.getXMLTypeDocumentRoot_Text(), false);
      }
      if ( !isEmpty(parts))
      {
         if (parts.size() == 1)
         {
            result = parts.iterator().next().toString();
         }
         else
         {
            StringBuilder sb = new StringBuilder();
            for (Object o : parts)
            {
               sb.append(o.toString());
            }
            result = sb.toString();
         }
      }
      return result;
   }

   protected static void setCDataString(FeatureMap mixed, String description)
   {
      setCDataString(mixed, description, false);
   }

   protected static void setCDataString(FeatureMap mixed, String text, boolean normalizeCrLf)
   {
      if (normalizeCrLf && !isEmpty(text))
      {
         text.replace("\r\n", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
      }

      // clean up any pending previous non-CDATA content (i.e. from XPDL load)
      if (mixed.isSet(XMLTypePackage.eINSTANCE.getXMLTypeDocumentRoot_Text()))
      {
         mixed.unset(XMLTypePackage.eINSTANCE.getXMLTypeDocumentRoot_Text());
      }
      mixed.set(XMLTypePackage.eINSTANCE.getXMLTypeDocumentRoot_CDATA(), singleton(text));
   }

   private static Map<String, Object> findExtensionAttributes(FeatureMap featureMap,
         String tag)
   {
      Map<String, Object> attributes = new HashMap<String, Object>();

      for (FeatureMap.Entry attribute : featureMap)
      {
         if (isInFilter(attribute.getEStructuralFeature(), tag))
         {
            String name = attribute.getEStructuralFeature().getName();
            Object value = attribute.getValue();

            attributes.put(name, value);
         }
      }

      return attributes;
   }

   private static boolean isInFilter(EStructuralFeature eStructuralFeature, String tag)
   {
      String extensionNs = ExtendedMetaData.INSTANCE.getNamespace(eStructuralFeature);
      if (NS_URI_STARDUST.equals(extensionNs))
      {
         if ((null != tag) && tag.equals(eStructuralFeature.getName()))
         {
            return true;
         }
      }
      return false;
   }

   private static Map<String, Object> findAllExtensionAttributes(FeatureMap featureMap)
   {
      Map<String, Object> attributes = new HashMap<String, Object>();

      for (FeatureMap.Entry attribute : featureMap)
      {
         String name = attribute.getEStructuralFeature().getName();
         Object value = attribute.getValue();

         attributes.put(name, value);
      }

      return attributes;
   }

   private static <T extends EObject> T getOrCreate(Class<T> cls, List<T> elements)
   {
      if (elements.isEmpty())
      {
         EClass ecls = classToEClassMap.get(cls);
         if (ecls == null)
         {
            ecls = scanForEClass(cls, Bpmn2Package.eINSTANCE.getEClassifiers());
            if (ecls == null)
            {
               return null;
            }
         }

         @SuppressWarnings("unchecked")
         T element = (T) Bpmn2Factory.eINSTANCE.create(ecls);

         elements.add(element);
      }

      return elements.get(0);
   }

   private static EClass scanForEClass(Class<?> cls, List<EClassifier> classifiers)
   {
      String clsName = cls.getSimpleName();
      for (EClassifier classifier : classifiers)
      {
         if (cls.getName().equals(classifier.getInstanceClassName())
               || clsName.equals(classifier.getName()) && (classifier instanceof EClass))
         {
            return (EClass) classifier;
         }
      }

      return null;
   }

   private static final Map<Class<?>, EClass> classToEClassMap = new HashMap<Class<?>, EClass>();
}