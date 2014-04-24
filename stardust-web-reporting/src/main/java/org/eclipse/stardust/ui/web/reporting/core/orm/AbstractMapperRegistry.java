/*******************************************************************************
 * Copyright (c) 2011 - 2012 SunGard CSA LLC
 *******************************************************************************/

package org.eclipse.stardust.ui.web.reporting.core.orm;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.ui.web.reporting.common.RequestColumn;
import org.eclipse.stardust.ui.web.reporting.core.Constants;
import org.eclipse.stardust.ui.web.reporting.core.orm.DataField.DataFieldType;

public abstract class AbstractMapperRegistry<T>
{
   private Map<String, IMappingProvider<? , T>> registeredMapper;

   public AbstractMapperRegistry()
   {
      registeredMapper = new HashMap<String, IMappingProvider<?, T>>();
      register(Constants.DimensionField.COUNT.getId(), new TotalCountMappingProvider());
   }

   public void register(String key, IMappingProvider<? , T> provider)
   {
      registeredMapper.put(key, provider);
   }

   public IMappingProvider<? , T> getRegisteredMapper(String key) throws RuntimeException
   {
      IMappingProvider<? , T> mapper = registeredMapper.get(key);
      if(mapper == null)
      {
         throw new RuntimeException("No Mapper found for key: "+key);
      }

      return mapper;
   }

   public List<DataField> getMetaFields(List<RequestColumn> columns)
   {
      List<DataField> result = new ArrayList<DataField>();
      for(RequestColumn column: columns)
      {
         IMappingProvider<? , T> mapper = getRegisteredMapper(column.getId());
         result.add(mapper.provideDataField(null));
      }

      return result;
   }

   public class TotalCountMappingProvider implements IMappingProvider<Long , T>
   {
      @Override
      public Long provideResultSetValue(ProviderContext context, ResultSet rs) throws SQLException
      {
         // TODO Auto-generated method stub
         return null;
      }

      @Override
      public Long provideObjectValue(ProviderContext context, T t)
      {
         return context.getTotalCount();
      }

      @Override
      public DataField provideDataField(ProviderContext context)
      {
         return new DataField(Constants.DimensionField.COUNT.getId(), DataFieldType.NUMBER);
      }
   }
}
