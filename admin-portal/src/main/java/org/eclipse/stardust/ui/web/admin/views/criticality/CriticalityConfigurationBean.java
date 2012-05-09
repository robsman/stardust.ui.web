/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.admin.views.criticality;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.engine.core.javascript.CriticalityEvaluationAction;
import org.eclipse.stardust.engine.core.monitoring.ActivityInstanceStateChangeMonitor;
import org.eclipse.stardust.engine.core.monitoring.UpdateCriticalityAction;
import org.eclipse.stardust.engine.core.preferences.PreferenceScope;
import org.eclipse.stardust.engine.core.preferences.Preferences;
import org.eclipse.stardust.engine.core.preferences.PreferencesConstants;
import org.eclipse.stardust.ui.web.admin.messages.AdminMessagesPropertiesBean;
import org.eclipse.stardust.ui.web.common.UIComponentBean;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnAlignment;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnDataType;
import org.eclipse.stardust.ui.web.common.column.DefaultColumnModel;
import org.eclipse.stardust.ui.web.common.column.IColumnModel;
import org.eclipse.stardust.ui.web.common.event.ViewEvent;
import org.eclipse.stardust.ui.web.common.event.ViewEvent.ViewEventType;
import org.eclipse.stardust.ui.web.common.event.ViewEventHandler;
import org.eclipse.stardust.ui.web.common.message.MessageDialog;
import org.eclipse.stardust.ui.web.common.table.DataTable;
import org.eclipse.stardust.ui.web.common.table.DataTableRowSelector;
import org.eclipse.stardust.ui.web.common.table.IDataTable;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.common.configuration.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.viewscommon.common.criticality.CriticalityCategory;
import org.eclipse.stardust.ui.web.viewscommon.common.criticality.CriticalityConfigurationHelper;
import org.eclipse.stardust.ui.web.viewscommon.common.criticality.CriticalityConfigurationUtil;
import org.eclipse.stardust.ui.web.viewscommon.common.criticality.CriticalityConfigurationUtil.ICON_COLOR;
import org.eclipse.stardust.ui.web.viewscommon.core.ResourcePaths;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.ConfigurationImportDialogBean;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.PreferencesResource;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;

import com.icesoft.faces.context.Resource;



/**
 * @author Shrikant.Gangal
 *
 */
public class CriticalityConfigurationBean extends UIComponentBean implements ViewEventHandler
{
   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private IDataTable<CriticalityConfigurationTableEntry> criticalityConfEntryTable;
   
   private String defaultCriticalityFormula = "";
   
   private boolean onActivityCreation = true;
   private boolean onActivitySuspendAndSave = true;
   private boolean onProcessPriorityChange = true;
   private List<CriticalityConfigurationTableEntry> criticalityCategoriesList;
   private AdminMessagesPropertiesBean propsBean;
   private final int RANGE_LOWER_LIMIT = 0;
   private final int RANGE_HIGHER_LIMIT = 1000;
   private Set<String> errorMessages;
   private Resource fileResource;
   
   /**
    * 
    */
   public CriticalityConfigurationBean()
   {
      super("criticalityConfigurationBean");
      propsBean = AdminMessagesPropertiesBean.getInstance();
      try
      {
         initialize();
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }
   }
   
   public static CriticalityConfigurationBean getInstance()
   {
       return (CriticalityConfigurationBean) FacesContext.getCurrentInstance().getApplication()
           .getVariableResolver().resolveVariable(FacesContext.getCurrentInstance(),
                 "criticalityConfigurationBean");
   }

   @Override
   public void initialize()
   {
      errorMessages = new HashSet<String>();
      initializeCriticalityConfTable();
      onActivityCreation = retrieveOnCreateCriticalityCalc();
      onActivitySuspendAndSave = retrieveOnSuspendCriticalityCalc();
      onProcessPriorityChange = retrieveOnPrioChangeCriticalityCalc();
      defaultCriticalityFormula = retrieveDefaultCriticalityFormula();
      initializeFileResource();
   }
   
   /**
    * Initializes the resource object - which is used for the export functionality
    * The object needs to be reinitialized after every change to the configuration to reflect the changes
    * in the exported file.
    */
   public void initializeFileResource()
   {
      List<Preferences> preferencesList = new ArrayList<Preferences>();
      preferencesList.add(CriticalityConfigurationUtil.readCriticalityCategoryPrefs());
      fileResource = new PreferencesResource(preferencesList);
   }

   public void handleEvent(ViewEvent event)
   {
      if (ViewEventType.CREATED == event.getType())
      {
         initialize();
      }
   }
   
