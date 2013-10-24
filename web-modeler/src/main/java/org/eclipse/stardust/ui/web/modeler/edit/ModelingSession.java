package org.eclipse.stardust.ui.web.modeler.edit;

import static org.eclipse.stardust.common.CollectionUtils.newArrayList;
import static org.eclipse.stardust.common.CollectionUtils.newHashMap;
import static org.eclipse.stardust.common.CompareHelper.areEqual;
import static org.eclipse.stardust.ui.web.modeler.edit.ModelingSessionManager.getUniqueId;

import java.awt.Color;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.eclipse.emf.ecore.EObject;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.model.xpdl.builder.common.EObjectUUIDMapper;
import org.eclipse.stardust.model.xpdl.builder.session.EditingSession;
import org.eclipse.stardust.model.xpdl.builder.strategy.ModelManagementStrategy;
import org.eclipse.stardust.ui.web.modeler.common.ModelPersistenceService;
import org.eclipse.stardust.ui.web.modeler.common.ModelRepository;
import org.eclipse.stardust.ui.web.modeler.marshaling.ClassLoaderProvider;
import org.eclipse.stardust.ui.web.modeler.marshaling.ModelElementMarshaller;
import org.eclipse.stardust.ui.web.modeler.marshaling.ModelElementUnmarshaller;

@Component
@Scope("prototype")
public class ModelingSession
{
   public static final String SUPERUSER = ModelingSession.class.getName() + ".Superuser";

   private String ownerId;

   private String ownerName;

   private Map<String, Object> sessionAtributes = null;

   private Map<String, User> invitedUsers = newHashMap();

   private Map<String, User> prospectUsers = newHashMap();

   private Map<String, User> collaborators = newHashMap();

   private Map<User, Color> joinedUserColor = newHashMap();

   private Color ownerColor;

   private final EditingSession editingSession = new EditingSession();

   private final List<SessionStateListener> stateListeners = newArrayList();

   @Resource
   private ModelLockManager modelLockManager;

   @Resource(name="webModelerModelManagementStrategy")
   private ModelManagementStrategy modelManagementStrategy;

   @Resource(name="defaultClassLoaderProvider")
   private ClassLoaderProvider classLoaderProvider;

   @Resource
   private ModelPersistenceService modelPersistenceService;

   private ModelRepository modelRepository;

   private ModelElementMarshaller modelElementMarshaller = new ModelElementMarshaller()
   {
      @Override
      protected EObjectUUIDMapper eObjectUUIDMapper()
      {
         return uuidMapper();
      }

      @Override
      protected ModelManagementStrategy modelManagementStrategy()
      {
         return ModelingSession.this.modelManagementStrategy();
      }

      @Override
      protected ModelingSession modelingSession()
      {
         return ModelingSession.this;
      }
   };

   private ModelElementUnmarshaller modelElementUnmarshaller = new ModelElementUnmarshaller()
   {
      @Override
      protected ModelManagementStrategy modelManagementStrategy()
      {
         return ModelingSession.this.modelManagementStrategy();
      }

      @Override
      protected ModelingSession modelingSession()
      {
         return ModelingSession.this;
      }
   };

   public void reset()
   {
      editingSession.reset();
      modelLockManager.releaseLocks(this);
   }

   public String getId()
   {
      return getSession().getId();
   }

   public String getOwnerId()
   {
      return ownerId;
   }

   void setOwnerId(String ownerId)
   {
      this.ownerId = ownerId;
   }

   public String getOwnerName()
   {
      return ownerName;
   }

   void setOwnerName(String ownerName)
   {
      this.ownerName = ownerName;
   }

   public boolean isOwner(String userId)
   {
      return areEqual(ownerId, userId);
   }

   public Object getSessionAttribute(String name)
   {
      return (null != sessionAtributes) ? sessionAtributes.get(name) : null;
   }

   public Object setSessionAttribute(String name, Object value)
   {
      if (null == sessionAtributes)
      {
         this.sessionAtributes = newHashMap();
      }
      return sessionAtributes.put(name, value);
   }

   public ModelManagementStrategy modelManagementStrategy()
   {
      return modelManagementStrategy;
   }

   public ClassLoaderProvider classLoaderProvider()
   {
      return classLoaderProvider;
   }

   /**
    * Currently only used for ORION integration.
    *
    * @param strategy
    */
   public void setModelManagementStrategy(ModelManagementStrategy strategy)
   {
      this.modelManagementStrategy = strategy;
   }

   public ModelPersistenceService modelPersistenceService()
   {
      return modelPersistenceService;
   }

   public ModelRepository modelRepository()
   {
      if (null == modelRepository)
      {
         this.modelRepository = new ModelRepository(this);
      }
      return modelRepository;
   }

   public ModelElementMarshaller modelElementMarshaller()
   {
      return modelElementMarshaller;
   }

   public ModelElementUnmarshaller modelElementUnmarshaller()
   {
      return modelElementUnmarshaller;
   }

   public EObjectUUIDMapper uuidMapper()
   {
      return modelManagementStrategy().uuidMapper();
   }

   public void inviteUser(User user)
   {
      if ( !isOwner(getUniqueId(user)))
      {
         invitedUsers.put(getUniqueId(user), user);
      }
   }

   public void requestJoin(User user)
   {
      if ( !isOwner(getUniqueId(user)))
      {
         prospectUsers.put(getUniqueId(user), user);
         invitedUsers.remove(getUniqueId(user));
         // imageUris.put("prospect.getAccount()", );
      }
   }

