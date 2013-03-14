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

package org.eclipse.stardust.ui.web.modeler.portal;

import org.eclipse.stardust.ui.web.common.event.ViewEvent;

/**
 *
 * @author Marc.Gille
 *
 * TODO Use AbstractAdapterView with Spring Bean Properties?
 */
public class OrganizationView extends AbstractAdapterView {
   /**
    *
    */
	public OrganizationView()
   {
      super("/plugins/bpm-modeler/views/modeler/organizationView.html", "organizationFrameAnchor", "organizationName");
   }

   @Override
   public void handleEvent(ViewEvent event)
   {
      super.handleEvent(event);

      switch (event.getType())
      {
      case CREATED:
         event.getView().setIcon("/plugins/bpm-modeler/images/icons/organization.png");
         break;
      }
   }
}
