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
package org.eclipse.stardust.ui.web.admin.views.model;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.swing.tree.DefaultTreeModel;

import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.query.PreferenceQuery;
import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.engine.api.runtime.DeployedModel;
import org.eclipse.stardust.engine.api.runtime.ModelReconfigurationInfo;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.core.preferences.PreferenceScope;
import org.eclipse.stardust.engine.core.preferences.Preferences;
import org.eclipse.stardust.engine.core.preferences.configurationvariables.ConfigurationVariable;
import org.eclipse.stardust.engine.core.preferences.configurationvariables.ConfigurationVariableUtils;
import org.eclipse.stardust.engine.core.preferences.configurationvariables.ConfigurationVariables;
import org.eclipse.stardust.ui.web.admin.AdminportalConstants;
import org.eclipse.stardust.ui.web.admin.ResourcePaths;
import org.eclipse.stardust.ui.web.admin.WorkflowFacade;
import org.eclipse.stardust.ui.web.admin.common.configuration.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.admin.views.TreeNodeFactory;
import org.eclipse.stardust.ui.web.common.UIComponentBean;
import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.common.app.View;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference;
import org.eclipse.stardust.ui.web.common.column.DefaultColumnModel;
import org.eclipse.stardust.ui.web.common.column.IColumnModel;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnDataType;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialogHandler;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogActionType;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogContentType;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogStyle;
import org.eclipse.stardust.ui.web.common.event.ViewEvent;
import org.eclipse.stardust.ui.web.common.event.ViewEventHandler;
import org.eclipse.stardust.ui.web.common.event.ViewEvent.ViewEventType;
import org.eclipse.stardust.ui.web.common.treetable.NodeUserObject;
import org.eclipse.stardust.ui.web.common.treetable.TreeTable;
import org.eclipse.stardust.ui.web.common.treetable.TreeTableBean;
import org.eclipse.stardust.ui.web.common.treetable.TreeTableNode;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.common.util.StringUtils;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.ConfigurationImportDialogBean;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.ICallbackHandler;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.PreferencesResource;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;

import com.icesoft.faces.context.Resource;


/**
 * @author Vikas.Mishra 
 * Managed bean class for ConfigurationVariables view
 */
