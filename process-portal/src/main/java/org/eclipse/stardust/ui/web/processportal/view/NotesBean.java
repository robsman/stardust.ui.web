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
package org.eclipse.stardust.ui.web.processportal.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.dto.ContextKind;
import org.eclipse.stardust.engine.api.dto.Note;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceAttributes;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.ui.web.common.UIComponentBean;
import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.common.app.View;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference;
import org.eclipse.stardust.ui.web.common.column.DefaultColumnModel;
import org.eclipse.stardust.ui.web.common.column.IColumnModel;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnDataType;
import org.eclipse.stardust.ui.web.common.columnSelector.TableColumnSelectorPopup;
import org.eclipse.stardust.ui.web.common.event.ViewEvent;
import org.eclipse.stardust.ui.web.common.event.ViewEventHandler;
import org.eclipse.stardust.ui.web.common.event.ViewEvent.ViewEventType;
import org.eclipse.stardust.ui.web.common.table.DataTable;
import org.eclipse.stardust.ui.web.common.table.DataTableRowSelector;
import org.eclipse.stardust.ui.web.common.table.DataTableSortModel;
import org.eclipse.stardust.ui.web.common.table.IQuery;
import org.eclipse.stardust.ui.web.common.table.IQueryResult;
import org.eclipse.stardust.ui.web.common.table.ISearchHandler;
import org.eclipse.stardust.ui.web.common.table.ISortHandler;
import org.eclipse.stardust.ui.web.common.table.IUserObjectBuilder;
import org.eclipse.stardust.ui.web.common.table.PaginatorDataTable;
import org.eclipse.stardust.ui.web.common.table.SortCriterion;
import org.eclipse.stardust.ui.web.common.util.DateUtils;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.processportal.common.MessagePropertiesBean;
import org.eclipse.stardust.ui.web.processportal.common.Resources;
import org.eclipse.stardust.ui.web.processportal.common.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.viewscommon.common.NoteTip;
import org.eclipse.stardust.ui.web.viewscommon.common.event.IppEventController;
import org.eclipse.stardust.ui.web.viewscommon.common.event.NoteEvent;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.MyPicturePreferenceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ServiceFactoryUtils;

import com.icesoft.faces.context.effects.Effect;
import com.icesoft.faces.context.effects.Highlight;


public class NotesBean extends UIComponentBean implements IUserObjectBuilder<NotesTableEntry>, ViewEventHandler
{
   /**
    * 
    */
   private static final long serialVersionUID = -8644091834166833520L;
   private static final Logger trace = LogManager
   .getLogger(NotesBean.class);

   private static final int TEXT_PREVIEW_LENGTH = 80;

   private Effect effect;

   private List<Note> notes;

   private String editText;

   private boolean editMode;

   private Note lastValidNote;

   private PaginatorDataTable<NotesTableEntry, Note> notesTable;

   private ProcessInstanceAttributes attributes;

   private ProcessInstance processInstance;

   private ProcessInstance scopeProcessInstance;

   private View thisView;

   private String prevNoteValue;
   
   private String notesTitle;
   
   private String currentUserImageURL;
   
   public NotesBean()
   {
      super("notesPanel");
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
      if (ViewEventType.CREATED == event.getType())
      {
         this.thisView = event.getView();
         initialize();
      }

   }
   
   @Override
   public void initialize()
   {
      effect = new Highlight("#8898C2");
      getEffect().setFired(true);
      editMode = false;
      initColumnModel();

      processInstance = fetchProcessInstance(true);

      if (processInstance == null)
         return;

      scopeProcessInstance = ProcessInstanceUtils.getProcessInstance(processInstance.getScopeProcessInstanceOID());
      String noteNumber = (String) getParamValue("noteNr");
      attributes = fetchAttributes(scopeProcessInstance);
      List<Note> noteList = attributes.getNotes();
      setNotes(noteList);
      if (noteList != null && noteList.size() > 0)
         thisView.setIcon("/plugins/views-common/images/icons/mime-types/notes-filled.png");
       else
          thisView.setIcon("/plugins/views-common/images/icons/notes-blank.png");
      
      // FOR PANAMA
      PortalApplication.getInstance().updateViewIconClass(thisView);
      
      Date noteTimestamp = (Date) getParamValue("noteTimestamp");
      Integer noteIndex = (Integer) getParamValue("noteIndex");
      if (null != noteTimestamp)
      {
         openNote(noteTimestamp);
         setNotesTitle(noteList.size() - noteIndex);

      }
      else if (null != noteNumber)
      {
         openNote(getNotes().get(Integer.valueOf(noteNumber) - 1).getTimestamp());
         setNotesTitle(Integer.valueOf(noteNumber));
      }
      else if (CollectionUtils.isNotEmpty(noteList))
      {
         openNote(noteList.get(noteList.size() - 1).getTimestamp());
         setNotesTitle(noteList.size());
      }

      Object objCreateNote = getParamValue("createNote");
      if (null != objCreateNote)
      {
         if ("true".equals(objCreateNote))
         {
            addNote();
         }
      }
   }

