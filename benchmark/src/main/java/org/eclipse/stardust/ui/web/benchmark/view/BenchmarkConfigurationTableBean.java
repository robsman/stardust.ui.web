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
package org.eclipse.stardust.ui.web.benchmark.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.swing.tree.DefaultTreeModel;

import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.engine.api.runtime.DeployedModel;
import org.eclipse.stardust.engine.api.runtime.ModelReconfigurationInfo;
import org.eclipse.stardust.engine.core.preferences.PreferenceScope;
import org.eclipse.stardust.engine.core.preferences.Preferences;
import org.eclipse.stardust.engine.core.preferences.PreferencesConstants;
import org.eclipse.stardust.engine.core.preferences.configurationvariables.ConfigurationVariables;
import org.eclipse.stardust.ui.web.benchmark.common.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.benchmark.portal.messages.Messages;
import org.eclipse.stardust.ui.web.common.UIComponentBean;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnDataType;
import org.eclipse.stardust.ui.web.common.column.DefaultColumnModel;
import org.eclipse.stardust.ui.web.common.column.IColumnModel;
import org.eclipse.stardust.ui.web.common.event.ViewEvent;
import org.eclipse.stardust.ui.web.common.event.ViewEventHandler;
import org.eclipse.stardust.ui.web.common.message.MessageDialog;
import org.eclipse.stardust.ui.web.common.treetable.NodeUserObject;
import org.eclipse.stardust.ui.web.common.treetable.TreeTable;
import org.eclipse.stardust.ui.web.common.treetable.TreeTableBean;
import org.eclipse.stardust.ui.web.common.treetable.TreeTableNode;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.BenchmarkUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDefinitionUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ServiceFactoryUtils;

/**
 * @author Aditya.Gaikwad
 * @version $Revision: $ Managed bean class for Benchmark Configuration view
 */
public class BenchmarkConfigurationTableBean extends UIComponentBean implements ViewEventHandler, TreeTableBean
{
   private static final long serialVersionUID = 1L;
   private static final String DEFAULT_BENCHMARK = "-1"; // None
   private static final String BENCHMARK_MODEL_DEFAULT = "0";// Model Default

   private IColumnModel modelVariablesColumnModel;
   private Preferences benchmarkPreferences;
   private Set<BenchmarkModelConfigurationUserObject> modifiedUserObjects = new HashSet<BenchmarkModelConfigurationUserObject>();
   private TreeTable treeTable;
   private TreeTableNode rootModelNode = null;
   private boolean valueChanged = false;

   private List<SelectItem> availableBenchmarkDefs;
   private List<SelectItem> benchmarkDefsForModel;
   private String defaultBenchmarkId;
   private Boolean nonAuxiliaryProcessDefs = true;

   /**
    * Constructor for ModelVariablesViewBean
    * 
    */
   public BenchmarkConfigurationTableBean()
   {
      super(ResourcePaths.V_BENCHMARK_CONFIGURATION_PANEL_COLUMNS);

      initializeColumnModel();
      initialize();
   }

   /**
    * method to get current BenchmarkConfigurationTableBean object
    * 
    * @return
    */
   public static BenchmarkConfigurationTableBean getCurrent()
   {
      return (BenchmarkConfigurationTableBean) FacesUtils.getBeanFromContext("benchmarkConfigurationTableBean");
   }

   /**
    * method to build tree table
    * 
    * @param benchmarkConfigurations
    * @param parent
    */
   public void buildModelTree(BenchmarkConfigurations benchmarkConfigurations, TreeTableNode parent) throws Exception
   {
      BenchmarkModelConfigurationTreeItem nodeItem = new BenchmarkModelConfigurationTreeItem(benchmarkConfigurations,
            null);
      TreeTableNode treeNode = TreeNodeFactory.createTreeNode(treeTable, this, nodeItem, true);

      for (BenchmarkConfiguration benchmarkConfiguration : benchmarkConfigurations.getBenchmarkConfiguraions())
      {
         BenchmarkModelConfigurationTreeItem confItem = new BenchmarkModelConfigurationTreeItem(benchmarkConfiguration,
               benchmarkConfigurations);
         TreeTableNode confNode = TreeNodeFactory.createTreeNode(treeTable, this, confItem, true);
         treeNode.add(confNode);
      }
      parent.add(treeNode);
   }

