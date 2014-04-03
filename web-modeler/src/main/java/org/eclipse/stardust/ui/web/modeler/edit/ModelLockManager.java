package org.eclipse.stardust.ui.web.modeler.edit;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

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
      LockInfo lockInfo = lockRepository.get(modelId);
      return (null != lockInfo) && lockInfo.isLockedBySession(session);
   }

   public boolean isLockedByOther(ModelingSession session, String modelId)
   {
      LockInfo lockInfo = lockRepository.get(modelId);
      return (null != lockInfo) && !lockInfo.isLockedBySession(session);
   }

   public LockInfo getEditLockInfo(String modelId)
   {
      return lockRepository.get(modelId);
   }

   public boolean lockForEditing(ModelingSession session, String modelId)
   {
      if ( !isLockedByMe(session, modelId))
      {
         if ( !lockRepository.containsKey(modelId))
         {
            lockRepository.putIfAbsent(modelId, new LockInfo(modelId, session));
         }
      }
      return isLockedByMe(session, modelId);
   }

   public boolean releaseLock(ModelingSession session, String modelId)
   {
      LockInfo lockInfo = getEditLockInfo(modelId);
      if ((null != lockInfo) && lockInfo.isLockedBySession(session))
      {
         return lockRepository.remove(modelId, lockInfo);
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
      LockInfo lockInfo = lockRepository.get(modelId);
      if ((null != lockInfo) && lockInfo.canBreakEditLock(session))
      {
         return lockRepository.remove(modelId, lockInfo);
      }

      return false;
   }
}
