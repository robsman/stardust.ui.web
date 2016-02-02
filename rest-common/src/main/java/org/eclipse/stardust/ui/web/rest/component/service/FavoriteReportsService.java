package org.eclipse.stardust.ui.web.rest.component.service;

import java.util.List;

import javax.annotation.Resource;

import org.eclipse.stardust.ui.web.rest.component.util.FavoriteReportsUtils;
import org.eclipse.stardust.ui.web.rest.dto.FavoriteReportDTO;
import org.springframework.stereotype.Component;

@Component
public class FavoriteReportsService
{
   @Resource
   private FavoriteReportsUtils favoriteReportsUtils;

   public List<FavoriteReportDTO> getFavoriteReports()
   {
      return favoriteReportsUtils.getFavoriteReports();
   }

}
