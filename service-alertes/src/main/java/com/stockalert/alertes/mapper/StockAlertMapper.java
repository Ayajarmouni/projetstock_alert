package com.stockalert.alertes.mapper;

import com.stockalert.alertes.dto.StockAlertDto;
import com.stockalert.alertes.entite.StockAlert;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface StockAlertMapper {
    StockAlertDto toDto(StockAlert entity);
}
