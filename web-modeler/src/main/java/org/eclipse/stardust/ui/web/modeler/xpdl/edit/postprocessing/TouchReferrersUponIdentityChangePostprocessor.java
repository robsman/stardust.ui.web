package org.eclipse.stardust.ui.web.modeler.xpdl.edit.postprocessing;

import java.util.List;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.stardust.model.xpdl.builder.connectionhandler.EObjectReference;
import org.eclipse.stardust.model.xpdl.builder.session.Modification;
import org.eclipse.stardust.model.xpdl.carnot.CarnotWorkflowModelPackage;
import org.eclipse.stardust.model.xpdl.carnot.IIdentifiableElement;
import org.eclipse.stardust.model.xpdl.carnot.IIdentifiableModelElement;
import org.eclipse.stardust.model.xpdl.carnot.INodeSymbol;
import org.eclipse.stardust.model.xpdl.xpdl2.TypeDeclarationType;
import org.eclipse.stardust.model.xpdl.xpdl2.XpdlPackage;
import org.eclipse.stardust.ui.web.modeler.edit.spi.ChangePostprocessor;
import org.springframework.stereotype.Component;

@Component
public class TouchReferrersUponIdentityChangePostprocessor implements ChangePostprocessor
{
   private static final CarnotWorkflowModelPackage PKG_XPDL = CarnotWorkflowModelPackage.eINSTANCE;
   private static final XpdlPackage PKG_XPDL2 = XpdlPackage.eINSTANCE;

   @Override
   public int getInspectionPhase()
   {
      return 50;
   }

   @Override
   public void inspectChange(Modification change)
   {
      for (EObject candidate : change.getModifiedElements())
      {
         if ((candidate instanceof IIdentifiableElement) && change.wasModified(candidate, PKG_XPDL.getIIdentifiableElement_Id())
               || (candidate instanceof TypeDeclarationType) && change.wasModified(candidate, PKG_XPDL2.getTypeDeclarationType_Id()))
         {
            // TODO find all references and flag them as modified
            for (EObject other : candidate.eCrossReferences())
            {
               change.markAlsoModified(other);
            }

            for (Adapter adapter : candidate.eAdapters())
            {
               if (adapter instanceof EObjectReference)
               {
                  EObject self = ((EObjectReference) adapter).getSelf();
                  change.markAlsoModified(self);
                  if (self instanceof IIdentifiableModelElement)
                  {
                     List<INodeSymbol> symbols = ((IIdentifiableModelElement) self).getSymbols();
                     for (INodeSymbol symbol : symbols)
                     {
                        change.markAlsoModified(symbol);
                     }
                  }
               }
            }

            // TODO find non-EMF references (e.g. from Stardust Attributes)
         }
      }
   }

}
