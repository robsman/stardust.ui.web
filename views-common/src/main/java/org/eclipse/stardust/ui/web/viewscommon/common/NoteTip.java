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
package org.eclipse.stardust.ui.web.viewscommon.common;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang.StringEscapeUtils;
import org.eclipse.stardust.engine.api.dto.ContextKind;
import org.eclipse.stardust.engine.api.dto.Note;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.ui.web.common.util.DateUtils;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.ActivityInstanceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.FormatterUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.MyPicturePreferenceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils;


/**
 * @author Yogesh.Manware
 * @version $Revision: $
 */
public class NoteTip implements ToolTip, Serializable
{
   private static final long serialVersionUID = 1L;
   protected static final String NOTE = "note";
   private String user;
   private String timeStamp;
   private String title;
   private String scopeType;
   private String toolTipContent;
   private Date timeStampAsDate;
   private String userImageURL;

   /**
    * custom constructor - assist displaying tip for the note
    * 
    * @param note
    */
   public NoteTip(Note note, Long Oid)
   {
      super();
      user = FormatterUtils.getUserLabel(note.getUser());
      userImageURL = MyPicturePreferenceUtils.getUsersImageURI(note.getUser());
      if (note.getTimestamp() != null)
      {
         timeStamp = DateUtils.formatDateTime(note.getTimestamp());
         timeStampAsDate = note.getTimestamp();
      }
      String noteTitle = StringEscapeUtils.unescapeHtml(note.getText());
      toolTipContent = noteTitle;
      title = noteTitle == null || noteTitle.length() < 30 ? noteTitle : noteTitle.substring(0, 29) + "...";
      title = title.replaceAll("\\n", "");
      scopeType = getScopeType(note, Oid);
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.viewscommon.common.ToolTip#getToolTipType()
    */
   public String getToolTipType()
   {
      return NOTE;
   }
   
   public String getUser()
   {
      return user;
   }

   public String getTimeStamp()
   {
      return timeStamp;
   }

   public String getTitle()
   {
      return title;
   }

   public String getScopeType()
   {
      return scopeType;
   }

   public String getToolTipContent()
   {
      return toolTipContent;
   }

   public Date getTimeStampAsDate()
   {
      return timeStampAsDate;
   }
   
   public String getUserImageURL()
   {
      return userImageURL;
   }

   /**
    * @param note
    * @param Oid
    * @return Scope Type Process Definitaion or Activity Definition
    */
   private static String getScopeType(Note note, Long Oid)
   {
      String scopeType = "";
      MessagesViewsCommonBean msgBean = MessagesViewsCommonBean.getInstance();
      ContextKind contextkind = note.getContextKind();
      if (Oid != null)
      {
         if (ContextKind.ActivityInstance.equals(contextkind))
         {
            ActivityInstance activityInstance = ActivityInstanceUtils.getActivityInstance(Oid);
            scopeType = msgBean.getParamString("views.noteToolTip.activity",
                  ActivityInstanceUtils.getActivityLabel(activityInstance));
         }
         else
         {
            ProcessInstance processInstance = ProcessInstanceUtils.getProcessInstance(Oid);
            scopeType = msgBean.getParamString("views.noteToolTip.process",
                  ProcessInstanceUtils.getProcessLabel(processInstance));
         }
      }
      return scopeType;
   }
}