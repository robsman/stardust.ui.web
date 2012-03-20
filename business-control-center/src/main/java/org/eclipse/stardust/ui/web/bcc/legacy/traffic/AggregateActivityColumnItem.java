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
package org.eclipse.stardust.ui.web.bcc.legacy.traffic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.event.ActionEvent;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;


/**
 * An instance of this class represents a column of a data table and belongs to one
 * specific row.
 * 
 * @author mueller1
 * 
 */
public class AggregateActivityColumnItem implements IColumnItem
{

   private static final String DEFAULT_STATE_CALCULATOR = "org.eclipse.stardust.ui.web.bcc.legacy.traffic.TerminationTimeBasedCalculator";

   private final static String TRAFFIC_LIGHT_GREEN = "/plugins/views-common/images/icons/status.png";

   private final static String TRAFFIC_LIGHT_AMBER = "/plugins/views-common/images/icons/status-away.png";

   private final static String TRAFFIC_LIGHT_RED = "/plugins/views-common/images/icons/status-busy.png";

   private final static String TRAFFIC_LIGHT_GREY = "/plugins/views-common/images/icons/status-offline.png";

   private final static String TRAFFIC_LIGHT_COMPLETED = "/plugins/views-common/images/icons/tick.png";

   private final static String TRAFFIC_LIGHT_COMPLETED_TRANS = "/plugins/views-common/images/icons/traffic_light_complete_trans.gif";

   //public final static String PROCESSES_PASSED_AI = "aggregateActivityColumnItem/processesPassedAI";

   //public final static String PROCESSES_NOT_PASSED_AI = "aggregateActivityColumnItem/processesNotPassedAI";

   //public static final String SELECTED_ACTIVITY = "aggregateActivityColumnItem/selectedActivity";

   private String id;
   
   private String qualifiedId;

   private String name;

   private String symbolName;

   private String symbolUrl;

   private int completed = 0;

   private IRowItem rowItem;

   private List/* <ActivityInstance> */aInstances = new ArrayList();

   public AggregateActivityColumnItem(String id,String qualifiedId, String name, IRowItem rowItem)
   {
      this.id = id;
	  this.qualifiedId = qualifiedId;
      this.name = name;
      this.rowItem = rowItem;
   }

   public void addCompletedActivity()
   {
      this.completed = this.completed + 1;
   }

   public String getId()
   {
      return this.id;
   }
   
   
	public String getQualifiedId() {
		return qualifiedId;
	}



public String getName()
   {
      return this.name;
   }

   public String getSymbolName()
   {

      this.symbolName = completed + "/" + this.rowItem.getTotalCount();

      return symbolName;
   }

   public int getCompleted()
   {
      return completed;
   }

   public void setCompleted(int completed)
   {
      this.completed = completed;
   }

   /**
    * The method calculates the color coded status of the underlying table cell. The logic
    * which determines the state is externalized into an implementation of the interface
    * ActivityTrafficLightCalculator. The implementation class can be determined in the
    * configuration file by specifying the property
    * 
    * org.eclipse.stardust.ui.web.bcc.legacy.traffic.TerminationTimeBasedCalculator
    * 
    */
   public void calculateColumnState(String processId, String categoryId,
         String categoryValue)
   {

      String className = TrafficLightViewPropertyProvider.getInstance()
            .getStateCalculatorClassName(processId);
      className = StringUtils.isEmpty(className) ? DEFAULT_STATE_CALCULATOR : className;

      ActivityTrafficLightCalculator calculator = (ActivityTrafficLightCalculator) Reflect
            .createInstance(className);

      int state = calculator.getColorStateForActivity(processId, this.id, categoryId,
            categoryValue, this.rowItem.getTotalCount().intValue() - completed, this.completed);

      switch (state)
      {
      case 0:
         this.symbolUrl = TRAFFIC_LIGHT_GREEN;
         break;

      case 1:
         this.symbolUrl = TRAFFIC_LIGHT_AMBER;
         break;

      case 2:
         this.symbolUrl = TRAFFIC_LIGHT_RED;
         break;

      default:
         this.symbolUrl = TRAFFIC_LIGHT_GREY;
         break;
      }

   }

   public String getSymbolUrl()
   {
      symbolUrl = symbolUrl == null ? TRAFFIC_LIGHT_GREY : symbolUrl;
      return symbolUrl;
   }

   public String getCompletedIcon()
   {
      String completedIcon = null;

      if (this.rowItem.getTotalCount().longValue() == this.completed
            && this.rowItem.getTotalCount().longValue() != 0)
      {
         completedIcon = TRAFFIC_LIGHT_COMPLETED;
      }
      else
      {
         completedIcon = TRAFFIC_LIGHT_COMPLETED_TRANS;
      }

      return completedIcon;
   }

   public void showProcessTables(ActionEvent event)
   {
     
      Map activePIs = getActivePIs();
      List processesPassedAI = new ArrayList();
      List processesNotPassedAI = new ArrayList(rowItem.getActivePIs());
      for (Iterator iterator = aInstances.iterator(); iterator.hasNext();)
      {
         ActivityInstance aInstance = (ActivityInstance) iterator.next();
         ProcessInstance pi = (ProcessInstance) activePIs.get(new Long(aInstance
               .getProcessInstanceOID()));
         processesPassedAI.add(pi);
         processesNotPassedAI.remove(pi);
      }
      /* 
      SessionContext sessionCtx = SessionContext.findSessionContext();
      sessionCtx.bind(AggregateActivityColumnItem.PROCESSES_PASSED_AI, null);
      sessionCtx.bind(AggregateActivityColumnItem.PROCESSES_PASSED_AI,
            new ResetableDataModel(processesPassedAI));
      sessionCtx.bind(AggregateActivityColumnItem.PROCESSES_NOT_PASSED_AI, null);
      sessionCtx.bind(AggregateActivityColumnItem.PROCESSES_NOT_PASSED_AI,
            new ResetableDataModel(processesNotPassedAI));
      sessionCtx.bind(AggregateActivityColumnItem.SELECTED_ACTIVITY, name);*/
   }

   private Map getActivePIs()
   {
      Map activePIs = new HashMap();
      for (Iterator iterator = rowItem.getActivePIs().iterator(); iterator.hasNext();)
      {
         ProcessInstance pi = (ProcessInstance) iterator.next();
         activePIs.put(new Long(pi.getOID()), pi);
      }
      return activePIs;
   }

   public void addActivityInstance(ActivityInstance aInstance)
   {
      this.aInstances.add(aInstance);
   }

   public boolean isActivePIs()
   {
      return rowItem.getActivePIs() != null && (!rowItem.getActivePIs().isEmpty());
   }
}
