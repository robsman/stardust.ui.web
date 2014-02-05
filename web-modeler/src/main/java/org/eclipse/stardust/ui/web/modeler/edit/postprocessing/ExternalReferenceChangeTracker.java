package org.eclipse.stardust.ui.web.modeler.edit.postprocessing;

import java.util.*;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.change.impl.ChangeDescriptionImpl;
import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.core.struct.StructuredDataConstants;
import org.eclipse.stardust.model.xpdl.builder.session.Modification;
import org.eclipse.stardust.model.xpdl.builder.utils.WebModelerConnectionManager;
import org.eclipse.stardust.model.xpdl.carnot.DataType;
import org.eclipse.stardust.model.xpdl.carnot.IExtensibleElement;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.model.xpdl.carnot.util.AttributeUtil;
import org.eclipse.stardust.model.xpdl.carnot.util.ModelUtils;
import org.eclipse.stardust.model.xpdl.util.IConnection;
import org.eclipse.stardust.model.xpdl.util.IConnectionManager;
import org.eclipse.stardust.model.xpdl.xpdl2.Extensible;
import org.eclipse.stardust.model.xpdl.xpdl2.ExternalPackage;
import org.eclipse.stardust.model.xpdl.xpdl2.ExternalPackages;
import org.eclipse.stardust.model.xpdl.xpdl2.util.ExtendedAttributeUtil;
import org.eclipse.stardust.modeling.repository.common.Connection;
import org.eclipse.stardust.modeling.repository.common.impl.ConnectionImpl;
import org.eclipse.stardust.ui.web.modeler.edit.spi.ChangePostprocessor;
import org.eclipse.xsd.XSDImport;
import org.springframework.stereotype.Component;

@Component
public class ExternalReferenceChangeTracker implements ChangePostprocessor
{
   @Override
   public int getInspectionPhase()
   {
      return 100;
   }

   @Override
   public void inspectChange(Modification change)
   {
      Collection<EObject> removedList = change.getRemovedElements();
      Collection<EObject> modifiedList = change.getModifiedElements();

      if (!removedList.isEmpty())
      {
         EObject removedElement = (EObject) removedList.toArray()[0];
         if (removedElement.eContainer() instanceof ChangeDescriptionImpl)
         {
            ChangeDescriptionImpl changeDescription = (ChangeDescriptionImpl) removedElement.eContainer();
            EObject container = changeDescription.getOldContainer(removedElement);
            trackExternalReferences(change, container);
         }
      }

      if (!modifiedList.isEmpty())
      {
         EObject modifiedElement = (EObject) modifiedList.toArray()[0];
         trackExternalReferences(change, modifiedElement);
      }
   }

   private void trackExternalReferences(Modification change, EObject container)
   {
      ModelType model = ModelUtils.findContainingModel(container);
      if (model == null)
      {
         if (container instanceof DataType)
         {
            DataType dataType = (DataType) container;
            if (dataType.getType() != null)
            {
               model = ModelUtils.findContainingModel(dataType.getType());
            }
         }
      }
      if (model == null)
      {
         return;
      }
      // (fh) TODO: refactor and implement correctly
      // 1. track usages of ExternalPackages, if none found remove them
      // 2. track usages of [by reference] Connections, if none found remove them
      List<String> uris = ModelUtils.getURIsForExternalPackages(model);
      for (Iterator<String> i = uris.iterator(); i.hasNext();)
      {
         String uri = i.next();
         WebModelerConnectionManager cm = (WebModelerConnectionManager) model.getConnectionManager();
         Connection connection = (Connection) cm.findConnection(uri);
         List<EObject> references = this.getExternalReferences(model,
               (Connection) connection);
         if (references.size() == 1)
         {
            ExternalPackage externalReference = (ExternalPackage) references.get(0);
            removeConnection(externalReference);
            model.getExternalPackages().getExternalPackage().remove(externalReference);
            change.markAlsoRemoved(externalReference);
            change.markAlsoModified(model);
         }
      }
   }

   private void removeConnection(ExternalPackage externalPackage)
   {
      ModelType model = ModelUtils.findContainingModel(externalPackage);
      String uri = ExtendedAttributeUtil.getAttributeValue(externalPackage,
            IConnectionManager.URI_ATTRIBUTE_NAME);
      ConnectionImpl connection = (ConnectionImpl) model.getConnectionManager()
            .findConnection(uri);
      WebModelerConnectionManager man = (WebModelerConnectionManager) model.getConnectionManager();
      man.getRepository().getConnection().remove(connection);
   }

   public List<EObject> getExternalReferences(ModelType model, Connection connection)
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
            if (location != null && location.startsWith(StructuredDataConstants.URN_INTERNAL_PREFIX))
            {
               QName qname = QName.valueOf(location.substring(StructuredDataConstants.URN_INTERNAL_PREFIX.length()));
               String namespace = qname.getNamespaceURI();
               if (!XMLConstants.NULL_NS_URI.equals(namespace) && !namespace.equals(model.getId()))
               {
                  ExternalPackages packs = model.getExternalPackages();
                  if (packs != null)
                  {
                     ExternalPackage pack = packs.getExternalPackage(namespace);
                     if (pack != null)
                     {
                        String uri = ExtendedAttributeUtil.getAttributeValue(
                              pack, IConnectionManager.URI_ATTRIBUTE_NAME);
                        checkConnectionUsed(connectionManager, list, connectionId, modelElement,
                              uri == null ? null : URI.createURI(uri));
                     }
                  }
               }
            }
         }
      }
      return list;
   }

   private void checkExtensible(IConnectionManager connectionManager, String connectionId, List<EObject> list,
         EObject modelElement)
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
            uri = AttributeUtil.getAttributeValue(
                  (IExtensibleElement) modelElement,
                  IConnectionManager.URI_ATTRIBUTE_NAME);
         }
         else if (modelElement instanceof Extensible)
         {
            uri = ExtendedAttributeUtil.getAttributeValue(
                  (Extensible) modelElement, IConnectionManager.URI_ATTRIBUTE_NAME);
         }
         if (uri != null)
         {
            connectionUri = URI.createURI(uri);
         }
      }
      checkConnectionUsed(connectionManager, list, connectionId, modelElement, connectionUri);
   }

   private void checkConnectionUsed(IConnectionManager connectionManager, List<EObject> list,
         String connectionId, EObject modelElement, URI connectionUri)
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
}
