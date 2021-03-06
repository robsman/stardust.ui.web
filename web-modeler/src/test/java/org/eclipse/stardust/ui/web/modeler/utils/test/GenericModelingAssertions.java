package org.eclipse.stardust.ui.web.modeler.utils.test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xsd.XSDAnnotation;
import org.eclipse.xsd.XSDComplexTypeDefinition;
import org.eclipse.xsd.XSDElementDeclaration;
import org.w3c.dom.Node;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.model.xpdl.builder.connectionhandler.EObjectProxyHandler;
import org.eclipse.stardust.model.xpdl.carnot.*;
import org.eclipse.stardust.model.xpdl.carnot.extensions.FormalParameterMappingsType;
import org.eclipse.stardust.model.xpdl.carnot.util.AttributeUtil;
import org.eclipse.stardust.model.xpdl.carnot.util.ModelUtils;
import org.eclipse.stardust.model.xpdl.carnot.util.ModelVariable;
import org.eclipse.stardust.model.xpdl.carnot.util.VariableContext;
import org.eclipse.stardust.model.xpdl.xpdl2.*;
import org.eclipse.stardust.model.xpdl.xpdl2.DataTypeType;
import org.eclipse.stardust.model.xpdl.xpdl2.util.ExtendedAttributeUtil;
import org.eclipse.stardust.model.xpdl.xpdl2.util.TypeDeclarationUtils;

public class GenericModelingAssertions
{
   public static ProcessDefinitionType assertProcess(ModelType model, String processID, String processName)
   {
      ProcessDefinitionType process = (ProcessDefinitionType) ModelUtils
            .findIdentifiableElement(model,
                  CarnotWorkflowModelPackage.eINSTANCE.getModelType_ProcessDefinition(),
                  processID);
      assertThat(process, is(not(nullValue())));
      assertThat(process.getName(), is(not(nullValue())));
      assertThat(process.getName(), is(processName));
      return process;
   }
   
   public static DataPathType assertDataPath(ProcessDefinitionType process,
         String dataPathID, String dataPathName, DirectionType direction)
   {
      List<DataPathType> dataPaths = process.getDataPath();
      assertThat(dataPaths.isEmpty(), is(false));
      DataPathType dataPath = null;
      for (Iterator<DataPathType> i = dataPaths.iterator(); i.hasNext();)
      {
         DataPathType foundType = i.next();
         assertThat(foundType.getName(), is(not(nullValue())));
         assertThat(foundType.getId(), is(not(nullValue())));
         assertThat(foundType.getDirection(), is(not(nullValue())));
         if (foundType.getId().equals(dataPathID)
               && foundType.getName().equals(dataPathName)
               && foundType.getDirection().equals(direction))
         {
            dataPath = foundType;
         }
      }
      assertThat(dataPath, is(not(nullValue())));
      return dataPath;
   }
   
 
   
   public static void assertProcessDoesNotExist(ModelType model, String processID)
   {
      ProcessDefinitionType process = (ProcessDefinitionType) ModelUtils
            .findIdentifiableElement(model,
                  CarnotWorkflowModelPackage.eINSTANCE.getModelType_ProcessDefinition(),
                  processID);
      assertThat(process, is(nullValue()));
   }

   public static ProcessDefinitionType assertSubProcess(ActivityType activity,
         String subProcessID)
   {
      assertThat(activity.getImplementation(),
            is(ActivityImplementationType.SUBPROCESS_LITERAL));
      assertThat(activity.getSubProcessMode(), is(not(nullValue())));
      AttributeType attribute = AttributeUtil.getAttribute(activity,
            "carnot:engine:subprocess:copyAllData");
      assertThat(attribute, is(not(nullValue())));
      ProcessDefinitionType subProcess = activity.getImplementationProcess();
      assertThat(subProcess, is(not(nullValue())));
      assertThat(subProcess.getId(), is(subProcessID));
      ModelType subProcessModel = ModelUtils.findContainingModel(subProcess);
      ModelType activityModel = ModelUtils.findContainingModel(activity);
      assertThat(subProcessModel, is(not(nullValue())));
      assertThat(activityModel, is(not(nullValue())));
      assertThat(subProcessModel, is(activityModel));
      return subProcess;
   }

