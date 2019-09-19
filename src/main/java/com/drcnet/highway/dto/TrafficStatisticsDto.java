package com.drcnet.highway.dto;

import com.drcnet.highway.entity.TietouInbound;
import com.drcnet.highway.entity.TietouOrigin;
import com.drcnet.highway.enums.BoundEnum;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

/**
 * @Author: penghao
 * @CreateTime: 2019/8/16 11:22
 * @Description:
 */
@Data
public class TrafficStatisticsDto {

    private Integer stationId;

    private String stationName;

    private Integer boundType;

    private LocalDate currentDay;

    public TrafficStatisticsDto(TietouOrigin tietouOrigin, BoundEnum boundEnum) {
        Integer stationIdOther = null;
        String stationNameOther = null;
        LocalDate time = null;
        if (boundEnum == BoundEnum.OUTBOUND) {
            stationIdOther = tietouOrigin.getCkId();
            stationNameOther = tietouOrigin.getCk();
            time = Optional.ofNullable(tietouOrigin.getExtime()).map(LocalDateTime::toLocalDate).orElse(null);
        } else if (boundEnum == BoundEnum.INBOUND) {
            stationIdOther = tietouOrigin.getRkId();
            stationNameOther = tietouOrigin.getRk();
            time = Optional.ofNullable(tietouOrigin.getEntime()).map(LocalDateTime::toLocalDate).orElse(null);
        }
        this.stationId = stationIdOther;
        this.stationName = stationNameOther;
        this.currentDay = time;
        this.boundType = BoundEnum.OUTBOUND.code;
    }

    public TrafficStatisticsDto(TietouInbound tietouInbound) {
        String rk = tietouInbound.getRk();
        Integer rkId = tietouInbound.getRkId();
        LocalDateTime entime = tietouInbound.getEntime();
        stationId = rkId;
        stationName = rk;
        currentDay = entime.toLocalDate();
        boundType = BoundEnum.INBOUND.code;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TrafficStatisticsDto)) return false;
        TrafficStatisticsDto that = (TrafficStatisticsDto) o;
        return Objects.equals(stationId, that.stationId) &&
                Objects.equals(boundType, that.boundType) &&
                Objects.equals(currentDay, that.currentDay);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stationId, boundType, currentDay);
    }
}
