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

    /**
     * Convertit une image en DTO.
     *
     * @param image image à convertir
     * @return DTO correspondant à l'image
     */
    public ImageDto toDto(Image image) {
        return new ImageDto(image.getId(), image.getEvent().getId(), image.getUrl(),
                image.isPreview());
    }

    /**
     * Convertit une liste d'images en liste de DTO.
     *
     * @param images images à convertir
     * @return la liste des DTO correspondants
     */
    public List<ImageDto> toDtoList(List<Image> images) {
        return images.stream().map(this::toDto).toList();
    }
}