   public static ActivityType assertActivity(ProcessDefinitionType process,
         String activityID, String activityName, ActivityImplementationType implType)
   {
      ActivityType activity = (ActivityType) ModelUtils.findIdentifiableElement(process,
            CarnotWorkflowModelPackage.eINSTANCE.getProcessDefinitionType_Activity(),
            activityID);
      assertThat(activity, is(not(nullValue())));
      assertThat(activity.getName(), is(not(nullValue())));
      assertThat(activity.getName(), is(activityName));
      assertThat(activity.getImplementation(), is(not(nullValue())));
      assertThat(implType, is(activity.getImplementation()));
      return activity;
   }
   
   public static void assertActivityDoesNotExist(ProcessDefinitionType process,
         String activityID)
   {
      ActivityType activity = (ActivityType) ModelUtils.findIdentifiableElement(process,
            CarnotWorkflowModelPackage.eINSTANCE.getProcessDefinitionType_Activity(),
            activityID);     
      assertThat(activity, is(nullValue()));
   }

   public static ApplicationType assertApplication(ModelType model, String applicationID)
   {
      ApplicationType application = (ApplicationType) ModelUtils.findIdentifiableElement(
            model, CarnotWorkflowModelPackage.eINSTANCE.getModelType_Application(),
            applicationID);
      assertThat(application, is(not(nullValue())));
      assertThat(application.getName(), is(not(nullValue())));
      return application;
   }



   public static ContextType assertApplicationContextType(ApplicationType application,
         String contextTypeTypeID)
   {
      ContextType returnType = null;
      List<ContextType> contexts = application.getContext();
      assertThat(contexts, is(not(nullValue())));
      assertThat(contexts.size(), is(not(0)));
      for (Iterator<ContextType> i = application.getContext().iterator(); i.hasNext();)
      {
         ContextType contextType = i.next();
         ApplicationContextTypeType contextTypeType = contextType.getType();
         assertThat(contextTypeType, is(not(nullValue())));
         assertThat(contextTypeType.getId(), is(not(nullValue())));
         if (contextType.getType().getId().equals(contextTypeTypeID))
         {
            returnType = contextType;
         }
      }
      assertThat(returnType, is(not(nullValue())));
      return returnType;
   }

   public static AccessPointType assertAccessPoint(IAccessPointOwner accessPointOwner,
         String accessPointID, String accessPointName, DirectionType direction,
         String typeID, String type)
   {
      AccessPointType accessPointFound = null;
      List<AccessPointType> accessPoints = accessPointOwner.getAccessPoint();
      assertThat(accessPoints, is(not(nullValue())));
      assertThat(accessPoints.size(), is(not(0)));
      for (Iterator<AccessPointType> i = accessPoints.iterator(); i.hasNext();)
      {
         AccessPointType accessPoint = i.next();
         assertThat(accessPoint.getId(), is(not(nullValue())));
         assertThat(accessPoint.getName(), is(not(nullValue())));
         assertThat(accessPoint.getDirection(), is(not(nullValue())));
         assertThat(accessPoint.getType(), is(not(nullValue())));

         if (accessPoint.getId().equalsIgnoreCase(accessPointID)
               && accessPoint.getDirection().equals(direction))
         {
            accessPointFound = accessPoint;
         }
      }
      assertThat(accessPointFound, is(not(nullValue())));
      assertThat(accessPointFound.getName(), is(accessPointName));
      assertThat(accessPointFound.getType().getId(), is(typeID));

      AttributeType attribute = null;

      if (typeID.equals("struct") || typeID.equals("dmsDocument"))
      {
         attribute = AttributeUtil.getAttribute(accessPointFound,
               "carnot:engine:dataType");
      }
      else
      {
         attribute = AttributeUtil.getAttribute(accessPointFound,
               "carnot:engine:type");
      }

      assertThat(attribute, is(not(nullValue())));
      assertThat(attribute.getAttributeValue(), is(type));

      return accessPointFound;
   }

