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
package org.eclipse.stardust.ui.web.viewscommon.views.doctree;

import org.eclipse.stardust.ui.web.common.PopupUIComponentBean;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.ICallbackHandler;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.ICallbackHandler.EventType;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.views.document.ICustomDocumentSaveHandler;

/**
 * @author Yogesh.Manware
 * @version $Revision: $
 */
public class FileSaveDialog extends PopupUIComponentBean
{
   private static final long serialVersionUID = 1L;
   private static final String BEAN_NAME = "fileSaveDialog";
   public static final String COMMENTS = "comments";

   private FileSaveCallbackHandler callbackHandler;
   private String comments;
   private String headerMessage;
   private String message;

   // To show/hide the comment section on the dialog (page section link
   // action)
   private boolean showComment;
   private boolean customDialog;
   private String customDialogSource;
   private ICustomDocumentSaveHandler.CustomDialogPosition customDialogPosition;

   /**
    * default constructor
    */
   public FileSaveDialog()
   {
      super();
   }

   /**
    * @return fileUploadAdminDialog object
    */
   public static FileSaveDialog getInstance()
   {
      return (FileSaveDialog) FacesUtils.getBeanFromContext(BEAN_NAME);
   }

   @Override
   public void initialize()
   {
      // FacesUtils.refreshPage();
      setTitle(MessagesViewsCommonBean.getInstance().getString("views.documentView.saveDocumentDialog.saveDocument"));
      comments = "";
      headerMessage = "";
      callbackHandler = null;
      showComment = false;

      customDialog = false;
      customDialogSource = null;
      customDialogPosition = null;
   }

   public void continueAction()
   {
      fireCallback(EventType.APPLY);
   }

   public void toggleComment()
   {
      showComment = !showComment;
   }

   /**
    * @param eventType
    */
   private void fireCallback(EventType eventType)
   {
      if (callbackHandler != null)
      {
         callbackHandler.setComments(getComments());
         callbackHandler.handleEvent(eventType);
      }
      initialize();
      closePopup();
   }

   public String getComments()
   {
      return comments;
   }

   public void setComments(String comments)
   {
      this.comments = comments;
   }

   public boolean isShowComment()
   {
      return showComment;
   }

   public boolean isCustomDialog()
   {
      return customDialog;
   }

   public void setCustomDialog(boolean customDialog)
   {
      this.customDialog = customDialog;
   }

   public String getCustomDialogSource()
   {
      return customDialogSource;
   }

   public void setCustomDialogSource(String customDialogSource)
   {
      this.customDialogSource = customDialogSource;
   }

   public ICustomDocumentSaveHandler.CustomDialogPosition getCustomDialogPosition()
   {
      return customDialogPosition;
   }

   public void setCustomDialogPosition(ICustomDocumentSaveHandler.CustomDialogPosition customDialogPosition)
   {
      this.customDialogPosition = customDialogPosition;
   }

   public void setCallbackHandler(FileSaveCallbackHandler callbackHandler)
   {
      this.callbackHandler = callbackHandler;
   }

   public String getHeaderMessage()
   {
      return headerMessage;
   }

   public void setHeaderMessage(String headerMessage)
   {
      this.headerMessage = headerMessage;
   }

   public String getMessage()
   {
      return message;
   }

   public void setMessage(String message)
   {
      this.message = message;
   }

   public static abstract class FileSaveCallbackHandler implements ICallbackHandler
   {
      private String comments;

      public String getComments()
      {
         return comments;
      }

      public void setComments(String comments)
      {
         this.comments = comments;
      }
   }
}