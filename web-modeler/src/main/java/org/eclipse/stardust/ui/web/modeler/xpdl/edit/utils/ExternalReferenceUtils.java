/*******************************************************************************
 * Copyright (c) 2011 - 2012 SunGard CSA LLC
 *******************************************************************************/
package org.eclipse.stardust.ui.web.modeler.xpdl.edit.utils;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.xsd.XSDImport;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.core.struct.StructuredDataConstants;
import org.eclipse.stardust.model.xpdl.builder.utils.WebModelerConnectionManager;
import org.eclipse.stardust.model.xpdl.carnot.ActivityImplementationType;
import org.eclipse.stardust.model.xpdl.carnot.ActivityType;
import org.eclipse.stardust.model.xpdl.carnot.ApplicationType;
import org.eclipse.stardust.model.xpdl.carnot.AttributeType;
import org.eclipse.stardust.model.xpdl.carnot.DataType;
import org.eclipse.stardust.model.xpdl.carnot.IExtensibleElement;
import org.eclipse.stardust.model.xpdl.carnot.IIdentifiableModelElement;
import org.eclipse.stardust.model.xpdl.carnot.IModelParticipant;
import org.eclipse.stardust.model.xpdl.carnot.IdRef;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.model.xpdl.carnot.ProcessDefinitionType;
import org.eclipse.stardust.model.xpdl.carnot.util.AttributeUtil;
import org.eclipse.stardust.model.xpdl.carnot.util.ModelUtils;
import org.eclipse.stardust.model.xpdl.util.IConnection;
import org.eclipse.stardust.model.xpdl.util.IConnectionManager;
import org.eclipse.stardust.model.xpdl.xpdl2.ExtendedAttributeType;
import org.eclipse.stardust.model.xpdl.xpdl2.Extensible;
import org.eclipse.stardust.model.xpdl.xpdl2.ExternalPackage;
import org.eclipse.stardust.model.xpdl.xpdl2.ExternalPackages;
import org.eclipse.stardust.model.xpdl.xpdl2.ExternalReferenceType;
import org.eclipse.stardust.model.xpdl.xpdl2.TypeDeclarationType;
import org.eclipse.stardust.model.xpdl.xpdl2.util.ExtendedAttributeUtil;
import org.eclipse.stardust.modeling.repository.common.Connection;

public class ExternalReferenceUtils
{
   public static List<EObject> getExternalReferences(ModelType model,
         Connection connection)
   {
      String importString = connection.getAttribute("importByReference"); //$NON-NLS-1$
      if (importString != null && importString.equalsIgnoreCase("false")) //$NON-NLS-1$
      {
         return Collections.emptyList();
      }
      IConnectionManager connectionManager = model.getConnectionManager();
      String connectionId = connection.getId();
      List<EObject> list = CollectionUtils.newList();
      for (Iterator<EObject> i = model.eAllContents(); i.hasNext();)
      {
         EObject modelElement = i.next();
         checkExtensible(connectionManager, connectionId, list, modelElement);
         // (fh) special case, imports in embedded schemas
         if (modelElement instanceof XSDImport)
         {
            String location = ((XSDImport) modelElement).getSchemaLocation();
            if (location != null
                  && location.startsWith(StructuredDataConstants.URN_INTERNAL_PREFIX))
            {
               QName qname = QName.valueOf(location
                     .substring(StructuredDataConstants.URN_INTERNAL_PREFIX.length()));
               String namespace = qname.getNamespaceURI();
               if (!XMLConstants.NULL_NS_URI.equals(namespace)
                     && !namespace.equals(model.getId()))
               {
                  ExternalPackages packs = model.getExternalPackages();
                  if (packs != null)
                  {
                     ExternalPackage pack = packs.getExternalPackage(namespace);
                     if (pack != null)
                     {
                        String uri = ExtendedAttributeUtil.getAttributeValue(pack,
                              IConnectionManager.URI_ATTRIBUTE_NAME);
                        checkConnectionUsed(connectionManager, list, connectionId,
                              modelElement, uri == null ? null : URI.createURI(uri));
                     }
                  }
               }
            }
         }
      }
      return list;
   }

