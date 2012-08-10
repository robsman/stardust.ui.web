package org.eclipse.stardust.ui.web.modeler.edit;

import static org.eclipse.stardust.common.CollectionUtils.newConcurrentHashMap;

import java.util.concurrent.ConcurrentMap;

import javax.annotation.Resource;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import org.eclipse.stardust.model.xpdl.builder.session.EditingSession;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;

@Component
@Scope("singleton")
public class ModelingSessionManager
{
   @Resource
   private ConfigurableListableBeanFactory context;

   private ConcurrentMap<String, ModelingSession> sessions = newConcurrentHashMap();

   private ConcurrentMap<String, ModelingSession> collaborations = newConcurrentHashMap();

   public ModelingSession currentSession(String userId)
   {
      // prefer an active collaboration
      ModelingSession session = collaborations.get(userId);
      if (null == session)
      {
         // if not collaborating, use the user's original session
         session = sessions.get(userId);
         if (null == session)
         {
            // no original session yet, start a new one
            sessions.putIfAbsent(userId, createSession(userId));
            session = sessions.get(userId);
         }
      }

      return session;
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
