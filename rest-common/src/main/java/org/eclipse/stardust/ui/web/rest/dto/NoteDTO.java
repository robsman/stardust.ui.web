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
/**
 * @author Abhay.Thappan
 */
package org.eclipse.stardust.ui.web.rest.dto;

import org.eclipse.stardust.engine.api.dto.Note;
import org.eclipse.stardust.ui.web.rest.dto.core.DTOClass;
import org.eclipse.stardust.ui.web.viewscommon.utils.MyPicturePreferenceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.UserUtils;

@DTOClass
public class NoteDTO extends AbstractDTO
{

   public String note;

   public String creatorName;

   public long created;

   public int noteNumber;

   public String scopeType;

   public long userOID;
   
   public String avatarImageURI;

   public NoteDTO(Note note)
   {
      creatorName = UserUtils.getUserDisplayLabel(note.getUser()); // TODO Auto-generated
                                                                  // constructor stub
      created = note.getTimestamp().getTime();
      this.note = note.getText();
      userOID = note.getUser().getOID();
      avatarImageURI = MyPicturePreferenceUtils.getUsersImageURI(note.getUser());
   }

}
