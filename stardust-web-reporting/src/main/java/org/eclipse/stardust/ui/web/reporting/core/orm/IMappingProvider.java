package org.eclipse.stardust.ui.web.reporting.core.orm;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface IMappingProvider<T, O>
{
   public T provideResultSetValue(ProviderContext context, ResultSet rs) throws SQLException;
   public T provideObjectValue(ProviderContext context, O t);
   public DataField provideDataField(ProviderContext context);
}
