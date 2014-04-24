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
package org.eclipse.stardust.ui.web.admin.views.model.dialog;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.tree.DefaultTreeModel;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.config.ExtensionProviderUtils;
import org.eclipse.stardust.common.error.ErrorCase;
import org.eclipse.stardust.common.error.IErrorMessageProvider;
import org.eclipse.stardust.common.error.IErrorMessageProvider.Factory;
import org.eclipse.stardust.engine.api.dto.DeploymentInfoDetails;
import org.eclipse.stardust.engine.api.model.Inconsistency;
import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.engine.api.runtime.Daemon;
import org.eclipse.stardust.engine.api.runtime.DeploymentElement;
import org.eclipse.stardust.engine.api.runtime.DeploymentException;
import org.eclipse.stardust.engine.api.runtime.DeploymentInfo;
import org.eclipse.stardust.engine.core.preferences.configurationvariables.ConfigurationVariables;
import org.eclipse.stardust.ui.web.admin.ResourcePaths;
import org.eclipse.stardust.ui.web.admin.common.configuration.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.admin.messages.AdminMessagesPropertiesBean;
import org.eclipse.stardust.ui.web.admin.views.TreeNodeFactory;
import org.eclipse.stardust.ui.web.admin.views.model.ModelManagementUtil;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnAlignment;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnDataType;
import org.eclipse.stardust.ui.web.common.column.DefaultColumnModel;
import org.eclipse.stardust.ui.web.common.column.IColumnModel;
import org.eclipse.stardust.ui.web.common.event.ViewEvent;
import org.eclipse.stardust.ui.web.common.table.SortableTable;
import org.eclipse.stardust.ui.web.common.table.SortableTableComparator;
import org.eclipse.stardust.ui.web.common.treetable.NodeUserObject;
import org.eclipse.stardust.ui.web.common.treetable.TreeTable;
import org.eclipse.stardust.ui.web.common.treetable.TreeTableBean;
import org.eclipse.stardust.ui.web.common.treetable.TreeTableNode;
import org.eclipse.stardust.ui.web.common.util.FileUtils;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentMgmtUtility;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;
import org.eclipse.stardust.ui.web.viewscommon.wizard.WizardPage;
import org.eclipse.stardust.ui.web.viewscommon.wizard.WizardPageEvent;



/**
 *
 * @author Vikas.Mishra
 * @version $Revision: $
 */
public class ModelDeploymentStatusPage extends WizardPage implements TreeTableBean
{
   private AdminMessagesPropertiesBean propsBean;
   private IColumnModel modelVariablesColumnModel;
   private List<DeploymentInfo> deploymentInfoList = null;
   private final SessionContext sessionContext;
   private List<ConfigurationVariables> allConfigurationVariables;
   private List<ConfigurationVariables> configurationVariablesEditList;
   private SortableTable<DeploymentStatusTableEntry> deploymentStatus;
   private TreeTable treeTable;
   private TreeTableNode rootModelNode = null;
   private boolean containsConfigurationValues = false;
   private boolean containsErrors = false;
   private boolean containsWarnings = false;
   private ModelDeploymentPage deploymentPage;
   private boolean modelDeployed=false;
   private List<DeploymentElement> deployList;
   private boolean deploymentException;
   private ArrayList<Factory> translators;


   public ModelDeploymentStatusPage()
   {
      super("STATUS_PAGE", "/plugins/admin-portal/views/model/_modelDeployStatusPage.xhtml");
      propsBean = AdminMessagesPropertiesBean.getInstance();
      sessionContext = SessionContext.findSessionContext();
      translators = new ArrayList<IErrorMessageProvider.Factory>(ExtensionProviderUtils
            .getExtensionProviders(IErrorMessageProvider.Factory.class));
      createTreeTable();
      createStatusTable();
      initialize();
   }

   public boolean isModelDeployed()
   {
      return modelDeployed;
   }

   public List<ConfigurationVariables> getAllConfigurationVariables()
   {
      return allConfigurationVariables;
   }

   public List<ConfigurationVariables> getConfigurationVariablesEditList()
   {
      return configurationVariablesEditList;
   }

   public List<DeploymentInfo> getDeploymentInfoList()
   {
      return deploymentInfoList;
   }