   /**
    * @param param
    * @return param value from external context or from view scope
    */
   private Object getParamValue(String param)
   {
      Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
      Object value;
      if (params.containsKey(param))
      {
         value = params.get(param);
      }
      else
      {
         value = thisView.getViewParams().get(param);
      }

      return value;
   }

   private ProcessInstanceAttributes fetchAttributes(ProcessInstance pi)
   {
      ProcessInstanceAttributes pia = pi.getAttributes();
      return pia;
   }

   private ProcessInstance fetchProcessInstance(boolean forceReload)
   {

      String oid = thisView.getViewParams().get("oid") == null ? null : (String) thisView.getViewParams().get("oid");
      if (oid == null)
         return null;

      long oidl = Long.valueOf(oid);
      ProcessInstance pi = ProcessInstanceUtils.getProcessInstance(oidl, forceReload);
      return pi;
   }

   /**
    * @return
    */
   public static NotesBean getCurrentInstance()
   {
      return (NotesBean)FacesUtils.getBeanFromContext("processportal_NotesBean");
   }

   /**
    * @param event
    */
   public void openNote(ActionEvent event)
   {
      Date noteTimestamp = (Date) event.getComponent().getAttributes().get("noteTimestamp");
      openNote(noteTimestamp);
   }

   public String addNote()
   {
      getEffect().setFired(false);
      editMode = true;
      editText = "";
      return null;
   }

   public String saveNote()
   {
      if (!StringUtils.isEmpty(editText) && (editText.trim().length() > 0))
      {
         try
         {
            attributes = fetchAttributes(scopeProcessInstance);
            lastValidNote = attributes.addNote(editText, ContextKind.ProcessInstance, processInstance.getOID());
            currentUserImageURL = MyPicturePreferenceUtils.getUsersImageURI(lastValidNote.getUser());
            ServiceFactoryUtils.getWorkflowService()
                  .setProcessInstanceAttributes(attributes);
            thisView.getViewParams().remove("noteTimestamp");
            thisView.getViewParams().remove("noteNr");
            initialize();
            editMode = false;
            IppEventController.getInstance().notifyEvent(
                  new NoteEvent(scopeProcessInstance.getOID(), lastValidNote, attributes.getNotes()));
         }
         catch (Exception e)
         {
            ExceptionHandler.handleException(e);
         }
      }
      return null;
   }

   /**
    * @return
    */
   public String cancelNote()
   {
      editMode = false;
      // Checks the current row selected Notes Preview Text, set from NotesTableEntry
      if (StringUtils.isNotEmpty(prevNoteValue))
      {
         editText = prevNoteValue;
      }
      else if (lastValidNote != null)
      {
         editText = lastValidNote.getText();
      }
      else
      {
         editText = "";
      }

      return null;
   }

   /**
    * 
    */
   private void initNotesTable()
   {
      notesTable.refresh(true);
   }

