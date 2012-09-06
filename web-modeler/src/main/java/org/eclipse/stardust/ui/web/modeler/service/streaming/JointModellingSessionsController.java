package org.eclipse.stardust.ui.web.modeler.service.streaming;

import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractAsString;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.atmosphere.cpr.ApplicationConfig;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.cpr.BroadcasterFactory;
import org.atmosphere.cpr.FrameworkConfig;
import org.atmosphere.cpr.HeaderConfig;
import org.atmosphere.websocket.WebSocketEventListenerAdapter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.JsonObject;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.ui.web.modeler.marshaling.JsonMarshaller;
import org.eclipse.stardust.ui.web.modeler.service.ModelServiceFacade;

@Controller
@Scope("prototype")
@RequestMapping("/bpm-modeling/collaboration")
public class JointModellingSessionsController
{
   private static final Logger trace = LogManager.getLogger(JointModellingSessionsController.class);

   @Resource
   private JsonMarshaller jsonIo;

   @Resource
   private ModelServiceFacade modelServiceFacade;

   public static Broadcaster lookupInviteBroadcaster(String userId)
   {
      return lookupInviteBroadcaster(userId, false);
   }

   public static Broadcaster lookupModelChangeBroadcaster(String sessionId)
   {
      return lookupModelChangeBroadcaster(sessionId, false);
   }

   protected static Broadcaster lookupInviteBroadcaster(String userId, boolean createIfMissing)
   {
      return BroadcasterFactory.getDefault().lookup(
            "/services/streaming/bpm-modeling/collaboration/invite/" + userId, createIfMissing);
   }

   protected static Broadcaster lookupModelChangeBroadcaster(String sessionId, boolean createIfMissing)
   {
      return BroadcasterFactory.getDefault().lookup(
            "/services/streaming/bpm-modeling/collaboration/model/" + sessionId, createIfMissing);
   }

   /**
    * This method takes a request to subscribe to the topic.
    *
    * @param request
    * @return ModelAndView
    */
   @RequestMapping(value = "invite/{userId}", method = RequestMethod.GET)
   @ResponseBody
   public String subscribeToInvites(@PathVariable("userId") String userId,
         HttpServletRequest request) throws Exception
   {
      AtmosphereResource resource = (AtmosphereResource) request.getAttribute(FrameworkConfig.ATMOSPHERE_RESOURCE);

      resource.getResponse().setContentType("application/json;charset=UTF-8");

      Broadcaster b = lookupInviteBroadcaster(userId, true);
      resource.setBroadcaster(b);

      trace.info("(Re-)Susbcribing for collaboration invites: " + userId + " (scope: "
            + b.getScope() + ").");

      // enable event logging for resource
      resource.addEventListener(new WebSocketEventListenerAdapter());

      initiateSuspend(request, resource);

      return null;
   }

