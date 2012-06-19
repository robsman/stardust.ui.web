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
package org.eclipse.stardust.ui.web.viewscommon.common.spi;

import java.io.Serializable;
import java.util.List;

import org.eclipse.stardust.engine.api.model.DataPath;


/**
 * Filters the descriptors to be displayed in the worklists of the Process Execution
 * Portal.
 * 
 * @author fuhrmann
 * @version $Revision$
 */
public interface IDescriptorFilter extends Serializable
{
   /**
    * Returns all descriptors that should be displayed below the name of the activity in
    * the worklist by processes.
    * 
    * @param processIds
    *           A list with ids of process definitions.
    * 
    * @return An array of <code>DataPath</code> instances.
    */
   DataPath[] getProcessWorklistDescriptors(List/* <String> */processIds);

   /**
    * Returns all descriptors that should be displayed below the name of the activity in
    * the worklist by role.
    * 
    * @param processIds
    *           A list with ids of process definitions.
    * 
    * @return An array of <code>DataPath</code> instances.
    */
   DataPath[] getRoleWorklistDescriptors(List/* <String> */processIds);

   /**
    * Returns all descriptors that should be displayed in columns in the worklist by
    * processes. The descriptor columns will be displayed in the same order as the
    * descriptors returned from the array.
    * 
    * @param processIds
    *           A list with ids of process definitions.
    * 
    * @return An array of <code>DataPath</code> instances.
    */
   DataPath[] getProcessWorklistDescriptorColumns(List/* <String> */processIds);

   /**
    * Returns all descriptors that should be displayed in columns in the worklist by role.
    * The descriptor columns will be displayed in the same order as the descriptors
    * returned from the array.
    * 
    * @param processIds
    *           A list with ids of process definitions.
    * 
    * @return An array of <code>DataPath</code> instances.
    */
   DataPath[] getRoleWorklistDescriptorColumns(List/* <String> */processIds);

}
