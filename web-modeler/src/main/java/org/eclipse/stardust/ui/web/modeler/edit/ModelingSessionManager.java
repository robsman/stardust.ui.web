package org.eclipse.stardust.ui.web.modeler.edit;

import static org.eclipse.stardust.common.CollectionUtils.newConcurrentHashMap;
import static org.eclipse.stardust.common.StringUtils.isEmpty;

import java.util.concurrent.ConcurrentMap;

import javax.annotation.Resource;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.ui.web.modeler.common.UserIdProvider;
import org.eclipse.stardust.ui.web.modeler.integration.spring.scope.ModelingSessionScope;
import org.eclipse.stardust.ui.web.modeler.integration.spring.scope.ModelingSessionScopeManager;

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

   public ModelingSession findById(String sessionId)
   {
      return sessions.get(sessionId);
   }

   public ModelingSession getCurrentSession(String userId)
   {
      // prefer an active collaboration
      ModelingSession session = collaborations.get(userId);
      if (null == session)
      {
         // if not collaborating, use the user's original session
         session = userSessions.get(userId);
      }

      return session;
   }

   public ModelingSession getOrCreateSession(UserIdProvider userIdProvider)
   {
      String userId = userIdProvider.getCurrentUserId();

      // prefer an active collaboration
      ModelingSession session = getCurrentSession(userId);
      if (null == session)
      {
         // no original session yet, start a new one
         createSession(userId, userIdProvider.getCurrentUserDisplayName());
         session = userSessions.get(userId);
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
            sessions.remove(session.getId(), session);
            session.reset();
         }
      }
   }

   /**
    * Creates a new modeling session for the given user.
    * <p>
    * The new session will not return directly but should be retrieved from the
    * {@link #userSessions} map afterwards (to cater for concurrent creation attempts).
    *
    * @param userId
    * @param userName
    */
   private void createSession(String userId, String userName)
   {
      String beanName = null;
      for (String candidateName : context.getBeanNamesForType(ModelingSession.class))
      {
         if (context.getBeanDefinition(candidateName).isPrimary())
         {
            beanName = candidateName;
            break;
         }
      }

      if (!isEmpty(beanName))
      {
         // manually creating a raw instance, to have a chance to establish a new
         // modeling session scope
         ModelingSession session = new ModelingSession();

         // only proceed fully if this is the new primary session for the user
         if (null == userSessions.putIfAbsent(userId, session))
         {
            // track session also by id
            sessions.putIfAbsent(session.getId(), session);

            // establish the associated Spring scope
            ModelingSessionScopeManager sessionScopeProvider = context
                  .getBean(ModelingSessionScopeManager.class);
            ModelingSessionScope newScope = sessionScopeProvider
                  .createNewSessionScope(session.getId());
            newScope.putBean(beanName, session);

            // now fully initialize the instance, which potentially involves other
            // modeling sessions scoped beans
            context.configureBean(session, beanName);

            session.setOwnerId(userId);
            session.setOwnerName(userName);

            session.addStateListener(new ModelingSession.SessionStateListener()
            {
               @Override
               public void addedCollaborator(ModelingSession session, String userId)
               {
                  if (null != collaborations.putIfAbsent(userId, session))
                  {
                     trace.warn("User " + userId
                           + " must not collaborate in more than one modeling session.");
                  }
               }

               @Override
               public void removedCollaborator(ModelingSession session, String userId)
               {
                  if ( !collaborations.remove(userId, session))
                  {
                     trace.warn("User "
                           + userId
                           + " is currently collaborating in a separate modeling session.");
                  }
               }

            });
         }
      }
      else
      {
         throw new UnsupportedOperationException("Missing ModelingSession bean definition.");
      }
   }
}
