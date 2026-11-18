package com.stockalert.approvisionnement.mapper;

import com.stockalert.approvisionnement.dto.SupplierDto;
import com.stockalert.approvisionnement.entite.Supplier;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface SupplierMapper {
    Supplier toEntity(SupplierDto dto);

    SupplierDto toDto(Supplier entity);
}
