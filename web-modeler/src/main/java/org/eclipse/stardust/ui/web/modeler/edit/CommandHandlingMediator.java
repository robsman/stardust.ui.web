package org.eclipse.stardust.ui.web.modeler.edit;

import static org.eclipse.stardust.common.CollectionUtils.newArrayList;

import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.eclipse.emf.ecore.EObject;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;

import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.model.xpdl.builder.session.EditingSession;
import org.eclipse.stardust.model.xpdl.builder.session.Modification;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.ui.web.modeler.edit.diagram.node.ActivityCommandHandler;
import org.eclipse.stardust.ui.web.modeler.edit.diagram.node.ConnectionCommandHandler;
import org.eclipse.stardust.ui.web.modeler.edit.diagram.node.EventCommandHandler;
import org.eclipse.stardust.ui.web.modeler.edit.diagram.node.GatewayCommandHandler;
import org.eclipse.stardust.ui.web.modeler.edit.diagram.node.MoveNodeSymbolHandler;
import org.eclipse.stardust.ui.web.modeler.edit.diagram.node.SwimlaneCommandHandler;
import org.eclipse.stardust.ui.web.modeler.edit.model.element.CreateProcessCommandHandler;
import org.eclipse.stardust.ui.web.modeler.edit.model.element.StructuredTypeChangeCommandHandler;
import org.eclipse.stardust.ui.web.modeler.edit.model.element.ApplicationTypeChangeCommandHandler;

@Component
// TODO registry should be singleton scope, but somehow needs to have access to
// session-scoped EditingSession management
@Scope("session")
public class CommandHandlingMediator
{
   @Resource
   private EditingSessionManager editingSessionManager;

   @Resource
   private ApplicationContext springContext;

   private List<IChangeListener> changeListeners = newArrayList();

   public void broadcastChange(JsonObject commndJson)
   {
      for (IChangeListener listener : changeListeners)
      {
         try
         {
            listener.onCommand(commndJson);
         }
         catch (Exception e)
         {
            // TODO: handle exception
         }
      }
   }

   @PostConstruct
   public void bindChangeListeners()
   {
      if (null != springContext)
      {
         Map<String, IChangeListener> commandListeners = springContext.getBeansOfType(IChangeListener.class);
         if (null != commandListeners)
         {
            this.changeListeners.addAll(commandListeners.values());
         }
      }
   }

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
      else if ("structuredDataType.create".equals(commandId))
      {
         handler = new StructuredTypeChangeCommandHandler();
      }
      else if ("webServiceApplication.create".equals(commandId)
            || "messageTransformationApplication.create".equals(commandId)
            || "camelApplication.create".equals(commandId)
            || "uiMashupApplication.create".equals(commandId))
      {
         handler = new ApplicationTypeChangeCommandHandler();
      }
      else if ("connection.create".equals(commandId) || "connection.delete".equals(commandId))
      {
         handler = new ConnectionCommandHandler();
      }

      Modification change = null;
      if (null != handler)
      {
         // TODO wrap in undo/redo command generator

         EditingSession editingSession = editingSessionManager.getSession(containingModel);
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
            }
         }
      }

      System.out.println("Change: " + change);

      return change;
   }
}
