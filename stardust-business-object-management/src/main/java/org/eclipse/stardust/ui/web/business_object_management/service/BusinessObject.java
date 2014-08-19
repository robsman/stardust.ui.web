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
package org.eclipse.stardust.ui.web.business_object_management.service;

import java.io.Serializable;
import java.util.List;

import javax.xml.namespace.QName;

/**
 * Development project <a href="https://www.csa.sungard.com/wiki/display/infinity/Blackbird">Blackbird</a>.
 *
 * @author Florin.Herinean
 * @version $Revision: $
 */
public interface BusinessObject extends Serializable
{
   public static interface Definition
   {
      String getName();

      int getType();

      QName getTypeName();

      boolean isList();

      boolean isKey();

      boolean isPrimaryKey();

      List<Definition> getItems();
   }

   public static interface Value
   {
      long getProcessInstanceOid();

      Serializable getValue();
   }

   /**
    * Gets the oid of the model defining the business object.
    *
    * @return The oid of this object
    */
   long getModelOid();

   /**
    * Gets the id of the business object.
    *
    * @return The id.
    */
   String getId();

   /**
    * Gets the name of the business object.
    *
    * @return The name.
    */
   String getName();

   /**
    * Gets the definition of the business object.
    *
    * @return The name.
    */
   List<Definition> getItems();

   List<Value> getValues();
}