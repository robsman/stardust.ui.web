/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Anoop.Nair (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.rest.service.imageio;

public class PdfDocument extends MultiPageDocument implements IMultiPageDocument
{
   public PdfDocument(byte[] content)
   {
      super(content);
   }

   @Override
   public int getPageCount()
   {
      // TODO Auto-generated method stub
      return 0;
   }

   @Override
   public byte[] extractPages()
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public byte[] reorderPages()
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public byte[] deletePages()
   {
      // TODO Auto-generated method stub
      return null;
   }
}
