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
package org.eclipse.stardust.ui.web.viewscommon.views.chat;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.User;


/**
 * Application scoped class to hold all current chat rooms
 * 
 * @author Yogesh.Manware
 * @version $Revision: $
 */
public class ChatSystemManager
{
   private static final ChatSystemManager chatSystemManager = new ChatSystemManager();
   private Map<String, ChatRoom> chatRooms;

   /**
    * @return
    */
   public static ChatSystemManager getInstance()
   {
      return chatSystemManager;
   }

   /**
    * default constructor
    */
   private ChatSystemManager()
   {
      chatRooms = new HashMap<String, ChatRoom>();
   }

   /**
    * @param cRoomId
    * @return requested chat room
    */
   public ChatRoom getChatRoom(String cRoomId)
   {
      return chatRooms.get(cRoomId);
   }

   /**
    * create chat room
    * 
    * @return newly created chat room
    */
   public ChatRoom createChatRoom(User owner, ProcessInstance processInstance)
   {
      String id = generateId();
      ChatRoom cRoom = new ChatRoom(id, owner, processInstance);
      chatRooms.put(id, cRoom);
      return cRoom;
   }

   /**
    * @param id
    */
   public void removeChatRoom(String id)
   {
      ChatRoom cRoom = chatRooms.remove(id);
      cRoom.setClosed(true);
   }

   /**
    * @return
    */
   private String generateId()
   {
      Random o = new Random();
      return "CH" + o.nextInt(10000);
   }
}
