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

import java.io.Serializable;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.common.autocomplete.IAutocompleteMultiSelector.IAutocompleteMultiSelectorListener;
import org.eclipse.stardust.ui.web.common.message.AlertEntry;
import org.eclipse.stardust.ui.web.common.message.AlertHandler;
import org.eclipse.stardust.ui.web.common.message.AlertSystem;
import org.eclipse.stardust.ui.web.common.message.MessageDialog;
import org.eclipse.stardust.ui.web.common.util.SessionRendererHelper;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.common.spi.user.impl.IppUserProvider;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentMgmtUtility;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.RepositoryUtility;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.user.UserAutocompleteMultiSelector;
import org.eclipse.stardust.ui.web.viewscommon.user.UserWrapper;
import org.eclipse.stardust.ui.web.viewscommon.utils.DMSHelper;
import org.eclipse.stardust.ui.web.viewscommon.utils.DMSUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.MimeTypesHelper;
import org.eclipse.stardust.ui.web.viewscommon.utils.MyPicturePreferenceUtils;



/**
 * @author Yogesh.Manware
 * @version $Revision: $
 */
public class ChatRoom implements Serializable
{
   public static final String CHAT_ROOM_SESSION_GROUP_PREFIX = "ChatRoomSessions";
   private static final long serialVersionUID = 1L;
   private static final int MAX_USER_INDEX = 11;
   private static final int CHAT_INFO_MSG_COLOUR_INDEX = -1;
   private String id;
   private List<ChatMessage> chatMessages;
   private User owner;
   private boolean closed = false;
   private MessagesViewsCommonBean propsBean = null;
   private boolean chatSaved = false;
   private UserAutocompleteMultiSelector autoCompleteSelector;
   private Map<AlertEntry, org.eclipse.stardust.ui.web.common.spi.user.User> chatAlerts = new HashMap<AlertEntry, org.eclipse.stardust.ui.web.common.spi.user.User>();
   private ProcessInstance processInstance;
   private Map<Long, String> userNameMap = new HashMap<Long, String>();

   /**
    * @param id
    * @param owner
    */
   public ChatRoom(String id, User owner, ProcessInstance processInstance)
   {
      super();
      this.id = id;
      autoCompleteSelector = new UserAutocompleteMultiSelector(false, false);
      autoCompleteSelector.setAutocompleteMultiSelectorListener(new AutocompleteUserSelectorListener());
      this.owner = owner;
      UserWrapper userWrapper = new UserWrapper(owner, owner, true);
      userWrapper.setRemoveable(false);
      autoCompleteSelector.addSelectedUser(userWrapper);
      propsBean = MessagesViewsCommonBean.getInstance();
      chatMessages = new ArrayList<ChatMessage>();
      this.processInstance = processInstance;
      SessionRendererHelper.addCurrentSession(CHAT_ROOM_SESSION_GROUP_PREFIX + id);
      ChatMessage msg = new ChatMessage("", CHAT_INFO_MSG_COLOUR_INDEX, italicizeString(propsBean.getParamString(
            "views.chatView.chatRoom.opened", geUserName(owner))));
      addChatMessages(msg);
   }

   /**
    * convert the text into html format
    * 
    * @return
    */
   public String getHTMLString()
   {
      StringBuffer htmlString = new StringBuffer();
      int size = chatMessages.size();
      if (size > 0)
      {
         htmlString.append("<table style='width: 100%;'>");
         for (int n = 0; n < size; ++n)
         {
            htmlString.append("<tr style='color: #AAAAFF; font-size: small;'>");
            htmlString.append("<b><i>" + chatMessages.get(n).getUser() + "</i></b> <b>"
                  + chatMessages.get(n).getTimeStamp() + "</b>");
            htmlString.append("</tr>");

            htmlString.append("<tr style='font-size: medium;'>");
            htmlString.append(chatMessages.get(n).getMessage());
            htmlString.append("</tr>");
         }
         htmlString.append("</table>");
      }
      return htmlString.toString();
   }

   /**
    * open view when other user(s) accept the chat invite
    * 
    */
   private void afterInviteAccepted()
   {
      User invitedUser = SessionContext.findSessionContext().getUser();
      ChatMessage chatMessage = new ChatMessage("", CHAT_INFO_MSG_COLOUR_INDEX,
            italicizeString(propsBean.getParamString("views.chatView.joinChat", geUserName(invitedUser))));
      chatMessages.add(chatMessage);
      Map<String, Object> params = CollectionUtils.newTreeMap();
      params.put("chatRoomId", String.valueOf(this.id));
      params.put("processInstanceOID", new Long(processInstance.getOID()).toString());
      PortalApplication.getInstance().openViewById("chatView", id , params, null, true);
      SessionRendererHelper.addCurrentSession(CHAT_ROOM_SESSION_GROUP_PREFIX + id);
   }

