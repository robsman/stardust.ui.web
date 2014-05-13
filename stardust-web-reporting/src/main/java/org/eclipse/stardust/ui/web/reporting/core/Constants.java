package org.eclipse.stardust.ui.web.reporting.core;

import com.ibm.icu.util.Calendar;




public class Constants
{
   public enum TimeUnit {
      SECOND("s", "yyyy/MM/dd hh:mm:ss", Calendar.SECOND),
      MINUTE("m", "yyyy/MM/dd hh:mm", Calendar.MINUTE),
      HOUR("h", "yyyy/MM/dd hh", Calendar.HOUR_OF_DAY),
      DAY("d", "yyyy/MM/dd", Calendar.DAY_OF_YEAR),
      WEEK("w", "yyyy/MM/WW", Calendar.WEEK_OF_MONTH),
      MONTH("M", "yyyy/MM", Calendar.MONTH),
      YEAR("Y", "yyyy", Calendar.YEAR);

      private String id;
      private int calendarField;
      private String dateFormat;
      TimeUnit(String id, String dateFormat, int calendarField)
      {
         this.id = id;
         this.dateFormat = dateFormat;
         this.calendarField = calendarField;
      }

      public int getCalendarField()
      {
         return calendarField;
      }

      public String getDateFormat()
      {
         return dateFormat;
      }

      @Override
      public String toString()
      {
         return getId();
      }

      public String getId()
      {
         return id;
      }

      public static TimeUnit parse(String s)
      {
         for(TimeUnit type: TimeUnit.values())
         {
            if(type.getId().equals(s))
            {
               return type;
            }
         }

         StringBuilder errorMsgBuilder = new StringBuilder();
         errorMsgBuilder.append("Unkown DurationUnit: ").append("'");
         errorMsgBuilder.append(s);
         errorMsgBuilder.append("'");
         throw new RuntimeException(errorMsgBuilder.toString());
      }
   }


   public enum QueryType {
      PROCESS_INSTANCE("processInstance"),
      ACTIVITY_INSTANCE("activityInstance");

      private String id;
      QueryType(String id)
      {
         this.id = id;
      }

      @Override
      public String toString()
      {
         return getId();
      }

      public String getId()
      {
         return id;
      }

      public static QueryType parse(String s)
      {
         for(QueryType type: QueryType.values())
         {
            if(type.getId().equals(s))
            {
               return type;
            }
         }

         StringBuilder errorMsgBuilder = new StringBuilder();
         errorMsgBuilder.append("Unkown QueryType: ").append("'");
         errorMsgBuilder.append(s);
         errorMsgBuilder.append("'");
         throw new RuntimeException(errorMsgBuilder.toString());
      }
   }


   public enum DataSetType {
      SERIESGROUP("seriesGroup"),
      RECORDSET("recordSet");

      private String id;
      DataSetType(String id)
      {
         this.id = id;
      }

      @Override
      public String toString()
      {
         return getId();
      }

      public String getId()
      {
         return id;
      }

      public static DataSetType parse(String s)
      {
         for(DataSetType type: DataSetType.values())
         {
            if(type.getId().equals(s))
            {
               return type;
            }
         }

         StringBuilder errorMsgBuilder = new StringBuilder();
         errorMsgBuilder.append("Unkown DataSetType: ").append("'");
         errorMsgBuilder.append(s);
         errorMsgBuilder.append("'");
         throw new RuntimeException(errorMsgBuilder.toString());
      }
   }

   public enum FactField
   {
      COUNT("count");

      private String id;
      FactField(String id)
      {
         this.id = id;
      }

      public String getId()
      {
         return id;
      }

      public static FactField parse(String s)
      {
         for(FactField type: FactField.values())
         {
            if(type.getId().equals(s))
            {
               return type;
            }
         }

         StringBuilder errorMsgBuilder = new StringBuilder();
         errorMsgBuilder.append("Unkown FactFieldd: ").append("'");
         errorMsgBuilder.append(s);
         errorMsgBuilder.append("'");
         throw new RuntimeException(errorMsgBuilder.toString());
      }
   }

   public enum PiDimensionField
   {
      OID("processOID", true),
      PRIORITY("priority", true),
      PROCESS_NAME("processName", true),
      START_TIMESTAMP("processInstanceStartTimestamp", true),
      ROOT_START_TIMESTAMP("rootProcessInstanceStartTimestamp", true),
      STARTING_USER_NAME("startingUserName", true),
      TERMINATION_TIMESTAMP("terminationTimestamp", true),
      STATE("state", true),
      DURATION("processInstanceDuration", false),
      ROOT_DURATION("rootProcessInstanceDuration", false);

      private String id;
      private boolean scriptingVariable;
      PiDimensionField(String id, boolean scriptingVariable)
      {
         this.id = id;
         this.scriptingVariable = scriptingVariable;
      }

      public String getId()
      {
         return id;
      }

      public boolean isScriptingVariable()
      {
         return scriptingVariable;
      }

      public static PiDimensionField parse(String s)
      {
         for(PiDimensionField type: PiDimensionField.values())
         {
            if(type.getId().equals(s))
            {
               return type;
            }
         }

         StringBuilder errorMsgBuilder = new StringBuilder();
         errorMsgBuilder.append("Unkown PiDimensionField: ").append("'");
         errorMsgBuilder.append(s);
         errorMsgBuilder.append("'");
         throw new RuntimeException(errorMsgBuilder.toString());
      }
   }

   public enum AiDimensionField
   {
      OID("activityOID", true),
      PROCESS_OID(PiDimensionField.OID.getId(), true),
      START_TIMESTAMP("startTimestamp", true),
      PROCESS_INSTANCE_START_TIMESTAMP(PiDimensionField.START_TIMESTAMP.getId(), true),
      PROCESS_INSTANCE_ROOT_START_TIMESTAMP(PiDimensionField.ROOT_START_TIMESTAMP.getId(), true),
      LAST_MODIFICATION_TIMESTAMP("lastModificationTimestamp", true),
      ACTIVITY_NAME("activityName", true),
      PROCESS_NAME("processName", true),
      CRITICALITY("criticality", true),
      USER_PERFORMER_NAME("userPerformerName", true),
      PARTICIPANT_PERFORMER_NAME("participantPerformerName", true),
      STATE("state", true),
      DURATION("activityInstanceDuration", false),
      PROCESS_INSTANCE_DURATION(PiDimensionField.DURATION.getId(), false),
      PROCESS_INSTANCE_ROOT_DURATION(PiDimensionField.ROOT_DURATION.getId(), false);

      private String id;
      private boolean scriptingVariable;
      private AiDimensionField(String id, boolean scriptingVariable)
      {
         this.id = id;
         this.scriptingVariable = scriptingVariable;
      }

      public String getId()
      {
         return id;
      }

      public boolean isScriptingVariable()
      {
         return scriptingVariable;
      }

      public static AiDimensionField parse(String s)
      {
         for(AiDimensionField type: AiDimensionField.values())
         {
            if(type.getId().equals(s))
            {
               return type;
            }
         }

         StringBuilder errorMsgBuilder = new StringBuilder();
         errorMsgBuilder.append("Unkown AiDimensionField: ").append("'");
         errorMsgBuilder.append(s);
         errorMsgBuilder.append("'");
         throw new RuntimeException(errorMsgBuilder.toString());
      }
   }

   public enum FilterConstants
   {
      ALIVE("Alive");
      private FilterConstants(String id)
      {
         this.id = id;
      }
      private String id;

      public String getId()
      {
         return id;
      }

      @Override
      public String toString()
      {
         return getId();
      }
   }
}