   public static DataMappingType assertDataMapping(ActivityType activity, String dataMappingName, String dataMappingID,
         String context, DirectionType direction, DataType data, String accessPoint, String accessPointPath, String dataPath)
   {
      DataMappingType dataMappingFound = null;
      List<DataMappingType> dataMappings = activity.getDataMapping();
      assertThat(dataMappings, is(not(nullValue())));
      assertThat(dataMappings.size(), is(not(0)));
      for (Iterator<DataMappingType> i = dataMappings.iterator(); i.hasNext();)
      {
         DataMappingType dataMapping = i.next();
         assertThat(dataMapping.getId(), is(not(nullValue())));
         assertThat(dataMapping.getName(), is(not(nullValue())));
         assertThat(dataMapping.getContext(), is(not(nullValue())));
         assertThat(dataMapping.getDirection(), is(not(nullValue())));

         if (dataMapping.getId().equalsIgnoreCase(dataMappingID)
               && dataMapping.getDirection().equals(direction))
         {
            dataMappingFound = dataMapping;
         }

      }
      assertThat(dataMappingFound, is(not(nullValue())));
      assertThat(dataMappingFound.getName(), is(dataMappingName));
      assertThat(dataMappingFound.getId(), is(dataMappingID));
      assertThat(dataMappingFound.getDirection(), is(direction));
      assertThat(dataMappingFound.getContext(), is(context));
      assertThat(dataMappingFound.getData(), is(not(nullValue())));
      assertThat(dataMappingFound.getApplicationAccessPoint(), is(accessPoint));
      assertThat(dataMappingFound.getApplicationPath(), is(accessPointPath));
      assertThat(dataMappingFound.getDataPath(), is(dataPath));

      return dataMappingFound;
   }

   public static FormalParameterType assertFormalParameter(ProcessDefinitionType process, String parameterID, String parameterName, ModeType modeType)
   {
      assertThat(process.getFormalParameters(), is(not(nullValue())));
      assertThat(process.getFormalParameterMappings(), is(not(nullValue())));
      FormalParameterType parameter = process.getFormalParameters().getFormalParameter(parameterID);
      assertThat(parameter, is(not(nullValue())));
      assertThat(parameter.getMode(), is(not(nullValue())));
      assertThat(parameter.getDataType(), is(not(nullValue())));
      assertThat(parameter.getId(), is(not(nullValue())));
      assertThat(parameter.getName(), is(not(nullValue())));
      assertThat(parameter.getMode(), is(modeType));
      assertThat(parameter.getName(), is(parameterName));
      assertThat(parameter.getId(), is(parameterID));
      return parameter;
   }

   public static FormalParameterType assertPrimitiveFormalParameter(ProcessDefinitionType process, String parameterID, String parameterName, ModeType modeType, TypeType typeType)
   {
      FormalParameterType parameter = assertFormalParameter(process, parameterID, parameterName, modeType);
      DataTypeType dataTypeType = parameter.getDataType();
      assertThat(dataTypeType, is(not(nullValue())));
      assertThat(dataTypeType.getCarnotType(), is(not(nullValue())));
      assertThat(dataTypeType.getCarnotType(), is("primitive"));
      BasicTypeType basicTypeType = dataTypeType.getBasicType();
      assertThat(basicTypeType, is(not(nullValue())));
      assertThat(basicTypeType.getType(), is(not(nullValue())));
      assertThat(basicTypeType.getType(), is(typeType));
      return parameter;
   }

   public static FormalParameterType assertStructFormalParameter(ProcessDefinitionType process, String parameterID, String parameterName, ModeType modeType, String declarationID)
   {
      FormalParameterType parameter = assertFormalParameter(process, parameterID, parameterName, modeType);
      assertDeclarationType(parameter, "struct", declarationID);
      return parameter;
   }

