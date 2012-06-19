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
package org.eclipse.stardust.ui.web.bcc.jsf;

public interface IThresholdProvider
{
   /** State for hidden traffic light 
   **/
   final static int UNDEFINED_THRESHOLD_STATE = -1;
   /** State for red traffic light 
   **/
   final static int CRITICAL_THRESHOLD_STATE = 1;
   /** State for yellow traffic light 
   **/
   final static int WARNING_THRESHOLD_STATE = 2;
   /** State for green traffic light 
   **/
   final static int NO_THRESHOLD_STATE = 3;
   
   /** Gets the threshold state of a given process definition.
    *  This method is called in the priority overview table.
    *  @param pdwp Process definition with all priorities
    *  @return one of the state
    *  @see UNDEFINED_THRESHOLD_STATE, CRITICAL_THRESHOLD_STATE, 
    *        WARNING_THRESHOLD_STATE, NO_THRESHOLD_STATE
   **/
   int getProcessThreshold(ProcessDefinitionWithPrio pdwp);
   
   /** Gets the threshold state of a given activity.
    *  This method is called in the priority overview table.
    *  @param adwp Activity with all priorities
    *  @return one of the state
    *  @see UNDEFINED_THRESHOLD_STATE, CRITICAL_THRESHOLD_STATE, 
    *        WARNING_THRESHOLD_STATE, NO_THRESHOLD_STATE
   **/
   int getActivityThreshold(ActivityDefinitionWithPrio adwp);
}