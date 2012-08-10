package org.eclipse.stardust.ui.web.modeler.edit;

import static org.eclipse.stardust.common.CollectionUtils.newHashMap;
import static org.eclipse.stardust.common.CompareHelper.areEqual;

import java.util.Map;
import java.util.UUID;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.model.xpdl.builder.common.EObjectUUIDMapper;
import org.eclipse.stardust.model.xpdl.builder.session.EditingSession;
import org.eclipse.stardust.model.xpdl.builder.strategy.ModelManagementStrategy;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.ui.web.modeler.marshaling.ModelElementMarshaller;
import org.eclipse.stardust.ui.web.modeler.marshaling.ModelElementUnmarshaller;

@Component
@Scope("prototype")
public class ModelingSession
{
   private String ownerId;

   private Map<String, User> prospectUsers = newHashMap();

   private Map<String, User> collaborators = newHashMap();

   /**
    * see {@link #createEditingSession()}
    */
   private EditingSession editingSession;

   @Resource
   @Qualifier("default")
   private ModelManagementStrategy modelManagementStrategy;

   private ModelElementMarshaller modelElementMarshaller = new ModelElementMarshaller()
   {
      @Override
      protected EObjectUUIDMapper eObjectUUIDMapper()
      {
         return uuidMapper();
      }
   };

   private ModelElementUnmarshaller modelElementUnmarshaller = new ModelElementUnmarshaller()
   {
      @Override
      protected ModelManagementStrategy modelManagementStrategy()
      {
         return ModelingSession.this.modelManagementStrategy();
      }
   };

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

   public boolean isOwner(String userId)
   {
      return areEqual(ownerId, userId);
   }

   public ModelManagementStrategy modelManagementStrategy()
   {
      return modelManagementStrategy;
   }

   @Deprecated
   public void setModelManagementStrategy(ModelManagementStrategy strategy)
   {
      this.modelManagementStrategy = strategy;
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

   public void requestJoin(User user)
   {
      if ( !isOwner(user.getAccount()))
      {
         prospectUsers.put(user.getAccount(), user);
         // imageUris.put("prospect.getAccount()", );
      }
   }

   public void confirmJoin(User user)
   {
      if (prospectUsers.containsKey(user.getAccount()))
      {
         collaborators.put(user.getAccount(), user);
         prospectUsers.remove(user.getAccount());
      }
   }

   public synchronized EditingSession getSession(ModelType... models)
   {
      if (null == editingSession)
      {
         createEditingSession();
      }

      for (ModelType model : models)
      {
         if ( !editingSession.isTrackingModel(model))
         {
            editingSession.trackModel(model);
         }
      }

      return editingSession;
   }

   private EditingSession createEditingSession()
   {
      if (null == editingSession)
      {
         editingSession = new EditingSession();
      }
      return editingSession;
   }
}
