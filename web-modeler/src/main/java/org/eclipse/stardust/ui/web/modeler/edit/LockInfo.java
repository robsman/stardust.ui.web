package org.eclipse.stardust.ui.web.modeler.edit;

public class LockInfo
{
   public final String modelId;

   public final String sessionId;

   public final String ownerId;

   public final String ownerName;

   LockInfo(String modelId, ModelingSession session)
   {
      this.modelId = modelId;
      this.sessionId = session.getId();
      this.ownerId = session.getOwnerId();
      this.ownerName = session.getOwnerName();
   }

   public boolean isLockedBySession(ModelingSession session)
   {
      return sessionId.equals(session.getId());
   }

   public boolean canBreakEditLock(ModelingSession session)
   {
      if (isLockedBySession(session) || ownerId.equals(session.getOwnerId()))
      {
         return true;
      }
      else if (Boolean.TRUE
            .equals(session.getSessionAttribute(ModelingSession.SUPERUSER)))
      {
         // break lock if current user is admin
         return true;
      }

      return false;
   }
}