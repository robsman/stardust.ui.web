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
/**
 * @author Johnson.Quadras
 */
package org.eclipse.stardust.ui.web.rest.service.dto;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.reflect.TypeToken;

public class WorklistFilterDTO {

   public ProcessActivityDTO processDefinition;

   public ProcessActivityDTO overview;

   public RangeDTO oid;

   public RangeDTO started;

   public RangeDTO lastModified;

   public EqualsDTO status;

   public EqualsDTO priority;

   public RangesDTO criticality;

   public Map<String, DescriptorFilterDTO> descriptorFilterMap;


   public static class ProcessActivityDTO {
      public List<String> processes;
      public List<String> activities;
   }

   public static class RangeDTO {
      public Long from;
      public Long to;
   }

   public static class RangesDTO {
      public List<RangeDTO> rangeLike;
   }
   public static class EqualsDTO {
      public List<String> like;
   }
   
   public static class BooleanDTO {
      public boolean equals;
   }

   public static class PriorityLikeDTO {
      public List<PrioirtyDTO> priorityLike;
   }

   public static class CriticalityLikeDTO {
      public List<CriticalityDTO> criticalityLike;
   }

   public static class TextSearchDTO {
      public String textSearch;
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
      return customTokens;
   }

}
