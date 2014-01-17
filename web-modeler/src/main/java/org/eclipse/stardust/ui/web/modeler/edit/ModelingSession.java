package org.eclipse.stardust.ui.web.modeler.edit;

import static org.eclipse.stardust.common.CollectionUtils.newHashMap;
import static org.eclipse.stardust.common.CompareHelper.areEqual;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.Resource;

import org.eclipse.emf.ecore.EObject;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import org.eclipse.stardust.model.xpdl.builder.common.EObjectUUIDMapper;
import org.eclipse.stardust.model.xpdl.builder.session.EditingSession;
import org.eclipse.stardust.model.xpdl.builder.strategy.ModelManagementStrategy;
import org.eclipse.stardust.ui.web.modeler.common.ModelPersistenceService;
import org.eclipse.stardust.ui.web.modeler.common.ModelRepository;
import org.eclipse.stardust.ui.web.modeler.marshaling.ClassLoaderProvider;
import org.eclipse.stardust.ui.web.modeler.marshaling.ModelMarshaller;
import org.eclipse.stardust.ui.web.modeler.marshaling.ModelUnmarshaller;
import org.eclipse.stardust.ui.web.modeler.spi.ModelFormat;
import org.eclipse.stardust.ui.web.modeler.spi.ModelingSessionScoped;

@Component
@Primary
@ModelingSessionScoped
public final class ModelingSession
{
   public static final String SUPERUSER = ModelingSession.class.getName() + ".Superuser";

   private String ownerId;

   private String ownerName;

   private Map<String, Object> sessionAtributes = null;

   private final EditingSession editingSession = new EditingSession();

   private final List<SessionStateListener> stateListeners = new CopyOnWriteArrayList<ModelingSession.SessionStateListener>();

   @Resource
   private ModelLockManager modelLockManager;

   @Resource(name="webModelerModelManagementStrategy")
   private ModelManagementStrategy modelManagementStrategy;

   @Resource(name="defaultClassLoaderProvider")
   private ClassLoaderProvider classLoaderProvider;

   @Resource
   private ModelPersistenceService modelPersistenceService;

   @Resource
   private ModelRepository modelRepository;

   @Resource
   @ModelFormat(ModelFormat.XPDL)
   private ModelMarshaller xpdlMarshaller;

   @Resource
   @ModelFormat(ModelFormat.XPDL)
   private ModelUnmarshaller xpdlUnmarshaller;

   public void reset()
   {
      editingSession.reset();
      modelLockManager.releaseLocks(this);
   }

   public String getId()
   {
      return editingSession.getId();
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
      return modelRepository;
   }

   public ModelMarshaller xpdlMarshaller()
   {
      return xpdlMarshaller;
   }

   public ModelUnmarshaller xpdlUnmarshaller()
   {
      return xpdlUnmarshaller;
   }

   public EObjectUUIDMapper uuidMapper()
   {
      return modelManagementStrategy().uuidMapper();
   }

   public EditingSession getSession()
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
      if (editingSession.isTrackingModel(model))
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
      public void addedCollaborator(ModelingSession session, String userId)
      {
      }

      public void removedCollaborator(ModelingSession session, String userId)
      {
      }
   }

   public void userJoined(String userId)
   {
      for (SessionStateListener listener : stateListeners)
      {
         listener.addedCollaborator(this, userId);
      }
   }

   public void userLeft(String userId)
   {
      for (SessionStateListener listener : stateListeners)
      {
         listener.removedCollaborator(this, userId);
      }
   }
}
