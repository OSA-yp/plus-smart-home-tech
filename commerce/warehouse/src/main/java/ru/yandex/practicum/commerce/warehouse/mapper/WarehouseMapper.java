package ru.yandex.practicum.commerce.warehouse.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.commerce.interaction.dto.warehouse.DimensionDto;
import ru.yandex.practicum.commerce.warehouse.entity.Dimension;

@Component
public class WarehouseMapper {

    public Dimension toEntity(DimensionDto dto) {
        if (dto == null) {
            return null;
        }
        Dimension dimension = new Dimension();
        dimension.setWidth(dto.getWidth());
        dimension.setHeight(dto.getHeight());
        dimension.setDepth(dto.getDepth());
        return dimension;
    }

    public DimensionDto toDto(Dimension dimension) {
        if (dimension == null) {
            return null;
        }
        DimensionDto dto = new DimensionDto();
        dto.setWidth(dimension.getWidth());
        dto.setHeight(dimension.getHeight());
        dto.setDepth(dimension.getDepth());
        return dto;
    }
}
