/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.rest.dto;

import java.util.ArrayList;
import java.util.List;

public class GanttChartDTO extends AbstractDTO
{
   public String name;
   public long oid;
   public long startTime; 
   public long endTime;
   public boolean activatable; 
   public Object status;
   public String category;
   public String type;
   public boolean auxillary;
   public List<GanttChartDTO> children;
   public String qualifiedId;
   public BenchmarkDTO benchmark;
 
   /**
    */
   public GanttChartDTO(ProcessInstanceDTO process)
   {
      super();
      this.name = process.processName;
      this.oid = process.oid;
      this.startTime = process.startTime;
      this.endTime = process.endTime != null ?  process.endTime : 0;
      this.activatable = false;
      this.status = process.status;
      this.type = "process";
      this.auxillary = process.auxillary;
      this.qualifiedId = process.qualifiedId;
      this.benchmark = process.benchmark;
   }
   
   
   public GanttChartDTO(ActivityInstanceDTO activityInstanceDTO)
   {
      super();
      this.name = activityInstanceDTO.activity.name;
      this.oid = activityInstanceDTO.activityOID;
      this.startTime = activityInstanceDTO.startTime;
      this.endTime = activityInstanceDTO.lastModification;
      this.activatable = activityInstanceDTO.activatable;
      this.status = activityInstanceDTO.status;
      this.type = "activity";
      this.auxillary = activityInstanceDTO.auxillary;
      this.qualifiedId = activityInstanceDTO.activity.qualifiedId;
      this.benchmark = activityInstanceDTO.benchmark;
   }


   /**
    */
   public void addChildren(GanttChartDTO chartInfo)
   {
     if(this.children == null) 
     {
        this.children = new ArrayList<GanttChartDTO>();
     } 
     this.children.add(chartInfo);
   }

}
