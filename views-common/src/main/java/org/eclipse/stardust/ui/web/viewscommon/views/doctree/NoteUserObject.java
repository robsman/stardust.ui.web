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

import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.dto.Note;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.ui.web.viewscommon.common.NoteTip;
import org.eclipse.stardust.ui.web.viewscommon.common.ToolTip;
import org.eclipse.stardust.ui.web.viewscommon.utils.MyPicturePreferenceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils;



/**
 * @author Yogesh.Manware
 * @version $Revision: $
 */
public class NoteUserObject extends RepositoryResourceUserObject
{

   private static final long serialVersionUID = 1L;
   private ToolTip noteTip;
   private String noteIndex;
   private ProcessInstance processInstance;

   /**
    * constructor - set default properties
    * 
    * @param defaultMutableTreeNode
    * @param note
    */
   public NoteUserObject(DefaultMutableTreeNode defaultMutableTreeNode, Note note, String noteIndex,
         ProcessInstance processInstance)
   {
      super(defaultMutableTreeNode);
      setLeafIcon(MyPicturePreferenceUtils.getUsersImageURI(note.getUser()));
      this.noteTip = new NoteTip(note, processInstance.getOID());
      this.setLeaf(true);
      defaultMutableTreeNode.setAllowsChildren(false);
      this.processInstance = processInstance;
      this.noteIndex = noteIndex;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.stardust.ui.web.viewscommon.views.doctree.RepositoryResourceUserObject#createNote()
    */
   public void createNote()
   {}

   public ToolTip getToolTip()
   {
      return noteTip;
   }

   public boolean isSupportsToolTip()
   {
      return true;
   }

   @Override
   public RepositoryResourceUserObject createSubfolder()
   {
      return null;
   }

   @Override
   public RepositoryResourceUserObject createTextDocument(String fileType)
   {
      // This method should never get invoked
      return null;
   }

   @Override
   public void deleteResource()
   {
   // This method should never get invoked

   }

   @Override
   public String getLabel()
   {
      return noteTip.getTitle();
   }

   @Override
   public ResourceType getType()
   {
      return ResourceType.NOTE;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.stardust.ui.web.viewscommon.views.doctree.RepositoryResourceUserObject#openDocument()
    */
   public void openDocument()
   {
      if (null != processInstance)
      {
         Map<String, Object> params = CollectionUtils.newTreeMap();
         params.put("noteNr", noteIndex);
         ProcessInstanceUtils.openNotes(processInstance, params);
      }
   }

   @Override
   public void refresh()
   {

   }

   @Override
   public void rename(String newName)
   {

   }

   @Override
   public void upload()
   {
   // This method should never get invoked

   }

   @Override
   public void uploadFolder()
   {
   // This method should never get invoked

   }

   @Override
   public void versionHistory()
   {
   // This method should never get invoked
   }

   @Override
   public boolean isDownloadable()
   {

      return false;
   }

   @Override
   public boolean isDraggable()
   {
      return false;
   }

   @Override
   public boolean isReadable()
   {
      return true;
   }

   @Override
   public boolean isCanCreateFile()
   {
      return false;
   }

   @Override
   public boolean isRefreshable()
   {
      return false;
   }

   @Override
   public boolean isCanCreateNote()
   {
      return false;
   }

   @Override
   public boolean isSendFileAllowed()
   {
      return false;
   }

   @Override
   public void sendFile()
   {}

   @Override
   public void drop(DefaultMutableTreeNode valueNode)
   {}

   @Override
   public boolean isLeafNode()
   {
      return true;
   }

   @Override
   public void download()
   {   }
}
