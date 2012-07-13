package org.eclipse.stardust.ui.web.modeler.edit;

import static org.eclipse.stardust.common.CollectionUtils.newArrayList;

import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.model.xpdl.builder.session.EditingSession;
import org.eclipse.stardust.model.xpdl.builder.session.Modification;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.ui.web.modeler.edit.diagram.node.ActivityCommandHandler;
import org.eclipse.stardust.ui.web.modeler.edit.diagram.node.EventCommandHandler;
import org.eclipse.stardust.ui.web.modeler.edit.diagram.node.GatewayCommandHandler;
import org.eclipse.stardust.ui.web.modeler.edit.diagram.node.MoveNodeSymbolHandler;
import org.eclipse.stardust.ui.web.modeler.edit.diagram.node.SwimlaneCommandHandler;
import org.eclipse.stardust.ui.web.modeler.edit.model.element.CreateProcessCommandHandler;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;

@Component
// TODO registry should be singleton scope, but somehow needs to have access to
// session-scoped EditingSession management
@Scope("session")
public class CommandHandlingMediator
{
   @Resource
   private EditingSessionManager editingSessionManager;

   public Modification handleCommand(ModelType containingModel, String commandId,
         List<Pair<EObject, JsonObject>> changes)
   {
      // TODO register handlers externally
      // TODO proper handler attribute matching
      ICommandHandler handler = null;
      
      if ("modelElement.update".equals(commandId))
      {
         handler = new UpdateModelElementCommandHandler();
      }
      else if ("nodeSymbol.move".equals(commandId))
      {
         handler = new MoveNodeSymbolHandler();
      }
      else if ("activitySymbol.create".equals(commandId) || "activitySymbol.delete".equals(commandId))
      {
         handler = new ActivityCommandHandler();
      }
      else if ("eventSymbol.create".equals(commandId) || "eventSymbol.delete".equals(commandId))
      {
         handler = new EventCommandHandler();
      }
      else if ("gateSymbol.create".equals(commandId) || "gateSymbol.delete".equals(commandId))
      {
         handler = new GatewayCommandHandler();
      }
      else if ("swimlaneSymbol.create".equals(commandId) || "swimlaneSymbol.delete".equals(commandId))
      {
         handler = new SwimlaneCommandHandler();
      }
      else if ("process.create".equals(commandId))
      {
         handler = new CreateProcessCommandHandler();
      }
      
      Modification change = null;
      EObject changeRoot;
      if (null != handler)
      {
         // TODO wrap in undo/redo command generator

         EditingSession editingSession = editingSessionManager.getSession(containingModel);
         List<EObject> modifications = newArrayList();
         try
         {
            if (null != editingSession)
            {
               editingSession.beginEdit();
            }

            for (Pair<EObject, JsonObject> modification : changes)
            {
               // TODO verify type of target element against expected target (probably
               // requires some kind of annotation on handler)
               handler.handleCommand(commandId, modification.getFirst(), modification.getSecond());
            }
         }
         finally
         {
            if ((null != editingSession) && editingSession.endEdit())
            {
               change = editingSession.getPendingUndo();
               modifications.addAll(change.getChangeDescription()
                     .getObjectChanges()
                     .keySet());
               // TODO attached/detached objects are implicitly causing a modification
               // on the container, are they?
            }
            else
            {
               // wild guess assuming only the elements itself were changed
               for (Pair<EObject, JsonObject> modification : changes)
               {
                  modifications.add(modification.getFirst());
               }
            }
         }

         // determine what has changed (potentially beyond targetElement), find
         // "common root" containing all changes

         if (1 == modifications.size())
         {
            // exactly one element (probably targetELement) has changed
            changeRoot = modifications.get(0);
         }
         else
         {
            // find most specific common root of all modified elements
            changeRoot = findCommonRoot(containingModel, modifications);
         }
      }
      else
      {
         // did not handle command, so nothing has changed
         changeRoot = null;
      }

      return change;
   }

   private EObject findCommonRoot(ModelType model, List<EObject> elements)
   {
      // build model-rooted containment path for all elements
      List<List<EObject>> paths = newArrayList();
      for (EObject element : elements)
      {
         List<EObject> containment = newArrayList();
         containment.add(element);

         EObject currentStep = element;
         while ( !(currentStep instanceof ModelType)
               && (null != currentStep.eContainer()))
         {
            currentStep = currentStep.eContainer();
            containment.add(currentStep);
         }

         // reverse list so least specific node (the model) comes first
         Collections.reverse(containment);

         paths.add(containment);
      }

      // safe guess is, all elements are part of the given model
      EObject commonRoot = model;

      int depth = 0;
      while (true)
      {
         EObject rootCandidate = null;
         for (List<EObject> path : paths)
         {
            // To check if depth exceeds the path size
            if (depth > (path.size() - 1))
            {
               continue;
            }
            if (null == rootCandidate)
            {
               // no candidate yet, so assume one
               rootCandidate = path.get(depth);
            }
            else if (rootCandidate != path.get(depth))
            {
               // at current depth, roots start to diverge, so the most specific common
               // root was already found
               rootCandidate = null;
               break;
            }
         }

         if (null != rootCandidate)
         {
            // all paths share the same node at current depth, so this is a more specific
            // root then the previous assumption
            commonRoot = rootCandidate;
            ++depth;
         }
         else
         {
            // no better common root, can terminate search
            break;
         }
      }

      return commonRoot;
   }
}