   public SortableTable<DeploymentStatusTableEntry> getDeploymentStatus()
   {
      return deploymentStatus;
   }

   public int getIncreasedLabelCount()
   {
      return 0;
   }

   public TreeTableNode getRootModelNode()
   {
      return rootModelNode;
   }

   @Override
   public String getTitle()
   {
      return propsBean.getString("views.deploymodel.title.deployContinue");
   }

   public TreeTable getTreeTable()
   {
      return treeTable;
   }

   public void handleEvent(ViewEvent event)
   {}

   public void handleEvent(WizardPageEvent event)
   {
      if (event.getType().equals(WizardPageEvent.WizardPageEventType.PAGE_ACTIVATE))
      {
         if ((deploymentInfoList == null) && event.getFlowEvent().getOldPage() instanceof ModelDeploymentPage)
         {
            deploymentPage = (ModelDeploymentPage) event.getFlowEvent().getOldPage();
            deploymentStatus.setList(getDeploymentStatusTableData());
            deploymentStatus.initialize();

         }
      }else if (!modelDeployed && event.getType().equals(WizardPageEvent.WizardPageEventType.PAGE_ONLOAD))
      {
         modelDeployed=true;
         try
         {
            deploymentInfoList = deployModels();
            processConfigurationVariables(deployList);
         }
         catch (Exception e)
         {
            deploymentException = true;

            DeploymentInfoDetails infoDetail = new DeploymentInfoDetails(null, null,
                  ExceptionHandler.getExceptionMessage(e));
            Inconsistency inconsistency = new Inconsistency(e.getLocalizedMessage(),
                  null, Inconsistency.ERROR);
            infoDetail.addInconsistency(inconsistency);
            if (CollectionUtils.isEmpty(deploymentInfoList))
            {
               deploymentInfoList = new ArrayList<DeploymentInfo>(1);
            }

            deploymentInfoList.add(infoDetail);

            DeploymentStatusTableEntry entry = new DeploymentStatusTableEntry();
            entry.setErrors(1);
            entry.setCause(e);
            entry.setComplete(true);

            List<DeploymentStatusTableEntry> list=new ArrayList<DeploymentStatusTableEntry>(1);
            list.add(entry);

            deploymentStatus.setList(list);
            deploymentStatus.initialize();
         }

         initialize();
      }
   }

   /**
        *
        */
   public void initialize()
   {
      if (deploymentInfoList != null)
      {
         deploymentStatus.setList(getDeploymentStatusTableData());
         deploymentStatus.initialize();

         // create root node
         ErrorWarningTreeItem rootItem = new ErrorWarningTreeItem();
         rootItem.setMessage("");
         rootModelNode = TreeNodeFactory.createTreeNode(treeTable, this, rootItem, true);

         // Now Create a Model & Tree Table
         DefaultTreeModel rootNode = new DefaultTreeModel(rootModelNode);
         treeTable = new TreeTable(rootNode, modelVariablesColumnModel, null);
         treeTable.setAutoFilter(false);
         treeTable.setFilterRootNode(true);
         rootModelNode.getUserObject().setTreeTable(treeTable);

         List<ErrorWarningTreeItem> treeItems = getErrorWarningTreeTableData();

         for (ErrorWarningTreeItem item : treeItems)
         {
            buildTableTree(rootModelNode, item);
         }

         // Build Tree
         treeTable.initialize();
         // Rebuild the Tree
         treeTable.rebuildList();
      }
   }

   public boolean isContainsConfigurationValues()
   {
      return containsConfigurationValues;
   }

   public boolean isContainsErrors()
   {
      return containsErrors;
   }

   public boolean isContainsWarnings()
   {
      return containsWarnings;
   }

   public void setAllConfigurationVariables(List<ConfigurationVariables> allConfigurationVariables)
   {
      this.allConfigurationVariables = allConfigurationVariables;
   }

   public void setContainsConfigurationValues(boolean containsConfigurationValues)
   {
      this.containsConfigurationValues = containsConfigurationValues;
   }

   public void setDeploymentInfoList(List<DeploymentInfo> deploymentInfoList)
   {
      this.deploymentInfoList = deploymentInfoList;
   }

