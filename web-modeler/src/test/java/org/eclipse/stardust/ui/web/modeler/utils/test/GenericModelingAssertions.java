package org.eclipse.stardust.ui.web.modeler.utils.test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.Iterator;
import java.util.List;

import org.eclipse.stardust.model.xpdl.carnot.AccessPointType;
import org.eclipse.stardust.model.xpdl.carnot.ActivityImplementationType;
import org.eclipse.stardust.model.xpdl.carnot.ActivityType;
import org.eclipse.stardust.model.xpdl.carnot.ApplicationContextTypeType;
import org.eclipse.stardust.model.xpdl.carnot.ApplicationType;
import org.eclipse.stardust.model.xpdl.carnot.AttributeType;
import org.eclipse.stardust.model.xpdl.carnot.CarnotWorkflowModelPackage;
import org.eclipse.stardust.model.xpdl.carnot.ContextType;
import org.eclipse.stardust.model.xpdl.carnot.DataMappingType;
import org.eclipse.stardust.model.xpdl.carnot.DataType;
import org.eclipse.stardust.model.xpdl.carnot.DirectionType;
import org.eclipse.stardust.model.xpdl.carnot.IAccessPointOwner;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.model.xpdl.carnot.ProcessDefinitionType;
import org.eclipse.stardust.model.xpdl.carnot.RoleType;
import org.eclipse.stardust.model.xpdl.carnot.extensions.FormalParameterMappingsType;
import org.eclipse.stardust.model.xpdl.carnot.util.AttributeUtil;
import org.eclipse.stardust.model.xpdl.carnot.util.ModelUtils;
import org.eclipse.stardust.model.xpdl.xpdl2.ExtendedAttributeType;
import org.eclipse.stardust.model.xpdl.xpdl2.FormalParameterType;
import org.eclipse.stardust.model.xpdl.xpdl2.FormalParametersType;
import org.eclipse.stardust.model.xpdl.xpdl2.TypeDeclarationType;
import org.eclipse.stardust.model.xpdl.xpdl2.util.ExtendedAttributeUtil;

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

   public static void assertDataMapping(ActivityType activity, String dataMappingID,
         String accssPointID, String context, DirectionType direction, DataType data)
   {
      DataMappingType dataMapping = (DataMappingType) ModelUtils.findIdentifiableElement(activity,
            CarnotWorkflowModelPackage.eINSTANCE.getActivityType_DataMapping(), dataMappingID);
      assertThat(dataMapping, is(not(nullValue())));
      assertThat(dataMapping.getApplicationAccessPoint(), is(not(nullValue())));
      assertThat(dataMapping.getApplicationAccessPoint(), is(accssPointID));
      assertThat(dataMapping.getContext(), is(not(nullValue())));
      assertThat(dataMapping.getContext(), is(context));
      assertThat(dataMapping.getDirection(), is(not(nullValue())));
      assertThat(dataMapping.getDirection(), is(direction));
      assertThat(dataMapping.getData(), is(not(nullValue())));
      assertThat(dataMapping.getData(), is(data));


   }

   public static void assertProcessInterface(ModelType model, String interfaceID, String interfaceName,
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
      ExtendedAttributeType extAttribute = ExtendedAttributeUtil.getAttribute(typeDeclaration.getExtendedAttributes(),  "carnot:model:uuid");
      assertThat(extAttribute, is(not(nullValue())));
      assertThat(extAttribute.getValue(), is(not(nullValue())));
      return typeDeclaration;
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

   public static RoleType assertRole(ModelType model, String roleID, String roleName)
   {

      RoleType roleType = (RoleType) ModelUtils.findIdentifiableElement(model,
            CarnotWorkflowModelPackage.eINSTANCE.getModelType_Role(), roleID);
      assertThat(roleType, is(not(nullValue())));
      assertThat(roleType.getId(), is(not(nullValue())));
      assertThat(roleType.getName(), is(roleName));
      return roleType;
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





}