   private static void checkExtensible(IConnectionManager connectionManager,
         String connectionId, List<EObject> list, EObject modelElement)
   {
      URI connectionUri = null;
      if (modelElement.eIsProxy())
      {
         URI proxyUri = ((InternalEObject) modelElement).eProxyURI();
         if (IConnectionManager.SCHEME.equals(proxyUri.scheme()))
         {
            connectionUri = proxyUri.trimSegments(proxyUri.segmentCount());
         }
      }
      else
      {
         String uri = null;
         if (modelElement instanceof IExtensibleElement)
         {
            uri = AttributeUtil.getAttributeValue((IExtensibleElement) modelElement,
                  IConnectionManager.URI_ATTRIBUTE_NAME);
         }
         else if (modelElement instanceof Extensible)
         {
            uri = ExtendedAttributeUtil.getAttributeValue((Extensible) modelElement,
                  IConnectionManager.URI_ATTRIBUTE_NAME);
         }
         if (uri != null)
         {
            connectionUri = URI.createURI(uri);
         }
      }
      checkConnectionUsed(connectionManager, list, connectionId, modelElement,
            connectionUri);
   }

   private static void checkConnectionUsed(IConnectionManager connectionManager,
         List<EObject> list, String connectionId, EObject modelElement, URI connectionUri)
   {
      if (connectionUri != null)
      {
         IConnection refConnection = connectionManager.findConnection(connectionUri);
         if (refConnection != null && connectionId.equals(refConnection.getId()))
         {
            list.add(modelElement);
         }
      }
   }

   public static void checkExternalReferences(Map<String, ModelType> models,
         ModelType model)
   {
      List<String> uris = ModelUtils.getURIsForExternalPackages(model);
      for (Iterator<String> i = uris.iterator(); i.hasNext();)
      {
         String uri = i.next();
         WebModelerConnectionManager cm = (WebModelerConnectionManager) model
               .getConnectionManager();
         Connection connection = (Connection) cm.findConnection(uri);
         List<EObject> references = getExternalReferences(model, (Connection) connection);
         ModelType modelType = ModelUtils.getReferencedModelByURI(model, uri);
         ModelType refModel = models.get(modelType.getId());
         for (Iterator<EObject> j = references.iterator(); j.hasNext();)
         {
            EObject ref = j.next();
            if (ref instanceof ActivityType)
            {
               checkActivity((ActivityType) ref, refModel);
            }
            if (ref instanceof DataType)
            {
               checkData((DataType) ref, refModel);
            }
            if (ref instanceof IModelParticipant)
            {
               checkParticipant((IModelParticipant) ref, refModel);
            }
            if (ref instanceof ActivityType)
            {
               checkActivity((ActivityType) ref, refModel);
            }
         }
      }
   }


   private static boolean checkParticipant(IModelParticipant ref, ModelType refModel)
   {

      return false;
   }

   private static void checkData(DataType data, ModelType refModel)
   {
      if (data.getType().getId().equals("struct"))
      {
         ExternalReferenceType ref = data.getExternalReference();
         if (ref != null && ref.getUuid() != null)
         {
            TypeDeclarationType declaration = findTypeDeclarationModelUUID(refModel,
                  ref.getUuid());
            if (declaration != null)
            {
               if (!declaration.getId().equals(ref.getXref()))
               {
                  ref.setXref(declaration.getId());
                  AttributeType uriAttribute = AttributeUtil.getAttribute(
                        (IExtensibleElement) data, "carnot:connection:uri");
                  if (uriAttribute != null)
                  {
                     String uri = uriAttribute.getAttributeValue();
                     uri = uri.substring(0, uri.lastIndexOf("/")) + "/"
                           + declaration.getId();
                     AttributeUtil.setAttribute((IExtensibleElement) data,
                           "carnot:connection:uri", uri);
                  }
               }
            }
         }
      }
   }

   private static void checkActivity(ActivityType activity, ModelType refModel)
   {

      if (activity.getImplementation().getLiteral()
            .equals(ActivityImplementationType.SUBPROCESS_LITERAL.getLiteral()))
      {
         checkSubprocessActivity(activity, refModel);
      }

      if (activity.getImplementation().getLiteral()
            .equals(ActivityImplementationType.APPLICATION_LITERAL.getLiteral()))
      {
         checkApplicationActivity(activity, refModel);
      }

   }

