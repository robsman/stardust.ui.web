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
package org.eclipse.stardust.ui.web.common.event;

/**
 * Base type for UI events.
 * 
 * @param O
 *           The observer type for this event.
 *           <p>
 *           There is no common base type for event observers to cater for the requirement
 *           to observe multiple event types with one class. Having a common base type
 *           would either require instanceof checks in the implementation or forbid to
 *           have one class being an observer for multiple event types.
 * 
 * @author sauer
 * @version $Revision: 1.3 $
 */
public abstract class UiEventBase<O>
{
   /**
    * Implements the event specific dispatch in a type safe manner.
    * 
    * @param observer
    *           the observer to be notified
    */
   abstract void notifyObserver(O observer);
}
