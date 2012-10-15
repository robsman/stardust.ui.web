package org.eclipse.stardust.ui.web.modeler.edit.postprocessing;

import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.stardust.model.xpdl.builder.session.Modification;
import org.eclipse.stardust.model.xpdl.carnot.ActivitySymbolType;
import org.eclipse.stardust.model.xpdl.carnot.ActivityType;
import org.eclipse.stardust.model.xpdl.carnot.CarnotWorkflowModelPackage;
import org.eclipse.stardust.model.xpdl.carnot.IModelParticipant;
import org.eclipse.stardust.model.xpdl.carnot.LaneSymbol;
import org.eclipse.stardust.model.xpdl.carnot.util.ActivityUtil;
import org.eclipse.stardust.ui.web.modeler.edit.spi.ChangePostprocessor;
import org.springframework.stereotype.Component;

@Component
public class LaneParticipantChangeTracker implements ChangePostprocessor
{
   private static final CarnotWorkflowModelPackage PKG_XPDL = CarnotWorkflowModelPackage.eINSTANCE;

   private Modification modification;

   @Override
   public int getInspectionPhase()
   {
      return 100;
   }

   @Override
   public void inspectChange(Modification change)
   {
      modification = change;
      for (EObject candidate : change.getModifiedElements())
      {
         if ((candidate instanceof LaneSymbol)
               && change
                     .wasModified(candidate, PKG_XPDL.getISwimlaneSymbol_Participant()))
         {
            LaneSymbol lane = (LaneSymbol) candidate;
            IModelParticipant participant = lane.getParticipant();
            setParticipant(lane, participant);
         }
      }
   }

   private void setParticipant(LaneSymbol lane, IModelParticipant participant)
   {
      List<ActivitySymbolType> activitySymbols = lane.getActivitySymbol();
      for (int i = 0; i < activitySymbols.size(); i++)
      {
         ActivitySymbolType activitySymbol = (ActivitySymbolType) activitySymbols.get(i);
         ActivityType activity = (ActivityType) activitySymbol.getModelElement();

         if (ActivityUtil.isInteractive(activity))
         {
            setPerformer(activity, participant, activity.getPerformer(), false);
         }
      }
   }




   private void setPerformer(ActivityType activity, IModelParticipant newPerformer,
         IModelParticipant originalPerformer, boolean updateConnectionsOnly)
   {
      if (!updateConnectionsOnly && newPerformer != originalPerformer)
      {
         activity.setPerformer(newPerformer);
         modification.markAlsoModified(activity);
      }

   }

   public static String getPerformerName(IModelParticipant performer)
   {
      return performer == null ? "" : performer.getName() != null //$NON-NLS-1$
            ? performer.getName()
            : performer.getId() != null ? performer.getId() : Long.toString(performer
                  .getElementOid());
   }

}
