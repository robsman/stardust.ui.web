/*******************************************************************************
 * Copyright (c) 2016 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *	SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.rest.component.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.Resource;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.engine.api.dto.ActivityInstanceAttributes;
import org.eclipse.stardust.engine.api.dto.ActivityInstanceAttributesImpl;
import org.eclipse.stardust.engine.api.dto.ContextKind;
import org.eclipse.stardust.engine.api.dto.Note;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceAttributes;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.ui.web.rest.component.util.ActivityInstanceUtils;
import org.eclipse.stardust.ui.web.rest.component.util.ProcessInstanceUtils;
import org.eclipse.stardust.ui.web.rest.component.util.ServiceFactoryUtils;
import org.eclipse.stardust.ui.web.rest.dto.NoteDTO;
import org.eclipse.stardust.ui.web.rest.dto.NotesResultDTO;
import org.eclipse.stardust.ui.web.viewscommon.common.event.IppEventController;
import org.eclipse.stardust.ui.web.viewscommon.common.event.NoteEvent;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

/**
 * @author Yogesh.Manware
 * @version $Revision: $
 */
@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.INTERFACES)
public class NotesServiceImpl implements NotesService
{
   @Resource
   private ServiceFactoryUtils serviceFactoryUtils;

   @Resource
   private ActivityInstanceUtils activityInstanceUtils;

   @Resource(name = "ProcessInstanceUtilsREST")
   private ProcessInstanceUtils processInstanceUtils;

   @Override
   public NotesResultDTO getAllNotes(long processInstanceOid, boolean asc)
   {
      return getNotes(processInstanceOid, asc, false);
   }

   @Override
   public NotesResultDTO getProcessNotes(long processInstanceOid, boolean asc)
   {
      return getNotes(processInstanceOid, asc, true);
   }

   /**
    * @param processInstanceOid
    * @param asc
    * @param onlyProcesLevelNotes
    * @return
    */
   private NotesResultDTO getNotes(long processInstanceOid, boolean asc, boolean onlyProcesLevelNotes)
   {
      ProcessInstance processInstance = processInstanceUtils.getProcessInstance(processInstanceOid);

      if (processInstance == null)
      {
         throw new ObjectNotFoundException(BpmRuntimeError.ATDB_NO_MATCHING_PROCESS_INSTANCE.raise());
      }

      ProcessInstance scopeProcessInstance = processInstance;
      if (processInstance.getOID() != processInstance.getScopeProcessInstanceOID())
      {
         List<Long> oids = new ArrayList<Long>();
         oids.add(processInstance.getScopeProcessInstanceOID());
         scopeProcessInstance = processInstanceUtils.getProcessInstances(oids, true, false).get(0);
      }

      ProcessInstanceAttributes attributes = scopeProcessInstance.getAttributes();

      List<Note> noteList = attributes.getNotes();
      List<NoteDTO> noteDTOList = new ArrayList<NoteDTO>();

      MessagesViewsCommonBean msgBean = MessagesViewsCommonBean.getInstance();

      for (Note note : noteList)
      {
         if (ContextKind.ProcessInstance.equals(note.getContextKind()))
         {
            // Process level notes
            NoteDTO noteDTO = new NoteDTO(note);
            noteDTO.noteNumber = noteList.indexOf(note) + 1;
            noteDTO.scopeType = processInstanceUtils.getProcessLabel(processInstance);

            noteDTOList.add(noteDTO);
         }
         else if (!onlyProcesLevelNotes)
         {
            // activity level notes
            NoteDTO noteDTO = new NoteDTO(note);
            noteDTO.noteNumber = noteList.indexOf(note) + 1;

            long activityInstanceOid = note.getContextOid();
            ActivityInstance activityInstance = activityInstanceUtils.getActivityInstance(activityInstanceOid);
            if (activityInstance == null)
            {
               throw new ObjectNotFoundException(BpmRuntimeError.ATDB_NO_MATCHING_ACTIVITY_INSTANCE.raise());
            }
            noteDTO.scopeType = msgBean.getParamString("views.noteToolTip.activity",
                  activityInstanceUtils.getActivityLabel(activityInstance));

            noteDTOList.add(noteDTO);
         }
      }

      if (asc)
      {
         Collections.sort(noteDTOList, new Comparator<NoteDTO>()
         {
            public int compare(NoteDTO arg0, NoteDTO arg1)
            {
               return new Long(arg0.created).compareTo(new Long(arg1.created));
            }
         });
      }
      else
      {
         Collections.sort(noteDTOList, new Comparator<NoteDTO>()
         {
            public int compare(NoteDTO arg0, NoteDTO arg1)
            {
               return new Long(arg1.created).compareTo(new Long(arg0.created));
            }
         });
      }

      NotesResultDTO notesResultDTO = new NotesResultDTO();
      notesResultDTO.list = noteDTOList;
      notesResultDTO.totalCount = noteDTOList.size();
      notesResultDTO.label = processInstanceUtils.getProcessLabel(processInstance);
      return notesResultDTO;

   }

