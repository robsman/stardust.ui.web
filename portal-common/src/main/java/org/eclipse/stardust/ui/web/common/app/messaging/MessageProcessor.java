/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.common.app.messaging;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.message.MessageDialog;
import org.eclipse.stardust.ui.web.common.util.GsonUtils;
import org.eclipse.stardust.ui.web.common.util.MessagePropertiesBean;
import org.eclipse.stardust.ui.web.common.util.StringUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author Subodh.Godbole
 *
 */
public class MessageProcessor implements MessageTypeConstants
{
   private static final Logger trace = LogManager.getLogger(MessageProcessor.class);

   /**
    * @param jsonMessage
    * @throws MessageProcessingException
    */
   public static void processs(String jsonMessage) throws MessageProcessingException
   {
      if (StringUtils.isEmpty(jsonMessage))
         return;

      try
      {
         List<Message> messages = new ArrayList<Message>();
         JsonElement jsonElem = GsonUtils.readJsonElement(jsonMessage);
         if (jsonElem.isJsonObject())
         {
            messages.add(createMessage(jsonElem.getAsJsonObject()));
         }
         else if (jsonElem.isJsonArray())
         {
            JsonElement oneMsgElem;
            Iterator<JsonElement> it = jsonElem.getAsJsonArray().iterator();
            while (it.hasNext())
            {
               oneMsgElem = it.next();
               if (oneMsgElem.isJsonObject())
               {
                  messages.add(createMessage(oneMsgElem.getAsJsonObject()));
               }
               else
               {
                  trace.warn("Invalid Message. Ignoring it.");
               }
            }
         }
         else
         {
            trace.warn("Invalid Message. Ignoring it.");
         }

         for (Message message : messages)
         {
            //TODO: Before processing next message, focus view may need to be adjusted / resetted
            MessageProcessor.processs(message);
         }
      }
      catch (Exception e)
      {
         if (e instanceof MessageProcessingException)
         {
            throw (MessageProcessingException)e;
         }
         else
         {
            throw new MessageProcessingException(e);
         }
      }
   }

   /**
    * @param message
    * @throws MessageProcessingException
    */
   public static void processs(Message message) throws MessageProcessingException
   {
      if (null == message)
         return;

      try
      {
         if (T_OPEN_VIEW.equalsIgnoreCase(message.getType()))
         {
            String viewId = GsonUtils.extractString(message.getData(), D_VIEW_ID);
            String viewKey = GsonUtils.extractString(message.getData(), D_VIEW_KEY);
            Map<String, Object> params = GsonUtils.extractMap(message.getData(), D_VIEW_PARAMS);
            Boolean nested = GsonUtils.extractBoolean(message.getData(), D_NESTED, false);
   
            PortalApplication.getInstance().openViewById(viewId, viewKey, params, null, nested);
         }
         else if (T_CHANGE_PERSPECTIVE.equalsIgnoreCase(message.getType()))
         {
            String perspectiveId = GsonUtils.extractString(message.getData(), D_PERSPECTIVE_ID);
            Map<String, Object> params = GsonUtils.extractMap(message.getData(), D_VIEW_PARAMS);
            PortalApplication.getInstance().getPortalUiController().loadPerspective(perspectiveId, params);
         }
         else
         {
            // TODO: It's possible to keep a list of listeners and propagate the message further 
            // to listeners on custom Perspectives or Views
            MessageDialog.addErrorMessage(MessagePropertiesBean.getInstance().getString(
                  "portalFramework.error.messageNotSupported"));
         }
      }
      catch (Exception e)
      {
         throw new MessageProcessingException(e);
      }
   }

   /**
    * @param msgObject
    * @return
    */
   private static Message createMessage(JsonObject msgObject )
   {
      String command = GsonUtils.extractString(msgObject, "type");
      JsonObject data = GsonUtils.extractObject(msgObject, "data");
      return new Message(command, data);
   }
}
