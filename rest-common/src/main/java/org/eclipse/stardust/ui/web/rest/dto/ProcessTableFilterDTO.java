package org.eclipse.stardust.ui.web.rest.dto;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.ui.web.rest.dto.WorklistFilterDTO.ProcessActivityDTO;
import org.eclipse.stardust.ui.web.rest.dto.response.ParticipantDTO;

import com.google.gson.reflect.TypeToken;

public class ProcessTableFilterDTO implements FilterDTO
{

   public RangeDTO processOID;
   
   public RangeDTO rootPOID;

   public RangeDTO startTime;

   public RangeDTO endTime;
   
   public ProcessActivityDTO processName;
   
   public ProcessActivityDTO rootProcessName;
   
   public EqualsDTO status;

   public EqualsDTO priority;
   
   public ParticipantFilterDTO startingUser;

   public Map<String, DescriptorFilterDTO> descriptorFilterMap;

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
            new TypeToken<List<ParticipantDTO>>() {
      }.getType());
      return customTokens;
   }
   
   public static class ParticipantFilterDTO
   {
      public List<ParticipantDTO> participants;
   }
   
}
