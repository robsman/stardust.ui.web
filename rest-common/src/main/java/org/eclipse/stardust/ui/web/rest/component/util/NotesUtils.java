/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.rest.component.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.dto.ContextKind;
import org.eclipse.stardust.engine.api.dto.Note;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceAttributes;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.ui.web.rest.dto.NoteDTO;
import org.eclipse.stardust.ui.web.rest.dto.QueryResultDTO;
import org.eclipse.stardust.ui.web.viewscommon.common.NoteTip;
import org.eclipse.stardust.ui.web.viewscommon.common.event.IppEventController;
import org.eclipse.stardust.ui.web.viewscommon.common.event.NoteEvent;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.ActivityInstanceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ServiceFactoryUtils;
import org.springframework.stereotype.Component;

/**
 * @author Abhay.Thappan
 *
 */
@Component
public class NotesUtils
{

   public QueryResultDTO getNotes(long processInstanceOid)
   {
      ProcessInstance processInstance = ProcessInstanceUtils.getProcessInstance(processInstanceOid, true);

      if (processInstance == null)
      {
         return null;
      }

      ProcessInstance scopeProcessInstance = null;
      if (processInstance.getOID() != processInstance.getScopeProcessInstanceOID())
      {
         scopeProcessInstance = ProcessInstanceUtils.getProcessInstance(processInstance.getScopeProcessInstanceOID(),
               true);
      }
      else
      {
         scopeProcessInstance = ProcessInstanceUtils.getProcessInstance(processInstance.getScopeProcessInstanceOID());
      }

      ProcessInstanceAttributes attributes = fetchAttributes(scopeProcessInstance);
      List<Note> noteList = attributes.getNotes();
      List<NoteDTO> noteDTOList = new ArrayList<NoteDTO>();
      for (Note note : noteList)
      {
         NoteDTO noteDTO = new NoteDTO(note);
         noteDTO.scopeType = getScopeType(note, processInstanceOid);
         noteDTO.noteNumber = noteList.indexOf(note) + 1;
         noteDTOList.add(noteDTO);
      }
      
      Collections.sort(noteDTOList, new Comparator<NoteDTO>()
            {
               public int compare(NoteDTO arg0, NoteDTO arg1)
               {
                  return new Long(arg1.created).compareTo(new Long(arg0.created));
               }
            });

      QueryResultDTO queryResultDTO = new QueryResultDTO();
      queryResultDTO.list = noteDTOList;
      queryResultDTO.totalCount = noteDTOList.size();
      return queryResultDTO;
   }

   public void saveNote(String noteText, long processInstanceOid) throws Exception
   {
      if (!StringUtils.isEmpty(noteText) && (noteText.trim().length() > 0))
      {
         ProcessInstance processInstance = ProcessInstanceUtils.getProcessInstance(processInstanceOid, true);

         ProcessInstance scopeProcessInstance = null;
         if (processInstance.getOID() != processInstance.getScopeProcessInstanceOID())
         {
            scopeProcessInstance = ProcessInstanceUtils.getProcessInstance(
                  processInstance.getScopeProcessInstanceOID(), true);
         }
         else
         {
            scopeProcessInstance = ProcessInstanceUtils
                  .getProcessInstance(processInstance.getScopeProcessInstanceOID());
         }

         ProcessInstanceAttributes attributes = fetchAttributes(scopeProcessInstance);
         Note lastValidNote = attributes.addNote(noteText, ContextKind.ProcessInstance, processInstance.getOID());

         ServiceFactoryUtils.getWorkflowService().setProcessInstanceAttributes(attributes);

         IppEventController.getInstance().notifyEvent(
               new NoteEvent(scopeProcessInstance.getOID(), lastValidNote, attributes.getNotes()));
      }
   }

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

   private ProcessInstanceAttributes fetchAttributes(ProcessInstance pi)
   {
      ProcessInstanceAttributes pia = pi.getAttributes();
      return pia;
   }

}
