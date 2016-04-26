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

/**
 * 
 * @author Johnson.Quadras
 *
 */
public class InstanceCountsDTO extends AbstractDTO {

	public long total;

	public long active;
	
	public long completed;
	
	public long waiting;
	
	public long aborted;
	
	public long interrupted;
	
   public long halted;
}
