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

import java.util.List;

import javax.annotation.Resource;

import org.eclipse.stardust.ui.web.rest.service.dto.DeputyMemberDetailDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.QueryResultDTO;
import org.eclipse.stardust.ui.web.rest.service.utils.DeputyManagementUtils;
import org.springframework.stereotype.Component;

@Component
public class DeputyManagementService
{
   @Resource
   private DeputyManagementUtils deputyManagementUtils;

   /**
    * 
    * @return
    */
   public QueryResultDTO loadUsers()
   {
      List<DeputyMemberDetailDTO> users = deputyManagementUtils.loadUsers();

      QueryResultDTO resultDTO = new QueryResultDTO();
      resultDTO.list = users;
      resultDTO.totalCount = users.size();
      return resultDTO;
   }

   public QueryResultDTO loadDeputiesForUser(long userOID)
   {
      List<DeputyMemberDetailDTO> deputies = deputyManagementUtils.loadDeputiesForUser(userOID);

      QueryResultDTO resultDTO = new QueryResultDTO();
      resultDTO.list = deputies;
      resultDTO.totalCount = deputies.size();
      return resultDTO;
   }
}
