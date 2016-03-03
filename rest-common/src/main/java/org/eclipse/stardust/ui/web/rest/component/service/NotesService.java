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
package org.eclipse.stardust.ui.web.rest.component.service;

import org.eclipse.stardust.ui.web.rest.dto.NotesResultDTO;

/**
 * @author Yogesh.Manware
 * @version $Revision: $
 */
public interface NotesService
{
   /**
    * @param processInstanceOid
    * @param asc
    * @return
    */
   NotesResultDTO getProcessNotes(long processInstanceOid, boolean asc);

   /**
    * @param processInstanceOid
    * @param noteText
    */
   void saveProcessNotes(long processInstanceOid, String noteText);

   /**
    * @param activityInstanceOid
    * @param asc
    * @return
    */
   NotesResultDTO getActivityNotes(long activityInstanceOid, boolean asc);

   /**
    * @param activityInstanceOid
    * @param noteText
    */
   void saveActivityNotes(long activityInstanceOid, String noteText);

}
