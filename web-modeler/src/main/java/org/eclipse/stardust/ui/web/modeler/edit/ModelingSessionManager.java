package org.eclipse.stardust.ui.web.modeler.edit;

import static org.eclipse.stardust.common.CollectionUtils.newConcurrentHashMap;
import static org.eclipse.stardust.common.CollectionUtils.newLinkedList;

import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.Resource;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.ui.web.viewscommon.common.spi.user.impl.IppUserProvider;

@Component
@Scope("singleton")
public class ModelingSessionManager
{
   private static final Logger trace = LogManager.getLogger(ModelingSessionManager.class);

   @Resource
   private ConfigurableListableBeanFactory context;

   // session by ID
   private ConcurrentMap<String, ModelingSession> sessions = newConcurrentHashMap();

   // session by owner
   private ConcurrentMap<String, ModelingSession> userSessions = newConcurrentHashMap();

   // collaboration session per user (at most one per user)
   private ConcurrentMap<String, ModelingSession> collaborations = newConcurrentHashMap();

   public static String getUniqueId(User user)
   {
      return IppUserProvider.wrapUser(user).getUID();
   }

   public ModelingSession findById(String sessionId)
   {
      return sessions.get(sessionId);
   }

   public ModelingSession currentSession(User user)
   {
      return currentSession(getUniqueId(user));
   }

   public ModelingSession currentSession(String userId)
   {
      // prefer an active collaboration
      ModelingSession session = collaborations.get(userId);
      if (null == session)
      {
         // if not collaborating, use the user's original session
         session = userSessions.get(userId);
         if (null == session)
         {
            // no original session yet, start a new one
            userSessions.putIfAbsent(userId, createSession(userId));
            session = userSessions.get(userId);

            // the real user session should be tracked by ID as well
            sessions.putIfAbsent(session.getId(), session);
         }
      }

      return session;
   }

   /**
    * @param userId
    * TODO - pending review
    */
   public void destroySession(String userId)
   {
      ModelingSession session = collaborations.get(userId);
      if (null != session)
      {
         collaborations.remove(userId);
      }
      else
      {
         session = userSessions.get(userId);
         if (null != session)
         {
            userSessions.remove(userId);
         }
      }
   }

   private ModelingSession createSession(String userId)
   {
      ModelingSession session = null;
      // modeling
      for (String beanName : context.getBeanNamesForType(ModelingSession.class))
      {
         if (context.isPrototype(beanName))
         {
            session = context.getBean(beanName, ModelingSession.class);
            session.setOwnerId(userId);
            session.setOwnerColor(session.generateColor());
            session.addStateListener(new ModelingSession.SessionStateListener()
            {
               @Override
               public void addedCollaborator(ModelingSession session, User collaborator)
               {
                  if (null != collaborations.putIfAbsent(getUniqueId(collaborator), session))
                  {
                     trace.warn("User " + collaborator
                           + " must not collaborate in more than one modeling session.");
                  }
               }

               @Override
               public void removedCollaborator(ModelingSession session, User collaborator)
               {
                  if ( !collaborations.remove(getUniqueId(collaborator), session))
                  {
                     trace.warn("User "
                           + collaborator
                           + " is currently collaborating in a separate modeling session.");
                  }
               }

            });
            break;
         }
      }
      if (null == session)
      {
         throw new UnsupportedOperationException("Missing ModelingSession prototype bean definition.");
      }

      return session;
   }

   /**
    * Checks if a given user was invited to a session yet. Is a possibility to
    * check when the user logs in if he has pending invites.
    *
    * @param user
    *            The username of the user who logged into the portal just
    *            recently
    * @return a list of session owners who have invited him to join their
    *         modeling session
    */
   public List<String> getUserInvitedToSession(User user)
   {
      List<String> whoseSessions = newLinkedList();
      if ( !userSessions.isEmpty())
      {
         for (Entry<String, ModelingSession> entry : userSessions.entrySet())
         {
            if (entry.getValue().invitedContainsUser(user))
            {
               whoseSessions.add(entry.getKey());
            }
         }
      }
      return whoseSessions;
   }
}
