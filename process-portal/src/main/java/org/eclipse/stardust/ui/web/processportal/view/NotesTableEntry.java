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

import java.util.Date;

import org.eclipse.stardust.ui.web.common.table.DefaultRowModel;
import org.eclipse.stardust.ui.web.viewscommon.common.NoteTip;


/**
 * @author roland.stamm
 * 
 */
public class NotesTableEntry extends DefaultRowModel
{
   private static final long serialVersionUID = -4320677664685719435L;

   private String note;

   private String notePreview;

   private String creatorName;

   private Date created;

   private int noteNumber;
   
   private NoteTip noteInfo;
   
   private boolean selected;

   public NotesTableEntry()
   {    
   }
   public NotesTableEntry(String note, String notePreview, String creatorName, Date created, int noteNumber, NoteTip noteInfo)
   {
      super();
      this.note = note;
      if (notePreview.length() < note.length())
      {
         notePreview += " ...";
      }
      this.notePreview = notePreview;
      this.creatorName = creatorName;
      this.created = created;
      this.noteNumber = noteNumber;
      this.noteInfo = noteInfo;
   }

   public String getNote()
   {
      return note;
   }

   public void setNote(String note)
   {
      this.note = note;
   }

   public String getNotePreview()
   {
      return notePreview;
   }

   public void setNotePreview(String notePreview)
   {
      this.notePreview = notePreview;
   }

   public String getCreatorName()
   {
      return creatorName;
   }

   public void setCreatorName(String creatorName)
   {
      this.creatorName = creatorName;
   }

   public Date getCreated()
   {
      return created;
   }

   public void setCreated(Date created)
   {
      this.created = created;
   }

   public int getNoteNumber()
   {
      return noteNumber;
   }

   public void setNoteNumber(int noteNumber)
   {
      this.noteNumber = noteNumber;
   }

   public NoteTip getNoteInfo()
   {
      return noteInfo;
   }

   public boolean isSelected()
   {
      return selected;
   }

   public void setSelected(boolean selected)
   {
      this.selected = selected;
      if (selected)
      {
         NotesBean notesBean = NotesBean.getCurrentInstance();
         if (null != notesBean)
         {
            notesBean.setEditText(this.note);
            // set to update the editText on Cancel button event.
            notesBean.setPrevNoteValue(this.note);
            notesBean.setLastValidNote(notesBean.getNotes().get(noteNumber-1));
            notesBean.setNotesTitle(Integer.valueOf(this.noteNumber));
            notesBean.setCurrentUserImageURL(this.noteInfo.getUserImageURL());
         }
      }
   }

}
