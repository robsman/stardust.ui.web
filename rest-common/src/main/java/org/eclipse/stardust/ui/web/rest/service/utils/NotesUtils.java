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
package org.eclipse.stardust.ui.web.rest.service.utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.eclipse.stardust.engine.api.dto.ContextKind;
import org.eclipse.stardust.engine.api.dto.Note;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceAttributes;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.ui.web.rest.Options;
import org.eclipse.stardust.ui.web.rest.service.dto.NoteDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.QueryResultDTO;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.ActivityInstanceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils;
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
      /*
       * if (null == thisView.getViewParams().get("processName")) {
       * thisView.getViewParams().put( "processName",
       * I18nUtils.getProcessName(ProcessDefinitionUtils
       * .getProcessDefinition(processInstance.getModelOID(),
       * processInstance.getProcessID()))); thisView.resolveLabelAndDescription(); }
       */
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

      QueryResultDTO queryResultDTO = new QueryResultDTO();
      queryResultDTO.list = noteDTOList;
      queryResultDTO.totalCount = noteDTOList.size();
      return queryResultDTO;
   }

   public class NotesComparator implements Comparator<Note>
   {
      private Options options;

      /**
       * @param sortCriterion
       */
      public NotesComparator(Options options)
      {
         this.options = options;
      }

      /*
       * (non-Javadoc)
       * 
       * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
       */
      public int compare(Note arg0, Note arg1)
      {
         // note number is generated dynamically: most latest note has greatest note
         // number by default
         if ("created".equals(options.orderBy) || "noteNumber".equals(options.orderBy))
         {
            if (options.asc)
            {
               return arg0.getTimestamp().compareTo(arg1.getTimestamp());
            }
            else
            {
               return arg1.getTimestamp().compareTo(arg0.getTimestamp());
            }
         }
         return 0;
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
