package org.eclipse.stardust.ui.web.modeler.bpmn2.utils;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature.Setting;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.EcoreUtil.UsageCrossReferencer;
import org.eclipse.emf.ecore.util.InternalEList;

import org.eclipse.stardust.ui.web.modeler.bpmn2.Bpmn2Utils;

public class EObjectMorpher
{
   public static EObject morphType(EObject source, EClass targetType)
   {
      TypeMorphingCopier copier = new TypeMorphingCopier(targetType);

      // phase one, copy common attribute set, incl. contained elements
      EObject target = copier.copy(source);

      // phase two, reconnect target for source for all source-outgoing references
      copier.copyReferences();

      // phase three, reconnect target for source for remaining source-incoming references
      Collection<Setting> xRefs = UsageCrossReferencer.find(source, Bpmn2Utils.findContainingModel(source));
      for (Setting setting : xRefs)
      {
         EcoreUtil.replace(setting, source, target);
      }
      EcoreUtil.replace(source, target);

      return target;
   }

   private static class TypeMorphingCopier extends EcoreUtil.Copier
   {
      private static final long serialVersionUID = 1L;

      private EClass targetType;

      public TypeMorphingCopier(EClass targetType)
      {
         this.targetType = targetType;
      }

      @Override
      protected EClass getTarget(EClass eClass)
      {
         return (null != targetType) ? targetType : super.getTarget(eClass);
      }

      /**
       * Replace source for target, even for bidirectional references.
       * <p>
       * This is a slightly adjusted variant of the super method, including handling of
       * bidirectional references, too.
       *
       * @param eReference
       *           the reference to copy.
       * @param eObject
       *           the object from which to copy.
       * @param copyEObject
       *           the object to copy to.
       */
      @Override
      protected void copyReference(EReference eReference, EObject eObject,
            EObject copyEObject)
      {
         if (eObject.eIsSet(eReference))
         {
            boolean isBidirectional = eReference.getEOpposite() != null;

            if (eReference.isMany())
            {
               @SuppressWarnings("unchecked")
               InternalEList<EObject> source = (InternalEList<EObject>) eObject.eGet(eReference);
               @SuppressWarnings("unchecked")
               InternalEList<EObject> target = (InternalEList<EObject>) copyEObject.eGet(getTarget(eReference));
               if (source.isEmpty())
               {
                  target.clear();
               }
               else
               {
                  int index = 0;
                  for (Iterator<EObject> k = resolveProxies
                        ? source.iterator()
                        : source.basicIterator(); k.hasNext();)
                  {
                     EObject referencedEObject = k.next();
                     EObject copyReferencedEObject = get(referencedEObject);
                     if (copyReferencedEObject == null)
                     {
                        if (useOriginalReferences)
                        {
                           if (isBidirectional)
                           {
                              // disconnect source to make room for target
                              k.remove();
                           }
                           // reconnect target
                           target.addUnique(index, referencedEObject);
                           ++index;
                        }
                     }
                     else
                     {
                        if (isBidirectional)
                        {
                           // disconnect source to make room for target
                           k.remove();

                           // reconnect target
                           int position = target.indexOf(copyReferencedEObject);
                           if (position == -1)
                           {
                              target.addUnique(index, copyReferencedEObject);
                           }
                           else if (index != position)
                           {
                              target.move(index, copyReferencedEObject);
                           }
                        }
                        else
                        {
                           target.addUnique(index, copyReferencedEObject);
                        }
                        ++index;
                     }
                  }
               }
            }
            else
            {
               Object referencedEObject = eObject.eGet(eReference, resolveProxies);
               if (referencedEObject == null)
               {
                  copyEObject.eSet(getTarget(eReference), null);
               }
               else
               {
                  Object copyReferencedEObject = get(referencedEObject);
                  if (copyReferencedEObject == null)
                  {
                     if (useOriginalReferences && !isBidirectional)
                     {
                        if (isBidirectional)
                        {
                           // disconnect source to make room for target
                           eObject.eSet(eReference, null);
                        }

                        // reconnect target
                        copyEObject.eSet(getTarget(eReference), referencedEObject);
                     }
                  }
                  else
                  {
                     copyEObject.eSet(getTarget(eReference), copyReferencedEObject);
                  }
               }
            }
         }
      }
   }
}
