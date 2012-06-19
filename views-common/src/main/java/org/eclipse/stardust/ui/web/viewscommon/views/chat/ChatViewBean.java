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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.faces.event.ActionEvent;
import javax.swing.tree.DefaultMutableTreeNode;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.query.UserQuery;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.ui.web.common.UIComponentBean;
import org.eclipse.stardust.ui.web.common.app.View;
import org.eclipse.stardust.ui.web.common.event.ViewEvent;
import org.eclipse.stardust.ui.web.common.event.ViewEventHandler;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentViewUtil;
import org.eclipse.stardust.ui.web.viewscommon.services.ContextPortalServices;
import org.eclipse.stardust.ui.web.viewscommon.user.UserWrapper;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.UserUtils;
import org.eclipse.stardust.ui.web.viewscommon.views.doctree.RepositoryDocumentUserObject;


import com.icesoft.faces.component.dragdrop.DropEvent;

/**
 * @author Yogesh.Manware
 * @version $Revision: $
 */
public class ChatViewBean extends UIComponentBean implements ViewEventHandler
{
   private static final long serialVersionUID = 1L;
   private ProcessInstance processInstance;
   private User user;
   private ChatRoom chatRoom;
   private String userSearchKey;
   private List<User> searchResult;
   private List<UserWrapper> afterLogoutList;
   private List<ChatMessage> afterLogoutChatMessages;

   // Spring configuration injected bean
   private IMessenger messenger;
   private ChatCloseConfirmationPopup confirmationPopup;

