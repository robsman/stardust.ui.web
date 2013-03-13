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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.wsdl.*;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.struct.StructuredDataConstants;
import org.eclipse.stardust.engine.core.struct.spi.StructDataTransformerKey;
import org.eclipse.stardust.engine.extensions.jaxws.addressing.EndpointReferenceType;
import org.eclipse.stardust.engine.extensions.jaxws.addressing.WSAddressing;
import org.eclipse.stardust.engine.extensions.jaxws.app.IBasicAuthenticationParameters;
import org.eclipse.stardust.engine.extensions.jaxws.app.WSConstants;
import org.eclipse.stardust.model.xpdl.carnot.*;
import org.eclipse.stardust.model.xpdl.carnot.util.*;
import org.eclipse.stardust.model.xpdl.xpdl2.*;
import org.eclipse.stardust.model.xpdl.xpdl2.util.TypeDeclarationUtils;
import org.eclipse.stardust.ui.web.modeler.portal.JaxWSResource;
import org.eclipse.xsd.XSDNamedComponent;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public final class WebServiceApplicationUtils
{
   private static final Logger trace = LogManager.getLogger(WebServiceApplicationUtils.class);
   
   private static final String ENDPOINT_ADDRESS_LABEL = "Endpoint Address";
   private static final String ENDPOINT_REFERENCE_LABEL = "WS-Addressing EndpointReference";
   private static final String AUTHENTICATION_LABEL = "Authentication";

   private WebServiceApplicationUtils() {}

   public static boolean isWebServiceApplication(ApplicationType application)
   {
      ApplicationTypeType applicationType = application.getType();
      return applicationType != null &&
            PredefinedConstants.WS_APPLICATION.equals(applicationType.getId());
   }

   public static void updateWebServiceApplication(ApplicationType application, JsonObject applicationJson)
   {
      if (applicationJson.has("attributes"))
      {
         JsonObject attributes = WebServiceApplicationUtils.safeGetAsJsonObject(applicationJson, "attributes");
         updateAddressing(application, attributes);
         updateSecurity(application, attributes);
         updateService(application, attributes);
      }
   }

   private static void updateAddressing(ApplicationType application, JsonObject attributes)
   {
      List<AccessPointType> accessPoints = application.getAccessPoint();
      boolean hasReference = false;
      if (attributes.has(WSConstants.WS_IMPLEMENTATION_ATT))
      {
         String implementation = safeGetAsString(attributes, WSConstants.WS_IMPLEMENTATION_ATT);
         if (implementation != null)
         {
            hasReference = true;
            AccessPointType address = ModelUtils.findIdentifiableElement(accessPoints, WSConstants.WS_ENDPOINT_ADDRESS_ID);
            if (address != null)
            {
               accessPoints.remove(address);
            }
            // (fh) just recreate access point since it might have a different implementation class
            AccessPointType epr = ModelUtils.findIdentifiableElement(accessPoints, WSConstants.WS_ENDPOINT_REFERENCE_ID);
            if (epr != null)
            {
               accessPoints.remove(epr);
            }
            createSerializableAccessPoint(application,
                  WSConstants.WS_ENDPOINT_REFERENCE_ID, ENDPOINT_REFERENCE_LABEL,
                  DirectionType.IN_LITERAL, WSConstants.WS_CARNOT_EPR.equals(implementation) ?
                        WSAddressing.IPPEndpointReference.class.getName() :
                        EndpointReferenceType.class.getName());
         }
      }
      else
      {
         hasReference = AttributeUtil.getAttributeValue(application, WSConstants.WS_IMPLEMENTATION_ATT) != null;
      }
      if (!hasReference)
      {
         AccessPointType epr = ModelUtils.findIdentifiableElement(accessPoints, WSConstants.WS_ENDPOINT_REFERENCE_ID);
         if (epr != null)
         {
            accessPoints.remove(epr);
         }
         AccessPointType address = ModelUtils.findIdentifiableElement(accessPoints, WSConstants.WS_ENDPOINT_ADDRESS_ID);
         if (address == null)
         {
            createSerializableAccessPoint(application,
                  WSConstants.WS_ENDPOINT_ADDRESS_ID, ENDPOINT_ADDRESS_LABEL,
                  DirectionType.IN_LITERAL, String.class.getName());
         }
      }
   }

   private static void updateSecurity(ApplicationType application, JsonObject attributes)
   {
      if (attributes.has(WSConstants.WS_AUTHENTICATION_ATT) || attributes.has(WSConstants.WS_VARIANT_ATT))
      {
         String authentication = attributes.has(WSConstants.WS_AUTHENTICATION_ATT)
               ? safeGetAsString(attributes, WSConstants.WS_AUTHENTICATION_ATT)
               : AttributeUtil.getAttributeValue(application, WSConstants.WS_AUTHENTICATION_ATT);
         String variant = safeGetAsString(attributes, WSConstants.WS_VARIANT_ATT);
         if (!WSConstants.WS_SECURITY_AUTHENTICATION.equals(authentication))
         {
            if (variant != null)
            {
               attributes.addProperty(WSConstants.WS_VARIANT_ATT, (String) null);
            }
            variant = null;
         }

         List<AccessPointType> accessPoints = application.getAccessPoint();
         AccessPointType auth = ModelUtils.findIdentifiableElement(accessPoints, WSConstants.WS_AUTHENTICATION_ID);
         if (auth != null)
         {
            accessPoints.remove(auth);
         }
         
         if (authentication != null)
         {
            createSerializableAccessPoint(application,
                  WSConstants.WS_AUTHENTICATION_ID, AUTHENTICATION_LABEL,
                  DirectionType.IN_LITERAL, IBasicAuthenticationParameters.class.getName());
         }
      }
   }

   private static void updateService(ApplicationType application, JsonObject attributes)
   {
      if (attributes.has(WSConstants.WS_WSDL_URL_ATT)
            || attributes.has(WSConstants.WS_SERVICE_NAME_ATT)
            || attributes.has(WSConstants.WS_PORT_NAME_ATT)
            || attributes.has(WSConstants.WS_OPERATION_NAME_ATT))
      {
         application.getAccessPoint().retainAll(saveAccessPoints(application, WSConstants.WS_ENDPOINT_ADDRESS_ID,
               WSConstants.WS_ENDPOINT_REFERENCE_ID, WSConstants.WS_AUTHENTICATION_ID));
         
         String wsdlUrl = safeGetAsString(attributes, WSConstants.WS_WSDL_URL_ATT);
         if (StringUtils.isEmpty(wsdlUrl))
         {
            wsdlUrl = AttributeUtil.getAttributeValue(application, WSConstants.WS_WSDL_URL_ATT);
            if (StringUtils.isEmpty(wsdlUrl))
            {
               trace.warn("Service present but no WSDL location specified.");
               return;
            }
         }
         
         String serviceName = safeGetAsString(attributes, WSConstants.WS_SERVICE_NAME_ATT);
         String portName = safeGetAsString(attributes, WSConstants.WS_PORT_NAME_ATT);
         String operationName = safeGetAsString(attributes, WSConstants.WS_OPERATION_NAME_ATT);
         String operationInputName = null;
         String operationOutputName = null;
         if (operationName != null && operationName.endsWith(")"))
         {
            int ix = operationName.lastIndexOf('(');
            if (ix >= 0)
            {
               String params = operationName.substring(ix + 1, operationName.length() - 1).trim();
               operationName = operationName.substring(0, ix).trim();
               ix = params.indexOf(',');
               if (ix >= 0)
               {
                  operationInputName = getIOName(params.substring(0, ix).trim());
                  operationOutputName = getIOName(params.substring(ix + 1).trim());
               }
            }
         }
         
         String oldServiceName = AttributeUtil.getAttributeValue(application, WSConstants.WS_SERVICE_NAME_ATT);
         if (serviceName == null)
         {
            if (oldServiceName == null)
            {
               return;
            }
            serviceName = oldServiceName;
         }
         else if (!CompareHelper.areEqual(serviceName, oldServiceName)
               && (portName == null || operationName == null))
         {
            // partial definition
            AttributeUtil.setAttribute(application, WSConstants.WS_PORT_NAME_ATT, null);
            AttributeUtil.setAttribute(application, WSConstants.WS_OPERATION_NAME_ATT, null);
            return;
         }
         
         String oldPortName = AttributeUtil.getAttributeValue(application, WSConstants.WS_PORT_NAME_ATT);
         if (portName == null)
         {
            if (oldPortName == null)
            {
               return;
            }
            portName = oldPortName;
         }
         else if (!CompareHelper.areEqual(portName, oldPortName)
               && operationName == null)
         {
            // partial definition
            AttributeUtil.setAttribute(application, WSConstants.WS_OPERATION_NAME_ATT, null);
            return;
         }
         
         String oldOperationName = AttributeUtil.getAttributeValue(application, WSConstants.WS_OPERATION_NAME_ATT);
         String oldOperationInputName = AttributeUtil.getAttributeValue(application, WSConstants.WS_OPERATION_INPUT_NAME_ATT);
         String oldOperationOutputName = AttributeUtil.getAttributeValue(application, WSConstants.WS_OPERATION_OUTPUT_NAME_ATT);
         if (operationName == null)
         {
            if (oldOperationName == null)
            {
               return;
            }
            operationName = oldOperationName;
            operationInputName = oldOperationInputName;
            operationOutputName = oldOperationOutputName;
         }
 
         Definition definition = JaxWSResource.getDefinition(wsdlUrl);
         Binding binding = null;
         if (serviceName.equals(WSConstants.DYNAMIC_BOUND_SERVICE_QNAME.getLocalPart()))
         {
            attributes.addProperty(WSConstants.WS_SERVICE_NAME_ATT,
                  WSConstants.DYNAMIC_BOUND_SERVICE_QNAME.toString());
            binding = definition.getBinding(QName.valueOf(portName));
            if (binding == null)
            {
               trace.warn("No binding '" + portName + "' found.");
               return;
            }
            attributes.addProperty(WSConstants.WS_PORT_NAME_ATT, binding.getQName().toString());
         }
         else
         {
            Service service = findService(definition, serviceName);
            if (service == null)
            {
               trace.warn("No service '" + serviceName + "' found.");
               return;
            }
            attributes.addProperty(WSConstants.WS_SERVICE_NAME_ATT, service.getQName().toString());
            Port port = service.getPort(portName);
            if (port == null)
            {
               trace.warn("No port '" + portName + "' found.");
               return;
            }
            attributes.addProperty(WSConstants.WS_PORT_NAME_ATT, port.getName());
            binding = port.getBinding();
            if (binding == null)
            {
               trace.warn("No binding for port '" + portName + "' found.");
               return;
            }
         }
         BindingOperation bindingOperation = binding.getBindingOperation(operationName, operationInputName, operationOutputName);
         if (bindingOperation == null)
         {
            trace.warn("No operation '"
                  + getOperationName(operationName, operationInputName, operationOutputName) + "' found.");
            return;
         }
         attributes.addProperty(WSConstants.WS_OPERATION_NAME_ATT, bindingOperation.getName());
         BindingInput bindingInput = bindingOperation.getBindingInput();
         String bindingInputName = bindingInput == null ? null : bindingInput.getName();
         if (bindingInputName == null)
         {
            AttributeUtil.setAttribute(application, WSConstants.WS_OPERATION_INPUT_NAME_ATT, null);
         }
         else
         {
            attributes.addProperty(WSConstants.WS_OPERATION_INPUT_NAME_ATT, bindingInputName);
         }
         BindingOutput bindingOutput = bindingOperation.getBindingOutput();
         String bindingOutputName = bindingOutput == null ? null : bindingOutput.getName();
         if (bindingOutputName == null)
         {
            AttributeUtil.setAttribute(application, WSConstants.WS_OPERATION_OUTPUT_NAME_ATT, null);
         }
         else
         {
            attributes.addProperty(WSConstants.WS_OPERATION_OUTPUT_NAME_ATT, bindingOutputName);
         }
         attributes.addProperty(WSConstants.WS_SOAP_ACTION_URI_ATT, JaxWSResource.getSoapActionUri(bindingOperation));
         attributes.addProperty(WSConstants.WS_SOAP_PROTOCOL_ATT, JaxWSResource.getOperationProtocol(bindingOperation));
         Operation operation = bindingOperation.getOperation();
         Input input = operation.getInput();
         if (input == null)
         {
            AttributeUtil.setAttribute(application, WSConstants.WS_INPUT_ORDER_ATT, null);
         }
         else
         {
            Message message = input.getMessage();
            attributes.addProperty(WSConstants.WS_INPUT_ORDER_ATT, ModelService.getPartsOrder(message));
            if (message != null)
            {
               createAccessPoints(application, message, DirectionType.IN_LITERAL, wsdlUrl);
            }
         }
         Output output = operation.getOutput();
         if (output == null)
         {
            AttributeUtil.setAttribute(application, WSConstants.WS_OUTPUT_ORDER_ATT, null);
         }
         else
         {
            Message message = output.getMessage();
            attributes.addProperty(WSConstants.WS_OUTPUT_ORDER_ATT, ModelService.getPartsOrder(message));
            if (message != null)
            {
               createAccessPoints(application, message, DirectionType.OUT_LITERAL, wsdlUrl);
            }
         }
      }
   }

   private static List<AccessPointType> saveAccessPoints(ApplicationType application, String... ids)
   {
      if (ids == null || ids.length == 0)
      {
         return Collections.<AccessPointType>emptyList();
      }
      List<AccessPointType> savedAccessPoints = CollectionUtils.newList();
      List<AccessPointType> accessPoints = application.getAccessPoint();
      for (String id : ids)
      {
         AccessPointType accessPoint = ModelUtils.findIdentifiableElement(accessPoints, id);
         if (accessPoint != null)
         {
            savedAccessPoints.add(accessPoint);
         }
      }
      return savedAccessPoints;
   }

   private static void createAccessPoints(ApplicationType application, Message message,
         DirectionType direction, String wsdlUrl)
   {
      @SuppressWarnings("unchecked")
      Iterator<Part> i = message.getOrderedParts(null).iterator();
      while (i.hasNext())
      {
         Part part = i.next();
         createAccessPoints(application, part, direction, wsdlUrl);
      }
   }

   public static void createAccessPoints(ApplicationType application, Part part,
         DirectionType direction, String wsdlUrl)
   {
      createPlainXmlAccessPoint(application, part, direction);
      createStructAccessPoint(application, part, direction, wsdlUrl);
   }

   private static void createPlainXmlAccessPoint(ApplicationType application, Part part, DirectionType direction)
   {
      String id = part.getName();
      AccessPointType ap = AccessPointUtil.createAccessPoint(id, id, direction, ModelUtils.getDataType(
            application, PredefinedConstants.PLAIN_XML_DATA));
      if (DirectionType.IN_LITERAL.equals(direction))
      {
         AttributeUtil.setAttribute(ap, CarnotConstants.BROWSABLE_ATT, "boolean", //$NON-NLS-1$
               Boolean.TRUE.toString());
      }
      application.getAccessPoint().add(ap);
   }

   private static void createSerializableAccessPoint(ApplicationType application, String id,
         String name, DirectionType direction, String className)
   {
      AccessPointType ap = AccessPointUtil.createAccessPoint(id, name, direction, ModelUtils.getDataType(
            application, PredefinedConstants.SERIALIZABLE_DATA));
      application.getAccessPoint().add(ap);
      AttributeUtil.setAttribute(ap, CarnotConstants.CLASS_NAME_ATT, className);
      if (DirectionType.IN_LITERAL.equals(direction))
      {
         AttributeUtil.setAttribute(ap, CarnotConstants.BROWSABLE_ATT, "boolean", //$NON-NLS-1$
               Boolean.TRUE.toString());
      }
   }

   private static void createStructAccessPoint(ApplicationType application, Part part,
         DirectionType direction, String wsdlUrl)
   {
      // try to find corresponding type for the part
      QName qname = part.getElementName();
      if (qname == null)
      {
         qname = part.getTypeName();
      }
      
      if (qname != null && !XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(qname.getNamespaceURI()))
      {
         ModelType model = ModelUtils.findContainingModel(application);
         TypeDeclarationType typeDeclaration = findMatchingTypeDeclaration(model, qname);
         if (typeDeclaration == null)
         {
            typeDeclaration = createExternalReference(model, qname, wsdlUrl);
         }
         
         String id = part.getName() + WSConstants.STRUCT_POSTFIX;
         String name = id + " (" + typeDeclaration.getName() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
         String transformationType = null;
         if (direction.equals(DirectionType.IN_LITERAL) || direction.equals(DirectionType.INOUT_LITERAL))
         {
            // force IN/INOUT structured data values to be transformed to DOM
            transformationType = StructDataTransformerKey.DOM;
         }
         
         AccessPointType ap = AccessPointUtil.createAccessPoint(id, name, direction, ModelUtils.getDataType(
               application, StructuredDataConstants.STRUCTURED_DATA));
         // (fh) the access point must be added to the container before setting the reference to the type declaration
         application.getAccessPoint().add(ap);
         StructuredTypeUtils.setStructuredAccessPointAttributes(ap, typeDeclaration, transformationType);
      }
   }

   private static TypeDeclarationType createExternalReference(ModelType model,
         QName qname, String wsdlUrl)
   {
      XpdlFactory factory = XpdlFactory.eINSTANCE;
      TypeDeclarationsType declarations = model.getTypeDeclarations();
      if (declarations == null)
      {
         declarations = factory.createTypeDeclarationsType();
         model.setTypeDeclarations(declarations);
      }
      TypeDeclarationType declaration = factory.createTypeDeclarationType();
      declaration.setId(qname.getLocalPart());
      declaration.setName(qname.getLocalPart());
      ExternalReferenceType externalReference = factory.createExternalReferenceType();
      externalReference.setLocation(wsdlUrl);
      externalReference.setXref(qname.toString());
      declaration.setExternalReference(externalReference);
      declarations.getTypeDeclaration().add(declaration);
      return declaration;
   }

   public static TypeDeclarationType findMatchingTypeDeclaration(ModelType model, QName qname)
   {
      TypeDeclarationType type = findLocalTypeDeclaration(model, qname);
      if (type == null)
      {
         type = findExternalTypeDeclaration(model, qname);
      }
      return type;
   }

   private static TypeDeclarationType findExternalTypeDeclaration(ModelType model, QName qname)
   {
      ExternalPackages packages = model.getExternalPackages();
      if (packages != null)
      {
         for (ExternalPackage pkg : packages.getExternalPackage())
         {
            model = ModelUtils.getExternalModel(pkg);
            if (model != null)
            {
               TypeDeclarationType type = findLocalTypeDeclaration(model, qname);
               if (type != null)
               {
                  return type;
               }
            }
         }
      }
      return null;
   }

   public static TypeDeclarationType findLocalTypeDeclaration(ModelType model, QName qname)
   {
      TypeDeclarationsType typeDeclarations = model.getTypeDeclarations();
      if (typeDeclarations != null)
      {
         TypeDeclarationType typeDeclaration = typeDeclarations.getTypeDeclaration(qname.getLocalPart());
         if (typeDeclaration != null)
         {
            XSDNamedComponent component = TypeDeclarationUtils.findElementOrTypeDeclaration(typeDeclaration);
            if (component != null)
            {
               if (CompareHelper.areEqual(component.getName(), qname.getLocalPart()))
               {
                  String tns = component.getTargetNamespace();
                  if (tns == null)
                  {
                     tns = XMLConstants.NULL_NS_URI;
                  }
                  if (CompareHelper.areEqual(tns, qname.getNamespaceURI()))
                  {
                     return typeDeclaration;
                  }
               }
            }
         }
      }
      return null;
   }
   
   private static String getOperationName(String operationName,
         String operationInputName, String operationOutputName)
   {
      return operationInputName == null
            ? operationOutputName == null
               ? operationName
               : operationName + "(:none," + operationOutputName + ")"
            : operationOutputName == null
               ? operationName + "(" + operationInputName + ",:none)"
               : operationName + "(" + operationInputName + "," + operationOutputName + ")";
   }

   private static String getIOName(String raw)
   {
      return raw.equals(":none") ? null : raw;
   }

   private static Service findService(Definition definition, String newService)
   {
      QName serviceName = QName.valueOf(newService);
      if (serviceName.getNamespaceURI() == XMLConstants.NULL_NS_URI)
      {
         @SuppressWarnings("unchecked")
         Map<?, Service> services = definition.getServices();
         for (Service service : services.values())
         {
            if (newService.equals(service.getQName().getLocalPart()))
            {
               return service;
            }
         }
      }
      return definition.getService(serviceName);
   }

   private static JsonObject safeGetAsJsonObject(JsonObject jsonObject, String memberName)
   {
      if (jsonObject.has(memberName))
      {
         JsonElement member = jsonObject.get(memberName);
         if (member.isJsonObject())
         {
            return member.getAsJsonObject();
         }
      }
      return null;
   }

   private static String safeGetAsString(JsonObject jsonObject, String memberName)
   {
      if (jsonObject.has(memberName))
      {
         JsonElement member = jsonObject.get(memberName);
         if (member.isJsonPrimitive())
         {
            return member.getAsString();
         }
      }
      return null;
   }
}
