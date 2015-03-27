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
 * @author Johnson.Quadras
 */
package org.eclipse.stardust.ui.web.rest.service.dto;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.ui.web.rest.FilterDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.response.ParticipantSearchResponseDTO;

import com.google.gson.reflect.TypeToken;

public class WorklistFilterDTO implements FilterDTO{

   public ProcessActivityDTO processName;

   public ProcessActivityDTO activityName;

   public RangeDTO activityOID;

   public RangeDTO startTime;

   public RangeDTO lastModified;

   public EqualsDTO status;

   public EqualsDTO priority;

   public RangesDTO criticality;
   
   public ParticipantFilterDTO assignedTo;
   
   public ParticipantFilterDTO completedBy;
   
   public RangeDTO processOID;

   public Map<String, DescriptorFilterDTO> descriptorFilterMap;

   public static class ProcessActivityDTO {
      public List<String> processes;
      public List<String> activities;
   }
   
   public static class ParticipantFilterDTO {
      public List<ParticipantSearchResponseDTO> participants;
   }

   public static class DescriptorFilterDTO{
      public String type;
      public Object value;
      public DescriptorFilterDTO(String type, Object value)
      {
         super();
         this.type = type;
         this.value = value;
      }

   }
   
   

   /**
    * Tokens for converting custom object in JSON array to DTO
    *
    * @return
    */
   public static Map<String, Type> getCustomTokens() {
      Map<String, Type> customTokens = new HashMap<String, Type>();
      customTokens.put("rangeLike",
            new TypeToken<List<RangeDTO>>() {
      }.getType());
      
      customTokens.put("participants",
            new TypeToken<List<ParticipantSearchResponseDTO>>() {
      }.getType());
      return customTokens;
   }

}