   /**
    * default constructor
    */
   public ChatViewBean()
   {
      super("chatView");
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.stardust.ui.web.common.UIComponentBean#initialize()
    */
   public void initialize()
   {}

   /**
    * @param view
    */
   public void initialize(View view)
   {
      // set process instance
      String procInstOID = (String) view.getViewParams().get("processInstanceOID");
      if (StringUtils.isNotEmpty(procInstOID))
      {
         processInstance = ProcessInstanceUtils.getProcessInstance(Long.valueOf(procInstOID));
      }
      // set User
      user = ContextPortalServices.getUser();

      // set ChatRoom
      String chatRoomId = (String) view.getViewParams().get("chatRoomId");
      ChatSystemManager csm = ChatSystemManager.getInstance();
      if (StringUtils.isNotEmpty(chatRoomId))
      {
         chatRoom = csm.getChatRoom(chatRoomId);
      }
      else
      {
         chatRoom = csm.createChatRoom(user, processInstance);
      }
   }

   /**
    * find user
    */
   public void searchUser()
   {
      searchResult = new ArrayList<User>();
      userSearchKey = userSearchKey.toLowerCase();
      UserQuery userQuery = UserQuery.findActive();
      List<User> allUserList = ContextPortalServices.getQueryService().getAllUsers(userQuery);

      for (User user : allUserList)
      {
         if (user.getLastName().toLowerCase().startsWith(userSearchKey)
               || user.getFirstName().toLowerCase().startsWith(userSearchKey))
         {
            searchResult.add(user);
         }
      }
   }

   /**
    * @param event
    */
   public void sendMessage(ActionEvent event)
   {
      if (StringUtils.isNotEmpty(messenger.getContent().trim()))
      {
         String content = cleanMessage(messenger.getContent());
         ChatMessage cMsg = new ChatMessage(UserUtils.getUserDisplayLabel(user), chatRoom.getUsersIndex(user), content);
         chatRoom.addChatMessages(cMsg);
         messenger.setContent("");
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.eclipse.stardust.ui.web.common.event.ViewEventHandler#handleEvent(com.sungard.framework
    * .ui.event.ViewEvent)
    */
   public void handleEvent(ViewEvent event)
   {
      if (event.getType().equals(ViewEvent.ViewEventType.CREATED))
      {
         initialize(event.getView());
      }

      else if (event.getType().equals(ViewEvent.ViewEventType.TO_BE_CLOSED))
      {
         if ((!chatRoom.isClosed() && chatRoom.isUserStillInChat()) && !confirmationPopup.isConfirmed())
         {
            event.setVetoed(true);
            confirmationPopup.setViewCloseRequested(true);
            confirmationPopup.setChatView(event.getView());
            confirmationPopup.openPopup();

         }
      }
      else if (event.getType().equals(ViewEvent.ViewEventType.CLOSED))
      {
         if ((!chatRoom.isClosed() && chatRoom.isUserStillInChat()) && !confirmationPopup.isConfirmed())
         {
            event.setVetoed(true);
         }
         else
         {
            chatRoom.handleCloseViewEvent();
         }
      }
   }

   /**
    * @param event
    */
   public void handleLeaveChatRoomToolbarEvent(ActionEvent event)
   {
      chatRoom.handleLeaveChatRoomToolbarEvent();
   }

   /**
    * @param dropEvent
    */
   public void onDocumentDropped(DropEvent dropEvent)
   {
      if (dropEvent.getEventType() == DropEvent.DROPPED)
      {
         try
         {
            DefaultMutableTreeNode valueNode = (DefaultMutableTreeNode) dropEvent.getTargetDragValue();
            RepositoryDocumentUserObject docUserObject = (RepositoryDocumentUserObject) valueNode.getUserObject();
            Document draggedFile = (Document) docUserObject.getResource();
            chatRoom.addChatMessages(new ChatMessage(user.getAccount(), chatRoom.getUsersIndex(user), draggedFile));
         }
         catch (Exception e)
         {
            
         }
      }
   }

   /**
    * shows attachment
    */
   public void showAttachments(ActionEvent event)
   {
      Document document = (Document) event.getComponent().getAttributes().get("attachment");
      DocumentViewUtil.openJCRDocument(document);
   }

   /**
    * @param chatEntry
    * @return
    */
   private String cleanMessage(String chatEntry)
   {
      // Remove any trailing <br /> tags
      String breakTag = "<br />";
      if (chatEntry.endsWith(breakTag))
      {
         chatEntry = chatEntry.substring(0, chatEntry.length() - 6);
      }

      // Remove enclosing <p> tags which are created by the inputRichText
      // component
      String openParaTag = "<p>", closeParaTag = "</p>";
      if (chatEntry.startsWith(openParaTag))
      {
         chatEntry = chatEntry.substring(openParaTag.length());
      }
      if ((chatEntry.lastIndexOf(closeParaTag) > 0)
            && (chatEntry.lastIndexOf(closeParaTag) == chatEntry.length() - closeParaTag.length()))
      {
         chatEntry = chatEntry.substring(0, chatEntry.length() - closeParaTag.length());
      }

      // Check for string with only '\n'
      String checkString = chatEntry.replaceAll("<br />", "");
      checkString = chatEntry.replaceAll("\r", "");
      checkString = checkString.replaceAll("\n", "");
      checkString = checkString.replaceAll("&nbsp;", " ");

      if (checkString.trim().length() != 0)
      {
         return checkString.trim();
      }
      return chatEntry;
   }

   public boolean isOwner()
   {
      return this.user.getAccount().equals(chatRoom.getOwner().getAccount());
   }

   public String getUserSearchKey()
   {
      return userSearchKey;
   }

   public void setUserSearchKey(String userSearchKey)
   {
      this.userSearchKey = userSearchKey;
   }

   public List<User> getSearchResult()
   {
      return searchResult;
   }

   public ChatRoom getChatRoom()
   {
      return chatRoom;
   }

   public IMessenger getMessenger()
   {
      return messenger;
   }

   public void setMessenger(IMessenger messenger)
   {
      this.messenger = messenger;
   }

   public User getUser()
   {
      return user;
   }

   public boolean isCanSendMessage()
   {
      if ((chatRoom.isUserStillInChat() && !chatRoom.isClosed() && chatRoom.getConferenceSize() > 1))
      {
         return true;
      }
      else
      {
         return false;
      }
   }

   /**
    * @return
    */
   public List<UserWrapper> getAfterLogoutList()
   {
      if (null == afterLogoutList)
      {
         afterLogoutList = new LinkedList<UserWrapper>(chatRoom.getAutoCompleteSelector().getSelectedValues());
      }

      return afterLogoutList;
   }

   /**
    * @return
    */
   public List<ChatMessage> getAfterLogoutChatMessages()
   {
      if (afterLogoutChatMessages == null)
      {
         afterLogoutChatMessages = new LinkedList<ChatMessage>(chatRoom.getChatMessages());
      }

      return afterLogoutChatMessages;
   }

   /**
    * @return
    */
   public ChatCloseConfirmationPopup getConfirmationPopup()
   {
      return confirmationPopup;
   }

   /**
    * @param confirmationPopup
    */
   public void setConfirmationPopup(ChatCloseConfirmationPopup confirmationPopup)
   {
      this.confirmationPopup = confirmationPopup;
   }

}
