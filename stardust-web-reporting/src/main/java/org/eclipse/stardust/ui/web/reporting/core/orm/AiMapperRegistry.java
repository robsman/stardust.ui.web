/*******************************************************************************
 * Copyright (c) 2011 - 2012 SunGard CSA LLC
 *******************************************************************************/

package org.eclipse.stardust.ui.web.reporting.core.orm;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.ui.web.reporting.core.Constants.AiDimensionField;
import org.eclipse.stardust.ui.web.reporting.core.Constants.DurationUnit;
import org.eclipse.stardust.ui.web.reporting.core.orm.DataField.DataFieldType;
import org.eclipse.stardust.ui.web.reporting.core.util.ReportingUtil;

public class AiMapperRegistry extends AbstractMapperRegistry<ActivityInstance>
{
   public AiMapperRegistry()
   {

      register(AiDimensionField.START_TIMESTAMP.getId(),
            new IMappingProvider<Date, ActivityInstance>()
            {
               @Override
               public Date provideResultSetValue(ProviderContext context, ResultSet rs) throws SQLException
               {
                  return null;
               }

               @Override
               public Date provideObjectValue(ProviderContext context, ActivityInstance t)
               {
                  return t.getStartTime();
               }

               @Override
               public DataField provideDataField(ProviderContext context)
               {
                  // TODO Auto-generated method stub
                  return new DataField(AiDimensionField.START_TIMESTAMP.getId(),
                        DataFieldType.NUMBER);
               }
            });

      register(AiDimensionField.PROCESS_INSTANCE_START_TIMESTAMP.getId(),
            new IMappingProvider<Date, ActivityInstance>()
            {
               @Override
               public Date provideResultSetValue(ProviderContext context, ResultSet rs) throws SQLException
               {
                  return null;
               }

               @Override
               public Date provideObjectValue(ProviderContext context, ActivityInstance t)
               {
                  return t.getProcessInstance().getStartTime();
               }

               @Override
               public DataField provideDataField(ProviderContext context)
               {
                  // TODO Auto-generated method stub
                  return new DataField(AiDimensionField.PROCESS_INSTANCE_START_TIMESTAMP.getId(),
                        DataFieldType.NUMBER);
               }
            });

      register(AiDimensionField.PROCESS_INSTANCE_ROOT_START_TIMESTAMP.getId(),
            new IMappingProvider<Date, ActivityInstance>()
            {
               @Override
               public Date provideResultSetValue(ProviderContext context, ResultSet rs) throws SQLException
               {
                  return null;
               }

               @Override
               public Date provideObjectValue(ProviderContext context, ActivityInstance t)
               {
                  ProcessInstance pi = ReportingUtil.findRootProcessInstance(context.getQueryService(), t.getProcessInstance());
                  return pi.getStartTime();
               }

               @Override
               public DataField provideDataField(ProviderContext context)
               {
                  // TODO Auto-generated method stub
                  return new DataField(AiDimensionField.PROCESS_INSTANCE_ROOT_START_TIMESTAMP.getId(),
                        DataFieldType.NUMBER);
               }
            });

      register(AiDimensionField.LAST_MODIFICATION_TIMESTAMP.getId(),
            new IMappingProvider<Date, ActivityInstance>()
            {

               @Override
               public Date provideResultSetValue(ProviderContext context, ResultSet rs) throws SQLException
               {
                  // TODO Auto-generated method stub
                  return null;
               }

               @Override
               public Date provideObjectValue(ProviderContext context, ActivityInstance t)
               {
                  return t.getLastModificationTime();
               }

               @Override
               public DataField provideDataField(ProviderContext context)
               {
                  return new DataField(AiDimensionField.LAST_MODIFICATION_TIMESTAMP
                        .getId(), DataFieldType.NUMBER);
               }
            });

      register(AiDimensionField.ACTIVITY_NAME.getId(),
            new IMappingProvider<String, ActivityInstance>()
            {
               @Override
               public String provideResultSetValue(ProviderContext context, ResultSet rs) throws SQLException
               {
                  // TODO Auto-generated method stub
                  return null;
               }

               @Override
               public String provideObjectValue(ProviderContext context, ActivityInstance t)
               {
                  return t.getActivity().getName();
               }

               @Override
               public DataField provideDataField(ProviderContext context)
               {
                  return new DataField(AiDimensionField.ACTIVITY_NAME.getId(),
                        DataFieldType.SHORT_STRING);
               }
            });

      register(AiDimensionField.PROCESS_NAME.getId(),
            new IMappingProvider<String, ActivityInstance>()
            {

               @Override
               public String provideResultSetValue(ProviderContext context, ResultSet rs) throws SQLException
               {
                  // TODO Auto-generated method stub
                  return null;
               }

               @Override
               public String provideObjectValue(ProviderContext context, ActivityInstance t)
               {
                  return t.getProcessInstance().getProcessName();
               }

               @Override
               public DataField provideDataField(ProviderContext context)
               {
                  return new DataField(AiDimensionField.PROCESS_NAME.getId(),
                        DataFieldType.SHORT_STRING);
               }
            });

      register(AiDimensionField.CRITICALITY.getId(),
            new IMappingProvider<Double, ActivityInstance>()
            {
               @Override
               public Double provideResultSetValue(ProviderContext context, ResultSet rs) throws SQLException
               {
                  // TODO Auto-generated method stub
                  return null;
               }

               @Override
               public Double provideObjectValue(ProviderContext context, ActivityInstance t)
               {
                  return t.getCriticality();
               }

               @Override
               public DataField provideDataField(ProviderContext context)
               {
                  return new DataField(AiDimensionField.CRITICALITY.getId(),
                        DataFieldType.NUMBER);
               }
            });

      register(AiDimensionField.USER_PERFORMER_NAME.getId(),
            new IMappingProvider<String, ActivityInstance>()
            {

               @Override
               public String provideResultSetValue(ProviderContext context, ResultSet rs) throws SQLException
               {
                  // TODO Auto-generated method stub
                  return null;
               }

               @Override
               public String provideObjectValue(ProviderContext context, ActivityInstance t)
               {
                  return t.getUserPerformerName();
               }

               @Override
               public DataField provideDataField(ProviderContext context)
               {
                  return new DataField(AiDimensionField.USER_PERFORMER_NAME.getId(),
                        DataFieldType.SHORT_STRING);
               }
            });

      register(AiDimensionField.PARTICIPANT_PERFORMER_NAME.getId(),
            new IMappingProvider<String, ActivityInstance>()
            {

               @Override
               public String provideResultSetValue(ProviderContext context, ResultSet rs) throws SQLException
               {
                  // TODO Auto-generated method stub
                  return null;
               }

               @Override
               public String provideObjectValue(ProviderContext context, ActivityInstance t)
               {
                  return t.getParticipantPerformerName();
               }

               @Override
               public DataField provideDataField(ProviderContext context)
               {
                  return new DataField(AiDimensionField.PARTICIPANT_PERFORMER_NAME
                        .getId(), DataFieldType.SHORT_STRING);
               }
            });

      register(AiDimensionField.STATE.getId(),
            new IMappingProvider<Integer, ActivityInstance>()
            {

               @Override
               public Integer provideResultSetValue(ProviderContext context, ResultSet rs) throws SQLException
               {
                  // TODO Auto-generated method stub
                  return null;
               }

               @Override
               public Integer provideObjectValue(ProviderContext context, ActivityInstance t)
               {
                  return t.getState().getValue();
               }

               @Override
               public DataField provideDataField(ProviderContext context)
               {
                  return new DataField(AiDimensionField.STATE.getId(),
                        DataFieldType.NUMBER);
               }
            });

      register(AiDimensionField.DURATION.getId(),
            new IMappingProvider<Long, ActivityInstance>()
            {

               @Override
               public Long provideResultSetValue(ProviderContext context, ResultSet rs)
                     throws SQLException
               {
                  // TODO Auto-generated method stub
                  return null;
               }

               @Override
               public Long provideObjectValue(ProviderContext context, ActivityInstance t)
               {
                  DurationUnit du = (DurationUnit) context.getContextData(ProviderContext.DURATION_UNIT_ID);
                  return ReportingUtil.calculateDuration(t.getStartTime(),
                        t.getLastModificationTime(), du);
               }

               @Override
               public DataField provideDataField(ProviderContext context)
               {
                  return new DataField(AiDimensionField.DURATION.getId(),
                        DataFieldType.NUMBER);
               }
            });

   }
}
