package fr.CDA.GHE.mapper;

import fr.CDA.GHE.dto.ImageDto;
import fr.CDA.GHE.entity.Image;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Convertit l'entité {@link Image} vers son DTO d'exposition.
 */
@Component
public class ImageMapper {

    public ImageDto toDto(Image image) {
        return new ImageDto(image.getId(), image.getEvent().getId());
    }

    public List<ImageDto> toDtoList(List<Image> images) {
        return images.stream().map(this::toDto).toList();
    }
}
