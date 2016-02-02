package org.eclipse.stardust.ui.web.rest.component.service;

import java.io.File;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.Resource;
import javax.servlet.ServletContext;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.ui.web.html5.rest.RestControllerUtils;
import org.eclipse.stardust.ui.web.rest.component.message.RestCommonClientMessages;
import org.eclipse.stardust.ui.web.rest.component.util.CriticalityConfigUtils;
import org.eclipse.stardust.ui.web.rest.dto.CriticalityConfigDTO;
import org.eclipse.stardust.ui.web.rest.dto.CriticalityDTO;
import org.eclipse.stardust.ui.web.viewscommon.common.criticality.CriticalityCategory;
import org.eclipse.stardust.ui.web.viewscommon.common.criticality.CriticalityConfigurationUtil.ICON_COLOR;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.FileStorage;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class CriticalityConfigService
{

   private static final Logger trace = LogManager.getLogger(CriticalityConfigService.class);
   
   private final int RANGE_LOWER_LIMIT = 0;
   private final int RANGE_HIGHER_LIMIT = 1000;
   
   @Resource
   private RestCommonClientMessages restCommonClientMessages;
   
   @Resource
   private org.eclipse.stardust.ui.web.rest.component.util.CriticalityConfigUtils criticalityConfigUtilsREST;
   
   @Resource
   protected ServletContext servletContext;
   
   
   public CriticalityConfigDTO getCriticalityConfig()
   {
      CriticalityConfigDTO criticalityConfigDTO = new CriticalityConfigDTO();
      try
      {
         List<CriticalityDTO> criticalityDTOs = new ArrayList<CriticalityDTO>();
         for (CriticalityCategory criticalityCategory : criticalityConfigUtilsREST.getCriticalityCategoriesList()) {
            CriticalityDTO criticalityDTO = new CriticalityDTO();
            
            criticalityDTO.rangeFrom = criticalityCategory.getRangeFrom();
            criticalityDTO.rangeTo = criticalityCategory.getRangeTo();
            criticalityDTO.count = criticalityCategory.getIconCount();
            criticalityDTO.label = criticalityCategory.getLabel();
            if (criticalityCategory.getIconColor() != null) {
               criticalityDTO.setColor(criticalityCategory.getIconColor());
            }
            
            criticalityDTOs.add(criticalityDTO);
         }
         
         criticalityConfigDTO.criticalities = criticalityDTOs;
         criticalityConfigDTO.activityCreation = criticalityConfigUtilsREST.retrieveOnCreateCriticalityCalc();
         criticalityConfigDTO.activitySuspendAndSave = criticalityConfigUtilsREST.retrieveOnSuspendCriticalityCalc();
         criticalityConfigDTO.processPriorityChange = criticalityConfigUtilsREST.retrieveOnPrioChangeCriticalityCalc();
         criticalityConfigDTO.defaultCriticalityFormula = criticalityConfigUtilsREST.retrieveDefaultCriticalityFormula();
         
         return criticalityConfigDTO;
      }
      catch (Exception e)
      {
         trace.error(e, e);
      }
      
      return criticalityConfigDTO;
   }
   
   public void save(CriticalityConfigDTO criticalityConfigDTO) throws Exception
   {
      Set<String> errorMessages = validate(criticalityConfigDTO.criticalities);
      
      if (errorMessages.size() == 0) {
         criticalityConfigUtilsREST.saveCriticalityCategories(getCriticalityCategoriesAsMap(criticalityConfigDTO.criticalities));
         
         criticalityConfigUtilsREST.saveCriticalityEnginePreferences(criticalityConfigDTO.activityCreation,
               criticalityConfigDTO.activitySuspendAndSave, criticalityConfigDTO.processPriorityChange,
               criticalityConfigDTO.defaultCriticalityFormula);
      } else {
         StringBuffer buff = new StringBuffer();
         for (String str : errorMessages)
         {
            buff.append("- ").append(str).append("<br/>");
         }
         throw new Exception(buff.toString());
      }
   }
   
   public void exportCriticalityConfig(OutputStream outputStream) throws Exception {
      criticalityConfigUtilsREST.exportCriticalityConfig(outputStream);
   }
   
   public void importCriticalityConfig(String uuid) throws Exception
   {
      try
      {
         FileStorage fileStorage = (FileStorage) RestControllerUtils.resolveSpringBean(
               "fileStorage", servletContext);
         if (StringUtils.isNotEmpty(uuid))
         {
            String path = fileStorage.pullPath(uuid);
            if (StringUtils.isNotEmpty(path))
            {
               File file = new File(path);
               
               criticalityConfigUtilsREST.importCriticalityConfig(file);
            }
         }
      }
      catch (Exception e)
      {
         trace.error(e, e);
         throw new Exception(e.getMessage());
      }
   }
   
   
   
   private Set<String> validate(List<CriticalityDTO> criticalities)
   {
      Set<String> errorMessages = new HashSet<String>();
      Set<String> labels = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
      Set<Integer> uniqueRangeList = new HashSet<Integer>();
      for (CriticalityDTO te : criticalities)
      {
         if (te.color.equals(ICON_COLOR.WHITE))
         {
            errorMessages.add(restCommonClientMessages.getString("views.criticalityConf.criticality.validation.iconNotSelected.message"));
         }
         if (StringUtils.isEmpty(te.label))
         {
            errorMessages.add(restCommonClientMessages.getString("views.criticalityConf.criticality.validation.labelEmpty.message"));
         }
         processValueRangeValidations(uniqueRangeList, te.rangeFrom, te.rangeTo, errorMessages);         
         labels.add(te.label);
      }
      
      if (uniqueRangeList.size() < (RANGE_HIGHER_LIMIT - RANGE_LOWER_LIMIT + 1))
      {
         errorMessages.add(restCommonClientMessages.getString("views.criticalityConf.criticality.validation.values.missing.message"));
      }
      
      if (labels.size() < criticalities.size())
      {
         errorMessages.add(restCommonClientMessages.getString("views.criticalityConf.criticality.validation.labelNotUnique.message"));
      }
      
      return errorMessages;
   }
   
   private void processValueRangeValidations(Set<Integer> uniqueList, int minRange, int maxRange, Set<String> errorMessages)
   {
      if (minRange >= RANGE_LOWER_LIMIT && maxRange >= RANGE_LOWER_LIMIT
            && minRange <= RANGE_HIGHER_LIMIT && maxRange <= RANGE_HIGHER_LIMIT)
      {
         if (minRange <= maxRange)
         {
            for (int i = minRange; i <= maxRange; i++)
            {
               if (!uniqueList.add(i))
               {
                  errorMessages.add(restCommonClientMessages.getString("views.criticalityConf.criticality.validation.values.overlap.message"));
               }
            }
         }
         else
         {
            errorMessages.add(restCommonClientMessages.getString("views.criticalityConf.criticality.validation.minMaxReverse.message"));
         }
      }
      else
      {
         errorMessages.add(restCommonClientMessages.getParamString("views.criticalityConf.criticality.validation.values.oursideRange.message"
               , "[" + RANGE_LOWER_LIMIT + " - " + RANGE_HIGHER_LIMIT + "]"));
      }
   }
   
   /**
    * @return
    */
   private Map<String, Serializable> getCriticalityCategoriesAsMap(List<CriticalityDTO> criticalityCategoriesList)
   {
      Map<String, Serializable> criticalityCategoryMap = new HashMap<String, Serializable>();
      if (criticalityCategoriesList.size() > 0)
      {
         criticalityCategoryMap.put(CriticalityConfigUtils.CRITICALITY_CAT_PREF_KEY_PREFIX + "."
               + CriticalityConfigUtils.CRITICALITY_CAT_TOTAL_COUNT, criticalityCategoriesList.size());
         for (int i = 0; i < criticalityCategoriesList.size(); i++)
         {
            criticalityCategoryMap.put(CriticalityConfigUtils.CRITICALITY_CAT_PREF_KEY_PREFIX + i + "."
                  + CriticalityConfigUtils.CRITICALITY_CAT_LOWER_BOUND, criticalityCategoriesList.get(i)
                  .rangeFrom);
            criticalityCategoryMap.put(CriticalityConfigUtils.CRITICALITY_CAT_PREF_KEY_PREFIX + i + "."
                  + CriticalityConfigUtils.CRITICALITY_CAT_UPPER_BOUND, criticalityCategoriesList.get(i)
                  .rangeTo);
            criticalityCategoryMap.put(CriticalityConfigUtils.CRITICALITY_CAT_PREF_KEY_PREFIX + i + "."
                  + CriticalityConfigUtils.CRITICALITY_CAT_LABEL, criticalityCategoriesList.get(i).label);
            criticalityCategoryMap.put(CriticalityConfigUtils.CRITICALITY_CAT_PREF_KEY_PREFIX + i + "."
                  + CriticalityConfigUtils.CRITICALITY_CAT_ICON, criticalityCategoriesList.get(i).color);
            criticalityCategoryMap.put(CriticalityConfigUtils.CRITICALITY_CAT_PREF_KEY_PREFIX + i + "."
                  + CriticalityConfigUtils.CRITICALITY_CAT_ICON_DISPLAY, criticalityCategoriesList.get(i)
                  .count);
         }
      }
      
      return criticalityCategoryMap;
   }
   
}
