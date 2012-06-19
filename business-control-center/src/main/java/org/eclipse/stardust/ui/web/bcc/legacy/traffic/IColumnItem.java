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

import javax.faces.event.ActionEvent;

import org.eclipse.stardust.engine.api.runtime.ActivityInstance;


public interface IColumnItem
{
   public String getId();

   public String getName();

   public String getSymbolName();

   public void addCompletedActivity();

   public void calculateColumnState(String processId, String categoryId,
         String categoryValue);

   void showProcessTables(ActionEvent event);

   void addActivityInstance(ActivityInstance instance);
}
