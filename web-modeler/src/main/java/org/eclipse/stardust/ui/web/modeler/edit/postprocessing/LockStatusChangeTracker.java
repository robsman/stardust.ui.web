package org.eclipse.stardust.ui.web.modeler.edit.postprocessing;

import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.springframework.stereotype.Component;

import org.eclipse.stardust.model.xpdl.builder.session.Modification;
import org.eclipse.stardust.model.xpdl.carnot.AttributeType;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;

@Component
public class LockStatusChangeTracker extends AbstractChangeTracker
{
   @Override
   protected void inspectChange(Modification change, EObject candidate)
   {
      if ((candidate instanceof ModelType))
      {
         List<EObject> objects = change.getChangeDescription().getObjectsToDetach();
         if ( !objects.isEmpty() && objects.get(0) instanceof AttributeType)
         {
            AttributeType attributeType = (AttributeType) objects.get(0);
            if (attributeType.getName().equals("stardust:security:hash"))
            {
               change.getSession().clearUndoRedoStack();
            }
         }
         objects = change.getChangeDescription().getObjectsToAttach();
         if ( !objects.isEmpty() && objects.get(0) instanceof AttributeType)
         {
            AttributeType attributeType = (AttributeType) objects.get(0);
            if (attributeType.getName().equals("stardust:security:hash"))
            {
               change.getSession().clearUndoRedoStack();
            }
         }
      }
   }
}
