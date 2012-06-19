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
package org.eclipse.stardust.ui.web.viewscommon.process.history;

import java.util.Date;
import java.util.List;

import org.eclipse.stardust.engine.api.runtime.RuntimeObject;


/**
 * @author Yogesh.Manware
 *
 */
public interface IProcessHistoryTableEntry
{
   Long getOID();

   String getName();

   String getRuntimeObjectType();

   Date getStartTime();

   Date getLastModificationTime();

   String getState();

   String getPerformer();

   String getDetails();

   public RuntimeObject getRuntimeObject();

   public void setRuntimeObject(RuntimeObject runtimeObject);

   @SuppressWarnings("unchecked")
   public List getChildren();

   public boolean isNodePathToActivityInstance();

   public void setNodePathToActivityInstance(boolean isNodePathToActivityInstance);

   public String getStyleClass();
}
