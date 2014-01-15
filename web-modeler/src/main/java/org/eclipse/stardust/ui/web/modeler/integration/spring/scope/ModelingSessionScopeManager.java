package org.eclipse.stardust.ui.web.modeler.integration.spring.scope;

import static org.eclipse.stardust.common.CollectionUtils.newConcurrentHashMap;

import java.util.concurrent.ConcurrentMap;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("singleton")
public class ModelingSessionScopeManager
{
   private final ConcurrentMap<String, ModelingSessionScope> scopes = newConcurrentHashMap();

   public ModelingSessionScope createNewSessionScope(String id)
   {
      scopes.putIfAbsent(id, new ModelingSessionScope(id));

      return findScope(id);
   }

   public ModelingSessionScope findScope(String id)
   {
      return scopes.get(id);
   }

   public void destroySessionScope(String id)
   {
      ModelingSessionScope scope = scopes.remove(id);
      if (null != scope)
      {
         scope.dispose();
      }
   }
}
