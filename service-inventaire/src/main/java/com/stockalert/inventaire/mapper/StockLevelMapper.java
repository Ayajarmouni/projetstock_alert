package com.stockalert.inventaire.mapper;

import com.stockalert.inventaire.dto.StockLevelDto;
import com.stockalert.inventaire.entite.StockLevel;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface StockLevelMapper {
    StockLevel toEntity(StockLevelDto dto);

    StockLevelDto toDto(StockLevel entity);
}
