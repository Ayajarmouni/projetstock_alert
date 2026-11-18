package com.stockalert.alertes.mapper;

import com.stockalert.alertes.dto.ThresholdDto;
import com.stockalert.alertes.entite.Threshold;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface ThresholdMapper {
    Threshold toEntity(ThresholdDto dto);

    ThresholdDto toDto(Threshold entity);
}
