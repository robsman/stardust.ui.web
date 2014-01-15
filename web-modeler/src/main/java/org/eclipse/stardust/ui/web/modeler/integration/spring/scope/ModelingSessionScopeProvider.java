package org.eclipse.stardust.ui.web.modeler.integration.spring.scope;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.Scope;

import org.eclipse.stardust.ui.web.modeler.common.ModelingSessionLocator;
import org.eclipse.stardust.ui.web.modeler.edit.ModelingSession;
import org.eclipse.stardust.ui.web.modeler.spi.ModelingSessionScoped;

/**
 * Provider of modeling session scoped beans.
 *
 * @author Robert.Sauer
 * @see ModelingSessionScoped
 */
public class ModelingSessionScopeProvider implements Scope
{
   private ModelingSessionLocator sessionLocator;

   private ModelingSessionScopeManager scopeManager;

   @Autowired
   public ModelingSessionScopeProvider(ModelingSessionLocator sessionLocator,
         ModelingSessionScopeManager scopeManager)
   {
      this.sessionLocator = sessionLocator;
      this.scopeManager = scopeManager;
   }

   private ModelingSessionScope findScope()
   {
      ModelingSession currentSession = sessionLocator.currentModelingSession();
      if (null != currentSession)
      {
         return scopeManager.findScope(currentSession.getId());
      }

      return null;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Object get(String name, ObjectFactory< ? > objectFactory)
   {
      ModelingSessionScope scope = findScope();

      Object bean = null;

      if (null != scope)
      {
         bean = scope.getBean(name);
         if (null == bean)
         {
            bean = objectFactory.getObject();
            if (null != bean)
            {
               scope.putBean(name, bean);
            }
         }
      }

      return bean;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String getConversationId()
   {
      ModelingSessionScope scope = findScope();

      return (null != scope) ? scope.getScopeId() : null;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void registerDestructionCallback(String name, Runnable callback)
   {
      ModelingSessionScope scope = findScope();

      if (null != scope)
      {
         scope.registerDestructionCallback(name, callback);
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Object remove(String name)
   {
      ModelingSessionScope scope = findScope();

      Object bean = null;

      if (null != scope)
      {
         bean = scope.getBean(name);
         if (null != bean)
         {
            scope.removeBean(name);
         }
      }

      return bean;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Object resolveContextualObject(String key)
   {
      if ("modelingSession".equals(key))
      {
         return sessionLocator.currentModelingSession();
      }

      return null;
   }
}
