package org.eclipse.stardust.ui.web.reporting.core.util;

public class ReportingUtil
{
   /**
    * Converts the key of a duration unit into the equivalent in seconds.
    *
    * @param unit
    * @return
    */
   public static long convertDurationUnit(String unit)
   {
      if (unit.equals("m"))
      {
         return 1000 * 60;
      }
      else if (unit.equals("h"))
      {
         return 1000 * 60 * 60;
      }
      else if (unit.equals("d"))
      {
         return 1000 * 60 * 60 * 24;
      }
      else if (unit.equals("w"))
      {
         return 1000 * 60 * 60 * 24 * 7;
      }
      else if (unit.equals("M"))
      {
         return 1000 * 60 * 60 * 24 * 30; // TODO Consider calendar?
      }
      else if (unit.equals("Y"))
      {
         return 1000 * 60 * 60 * 24 * 30 * 256; // TODO Consider calendar?
      }

      throw new IllegalArgumentException("Duration unit \"" + unit + "\" is not supported.");
   }
}
