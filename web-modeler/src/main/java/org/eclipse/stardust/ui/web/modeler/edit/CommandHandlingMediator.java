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
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.model.xpdl.builder.session.EditingSession;
import org.eclipse.stardust.model.xpdl.builder.session.Modification;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.ui.web.modeler.edit.CommandHandlerRegistry.ICommandHandlerInvoker;

@Component
@Scope("session")
public class CommandHandlingMediator
{
   private static final Logger trace = LogManager.getLogger(CommandHandlingMediator.class);

   @Resource
   private EditingSessionManager editingSessionManager;

   @Resource
   private CommandHandlerRegistry commandHandlerRegistry;

   @Resource
   private ApplicationContext springContext;

   private List<IChangeListener> changeListeners = newArrayList();

   public void broadcastChange(EditingSession session, JsonObject commndJson)
   {
      for (IChangeListener listener : changeListeners)
      {
         try
         {
            listener.onCommand(session, commndJson);
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
      EditingSession editingSession = editingSessionManager.getSession(containingModel);
      Modification change = null;
      try
      {
         if (null != editingSession)
         {
            // starting to record changes in order to automatically be able to perform
            // undo/redo
            editingSession.beginEdit();
         }

         for (Pair<EObject, JsonObject> modification : changes)
         {
            ICommandHandlerInvoker invoker = null;
            if (null != commandHandlerRegistry)
            {
               invoker = commandHandlerRegistry.findCommandHandler(commandId,
                     modification.getFirst());
            }

            if (null != invoker)
            {
               invoker.handleCommand(commandId, modification.getFirst(),
                     modification.getSecond());
            }
            else
            {
               trace.error("Failed handling command: no suitable handler for command '"
                     + commandId + "'.");
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
}