   public int getIncreasedLabelCount()
   {
      return 0;
   }

   public TreeTableNode getRootModelNode()
   {
      return rootModelNode;
   }

   public TreeTable getTreeTable()
   {
      return treeTable;
   }

   /**
    * method to handle view events
    */
   public void handleEvent(ViewEvent event)
   {}

   /**
    * method to initialize table data
    */
   @Override
   public void initialize()
   {
      // create root node
      BenchmarkModelConfigurationTreeItem rootItem = new BenchmarkModelConfigurationTreeItem(
            new ConfigurationVariables(null), null);
      rootModelNode = TreeNodeFactory.createTreeNode(treeTable, this, rootItem, true);

      // Now Create a Model & Tree Table
      DefaultTreeModel rootNode = new DefaultTreeModel(rootModelNode);
      treeTable = new TreeTable(rootNode, modelVariablesColumnModel, null);
      treeTable.setHideRootNode(true);
      treeTable.setAutoFilter(true);
      treeTable.setFilterRootNode(true);
      rootModelNode.getUserObject().setTreeTable(treeTable);

      if (null != treeTable)
      {
         treeTable.setTooltipURL(org.eclipse.stardust.ui.web.viewscommon.core.ResourcePaths.V_PANELTOOLTIP_URL);
      }

      try
      {
         benchmarkPreferences = loadPreferences();

         Collection<DeployedModel> models = ModelCache.findModelCache().getActiveModels();

         for (Model model : models)
         {
            if (!(PredefinedConstants.PREDEFINED_MODEL_ID.equals(model.getId())))
            {
               List<BenchmarkConfiguration> benchmarkConfigurationList = new ArrayList<BenchmarkConfiguration>();
               List<ProcessDefinition> allProcessDefinitions = ProcessDefinitionUtils.getAllProcessDefinitions(model,
                     nonAuxiliaryProcessDefs);
               for (ProcessDefinition processDefinition : allProcessDefinitions)
               {
                  String defBenchmarkId = getBenchmarkFromPreferences(benchmarkPreferences, processDefinition.getQualifiedId(), false);
                  BenchmarkConfiguration bc = new BenchmarkConfiguration(model.getId(), processDefinition.getQualifiedId(),
                        processDefinition.getName(), defBenchmarkId);
                  benchmarkConfigurationList.add(bc);
               }
               String defBenchmarkId = getBenchmarkFromPreferences(benchmarkPreferences, model.getId(), true);
               BenchmarkConfigurations benchmarkConfigurations = new BenchmarkConfigurations(model.getId(),
                     defBenchmarkId, benchmarkConfigurationList);
               buildModelTree(benchmarkConfigurations, rootModelNode);
            }
         }
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }

      // Build Tree
      treeTable.initialize();
      // Rebuild the Tree
      treeTable.rebuildList();
   }

