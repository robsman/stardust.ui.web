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
package org.eclipse.stardust.ui.web.common.dialogs;

import org.eclipse.stardust.ui.web.common.PopupUIComponentBean;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.util.MessagePropertiesBean;

/**
 * @author Subodh.Godbole
 *
 */
public class ConfirmationDialog extends PopupUIComponentBean
{
   private static final long serialVersionUID = 1L;
   private static MessagePropertiesBean props = MessagePropertiesBean.getInstance();
   private static final Logger trace = LogManager.getLogger(ConfirmationDialog.class);

   public static enum DialogContentType
   {
      ERROR,
      WARNING,
      INFO,
      NONE
   }

   public static enum DialogActionType
   {
      OK_CANCEL,
      OK_CLOSE,
      APPLY_CANCEL,
      APPLY_CLOSE,
      YES_NO,
      SUBMIT_CANCEL,
      SUBMIT_CLOSE,
      CONTINUE_CANCEL,
      CONTINUE_CLOSE
   }
   
   public static enum DialogType
   {
      ACCEPT_ONLY,
      CANCEL_ONLY,
      NORMAL
   }

   public static enum DialogStyle
   {
      COMPACT,
      NORMAL
   }

   private String title;
   private String acceptLabel;
   private String cancelLabel;

   private DialogContentType contentType;
   private DialogType dialogType;
   private DialogStyle dialogStyle;
   private ConfirmationDialogHandler handler;

   private String includePath;
   private String message;

   private String styleClass;

   /**
    * 
    * @param contentType
    * @param actionType
    * @param dialogType
    * @param handler
    */
   public ConfirmationDialog(DialogContentType contentType, DialogActionType actionType, DialogType dialogType,
         DialogStyle dialogStyle, ConfirmationDialogHandler handler)
   {
      super("");
      setContentType(contentType);
      setActionType(actionType);
      setDialogType(dialogType);
      setDialogStyle(dialogStyle);
      setHandler(handler);
   }
   
   /**
    * @param contentType
    * @param actionType
    * @param dialogType
    * @param handler
    */
   public ConfirmationDialog(DialogContentType contentType, DialogActionType actionType, DialogType dialogType,
         ConfirmationDialogHandler handler)
   {
      this(contentType, actionType, dialogType, null, handler);
   }

   /**
    * @param contentType
    * @param actionType
    * @param handler
    */
   public ConfirmationDialog(DialogContentType contentType, DialogActionType actionType,
         ConfirmationDialogHandler handler)
   {
      this(contentType, actionType, null, null, handler);
  }

   @Override
   public void initialize()
   {
   }

   /**
    * 
    */
   public void acceptAction()
   {
      try
      {
         boolean success = true;
         if (null != handler)
         {
            success = handler.accept();
         }

         if (success)
         {
            closePopup();
         }
      }
      catch (Exception e)
      {
         trace.error(e);
      }
   }
   
   /**
    * 
    */
   public void cancelAction()
   {
      try
      {
         boolean success = true;
         if (null != handler)
         {
            success = handler.cancel();
         }

         if (success)
         {
            closePopup();
         }
      }
      catch (Exception e)
      {
         trace.error(e);
      }
   }

   /**
    * @param contentType
    */
   public void setContentType(DialogContentType contentType)
   {
      this.contentType = contentType;
      if (null == contentType)
      {
         this.contentType = DialogContentType.NONE;
      }
      
      switch (contentType)
      {
      case ERROR:
         title = props.getString("common.error");
         break;
      case WARNING:
         title = props.getString("common.warning");
         break;
      case INFO:
         title = props.getString("common.info");
         break;
      case NONE:
         title = "";
         break;
      }
   }

   /**
    * 
    */
   public void setActionType(DialogActionType actionType)
   {
      if (null == actionType)
      {
         actionType = DialogActionType.OK_CANCEL;
      }

      switch (actionType)
      {
      case OK_CANCEL:
         acceptLabel = props.getString("common.ok");
         cancelLabel = props.getString("common.cancel");
         break;
      case OK_CLOSE:
         acceptLabel = props.getString("common.ok");
         cancelLabel = props.getString("common.close");
         break;
      case APPLY_CANCEL:
         acceptLabel = props.getString("common.apply");
         cancelLabel = props.getString("common.cancel");
         break;
      case APPLY_CLOSE:
         acceptLabel = props.getString("common.apply");
         cancelLabel = props.getString("common.close");
         break;
      case YES_NO:
         acceptLabel = props.getString("common.yes");
         cancelLabel = props.getString("common.no");
         break;
      case SUBMIT_CANCEL:
         acceptLabel = props.getString("common.submit");
         cancelLabel = props.getString("common.cancel");
         break;
      case SUBMIT_CLOSE:
         acceptLabel = props.getString("common.submit");
         cancelLabel = props.getString("common.close");
         break;
      case CONTINUE_CANCEL:
         acceptLabel = props.getString("common.continue");
         cancelLabel = props.getString("common.cancel");
         break;
      case CONTINUE_CLOSE:
         acceptLabel = props.getString("common.continue");
         cancelLabel = props.getString("common.close");
         break;
      }
   }

   /**
    * 
    * @param dialogType
    */
   public void setDialogType(DialogType dialogType)
   {
      this.dialogType = dialogType;
      if (null == dialogType)
      {
         this.dialogType = DialogType.NORMAL;
      }
   }
   
   /**
    * 
    * @param dialogStyle
    */
   public void setDialogStyle(DialogStyle dialogStyle)
   {
      this.dialogStyle = dialogStyle;
      if (null == dialogStyle)
      {
         this.dialogStyle = DialogStyle.NORMAL;
      }
      switch (this.dialogStyle)
      {
      case NORMAL:
         this.styleClass = "pnlPopUpMsgBrdrDialog";
         break;
      case COMPACT:
         this.styleClass = "pnlPopUpMsgBrdrCnfDialog";
         break;
      }
   }
   
   
   public void setHandler(ConfirmationDialogHandler handler)
   {
      this.handler = handler;
   }

   public String getAcceptLabel()
   {
      return acceptLabel;
   }

   public void setAcceptLabel(String acceptLabel)
   {
      this.acceptLabel = acceptLabel;
   }

   public String getCancelLabel()
   {
      return cancelLabel;
   }

   public void setCancelLabel(String cancelLabel)
   {
      this.cancelLabel = cancelLabel;
   }

   public String getIncludePath()
   {
      return includePath;
   }

   public void setIncludePath(String includePath)
   {
      this.includePath = includePath;
   }

   public DialogContentType getContentType()
   {
      return contentType;
   }

   public String getContentTypeAsString()
   {
      return contentType.name();
   }

   public String getTitle()
   {
      return title;
   }

   public void setTitle(String title)
   {
      this.title = title;
   }

   public String getMessage()
   {
      return message;
   }

   public void setMessage(String message)
   {
      this.message = message;
   }

   public String getStyleClass()
   {
      return styleClass;
   }

   public void setStyleClass(String styleClass)
   {
      this.styleClass = styleClass;
   }
   
   public DialogType getDialogType()
   {
      return dialogType;
   }

   public String getDialogTypeAsString()
   {
      return dialogType.name();
   }
   
   
}
