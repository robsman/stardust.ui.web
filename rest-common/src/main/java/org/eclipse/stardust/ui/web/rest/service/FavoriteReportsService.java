package org.eclipse.stardust.ui.web.rest.service;

import java.util.List;

import javax.annotation.Resource;

import org.eclipse.stardust.ui.web.rest.service.dto.FavoriteReportDTO;
import org.eclipse.stardust.ui.web.rest.service.utils.FavoriteReportsUtils;
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