   /**
    * method define table structure
    */
   public void initializeColumnModel()
   {
      availableBenchmarkDefs = new ArrayList<SelectItem>();
      benchmarkDefsForModel = new ArrayList<SelectItem>();

      Map<String, String> benchmarkDefinitionsInfo = null;
      try
      {
         benchmarkDefinitionsInfo = BenchmarkUtils.getRuntimeBenchmarkDefinitionsInfo();
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }

      Set<Entry<String, String>> entrySet = benchmarkDefinitionsInfo.entrySet();
      int i = 1;
      for (Entry<String, String> entry : entrySet)
      {
         availableBenchmarkDefs.add(new SelectItem(entry.getKey(), entry.getValue()));
         benchmarkDefsForModel.add(new SelectItem(entry.getKey(), entry.getValue()));
         i = i + 1;
      }

      availableBenchmarkDefs.add(new SelectItem(BENCHMARK_MODEL_DEFAULT, Messages.getInstance().getString(
            "views.benchmarkPanelConfiguration.benchmarkModelDefault.label")));
      availableBenchmarkDefs.add(new SelectItem(DEFAULT_BENCHMARK, Messages.getInstance().getString(
            "views.benchmarkPanelConfiguration.defaultBenchmark.label")));
      
      benchmarkDefsForModel.add(new SelectItem(DEFAULT_BENCHMARK, Messages.getInstance().getString(
            "views.benchmarkPanelConfiguration.defaultBenchmark.label")));

      List<ColumnPreference> cols = new ArrayList<ColumnPreference>();

      ColumnPreference colModel = new ColumnPreference("Model", "modelName", ColumnDataType.STRING, "Model");

      ColumnPreference colDefaultBenchmark = new ColumnPreference("DefaultBenchmark", "defaultBenchmark",
            "Default Benchmark", ResourcePaths.V_BENCHMARK_CONFIGURATION_PANEL_COLUMNS, true, false);

      cols.add(colModel);
      cols.add(colDefaultBenchmark);

      modelVariablesColumnModel = new DefaultColumnModel(cols, null, null, UserPreferencesEntries.M_BENCHMARK,
            ResourcePaths.V_benchmarkPanelView);

      modelVariablesColumnModel.initialize();
   }

   public boolean isValueChanged()
   {
      return valueChanged || !modifiedUserObjects.isEmpty();
   }

   public void saveAndCloseConfigurationValue()
   {
      saveConfigurationValue();
   }

   /**
    * @return
    */
   public List<ModelReconfigurationInfo> saveConfigurationValue()
   {
      List<ModelReconfigurationInfo> infoList = null;
      Set<BenchmarkConfigurations> modifiedBmModelConfs = new HashSet<BenchmarkConfigurations>();
      Set<BenchmarkConfiguration> modifiedBmProcDefConfs = new HashSet<BenchmarkConfiguration>();
      AdministrationService administrationService = ServiceFactoryUtils.getServiceFactory().getAdministrationService();

      if (!modifiedUserObjects.isEmpty())
      {

         for (BenchmarkModelConfigurationUserObject userObject : modifiedUserObjects)
         {
            if (userObject.getSource() instanceof BenchmarkConfiguration)
            {
               BenchmarkConfiguration variables = (BenchmarkConfiguration) userObject.getSource();
               modifiedBmProcDefConfs.add(variables);
            }
            else if (userObject.getSource() instanceof BenchmarkConfigurations)
            {
               BenchmarkConfigurations variables = (BenchmarkConfigurations) userObject.getSource();
               modifiedBmModelConfs.add(variables);
            }
         }

         Map<String, Serializable> bmPreferencesMap = benchmarkPreferences.getPreferences();

         for (BenchmarkConfigurations benchmarkConfiguration : modifiedBmModelConfs)
         {
            bmPreferencesMap.put(benchmarkConfiguration.getModelId(), benchmarkConfiguration.getDefaultBenchmarkId());
         }

         for (BenchmarkConfiguration benchmarkConfiguration : modifiedBmProcDefConfs)
         {
            bmPreferencesMap.put(benchmarkConfiguration.getProcessId(), benchmarkConfiguration.getDefaultBenchmarkId());
         }

         administrationService.savePreferences(benchmarkPreferences);
      }

      MessageDialog
            .addInfoMessage(Messages.getInstance().getString("views.benchmarkPanelConfiguration.saveSuccessful"));

      return infoList;
   }

   public void setSelectedNodeLabel(String label)
   {}

   public void setSelectedNodeObject(NodeUserObject nodeObject)
   {}

   public void setValueChanged(boolean valueChanged)
   {
      this.valueChanged = valueChanged;
   }

   public List<SelectItem> getAvailableBenchmarkDefs()
   {
      return availableBenchmarkDefs;
   }

   public void setAvailableBenchmarkDefs(List<SelectItem> availableBenchmarkDefs)
   {
      this.availableBenchmarkDefs = availableBenchmarkDefs;
   }

   public String getDefaultBenchmarkId()
   {
      return defaultBenchmarkId;
   }