   public void setSelectedNodeLabel(String label)
   {}

   public void setSelectedNodeObject(NodeUserObject nodeObject)
   {}

   public void setTreeTable(TreeTable treeTable)
   {
      this.treeTable = treeTable;
   }

   private void buildTableTree(TreeTableNode parentNode, ErrorWarningTreeItem treeItem)
   {
      TreeTableNode treeNode = TreeNodeFactory.createTreeNode(treeTable, this, treeItem, true);
      parentNode.add(treeNode);

      for (ErrorWarningTreeItem item : treeItem.getChildrens())
      {
         buildTableTree(treeNode, item);
      }
   }

   /**
    * method to create table definition for model file
    */
   private void createStatusTable()
   {
      List<ColumnPreference> cols = new ArrayList<ColumnPreference>();

      ColumnPreference colProgress = new ColumnPreference("DeploymentProgress", null, propsBean
            .getString("views.deploymodel.deploymentStatus.column.deploymentProgress"),
            ResourcePaths.V_DEPLOYMENT_STATUS_COLUMNS, true, false);

      colProgress.setColumnAlignment(ColumnAlignment.CENTER);

      ColumnPreference colStatus = new ColumnPreference("Status", null, propsBean
            .getString("views.deploymodel.deploymentStatus.column.status"), ResourcePaths.V_DEPLOYMENT_STATUS_COLUMNS,
            true, false);

      colStatus.setColumnAlignment(ColumnAlignment.CENTER);

      ColumnPreference colErrors = new ColumnPreference("Errors", "errors",  propsBean
            .getString("views.deploymodel.deploymentStatus.column.errors"), ResourcePaths.V_DEPLOYMENT_STATUS_COLUMNS, true, false);
      colErrors.setColumnAlignment(ColumnAlignment.CENTER);

      ColumnPreference colWarnings = new ColumnPreference("Warnings", "warnings", propsBean
            .getString("views.deploymodel.deploymentStatus.column.warnings"), ResourcePaths.V_DEPLOYMENT_STATUS_COLUMNS, true, false);

      colWarnings.setColumnAlignment(ColumnAlignment.CENTER);

      cols.add(colStatus);
      cols.add(colErrors);
      cols.add(colWarnings);

      List<ColumnPreference> fixedBeforeColumns = new ArrayList<ColumnPreference>();
      List<ColumnPreference> fixedAfterColumns = new ArrayList<ColumnPreference>();

      fixedBeforeColumns.add(colProgress);

      IColumnModel deploymentStatusColumnModel = new DefaultColumnModel(cols, fixedBeforeColumns, fixedAfterColumns,
            UserPreferencesEntries.M_ADMIN, ResourcePaths.V_modelManagementView);

      deploymentStatus = new SortableTable<DeploymentStatusTableEntry>(deploymentStatusColumnModel, null,
            new SortableTableComparator<DeploymentStatusTableEntry>("errors", true));
      deploymentStatus.initialize();
   }

   private ErrorWarningTreeItem createTreeItem(Inconsistency inconsistency, String modelId)
   {
      String message = "";
      ErrorWarningTreeItem childItem = new ErrorWarningTreeItem();
      childItem.setElement(inconsistency.getSourceElementName());
      if (inconsistency.getError() != null)
      {
         message = getMessageFromErrorCase(inconsistency.getError());
      }
      else
      {
         message = inconsistency.getMessage();
      }
      childItem.setMessage(message);
      childItem.setElementId(inconsistency.getSourceElementId());
      childItem.setModelId(modelId);

      if (inconsistency.getSeverity() == Inconsistency.ERROR)
      {
         childItem.setType(ErrorWarningTreeItem.Type.ERROR);
      }
      else
      {
         childItem.setType(ErrorWarningTreeItem.Type.WARNING);
      }

      return childItem;
   }

   private String getMessageFromErrorCase(ErrorCase errorCase)
   {
      Iterator<IErrorMessageProvider.Factory> tIter = translators.iterator();

      while (tIter.hasNext())
      {
         IErrorMessageProvider.Factory msgFactory = (IErrorMessageProvider.Factory) tIter.next();
         IErrorMessageProvider msgProvider = msgFactory.getProvider(errorCase);
         if (msgProvider != null)
         {
            return msgProvider.getErrorMessage(
                  errorCase,
                  null,
                  org.eclipse.stardust.ui.web.common.util.FacesUtils.getLocaleFromRequest());
         }
      }

      return null;
   }

