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
package org.eclipse.stardust.ui.web.common.spring;

import org.eclipse.stardust.ui.web.common.PreferencePage;
import org.eclipse.stardust.ui.web.common.PreferencesDefinition;

/**
 * @author Subodh.Godbole
 *
 */
public class PreferencesFactoryBean extends HierarchyFactoryBean<PreferencesDefinition, PreferencePage>
{
   public void applyElement(PreferencePage prefPage) throws Exception
   {
      this.parent.addElement(prefPage);
   }

   public Class<PreferencesDefinition> getObjectType()
   {
      return PreferencesDefinition.class;
   }
}
