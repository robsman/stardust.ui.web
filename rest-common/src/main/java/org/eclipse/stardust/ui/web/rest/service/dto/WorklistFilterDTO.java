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

   public PriorityLikeDTO priority;

   public CriticalityLikeDTO criticality;

   public static class ProcessActivityDTO {
      public List<String> processes;
      public List<String> activities;
   }

   public static class RangeDTO {
      public Long from;
      public Long to;
   }

   public static class EqualsDTO {
      public List<String> like;
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

   /**
    * Tokens for converting custom object in JSON array to DTO
    * 
    * @return
    */
   public static Map<String, Type> getCustomTokens() {
      Map<String, Type> customTokens = new HashMap<String, Type>();
      customTokens.put("priorityLike", new TypeToken<List<PrioirtyDTO>>() {
      }.getType());
      customTokens.put("criticalityLike",
            new TypeToken<List<CriticalityDTO>>() {
      }.getType());
      return customTokens;
   }

}
