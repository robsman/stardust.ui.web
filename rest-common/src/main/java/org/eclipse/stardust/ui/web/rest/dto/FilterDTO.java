/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.rest.dto;

import java.util.List;

public interface FilterDTO
{

   public static class RangesDTO
   {
      public List<RangeDTO> rangeLike;
   }

   public static class EqualsDTO
   {
      public List<String> like;
   }

   public static class BooleanDTO
   {
      public boolean equals;
   }

   public static class TextSearchDTO
   {
      public String textSearch;
   }

   public static class RangeDTO
   {
      public Long from;

      public Long to;
   }
   
   public static class NameDTO
   {
      public String firstName;

      public String lastName;
   }
   
}
