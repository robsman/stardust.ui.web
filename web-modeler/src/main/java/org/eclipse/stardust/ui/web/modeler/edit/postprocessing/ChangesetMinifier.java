package org.eclipse.stardust.ui.web.modeler.edit.postprocessing;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.change.ChangeKind;
import org.eclipse.emf.ecore.change.FeatureChange;
import org.eclipse.emf.ecore.change.ListChange;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.model.xpdl.builder.session.Modification;
import org.eclipse.stardust.ui.web.modeler.edit.spi.ChangePostprocessor;

public class ChangesetMinifier implements ChangePostprocessor
{
   @Override
   public void inspectChange(Modification change)
   {
      for (EObject candidate : change.getModifiedElements())
      {
         EList<FeatureChange> modifications = change.getChangeDescription()
               .getObjectChanges()
               .get(candidate);
         // count up to be sure not to miss any non-pure change
         int nPureContainerChanges = 0;
         if (CollectionUtils.isNotEmpty(modifications))
         {
            for (FeatureChange modification : modifications)
            {
               if (modification.getFeature() instanceof EReference)
               {
                  EReference eRef = (EReference) modification.getFeature();
                  if (eRef.isContainment())
                  {
                     if (eRef.isMany())
                     {
                        if (modification.getListChanges().isEmpty())
                        {
                           @SuppressWarnings("rawtypes")
                           EList elements = (EList) candidate.eGet(eRef);
                           if (change.getAddedElements().containsAll(elements))
                           {
                              ++nPureContainerChanges;
                              continue;
                           }
                        }
                        else
                        {
                           for (ListChange listChange : modification.getListChanges())
                           {
                              if (ChangeKind.REMOVE_LITERAL == listChange.getKind())
                              {
                                 // this actually means something was added
                                 @SuppressWarnings("rawtypes")
                                 EObject addedElement = (EObject) ((EList) candidate.eGet(eRef)).get(listChange.getIndex());
                                 if (change.getAddedElements().contains(addedElement))
                                 {
                                    ++nPureContainerChanges;
                                    continue;
                                 }
                              }
                              else if (ChangeKind.ADD_LITERAL == listChange.getKind())
                              {
                                 // this actually means something was removed
                                 if (change.getRemovedElements().containsAll(
                                       listChange.getReferenceValues()))
                                 {
                                    ++nPureContainerChanges;
                                    continue;
                                 }
                              }
                           }
                        }
                     }
                  }
                  else
                  {
                     // other than modified containment reference, exit quickly
                     break;
                  }
               }
               else
               {
                  // other than modified reference, exit quickly
                  break;
               }
            }
         }

         if (CollectionUtils.isEmpty(modifications) || nPureContainerChanges == modifications.size())
         {
            change.markUnmodified(candidate);
         }
      }

   }
}
