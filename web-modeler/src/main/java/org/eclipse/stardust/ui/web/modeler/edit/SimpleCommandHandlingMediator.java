package org.eclipse.stardust.ui.web.modeler.edit;

import static org.eclipse.stardust.common.CollectionUtils.newArrayList;

import java.util.List;

import javax.annotation.Resource;

import org.eclipse.emf.ecore.EObject;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.model.xpdl.builder.session.EditingSession;
import org.eclipse.stardust.model.xpdl.builder.session.Modification;
import org.eclipse.stardust.model.xpdl.carnot.AttributeType;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.model.xpdl.carnot.util.AttributeUtil;
import org.eclipse.stardust.ui.web.modeler.edit.CommandHandlerRegistry.ICommandHandlerInvoker;
import org.eclipse.stardust.ui.web.modeler.edit.spi.CommandHandlingMediator;

@Component
@Scope("session")
public class SimpleCommandHandlingMediator
{
   private static final Logger trace = LogManager.getLogger(SimpleCommandHandlingMediator.class);

   @Resource
   private CommandHandlerRegistry commandHandlerRegistry;

   @Resource
   private List<IChangeListener> changeListeners = newArrayList();

   public boolean isTwophase()
   {
      return false;
   }

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

   public Modification handleCommand(EditingSession editingSession, String commandId,
         List<CommandHandlingMediator.ChangeRequest> changes)
   {
      Modification change = null;
      try
      {
         if (null != editingSession)
         {
            // starting to record changes in order to automatically be able to perform
            // undo/redo
            editingSession.beginEdit();
         }

         for (CommandHandlingMediator.ChangeRequest modification : changes)
         {
            ICommandHandlerInvoker invoker = null;
            if (isReadOnly(modification.getModel())
                  && !(commandId.equalsIgnoreCase("modelLockStatus.update")))
            {
               trace.error("Failed handling command: '" + commandId
                     + "' - Request tried to modify a locked model!");
               throw new RuntimeException("Request tried to modify a locked model!");
            }
            if (null != commandHandlerRegistry)
            {
               invoker = commandHandlerRegistry.findCommandHandler(commandId,
                     modification.getModel(), modification.getContextElement());
            }

            if (null != invoker)
            {
               invoker.handleCommand(commandId, modification.getModel(),
                     modification.getContextElement(), modification.getChangeDescriptor());
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

   public boolean isReadOnly(EObject element)
   {
      if (element != null && element instanceof ModelType)
      {
         AttributeType attribute = AttributeUtil.getAttribute((ModelType) element,
               "stardust:security:hash");
         if ((attribute != null) && (attribute.getValue() != null)
               && (attribute.getValue().length() > 0))
         {
            return true;
         }

      }
      return false;
   }
}
