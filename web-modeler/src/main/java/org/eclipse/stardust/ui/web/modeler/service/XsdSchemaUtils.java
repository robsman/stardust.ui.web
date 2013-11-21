/*******************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.modeler.service;

import static org.eclipse.stardust.common.CollectionUtils.newHashMap;
import static org.eclipse.stardust.common.StringUtils.isEmpty;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.hasNotJsonNull;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.toPrettyString;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.xsd.XSDAnnotation;
import org.eclipse.xsd.XSDAttributeDeclaration;
import org.eclipse.xsd.XSDAttributeGroupContent;
import org.eclipse.xsd.XSDAttributeGroupDefinition;
import org.eclipse.xsd.XSDAttributeUse;
import org.eclipse.xsd.XSDAttributeUseCategory;
import org.eclipse.xsd.XSDComplexTypeContent;
import org.eclipse.xsd.XSDComplexTypeDefinition;
import org.eclipse.xsd.XSDComponent;
import org.eclipse.xsd.XSDCompositor;
import org.eclipse.xsd.XSDConstrainingFacet;
import org.eclipse.xsd.XSDDerivationMethod;
import org.eclipse.xsd.XSDElementDeclaration;
import org.eclipse.xsd.XSDEnumerationFacet;
import org.eclipse.xsd.XSDFactory;
import org.eclipse.xsd.XSDImport;
import org.eclipse.xsd.XSDModelGroup;
import org.eclipse.xsd.XSDNamedComponent;
import org.eclipse.xsd.XSDParticle;
import org.eclipse.xsd.XSDPatternFacet;
import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.XSDSchemaContent;
import org.eclipse.xsd.XSDSimpleTypeDefinition;
import org.eclipse.xsd.XSDTerm;
import org.eclipse.xsd.XSDTypeDefinition;
import org.eclipse.xsd.XSDVariety;
import org.eclipse.xsd.XSDWildcard;
import org.eclipse.xsd.impl.XSDSchemaImpl;
import org.eclipse.xsd.util.XSDSwitch;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import org.eclipse.stardust.common.Predicate;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.core.struct.StructuredDataConstants;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelBuilderFacade;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.model.xpdl.carnot.util.ModelUtils;
import org.eclipse.stardust.model.xpdl.xpdl2.ExternalPackage;
import org.eclipse.stardust.model.xpdl.xpdl2.ExternalPackages;
import org.eclipse.stardust.model.xpdl.xpdl2.SchemaTypeType;
import org.eclipse.stardust.model.xpdl.xpdl2.TypeDeclarationType;
import org.eclipse.stardust.model.xpdl.xpdl2.TypeDeclarationsType;
import org.eclipse.stardust.model.xpdl.xpdl2.util.TypeDeclarationUtils;
import org.eclipse.stardust.model.xpdl.xpdl2.util.XSDElementCheckForType;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils;

public final class XsdSchemaUtils
{
   private static final Logger trace = LogManager.getLogger(XsdSchemaUtils.class);

   private static final String NS_URI = "http://www.carnot.ag/workflowmodel/3.1/struct";

   private static final String XMLNS_CARNOT = XMLConstants.XMLNS_ATTRIBUTE + ":carnot";
   private static final String PREFIX = "carnot:";

   private static final String LOCATIONS = "locations";

   public static void main(String[] args)
   {
      try
      {
         URL location = XsdSchemaUtils.class.getResource("TestSchema.xsd");
         XSDSchema schema = ModelService.loadSchema(location.toString());
         /*
         System.out.println(toPrettyString(toSchemaJson(schema, "ExtendedRGB")));
         System.out.println(toPrettyString(toSchemaJson(schema, "ExtendedColor")));
         System.out.println(toPrettyString(toSchemaJson(schema, "Material")));
         System.out.println(toPrettyString(toSchemaJson(schema, "employee")));*/

         System.out.println(toPrettyString(toSchemaJson(schema, "RGB")));
         System.out.println(toPrettyString(toSchemaJson(schema, "ARGB")));
         System.out.println(toPrettyString(toSchemaJson(schema, "X1")));
         System.out.println(toPrettyString(toSchemaJson(schema, "SuperColor")));
         System.out.println(toPrettyString(toSchemaJson(schema, "Color")));
      }
      catch (IOException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }

   private static Predicate<String> NON_EMPTY_STRINGS = new Predicate<String>()
   {
      @Override
      public boolean accept(String value)
      {
         return value != null && !value.isEmpty();
      }
   };

   public static JsonObject toSchemaJson(XSDSchema schema, String componentId)
   {
      JsonObject json = new Xsd2Json(null/*componentId*/).doSwitch(schema);
      if (trace.isDebugEnabled())
      {
         trace.debug(toPrettyString(json));
      }
      return json;
   }

   public static JsonObject toSchemaJson(XSDSchema schema)
   {
      JsonObject json = new Xsd2Json(null).doSwitch(schema);
      if (trace.isDebugEnabled())
      {
         trace.debug(toPrettyString(json));
      }
      return json;
   }

   public static XSDNamedComponent findNamedComponent(XSDSchema schema, String componentId)
   {
      String tns = normalizeNamespace(schema.getTargetNamespace());

      if (componentId.startsWith("{"))
      {
         QName typeQName = QName.valueOf(componentId);
         componentId = typeQName.getLocalPart();
         String namespaceURI = typeQName.getNamespaceURI();
         if(!StringUtils.isEmpty(namespaceURI))
         {
            tns = namespaceURI;
         }
      }

      // try to find exact element match
      List<XSDElementDeclaration> decls = schema.getElementDeclarations();
      for (XSDElementDeclaration decl : decls)
      {
         if (componentId.equals(decl.getName()) && tns.equals(normalizeNamespace(decl.getTargetNamespace())))
         {
            return decl;
         }
      }

      // try to find exact type match
      List<XSDTypeDefinition> defs = schema.getTypeDefinitions();
      for (XSDTypeDefinition typedef : defs)
      {
         if (componentId.equals(typedef.getName()) && tns.equals(normalizeNamespace(typedef.getTargetNamespace())))
         {
            return typedef;
         }
      }

      // oops, no exact match, try to ignore namespace now
      for (XSDElementDeclaration decl : decls)
      {
         if (componentId.equals(decl.getName()))
         {
            return decl;
         }
      }
      // and the same for types
      for (XSDTypeDefinition typedef : defs)
      {
         if (componentId.equals(typedef.getName()) && tns.equals(normalizeNamespace(typedef.getTargetNamespace())))
         {
            return typedef;
         }
      }

      // this shouldn't happen, but it's not excluded from spec

      // return first element declaration, if any.
      if (!decls.isEmpty())
      {
         return decls.get(0);
      }

      // return first type definition, if any.
      if (!defs.isEmpty())
      {
         return defs.get(0);
      }

      // this line should never be reached.
      return null;
   }

   private static String normalizeNamespace(String targetNamespace)
   {
      return targetNamespace == null ? XMLConstants.NULL_NS_URI : targetNamespace;
   }

   private static String getCardinality(XSDTerm term)
   {
      if (term.eContainer() instanceof XSDParticle)
      {
         XSDParticle particle = (XSDParticle) term.eContainer();
         int minOccurs = particle.getMinOccurs();
         int maxOccurs = particle.getMaxOccurs();
         return maxOccurs == XSDParticle.UNBOUNDED || maxOccurs > 1
               ? minOccurs == 0 ? "many" : "atLeastOne"
               : minOccurs == 0 ? "optional" : "required";
      }
      return null;
   }

   private static class Xsd2Json extends XSDSwitch<JsonObject>
   {
      private String componentId;

      private XSDSchema schema;
      private Stack<XSDComponent> visited;
      private boolean includeIcon;

      private JsonObject nsMappings;
      private Map<String, String> prefixes;

      public Xsd2Json(String componentId)
      {
         this.componentId = componentId;
      }

      @Override
      public JsonObject caseXSDSchema(XSDSchema schema)
      {
         this.schema = schema;
         visited = new Stack<XSDComponent>();

         Predicate<XSDComponent> filter = null;
         List<XSDElementDeclaration> elements = null;
         List<XSDTypeDefinition> types = null;

         if (componentId == null)
         {
            final String tns = normalizeNamespace(schema.getTargetNamespace());
            filter = new Predicate<XSDComponent>()
            {
               @Override
               public boolean accept(XSDComponent component)
               {
                  XSDSchema schema = component.getSchema();
                  return tns.equals(schema == null ? XMLConstants.NULL_NS_URI
                        : normalizeNamespace(schema.getTargetNamespace()));
               }
            };

            if (!schema.getElementDeclarations().isEmpty())
            {
               elements = schema.getElementDeclarations();
            }
            if (!schema.getTypeDefinitions().isEmpty())
            {
               types = schema.getTypeDefinitions();
            }
         }
         else
         {
            XSDNamedComponent component = findNamedComponent(schema, componentId);
            if (component instanceof XSDElementDeclaration)
            {
               elements = Collections.singletonList((XSDElementDeclaration) component);
            }
            if (component instanceof XSDTypeDefinition)
            {
               types = Collections.singletonList((XSDTypeDefinition) component);
            }
         }

         JsonObject json = new JsonObject();
         json.addProperty("targetNamespace", normalizeNamespace(schema.getTargetNamespace()));
         if (includeIcon)
         {
            json.addProperty("icon", XsdIcon.Schema.getSimpleName());
         }

         prefixes = new HashMap<String, String>();
         nsMappings = new JsonObject();
         for (Map.Entry<String, String> entry : schema.getQNamePrefixToNamespaceMap().entrySet())
         {
            String prefix = entry.getKey();
            if (NON_EMPTY_STRINGS.accept(prefix))
            {
               String namespace = entry.getValue();
               nsMappings.addProperty(prefix, namespace);
               prefixes.put(namespace, prefix);
            }
         }
         json.add("nsMappings", nsMappings);

         JsonObject locations = new JsonObject();
         for (XSDSchemaContent item : schema.getContents())
         {
            if (item instanceof XSDImport)
            {
               XSDImport xsdImport = (XSDImport) item;
               String location = xsdImport.getSchemaLocation();
               if (location.startsWith(StructuredDataConstants.URN_INTERNAL_PREFIX))
               {
                  location = location.substring(StructuredDataConstants.URN_INTERNAL_PREFIX.length());
                  locations.addProperty(xsdImport.getNamespace(), location);
               }
            }
         }
         if (!locations.entrySet().isEmpty())
         {
            json.add(LOCATIONS, locations);
         }

         if (elements != null && !elements.isEmpty())
         {
            JsonArray jsonArray = new JsonArray();
            for (XSDElementDeclaration component : elements)
            {
               if (filter == null || filter.accept(component))
               {
                  jsonArray.add(doSwitch(component));
               }
            }
            json.add("elements", jsonArray);
         }
         if (types != null && !types.isEmpty())
         {
            JsonArray jsonArray = new JsonArray();
            for (XSDTypeDefinition component : types)
            {
               if (filter == null || filter.accept(component))
               {
                  jsonArray.add(doSwitch(component));
               }
            }
            json.add("types", jsonArray);
         }

         return json;
      }

      @Override
      public JsonObject caseXSDElementDeclaration(XSDElementDeclaration element)
      {
         if (element.isElementDeclarationReference())
         {
            return doSwitch(element.getResolvedElementDeclaration());
         }

         XSDTypeDefinition type = element.getTypeDefinition();

         // elements are constructed similar with types
         JsonObject json = new JsonObject();
         if (type == element.getAnonymousTypeDefinition()
               || type instanceof XSDSimpleTypeDefinition
               || (type instanceof XSDComplexTypeDefinition && element.eContainer() instanceof XSDSchema))
         {
            json = doSwitch(type);
         }

         // now overwrite properties
         json.addProperty("name", element.getName());
         if (includeIcon)
         {
            json.addProperty("icon", XsdIcon.ElementDeclaration.getSimpleName());
         }
         json.addProperty("classifier", "element");

         // now overwrite element specific properties
         String cardinality = getCardinality(element);
         if (cardinality != null)
         {
            json.addProperty("cardinality", cardinality);
         }

         if (type != element.getAnonymousTypeDefinition())
         {
            json.addProperty("type", getPrefixedName(type));
         }

         addAnnotations(json, element.getAnnotation());

         return json;
      }

      private void addAnnotations(JsonObject json, XSDAnnotation annotation)
      {
         if (annotation != null)
         {
            JsonObject jsObject = new JsonObject();
            for (Element appInfo : annotation.getApplicationInformation())
            {
               NodeList children = appInfo.getChildNodes();
               for (int i = 0, l = children.getLength(); i < l; i++)
               {
                  Node node = children.item(i);
                  if (node instanceof Element && NS_URI.equals(node.getNamespaceURI()))
                  {
                     jsObject.add(node.getLocalName(), toJson((Element) node));
                  }
               }
            }
            json.add("appinfo", jsObject);
         }
      }

      private JsonElement toJson(Element element)
      {
         JsonObject jsObject = new JsonObject();
         NodeList children = element.getChildNodes();
         for (int i = 0, l = children.getLength(); i < l; i++)
         {
            Node node = children.item(i);
            if (node instanceof Element && NS_URI.equals(node.getNamespaceURI()))
            {
               jsObject.add(node.getLocalName(), toJson((Element) node));
            }
         }
         return jsObject.entrySet().isEmpty() ? getValue(element) : jsObject;
      }

      private JsonPrimitive getValue(Element element)
      {
         StringBuilder sb = new StringBuilder();
         NodeList children = element.getChildNodes();
         for (int i = 0, l = children.getLength(); i < l; i++)
         {
            Node node = children.item(i);
            if (node instanceof Text)
            {
               sb.append(((Text) node).getData());
            }
         }
         return new JsonPrimitive(sb.toString());
      }

      @Override
      public JsonObject caseXSDComplexTypeDefinition(XSDComplexTypeDefinition type)
      {
         JsonObject json = caseXSDTypeDefinition(type);
         if (includeIcon)
         {
            json.addProperty("icon", XsdIcon.ComplexTypeDefinition.getSimpleName());
         }
         json.addProperty("classifier", "complexType");

         // prohibit recursion
         if (!visited.contains(type))
         {
            visited.push(type);
            try
            {
               // don't go into predefined types
               if (!XMLResource.XML_SCHEMA_URI.equals(type.getTargetNamespace()))
               {
                  if (type.isSetDerivationMethod())
                  {
                     if(type.getBaseType() != null)
                     {
                        json.addProperty("base", getPrefixedName(type.getBaseType()));
                     }
                     if(type.getDerivationMethod() != null)
                     {
                        json.addProperty("method", type.getDerivationMethod().getName());
                     }
                  }

                  XSDComplexTypeContent content = type.getContent();
                  XSDParticle particle = type.getComplexType();
                  if (particle != null)
                  {
                     List<XSDParticle> particles = Collections.singletonList(particle);
                     XSDModelGroup pt = (XSDModelGroup) particle.getTerm();
                     if (pt.getContents().isEmpty()) // unwrap content
                     {
                        XSDTerm term = particle.getTerm();
                        if (term instanceof XSDModelGroup)
                        {
                           XSDModelGroup group = (XSDModelGroup) term;
                           List<XSDParticle> groupParticles = group.getParticles();
                           if (!groupParticles.isEmpty())
                           {
                              particles = groupParticles;
                           }
                        }
                        else
                        {
                           // this should never happen
                           System.err.println("Expected XSDModelGroup but found: " + term);
                        }
                     }
                     if (!particles.contains(content) && content instanceof XSDParticle)
                     {
                        particles = new ArrayList<XSDParticle>(particles);
                        particles.add((XSDParticle) content);
                     }
                     addBody(particles, json, content, true);
                  }

                  addFacets(type, json);
                  addAttributes(type, json);
               }
            }
            finally
            {
               visited.pop();
            }
         }

         return json;
      }

      private void addFacets(XSDTypeDefinition type, JsonObject json)
      {
         XSDSimpleTypeDefinition simple = type.getSimpleType();
         if (simple != null)
         {
            XSDSimpleTypeDefinition primitiveType = simple.getPrimitiveTypeDefinition();
            if (primitiveType != null)
            {
               json.addProperty("primitiveType", getPrefixedName(primitiveType));
            }
            JsonArray jsonArray = new JsonArray();
            XSDTypeDefinition base = type.getBaseType();
            XSDSimpleTypeDefinition simpleBase = base != null ? base.getSimpleType() : null;
            List<XSDConstrainingFacet> inheritedFacets = simpleBase == null
                  ? Collections.<XSDConstrainingFacet>emptyList()
                  : simpleBase.getFacetContents();
            for (XSDConstrainingFacet facet : simple.getFacetContents())
            {
               JsonObject facetJson = doSwitch(facet);
               if (inheritedFacets.contains(facet))
               {
                  facetJson.addProperty("inherited", Boolean.TRUE);
               }
               jsonArray.add(facetJson);
            }
            if (jsonArray.size() > 0)
            {
               json.add("facets", jsonArray);
            }
         }
      }

      private void addAttributes(XSDComplexTypeDefinition type, JsonObject json)
      {
         List<XSDAttributeUse> attributeUses = type.getAttributeUses();
         List<XSDAttributeGroupContent> attributes = type.getAttributeContents();
         JsonArray jsonArray = new JsonArray();
         for (XSDAttributeUse attribute : attributeUses)
         {
            JsonObject attributeJson = doSwitch(attribute);
            if (!containsAttribute(attributes, attribute))
            {
               attributeJson.addProperty("inherited", Boolean.TRUE);
            }
            jsonArray.add(attributeJson);
         }
         XSDWildcard wildcard = type.getAttributeWildcard();
         if (wildcard != null)
         {
            JsonObject wildcardJson = doSwitch(wildcard);
            if (wildcard != type.getAttributeWildcardContent())
            {
               wildcardJson.addProperty("inherited", Boolean.TRUE);
            }
            jsonArray.add(wildcardJson);
         }
         if (jsonArray.size() > 0)
         {
            json.add("attributes", jsonArray);
         }
      }

      private boolean containsAttribute(List<XSDAttributeGroupContent> attributes, XSDAttributeUse target)
      {
         for (XSDAttributeGroupContent attribute : attributes)
         {
            if (attribute instanceof XSDAttributeGroupDefinition)
            {
               XSDAttributeGroupDefinition group = (XSDAttributeGroupDefinition) attribute;
               List<XSDAttributeUse> uses = group.getAttributeUses();
               for (XSDAttributeUse use : uses)
               {
                  if (use.getAttributeDeclaration() == target.getAttributeDeclaration())
                  {
                     return true;
                  }
               }
            }
            else if (attribute instanceof XSDAttributeUse)
            {
               XSDAttributeUse use = (XSDAttributeUse) attribute;
               if (use.getAttributeDeclaration() == target.getAttributeDeclaration())
               {
                  return true;
               }
            }
         }
         return false;
      }

      @Override
      public JsonObject caseXSDSimpleTypeDefinition(XSDSimpleTypeDefinition type)
      {
         JsonObject json = caseXSDTypeDefinition(type);
         if (includeIcon)
         {
            json.addProperty("icon", XsdIcon.SimpleTypeDefinition.getSimpleName());
         }
         json.addProperty("classifier", "simpleType");

         // don't go into predefined types
         if (!XMLResource.XML_SCHEMA_URI.equals(type.getTargetNamespace()))
         {
            if (type.isSetVariety())
            {
               XSDVariety variety = type.getVariety();
               if(type.getBaseType() != null)
               {
                  json.addProperty("base", getPrefixedName(type.getBaseType()));
               }
               json.addProperty("method", variety == XSDVariety.ATOMIC_LITERAL ? "restriction" : variety.name());
               switch (variety)
               {
               case LIST_LITERAL:
                  // handle lists;
                  break;
               case UNION_LITERAL:
                  // handle unions;
                  break;
               default:
               }
            }
            addFacets(type, json);
         }
         return json;
      }

      @Override
      public JsonObject caseXSDTypeDefinition(XSDTypeDefinition type)
      {
         JsonObject json = new JsonObject();
         json.addProperty("name", type.getName());
         return json;
      }

      @Override
      public JsonObject caseXSDEnumerationFacet(XSDEnumerationFacet facet)
      {
         JsonObject json = caseXSDConstrainingFacet(facet);
         if (includeIcon)
         {
            json.addProperty("icon", XsdIcon.EnumerationFacet.getSimpleName());
         }
         return json;
      }

      @Override
      public JsonObject caseXSDPatternFacet(XSDPatternFacet facet)
      {
         JsonObject json = caseXSDConstrainingFacet(facet);
         if (includeIcon)
         {
            json.addProperty("icon", XsdIcon.PatternFacet.getSimpleName());
         }
         return json;
      }

      @Override
      public JsonObject caseXSDConstrainingFacet(XSDConstrainingFacet facet)
      {
         JsonObject json = new JsonObject();
         json.addProperty("name", facet.getLexicalValue());
         json.addProperty("classifier", facet.getFacetName());
         return json;
      }

      @Override
      public JsonObject caseXSDParticle(XSDParticle particle)
      {
         return doSwitch(particle.getTerm());
      }

      @Override
      public JsonObject caseXSDWildcard(XSDWildcard wildcard)
      {
         JsonObject json = new JsonObject();
         json.addProperty("name", "<any>");
         if (includeIcon)
         {
            json.addProperty("icon", XsdIcon.WildcardElement.getSimpleName());
         }
         json.addProperty("cardinality", getCardinality(wildcard));
         return json;
      }

      @Override
      public JsonObject caseXSDModelGroup(XSDModelGroup group)
      {
         JsonObject json = new JsonObject();

         XSDCompositor compositor = group.getCompositor();
         json.addProperty("name", "<" + compositor.getName() + ">");
         if (includeIcon)
         {
            switch (compositor)
            {
            case ALL_LITERAL:
               json.addProperty("icon", XsdIcon.ModelGroupAll.getSimpleName());
               break;
            case CHOICE_LITERAL:
               json.addProperty("icon", XsdIcon.ModelGroupChoice.getSimpleName());
               break;
            case SEQUENCE_LITERAL:
               json.addProperty("icon", XsdIcon.ModelGroupSequence.getSimpleName());
               break;
            default:
               json.addProperty("icon", XsdIcon.ModelGroupUnresolved.getSimpleName());
               break;
            }
         }
         json.addProperty("classifier", compositor.getName());
         json.addProperty("cardinality", getCardinality(group));

         addBody(group.getParticles(), json, null, false);

         return json;
      }

      private void addBody(List<XSDParticle> list, JsonObject json, XSDComplexTypeContent content, boolean markInherited)
      {
         JsonArray jsonArray = new JsonArray();
         for (XSDParticle particle : list)
         {
            JsonObject particleJson = doSwitch(particle);
            if (markInherited && particle != content)
            {
               particleJson.addProperty("inherited", Boolean.TRUE);
            }
            jsonArray.add(particleJson);
         }
         if (jsonArray.size() > 0)
         {
            json.add("body", jsonArray);
         }
      }

      @Override
      public JsonObject caseXSDAttributeUse(XSDAttributeUse attribute)
      {
         JsonObject json = caseXSDAttributeDeclaration(attribute.getAttributeDeclaration());
         XSDAttributeUseCategory use = attribute.getUse();
         if (use != null)
         {
            json.addProperty("cardinality", use.getName());
         }
         return json;
      }

      @Override
      public JsonObject caseXSDAttributeDeclaration(XSDAttributeDeclaration attribute)
      {
         XSDTypeDefinition type = attribute.getTypeDefinition();

         // elements are constructed similar with types
         if (type != null)
         {
            JsonObject json = doSwitch(type);
   
            // now overwrite properties
            json.addProperty("name", attribute.getName());
            if (includeIcon)
            {
               json.addProperty("icon", XsdIcon.AttributeDeclaration.getSimpleName());
            }
            json.addProperty("classifier", "attribute");
   
            if (type != attribute.getAnonymousTypeDefinition())
            {
               json.addProperty("type", getPrefixedName(type));
            }
   
            addAnnotations(json, attribute.getAnnotation());
   
            return json;
         }
         return null;
      }

      private String getPrefixedName(XSDNamedComponent type)
      {

         String qName = type.getQName(schema);
         String tns = type.getTargetNamespace();
         if (!StringUtils.isEmpty(tns) && qName.indexOf(':') < 0)
         {
            String prefix = prefixes.get(tns);
            if (prefix == null)
            {
               XSDSchema otherSchema = type.getSchema();
               String q1 = type.getQName(otherSchema);
               int ix = q1.indexOf(':');
               if (ix <= 0)
               {
                  prefix = "px";
               }
               else
               {
                  prefix = q1.substring(0, ix);
               }
               String base = prefix + "_";
               int counter = 0;
               while (nsMappings.has(prefix))
               {
                  counter++;
                  prefix = base + counter;
               }
               prefixes.put(tns, prefix);
               nsMappings.addProperty(prefix, tns);
            }
            qName = prefix + ":" + qName;
         }
         return qName;
      }
   }

   public static void updateXSDSchemaType(ModelBuilderFacade facade, SchemaTypeType schemaType, JsonObject schemaJson)
   {
      if (trace.isDebugEnabled())
      {
         trace.debug(toPrettyString(schemaJson));
      }

      XSDSchema schema = schemaType.getSchema();

      if (schemaJson.has("targetNamespace"))
      {
         schema.setTargetNamespace(GsonUtils.safeGetAsString(schemaJson, "targetNamespace"));
      }

      JsonObject locations = GsonUtils.safeGetAsJsonObject(schemaJson, LOCATIONS);
      if (locations != null)
      {
         updateImports(facade, schema, locations);
      }

      if (schemaJson.has("types"))
      {
         updateXSDTypeDefinitions(schema, GsonUtils.safeGetAsJsonArray(schemaJson, "types"), locations);
      }

      if (schemaJson.has("elements"))
      {
         updateElementDeclarations(schema, GsonUtils.safeGetAsJsonArray(schemaJson, "elements"), locations);
      }
   }

   private static void updateImports(ModelBuilderFacade facade, XSDSchema schema, JsonObject json)
   {
      ModelType model = ModelUtils.findContainingModel(schema);
      for (Map.Entry<String, JsonElement> entry : json.entrySet())
      {
         JsonElement element = entry.getValue();
         if (element.isJsonPrimitive())
         {
            updateReferences(facade, model, element.getAsString());
         }
      }
   }

   private static void updateReferences(ModelBuilderFacade facade, ModelType model, String rawLocation)
   {
      QName qname = QName.valueOf(rawLocation);
      String namespace = qname.getNamespaceURI();
      if (XMLConstants.NULL_NS_URI == namespace || namespace.equals(model.getId()))
      {
         return;
      }
      ModelType ref = facade.findModel(namespace);
      if (ref != null)
      {
         facade.updateReferences(model, ref);
      }
   }

   public static void updateElementDeclarations(XSDSchema schema, JsonArray json, JsonObject locations)
   {
      // (fh) create a map for easy retrieval of elements by name
      Map<String, XSDElementDeclaration> elementsIndex = newHashMap();
      for (XSDElementDeclaration element : schema.getElementDeclarations())
      {
         if (element.getSchema() == schema)
         {
            elementsIndex.put(element.getName(), element);
         }
      }

      for (JsonElement entry : json)
      {
         if (entry instanceof JsonObject)
         {
            JsonObject defJson = (JsonObject) entry;
            String typeName = GsonUtils.safeGetAsString(defJson, ModelerConstants.NAME_PROPERTY);
            XSDElementDeclaration element = elementsIndex.get(typeName);
            if (defJson.has("type"))
            {
               String typeRef = GsonUtils.safeGetAsString(defJson, "type");
               int ix = typeRef.indexOf(":");
               if (ix >= 0)
               {
                  typeRef = typeRef.substring(ix + 1);
               }
               XSDTypeDefinition type = element.getTypeDefinition();
               XSDTypeDefinition updatedType = updateTypeDefinition(schema, defJson, typeRef, type, locations);
               if (updatedType != type)
               {
                  element.setTypeDefinition(updatedType);
               }
            }
            else
            {
               XSDTypeDefinition type = element.getAnonymousTypeDefinition();
               XSDTypeDefinition updatedType = updateTypeDefinition(schema, defJson, null, type, locations);
               if (updatedType != type)
               {
                  element.setAnonymousTypeDefinition(updatedType);
               }
            }
         }
         else
         {
            trace.warn("Expected object, but received " + entry);
         }
      }
   }

   public static void updateXSDTypeDefinitions(XSDSchema schema, JsonArray json, JsonObject locations)
   {
      //Set<XSDTypeDefinition> updatedTypes = newHashSet();

      // (fh) create a map for easy retrieval of types by name
      Map<String, XSDTypeDefinition> typesIndex = newHashMap();
      for (XSDTypeDefinition type : schema.getTypeDefinitions())
      {
         if (type.getSchema() == schema)
         {
            typesIndex.put(type.getName(), type);
         }
      }

      for (JsonElement entry : json)
      {
         if (entry instanceof JsonObject)
         {
            JsonObject defJson = (JsonObject) entry;
            String typeName = GsonUtils.safeGetAsString(defJson, ModelerConstants.NAME_PROPERTY);
            XSDTypeDefinition def = typesIndex.get(typeName);
            typesIndex.remove(typeName);
            typesIndex.put(typeName, updateTypeDefinition(schema, defJson, typeName, def, locations));

            //updatedTypes.add(def);
         }
         else
         {
            trace.warn("Expected object, but received " + entry);
         }
      }

      // TODO: (fh) review !!! we might remove too much here
      // remove types not present in JSON anymore
      /*for (Iterator<XSDTypeDefinition> i = schema.getTypeDefinitions().iterator(); i.hasNext();)
      {
         XSDTypeDefinition typeDefinition = i.next();
         if (!updatedTypes.contains(typeDefinition))
         {
            i.remove();
         }
      }*/
   }

   private static XSDTypeDefinition updateTypeDefinition(XSDSchema schema, JsonObject defJson, String typeName,
         XSDTypeDefinition def, JsonObject locations)
   {
      boolean isComplexType = defJson.has("body") || defJson.has("attributes");

      int contentsIdx = schema.getContents().size();
      int typeIdx = schema.getTypeDefinitions().size();
      if ((isComplexType && (def instanceof XSDSimpleTypeDefinition))
            || ( !isComplexType && (def instanceof XSDComplexTypeDefinition)))
      {
         // coerce between complex/simple type (insert as same position as before)
         contentsIdx = schema.getContents().indexOf(def);
         if (contentsIdx >= 0)
         {
            typeIdx = schema.getTypeDefinitions().indexOf(def);
            schema.getContents().remove(contentsIdx);
         }
         def = null;
      }

      if (def == null)
      {
         def = isComplexType
               ? XSDFactory.eINSTANCE.createXSDComplexTypeDefinition()
               : XSDFactory.eINSTANCE.createXSDSimpleTypeDefinition();
         if (contentsIdx >= 0)
         {
            schema.getContents().add(contentsIdx, def);
            schema.getTypeDefinitions().move(typeIdx, def);
         }
         if (typeName != null)
         {
            def.setName(typeName);
         }
      }

      if (isComplexType)
      {
         updateXSDComplexTypeDefinition((XSDComplexTypeDefinition) def, defJson, locations);
      }
      else
      {
         updateXSDSimpleTypeDefinition((XSDSimpleTypeDefinition) def, defJson, locations);
      }
      return def;
   }

   /**
    *
    * @param def
    * @param simpleTypeJson
    */
   private static void updateXSDSimpleTypeDefinition(XSDSimpleTypeDefinition def,
         JsonObject simpleTypeJson, JsonObject locations)
   {
      List<XSDConstrainingFacet> facets = def.getFacetContents();

      if (simpleTypeJson.has("base"))
      {
         String baseTypeName = GsonUtils.safeGetAsString(simpleTypeJson, "base");
         XSDTypeDefinition base = resolveType(def, baseTypeName, locations);
         if (base instanceof XSDSimpleTypeDefinition)
         {
            def.setBaseTypeDefinition((XSDSimpleTypeDefinition) base);
         }
      }

      facets.clear();

      JsonElement minLengthJson = simpleTypeJson.get("minLength");
      if ((null != minLengthJson) && minLengthJson.isJsonPrimitive()
            && minLengthJson.getAsJsonPrimitive().isNumber())
      {
         XSDConstrainingFacet minLengthFacet = SupportedXSDConstrainingFacets.minLength.create();
         minLengthFacet.setLexicalValue(minLengthJson.getAsJsonPrimitive().getAsString());
         facets.add(minLengthFacet);
      }
      JsonElement maxLengthJson = simpleTypeJson.get("maxLength");
      if ((null != maxLengthJson) && maxLengthJson.isJsonPrimitive()
            && maxLengthJson.getAsJsonPrimitive().isNumber())
      {
         XSDConstrainingFacet maxLengthFacet = SupportedXSDConstrainingFacets.maxLength.create();
         maxLengthFacet.setLexicalValue(maxLengthJson.getAsJsonPrimitive().getAsString());
         facets.add(maxLengthFacet);
      }

      if (hasNotJsonNull(simpleTypeJson, "facets"))
      {
         JsonArray facetsJson = simpleTypeJson.getAsJsonArray("facets");
         for (JsonElement entry : facetsJson)
         {
            if ( !(entry instanceof JsonObject))
            {
               trace.warn("Expected object, but received " + entry);
               continue;
            }
            JsonObject facetJson = (JsonObject) entry;
            String classifier = facetJson.getAsJsonPrimitive("classifier").getAsString();
            XSDConstrainingFacet facet = SupportedXSDConstrainingFacets.valueOf(
                  classifier).create();
            facet.setLexicalValue(facetJson.getAsJsonPrimitive("name").getAsString());
            facets.add(facet);
         }
      }
   }

   private static enum SupportedXSDConstrainingFacets
   {
      // (fh) Only added what is supported by the eclipse modeler. Should be all of them.
      enumeration, pattern, maxLength, minLength;

      XSDConstrainingFacet create()
      {
         switch (this)
         {
         case enumeration:
            return XSDFactory.eINSTANCE.createXSDEnumerationFacet();
         case pattern:
            return XSDFactory.eINSTANCE.createXSDPatternFacet();
         case maxLength:
            return XSDFactory.eINSTANCE.createXSDMaxLengthFacet();
         case minLength:
            return XSDFactory.eINSTANCE.createXSDMinLengthFacet();
         }
         return null; // (fh) unreachable
      }
   }

   /**
    *
    * @param def
    * @param json
    */
   private static void updateXSDComplexTypeDefinition(XSDComplexTypeDefinition def,
         JsonObject json, JsonObject locations)
   {
      if (json.has("base"))
      {
         String rawBase = GsonUtils.safeGetAsString(json, "base");
         if (rawBase == null)
         {
            XSDTypeDefinition baseTypeDefinition = def.getBaseTypeDefinition();
            def.setBaseTypeDefinition(null);
            if (baseTypeDefinition != null)
            {
               if (!XSDElementCheckForType.needsType(def, baseTypeDefinition))
               {
                  XSDImport xsdImport = TypeDeclarationUtils.removeImport(def.getSchema(), baseTypeDefinition.getSchema());
                  if (xsdImport != null)
                  {
                     String targetNamespace = xsdImport.getNamespace();
                     String namespacePrefix = TypeDeclarationUtils.getNamespacePrefix(def.getSchema(), targetNamespace);
                     if (!StringUtils.isEmpty(namespacePrefix))
                     {
                        def.getSchema().getQNamePrefixToNamespaceMap().remove(namespacePrefix);
                     }
                  }
               }
            }
         }
         else
         {
            XSDTypeDefinition baseTypeDefinition = def.getBaseTypeDefinition();
            XSDTypeDefinition type = resolveType(def, rawBase, locations);
            def.setBaseTypeDefinition(type);
            if (baseTypeDefinition != null && type != null &&
                  !baseTypeDefinition.getTargetNamespace().equals(type.getTargetNamespace()))
            {
               if (!XSDElementCheckForType.needsType(def, baseTypeDefinition))
               {
                  XSDImport xsdImport = TypeDeclarationUtils.removeImport(def.getSchema(), baseTypeDefinition.getSchema());
                  if (xsdImport != null)
                  {
                     String targetNamespace = xsdImport.getNamespace();
                     String namespacePrefix = TypeDeclarationUtils.getNamespacePrefix(def.getSchema(), targetNamespace);
                     if (!StringUtils.isEmpty(namespacePrefix))
                     {
                        def.getSchema().getQNamePrefixToNamespaceMap().remove(namespacePrefix);
                     }
                  }
               }
            }
         }
      }
      if (json.has("method"))
      {
         String rawMethod = GsonUtils.safeGetAsString(json, "method");
         if (rawMethod == null)
         {
            def.unsetDerivationMethod();
         }
         else
         {
            XSDDerivationMethod method = XSDDerivationMethod.getByName(rawMethod);
            def.setDerivationMethod(method);
         }
      }
      JsonArray bodyArrayJson = json.getAsJsonArray("body");
      for (JsonElement item : bodyArrayJson)
      {
         if (item instanceof JsonObject)
         {
            JsonObject bodyJson = (JsonObject) item;
            if (!GsonUtils.safeGetBool(bodyJson, "inherited"))
            {
               updateComplexTypeStructure(def, bodyJson, locations);
               break;
            }
         }
      }
   }

   private static void updateComplexTypeStructure(XSDComplexTypeDefinition def, JsonObject bodyJson, JsonObject locations)
   {
      XSDComplexTypeContent content = def.getContent();

      if (null == content)
      {
         content = XSDFactory.eINSTANCE.createXSDParticle();
         ((XSDParticle) content).setContent(XSDFactory.eINSTANCE.createXSDModelGroup());
         def.setContent(content);
      }

      if (content instanceof XSDParticle)
      {
         XSDParticle particle = (XSDParticle) content;
         XSDTerm term = particle.getTerm();

         if (term instanceof XSDModelGroup)
         {
            XSDModelGroup group = (XSDModelGroup) term;
            String classifier = bodyJson.getAsJsonPrimitive("classifier").getAsString();
            group.setCompositor(XSDCompositor.get(classifier));
            List<XSDParticle> particles = group.getContents();
            particles.clear();

            if (hasNotJsonNull(bodyJson, "body"))
            {
               JsonArray elements = bodyJson.getAsJsonArray("body");
               for (JsonElement entry : elements)
               {
                  if ( !(entry instanceof JsonObject))
                  {
                     trace.warn("Expected object, but received " + entry);
                     continue;
                  }
                  JsonObject elementJson = (JsonObject) entry;
                  XSDParticle p = XSDFactory.eINSTANCE.createXSDParticle();
                  ParticleCardinality.get(
                        elementJson.getAsJsonPrimitive("cardinality").getAsString())
                        .update(p);
                  XSDElementDeclaration decl = XSDFactory.eINSTANCE.createXSDElementDeclaration();
                  p.setContent(decl);
                  decl.setName(elementJson.getAsJsonPrimitive("name").getAsString());
                  String type = elementJson.getAsJsonPrimitive("type").getAsString();

                  XSDTypeDefinition typeDefinition = resolveType(def, type, locations);
                  decl.setTypeDefinition(typeDefinition );
                  particles.add(p);

                  setAnnotations(decl, elementJson);
               }
            }
         }
         // else unsupported wildcard and element declaration
      }
      // else unsupported simple & complex content
   }

   private static void setAnnotations(XSDElementDeclaration decl, JsonObject json)
   {
      JsonObject appinfoJson = GsonUtils.safeGetAsJsonObject(json, "appinfo");
      if (appinfoJson != null)
      {
         XSDAnnotation annotation = XSDFactory.eINSTANCE.createXSDAnnotation();
         decl.setAnnotation(annotation);
         Element appInfo = annotation.createApplicationInformation(null);
         addChildren(appInfo.getOwnerDocument(), appInfo, appinfoJson, true);
         annotation.getElement().appendChild(appInfo);
      }
   }

   private static void addChildren(Document document, Element element, JsonObject value, boolean setNamespace)
   {
      for (Map.Entry<String, JsonElement> entry : ((JsonObject) value).entrySet())
      {
         element.appendChild(createElement(document, entry, setNamespace));
      }
   }

   private static Element createElement(Document document, Entry<String, JsonElement> entry, boolean setNamespace)
   {
      Element element = document.createElementNS(NS_URI, PREFIX + entry.getKey());
      if (setNamespace)
      {
         element.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, XMLNS_CARNOT, NS_URI);
      }
      JsonElement value = entry.getValue();
      if (value.isJsonPrimitive())
      {
         element.appendChild(document.createTextNode(value.getAsString()));
      }
      else if (value.isJsonObject())
      {
         addChildren(document, element, (JsonObject) value, false);
      }
      // else if (value.isJsonArray())
      return element;
   }

   private static XSDTypeDefinition resolveType(XSDComponent def, String type, JsonObject locations)
   {
      XSDSchema schema = def.getSchema();
      Map<String, String> prefix2Namespace = schema.getQNamePrefixToNamespaceMap();

      boolean updateSchema = false;
      String namespace = null;
      String localName = null;
      if (type.startsWith("{"))
      {
         updateSchema = true;
         QName qname = QName.valueOf(type);
         localName = qname.getLocalPart();
         namespace = qname.getNamespaceURI();
         if (!prefix2Namespace.containsValue(namespace))
         {
            String nsPrefix = qname.getPrefix();
            if (isEmpty(nsPrefix))
            {
               nsPrefix = TypeDeclarationUtils.computePrefix(localName.toLowerCase(), prefix2Namespace.keySet());
            }
            prefix2Namespace.put(nsPrefix, namespace);
            // propagate ns-prefix mappings to DOM
            schema.updateElement(true);
         }
      }
      else
      {
         int ix = type.indexOf(':');
         namespace = prefix2Namespace.get(ix < 0 ? null : type.substring(0, ix));
         if (ix > 0)
         {
            localName = type.substring(ix + 1);
         }
      }

      XSDTypeDefinition typeDefinition = def.resolveTypeDefinition(namespace, localName);
      if (typeDefinition.eContainer() != null)
      {
         // type could be properly resolved by current schema
         return typeDefinition;
      }

      Collection<XSDSchema> targetSchemas = ((XSDSchemaImpl) schema).resolveSchema(namespace);
      if (targetSchemas.isEmpty() && updateSchema)
      {
         // find target schema
         ModelType model = ModelUtils.findContainingModel(def);
         if (locations != null)
         {
            String location = GsonUtils.safeGetAsString(locations, namespace);
            if (location != null)
            {
               QName qname = QName.valueOf(location);
               String refModelId = qname.getNamespaceURI();
               if (XMLConstants.NULL_NS_URI != refModelId)
               {
                  ExternalPackages packs = model.getExternalPackages();
                  if (packs != null)
                  {
                     ExternalPackage refPack = packs.getExternalPackage(refModelId);
                     if (refPack != null)
                     {
                        ModelType refModel = ModelUtils.getExternalModel(refPack);
                        if (refModel != null)
                        {
                           model = refModel;
                        }
                     }
                  }
               }
            }
         }
         if (model != null)
         {
            TypeDeclarationsType declarations = model.getTypeDeclarations();
            // (fh) try first an exact match
            TypeDeclarationType decl = declarations.getTypeDeclaration(localName);
            if (decl != null && canResolve(decl, namespace, localName))
            {
               addImport(schema, namespace, decl);
            }
            else
            {
               for (TypeDeclarationType typeDeclaration : declarations.getTypeDeclaration())
               {
                  if (canResolve(typeDeclaration, namespace, localName))
                  {
                     addImport(schema, namespace, typeDeclaration);
                     break;
                  }
               }
            }
         }
      }

      typeDefinition = def.resolveTypeDefinition(namespace, localName);
      return typeDefinition.eContainer() == null ? null : typeDefinition;
   }

   private static boolean canResolve(TypeDeclarationType typeDeclaration, String namespace, String localName)
   {
      XSDSchema schema = typeDeclaration.getSchema();
      if (schema != null)
      {
         XSDTypeDefinition type = schema.resolveTypeDefinition(namespace, localName);
         if (type != null && type.eContainer() != null)
         {
            return true;
         }
      }
      return false;
   }

   private static void addImport(XSDSchema schema, String namespace, TypeDeclarationType decl)
   {
      if (!TypeDeclarationUtils.hasImport(schema, decl))
      {
         XSDImport schemaImport = XSDFactory.eINSTANCE.createXSDImport();
         schemaImport.setNamespace(namespace);
         ModelType model = ModelUtils.findContainingModel(decl);
         String location = model == ModelUtils.findContainingModel(schema)
               ? StructuredDataConstants.URN_INTERNAL_PREFIX + decl.getId()
               : StructuredDataConstants.URN_INTERNAL_PREFIX + '{' + model.getId() + '}' + decl.getId();
         schemaImport.setSchemaLocation(location);
         schemaImport.setResolvedSchema(decl.getSchema());
         schema.getContents().add(0, schemaImport);
      }
   }

   private static enum ParticleCardinality
   {
      required, optional, many, atLeastOne;

      void update(XSDParticle particle)
      {
         switch (this)
         {
         case required:
            particle.unsetMinOccurs();
            particle.unsetMaxOccurs();
            break;
         case optional:
            particle.setMinOccurs(0);
            particle.unsetMaxOccurs();
            break;
         case many:
            particle.setMinOccurs(0);
            particle.setMaxOccurs(XSDParticle.UNBOUNDED);
            break;
         case atLeastOne:
            particle.unsetMinOccurs();
            particle.setMaxOccurs(XSDParticle.UNBOUNDED);
            break;
         }
      }

      static ParticleCardinality get(String name)
      {
         if ("at least one".equals(name))
         {
            return atLeastOne;
         }
         return valueOf(name);
      }
   }
}