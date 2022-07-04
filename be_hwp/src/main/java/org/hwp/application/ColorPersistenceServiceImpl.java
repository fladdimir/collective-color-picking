package org.hwp.application;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.Validator;

import org.hwp.domain.color.Color;
import org.hwp.domain.color.ColorConfig;
import org.hwp.domain.color.ColorPersistenceService;
import org.jboss.logging.Logger;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class ColorPersistenceServiceImpl implements ColorPersistenceService {

    final ColorConfig colorConfig;
    final ColorRepository colorRepository;
    final Validator validator;
    final ColorMapper colorMapper;
    final Logger logger;

    @Transactional
    public Color getColor() {
        return colorMapper.toDto(getOrCreateValid());
    }

    @Transactional
    public Color saveColor(@Valid Color colorDto) {
        var color = colorMapper.toEntity(colorDto);
        color.setId(getId());
        return colorMapper.toDto(colorRepository.save(color));
    }

    private ColorEntity getOrCreateValid() {

        var colorOpt = colorRepository.findById(getId());
        if (!colorOpt.isPresent()) {
            return initColor();
        }
        var color = colorOpt.get();

        var validations = validator.validate(color);
        if (!validations.isEmpty()) {
            logger.errorf("found invalid color in DB: %s, violations: %s", color, validations);
            colorRepository.delete(color);
            return initColor();
        }

        return color;
    }

    private ColorEntity initColor() {
        var color = new ColorEntity();
        color.setId(getId());
        return colorRepository.save(color);
    }

    private long getId() {
        return colorConfig.getColorId();
    }

    @Mapper(componentModel = "cdi")
    public static interface ColorMapper {

        Color toDto(ColorEntity color);

        @Mapping(target = "id", ignore = true)
        ColorEntity toEntity(Color colorDto);
    }
}
