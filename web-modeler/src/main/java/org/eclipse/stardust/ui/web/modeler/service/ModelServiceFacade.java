package org.eclipse.stardust.ui.web.modeler.service;

import java.util.Collection;

import javax.annotation.Resource;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.ui.web.modeler.edit.ModelingSession;
import org.eclipse.stardust.ui.web.modeler.edit.ModelingSessionManager;

@Component
@Scope("singleton")
public class ModelServiceFacade
{
   @Resource
   private ModelingSessionManager sessionManager;

   public void requestJoin(String sessionId, String joiner)
   {
      ModelingSession collaborationSession = sessionManager.findById(sessionId);
      User currentUser = null;
      for (User user : collaborationSession.getAllInvited())
      {
         if (user.getAccount().equals(joiner))
         {
            currentUser = user;
            break;
         }
      }
      collaborationSession.requestJoin(currentUser);
   }

   public void declineInvite(String sessionId, String joiner)
   {
      ModelingSession collaborationSession = sessionManager.findById(sessionId);
      User currentUser = null;
      for (User user : collaborationSession.getAllInvited())
      {
         if (user.getAccount().equals(joiner))
         {
            currentUser = user;
            break;
         }
      }
      collaborationSession.declineInvite(currentUser);
   }

   public void confirmJoin(String sessionId, String joiner)
   {
      ModelingSession collaborationSession = sessionManager.findById(sessionId);
      User currentUser = null;
      for (User user : collaborationSession.getAllProspects())
      {
         if (user.getAccount().equals(joiner))
         {
            currentUser = user;
            break;
         }
      }
      collaborationSession.confirmJoin(currentUser);
   }

   public Collection<User> getProspects(String sessionId)
   {
      ModelingSession collaborationSession = sessionManager.findById(sessionId);
      return collaborationSession.getAllProspects();
   }

   public Collection<User> getCollaborators(String sessionId)
   {
      ModelingSession collaborationSession = sessionManager.findById(sessionId);
      return collaborationSession.getAllCollaborators();
   }
}
