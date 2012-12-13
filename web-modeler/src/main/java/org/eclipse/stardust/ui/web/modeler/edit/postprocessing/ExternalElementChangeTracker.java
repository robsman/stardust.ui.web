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

package org.eclipse.stardust.ui.web.modeler.edit.postprocessing;

import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.change.impl.ChangeDescriptionImpl;
import org.springframework.stereotype.Component;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.model.xpdl.builder.session.Modification;
import org.eclipse.stardust.model.xpdl.builder.utils.LaneParticipantUtil;
import org.eclipse.stardust.model.xpdl.carnot.ConditionalPerformerType;
import org.eclipse.stardust.model.xpdl.carnot.DataType;
import org.eclipse.stardust.model.xpdl.carnot.IExtensibleElement;
import org.eclipse.stardust.model.xpdl.carnot.IIdentifiableModelElement;
import org.eclipse.stardust.model.xpdl.carnot.IModelParticipant;
import org.eclipse.stardust.model.xpdl.carnot.INodeSymbol;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.model.xpdl.carnot.OrganizationType;
import org.eclipse.stardust.model.xpdl.carnot.RoleType;
import org.eclipse.stardust.model.xpdl.carnot.util.AttributeUtil;
import org.eclipse.stardust.model.xpdl.carnot.util.ModelUtils;
import org.eclipse.stardust.model.xpdl.carnot.util.UnusedModelElementsSearcher;
import org.eclipse.stardust.model.xpdl.util.IConnectionManager;
import org.eclipse.stardust.model.xpdl.xpdl2.Extensible;
import org.eclipse.stardust.model.xpdl.xpdl2.util.ExtendedAttributeUtil;
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
      return 10;
   }

   @Override
   public void inspectChange(Modification change)
   {
      for (EObject candidate : change.getRemovedElements())
      {
         trackModification(candidate, true, change);
      }
      for (EObject candidate : change.getModifiedElements())
      {
         trackModification(candidate, false, change);
      }
   }

   private void trackModification(EObject candidate, boolean removed, Modification change)
   {
      ModelType model = ModelUtils.findContainingModel(candidate);
      if(removed)
      {
         if (candidate.eContainer() instanceof ChangeDescriptionImpl)
         {
            ChangeDescriptionImpl changeDescription = (ChangeDescriptionImpl) candidate.eContainer();
            EObject container = changeDescription.getOldContainer(candidate);
            model = ModelUtils.findContainingModel(container);
         }
      }

      if(model != null)
      {
         if(candidate instanceof DataType && !removed)
         {
            String id = checkIsExternalReference(candidate);
            if(!StringUtils.isEmpty(id))
            {
               UnusedModelElementsSearcher searcher = new UnusedModelElementsSearcher();
               Map matchedElements = searcher.search(model);
               List children = (List) matchedElements.get(model);
               if(children != null && children.contains(candidate))
               {
                  EList<INodeSymbol> symbols = ((DataType) candidate).getSymbols();
                  if(symbols.size() == 0)
                  {
                     model.getData().remove(candidate);
                     change.markAlsoModified(candidate);
                  }
               }
            }
         }

         UnusedModelElementsSearcher searcher = new UnusedModelElementsSearcher();
         Map matchedElements = searcher.search(model);
         List<EObject> children = (List) matchedElements.get(model);
         if(children != null)
         {
            boolean modified = false;

            for(EObject element : children)
            {
               String id = checkIsExternalReference(element);
               if(!StringUtils.isEmpty(id))
               {
                  if(element instanceof IIdentifiableModelElement)
                  {
                     if(((IIdentifiableModelElement) element).getSymbols().isEmpty())
                     {
                        if(element instanceof RoleType && !LaneParticipantUtil.isUsedInLane((IModelParticipant) element))
                        {
                           model.getRole().remove(element);
                           modified = true;
                        }
                        else if(element instanceof ConditionalPerformerType && !LaneParticipantUtil.isUsedInLane((IModelParticipant) element))
                        {
                           model.getConditionalPerformer().remove(element);
                           modified = true;
                        }
                        else if(element instanceof OrganizationType && !LaneParticipantUtil.isUsedInLane((IModelParticipant) element))
                        {
                           model.getOrganization().remove(element);
                           modified = true;
                        }
                        else if(element instanceof DataType)
                        {
                           //TODO:Investigate - related to delete problem for external Data
                           //model.getData().remove(element);
                           //modified = true;
                        }
                        if(modified)
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

   public String checkIsExternalReference(EObject modelElement)
   {
      String uri = null;
      if (modelElement instanceof Extensible)
      {
         uri = ExtendedAttributeUtil.getAttributeValue((Extensible) modelElement, IConnectionManager.URI_ATTRIBUTE_NAME);
      }
      else if (modelElement instanceof IExtensibleElement)
      {
         uri = AttributeUtil.getAttributeValue((IExtensibleElement) modelElement, IConnectionManager.URI_ATTRIBUTE_NAME);
      }
      if(uri != null)
      {
         URI createURI = URI.createURI(uri);
         if (IConnectionManager.SCHEME.equals(createURI.scheme()))
         {
            return createURI.authority();
         }
      }

      return null;
   }
}