   /**
    * @param user
    * @return
    */
   public boolean isOwner(User user)
   {
      if (owner.getAccount().equals(user.getAccount()))
      {
         return true;
      }
      return false;
   }

   public UserAutocompleteMultiSelector getAutoCompleteSelector()
   {
      return autoCompleteSelector;
   }

   public void addChatMessages(ChatMessage msg)
   {
      chatMessages.add(msg);
      refreshChatSessions();
   }

   public String getId()
   {
      return id;
   }

   public User getOwner()
   {
      return owner;
   }

   public void setOwner(User owner)
   {
      this.owner = owner;
   }

   public List<ChatMessage> getChatMessages()
   {
      return this.chatMessages;
   }

   public boolean isClosed()
   {
      return closed;
   }

   public void setClosed(boolean closed)
   {
      this.closed = closed;
   }

   public boolean isChatSaved()
   {
      return chatSaved;
   }

   /**
    * @param chatSaved
    */
   public void setChatSaved(boolean chatSaved)
   {
      this.chatSaved = chatSaved;
   }

   /**
    * @return
    */
   public boolean isCurrentUserChatOwner()
   {

      return owner.getAccount().equals(SessionContext.findSessionContext().getUser().getAccount());
   }

   /**
    * 
    */
   public void removeAllChatAlerts()
   {
      Iterator<AlertEntry> iter = chatAlerts.keySet().iterator();
      while (iter.hasNext())
      {
         AlertEntry entry = iter.next();
         AlertSystem.removeAlert(entry, chatAlerts.get(entry));
         iter.remove();
      }
   }

   /**
    * 
    */
   public void removeChatAlertsForUser(User user)
   {
      Iterator<AlertEntry> iter = chatAlerts.keySet().iterator();
      while (iter.hasNext())
      {
         AlertEntry entry = iter.next();
         org.eclipse.stardust.ui.web.common.spi.user.User usr = chatAlerts.get(entry);
         if (usr.getLoginName().equals(user.getAccount()))
         {
            AlertSystem.removeAlert(entry, chatAlerts.get(entry));
            iter.remove();
         }
      }
   }

   /**
    * 
    */
   public void informExit()
   {
      User invitedUser = SessionContext.findSessionContext().getUser();

      // autoCompleteSelector.removeSelectedUser is not used here as we do now wan't
      // "User removed" to be displayed - which is displayed by the autoComplete listner.
      List<UserWrapper> usrList = autoCompleteSelector.getSelectedValues();
      usrList.remove(new UserWrapper(invitedUser, true));
      autoCompleteSelector.setSelectedValues(usrList);
      ChatMessage chatMessage = new ChatMessage(
            "",
            CHAT_INFO_MSG_COLOUR_INDEX,
            italicizeString(propsBean.getParamString("views.chatView.chatRoom.userLeaveEntry", geUserName(invitedUser))));
      chatMessages.add(chatMessage);
      refreshChatSessions();
   }

   /**
    * @param user
    * @return
    */
   public int getUsersIndex(User user)
   {
      return (autoCompleteSelector.getSelectedValues().indexOf(new UserWrapper(user, true)) % (MAX_USER_INDEX + 1));
   }

   /**
    * 
    */
   public void handleCloseViewEvent()
   {
      if (!closed && isUserStillInChat())
      {
         if (isCurrentUserChatOwner())
         {
            closeChatRoom();
            ChatSystemManager.getInstance().removeChatRoom(getId());
         }
         else
         {
            informExit();
         }
      }
   }

   /**
    * 
    */
   public void handleLeaveChatRoomToolbarEvent()
   {
      handleCloseViewEvent();
   }

   /**
    * 
    */
   public void closeChatRoom()
   {
      chatMessages.add(new ChatMessage("", CHAT_INFO_MSG_COLOUR_INDEX, italicizeString(propsBean
            .getParamString("views.chatView.chatRoom.closeEntry"))));
      refreshChatSessions();
      removeAllChatAlerts();
      emptyChatRoom();
      saveChat();
      setClosed(true);
   }

   /**
    * @return
    */
   public boolean isUserStillInChat()
   {
      User currentUser = SessionContext.findSessionContext().getUser();
      List<UserWrapper> usersInChat = autoCompleteSelector.getSelectedValues();
      if (usersInChat.contains(new UserWrapper(currentUser, true)))
      {
         return true;
      }
      else
      {
         SessionRendererHelper.removeCurrentSession(CHAT_ROOM_SESSION_GROUP_PREFIX + getId());
         return false;
      }
   }

