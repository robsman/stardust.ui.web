package org.eclipse.stardust.ui.web.modeler.edit.postprocessing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.change.impl.ChangeDescriptionImpl;
import org.eclipse.stardust.model.xpdl.builder.session.Modification;
import org.eclipse.stardust.model.xpdl.builder.utils.WebModelerConnectionManager;
import org.eclipse.stardust.model.xpdl.carnot.IExtensibleElement;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.model.xpdl.carnot.util.AttributeUtil;
import org.eclipse.stardust.model.xpdl.carnot.util.ModelUtils;
import org.eclipse.stardust.model.xpdl.util.IConnectionManager;
import org.eclipse.stardust.model.xpdl.xpdl2.Extensible;
import org.eclipse.stardust.model.xpdl.xpdl2.ExternalPackage;
import org.eclipse.stardust.model.xpdl.xpdl2.util.ExtendedAttributeUtil;
import org.eclipse.stardust.modeling.repository.common.Connection;
import org.eclipse.stardust.modeling.repository.common.impl.ConnectionImpl;
import org.eclipse.stardust.ui.web.modeler.edit.spi.ChangePostprocessor;
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

      ModelType model = null;

      if (!removedList.isEmpty())
      {
         EObject removedElement = (EObject) removedList.toArray()[0];
         if (removedElement.eContainer() instanceof ChangeDescriptionImpl)
         {
            ChangeDescriptionImpl changeDescription = (ChangeDescriptionImpl) removedElement
                  .eContainer();
            EObject container = changeDescription.getOldContainer(removedElement);
            model = ModelUtils.findContainingModel(container);
            List<String> uris = ModelUtils.getURIsForExternalPackages(model);
            for (Iterator<String> i = uris.iterator(); i.hasNext();)
            {
               String uri = i.next();
               WebModelerConnectionManager cm = (WebModelerConnectionManager) model
                     .getConnectionManager();
               Connection connection = (Connection) cm.findConnection(uri);
               List<EObject> references = this.getExternalReferences(model,
                     (Connection) connection);
               if (references.size() == 1)
               {
                  ExternalPackage externalReference = (ExternalPackage) references.get(0);
                  removeConnection(externalReference);
                  model.getExternalPackages().getExternalPackage()
                        .remove(externalReference);
                  change.markAlsoRemoved(externalReference);
                  change.markAlsoModified(model);
               }
            }
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
      WebModelerConnectionManager man = (WebModelerConnectionManager) model
            .getConnectionManager();
      man.getRepository().getConnection().remove(connection);
   }

   public List<EObject> getExternalReferences(ModelType model, Connection connection)
   {
      List<EObject> list = new ArrayList<EObject>();
      for (Iterator<EObject> i = model.eAllContents(); i.hasNext();)
      {
         EObject modelElement = i.next();
         if (modelElement != null)
         {
            if (modelElement instanceof IExtensibleElement)
            {
               if (AttributeUtil.getAttributeValue((IExtensibleElement) modelElement,
                     IConnectionManager.URI_ATTRIBUTE_NAME) != null)
               {
                  String uri = AttributeUtil.getAttributeValue(
                        (IExtensibleElement) modelElement,
                        IConnectionManager.URI_ATTRIBUTE_NAME);
                  Connection refConnection = (Connection) model.getConnectionManager()
                        .findConnection(uri);
                  if (refConnection != null)
                  {
                     String importString = connection.getAttribute("importByReference"); //$NON-NLS-1$
                     if (importString != null && importString.equalsIgnoreCase("false")) //$NON-NLS-1$
                     {

                     }
                     else
                     {
                        if (connection.getId().equals(refConnection.getId()))
                        {
                           list.add(modelElement);
                        }
                     }
                  }
               }
            }
            if (modelElement instanceof Extensible)
            {
               if (ExtendedAttributeUtil.getAttributeValue((Extensible) modelElement,
                     IConnectionManager.URI_ATTRIBUTE_NAME) != null)
               {
                  String uri = ExtendedAttributeUtil.getAttributeValue(
                        (Extensible) modelElement, IConnectionManager.URI_ATTRIBUTE_NAME);
                  Connection refConnection = (Connection) model.getConnectionManager()
                        .findConnection(uri);
                  if (refConnection != null)
                  {
                     String importString = connection.getAttribute("importByReference"); //$NON-NLS-1$
                     if (importString != null && importString.equalsIgnoreCase("false")) //$NON-NLS-1$
                     {

                     }
                     else
                     {
                        if (connection.getId().equals(refConnection.getId()))
                        {
                           list.add(modelElement);
                        }
                     }
                  }

               }
            }

         }

      }
      return list;

   }

}
