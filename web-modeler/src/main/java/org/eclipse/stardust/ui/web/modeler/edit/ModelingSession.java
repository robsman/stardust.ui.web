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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.model.xpdl.builder.common.EObjectUUIDMapper;
import org.eclipse.stardust.model.xpdl.builder.session.EditingSession;
import org.eclipse.stardust.model.xpdl.builder.strategy.ModelManagementStrategy;
import org.eclipse.stardust.ui.web.modeler.common.ModelRepository;
import org.eclipse.stardust.ui.web.modeler.marshaling.ModelElementMarshaller;
import org.eclipse.stardust.ui.web.modeler.marshaling.ModelElementUnmarshaller;

@Component
@Scope("prototype")
public class ModelingSession
{
   private String ownerId;

   private Map<String, User> invitedUsers = newHashMap();

   private Map<String, User> prospectUsers = newHashMap();

   private Map<String, User> collaborators = newHashMap();
   
   private Map<User, Color> joinedUserColor = newHashMap();
   
   private Color ownerColor; 

   private final EditingSession editingSession = new EditingSession();

   private final List<SessionStateListener> stateListeners = newArrayList();

   @Resource
   @Qualifier("default")
   private ModelManagementStrategy modelManagementStrategy;

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

   public synchronized EditingSession getSession(EObject... models)
   {
      for (EObject model : models)
      {
         if ( !editingSession.isTrackingModel(model))
         {
            editingSession.trackModel(model);
         }
      }

      return editingSession;
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
