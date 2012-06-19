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
package org.eclipse.stardust.ui.web.bcc.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.query.ProcessDefinitionFilter;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.engine.api.query.QueryResult;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.core.preferences.IPreferenceStorageManager;
import org.eclipse.stardust.ui.web.bcc.WorkflowFacade;
import org.eclipse.stardust.ui.web.bcc.jsf.InvalidServiceException;
import org.eclipse.stardust.ui.web.bcc.legacy.gantt.PropertyProvider;
import org.eclipse.stardust.ui.web.common.UIComponentBean;
import org.eclipse.stardust.ui.web.common.app.View;
import org.eclipse.stardust.ui.web.common.event.ViewEvent;
import org.eclipse.stardust.ui.web.common.event.ViewEventHandler;
import org.eclipse.stardust.ui.web.common.event.ViewEvent.ViewEventType;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;




/**
 * @author Ankita.Patel
 * @version $Revision: $
 */
public class GanttChartSelectionBean extends UIComponentBean implements ViewEventHandler
{
   private static final long serialVersionUID = 1L;
   public static final String ENABLED_PROCESSES = "GanttDiagramSelectionBean/processes";

   private Map processes;

   private SessionContext sessionCtx;

   private Long currentProcessOID;

   private String processId;
   
   private View thisView;
   private GanttChartManagerBean ganttChart;

   /**
    * @return
    */
   public static GanttChartSelectionBean getInstance()
   {
      return (GanttChartSelectionBean) FacesUtils
            .getBeanFromContext("ganttChartSelectionBean");
   }

   /**
    * 
    */
   public GanttChartSelectionBean()
   {
      
   }

   public void handleEvent(ViewEvent event)
   {
      if (ViewEventType.CREATED == event.getType())
      {
         this.thisView = event.getView();

         sessionCtx = SessionContext.findSessionContext();

         String pOID = thisView.getParamValue("processInstanceOId");
         if (!StringUtils.isEmpty(pOID))
            currentProcessOID = Long.parseLong(pOID);

         initialize();


      }
      
   }
   
   /**
    * Method stores process instance OID within the session context.
    */
   public void addProcessParameter()
   {

   }

   /**
    * ProcessInstanceQuery will be filtered by adding all necessary
    * ProcessDefinitionFilter. Which process definition must be considered can be
    * determined within the configuration file of the Gantt Diagram View.
    */
   public QueryResult performSearch(Query query)
   {
      List< ? > processDefinitions = PropertyProvider.getInstance()
            .getAllProcessDefinitionIDs();
      for (Iterator< ? > _iterator = processDefinitions.iterator(); _iterator.hasNext();)
      {
         String processDefintionId = (String) _iterator.next();
         query.getFilter().add(new ProcessDefinitionFilter(processDefintionId, false));
      }

      ServiceFactory sFactory = sessionCtx.getServiceFactory();
      QueryService qService = sFactory.getQueryService();

      try
      {
         return qService.getAllProcessInstances((ProcessInstanceQuery) query);
      }
      catch (Exception e)
      {
         return null;
      }
   }

   /**
    * @return
    */
   public Map getProcesses()
   {
      Object obj = sessionCtx.lookup(ENABLED_PROCESSES);
      if (obj == null)
      {
         initialize();
      }
      return processes;
   }

   /**
    * @return
    */
   public boolean isShowGanttColumnEnabled()
   {
      return processes.size() > 0;
   }

   @Override
   public void initialize()
   {
      if (currentProcessOID == null)
         return;

      processes = new HashMap();
      boolean useRepository = Parameters.instance().getBoolean(
            IPreferenceStorageManager.PRP_USE_DOCUMENT_REPOSITORY, false);

      List< ? > processDefinitions = new ArrayList();
      for (Iterator iterator = ModelCache.findModelCache().getAllModels().iterator(); iterator
            .hasNext();)
      {
         Model model = (Model) iterator.next();
         processDefinitions.addAll(model.getAllProcessDefinitions());
      }

      boolean isEnabledForAllProcesses = PropertyProvider.getInstance().hasConfigParam(
            PropertyProvider.ENABLE_DIAGRAM_FOR_ALL_PROCESSES_PROPERTY)
            ? PropertyProvider.getInstance().getBooleanProperty(
                  PropertyProvider.ENABLE_DIAGRAM_FOR_ALL_PROCESSES_PROPERTY)
            : true;

      isEnabledForAllProcesses = PropertyProvider.getInstance().hasConfigParam(
            PropertyProvider.DISABLE_DIAGRAM_FOR_ALL_PROCESSES_PROPERTY)
            ? PropertyProvider.getInstance().getBooleanProperty(
                  PropertyProvider.DISABLE_DIAGRAM_FOR_ALL_PROCESSES_PROPERTY)
                  ? false
                  : isEnabledForAllProcesses
            : isEnabledForAllProcesses;

      for (Iterator iterator = processDefinitions.iterator(); iterator.hasNext();)
      {
         ProcessDefinition pd = (ProcessDefinition) iterator.next();

         boolean isEnabled = true;
         if (!isEnabledForAllProcesses)
         {
            isEnabled = PropertyProvider.getInstance().hasConfigParam(pd.getId(),
                  PropertyProvider.ENABLE_DIAGRAM_PROPERTY) ? PropertyProvider
                  .getInstance().getBooleanProperty(pd.getId(),
                        PropertyProvider.ENABLE_DIAGRAM_PROPERTY) : false;
         }
         else if (isEnabledForAllProcesses)
         {
            isEnabled = PropertyProvider.getInstance().hasConfigParam(pd.getId(),
                  PropertyProvider.ENABLE_DIAGRAM_PROPERTY) ? PropertyProvider
                  .getInstance().getBooleanProperty(pd.getId(),
                        PropertyProvider.ENABLE_DIAGRAM_PROPERTY) : true;
         }

         if (isEnabled)
         {
            processes.put(pd.getId(), Boolean.TRUE);
         }
      }
      sessionCtx.bind(org.eclipse.stardust.ui.web.bcc.legacy.gantt.GanttChartBtnPropertyProvider.ENABLED_PROCESSES, processes);

      ProcessInstanceQuery query = ProcessInstanceQuery.findAll();
      ProcessInstance processInstance = null;
      
      query.getFilter().add(ProcessInstanceQuery.OID.isEqual(currentProcessOID));
      try
      {
         WorkflowFacade facade = WorkflowFacade.getWorkflowFacade();
         Iterator pIter = facade.getAllProcessInstances(query).iterator();
         if (pIter.hasNext())
         {
            processInstance = (ProcessInstance) pIter.next();
         }

         processId = processInstance.getProcessID();

      }
      catch (InvalidServiceException e)
      {
         // ignore
      }

      // if selected process Id is in list, creates GanttChart view
      if (processes.containsKey(processId))
      {
         //ganttChart = GanttChartManagerBean.getInstance();
         ganttChart = new GanttChartManagerBean();
         ganttChart.setCurrentProcessOID(currentProcessOID);
         ganttChart.createGanttChart();
         
      }

   }

   /**
    * @return
    */
   public String getProcessId()
   {
      return processId;
   }

   public final GanttChartManagerBean getGanttChart()
   {
      return ganttChart;
   }

   

}