   /**
    * method define tree table structure
    */
   private void createTreeTable()
   {
      List<ColumnPreference> cols = new ArrayList<ColumnPreference>();

      ColumnPreference colMessage = new ColumnPreference("Message", "messageDetail", ColumnDataType.STRING, propsBean
            .getString("views.deploymodel.deploymentStatus.errorwarning.table.column.message"));

      ColumnPreference colModel = new ColumnPreference("Model", "modelId", ColumnDataType.STRING, propsBean
            .getString("views.deploymodel.deploymentStatus.errorwarning.table.column.model"));

      ColumnPreference colElement = new ColumnPreference("Element", "element", ColumnDataType.STRING, propsBean
            .getString("views.deploymodel.deploymentStatus.errorwarning.table.column.element"));

      cols.add(colMessage);
      cols.add(colModel);
      cols.add(colElement);

      modelVariablesColumnModel = new DefaultColumnModel(cols, null, null, UserPreferencesEntries.M_ADMIN,
            ResourcePaths.V_configurationVariablesView);

      modelVariablesColumnModel.initialize();
   }

   private List<DeploymentStatusTableEntry> getDeploymentStatusTableData()
   {
      List<DeploymentStatusTableEntry> list = new ArrayList<DeploymentStatusTableEntry>();
      DeploymentStatusTableEntry entry = getDeploymentStatusTableEntry();
      list.add(entry);

      return list;
   }

   private DeploymentStatusTableEntry getDeploymentStatusTableEntry()
   {
      DeploymentStatusTableEntry entry = new DeploymentStatusTableEntry();
      if (deploymentInfoList != null)
      {
         for (DeploymentInfo info : deploymentInfoList)
         {
            if (info.hasErrors())
            {
               int errorCount = entry.getErrors() + info.getErrors().size();
               entry.setErrors(errorCount);
            }

            if (info.hasWarnings())
            {
               int warningCount = entry.getWarnings() + info.getWarnings().size();
               entry.setWarnings(warningCount);
            }
         }
      }

      // if contain error(s) then deployment is incomplete otherwise successfully deployed
      entry.setComplete(!entry.hasErrors());
      containsErrors = entry.hasErrors();
      containsWarnings = (entry.getWarnings() > 0) ? true : false;

      return entry;
   }

   /**
    * Mock data
    *
    * @return
    */
   private List<ErrorWarningTreeItem> getErrorWarningTreeTableData()
   {
      List<ErrorWarningTreeItem> treeItems = new ArrayList<ErrorWarningTreeItem>();
      ErrorWarningTreeItem errorItem = new ErrorWarningTreeItem();
      errorItem.setType(ErrorWarningTreeItem.Type.ERROR);

      ErrorWarningTreeItem warningItem = new ErrorWarningTreeItem();
      warningItem.setType(ErrorWarningTreeItem.Type.WARNING);

      for (DeploymentInfo info : deploymentInfoList)
      {
         // add errors
         for (Inconsistency inconsistency : info.getWarnings())
         {
            ErrorWarningTreeItem childItem = createTreeItem(inconsistency, info.getId());
            warningItem.getChildrens().add(childItem);
         }

         for (Inconsistency inconsistency : info.getErrors())
         {
            ErrorWarningTreeItem childItem = createTreeItem(inconsistency, info.getId());
            errorItem.getChildrens().add(childItem);
         }
      }

      if (!errorItem.getChildrens().isEmpty())
      {
         errorItem.setMessage(propsBean.getParamString("views.deploymodel.deploymentStatus.errorCount",
               new String[] {String.valueOf(errorItem.getChildrens().size())}));
         treeItems.add(errorItem);
      }

      if (!warningItem.getChildrens().isEmpty())
      {
         warningItem.setMessage(propsBean.getParamString("views.deploymodel.deploymentStatus.warningCount",
               new String[] {String.valueOf(warningItem.getChildrens().size())}));
         treeItems.add(warningItem);
      }

      return treeItems;
   }
   /**
    * method to get value for configuration variables by DeploymentElement object's.
    * @param deploymentElementList
    */

