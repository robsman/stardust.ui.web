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
package org.eclipse.stardust.ui.web.rest.service;

import javax.annotation.Resource;

import org.eclipse.stardust.ui.web.rest.service.dto.QueryResultDTO;
import org.eclipse.stardust.ui.web.rest.service.utils.NotesUtils;
import org.springframework.stereotype.Component;

@Component
public class NotesService
{
   @Resource
   private NotesUtils notesUtils;

   public QueryResultDTO getNotes(long processInstanceOid)
   {
      return notesUtils.getNotes(processInstanceOid);
   }
}
