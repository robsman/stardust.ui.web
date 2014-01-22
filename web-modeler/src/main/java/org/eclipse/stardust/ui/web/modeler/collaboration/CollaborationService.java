package org.eclipse.stardust.ui.web.modeler.collaboration;

import static org.eclipse.stardust.common.CollectionUtils.newConcurrentHashMap;
import static org.eclipse.stardust.common.CollectionUtils.newHashMap;
import static org.eclipse.stardust.common.CollectionUtils.newLinkedList;
import static org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants.TYPE_PROPERTY;

import java.awt.Color;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.Resource;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.api.runtime.UserService;
import org.eclipse.stardust.ui.web.modeler.edit.ModelingSession;
import org.eclipse.stardust.ui.web.modeler.edit.ModelingSessionManager;
import org.eclipse.stardust.ui.web.modeler.service.ModelService;
import org.eclipse.stardust.ui.web.viewscommon.common.spi.user.impl.IppUserProvider;

@Service
@Scope("singleton")
public class CollaborationService
{
   private static final Logger trace = LogManager.getLogger(CollaborationService.class);

   @Resource
   ModelService modelService;

   @Resource
   private ModelingSessionManager sessionManager;

   private final ConcurrentMap<String, SessionInfo> sessionState = newConcurrentHashMap();

   public String getAllCollaborators(String account)
   {
      UserService userService = modelService.getServiceFactory().getUserService();
      User sessionOwner = userService.getUser(account);

      JsonObject allInvitedUsers = new JsonObject();
      allInvitedUsers.addProperty(TYPE_PROPERTY, "UPDATE_INVITED_USERS_COMMAND");
      allInvitedUsers.addProperty("account", account);
      allInvitedUsers.addProperty("timestamp", System.currentTimeMillis());
      allInvitedUsers.addProperty("path", "users");
      allInvitedUsers.addProperty("operation", "updateCollaborators");

      JsonObject old = new JsonObject();

      JsonArray allUsers = new JsonArray();
      ModelingSession currentSession = sessionManager
            .getCurrentSession(getUniqueId(sessionOwner));
      if (null != currentSession)
      {
         SessionInfo info = retrieveCollaborationStatus(currentSession.getId());
         allInvitedUsers.addProperty("ownerColor",
               Integer.toHexString(info.getOwnerColor().getRGB() & 0x00ffffff));
         for (User user : info.getAllCollaborators())
         {
            JsonObject userJson = new JsonObject();
            userJson.addProperty("account", user.getAccount());
            userJson.addProperty("firstName", user.getFirstName());
            userJson.addProperty("lastName", user.getLastName());
            userJson.addProperty("email", user.getEMail());
            userJson.addProperty("imageUrl", "");
            trace.info(">>>>>>>>>>>>>>>> usercolour: "
                  + Integer.toHexString(info.getColor(user).getRGB()));
            userJson.addProperty("color",
                  Integer.toHexString(info.getColor(user).getRGB() & 0x00ffffff));

            allUsers.add(userJson);
         }
      }
      old.add("users", allUsers);
      allInvitedUsers.add("oldObject", old);
      allInvitedUsers.add("newObject", new JsonObject());
      trace.info(">>>>>>>>>>>>>>>> following Json Object will be send: "
            + allInvitedUsers.toString());
      return allInvitedUsers.toString();
   }

   /**
    *
    * @param account
    * @return
    */
   public String getAllProspects(String account)
   {
      UserService userService = modelService.getServiceFactory().getUserService();
      User sessionOwner = userService.getUser(account);

      JsonObject allProspectUsers = new JsonObject();
      allProspectUsers.addProperty(TYPE_PROPERTY, "UPDATE_INVITED_USERS_COMMAND");
      allProspectUsers.addProperty("account", account);
      allProspectUsers.addProperty("timestamp", System.currentTimeMillis());
      allProspectUsers.addProperty("path", "users");
      allProspectUsers.addProperty("operation", "updateProspects");

      JsonObject old = new JsonObject();
      JsonArray allUsers = new JsonArray();

      ModelingSession currentSession = sessionManager
            .getCurrentSession(getUniqueId(sessionOwner));
      if (null != currentSession)
      {
         SessionInfo info = retrieveCollaborationStatus(currentSession.getId());
         for (User user : info.getAllProspects())
         {
            JsonObject userJson = new JsonObject();
            userJson.addProperty("account", user.getAccount());
            userJson.addProperty("firstName", user.getFirstName());
            userJson.addProperty("lastName", user.getLastName());
            userJson.addProperty("email", user.getEMail());
            userJson.addProperty("imageUrl", "");

            allUsers.add(userJson);
         }
      }
      old.add("users", allUsers);
      allProspectUsers.add("oldObject", old);
      allProspectUsers.add("newObject", new JsonObject());
      trace.info(">>>>>>>>>>>>>>>> following Json Object will be send: "
            + allProspectUsers.toString());
      return allProspectUsers.toString();

   }