   public static FormalParameterType assertDocumentFormalParameter(ProcessDefinitionType process, String parameterID, String parameterName, ModeType modeType, String declarationID)
   {
      FormalParameterType parameter = assertFormalParameter(process, parameterID, parameterName, modeType);
      assertDeclarationType(parameter, "dmsDocument", declarationID);
      return parameter;
   }

   public static DataTypeType assertDeclarationType(FormalParameterType parameter, String carnotTypeID, String declarationID)
   {
      DataTypeType dataTypeType = parameter.getDataType();
      assertThat(dataTypeType, is(not(nullValue())));
      assertThat(dataTypeType.getCarnotType(), is(not(nullValue())));
      DeclaredTypeType declaredTypeType = dataTypeType.getDeclaredType();
      assertThat(declaredTypeType, is(not(nullValue())));
      assertThat(declaredTypeType.getId(), is(not(nullValue())));
      assertThat(declaredTypeType.getId(), is(declarationID));
      assertThat(dataTypeType.getCarnotType(), is(carnotTypeID));
      return dataTypeType;
   }
   
   public static void assertAnnotationForTypeDeclaration(TypeDeclarationType declaration,
         String elementName, String annotationName, String value)
   {
      XSDElementDeclaration element = getElementDeclaration(declaration, elementName);
      XSDAnnotation annotation = element.getAnnotation();
      Node uiNode = annotation.getElement().getFirstChild().getFirstChild();
      Node storageNode = uiNode.getNextSibling();
      List<Node> uiNodes = getUIAnnotations(uiNode);
      List<Node> storageNodes = getUIAnnotations(storageNode);
      List<Node> allAnnotationNodes = new ArrayList<Node>();
      allAnnotationNodes.addAll(uiNodes);
      allAnnotationNodes.addAll(storageNodes);
      uiNodes.addAll(storageNodes);
      Node valueNode = findValueNodeByLocalName(allAnnotationNodes, annotationName);
      assertThat(valueNode, is(not(nullValue())));
      assertThat(valueNode.getNodeValue(), is(not(nullValue())));
      assertThat(valueNode.getNodeValue(), is(value));
   }
   

   public static ProcessDefinitionType assertProcessInterface(ModelType model, String interfaceID, String interfaceName,
         int paramCount)
   {
      ProcessDefinitionType process = assertProcess(model, interfaceID, interfaceName);
      FormalParametersType parametersType = process.getFormalParameters();
      assertThat(parametersType, is(not(nullValue())));

      List<FormalParameterType> parameters = parametersType.getFormalParameter();
      assertThat(parameters, is(not(nullValue())));
      assertThat(parameters.size(), is(paramCount));

      FormalParameterMappingsType mappingsType = process.getFormalParameterMappings();
      assertThat(mappingsType, is(not(nullValue())));

      if (paramCount > 0)
      {
         for (Iterator<FormalParameterType> i = parameters.iterator(); i.hasNext();)
         {
            FormalParameterType formalParameter = i.next();
            assertThat(mappingsType.getMappedData(formalParameter), is(not(nullValue())));
         }
      }
      return process;
   }

   public static TypeDeclarationType assertTypeDeclaration(ModelType model, String declID, String declName)
   {
      TypeDeclarationType typeDeclaration = model.getTypeDeclarations().getTypeDeclaration(declID);
      assertThat(typeDeclaration, is(not(nullValue())));
      assertThat(typeDeclaration.getSchema(), is(not(nullValue())));
      assertThat(typeDeclaration.getId(), is(not(nullValue())));
      assertThat(typeDeclaration.getName(), is(not(nullValue())));
      assertThat(typeDeclaration.getId(), is(declID));
      assertThat(typeDeclaration.getName(), is(declName));
      ExtendedAttributeType extAttribute = ExtendedAttributeUtil.getAttribute(typeDeclaration.getExtendedAttributes(),  PredefinedConstants.MODEL_ELEMENT_UUID);
      assertThat(extAttribute, is(not(nullValue())));
      assertThat(extAttribute.getValue(), is(not(nullValue())));
      return typeDeclaration;
   }

