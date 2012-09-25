package org.eclipse.stardust.ui.web.modeler.edit.twophase;

import static org.eclipse.stardust.common.CollectionUtils.isEmpty;
import static org.eclipse.stardust.common.CollectionUtils.newHashMap;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.compare.diff.merge.service.MergeService;
import org.eclipse.emf.compare.diff.metamodel.DiffElement;
import org.eclipse.emf.compare.diff.metamodel.DiffModel;
import org.eclipse.emf.compare.diff.service.DiffService;
import org.eclipse.emf.compare.match.metamodel.Match2Elements;
import org.eclipse.emf.compare.match.metamodel.MatchElement;
import org.eclipse.emf.compare.match.metamodel.MatchModel;
import org.eclipse.emf.compare.match.service.MatchService;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.model.xpdl.builder.session.EditingSession;
import org.eclipse.stardust.model.xpdl.builder.session.Modification;
import org.eclipse.stardust.ui.web.modeler.edit.CommandHandlerRegistry;
import org.eclipse.stardust.ui.web.modeler.edit.CommandHandlerRegistry.ICommandHandlerInvoker;
import org.eclipse.stardust.ui.web.modeler.edit.SimpleCommandHandlingMediator;
import org.eclipse.stardust.ui.web.modeler.edit.spi.CommandHandlingMediator;

@Component
@Scope("prototype")
public class TwophaseCommandHandlingMediator implements CommandHandlingMediator
{
   private static final Logger trace = LogManager.getLogger(TwophaseCommandHandlingMediator.class);

   @Resource
   private SimpleCommandHandlingMediator onephaseMediator;

   @Resource
   private CommandHandlerRegistry commandHandlerRegistry;

   public boolean isTwophase()
   {
      return true;
   }

   public void broadcastChange(EditingSession session, JsonObject commndJson)
   {
      onephaseMediator.broadcastChange(session, commndJson);
   }

   public Modification handleCommand(EditingSession editingSession, String commandId,
         List<ChangeRequest> changes)
   {
      Modification change = null;
      try
      {
         Map<EObject, EObject> baseModels = newHashMap();
         Map<EObject, EObject> tryModels = newHashMap();
         Map<EObject, MatchModel> tryMatches = newHashMap();

         for (ChangeRequest modification : changes)
         {
            EObject tryModel = tryModels.get(modification.getModel());
            if (null == tryModel)
            {
               baseModels.put(modification.getModel(), cloneModel(modification.getModel()));
               tryModels.put(modification.getModel(), cloneModel(modification.getModel()));
               tryModel = tryModels.get(modification.getModel());

               while ( !tryMatches.containsKey(tryModel))
               {
                  try
                  {
                     // TODO match options?
                     tryMatches.put(tryModel, MatchService.doMatch(modification.getModel(), tryModel, null));
                  }
                  catch (InterruptedException e)
                  {
                     // try again
                  }
               }
            }

            EObject tryContext = findMatchingElement(tryMatches.get(tryModel),
                  modification.getContextElement());

            ICommandHandlerInvoker invoker = null;
            if (null != commandHandlerRegistry)
            {
               invoker = commandHandlerRegistry.findCommandHandler(commandId, tryModel,
                     tryContext);
            }

            if (null != invoker)
            {
               invoker.handleCommand(commandId, tryModel,
                     tryContext, modification.getChangeDescriptor());
            }
            else
            {
               trace.error("Failed handling command: no suitable handler for command '"
                     + commandId + "'.");
            }
         }

         // TODO exclusively lock affected models

         // prepare merge, verify modification is conflict free
         Map<EObject, DiffModel> differences = newHashMap();
         for (EObject model : baseModels.keySet())
         {
            EObject baseModel = baseModels.get(model);
            EObject tryModel = tryModels.get(model);

            while ( !differences.containsKey(model))
            {
               try
               {
                  MatchModel conflictMatches = (null != baseModel.eContainer())
                        ? MatchService.doMatch(tryModel.eContainer(), model.eContainer(),
                              baseModel.eContainer(), null) //
                        : MatchService.doMatch(tryModel, model, baseModel, null);
                  DiffModel diff = DiffService.doDiff(conflictMatches, true);
                  if ( !hasConflicts(diff.getDifferences()))
                  {
                     differences.put(model, diff);
                     // verify next model
                     break;
                  }
                  else
                  {
                     trace.warn("Aborting modification fue to conflicts.");
                     throw new WebApplicationException(Status.CONFLICT);
                  }
               }
               catch (InterruptedException e)
               {
                  // try again
               }
            }
         }

         if (null != editingSession)
         {
            // starting to record changes in order to automatically be able to perform
            // undo/redo
            editingSession.beginEdit();

            // merge modification into main models, build undo/redo information on the go
            for (EObject model : differences.keySet())
            {
               MergeService.merge(differences.get(model).getDifferences(), true);
            }
         }

      }
      finally
      {
         if ((null != editingSession) && editingSession.endEdit())
         {
            change = editingSession.getPendingUndo();
         }
      }

      if (trace.isDebugEnabled())
      {
         trace.debug("Change: " + change);
      }

      return change;
   }

   private EObject cloneModel(EObject model)
   {
      if (null != model.eContainer())
      {
         // clone model container to allow "model move" detection (causes NPE otherwise)
         EObject clonedContainer = EcoreUtil.copy(model.eContainer());
         return (EObject) clonedContainer.eGet(model.eContainingFeature());
      }
      else
      {
         return EcoreUtil.copy(model);
      }
   }

   private EObject findMatchingElement(MatchModel matches, EObject lhs)
   {
      EObject rhs = findMatchingElement(matches.getMatchedElements(), lhs);
      if (null == rhs)
      {
         throw new IllegalArgumentException("Must not have no match for " + lhs);
      }

      return rhs;
   }

   private EObject findMatchingElement(EList<MatchElement> matches, EObject lhs)
   {
      EObject rhs = null;
      for (MatchElement match : matches)
      {
         if ((match instanceof Match2Elements)
               && (lhs == ((Match2Elements) match).getLeftElement()))
         {
            rhs = ((Match2Elements) match).getRightElement();
            break;
         }
         else if ( !isEmpty(match.getSubMatchElements()))
         {
            rhs = findMatchingElement(match.getSubMatchElements(), lhs);
            if (null != rhs)
            {
               break;
            }
         }
      }

      return rhs;
   }

   private boolean hasConflicts(EList<DiffElement> diff)
   {
      if ( !isEmpty(diff))
      {
         for (DiffElement diffElement : diff)
         {
            if (diffElement.isConflicting())
            {
               trace.warn("Found merge conflict: " + diffElement);
               return true;
            }
            else
            {
               return hasConflicts(diffElement.getSubDiffElements());
            }
         }
      }

      return false;
   }

}
