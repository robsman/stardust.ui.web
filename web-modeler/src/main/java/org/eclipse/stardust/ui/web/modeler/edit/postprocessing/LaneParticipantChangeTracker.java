package org.eclipse.stardust.ui.web.modeler.edit.postprocessing;

import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.springframework.stereotype.Component;

import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.model.xpdl.builder.session.Modification;
import org.eclipse.stardust.model.xpdl.builder.utils.LaneParticipantUtil;
import org.eclipse.stardust.model.xpdl.carnot.ActivitySymbolType;
import org.eclipse.stardust.model.xpdl.carnot.ActivityType;
import org.eclipse.stardust.model.xpdl.carnot.CarnotWorkflowModelPackage;
import org.eclipse.stardust.model.xpdl.carnot.IModelParticipant;
import org.eclipse.stardust.model.xpdl.carnot.LaneSymbol;
import org.eclipse.stardust.model.xpdl.carnot.StartEventSymbol;
import org.eclipse.stardust.model.xpdl.carnot.TriggerType;
import org.eclipse.stardust.model.xpdl.carnot.util.ActivityUtil;
import org.eclipse.stardust.model.xpdl.carnot.util.AttributeUtil;
import org.eclipse.stardust.ui.web.modeler.edit.spi.ChangePostprocessor;

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
         if (candidate instanceof LaneSymbol               
               && (change.wasModified(candidate, PKG_XPDL.getISwimlaneSymbol_Participant())
               || change.wasModified(candidate, PKG_XPDL.getISwimlaneSymbol_ParticipantReference())))
         {
            LaneSymbol lane = (LaneSymbol) candidate;
            IModelParticipant participant = LaneParticipantUtil.getParticipant(lane);
            setParticipant(lane, participant);
         }
      }
   }

   private void setParticipant(LaneSymbol lane, IModelParticipant participant)
   {
      List<ActivitySymbolType> activitySymbols = lane.getActivitySymbol();
      for (int i = 0; i < activitySymbols.size(); i++ )
      {
         ActivitySymbolType activitySymbol = (ActivitySymbolType) activitySymbols.get(i);
         ActivityType activity = (ActivityType) activitySymbol.getModelElement();

         if (ActivityUtil.isInteractive(activity))
         {
            setPerformer(activity, participant, activity.getPerformer());
         }
      }

      List<StartEventSymbol> startEventSymbols = lane.getStartEventSymbols();
      for (int i = 0; i < startEventSymbols.size(); i++ )
      {
         StartEventSymbol startEventSymbol = startEventSymbols.get(i);
         TriggerType trigger = startEventSymbol.getTrigger();

         if ((null != trigger)
               && PredefinedConstants.MANUAL_TRIGGER.equals(trigger.getType().getId()))
         {
            setPerformer(trigger, participant);
         }
      }
   }

   private void setPerformer(ActivityType activity, IModelParticipant newPerformer,
         IModelParticipant originalPerformer)
   {
      if (newPerformer != originalPerformer)
      {
         activity.setPerformer(newPerformer);
         modification.markAlsoModified(activity);
      }

   }

   private void setPerformer(TriggerType manualTrigger, IModelParticipant newPerformer)
   {
      String originalPerformerId = AttributeUtil.getAttributeValue(manualTrigger,
            PredefinedConstants.MANUAL_TRIGGER_PARTICIPANT_ATT);
      String newPerformerId = (null != newPerformer) ? newPerformer.getId() : null;
      if ( !CompareHelper.areEqual(newPerformerId, originalPerformerId))
      {
         AttributeUtil.setAttribute(manualTrigger,
               PredefinedConstants.MANUAL_TRIGGER_PARTICIPANT_ATT, newPerformerId);
         modification.markAlsoModified(manualTrigger);
      }
   }
}