   public void setDefaultBenchmarkId(String defaultBenchmarkId)
   {
      this.defaultBenchmarkId = defaultBenchmarkId;
   }

   public List<SelectItem> getBenchmarkDefsForModel()
   {
      return benchmarkDefsForModel;
   }

   public void setBenchmarkDefsForModel(List<SelectItem> benchmarkDefsForModel)
   {
      this.benchmarkDefsForModel = benchmarkDefsForModel;
   }

   public Preferences getBenchmarkPreferences()
   {
      return benchmarkPreferences;
   }

   public void setBenchmarkPreferences(Preferences benchmarkPreferences)
   {
      this.benchmarkPreferences = benchmarkPreferences;
   }

   /**
    * method to update tree table and reset some flags
    */
   public void update()
   {
      modifiedUserObjects.clear();
      valueChanged = false;
      initialize();
   }

   /**
    * method get called for value change on tree table value text field
    * 
    * @see ValueChangeEvent
    * @param event
    */
   public void valueChange(ValueChangeEvent event)
   {
      valueChanged = true;

      String texto = event.getNewValue().toString();
      BenchmarkModelConfigurationUserObject userObject = (BenchmarkModelConfigurationUserObject) event.getComponent()
            .getAttributes().get("row");
      userObject.setDefaultBenchmarkId(texto);
      modifiedUserObjects.add(userObject);
   }

   /**
    * @return
    * @throws Exception
    */
   private Preferences loadPreferences() throws Exception
   {
      AdministrationService administrationService = ServiceFactoryUtils.getServiceFactory().getAdministrationService();
      Preferences benchmarkPreferences = administrationService.getPreferences(PreferenceScope.PARTITION,
            PreferencesConstants.MODULE_ID_ENGINE_INTERNALS, PreferencesConstants.PREFERENCE_ID_BENCHMARKS);

      return benchmarkPreferences;
   }

   /**
    * 
    */
   public void saveBenchmarkPreferences()
   {
      AdministrationService administrationService = ServiceFactoryUtils.getServiceFactory().getAdministrationService();
      administrationService.savePreferences(benchmarkPreferences);
   }

   /**
    * @param preferences
    * @param id
    * @return
    */
   private String getBenchmarkFromPreferences(Preferences preferences, String id, Boolean isModel)
   {
      Map<String, Serializable> bmPreferencesMap = preferences.getPreferences();
      Serializable serializable = bmPreferencesMap.get(id);
      return (serializable != null) ? serializable.toString() : (isModel) ? DEFAULT_BENCHMARK : BENCHMARK_MODEL_DEFAULT;
   }

   /**
    * 
    */
   public void reset()
   {
      Map<String, Serializable> benchmarkPreferencesMap = benchmarkPreferences.getPreferences();
      Set<Entry<String, Serializable>> bmEntrySet = benchmarkPreferencesMap.entrySet();
      bmEntrySet.clear();
      /*for (Entry<String, Serializable> entry : bmEntrySet)
      {
         if (!(entry.getKey().equals(ActivityInstanceStateChangeMonitor.BENCHMARK_PREF_RECALC_ONCREATE) || entry
               .getKey().equals(ActivityInstanceStateChangeMonitor.BENCHMARK_PREF_RECALC_ONSUSPEND)))
         {
            entry.setValue(DEFAULT_BENCHMARK);
         }
      }*/
      FacesUtils.clearFacesTreeValues();
      AdministrationService administrationService = ServiceFactoryUtils.getServiceFactory().getAdministrationService();
      administrationService.savePreferences(benchmarkPreferences);
      initialize();
   }
   
   public void toggleAuxPDFilter(ActionEvent ae) {
      nonAuxiliaryProcessDefs = (nonAuxiliaryProcessDefs) ? false : true;
      initialize();
   }

   public Boolean getNonAuxiliaryProcessDefs()
   {
      return nonAuxiliaryProcessDefs;
   }

   public void setNonAuxiliaryProcessDefs(Boolean nonAuxiliaryProcessDefs)
   {
      this.nonAuxiliaryProcessDefs = nonAuxiliaryProcessDefs;
   }

}
