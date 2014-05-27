/*******************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SunGard CSA LLC - initial API and implementation
 *     
 * @author Barry.Grotjahn
 *******************************************************************************/

package org.eclipse.stardust.ui.web.modeler.xpdl;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.InvalidRegistryObjectException;

public class StardustExtension implements IExtension
{
   private static final IConfigurationElement[] EMPTY_CONFIGURATION_ELEMENTS = new IConfigurationElement[0];
   private List<IConfigurationElement> elements = new ArrayList<IConfigurationElement>();
   
   public void addConfigurationElement(IConfigurationElement element)
   {
      elements.add(element);
   }
   
   
   @Override
   public IConfigurationElement[] getConfigurationElements()
         throws InvalidRegistryObjectException
   {
      return elements.isEmpty() ? EMPTY_CONFIGURATION_ELEMENTS : elements.toArray(new IConfigurationElement[elements.size()]);
   }

   @Override
   public String getNamespace() throws InvalidRegistryObjectException
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public String getNamespaceIdentifier() throws InvalidRegistryObjectException
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public IContributor getContributor() throws InvalidRegistryObjectException
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public String getExtensionPointUniqueIdentifier()
         throws InvalidRegistryObjectException
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public String getLabel() throws InvalidRegistryObjectException
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public String getLabel(String locale) throws InvalidRegistryObjectException
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public String getSimpleIdentifier() throws InvalidRegistryObjectException
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public String getUniqueIdentifier() throws InvalidRegistryObjectException
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public boolean isValid()
   {
      // TODO Auto-generated method stub
      return false;
   }

}
