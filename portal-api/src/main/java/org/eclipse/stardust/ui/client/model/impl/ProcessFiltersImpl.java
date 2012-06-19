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
package org.eclipse.stardust.ui.client.model.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.stardust.engine.api.model.Activity;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.runtime.DeployedModelDescription;
import org.eclipse.stardust.ui.client.event.StatusEvent;
import org.eclipse.stardust.ui.client.model.Models;
import org.eclipse.stardust.ui.client.model.ProcessFilter;
import org.eclipse.stardust.ui.client.model.ProcessFilters;



public class ProcessFiltersImpl extends AbstractWorkflowCollection<ProcessFilter> implements ProcessFilters
{
   private ClientImpl client;

   public ProcessFiltersImpl(ClientImpl client)
   {
      this.client = client;
   }

   @Override
   public void update()
   {
      HashMap<String, ProcessFilter> filters = new HashMap<String, ProcessFilter>();
      Models models = client.getModels();
      models.update();
      Iterator<DeployedModelDescription> iterator = models.iterator();
      while (iterator.hasNext())
      {
         Model model = models.getModel(iterator.next().getModelOID());
         @SuppressWarnings("unchecked") List<ProcessDefinition> definitions = model.getAllProcessDefinitions();
         for (ProcessDefinition processDefinition : definitions)
         {
            @SuppressWarnings("unchecked") List<Activity> activities = processDefinition.getAllActivities();
            for (Activity activity : activities)
            {
               if (activity.isInteractive())
               {
                  String id = processDefinition.getId();
                  ProcessFilter filter = filters.get(id);
                  if (filter == null)
                  {
                     filter = new ProcessFilter(client, id);
                     filters.put(id, filter);
                  }
                  filter.add(processDefinition);
                  break;
               }
            }
         }
      }
      items = filters.values().toArray(new ProcessFilter[filters.size()]);
      observers.notifyObservers(StatusEvent.updated(this));
   }

   public ProcessFilter getFilter(String id)
   {
      if (items != null)
      {
         for (int i = 0; i < items.length; i++)
         {
            if (items[i].getId().equals(id))
            {
               return items[i];
            }
         }
      }
      return null;
   }
}
