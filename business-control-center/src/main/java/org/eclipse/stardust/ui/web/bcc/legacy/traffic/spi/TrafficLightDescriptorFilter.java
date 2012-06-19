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
package org.eclipse.stardust.ui.web.bcc.legacy.traffic.spi;

import java.io.Serializable;

import org.eclipse.stardust.engine.api.model.DataPath;


public interface TrafficLightDescriptorFilter extends Serializable
{
   /**
    * Returns all descriptors that should be displayed in columns in the process instance
    * tables in the Traffic Light View of Business Control Center. The descriptor columns
    * will be displayed in the same order as the descriptors returned from the array.
    * 
    * @param processId
    *            The id of the process definition.
    * 
    * @return An array of <code>DataPath</code> instances.
    */
   DataPath[] getDescriptors(String processId);
}