   public static void assertInterfaceReference(ProcessDefinitionType interfaceImplementation, String modelID, String interfaceID)
   {
      IdRef externalReference = interfaceImplementation.getExternalRef();
      assertThat(externalReference, is(not(nullValue())));
      assertThat(externalReference.getRef(), is(interfaceID));
      assertThat(externalReference.getPackageRef(), is(not(nullValue())));
      assertThat(externalReference.getPackageRef().getId(), is(modelID));
   }

   public static DataType assertPrimitiveData(ModelType model, String dataID, String dataName,
         String primitiveType)
   {
      DataType dataType = (DataType) ModelUtils.findIdentifiableElement(model,
            CarnotWorkflowModelPackage.eINSTANCE.getModelType_Data(), dataID);
      assertThat(dataType, is(not(nullValue())));
      assertThat(dataType.getType(), is(not(nullValue())));
      assertThat(dataType.getType().getId(), is("primitive"));
      AttributeType attribute = AttributeUtil
            .getAttribute(dataType, "carnot:engine:type");
      assertThat(attribute, is(not(nullValue())));
      assertThat(attribute.getAttributeValue(), is(primitiveType));
      assertThat(dataType.getName(), is(not(nullValue())));
      assertThat(dataType.getName(), is(dataName));
      return dataType;
   }
   
   public static void assertDataDoesNotExist(ModelType model, String dataID)
   {
      DataType dataType = (DataType) ModelUtils.findIdentifiableElement(model,
            CarnotWorkflowModelPackage.eINSTANCE.getModelType_Data(), dataID);
      assertThat(dataType, is(nullValue()));
   }

   public static RoleType assertRole(ModelType model, String roleID, String roleName)
   {

      RoleType roleType = (RoleType) ModelUtils.findIdentifiableElement(model,
            CarnotWorkflowModelPackage.eINSTANCE.getModelType_Role(), roleID);
      assertThat(roleType, is(not(nullValue())));
      assertThat(roleType.getId(), is(not(nullValue())));
      assertThat(roleType.getName(), is(roleName));
      return roleType;
   }
   
   public static OrganizationType assertOrganization(ModelType model, String orgID, String orgName)
   {

      OrganizationType orgType = (OrganizationType) ModelUtils.findIdentifiableElement(model,
            CarnotWorkflowModelPackage.eINSTANCE.getModelType_Organization(), orgID);
      assertThat(orgType, is(not(nullValue())));
      assertThat(orgType.getId(), is(not(nullValue())));
      assertThat(orgType.getName(), is(orgName));
      return orgType;
   }


   public static DataType assertStructData(ModelType model, String dataID, String dataName,
         String assignedDeclaration)
   {
      DataType dataType = (DataType) ModelUtils.findIdentifiableElement(model,
            CarnotWorkflowModelPackage.eINSTANCE.getModelType_Data(), dataID);
      assertThat(dataType, is(not(nullValue())));
      assertThat(dataType.getType(), is(not(nullValue())));
      assertThat(dataType.getType().getId(), is("struct"));
      AttributeType attribute = AttributeUtil.getAttribute(dataType,
            "carnot:engine:dataType");
      assertThat(attribute, is(not(nullValue())));
      assertThat(attribute.getAttributeValue(), is(assignedDeclaration));
      assertThat(dataType.getName(), is(not(nullValue())));
      assertThat(dataType.getName(), is(dataName));
      return dataType;
   }


   public static DataType assertDocumentData(ModelType model, String dataID, String dataName,
         String assignedDeclaration)
   {
      DataType dataType = (DataType) ModelUtils.findIdentifiableElement(model,
            CarnotWorkflowModelPackage.eINSTANCE.getModelType_Data(), dataID);
      assertThat(dataType, is(not(nullValue())));
      assertThat(dataType.getType(), is(not(nullValue())));
      assertThat(dataType.getType().getId(), is("dmsDocument"));
      AttributeType attribute = AttributeUtil.getAttribute(dataType,
            "carnot:engine:dms:resourceMetadataSchema");
      assertThat(attribute, is(not(nullValue())));
      assertThat(attribute.getAttributeValue(), is(assignedDeclaration));
      assertThat(dataType.getName(), is(not(nullValue())));
      assertThat(dataType.getName(), is(dataName));
      return dataType;
   }