public class ConfigurationVariablesBean extends UIComponentBean
      implements ViewEventHandler, TreeTableBean, ICallbackHandler, ConfirmationDialogHandler
{
   private static final long serialVersionUID = 1L;
   private IColumnModel modelVariablesColumnModel;
   private List<ConfigurationVariables> allConfigurationVariables;
   private List<Preferences> preferencesList;
   private Resource fileResource;
   private ServiceFactory serviceFactory;
   private Set<Integer> activeVersions;
   private Set<ModelConfigurationUserObject> modifiedUserObjects = new HashSet<ModelConfigurationUserObject>();
   private TreeTable treeTable;
   private TreeTableNode rootModelNode = null;
   private View currentView;
   private WorkflowFacade workflowFacade;
   private boolean hideDefaultValue = false;
   private boolean showActiveModel = false;
   private boolean valueChanged = false;
   private ConfirmationDialog configVariableConfirmationDialog;

   /**
    * Constructor for ModelVariablesViewBean
    * 
    */
   public ConfigurationVariablesBean()
   {
      super(ResourcePaths.V_configurationVariablesView);

      workflowFacade = (WorkflowFacade) SessionContext.findSessionContext()
            .lookup(AdminportalConstants.WORKFLOW_FACADE);
   }

   /**
    * use this constructor when we show Configuration Variables from model file instead if
    * load from DB/preference store.
    * 
    * @param hideDefaultValue
    */
   public ConfigurationVariablesBean(boolean hideDefaultValue)
   {
      this(); // call default constructor
      this.hideDefaultValue = hideDefaultValue;
   }

   /**
    * method to get current ConfigurationVariablesBean object
    * 
    * @return
    */
   public static ConfigurationVariablesBean getCurrent()
   {
      return (ConfigurationVariablesBean) FacesUtils.getBeanFromContext("configurationVariablesBean");
   }

   /**
    * method to build tree table
    * 
    * @param variables
    * @param parent
    */
   public void buildModelTree(ConfigurationVariables variables, TreeTableNode parent) throws Exception
   {
      ModelConfigurationTreeItem nodeItem = new ModelConfigurationTreeItem(variables, null);
      TreeTableNode treeNode = TreeNodeFactory.createTreeNode(treeTable, this, nodeItem, true);
      ConfigurationVariables preferenceVariables = null;
      if (isHideDefaultValue())
      {
         AdministrationService administrationService = SessionContext.findSessionContext().getServiceFactory()
               .getAdministrationService();
         preferenceVariables = administrationService.getConfigurationVariables(variables.getModelId());
      }
      List<ConfigurationVariable> vars=variables.getConfigurationVariables();
      
    
      Collections.sort(vars, new Comparator<ConfigurationVariable>()
      {

         public int compare(ConfigurationVariable o1, ConfigurationVariable o2)
         {
            return o1.getName().compareToIgnoreCase(o2.getName());
         }
      });
      
      for (ConfigurationVariable confVariable :vars)
      {
         //CASE-1( Model deployment dialog)
         if (isHideDefaultValue())
         {

            if (StringUtils.isNotEmpty(confVariable.getDefaultValue()))
            {
               continue;

            }
            else if (isPreferencesVariablesExist(confVariable, preferenceVariables))
            {
               continue;
            }
            else
            {
               ModelConfigurationTreeItem confItem = new ModelConfigurationTreeItem(confVariable, variables);
               TreeTableNode confNode = TreeNodeFactory.createTreeNode(treeTable, this, confItem, true);
               treeNode.add(confNode);
            }
         }
         //CASE-2(Configuration Variable Page)
         else if (!showActiveModel || activeVersions.contains(confVariable.getModelOid()))
         {
            ModelConfigurationTreeItem confItem = new ModelConfigurationTreeItem(confVariable, variables);
            TreeTableNode confNode = TreeNodeFactory.createTreeNode(treeTable, this, confItem, true);
            treeNode.add(confNode);

         }
      }

      // do not show model node if it is not contains any children
      if (!treeNode.getChildren().isEmpty())
      {
         parent.add(treeNode);
      }
   }

   private boolean isPreferencesVariablesExist(ConfigurationVariable confVariable,
         ConfigurationVariables preferenceVariables)
   {
      for (ConfigurationVariable var : preferenceVariables.getConfigurationVariables())
      {
         if (var.getName().equals(confVariable.getName()) && StringUtils.isNotEmpty(var.getValue()))
         {
            return true;
         }

      }
      return false;

   }

   public void closeView()
   {
      valueChanged = false;
      modifiedUserObjects.clear();

      if (currentView != null)
      {
         // PortalApplication portalApplication = (PortalApplication) FacesUtils
         // .getBeanFromContext(PortalApplication.BEAN_NAME);
         // portalApplication.closeView(currentView);
         PortalApplication.getInstance().closeView(currentView, true);
      }
   }

   public List<ConfigurationVariables> getAllConfigurationVariables()
   {
      return allConfigurationVariables;
   }

   public Resource getFileResource()
   {
      return fileResource;
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
   {
      if (ViewEventType.CREATED == event.getType())
      {
         currentView = null;
         initializeColumnModel();
         serviceFactory = SessionContext.findSessionContext().getServiceFactory();
      }
      else if ((ViewEventType.ACTIVATED == event.getType()) && !valueChanged && (currentView == null))
      {
         try
         {
            preferencesList = loadPreferences();
            initActiveVersions();
            initialize();
            fileResource = new PreferencesResource(preferencesList);
         }
         catch (Exception e)
         {
            ExceptionHandler.handleException(e);
         }
      }
      else if (ViewEventType.TO_BE_CLOSED == event.getType())
      {
         if (valueChanged && !modifiedUserObjects.isEmpty())
         {
            currentView = event.getView();
            openSaveConfiguration();
            event.setVetoed(true);
         }
      }
   }

   public void handleEvent(EventType event)
   {
      if (event.equals(EventType.APPLY))
      {
         forceSaveConfigurationValue();
         update();
      }
      closeView();     
   }
   public void forceSaveConfigurationValue()
   {
      AdministrationService administrationService = workflowFacade.getServiceFactory().getAdministrationService();
      for (ModelConfigurationUserObject userObject : modifiedUserObjects)
      {
         if (userObject.getSource() instanceof ConfigurationVariable)
         {
            try
            {
               ConfigurationVariables variables = (ConfigurationVariables) userObject.getParent();
               administrationService.saveConfigurationVariables(variables, true);
            }
            catch (Exception e)
            {
               //do nothing
            }
         }
      }
      modifiedUserObjects.clear();
   }

   public void importVariables(ActionEvent event)
   {
      ConfigurationImportDialogBean com = ConfigurationImportDialogBean.getCurrent();
      com.openPopup();
   }
   
   public void initialize(List<ConfigurationVariables> allConfigurationVariables)
{
      this.allConfigurationVariables=allConfigurationVariables;
      initialize();
}
 

   /**
    * method to initialize table data
    */
   @Override
   public void initialize()
   {
      // create root node
      ModelConfigurationTreeItem rootItem = new ModelConfigurationTreeItem(new ConfigurationVariables(null), null);
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
         // create root node end
         if (!isHideDefaultValue())
         {
            AdministrationService administrationService = workflowFacade.getServiceFactory().getAdministrationService();
            Collection<DeployedModel> models = ModelCache.findModelCache().getAllModels();
            Set<String> idSet = new HashSet<String>();

            for (Model model : models)
            {
               idSet.add(model.getId());
            }

            for (String id : idSet)
            {
               ConfigurationVariables confVariables = administrationService.getConfigurationVariables(id);

               // add model only if ConfigurationVariables present for model id
               if (!confVariables.getConfigurationVariables().isEmpty())
               {
                  buildModelTree(confVariables, rootModelNode);
               }
            }
         }
         else
         {
            for (ConfigurationVariables variables : allConfigurationVariables)
            {
               buildModelTree(variables, rootModelNode);
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
      List<ColumnPreference> cols = new ArrayList<ColumnPreference>();

      ColumnPreference colModel = new ColumnPreference("Model", "modelName", ColumnDataType.STRING, this.getMessages()
            .getString("column.modelName"));

      ColumnPreference colName = new ColumnPreference("Name", "name", ColumnDataType.STRING, this.getMessages()
            .getString("column.name"));

      ColumnPreference colValue = new ColumnPreference("Value", "value", this.getMessages().getString("column.value"),
            ResourcePaths.V_MODEL_CONFIGURATION_VIEW_COLUMNS, true, false);

      ColumnPreference colType = new ColumnPreference("Type", "type", ColumnDataType.STRING, this.getMessages()
            .getString("column.type"));
      // if (hideDefaultValue)
      // {
      // colValue.setColumnDataFilterPopup(new TableDataFilterPopup(new
      // TableDataFilterSearch()));
      // }
      ColumnPreference colDescription = new ColumnPreference("Description", "description", this
            .getMessages().getString("column.description"),ResourcePaths.V_MODEL_CONFIGURATION_VIEW_COLUMNS,true, false);

      cols.add(colModel);
      cols.add(colName);
      cols.add(colValue);
      cols.add(colDescription);
      cols.add(colType);

      if (!isHideDefaultValue())
      {
         ColumnPreference colDefaultValue = new ColumnPreference("Default Value", "defaultValue", this.getMessages()
               .getString("column.defaultValue"), ResourcePaths.V_MODEL_CONFIGURATION_VIEW_COLUMNS, true, false);
         cols.add(3, colDefaultValue);
      }

      modelVariablesColumnModel = new DefaultColumnModel(cols, null, null, UserPreferencesEntries.M_ADMIN,
            ResourcePaths.V_configurationVariablesView);

      modelVariablesColumnModel.initialize();
   }

   public boolean isContainPreferences()
   {
      return (preferencesList != null) && !preferencesList.isEmpty();
   }

   public boolean isHideDefaultValue()
   {
      return hideDefaultValue;
   }

   public boolean isShowActiveModel()
   {
      return showActiveModel;
   }

   public boolean isValueChanged()
   {
      return valueChanged || !modifiedUserObjects.isEmpty();
   }

   public void saveAndCloseConfigurationValue()
   {
      saveConfigurationValue();
   }

   public  List<ModelReconfigurationInfo> saveConfigurationValue()
   {
      List<ModelReconfigurationInfo> infoList =null;
      // using a set for modified ConfigurationVariables to minimize kernel call for
      // update.
      Set<ConfigurationVariables> modifiedCofvars = new HashSet<ConfigurationVariables>();
      AdministrationService administrationService = workflowFacade.getServiceFactory().getAdministrationService();
      if (!modifiedUserObjects.isEmpty())
      {

         for (ModelConfigurationUserObject userObject : modifiedUserObjects)
         {
            if (userObject.getSource() instanceof ConfigurationVariable)
            {
               ConfigurationVariables variables = (ConfigurationVariables) userObject.getParent();
               modifiedCofvars.add(variables);
            }

            userObject.setEdited(false);
         }

         // now fires saveConfigurationVariables for unique ConfigurationVariables
         // objects.
         try
         {
            if (isHideDefaultValue())// CASE - 1
            {
               for (ConfigurationVariables allVars : allConfigurationVariables)
               {
                  for (ConfigurationVariables editedvars : modifiedCofvars)
                  {
                     if (allVars.getModelId().equals(editedvars))
                     {
                        for (ConfigurationVariable allvar : editedvars.getConfigurationVariables())
                        {
                           for (ConfigurationVariable editedVar : editedvars.getConfigurationVariables())
                           {
                              if (allvar.getName().equals(editedVar.getName()))
                              {
                                 allvar.setValue(editedVar.getValue());

                                 break;
                              }
                           }
                        }
                     }
                  }
                  // save all configuration and update edited configuration value
                  infoList=  administrationService.saveConfigurationVariables(allVars, false);
                  //modifiedUserObjects.clear();
               }
            }
            else
            // CASE - 2
            {
               List<ModelReconfigurationInfo> allInfoList = new ArrayList<ModelReconfigurationInfo>();

               for (ConfigurationVariables variables : modifiedCofvars)
               {
                  infoList = administrationService.saveConfigurationVariables(variables,
                        false);
                  allInfoList.addAll(infoList);
               }              

               ConfigurationValidationDialogBean dialog = ConfigurationValidationDialogBean.getCurrent();
               dialog.setDeploymentInfoList(allInfoList);
               dialog.setICallbackHandler(this);
               dialog.openPopup();
               
               infoList= allInfoList;
               valueChanged = false;      
            }
         }
         catch (Exception e)
         {
            ExceptionHandler.handleException(e);
         }
      }
//      else if (isHideDefaultValue())// if none of variables is modified then save all
//      // variables (as it is)
//      {
//         try
//         {
//            for (ConfigurationVariables allVars : allConfigurationVariables)
//            {
//               infoList= administrationService.saveConfigurationVariables(allVars, true);
//               modifiedUserObjects.clear();
//            }
//         }
//         catch (Exception e)
//         {
//            ExceptionHandler.handleException(e);
//         }
//         
//      }

     
      //valueChanged = false;      
      return infoList;
   }

   public void setAllConfigurationVariables(List<ConfigurationVariables> allConfigurationVariables)
   {
      this.allConfigurationVariables = allConfigurationVariables;
   }

   public void setFileResource(Resource fileResource)
   {
      this.fileResource = fileResource;
   }

   public void setHideDefaultValue(boolean hideDefaultValue)
   {
      this.hideDefaultValue = hideDefaultValue;
   }

   public void setSelectedNodeLabel(String label)
   {}

   public void setSelectedNodeObject(NodeUserObject nodeObject)
   {}

   public void setShowActiveModel(boolean showActiveModel)
   {
      this.showActiveModel = showActiveModel;
   }

   public void setValueChanged(boolean valueChanged)
   {
      this.valueChanged = valueChanged;
   }

   public void toggleVersion(ActionEvent event)
   {
      showActiveModel = !showActiveModel;

      initialize();
   }
   
   public ConfirmationDialog getConfigVariableConfirmationDialog()
   {
      return configVariableConfirmationDialog;
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
      ModelConfigurationUserObject userObject = (ModelConfigurationUserObject) event.getComponent().getAttributes()
            .get("row");
      userObject.setValue(texto);
      userObject.setEdited(true);
      modifiedUserObjects.add(userObject);
   }

   private void initActiveVersions()
   {
      List<DeployedModel> activeModels = ModelCache.findModelCache().getActiveModels();
      activeVersions = new HashSet<Integer>();

      for (Model model : activeModels)
      {
         activeVersions.add(model.getModelOID());
      }
   }

   private List<Preferences> loadPreferences()throws Exception
   {
      PreferenceQuery preferenceQuery = PreferenceQuery.findPreferences(PreferenceScope.PARTITION,
            ConfigurationVariableUtils.CONFIGURATION_VARIABLES, "*");

      return serviceFactory.getQueryService().getAllPreferences(preferenceQuery);
   }

   /**
    * Confirmation dialog to save details on View close.
    */
   private void openSaveConfiguration()
   {
      String title = this.getMessages().getString("confirmSaveConfiguration.title");
      configVariableConfirmationDialog = new ConfirmationDialog(DialogContentType.NONE, DialogActionType.YES_NO, null,
            DialogStyle.COMPACT, this);
      configVariableConfirmationDialog.setTitle(title);
      configVariableConfirmationDialog.setMessage(this.getMessages().getString("confirmSaveConfiguration.info"));
      configVariableConfirmationDialog.setFromView(currentView);
      configVariableConfirmationDialog.openPopup();
   }

   /**
    * 
    */
   public boolean accept()
   {
      configVariableConfirmationDialog = null;
      saveAndCloseConfigurationValue();
      return true;
   }

   /**
    * 
    */
   public boolean cancel()
   {
      configVariableConfirmationDialog = null;
      closeView();
      return true;
   }
}