   public IDataTable<CriticalityConfigurationTableEntry> getCriticalityConfEntryTable()
   {
      return criticalityConfEntryTable;
   }

   public void setCriticalityConfEntryTable(IDataTable<CriticalityConfigurationTableEntry> criticalityConfEntryTable)
   {
      this.criticalityConfEntryTable = criticalityConfEntryTable;
   }
   public boolean isOnActivityCreation()
   {
      return onActivityCreation;
   }
   public void setOnActivityCreation(boolean onActivityCreation)
   {
      this.onActivityCreation = onActivityCreation;
   }
   public boolean isOnActivitySuspendAndSave()
   {
      return onActivitySuspendAndSave;
   }
   public void setOnActivitySuspendAndSave(boolean onActivitySuspendAndSave)
   {
      this.onActivitySuspendAndSave = onActivitySuspendAndSave;
   }
   public boolean isOnProcessPriorityChange()
   {
      return onProcessPriorityChange;
   }
   public void setOnProcessPriorityChange(boolean onProcessPriorityChange)
   {
      this.onProcessPriorityChange = onProcessPriorityChange;
   }

   public String getDefaultCriticalityFormula()
   {
      return defaultCriticalityFormula;
   }

   public void setDefaultCriticalityFormula(String defaultCriticalityFormula)
   {
      this.defaultCriticalityFormula = defaultCriticalityFormula;
   }

   public Set<String> getErrorMessages()
   {
      return errorMessages;
   }

