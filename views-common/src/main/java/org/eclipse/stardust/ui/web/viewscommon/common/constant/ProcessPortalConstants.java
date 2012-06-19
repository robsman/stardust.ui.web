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
package org.eclipse.stardust.ui.web.viewscommon.common.constant;

/**
 * @author rsauer
 * @version $Revision$
 */
public interface ProcessPortalConstants
{

   String PROCESSPORTAL_PREFIX = "Carnot.ProcessPortal.";
   String ASSEMBLY_LINE_MODE_ENABLED = "Carnot.BpmClient.Features.AssemblyLineTaskAssignment";
   String ASSEMBLY_LINE_PUSH_SERVICE = "Carnot.BpmClient.Features.AssemblyLinePushService";   

   String DESCRIPTOR_FILTER_PREFIX = PROCESSPORTAL_PREFIX + "DescriptorFilter.";
  // String CONSIDER_ONLY_ACTIVE_MODEL = DESCRIPTOR_FILTER_PREFIX + "ConsiderOnlyActiveModel";
  // String FILTER_COLUMN_DESCRIPTOR_IDS = DESCRIPTOR_FILTER_PREFIX + "ColumnDescriptorIds";
   
   public final static String WORKFLOW_FACADE = "carnot/processWorkflowFacade";
   //public final static String WORKLIST_TREE = "carnot/worklistTree";
   
   public final static String BOOLEAN_TYPE = "Boolean";
   public final static String STRING_TYPE = "String";
   public final static String TIMESTAMP_TYPE = "TimeStamp";
   public final static String LONG_TYPE = "Long";
   public final static String DOUBLE_TYPE = "Double";
   public final static String STRUCTURED_TYPE = "Structured";
   public final static String PRIORITY_TYPE = "Priority";
   public final static String TIME_TYPE = "Time";
   public final static String DATE_TYPE = "date";

   public static final String XSD_TIME_TYPE_NAME = "time";
}