   public static void assertTransition(ActivityType fromActivity, ActivityType toActivity)
   {
      List<TransitionType> outTransitions = fromActivity.getOutTransitions();
      List<TransitionType> inTransitions = toActivity.getInTransitions();
      assertThat(outTransitions, is(not(nullValue())));
      assertThat(inTransitions, is(not(nullValue())));
      assertThat(outTransitions.size(), is(not(0)));
      assertThat(inTransitions.size(), is(not(0)));
      for (Iterator<TransitionType> i = outTransitions.iterator(); i.hasNext();)
      {
         TransitionType transition = i.next();
         assertThat(transition.getCondition(), is(not(nullValue())));
         assertThat(transition.getFrom(), is(fromActivity));
         assertThat(transition.getTo(), is(toActivity));

      }
   }

   public static EventHandlerType assertEventHandler(ActivityType activity, String id,
         String name, String type, boolean logHandler)
   {
      assertThat(activity.getEventHandler(), is(not(nullValue())));
      assertThat(activity.getEventHandler().size(), is(not(0)));
      EventHandlerType foundHandler = null;
      for (Iterator<EventHandlerType> i = activity.getEventHandler().iterator(); i
            .hasNext();)
      {
         EventHandlerType eventHandler = i.next();
         assertThat(eventHandler.getId(), is(not(nullValue())));
         assertThat(eventHandler.getName(), is(not(nullValue())));
         assertThat(eventHandler.getType(), is(not(nullValue())));
         assertThat(eventHandler.getType().getId(), is(not(nullValue())));
         if (eventHandler.getId().equals(id))
         {
            foundHandler = eventHandler;
         }
      }
      assertThat(foundHandler, is(not(nullValue())));
      assertThat(foundHandler.getName(), is(name));
      assertThat(foundHandler.getType().getId(), is(type));
      assertThat(foundHandler.isLogHandler(), is(logHandler));
      return foundHandler;
   }
   
   public static DataPathType assertDataPath(ProcessDefinitionType process, String dataPathID, String dataPathName, DataType dataType, boolean isDescriptor, boolean isKey, DirectionType direction, String expression) 
   {      
      DataPathType foundDataPath = null;
      for (Iterator<DataPathType> i = process.getDataPath().iterator(); i.hasNext(); i
            .hasNext())
      {
         DataPathType dataPath = i.next();
         if (dataPath.getId().equals(dataPathID))
         {
            assertThat(dataPath.getDirection(), is(direction));
            assertThat(dataPath.getName(), is(dataPathName));
            assertThat(dataPath.isDescriptor(), is(isDescriptor));
            assertThat(dataPath.isKey(), is(isKey));
            if (dataType == null)
            {
               assertThat(dataPath.getData(), is(nullValue()));
            }
            else
            {
               assertThat(dataPath.getData(), is(dataType));
            }
            if (expression == null)
            {
               assertThat(dataPath.getDataPath(), is(nullValue()));
            }
            else
            {
               assertThat(dataPath.getDataPath(), is(expression));
            }
            foundDataPath = dataPath;
         }
      }
      assertThat(foundDataPath, is(not(nullValue())));
      return foundDataPath;
   }

   public static void assertReferencedRole(ModelType consumerModel, ModelType providerModel, String roleID, String roleName)
   {
      RoleType role = GenericModelingAssertions.assertRole(consumerModel, roleID, roleName);
      assertProxyReference(providerModel, role);
   }