   @Override
   public void saveProcessNotes(long processInstanceOid, String noteText)
   {
      if (!StringUtils.isEmpty(noteText) && (noteText.trim().length() > 0))
      {
         ProcessInstance processInstance = processInstanceUtils.getProcessInstance(processInstanceOid);

         ProcessInstance scopeProcessInstance = null;
         if (processInstance.getOID() != processInstance.getScopeProcessInstanceOID())
         {
            scopeProcessInstance = processInstanceUtils
                  .getProcessInstance(processInstance.getScopeProcessInstanceOID());
         }
         else
         {
            scopeProcessInstance = processInstanceUtils
                  .getProcessInstance(processInstance.getScopeProcessInstanceOID());
         }

         ProcessInstanceAttributes attributes = scopeProcessInstance.getAttributes();
         Note lastValidNote = attributes.addNote(noteText, ContextKind.ProcessInstance, processInstance.getOID());

         serviceFactoryUtils.getWorkflowService().setProcessInstanceAttributes(attributes);

         scopeProcessInstance = processInstanceUtils.getProcessInstance(scopeProcessInstance.getOID());

         IppEventController.getInstance().notifyEvent(
               new NoteEvent(scopeProcessInstance.getOID(), lastValidNote, scopeProcessInstance.getAttributes()
                     .getNotes()));
      }
   }

   /**
    *
    */
   @Override
   public NotesResultDTO getActivityNotes(long activityInstanceOid, boolean asc)
   {
      ActivityInstance activityInstance = activityInstanceUtils.getActivityInstance(activityInstanceOid);
      if (activityInstance == null)
      {
         throw new ObjectNotFoundException(BpmRuntimeError.ATDB_NO_MATCHING_ACTIVITY_INSTANCE.raise());
      }

      ActivityInstanceAttributes attributes = activityInstance.getAttributes();
      List<Note> noteList = new ArrayList<Note>();
      if (attributes != null)
      {
         noteList = attributes.getNotes();
      }

      List<NoteDTO> noteDTOList = new ArrayList<NoteDTO>();
      for (Note note : noteList)
      {
         NoteDTO noteDTO = new NoteDTO(note);
         noteDTO.noteNumber = noteList.indexOf(note) + 1;
         noteDTOList.add(noteDTO);
      }

      if (asc)
      {
         Collections.sort(noteDTOList, new Comparator<NoteDTO>()
         {
            public int compare(NoteDTO arg0, NoteDTO arg1)
            {
               return new Long(arg0.created).compareTo(new Long(arg1.created));
            }
         });
      }
      else
      {
         Collections.sort(noteDTOList, new Comparator<NoteDTO>()
         {
            public int compare(NoteDTO arg0, NoteDTO arg1)
            {
               return new Long(arg1.created).compareTo(new Long(arg0.created));
            }
         });
      }

      NotesResultDTO notesResultDTO = new NotesResultDTO();
      notesResultDTO.list = noteDTOList;
      notesResultDTO.totalCount = noteDTOList.size();
      notesResultDTO.label = activityInstanceUtils.getActivityLabel(activityInstance);
      return notesResultDTO;
   }

   /**
    *
    */
   @Override
   public void saveActivityNotes(long activityInstanceOid, String noteText)
   {
      if (!StringUtils.isEmpty(noteText) && (noteText.trim().length() > 0))
      {
         ActivityInstance activityInstance = activityInstanceUtils.getActivityInstance(activityInstanceOid);
         ActivityInstanceAttributes attributes = activityInstance.getAttributes();
         if (attributes == null)
         {
            attributes = new ActivityInstanceAttributesImpl(activityInstance.getOID());
         }
         attributes.addNote(noteText);
         serviceFactoryUtils.getWorkflowService().setActivityInstanceAttributes(attributes);
      }
   }
}