   public void declineInvite(User user)
   {
      if ( !isOwner(getUniqueId(user)))
      {
         invitedUsers.remove(getUniqueId(user));
      }
   }

   public void confirmJoin(User user)
   {
      if (prospectUsers.containsKey(getUniqueId(user)))
      {
         collaborators.put(getUniqueId(user), user);
         joinedUserColor.put(user, generateColor());
         prospectUsers.remove(getUniqueId(user));

         for (SessionStateListener listener : stateListeners)
         {
            listener.addedCollaborator(this, user);
         }
      }
   }

   protected Color generateColor()
   {

      float r = (float) (Math.random() * (1 - 0.5) + 0.5);
      float g = (float) (Math.random() * (1 - 0.5) + 0.5);
      float b = (float) (Math.random() * (1 - 0.5) + 0.5);

      Color color = new Color(r, g, b);
      return color;
   }

   public Color getColor(User user)
   {
      Color color = null;
      if(collaborators.containsKey(getUniqueId(user)))
      {
         color = joinedUserColor.get(user);
      }
     return color;
   }

   public synchronized EditingSession getSession()
   {
      return editingSession;
   }

   /**
    * Request a session suitable to perform modifications on the underlying models.
    *
    * @param models
    * @return
    */
   public synchronized EditingSession getEditSession(EObject... models) throws MissingWritePermissionException
   {
      // TODO ensure all previously tracked models can still be edited
      for (EObject model : editingSession.getTrackedModels())
      {
         if ( !ensureEditLock(model, false))
         {
            // TODO improve message
            throw new MissingWritePermissionException(
                  "Failed to re-validate edit lock on model "
                        + this.modelRepository().getModelBinding(model).getModelId(model));
         }
      }


      for (EObject model : models)
      {
         if ( !editingSession.isTrackingModel(model))
         {
            if (ensureEditLock(model, true))
            {
               editingSession.trackModel(model);
            }
            else
            {
               // TODO improve message
               throw new MissingWritePermissionException(
                     "Failed to obtain edit lock on model "
                           + this.modelRepository().getModelBinding(model).getModelId(model));
            }
         }
      }

      return editingSession;
   }

   // TODO can this become a more generic contract? Ideally combined with the check if model needs to be saved at all.
   public boolean canSaveModel(String modelId)
   {
      EObject model = modelRepository().findModel(modelId);
      if (getSession().isTrackingModel(model))
      {
         return ensureEditLock(model, false);
      }
      else
      {
         // if the model is currently not tracked for changes, at least make sure nobody
         // else has an edit lock
         return modelLockManager.isLockedByMe(this, modelId)
               || !modelLockManager.isLockedByOther(this, modelId);
      }
   }

   public boolean releaseEditLock(EObject model)
   {
      String modelId = modelRepository().getModelBinding(model).getModelId(model);
      boolean isLockedByMe = modelLockManager.isLockedByMe(this, modelId);
      if (isLockedByMe)
      {
         modelLockManager.releaseLock(this, modelId);
         return true;
      }
      return false;
   }

   public boolean breakEditLock(EObject model)
   {
      String modelId = modelRepository().getModelBinding(model).getModelId(model);
      return modelLockManager.breakEditLock(this, modelId);
   }

   private boolean ensureEditLock(EObject model, boolean obtainLockIfNeeded)
   {
      String modelId = modelRepository().getModelBinding(model).getModelId(model);
      boolean isLockedByMe = modelLockManager.isLockedByMe(this, modelId);
      if ( !isLockedByMe && obtainLockIfNeeded && !modelLockManager.isLockedByOther(this, modelId))
      {
         isLockedByMe = modelLockManager.lockForEditing(this, modelId);
      }

      return isLockedByMe;
   }

   public LockInfo getEditLockInfo(EObject model)
   {
      String modelId = modelRepository().getModelBinding(model).getModelId(model);
      return modelLockManager.getEditLockInfo(modelId);
   }

   public boolean invitedContainsUser(User user)
   {
      if (invitedUsers.containsKey(getUniqueId(user)))
      {
         return true;
      }
      return false;
   }

   public boolean prospectContainsUser(User user)
   {
      if (prospectUsers.containsKey(getUniqueId(user)))
      {
         return true;
      }
      return false;
   }

   public boolean participantContainsUser(User user)
   {
      if (collaborators.containsKey(getUniqueId(user)))
      {
         return true;
      }
      return false;
   }

   public Collection<User> getAllProspects()
   {
      return (Collection<User>) prospectUsers.values();
   }

   public Collection<User> getAllCollaborators()
   {
      return (Collection<User>) collaborators.values();
   }

   public Collection<User> getAllInvited()
   {
      return (Collection<User>) invitedUsers.values();
   }

   public void addStateListener(SessionStateListener listener)
   {
      if ( !stateListeners.contains(listener))
      {
         stateListeners.add(listener);
      }
   }

   public void removeStateListener(SessionStateListener listener)
   {
      stateListeners.remove(listener);
   }

   public static class SessionStateListener
   {
      public void addedCollaborator(ModelingSession session, User collaborator)
      {
      }

      public void removedCollaborator(ModelingSession session, User collaborator)
      {
      }
   }

   public void setOwnerColor(Color color)
   {
      this.ownerColor = color;

   }

   public Color getOwnerColor()
   {
      return ownerColor;
   }
}
