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
package org.eclipse.stardust.ui.web.bcc.legacy.gantt;

import java.util.Comparator;

public class ModelTreeComparator implements Comparator<ModelTreeItem> {

    
	/**
	 * Compares two objects of type <code>ModelTreeItem</code>.
	 */
	public int compare(ModelTreeItem first, ModelTreeItem second) {

		// TODO XXX implement a more sophisticated sorting mechanism 
		
	   if(first.getRoot().getPlannedStartTime()!=null && second.getRoot().getPlannedStartTime()!=null )
	   {
		long timestampFirst = first.getRoot().getPlannedStartTime().getTime();
		long timestampSecond = second.getRoot().getPlannedStartTime().getTime();

		int comparison = new Long(timestampFirst).compareTo(new Long(
				timestampSecond));

		if (comparison == 0) {
			comparison = new Long(((ModelTreeItem)first).getRoot().getElementOid())
					.compareTo(new Long(((ModelTreeItem)second).getRoot().getElementOid()));
		}
		
		if (comparison == 0)
		{
			comparison = ((ModelTreeItem)first).getRoot().getBusinessId().compareTo(
			      ((ModelTreeItem)second).getRoot().getBusinessId());
		}

		return comparison;
	   }
	   return 0;
	}
}
