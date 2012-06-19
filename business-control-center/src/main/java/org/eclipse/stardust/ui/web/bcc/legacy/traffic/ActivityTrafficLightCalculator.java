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

/**
 * Interface to control the display of a traffic light icon that represents the business state
 * or <i>criticality</i> of a process definition concerning a particular activity.
 */
public interface ActivityTrafficLightCalculator {

	public int NEUTRAL = -1;

	public int NORMAL = 0;

	public int WARN = 1;

	public int CRITICAL = 2;

	/**
	 * Returns a status for a traffic light on the basis of a process definition, activity,
	 * number of completed activity instances, number of not completed activity instances, and
	 * a category.
	 * 
	 * @param processDefintionId
	 * @param activityId
	 * @param categoryId the category selected in the view (corresponds to a data ID).
	 * 		The parameter contains the Sring "total" if the 'Total' row was selected.
	 * @param categoryValue the individual value of the selected category.
	 * 		The parameter contains the Sring "total" if the 'Total' row was selected.
	 * @param numberOfNotCompletedActivities the number of process instances that have not
	 * 		yet completed the given activity.
	 * @param numberOfCompletedActivities the number of process instances that have already
	 * 		completed the given activity.
	 * @return Code that represents a specific state ({@link #NEUTRAL}, {@link #NORMAL},
	 * 		{@link #WARN}, {@link #CRITICAL}).
	 */
	public int getColorStateForActivity(String processDefintionId,
			String activityId, String categoryId, String categoryValue, 
			int numberOfNotCompletedActivities,	int numberOfCompletedActivities);

}
