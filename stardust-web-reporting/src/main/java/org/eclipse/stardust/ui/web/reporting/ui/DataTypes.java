package org.eclipse.stardust.ui.web.reporting.ui;

/**
 * @author Yogesh.Manware 
 * Note: These types should remain in sync with UI side types in
 *         ReportingService#metadata
 * 
 */
public enum DataTypes {
   STRING("stringType", "String"), INTEGER("integerType", "Integer"), DECIMAL("decimalType", "Decimal"), COUNT("countType", "Count"), TIMESTAMP(
         "timestampType", "Timestamp"), DURATION("durationType", "Duration"), ENUMERATION("enumerationType",
         "Enumeration"), BOOLEAN("booleanType", "Boolean");

   private String id;
   private String name;

   private DataTypes(String id, String name)
   {
      this.id = id;
      this.name = name;
   }

   public String getId()
   {
      return id;
   }

   public String getName()
   {
      return name;
   }
}
