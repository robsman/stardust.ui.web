package org.eclipse.stardust.ui.web.reporting.core;


public class Constants
{
   public enum DurationUnit {
      SECOND("s"),
      MINUTE("m"),
      HOUR("h"),
      DAY("d"),
      WEEK("w"),
      MONTH("M"),
      YEAR("Y");

      private String id;
      DurationUnit(String id)
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

      public static DurationUnit parse(String s)
      {
         for(DurationUnit type: DurationUnit.values())
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

   public enum DimensionField
   {
      COUNT("count");

      private String id;
      DimensionField(String id)
      {
         this.id = id;
      }

      public String getId()
      {
         return id;
      }

      public static DimensionField parse(String s)
      {
         for(DimensionField type: DimensionField.values())
         {
            if(type.getId().equals(s))
            {
               return type;
            }
         }

         StringBuilder errorMsgBuilder = new StringBuilder();
         errorMsgBuilder.append("Unkown DimensionField: ").append("'");
         errorMsgBuilder.append(s);
         errorMsgBuilder.append("'");
         throw new RuntimeException(errorMsgBuilder.toString());
      }
   }


   public enum PiDimensionField
   {
      OID("processOID"),
      PRIORITY("priority"),
      PROCESS_NAME("processName"),
      START_TIMESTAMP("processInstanceStartTimestamp"),
      ROOT_START_TIMESTAMP("rootProcessInstanceStartTimestamp"),
      STARTING_USER_NAME("startingUserName"),
      TERMINATION_TIMESTAMP("terminationTimestamp"),
      STATE("state"),
      DURATION("duration");

      private String id;
      PiDimensionField(String id)
      {
         this.id = id;
      }

      public String getId()
      {
         return id;
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
      OID("activityOID"),
      PROCESS_OID(PiDimensionField.OID.getId()),
      START_TIMESTAMP("startTimestamp"),
      PROCESS_INSTANCE_START_TIMESTAMP(PiDimensionField.START_TIMESTAMP.getId()),
      PROCESS_INSTANCE_ROOT_START_TIMESTAMP(PiDimensionField.ROOT_START_TIMESTAMP.getId()),
      LAST_MODIFICATION_TIMESTAMP("lastModificationTimestamp"),
      ACTIVITY_NAME("activityName"),
      PROCESS_NAME("processName"),
      CRITICALITY("criticality"),
      USER_PERFORMER_NAME("USERPERFORMERNAME"),
      PARTICIPANT_PERFORMER_NAME("participantPerformerName"),
      STATE("state"),
      DURATION("duration");

      private String id;
      private AiDimensionField(String id)
      {
         this.id = id;
      }

      public String getId()
      {
         return id;
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
}