   public static void assertProxyReference(ModelType providerModel, IIdentifiableElement data)
   {
      assertThat(data.eIsProxy(), is(true));
      assertThat(Proxy.getInvocationHandler(data), is(not(nullValue())));
      assertThat(Proxy.getInvocationHandler(data), instanceOf(EObjectProxyHandler.class));
      EObjectProxyHandler handler = (EObjectProxyHandler) Proxy
            .getInvocationHandler(data);
      assertThat(handler.getSelf(), is(not(nullValue())));
      assertThat(handler.getSelf(), instanceOf(IIdentifiableElement.class));
      assertThat(handler.getTarget(), is(not(nullValue())));
      assertThat(handler.getTarget(), instanceOf(IIdentifiableElement.class));
      IIdentifiableElement self = (IIdentifiableElement) handler.getSelf();
      IIdentifiableElement target = (IIdentifiableElement) handler.getTarget();
      assertThat((ModelType) target.eContainer(), is(equalTo(providerModel)));
      assertThat(self.getId(), is(target.getId()));
   }

   public static AttributeType assertAttribute(IExtensibleElement element, String key,
         String value)
   {
      AttributeType attribute = AttributeUtil.getAttribute(element, key);
      if (value == null)
      {
         assertThat(attribute, is(nullValue()));
         return null;
      }
      assertThat(attribute, is(not(nullValue())));
      assertThat(attribute.getValue(), is(not(nullValue())));
      assertThat(attribute.getValue(), is(value));
      return attribute;
   }

   public static void assertJsonHas(JsonObject json, String... keys)
   {
      for (int i = 0; i < keys.length; i++)
      {
         assertThat(json.has(keys[i]), is(true));
      }
   }

   public static void assertJsonHasKeyValue(JsonObject json, String... keyValues)
   {
      for (int i = 0; i < keyValues.length; i++)
      {
         String keyValue = keyValues[i];
         String key = keyValue.split("=")[0];
         assertThat(json.has(key), is(true));

         String value = keyValue.split("=")[1];
         String containedValue = json.get(key).getAsString();
         assertThat(value, is(containedValue));
      }
   }
   
   public static void assertJsonArrayHasKeyValue(JsonArray jsonArray, String... keyValues)
   {
      for (int i = 0; i < jsonArray.size(); i++)
      {
         JsonObject json = jsonArray.get(i).getAsJsonObject();
         assertJsonHasKeyValue(json, keyValues[i]);
      }
   }
   
   public static AttributeType assertConfigVariableAttributeReference(
         VariableContext context, ModelVariable variable, int refIndex, String value)
   {
      List<EObject> references = context.getReferences(variable);
      EObject element = references.get(refIndex);
      assertThat(element instanceof AttributeType, is(true));
      AttributeType attribute = (AttributeType) element;
      assertThat(attribute.getAttributeValue(), is(value));
      return attribute;
   }
      
   private static Node findValueNodeByLocalName(List<Node> nodes, String annotationName)
   {
      for (Iterator<Node> i = nodes.iterator(); i.hasNext();)
      {
         Node node = i.next();
         String localName = node.getLocalName();
         if (localName.equals(annotationName))
         {
            return node.getFirstChild();
         }
         ;
      }
      return null;
   }

   private static List<Node> getUIAnnotations(Node uiNode)
   {
      List<Node> nodeList = new ArrayList<Node>();
      Node firstChild = uiNode.getFirstChild();
      nodeList.add(firstChild);
      while (firstChild.getNextSibling() != null)
      {
         nodeList.add(firstChild.getNextSibling());
         firstChild = firstChild.getNextSibling();
      }
      return nodeList;
   }

   private static XSDElementDeclaration getElementDeclaration(
         TypeDeclarationType declaration, String name)
   {
      XSDComplexTypeDefinition def = TypeDeclarationUtils.getComplexType(declaration);
      for (Iterator<EObject> i = def.eAllContents(); i.hasNext();)
      {
         EObject object = i.next();
         if (object instanceof XSDElementDeclaration)
         {
            XSDElementDeclaration elementDeclaration = (XSDElementDeclaration) object;
            if (elementDeclaration.getName().equals(name))
            {
               return elementDeclaration;
            }
         }
      }
      return null;
   }



}