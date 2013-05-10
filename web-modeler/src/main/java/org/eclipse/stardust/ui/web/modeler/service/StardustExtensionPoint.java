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

package org.eclipse.stardust.ui.web.modeler.service;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.InvalidRegistryObjectException;

public class StardustExtensionPoint implements IExtensionPoint
{
   private static final IExtension[] EMPTY_EXTENSIONS = new IExtension[0];
   private List<IExtension> elements = new ArrayList<IExtension>();

   public void addExtension(IExtension ext)
   {
      elements.add(ext);
   }

   @Override
   public IConfigurationElement[] getConfigurationElements()
         throws InvalidRegistryObjectException
   {
      // TODO Auto-generated method stub
      return null;
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
   public IExtension getExtension(String extensionId)
         throws InvalidRegistryObjectException
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public IExtension[] getExtensions() throws InvalidRegistryObjectException
   {
      return elements.isEmpty() ? EMPTY_EXTENSIONS : elements.toArray(new IExtension[elements.size()]);
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
   public String getSchemaReference() throws InvalidRegistryObjectException
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
