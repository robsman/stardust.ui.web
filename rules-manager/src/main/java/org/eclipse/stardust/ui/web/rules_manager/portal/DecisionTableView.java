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

package org.eclipse.stardust.ui.web.rules_manager.portal;

import org.eclipse.stardust.ui.web.common.event.ViewEvent;


public class DecisionTableView extends AbstractAdapterView {  
   /**
    * 
    */
	public DecisionTableView()
   {
      super("/plugins/rules-manager/views/decisionTableView.html", "decisionTableFrameAnchor");
   }	

   @Override
   public void handleEvent(ViewEvent event)
   {
      super.handleEvent(event);
      
      event.getView().setIcon("/doesnot-exist/pi-decision-table.png");
   }	
}