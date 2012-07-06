package org.eclipse.stardust.ui.web.modeler.edit;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.stardust.model.xpdl.builder.session.EditingSession;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.model.xpdl.carnot.ProcessDefinitionType;
import org.eclipse.stardust.model.xpdl.carnot.util.ModelUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("session")
public class EditingSessionManager
{
   private final ConcurrentMap<EObject, EditingSession> editingSessions = new ConcurrentHashMap<EObject, EditingSession>();

   public EditingSession getSession(ProcessDefinitionType processDefinition)
   {
      return editingSessions.get(processDefinition);
   }

   public EditingSession createEditingSession(ProcessDefinitionType processDefinition)
   {
      if ( !editingSessions.containsKey(processDefinition))
      {
         ModelType model = ModelUtils.findContainingModel(processDefinition);
         editingSessions.putIfAbsent(processDefinition, new EditingSession(model));
      }
      return editingSessions.get(processDefinition);
   }
}