   /**
    * Takes a request to post data and broadcasts it to everyone else.
    *
    * @param request
    * @return String
    */
   @RequestMapping(value = "invite/{userId}", method = RequestMethod.POST)
   @ResponseBody
   public String broadcastInvite(@PathVariable("userId") String userId,
         HttpServletRequest request) throws Exception
   {
      // @RequestBody does not work (at least on IE) due to missing/invalid content type
      // TODO read fully
      String message = request.getReader().readLine();

      trace.info("About to broadcast collaboration handshake message: " + userId);

      JsonObject obj = jsonIo.readJsonObject(message);

      String msgType = extractAsString(obj, "type");
      if ("ACCEPT_INVITE_COMMAND".equals(msgType))
      {
         String sessionId = extractAsString(obj.getAsJsonObject("newObject"), "sessionId");
         String joiner = extractAsString(obj.getAsJsonObject("newObject"), "account");
         modelServiceFacade.requestJoin(sessionId, joiner);

         for (User user : modelServiceFacade.getCollaborators(sessionId))
         {
            Broadcaster collabs = lookupInviteBroadcaster(user.getAccount());
            collabs.broadcast(message);
         }

         Broadcaster invitee = lookupInviteBroadcaster(joiner);
         invitee.broadcast(message);

         String sessionOwner = extractAsString(obj.getAsJsonObject("oldObject"), "sessionOwner");
         Broadcaster owner = lookupInviteBroadcaster(sessionOwner);
         owner.broadcast(message);
      }
      else if ("DECLINE_INVITE_COMMAND".equals(msgType))
      {
         String sessionId = extractAsString(obj.getAsJsonObject("newObject"), "sessionId");
         String joiner = extractAsString(obj.getAsJsonObject("newObject"), "account");
         modelServiceFacade.declineInvite(sessionId, joiner);

         for (User user : modelServiceFacade.getProspects(sessionId))
         {
            if(joiner.equals(user.getAccount()))
            {
               Broadcaster invitee = lookupInviteBroadcaster(user.getAccount());
               invitee.broadcast(message);
            }
         }

         String sessionOwner = extractAsString(obj.getAsJsonObject("oldObject"), "sessionOwner");
         Broadcaster owner = lookupInviteBroadcaster(sessionOwner);
         owner.broadcast(message);
      }
      else if ("CONFIRM_JOIN_COMMAND".equals(msgType))
      {
         String sessionId = extractAsString(obj.getAsJsonObject("oldObject"), "sessionId");
         String joiner = extractAsString(obj.getAsJsonObject("oldObject"), "account");
         modelServiceFacade.confirmJoin(sessionId, joiner);

         obj.addProperty("modelSession", sessionId);

         for (User user : modelServiceFacade.getCollaborators(sessionId))
         {
            Broadcaster invitee = lookupInviteBroadcaster(user.getAccount());
            invitee.broadcast(obj.toString());
         }

         String sessionOwner = extractAsString(obj, "account");
         Broadcaster owner = lookupInviteBroadcaster(sessionOwner);
         owner.broadcast(obj.toString());
      }
      else
      {
         trace.info("Unsupported message: " + message);
      }

      return null;
   }

   @RequestMapping(value = "model/{sessionId}", method = RequestMethod.GET)
   @ResponseBody
   public String subscribeToModelChanges(@PathVariable("sessionId") String sessionId,
         HttpServletRequest request) throws Exception
   {
      AtmosphereResource resource = (AtmosphereResource) request.getAttribute(FrameworkConfig.ATMOSPHERE_RESOURCE);

      resource.getResponse().setContentType("application/json;charset=UTF-8");

      Broadcaster b = lookupModelChangeBroadcaster(sessionId, true);
      resource.setBroadcaster(b);

      trace.info("(Re-)Susbcribing to collaboration session: " + sessionId);

      // enable event logging for resource
      resource.addEventListener(new WebSocketEventListenerAdapter());

      initiateSuspend(request, resource);

      return null;
   }

   /**
    * Takes a request to post data and broadcasts it to everyone else.
    *
    * @param request
    * @return String
    */
   @RequestMapping(value = "model/{sessionId}", method = RequestMethod.POST)
   @ResponseBody
   public String broadcastModelChange(@PathVariable("sessionId") String sessionId,
         HttpServletRequest request) throws Exception
   {
      if (trace.isDebugEnabled())
      {
         trace.debug("About to broadcast message within collaboration session: "
               + sessionId);
      }

      // @RequestBody does not work (at least on IE) due to missing/invalid content type
      // TODO read fully
      String message = request.getReader().readLine();

      Broadcaster forwarder = lookupModelChangeBroadcaster(sessionId);
      if (null != forwarder)
      {
         if (trace.isDebugEnabled())
         {
            trace.debug("About to broadcast model change: " + message);
         }
         forwarder.broadcast(message);
      }
      else
      {
         trace.info("Skipping broadcast of model change (no subscribers):");
      }

      return null;
   }

   private void initiateSuspend(HttpServletRequest request, AtmosphereResource resource)
   {
      String header = request.getHeader(HeaderConfig.X_ATMOSPHERE_TRANSPORT);
      if (HeaderConfig.LONG_POLLING_TRANSPORT.equalsIgnoreCase(header))
      {
         request.setAttribute(ApplicationConfig.RESUME_ON_BROADCAST, Boolean.TRUE);
         resource.suspend( -1, false);
      }
      else
      {
         resource.suspend( -1);
      }
   }
}