   private static void checkSubprocessActivity(ActivityType activity, ModelType refModel)
   {
      AttributeType uuidAttribute = AttributeUtil.getAttribute(
            (IIdentifiableModelElement) activity, "carnot:connection:uuid");
      if (uuidAttribute != null)
      {
         ProcessDefinitionType process = findProcessByModelUUID(refModel,
               uuidAttribute.getAttributeValue());
         if (process != null)
         {
            IdRef externalReference = activity.getExternalRef();
            if (!externalReference.getRef().equals(process.getId()))
            {
               externalReference.setRef(process.getId());
               AttributeType uriAttribute = AttributeUtil.getAttribute(
                     (IExtensibleElement) activity, "carnot:connection:uri");
               if (uriAttribute != null)
               {
                  String uri = uriAttribute.getAttributeValue();
                  uri = uri.substring(0, uri.lastIndexOf("/")) + "/"
                        + process.getId();
                  AttributeUtil.setAttribute((IExtensibleElement) activity,
                        "carnot:connection:uri", uri);
               }
            }
         }
      }
   }

   private static void checkApplicationActivity(ActivityType activity, ModelType refModel)
   {
      AttributeType uuidAttribute = AttributeUtil.getAttribute(
            (IIdentifiableModelElement) activity, "carnot:connection:uuid");
      if (uuidAttribute != null)
      {
         ApplicationType application = findApplicationByModelUUID(refModel,
               uuidAttribute.getAttributeValue());
         if (application != null)
         {
            IdRef externalReference = activity.getExternalRef();
            if (!externalReference.getRef().equals(application.getId()))
            {
               externalReference.setRef(application.getId());
               AttributeType uriAttribute = AttributeUtil.getAttribute(
                     (IExtensibleElement) activity, "carnot:connection:uri");
               if (uriAttribute != null)
               {
                  String uri = uriAttribute.getAttributeValue();
                  uri = uri.substring(0, uri.lastIndexOf("/")) + "/"
                        + application.getId();
                  AttributeUtil.setAttribute((IExtensibleElement) activity,
                        "carnot:connection:uri", uri);
               }
            }
         }
      }
   }

   private static ApplicationType findApplicationByModelUUID(ModelType refModel,
         String uuid)
   {
      for (Iterator<ApplicationType> i = refModel.getApplication().iterator(); i
            .hasNext();)
      {
         ApplicationType application = i.next();
         AttributeType uuidAttribute = AttributeUtil.getAttribute(
               (IIdentifiableModelElement) application, "carnot:model:uuid");
         if (uuidAttribute != null)
         {
            if (uuidAttribute.getAttributeValue().equals(uuid))
            {
               return application;
            }
         }
      }
      return null;
   }

   private static ProcessDefinitionType findProcessByModelUUID(ModelType refModel,
         String uuid)
   {
      for (Iterator<ProcessDefinitionType> i = refModel.getProcessDefinition().iterator(); i
            .hasNext();)
      {
         ProcessDefinitionType process = i.next();
         AttributeType uuidAttribute = AttributeUtil.getAttribute(
               (IIdentifiableModelElement) process, "carnot:model:uuid");
         if (uuidAttribute != null)
         {
            if (uuidAttribute.getAttributeValue().equals(uuid))
            {
               return process;
            }
         }
      }
      return null;
   }

   private static TypeDeclarationType findTypeDeclarationModelUUID(ModelType refModel,
         String uuid)
   {
      for (Iterator<TypeDeclarationType> i = refModel.getTypeDeclarations()
            .getTypeDeclaration().iterator(); i.hasNext();)
      {
         TypeDeclarationType declaration = i.next();
         ExtendedAttributeType uuidAttribute = ExtendedAttributeUtil.getAttribute(
               declaration.getExtendedAttributes(), "carnot:model:uuid");
         if (uuidAttribute != null)
         {
            if (uuidAttribute.getValue().equals(uuid))
            {
               return declaration;
            }
         }
      }
      return null;
   }

}
