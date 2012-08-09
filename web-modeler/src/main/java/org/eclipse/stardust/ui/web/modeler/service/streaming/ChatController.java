package org.eclipse.stardust.ui.web.modeler.service.streaming;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

@Controller
@Scope("prototype")
@RequestMapping("/chat")
public class ChatController
{
   /**
    * This method takes a request to subscribe to the topic.
    *
    * @param request
    * @return ModelAndView
    */
   @RequestMapping(value = "{topicId}", method = RequestMethod.GET)
   @ResponseBody
   public String subscribe(@PathVariable("topicId") String topicId,
         HttpServletRequest request) throws Exception
   {
      AtmosphereResource resource = (AtmosphereResource) request.getAttribute(FrameworkConfig.ATMOSPHERE_RESOURCE);

      this.doGet(topicId, resource, resource.getResponse());

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

      return null;
   }

   /**
    * Takes a request to post data and broadcasts it to everyone else.
    *
    * @param request
    * @return String
    */
   @RequestMapping(value = "{topicId}", method = RequestMethod.POST)
   @ResponseBody
   public String broadcastMessage(@PathVariable("topicId") String topicId,
         HttpServletRequest request) throws Exception
   {
      // @RequestBody does not work (at least on IE) due to missing/invalid content type
      String message = request.getReader().readLine();

      this.doPost(topicId, message);

      return null;
   }

   // See AtmosphereHandlerPubSub example - same code as GET
   private void doGet(String topicId, AtmosphereResource r, HttpServletResponse response)
   {
      // Log all events on the console, including WebSocket events.
      r.addEventListener(new WebSocketEventListenerAdapter());

      response.setContentType("application/json;charset=UTF-8");

      Broadcaster b = lookupBroadcaster(topicId);
      r.setBroadcaster(b);
   }

   // See AtmosphereHandlerPubSub example - same code as POST
   private void doPost(String topicId, String message) throws IOException
   {
      Broadcaster b = lookupBroadcaster(topicId);

      if (message != null && message.indexOf("message") != -1)
      {
         b.broadcast(message);
      }
   }

   /**
    * Retrieve the {@link Broadcaster} based on the request's path info.
    *
    * @param pathInfo
    * @return the {@link Broadcaster} based on the request's path info.
    */
   Broadcaster lookupBroadcaster(String pathInfo)
   {
      if (pathInfo == null)
      {
         return BroadcasterFactory.getDefault().lookup("/", true);
      }
      else
      {
         String[] decodedPath = pathInfo.split("/");
         return BroadcasterFactory.getDefault().lookup(
               decodedPath[decodedPath.length - 1], true);
      }
   }

}