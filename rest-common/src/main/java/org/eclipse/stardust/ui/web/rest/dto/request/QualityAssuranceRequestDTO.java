package org.eclipse.stardust.ui.web.rest.dto.request;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.ui.web.rest.dto.QualityAssuranceActivityDTO;
import org.eclipse.stardust.ui.web.rest.dto.QualityAssuranceDepartmentDTO;

import com.google.gson.reflect.TypeToken;

public class QualityAssuranceRequestDTO
{
   public List<QualityAssuranceDepartmentDTO> departments;
   public List<QualityAssuranceActivityDTO> activities;
   
   
   public static Map<String, Type> getCustomTokens()
   {
      Map<String, Type> customTokens = new HashMap<String, Type>();
      customTokens.put("departments", new TypeToken<List<QualityAssuranceDepartmentDTO>>()
      {
      }.getType());

      customTokens.put("activities", new TypeToken<List<QualityAssuranceActivityDTO>>()
      {
      }.getType());
      return customTokens;
   }
}