   /**
    * 
    */
   private void initColumnModel()
   {
      List<ColumnPreference> roleCols = new ArrayList<ColumnPreference>();

      ColumnPreference colStarted = new ColumnPreference("Created", "created", ColumnDataType.DATE, this.getMessages()
            .getString("column.creationTime"), true, true);
      colStarted.setNoWrap(true);

      ColumnPreference colProcessName = new ColumnPreference("CreatorName", "creatorName", this.getMessages()
            .getString("column.createdBy"), Resources.VIEW_NOTES_COLUMNS, true, false);

      // colPriority.setColumnAlignment(ColumnAlignment.CENTER);

      ColumnPreference colNotes = new ColumnPreference("Notes", "note", this.getMessages().getString("column.preview"),
            Resources.VIEW_NOTES_COLUMNS, true, false);
      colNotes.setNoWrap(true);

      ColumnPreference colNoteNumber = new ColumnPreference("NoteNumber", "noteNumber", ColumnDataType.NUMBER, this
            .getMessages().getString("column.noteNumber"), true, true);

      roleCols.add(colNoteNumber);
      roleCols.add(colProcessName);
      roleCols.add(colStarted);
      roleCols.add(colNotes);

      IColumnModel notesColumnModel = new DefaultColumnModel(roleCols, null, null, UserPreferencesEntries.M_WORKFLOW,
            UserPreferencesEntries.V_NOTES);

      TableColumnSelectorPopup notesColSelecpopup = new TableColumnSelectorPopup(notesColumnModel);
      notesTable = new PaginatorDataTable<NotesTableEntry, Note>(notesColSelecpopup, new NotesSearchHandler(),
            null, new NotesSortHandler(), this, new DataTableSortModel<NotesTableEntry>("created", false));
      notesTable.setRowSelector(new DataTableRowSelector("selected",false));
      notesTable.initialize();
   }

   public int getNotesCount()
   {
      return notes.size();
   }

   public List<Note> getNotes()
   {
      return notes;
   }

   public void setNotes(List<Note> notes)
   {
      this.notes = notes;
      initNotesTable();
   }

   public String getEditText()
   {
      return editText;
   }

   public void setEditText(String editText)
   {
      this.editText = editText;
   }

   public boolean isEditMode()
   {
      return editMode;
   }

   public Effect getEffect()
   {
      return effect;
   }

   public DataTable<NotesTableEntry> getNotesTable()
   {
      return notesTable;
   }
   
   public void setPrevNoteValue(String prevNoteValue)
   {
      this.prevNoteValue = prevNoteValue;
   }
   
   public String getNotesTitle()
   {
      return notesTitle;
   }

   public void setNotesTitle(Integer noteNumber)
   {
      MessagePropertiesBean messageBean = MessagePropertiesBean.getInstance();
      // Will append the current row Note Id to View title
      this.notesTitle = messageBean.getParamString("views.notesPanel.header.viewNote", noteNumber.toString(),
            DateUtils.formatDateTime(lastValidNote.getTimestamp()));
   }
   
   public String getCurrentUserImageURL()
   {
      return currentUserImageURL;
   }

   public void setCurrentUserImageURL(String currentUserImageURL)
   {
      this.currentUserImageURL = currentUserImageURL;
   }
   
   public String getUserLabel()
   {
      return I18nUtils.getUserLabel(lastValidNote.getUser());
   }

   public Note getLastValidNote()
   {
      return lastValidNote;
   }
   
   public void setLastValidNote(Note lastValidNote)
   {
      this.lastValidNote = lastValidNote;
   }

   /* common table interface */
   public NotesTableEntry createUserObject(Object resultRow)
   {
      Note note = (Note) resultRow;
      NoteTip noteInfo = new NoteTip(note, Long.valueOf((String) thisView
            .getViewParams().get("oid")));
      if (note.getUser() != null)
      {
         try{            
         return new NotesTableEntry(note.getText(), note.getText().substring(0,
               Math.min(note.getText().length(), TEXT_PREVIEW_LENGTH)), I18nUtils.getUserLabel(note.getUser()), note
               .getTimestamp(), notes.indexOf(note) + 1, noteInfo);
         }
         catch (Exception  e) {
            trace.error(e);
            NotesTableEntry notesTableEntry= new NotesTableEntry();
            notesTableEntry.setLoaded(false);          
            notesTableEntry.setCause(e);
            return notesTableEntry;
         }
        
      }
      // debug code, user == null should not happen.
      else
      {
         return new NotesTableEntry(note.getText(), note.getText().substring(0,
               Math.min(note.getText().length(), TEXT_PREVIEW_LENGTH)), "Error: no user found", note.getTimestamp(),
               notes.indexOf(note) + 1, noteInfo);
      }
   }


