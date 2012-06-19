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
package org.eclipse.stardust.ui.web.viewscommon.descriptors;

import java.io.Serializable;

/**
 * @author Yogesh.Manware
 * 
 */
public class NumberRange implements Serializable
{
   private static final long serialVersionUID = 1L;
   private Number fromValue;
   private Number toValue;

   /**
    * @param fromValue
    * @param toValue
    */
   public NumberRange(Number fromValue, Number toValue)
   {
      super();
      this.fromValue = fromValue;
      this.toValue = toValue;
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((fromValue == null) ? 0 : fromValue.hashCode());
      result = prime * result + ((toValue == null) ? 0 : toValue.hashCode());
      return result;
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      NumberRange other = (NumberRange) obj;
      if (fromValue == null)
      {
         if (other.fromValue != null)
            return false;
      }
      else if (!fromValue.equals(other.fromValue))
         return false;
      if (toValue == null)
      {
         if (other.toValue != null)
            return false;
      }
      else if (!toValue.equals(other.toValue))
         return false;
      return true;
   }

   /**
    * @return the fromValue
    */
   public Number getFromValue()
   {
      return fromValue;
   }

   /**
    * @param fromValue
    *           the fromValue to set
    */
   public void setFromValue(Number fromValue)
   {
      this.fromValue = fromValue;
   }

   /**
    * @return the toValue
    */
   public Number getToValue()
   {
      return toValue;
   }

   /**
    * @param toValue
    *           the toValue to set
    */
   public void setToValue(Number toValue)
   {
      this.toValue = toValue;
   }
}
