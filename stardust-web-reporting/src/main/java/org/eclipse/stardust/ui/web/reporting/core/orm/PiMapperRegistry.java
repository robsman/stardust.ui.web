/*******************************************************************************
 * Copyright (c) 2011 - 2012 SunGard CSA LLC
 *******************************************************************************/

package org.eclipse.stardust.ui.web.reporting.core.orm;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.ui.web.reporting.core.Constants.DurationUnit;
import org.eclipse.stardust.ui.web.reporting.core.Constants.PiDimensionField;
import org.eclipse.stardust.ui.web.reporting.core.orm.DataField.DataFieldType;
import org.eclipse.stardust.ui.web.reporting.core.util.ReportingUtil;

public class PiMapperRegistry extends AbstractMapperRegistry<ProcessInstance>
{
   public PiMapperRegistry()
   {
      register(PiDimensionField.PRIORITY.getId(),
            new IMappingProvider<Integer, ProcessInstance>()
            {
               @Override
               public Integer provideResultSetValue(ProviderContext context, ResultSet rs) throws SQLException
               {
                  // TODO Auto-generated method stub
                  return null;
               }

               @Override
               public Integer provideObjectValue(ProviderContext context, ProcessInstance t)
               {
                  return t.getPriority();
               }

               @Override
               public DataField provideDataField(ProviderContext context)
               {
                  return new DataField(PiDimensionField.PRIORITY.getId(),
                        DataFieldType.NUMBER);
               }
            });

      register(PiDimensionField.PROCESS_NAME.getId(),
            new IMappingProvider<String, ProcessInstance>()
            {
               @Override
               public String provideResultSetValue(ProviderContext context, ResultSet rs) throws SQLException
               {
                  return null;
               }

               @Override
               public String provideObjectValue(ProviderContext context, ProcessInstance t)
               {
                  return t.getProcessName();
               }

               @Override
               public DataField provideDataField(ProviderContext context)
               {
                  return new DataField(PiDimensionField.PROCESS_NAME.getId(),
                        DataFieldType.SHORT_STRING);
               }
            });

      register(PiDimensionField.START_TIMESTAMP.getId(),
            new IMappingProvider<Date, ProcessInstance>()
            {
               @Override
               public Date provideResultSetValue(ProviderContext context, ResultSet rs) throws SQLException
               {
                  return null;
               }

               @Override
               public Date provideObjectValue(ProviderContext context, ProcessInstance t)
               {
                  return t.getStartTime();
               }

               @Override
               public DataField provideDataField(ProviderContext context)
               {
                  return new DataField(PiDimensionField.START_TIMESTAMP.getId(),
                        DataFieldType.NUMBER);
               }
            });

      register(PiDimensionField.STARTING_USER_NAME.getId(),
            new IMappingProvider<String, ProcessInstance>()
            {
               @Override
               public String provideResultSetValue(ProviderContext context, ResultSet rs) throws SQLException
               {
                  return null;
               }

               @Override
               public String provideObjectValue(ProviderContext context, ProcessInstance t)
               {
                  return t.getStartingUser().getName();
               }

               @Override
               public DataField provideDataField(ProviderContext context)
               {
                  return new DataField(PiDimensionField.STARTING_USER_NAME.getId(),
                        DataFieldType.SHORT_STRING);
               }
            });

      register(PiDimensionField.TERMINATION_TIMESTAMP.getId(),
            new IMappingProvider<Date, ProcessInstance>()
            {
               @Override
               public Date provideResultSetValue(ProviderContext context, ResultSet rs) throws SQLException
               {
                  return null;
               }

               @Override
               public Date provideObjectValue(ProviderContext context, ProcessInstance t)
               {
                  return t.getTerminationTime();
               }

               @Override
               public DataField provideDataField(ProviderContext context)
               {
                  return new DataField(PiDimensionField.TERMINATION_TIMESTAMP.getId(),
                        DataFieldType.NUMBER);
               }
            });

      register(PiDimensionField.STATE.getId(),
            new IMappingProvider<Integer, ProcessInstance>()
            {
               @Override
               public Integer provideResultSetValue(ProviderContext context, ResultSet rs) throws SQLException
               {
                  return null;
               }

               @Override
               public Integer provideObjectValue(ProviderContext context, ProcessInstance t)
               {
                  return t.getState().getValue();
               }

               @Override
               public DataField provideDataField(ProviderContext context)
               {
                  return new DataField(PiDimensionField.STATE.getId(),
                        DataFieldType.NUMBER);
               }
            });

      register(PiDimensionField.DURATION.getId(), new IMappingProvider<Long, ProcessInstance>()
      {
         @Override
         public Long provideResultSetValue(ProviderContext context, ResultSet rs)
               throws SQLException
         {
            // TODO Auto-generated method stub
            return null;
         }

         @Override
         public Long provideObjectValue(ProviderContext context, ProcessInstance t)
         {
            DurationUnit du = (DurationUnit) context.getContextData(ProviderContext.DURATION_UNIT_ID);
            return ReportingUtil.calculateDuration(t.getStartTime(),
                  t.getTerminationTime(), du);
         }

         @Override
         public DataField provideDataField(ProviderContext context)
         {
            return new DataField(PiDimensionField.DURATION.getId(),
                  DataFieldType.NUMBER);
         }

      });
   }
}