   /**
    * @param docId
    */
   private void openNote(Date noteTimestamp)
   {
      lastValidNote = getSelctedNote(noteTimestamp);
      editText = new String(lastValidNote.getText());
      currentUserImageURL = MyPicturePreferenceUtils.getUsersImageURI(lastValidNote.getUser());
   }

   private Note getSelctedNote(Date noteTimestamp)
   {
      for (Note note : getNotes())
      {
         if (note.getTimestamp().equals(noteTimestamp))
         {
            return note;
         }
      }
      return null;
   }
   
   /**
    * @author Yogesh.Manware
    * 
    */
   public class NotesSearchHandler implements ISearchHandler<Note>
   {
      private static final long serialVersionUID = 1L;

      /*
       * (non-Javadoc)
       * 
       * @see org.eclipse.stardust.ui.web.common.table.ISearchHandler#buildQuery()
       */
      public IQuery buildQuery()
      {
         return new NotesQuery();
      }

      /*
       * (non-Javadoc)
       * 
       * @see
       * org.eclipse.stardust.ui.web.common.table.ISearchHandler#performSearch(org.eclipse.stardust.ui.web.common.table.IQuery, int, int)
       */
      public IQueryResult<Note> performSearch(IQuery query, int startRow, int pageSize)
      {
         // Sorting
         List<Note> tempNotes = new ArrayList<Note>(notes);
         Collections.sort(tempNotes, new NotesComparator(((NotesQuery) query).getSortCriterion()));

         // Return requested page
         long totalCount = tempNotes.size();
         List<Note> results = new ArrayList<Note>();
         int endRow = startRow + pageSize;
         for (int i = startRow; i < endRow && i < totalCount; i++)
         {
            results.add(tempNotes.get(i));
         }

         return new NotesQueryResult(results, totalCount);
      }
   }

   /**
    * @author Yogesh.Manware
    * 
    */
   public class NotesComparator implements Comparator<Note>
   {
      private SortCriterion sortCriterion;

      /**
       * @param sortCriterion
       */
      public NotesComparator(SortCriterion sortCriterion)
      {
         this.sortCriterion = sortCriterion;
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
         if ("created".equals(sortCriterion.getProperty()) || "noteNumber".equals(sortCriterion.getProperty()))
         {
            if (sortCriterion.isAscending())
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

   /**
    * @author Yogesh.Manware
    * 
    */
   public class NotesQuery implements IQuery
   {
      private static final long serialVersionUID = 1L;
      private SortCriterion sortCriterion;

      public SortCriterion getSortCriterion()
      {
         return sortCriterion;
      }

      public void setSortCriterion(SortCriterion sortCriterion)
      {
         this.sortCriterion = sortCriterion;
      }

      public IQuery getClone()
      {
         NotesQuery clone = new NotesQuery();
         clone.setSortCriterion(this.getSortCriterion());
         return clone;
      }
   }

   /**
    * @author Yogesh.Manware
    * 
    */
   public class NotesQueryResult implements IQueryResult<Note>
   {
      private static final long serialVersionUID = 1L;
      private List<Note> items;
      private Long totalCount;

      public NotesQueryResult(List<Note> items, Long totalCount)
      {
         this.items = items;
         this.totalCount = totalCount;
      }

      public List<Note> getData()
      {
         return items;
      }

      public long getTotalCount() throws UnsupportedOperationException
      {
         return totalCount;
      }
   }

   /**
    * @author Yogesh.Manware
    * 
    */
   public class NotesSortHandler implements ISortHandler
   {
      private static final long serialVersionUID = 1L;

      /*
       * (non-Javadoc)
       * 
       * @see
       * org.eclipse.stardust.ui.web.common.table.ISortHandler#applySorting(org.eclipse.stardust.ui.web.common.table.IQuery, java.util.List)
       */
      public void applySorting(IQuery iQuery, List<SortCriterion> sortCriterias)
      {
         Iterator< ? > iterator = sortCriterias.iterator();
         if (iterator.hasNext())
         {
            SortCriterion sortCriterion = (SortCriterion) iterator.next();
            ((NotesQuery) iQuery).setSortCriterion(sortCriterion);
         }
      }
   }
   
}
