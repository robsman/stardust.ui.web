package org.eclipse.stardust.ui.web.modeler.edit.postprocessing;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.change.impl.ChangeDescriptionImpl;
import org.springframework.stereotype.Component;

import org.eclipse.stardust.model.xpdl.builder.session.Modification;
import org.eclipse.stardust.model.xpdl.builder.utils.LaneParticipantUtil;
import org.eclipse.stardust.model.xpdl.carnot.ActivityType;
import org.eclipse.stardust.model.xpdl.carnot.DiagramType;
import org.eclipse.stardust.model.xpdl.carnot.IModelParticipant;
import org.eclipse.stardust.model.xpdl.carnot.LaneSymbol;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.model.xpdl.carnot.PoolSymbol;
import org.eclipse.stardust.model.xpdl.carnot.ProcessDefinitionType;
import org.eclipse.stardust.ui.web.modeler.edit.spi.ChangePostprocessor;

@Component
public class ParticipantChangeTracker implements ChangePostprocessor
{
   @Override
   public int getInspectionPhase()
   {
      return 100;
   }

   @Override
   public void inspectChange(Modification change)
   {
      for (EObject candidate : change.getModifiedElements())
      {
         if (candidate instanceof IModelParticipant)
         {
            trackModification(candidate, false, change);            
         }
      }
      for (EObject candidate : change.getRemovedElements())
      {
         if (candidate instanceof IModelParticipant)
         {         
            trackModification(candidate, true, change);                     
         }
      }
   }

   private void trackModification(EObject candidate, boolean removed, Modification change)
   {
      EObject container = null;
      if (candidate.eContainer() instanceof ChangeDescriptionImpl)
      {
         ChangeDescriptionImpl changeDescription = (ChangeDescriptionImpl) candidate.eContainer();
         container = changeDescription.getOldContainer(candidate);
      }
      else
      {
         container = candidate.eContainer();
      }
      
      ModelType model = (ModelType) container;
      for(ProcessDefinitionType process : model.getProcessDefinition())
      {         
         for(DiagramType diagram : process.getDiagram())
         {
            for(PoolSymbol pool : diagram.getPoolSymbols())
            {
               for(LaneSymbol lane : pool.getLanes())
               {
                  IModelParticipant oldParticipant = LaneParticipantUtil.getParticipant(lane);
                  if(removed && oldParticipant != null && oldParticipant.equals(candidate))
                  {
                     LaneParticipantUtil.setParticipant(lane, null);
                     change.markAlsoModified(lane);                     
                  }
                  else if(!removed && oldParticipant != null && oldParticipant.equals(candidate))
                  {
                     LaneParticipantUtil.setParticipant(lane, (IModelParticipant) candidate);                     
                     change.markAlsoModified(lane);                     
                  }
               }               
            }
         }
         
         for(ActivityType activity : process.getActivity())
         {
            IModelParticipant oldParticipant = activity.getPerformer();
            if(removed && oldParticipant != null && oldParticipant.equals(candidate))
            {
               activity.setPerformer(null);               
               change.markAlsoModified(activity);                     
            }
            else if(!removed && oldParticipant != null && oldParticipant.equals(candidate))
            {
               activity.setPerformer((IModelParticipant) candidate);               
               change.markAlsoModified(activity);                     
            }
         }
      }
   }
}