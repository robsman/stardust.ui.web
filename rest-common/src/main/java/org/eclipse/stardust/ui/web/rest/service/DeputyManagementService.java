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
import org.eclipse.stardust.ui.web.rest.service.dto.SelectItemDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.response.ParticipantSearchResponseDTO;
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

   /**
    * 
    * @param userOID
    * @return
    */
   public QueryResultDTO loadDeputiesForUser(long userOID)
   {
      List<DeputyMemberDetailDTO> deputies = deputyManagementUtils.loadDeputiesForUser(userOID);

      QueryResultDTO resultDTO = new QueryResultDTO();
      resultDTO.list = deputies;
      resultDTO.totalCount = deputies.size();
      return resultDTO;
   }

   /**
    * 
    * @param userOID
    * @param searchValue
    * @param searchMode
    * @return
    */
   public QueryResultDTO getDeputyUsersData(long userOID, String searchValue, String searchMode)
   {
      List<ParticipantSearchResponseDTO> userWrappers = deputyManagementUtils.getDeputyUsersData(userOID, searchValue,
            searchMode);

      QueryResultDTO resultDTO = new QueryResultDTO();
      resultDTO.list = userWrappers;
      resultDTO.totalCount = userWrappers.size();
      return resultDTO;
   }
   /**
    * 
    * @param userOID
    * @return
    */
   public QueryResultDTO getAuthorizations(long userOID)
   {
      List<SelectItemDTO> authorozations = deputyManagementUtils.getAuthorizations(userOID);

      QueryResultDTO resultDTO = new QueryResultDTO();
      resultDTO.list = authorozations;
      resultDTO.totalCount = authorozations.size();
      return resultDTO;
   }
   /**
    * 
    * @param userOID
    * @param deputyOID
    * @param validFrom
    * @param validTo
    * @param modelParticipantIds
    * @param mode
    */
   public void addDeputy(long userOID, long deputyOID, long validFrom, long validTo, List<String> modelParticipantIds, String mode)
   {
      deputyManagementUtils.addDeputy(userOID, deputyOID, validFrom, validTo,
            modelParticipantIds,mode);     
   }
   /**
    * 
    * @param userOID
    * @param deputyOID
    */
   public void removeUserDeputy(long userOID, long deputyOID){
      deputyManagementUtils.removeUserDeputy(userOID, deputyOID);    
   }
}
