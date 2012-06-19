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
package org.eclipse.stardust.ui.event;

/**
 * @author sauer
 * @version $Revision: 1.3 $
 */
public abstract class AbstractEvent<O extends AbstractEventObserver>
{
   /**
    * This callback is used for typesafe dispatching of events to observers.
    * 
    * @param observer
    *            The observer to be notified.
    */
   protected abstract void notifyObserver(O observer);
}