   /**
    * Checks if a given user was invited to a session yet. Is a possibility to check when
    * the user logs in if he has pending invites.
    *
    * @param user
    *           The username of the user who logged into the portal just recently
    * @return a list of session owners who have invited him to join their modeling session
    */
   public List<String> getUserInvitedToSession(User user)
   {
      List<String> whoseSessions = newLinkedList();
      if (!sessionState.isEmpty())
      {
         for (SessionInfo info : sessionState.values())
         {
            if (info.invitedUsers.containsKey(getUniqueId(user)))
            {
               whoseSessions.add(sessionManager.findById(info.sessionId).getOwnerName());
            }
         }
      }

      return whoseSessions;
   }

   public void requestJoin(String sessionId, String joiner)
   {
      SessionInfo info = retrieveCollaborationStatus(sessionId);
      User currentUser = null;
      for (User user : info.getAllInvited())
      {
         if (user.getAccount().equals(joiner))
         {
            currentUser = user;
            break;
         }
      }
      info.requestJoin(currentUser);
   }

   public void declineInvite(String sessionId, String joiner)
   {
      SessionInfo info = retrieveCollaborationStatus(sessionId);
      User currentUser = null;
      for (User user : info.getAllInvited())
      {
         if (user.getAccount().equals(joiner))
         {
            currentUser = user;
            break;
         }
      }
      info.declineInvite(currentUser);
   }

   public void confirmJoin(String sessionId, String joiner)
   {
      SessionInfo info = retrieveCollaborationStatus(sessionId);
      User currentUser = null;
      for (User user : info.getAllProspects())
      {
         if (user.getAccount().equals(joiner))
         {
            currentUser = user;
            break;
         }
      }
      info.confirmJoin(currentUser);
   }

   public Collection<User> getProspects(String sessionId)
   {
      return retrieveCollaborationStatus(sessionId).getAllProspects();
   }

   public Collection<User> getCollaborators(String sessionId)
   {
      return retrieveCollaborationStatus(sessionId).getAllCollaborators();
   }

   private SessionInfo retrieveCollaborationStatus(String sessionId)
   {
      SessionInfo info = sessionState.get(sessionId);
      if (null == info)
      {
         sessionState.putIfAbsent(sessionId, new SessionInfo(sessionId));
         info = sessionState.get(sessionId);
      }
      return info;
   }

   private static String getUniqueId(User user)
   {
      return IppUserProvider.wrapUser(user).getUID();
   }

   private class SessionInfo
   {
      public final String sessionId;

      private final Map<String, User> invitedUsers = newHashMap();

      private final Map<String, User> prospectUsers = newHashMap();

      private final Map<String, User> collaborators = newHashMap();

      private final Map<User, Color> joinedUserColor = newHashMap();

      private Color ownerColor;

      public SessionInfo(String sessionId)
      {
         this.sessionId = sessionId;

         setOwnerColor(generateColor());
      }

      public Collection<User> getAllProspects()
      {
         return prospectUsers.values();
      }

      public Collection<User> getAllInvited()
      {
         return invitedUsers.values();
      }

      public void setOwnerColor(Color color)
      {
         this.ownerColor = color;

      }

      public Color getOwnerColor()
      {
         return ownerColor;
      }

      public void inviteUser(User user)
      {
         ModelingSession session = sessionManager.findById(sessionId);
         if ((null != session) && !session.isOwner(getUniqueId(user)))
         {
            invitedUsers.put(getUniqueId(user), user);
         }
      }

      public void requestJoin(User user)
      {
         ModelingSession session = sessionManager.findById(sessionId);
         if ((null != session) && !session.isOwner(getUniqueId(user)))
         {
            prospectUsers.put(getUniqueId(user), user);
            invitedUsers.remove(getUniqueId(user));
            // imageUris.put("prospect.getAccount()", );
         }
      }

      public void declineInvite(User user)
      {
         ModelingSession session = sessionManager.findById(sessionId);
         if ((null != session) && !session.isOwner(getUniqueId(user)))
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

            ModelingSession session = sessionManager.findById(sessionId);
            if (null != session)
            {
               session.userJoined(getUniqueId(user));
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
         if (collaborators.containsKey(getUniqueId(user)))
         {
            color = joinedUserColor.get(user);
         }
         return color;
      }

      public Collection<User> getAllCollaborators()
      {
         return (Collection<User>) collaborators.values();
      }
   }
}
