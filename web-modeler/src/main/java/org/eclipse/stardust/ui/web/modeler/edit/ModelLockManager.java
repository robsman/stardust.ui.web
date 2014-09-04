package org.eclipse.stardust.ui.web.modeler.edit;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.emf.ecore.EObject;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import org.eclipse.stardust.common.CollectionUtils;

@Service
@Scope("singleton")
public class ModelLockManager
{
   private final ConcurrentMap<String, LockInfo> lockRepository = CollectionUtils.newConcurrentHashMap();

   public boolean isLockedByMe(ModelingSession session, String modelId)
   {
      String uniqueModelId = toUniqueModelId(session, modelId);

      LockInfo lockInfo = lockRepository.get(uniqueModelId);
      return (null != lockInfo) && lockInfo.isLockedBySession(session);
   }

   public boolean isLockedByOther(ModelingSession session, String modelId)
   {
      String uniqueModelId = toUniqueModelId(session, modelId);

      LockInfo lockInfo = lockRepository.get(uniqueModelId);
      return (null != lockInfo) && !lockInfo.isLockedBySession(session);
   }

   public LockInfo getEditLockInfo(ModelingSession session, String modelId)
   {
      String uniqueModelId = toUniqueModelId(session, modelId);

      return lockRepository.get(uniqueModelId);
   }

   public boolean lockForEditing(ModelingSession session, String modelId)
   {
      if ( !isLockedByMe(session, modelId))
      {
         String uniqueModelId = toUniqueModelId(session, modelId);

         if ( !lockRepository.containsKey(uniqueModelId))
         {
            lockRepository.putIfAbsent(uniqueModelId, new LockInfo(modelId, session));
         }
      }
      return isLockedByMe(session, modelId);
   }

   public boolean releaseLock(ModelingSession session, String modelId)
   {
      LockInfo lockInfo = getEditLockInfo(session, modelId);
      if ((null != lockInfo) && lockInfo.isLockedBySession(session))
      {
         String uniqueModelId = toUniqueModelId(session, modelId);

         return lockRepository.remove(uniqueModelId, lockInfo);
      }
      else
      {
         return false;
      }
   }

   public void releaseLocks(ModelingSession session)
   {
      for (Iterator<Map.Entry<String, LockInfo>> i = lockRepository.entrySet().iterator(); i
            .hasNext();)
      {
         Map.Entry<String, LockInfo> entry = i.next();
         if (entry.getValue().sessionId.equals(session.getId()))
         {
            i.remove();
         }
      }
   }

   public boolean breakEditLock(ModelingSession session, String modelId)
   {
      String uniqueModelId = toUniqueModelId(session, modelId);

      LockInfo lockInfo = lockRepository.get(uniqueModelId);
      if ((null != lockInfo) && lockInfo.canBreakEditLock(session))
      {
         return lockRepository.remove(uniqueModelId, lockInfo);
      }

      return false;
   }

   private static String toUniqueModelId(ModelingSession session, String modelId)
   {
      EObject model = session.modelRepository().findModel(modelId);
      return session.modelRepository().getUniqueModelId(model);
   }
}
