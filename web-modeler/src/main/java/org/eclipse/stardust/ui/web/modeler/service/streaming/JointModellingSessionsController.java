package org.eclipse.stardust.ui.web.modeler.service.streaming;

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
import org.eclipse.stardust.ui.web.modeler.marshaling.JsonMarshaller;

@Controller
@Scope("prototype")
@RequestMapping("/bpm-modeling/collaboration")
public class JointModellingSessionsController
{
   private static final Logger trace = LogManager.getLogger(JointModellingSessionsController.class);

   @Resource
   private JsonMarshaller jsonIo;

   /**
    * This method takes a request to subscribe to the topic.
    *
    * @param request
    * @return ModelAndView
    */
   @RequestMapping(value = "{sessionId}", method = RequestMethod.GET)
   @ResponseBody
   public String subscribe(@PathVariable("sessionId") String sessionId,
         HttpServletRequest request) throws Exception
   {
      AtmosphereResource resource = (AtmosphereResource) request.getAttribute(FrameworkConfig.ATMOSPHERE_RESOURCE);

      resource.getResponse().setContentType("application/json;charset=UTF-8");

      Broadcaster b = lookupBroadcaster(sessionId);
      resource.setBroadcaster(b);

      trace.info("(Re-)Susbcribing to collaboration session: " + sessionId + " (scope: "
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
   @RequestMapping(value = "{sessionId}", method = RequestMethod.POST)
   @ResponseBody
   public String broadcastMessage(@PathVariable("sessionId") String sessionId,
         HttpServletRequest request) throws Exception
   {
      // @RequestBody does not work (at least on IE) due to missing/invalid content type
      // TODO read fully
      String message = request.getReader().readLine();

      Broadcaster b = this.lookupBroadcaster(sessionId);

      trace.info("About to broadcast message within collaboration session: " + sessionId
            + " (scope: " + b.getScope() + ").");

      JsonObject obj = jsonIo.readJsonObject(message);

      if (null != b)
      {
         b.broadcast(message);
      }

      return null;
   }

   private Broadcaster lookupBroadcaster(String sessionId)
   {
      return BroadcasterFactory.getDefault().lookup(
            "/bpm-modelling/collaboration/" + sessionId, true);
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