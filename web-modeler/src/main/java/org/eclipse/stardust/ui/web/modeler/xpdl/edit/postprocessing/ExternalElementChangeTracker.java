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

package org.eclipse.stardust.ui.web.modeler.xpdl.edit.postprocessing;

import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.change.impl.ChangeDescriptionImpl;
import org.springframework.stereotype.Component;

import org.eclipse.stardust.model.xpdl.builder.session.Modification;
import org.eclipse.stardust.model.xpdl.carnot.*;
import org.eclipse.stardust.model.xpdl.carnot.impl.DataSymbolTypeImpl;
import org.eclipse.stardust.model.xpdl.carnot.util.AttributeUtil;
import org.eclipse.stardust.model.xpdl.carnot.util.ModelUtils;
import org.eclipse.stardust.model.xpdl.carnot.util.UnusedModelElementsSearcher;
import org.eclipse.stardust.model.xpdl.util.IConnectionManager;
import org.eclipse.stardust.model.xpdl.xpdl2.Extensible;
import org.eclipse.stardust.model.xpdl.xpdl2.util.ExtendedAttributeUtil;
import org.eclipse.stardust.modeling.repository.common.Connection;
import org.eclipse.stardust.ui.web.modeler.edit.spi.ChangePostprocessor;

/**
 * @author Barry.Grotjahn
 *
 */
@Component
public class ExternalElementChangeTracker implements ChangePostprocessor
{
   @Override
   public int getInspectionPhase()
   {
      return 700;
   }

   @Override
   public void inspectChange(Modification change)
   {
      for (EObject candidate : change.getRemovedElements())
      {
         if (candidate instanceof DataSymbolTypeImpl)
         {
            for (EObject dataCandidate : change.getModifiedElements())
            {
               if (dataCandidate instanceof DataType)
               {
                  trackDataModification((DataType) dataCandidate, change);
               }
            }
         }
         else
         {
            trackModification(candidate, true, change);
         }
      }
      for (EObject candidate : change.getModifiedElements())
      {
         if (!(candidate instanceof INodeSymbol)) 
         {
            trackModification(candidate, false, change);   
         }         
      }

   }

   private void trackDataModification(DataType dataType, Modification change)
   {
      ModelType model = ModelUtils.findContainingModel(dataType);
      if (isExternalReference(dataType))
      {
         UnusedModelElementsSearcher searcher = new UnusedModelElementsSearcher();
         @SuppressWarnings("unchecked")
         Map<ModelType, List<EObject>> matchedElements = searcher.search(model);
         List<EObject> children = matchedElements.get(model);
         if (children != null && children.contains(dataType))
         {
            List<INodeSymbol> symbols = dataType.getSymbols();
            if (symbols.size() == 0)
            {
               model.getData().remove(dataType);
               change.markAlsoModified(model);
               change.markAlsoRemoved(dataType, true);
            }
         }
      }
   }

   private void trackModification(EObject candidate, boolean removed, Modification change)
   {
      ModelType model = ModelUtils.findContainingModel(candidate);
      if (removed)
      {
         if (candidate.eContainer() instanceof ChangeDescriptionImpl)
         {
            ChangeDescriptionImpl changeDescription = (ChangeDescriptionImpl) candidate.eContainer();
            EObject container = changeDescription.getOldContainer(candidate);
            model = ModelUtils.findContainingModel(container);
         }
      }

      if (model != null)
      {
         UnusedModelElementsSearcher searcher = new UnusedModelElementsSearcher();
         @SuppressWarnings("unchecked")
         Map<ModelType, List<EObject>> matchedElements = searcher.search(model);
         List<EObject> children = matchedElements.get(model);
         if (children != null)
         {
            boolean modified = false;

            for (EObject element : children)
            {
               if (isExternalReference(element))
               {
                  if (element instanceof IIdentifiableModelElement)
                  {
                     if (((IIdentifiableModelElement) element).getSymbols().isEmpty())
                     {
                        if (candidate instanceof ProcessDefinitionType
                              && element instanceof DataType)
                        {
                           model.getData().remove(element);
                           change.markAlsoRemoved(element);
                           modified = true;
                        }
                        else if (candidate instanceof DataType
                              && element instanceof DataType)
                        {
                           model.getData().remove(element);
                           modified = true;
                        }
                        if (modified)
                        {
                           change.markAlsoModified(element);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public boolean isExternalReference(EObject modelElement)
   {
      if (modelElement != null)
      {
         if (modelElement instanceof IModelParticipant)
         {
            IModelParticipant participtant = (IModelParticipant) modelElement;
            if (participtant.eIsProxy())
            {
               return true;
            }
         }
         if (modelElement instanceof DataType)
         {
            DataType dataType = (DataType) modelElement;
            if (dataType.eIsProxy())
            {
               return true;
            }
         }
         else if (modelElement instanceof IExtensibleElement)
         {
            if (AttributeUtil.getAttributeValue((IExtensibleElement) modelElement,
                  IConnectionManager.URI_ATTRIBUTE_NAME) != null)
            {
               String uri = AttributeUtil.getAttributeValue(
                     (IExtensibleElement) modelElement,
                     IConnectionManager.URI_ATTRIBUTE_NAME);
               ModelType model = ModelUtils.findContainingModel(modelElement);
               if (model == null)
               {
                  return false;
               }
               Connection connection = (Connection) model.getConnectionManager()
                     .findConnection(uri);
               if (connection != null)
               {
                  String importString = connection.getAttribute("importByReference"); //$NON-NLS-1$
                  if (importString != null && importString.equalsIgnoreCase("false")) //$NON-NLS-1$
                  {
                     return false;
                  }
               }
               return true;
            }
         }
         else if (modelElement instanceof Extensible)
         {
            if (ExtendedAttributeUtil.getAttributeValue((Extensible) modelElement,
                  IConnectionManager.URI_ATTRIBUTE_NAME) != null)
            {
               String uri = ExtendedAttributeUtil.getAttributeValue(
                     (Extensible) modelElement, IConnectionManager.URI_ATTRIBUTE_NAME);
               ModelType model = ModelUtils.findContainingModel(modelElement);
               if (model == null)
               {
                  return false;
               }
               Connection connection = (Connection) model.getConnectionManager()
                     .findConnection(uri);
               if (connection != null)
               {
                  String importString = connection.getAttribute("importByReference"); //$NON-NLS-1$
                  if (importString != null && importString.equalsIgnoreCase("false")) //$NON-NLS-1$
                  {
                     return false;
                  }
               }
               return true;
            }
         }
      }
      return false;
   }

   public static boolean isUsedInActivity(IModelParticipant participant)
   {
      ModelType model = ModelUtils.findContainingModel(participant);
      for(ProcessDefinitionType process : model.getProcessDefinition())
      {
         List<ActivityType> activities = process.getActivity();
         for (ActivityType activity : activities)
         {
            if(activity.getPerformer() != null)
            {
               if(activity.getPerformer().equals(participant))
               {
                  //return true;
               }
            }
            if(activity.getQualityControlPerformer() != null)
            {
               if(activity.getQualityControlPerformer().equals(participant))
               {
                  return true;
               }
            }
         }
      }

      return false;
   }
}