   public void save(ActionEvent event)
   {
      try
      {
         if (validate())
         {
            //Sort the table in ascending order
            Collections.sort(criticalityCategoriesList, new Comparator<CriticalityConfigurationTableEntry>() {
               public int compare(CriticalityConfigurationTableEntry o1, CriticalityConfigurationTableEntry o2)
               {
                  return o1.getRangeFrom() - o2.getRangeFrom();
               }
            });
            clearRowSelection();
            clearEditable();
            criticalityConfEntryTable.initialize();
            CriticalityConfigurationUtil.saveCriticalityCategories(getCriticalityCategoriesAsMap());
            saveCriticalityEnginePreferences();
            initializeFileResource();

            //Re-initialize criticality configuration helper.
            CriticalityConfigurationHelper.getInstance().initialize();
            MessageDialog.addInfoMessage(propsBean.getString("views.criticalityConf.criticality.save.success.dialog"));
         }
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e,
               propsBean.getString("views.criticalityConf.criticality.save.failure.dialog"));
      }
   }
   
   public void addRow(ActionEvent event)
   {
      criticalityCategoriesList.add(getNewCriticalityConfigurationEntry());
      criticalityConfEntryTable.initialize();
      validate();
   }
   
   public void editRow(ActionEvent event)
   {
      for (CriticalityConfigurationTableEntry te : criticalityCategoriesList)
      {
         if(te.isSelected())
         {
            te.setEditable(true);
         }
      }
   }
   
   public void removeRows(ActionEvent event)
   {
      Iterator<CriticalityConfigurationTableEntry> iter = criticalityCategoriesList.iterator();
      while (iter.hasNext())
      {
         if(iter.next().isSelected())
         {
            iter.remove();
         }
      }
      criticalityConfEntryTable.initialize();
      validate();
   }
   
   public void reset(ActionEvent event)
   {
      try
      {
         initialize();
         MessageDialog.addInfoMessage(propsBean.getString("views.criticalityConf.criticality.reset.success.dialog"));
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e,
               propsBean.getString("views.criticalityConf.criticality.reset.failure.dialog"));
      }
   }
   
   public void closeAllIconSelectors()
   {
      for (CriticalityConfigurationTableEntry te : criticalityCategoriesList)
      {
         te.getCriticalityIconsSelectorPopup().setVisible(false);
      }
   }
   
   public String getValidationMsg()
   {      
      if (errorMessages.size() > 0)
      {
         StringBuffer buff = new StringBuffer();
         for (String str : errorMessages)
         {
            buff.append("- ").append(str).append("<br/>");
         }
         
         return buff.toString();
      }
      
      return null;
   }
   
   /**
    * Return true is at least one row is selected.
    * 
    * @return
    */
   public boolean isDeleteEnabled()
   {
      for (CriticalityConfigurationTableEntry te : criticalityCategoriesList)
      {
         if (te.isSelected())
         {
            return true;
         }
      }
      
      return false;
   }
   
   /**
    * Return true is at least one uneditable row is selected.
    * 
    * @return
    */
   public boolean isEditEnabled()
   {
      for (CriticalityConfigurationTableEntry te : criticalityCategoriesList)
      {
         if (te.isSelected() && !te.isEditable())
         {
            return true;
         }
      }
      
      return false;
   }
   

   /**
    * @param event
    */
   public void importVariables(ActionEvent event)
   {
      ConfigurationImportDialogBean.getCurrent().openPopup();
   }   

   /**
    * @return
    */
   public Resource getFileResource()
   {
      return fileResource;
   }
   
   private void clearRowSelection()
   {
      for (CriticalityConfigurationTableEntry te : criticalityCategoriesList)
      {
         te.setSelected(false);
      }
   }
   
   private void clearEditable()
   {
      for (CriticalityConfigurationTableEntry te : criticalityCategoriesList)
      {
         te.setEditable(false);
      }
   }

   public boolean validate()
   {
      errorMessages = new HashSet<String>();
      Set<String> labels = new HashSet<String>();
      Set<Integer> uniqueRangeList = new HashSet<Integer>();
      for (CriticalityConfigurationTableEntry te : criticalityCategoriesList)
      {
         if (te.getIconColor().equals(ICON_COLOR.WHITE))
         {
            errorMessages.add(propsBean.getString("views.criticalityConf.criticality.validation.iconNotSelected.message"));
         }
         if (StringUtils.isEmpty(te.getLabel()))
         {
            errorMessages.add(propsBean.getString("views.criticalityConf.criticality.validation.labelEmpty.message"));
         }
         processValueRangeValidations(uniqueRangeList, te.getRangeFrom(), te.getRangeTo(), errorMessages);         
         labels.add(te.getLabel());
      }
      
      if (uniqueRangeList.size() < (RANGE_HIGHER_LIMIT - RANGE_LOWER_LIMIT + 1))
      {
         errorMessages.add(propsBean.getString("views.criticalityConf.criticality.validation.values.missing.message"));
      }
      
      if (labels.size() < criticalityCategoriesList.size())
      {
         errorMessages.add(propsBean.getString("views.criticalityConf.criticality.validation.labelNotUnique.message"));
      }
      
      if (errorMessages.size() > 0)
      {
         return false;
      }
      
      return true;
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
                  errorMessages.add(propsBean.getString("views.criticalityConf.criticality.validation.values.overlap.message"));
               }
            }
         }
         else
         {
            errorMessages.add(propsBean.getString("views.criticalityConf.criticality.validation.minMaxReverse.message"));
         }
      }
      else
      {
         errorMessages.add(propsBean.getParamString("views.criticalityConf.criticality.validation.values.oursideRange.message", "[" + RANGE_LOWER_LIMIT + " - " + RANGE_HIGHER_LIMIT + "]"));
      }
   }
   
   private void initializeCriticalityConfTable()
   {

      List<ColumnPreference> cols = new ArrayList<ColumnPreference>();

      ColumnPreference colCriticalityVal = new ColumnPreference("CriticalityValue", propsBean.getString("views.criticalityConf.criticality.categories.criticalityValue"));
      ColumnPreference colRangeFrom = new ColumnPreference("RangeFrom", "rangeFrom", ColumnDataType.STRING,
            propsBean.getString("views.criticalityConf.criticality.categories.rangeFrom"), true, false);
      colRangeFrom.setColumnAlignment(ColumnAlignment.CENTER);
      colRangeFrom.setColumnContentUrl("/plugins/admin-portal/views/criticalityConfigurationColumns.xhtml");
      ColumnPreference colRangeTo = new ColumnPreference("RangeTo", "rangeTo", ColumnDataType.STRING,
            propsBean.getString("views.criticalityConf.criticality.categories.rangeTo"), true, false);
      colRangeTo.setColumnAlignment(ColumnAlignment.CENTER);
      colRangeTo.setColumnContentUrl("/plugins/admin-portal/views/criticalityConfigurationColumns.xhtml");
      colCriticalityVal.addChildren(colRangeFrom);
      colCriticalityVal.addChildren(colRangeTo);
      ColumnPreference colLabel = new ColumnPreference("Label", "label", ColumnDataType.STRING,
            propsBean.getString("views.criticalityConf.criticality.categories.label"), true, false);
      colLabel.setColumnAlignment(ColumnAlignment.LEFT);
      colLabel.setColumnContentUrl("/plugins/admin-portal/views/criticalityConfigurationColumns.xhtml");
      
      ColumnPreference colIcon = new ColumnPreference("Icon", "icon", ColumnDataType.STRING,
            propsBean.getString("views.criticalityConf.criticality.categories.icon"), true, false);
      colIcon.setColumnAlignment(ColumnAlignment.CENTER);
      colIcon.setColumnContentUrl("/plugins/admin-portal/views/criticalityConfigurationColumns.xhtml");
      
      ColumnPreference colIconDisplay = new ColumnPreference("IconDisplay", "iconDisplay", ColumnDataType.STRING,
            propsBean.getString("views.criticalityConf.criticality.categories.iconDisplay"), true, false);
      colIconDisplay.setColumnAlignment(ColumnAlignment.CENTER);
      colIconDisplay.setColumnContentUrl("/plugins/admin-portal/views/criticalityConfigurationColumns.xhtml");

      cols.add(colCriticalityVal);
      cols.add(colLabel);
      cols.add(colIcon);
      cols.add(colIconDisplay);
     
      List<ColumnPreference> fixedAfterColumns = new ArrayList<ColumnPreference>();

      

      IColumnModel criticalityConfColumnModel = new DefaultColumnModel(cols, null, fixedAfterColumns,
            UserPreferencesEntries.M_ADMIN_PORTAL, ResourcePaths.V_CRITICALITY_CONFIG_VIEW);

      // without column selector
      criticalityConfEntryTable = new DataTable<CriticalityConfigurationTableEntry>(null, criticalityConfColumnModel, null);
      initializeConfigCategoriesList();
      criticalityConfEntryTable.setList(criticalityCategoriesList);
      criticalityConfEntryTable.setRowSelector(new DataTableRowSelector("selected",true));
      criticalityConfEntryTable.initialize();
   
   }
   
   private void initializeConfigCategoriesList()
   {
      criticalityCategoriesList = new ArrayList<CriticalityConfigurationTableEntry>();
      List<CriticalityCategory> ccList = CriticalityConfigurationUtil.getCriticalityCategoriesList();
      for (CriticalityCategory cc : ccList)
      {
         CriticalityConfigurationTableEntry ce = new CriticalityConfigurationTableEntry(cc.getRangeFrom(),
               cc.getRangeTo(), cc.getLabel(), cc.getIconColor(), cc.getIconCount());
         criticalityCategoriesList.add(ce);
      }
   }
   
   private CriticalityConfigurationTableEntry getNewCriticalityConfigurationEntry()
   {
      CriticalityConfigurationTableEntry neo = new CriticalityConfigurationTableEntry();
      neo.setRangeFrom(0);
      neo.setRangeTo(0);
      neo.setIconColor(ICON_COLOR.WHITE);
      neo.setIconCount(1);
      neo.setLabel("");
      neo.setEditable(true);
      
      return neo;
   }

   /**
    * @return
    */
   private boolean retrieveOnCreateCriticalityCalc()
   {
      AdministrationService adminService = SessionContext.findSessionContext().getServiceFactory().getAdministrationService();
      Preferences retrievedPrefs = adminService.getPreferences(PreferenceScope.PARTITION,
            PreferencesConstants.MODULE_ID_ENGINE_INTERNALS,
            PreferencesConstants.PREFERENCE_ID_WORKFLOW_CRITICALITES);
      Boolean criteria = (Boolean) retrievedPrefs.getPreferences().get(ActivityInstanceStateChangeMonitor.CRITICALITY_PREF_RECALC_ONCREATE);
      
      if (null != criteria)
      {
         return criteria.booleanValue();
      }
      
      return true; //default true
   }
   
   /**
    * @return
    */
   private boolean retrieveOnSuspendCriticalityCalc()
   {
      AdministrationService adminService = SessionContext.findSessionContext().getServiceFactory().getAdministrationService();
      Preferences retrievedPrefs = adminService.getPreferences(PreferenceScope.PARTITION,
            PreferencesConstants.MODULE_ID_ENGINE_INTERNALS,
            PreferencesConstants.PREFERENCE_ID_WORKFLOW_CRITICALITES);
      Boolean criteria = (Boolean) retrievedPrefs.getPreferences().get(ActivityInstanceStateChangeMonitor.CRITICALITY_PREF_RECALC_ONSUSPEND);
      
      if (null != criteria)
      {
         return criteria.booleanValue();
      }
      
      return true; //default true
   }
   
   /**
    * @return
    */
   private boolean retrieveOnPrioChangeCriticalityCalc()
   {
      AdministrationService adminService = SessionContext.findSessionContext().getServiceFactory().getAdministrationService();
      Preferences retrievedPrefs = adminService.getPreferences(PreferenceScope.PARTITION,
            PreferencesConstants.MODULE_ID_ENGINE_INTERNALS,
            PreferencesConstants.PREFERENCE_ID_WORKFLOW_CRITICALITES);
      Boolean criteria = (Boolean) retrievedPrefs.getPreferences().get(UpdateCriticalityAction.CRITICALITY_PREF_RECALC_ONPRIORITY);
      
      if (null != criteria)
      {
         return criteria.booleanValue();
      }
      
      return true; //default true
   }
   
   /**
    * @return
    */
   private String retrieveDefaultCriticalityFormula()
   {
      AdministrationService adminService = SessionContext.findSessionContext().getServiceFactory().getAdministrationService();
      Preferences retrievedPrefs = adminService.getPreferences(PreferenceScope.PARTITION,
            PreferencesConstants.MODULE_ID_ENGINE_INTERNALS,
            PreferencesConstants.PREFERENCE_ID_WORKFLOW_CRITICALITES);
      String defaultFormula = (String) retrievedPrefs.getPreferences().get(CriticalityEvaluationAction.DEFAULT_PREF_CRITICALITY_FORMULA);
      
      if (StringUtils.isNotEmpty(defaultFormula))
      {
         return defaultFormula;
      }
      
      return "";
   }
   
   /**
    * 
    */
   private void saveCriticalityEnginePreferences()
   {
      AdministrationService adminService = SessionContext.findSessionContext().getServiceFactory().getAdministrationService();
      Map<String, Serializable> preferenceMap = new HashMap<String, Serializable>();
      preferenceMap.put(ActivityInstanceStateChangeMonitor.CRITICALITY_PREF_RECALC_ONCREATE,
            onActivityCreation);
      preferenceMap.put(ActivityInstanceStateChangeMonitor.CRITICALITY_PREF_RECALC_ONSUSPEND,
            onActivitySuspendAndSave);
      preferenceMap.put(UpdateCriticalityAction.CRITICALITY_PREF_RECALC_ONPRIORITY, onProcessPriorityChange);
      preferenceMap.put(CriticalityEvaluationAction.DEFAULT_PREF_CRITICALITY_FORMULA, defaultCriticalityFormula);

      Preferences prefs = new Preferences(PreferenceScope.PARTITION,
            PreferencesConstants.MODULE_ID_ENGINE_INTERNALS,
            PreferencesConstants.PREFERENCE_ID_WORKFLOW_CRITICALITES, preferenceMap);

      adminService.savePreferences(prefs);
   }
   
   /**
    * @return
    */
   private Map<String, Serializable> getCriticalityCategoriesAsMap()
   {
      Map<String, Serializable> criticalityCategoryMap = new HashMap<String, Serializable>();
      if (criticalityCategoriesList.size() > 0)
      {
         criticalityCategoryMap.put(CriticalityConfigurationUtil.CRITICALITY_CAT_PREF_KEY_PREFIX + "."
               + CriticalityConfigurationUtil.CRITICALITY_CAT_TOTAL_COUNT, criticalityCategoriesList.size());
         for (int i = 0; i < criticalityCategoriesList.size(); i++)
         {
            criticalityCategoryMap.put(CriticalityConfigurationUtil.CRITICALITY_CAT_PREF_KEY_PREFIX + i + "."
                  + CriticalityConfigurationUtil.CRITICALITY_CAT_LOWER_BOUND, criticalityCategoriesList.get(i)
                  .getRangeFrom());
            criticalityCategoryMap.put(CriticalityConfigurationUtil.CRITICALITY_CAT_PREF_KEY_PREFIX + i + "."
                  + CriticalityConfigurationUtil.CRITICALITY_CAT_UPPER_BOUND, criticalityCategoriesList.get(i)
                  .getRangeTo());
            criticalityCategoryMap.put(CriticalityConfigurationUtil.CRITICALITY_CAT_PREF_KEY_PREFIX + i + "."
                  + CriticalityConfigurationUtil.CRITICALITY_CAT_LABEL, criticalityCategoriesList.get(i).getLabel());
            criticalityCategoryMap.put(CriticalityConfigurationUtil.CRITICALITY_CAT_PREF_KEY_PREFIX + i + "."
                  + CriticalityConfigurationUtil.CRITICALITY_CAT_ICON, criticalityCategoriesList.get(i).getIconColor().toString());
            criticalityCategoryMap.put(CriticalityConfigurationUtil.CRITICALITY_CAT_PREF_KEY_PREFIX + i + "."
                  + CriticalityConfigurationUtil.CRITICALITY_CAT_ICON_DISPLAY, criticalityCategoriesList.get(i)
                  .getIconCount());
         }
      }

      return criticalityCategoryMap;
   }
}
