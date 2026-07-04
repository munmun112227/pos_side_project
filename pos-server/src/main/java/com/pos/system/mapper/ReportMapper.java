package com.pos.system.mapper;

import com.pos.system.domain.report.CashierSalesSummaryReport;
import com.pos.system.domain.report.DailyRevenueReport;
import com.pos.system.domain.report.HotSellingProductReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDate;
import java.util.List;

/**
 * MyBatis mapper for complex reports.
 */
@Mapper
public interface ReportMapper {

    /**
     * Calculates daily revenue reports.
     */
    List<DailyRevenueReport> calculateDailyRevenue(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * Retrieves hot selling products ranking.
     */
    List<HotSellingProductReport> getHotSellingProducts(
            @Param("limit") Integer limit,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * Gathers sales summary for cashiers.
     */
    List<CashierSalesSummaryReport> getCashierSalesSummary(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
