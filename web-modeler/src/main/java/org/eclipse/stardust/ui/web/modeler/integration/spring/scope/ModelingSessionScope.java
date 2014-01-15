package org.eclipse.stardust.ui.web.modeler.integration.spring.scope;

import static org.eclipse.stardust.common.CollectionUtils.newConcurrentHashMap;

import java.util.concurrent.ConcurrentMap;

public class ModelingSessionScope
{
   private final String id;

   private final ConcurrentMap<String, Object> beans = newConcurrentHashMap();

   private final ConcurrentMap<String, Runnable> destructionCallbacks = newConcurrentHashMap();

   public ModelingSessionScope(String id)
   {
      this.id = id;
   }

   public void dispose()
   {
      while (!beans.isEmpty())
      {
         removeBean(beans.keySet().iterator().next());
      }
   }

   public String getScopeId()
   {
      return id;
   }

   public Object getBean(String name)
   {
      return beans.get(name);
   }

   public void putBean(String name, Object bean)
   {
      if (null != beans.putIfAbsent(name, bean))
      {
         // TODO throw if there already was a bean?
      }
   }

   public void removeBean(String name)
   {
      beans.remove(name);

      Runnable callback = destructionCallbacks.remove(name);
      if (null != callback)
      {
         callback.run();
      }
   }

   public void registerDestructionCallback(String name, Runnable callback)
   {
      if (null != destructionCallbacks.putIfAbsent(name, callback))
      {
         // TODO throw if there already was a callback?
      }
   }
}