package org.eclipse.stardust.ui.web.modeler.edit;

import static org.eclipse.stardust.common.CollectionUtils.newConcurrentHashMap;

import java.util.concurrent.ConcurrentMap;

import javax.annotation.Resource;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.ui.web.viewscommon.common.spi.user.impl.IppUserProvider;

@Component
@Scope("singleton")
public class ModelingSessionManager
{
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
            break;
         }
      }
      if (null == session)
      {
         throw new UnsupportedOperationException("Missing ModelingSession prototype bean definition.");
      }

      return session;
   }
}
