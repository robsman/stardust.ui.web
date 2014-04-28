package org.eclipse.stardust.ui.web.reporting.core.handler;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.eclipse.stardust.ui.web.reporting.core.DataField;

public interface IMappingHandler<T, O>
{
   public T provideResultSetValue(HandlerContext context, ResultSet rs) throws SQLException;
   public T provideObjectValue(HandlerContext context, O t);
   public DataField provideDataField(HandlerContext context);
}
