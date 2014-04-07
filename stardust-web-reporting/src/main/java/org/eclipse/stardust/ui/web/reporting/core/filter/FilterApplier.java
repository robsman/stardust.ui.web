package org.eclipse.stardust.ui.web.reporting.core.filter;

import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.ui.web.reporting.common.mapping.request.ReportFilter;

public abstract class FilterApplier<T extends Query>
{
   public abstract void apply(T query, ReportFilter filter);
}