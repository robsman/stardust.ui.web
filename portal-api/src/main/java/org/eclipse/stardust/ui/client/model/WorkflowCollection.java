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
package org.eclipse.stardust.ui.client.model;

import java.util.Collection;

import org.eclipse.stardust.ui.client.event.StatusEventObserver;


public interface WorkflowCollection<E> extends Collection<E>
{
   void update();
   
   void addStatusListener(StatusEventObserver observer);

   void removeStatusListener(StatusEventObserver observer);
}