   /**
    * save chat as html document
    */
   public void saveChat()
   {
      // check if chat is already saved
      if (!isChatSaved() && isCurrentUserChatOwner() && getChatMessages().size() > 0)
      {
         DateFormat format = DateFormat.getDateTimeInstance();

         String fileName = propsBean.getString("views.chatView.chatTranscript") + " "
               + DMSUtils.replaceAllSpecialChars(format.format(new Date(System.currentTimeMillis())));

         Document document = DocumentMgmtUtility.createDocument(
               RepositoryUtility.getProcessAttachmentsFolder(processInstance).getPath(), fileName, getHTMLString()
                     .getBytes(), null, MimeTypesHelper.HTML.getType(), null, null, null);

         DMSHelper.addAndSaveProcessAttachment(processInstance, document);

         setChatSaved(true);
      }
   }

   /**
    * @return
    */
   public String getConfirmationPopupTitle()
   {
      if (isCurrentUserChatOwner())
      {
         return propsBean.getString("views.chatView.toolbar.close.confirmTitle");
      }
      else
      {
         return propsBean.getString("views.chatView.toolbar.leave.confirmTitle");
      }
   }

   /**
    * @return
    */
   public String getConfirmationPopupQuestion()
   {
      if (isCurrentUserChatOwner())
      {
         return propsBean.getString("views.chatView.toolbar.close.confirmMsg");
      }
      else
      {
         return propsBean.getString("views.chatView.toolbar.leave.confirmMsg");
      }
   }

   /**
    * @return
    */
   public int getConferenceSize()
   {
      return autoCompleteSelector.getSelectedValues().size();
   }

   /**
    * 
    */
   private void refreshChatSessions()
   {
      SessionRendererHelper.render(CHAT_ROOM_SESSION_GROUP_PREFIX + getId());
   }

   /**
    * 
    */
   private void emptyChatRoom()
   {
      autoCompleteSelector.getSelectedValues().clear();
      refreshChatSessions();
   }

   /**
    * @param user
    * @return
    */
   public String geUserName(User user)
   {
      if (null != user && userNameMap.containsKey(user.getOID()))
      {
         return userNameMap.get(user.getOID());
      }
      else
      {
         String name = I18nUtils.getUserLabel(user);
         userNameMap.put(user.getOID(), name);
         return name;
      }

   }

   private String italicizeString(String str)
   {
      return "<i>" + str + "</i>";
   }

   /**
    * @author Yogesh.Manware
    * 
    */
   public class AutocompleteUserSelectorListener implements IAutocompleteMultiSelectorListener<UserWrapper>
   {
      /*
       * (non-Javadoc)
       * 
       * @see org.eclipse.stardust.ui.web.common.autocomplete.IAutocompleteMultiSelector.IAutocompleteMultiSelectorListener#dataAdded(java.lang.Object)
       */
      public void dataAdded(UserWrapper userWrapper)
      {
         if (isClosed())
         {
            MessageDialog.addInfoMessage(propsBean.getString("views.chatView.chatClosed.cannotAddParticipants"));
            emptyChatRoom();
            return;
         }
         if (!isOwner(userWrapper.getUser()))
         {
            // Add new alert for Chat Invitation
            AlertEntry aEntry = new AlertEntry(MyPicturePreferenceUtils.getLoggedInUsersImageURI(),
                  IppUserProvider.wrapUser(owner), propsBean.getString("views.chatView.newInvite"), new AlertHandler()
                  {
                     public boolean handleAlert(AlertEntry appAlert)
                     {
                        afterInviteAccepted();
                        return true;
                     }
                  });
            AlertSystem.addAlert(aEntry, IppUserProvider.wrapUser(userWrapper.getUser()));
            chatAlerts.put(aEntry, IppUserProvider.wrapUser(userWrapper.getUser()));
            chatMessages.add(new ChatMessage("", CHAT_INFO_MSG_COLOUR_INDEX, italicizeString(propsBean.getParamString(
                  "views.chatView.chatRoom.userAddEntry", geUserName(userWrapper.getUser())))));
         }
      }

      /*
       * (non-Javadoc)
       * 
       * @see org.eclipse.stardust.ui.web.common.autocomplete.IAutocompleteMultiSelector.IAutocompleteMultiSelectorListener#dataRemoved(java.lang.Object)
       */
      public void dataRemoved(UserWrapper userWrapper)
      {
         chatMessages.add(new ChatMessage("", CHAT_INFO_MSG_COLOUR_INDEX, italicizeString(propsBean.getParamString(
               "views.chatView.chatRoom.userRemoveEntry", geUserName(userWrapper.getUser())))));
         removeChatAlertsForUser(userWrapper.getUser());
         refreshChatSessions();
      }
   }
}