/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Anoop.Nair (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.rest.service;

import java.util.List;

import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;

import org.eclipse.stardust.ui.web.rest.service.dto.DocumentDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.ProcessInstanceDTO;

/**
 * @author Anoop.Nair
 * @version $Revision: $
 */
@Component
public class ProcessInstanceService
{

   public ProcessInstanceDTO startProcess(JsonObject json)
   {
      // TODO Auto-generated method stub
      return null;
   }

   public List<ProcessInstanceDTO> getPendingProcesses(JsonObject json)
   {
      // TODO Auto-generated method stub
      return null;
   }

   public ProcessInstanceDTO removeProcessInstanceDocument(long processInstanceOid,
         String dataPathId, String documentId)
   {
      // TODO Auto-generated method stub
      return null;
   }

   public DocumentDTO splitDocument(long processInstanceOid, String documentId,
         JsonObject json)
   {
      // TODO Auto-generated method stub
      return null;
   }

   public ProcessInstanceDTO addProcessInstanceDocument(long parseLong,
         String dataPathId, JsonObject json)
   {
      // TODO Auto-generated method stub
      return null;
   }

}