   private void processConfigurationVariables(List<DeploymentElement> deploymentElementList)
   {
      AdministrationService administrationService = sessionContext.getServiceFactory().getAdministrationService();
      allConfigurationVariables = new ArrayList<ConfigurationVariables>();

      try
      {
         for (DeploymentElement deploymentElement : deploymentElementList)
         {
            ConfigurationVariables variables = administrationService.getConfigurationVariables(deploymentElement
                  .getContent());
            allConfigurationVariables.add(variables);
         }

         // just to know ,we can move to next page we check Contains ConfigurationValues( for edit)
         // similar logic we execute again while rendering tree table for configuration variable page.
         configurationVariablesEditList = ModelManagementUtil
               .getEditableConfigurationVariables(allConfigurationVariables);
         setContainsConfigurationValues(!configurationVariablesEditList.isEmpty());
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }
   }

   /**
    * method to deploy models
    */
   public List<DeploymentInfo> deployModels() throws Exception
   {
      List<DeploymentInfo> list = new ArrayList<DeploymentInfo>(1);

      AdministrationService administrationService = sessionContext.getServiceFactory().getAdministrationService();

      if (sessionContext.isSessionInitialized())
      {
         Daemon[] stoppedDaemons = null;
         try
         {
            stoppedDaemons = ModelManagementUtil.stopDaemons(sessionContext);
            deployList = new ArrayList<DeploymentElement>();

            for (ModelDeployTableEntry deployTableEntry : deploymentPage.getModelList())
            {
               byte[] data = null;

               if (deployTableEntry.getFilePath().toLowerCase().endsWith(FileUtils.ZIP_FILE))
               {
                  data = FileUtils.fileBytesFromZip(deployTableEntry.getFileName(), deployTableEntry.getFilePath());
               }
               else if (deployTableEntry.getFilePath().startsWith("/process-models/"))
               {
                  data = DocumentMgmtUtility.getFileContent(DocumentMgmtUtility.getDocumentManagementService().getDocument(deployTableEntry.getFilePath()).getId());
               }
               else
               {
                  data = FileUtils.fileToBytes(deployTableEntry.getFilePath());
               }

               DeploymentElement DeploymentElement = new DeploymentElement(data);
               deployList.add(DeploymentElement);
            }

            if (deploymentPage.isOverwrite())
            {
               DeploymentElement element = deployList.get(0);
               DeploymentInfo info = null;

               try
               {
                  info = administrationService.overwriteModel(element,  deploymentPage.getModelOID(), deploymentPage.getDeploymentOptions());

                  // ...reset data model in order to refresh the model table...
                  ModelCache.findModelCache().reset();
               }
               catch (DeploymentException e)
               {
                  info = e.getDeploymentInfo();
                  if (!info.hasErrors())
                  {
                     throw e;
                  }
               }

               list.add(info);

            }
            else
            {
               try
               {
                  list = administrationService.deployModel(deployList, deploymentPage.getDeploymentOptions());

                  // ...reset data model in order to refresh the model table...
                  ModelCache.findModelCache().reset();
               }
               catch (DeploymentException e)
               {
                  DeploymentInfo info = e.getDeploymentInfo();
                  if (!info.hasErrors() && !info.hasWarnings())
                  {
                     DeploymentInfoDetails infoDetail = new DeploymentInfoDetails(info.getValidFrom(), info.getId(),
                           info.getDeploymentComment());
                     Inconsistency inconsistency = new Inconsistency(e.getLocalizedMessage(), null, Inconsistency.ERROR);
                     infoDetail.addInconsistency(inconsistency);
                     info = infoDetail;
                  }

                  list = new ArrayList<DeploymentInfo>(1);
                  list.add(info);
                  deploymentException = true;
               }
            }
         }

         finally
         {
            if (stoppedDaemons != null && stoppedDaemons.length > 0)
            {
               ModelManagementUtil.startDaemons(stoppedDaemons,sessionContext);
            }
         }

      }

      return list;
   }

   public boolean isDeploymentException()
   {
      return deploymentException;
   }

}
