package org.eclipse.stardust.ui.web.rest;

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
