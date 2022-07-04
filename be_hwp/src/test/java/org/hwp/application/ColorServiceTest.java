package org.hwp.application;

import static org.assertj.core.api.Assertions.assertThat;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import org.hwp.QuarkusPostgresLocalOrTestContainerTestResource;
import org.hwp.domain.color.Color;
import org.hwp.domain.color.ColorConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@QuarkusTestResource(QuarkusPostgresLocalOrTestContainerTestResource.class)
class ColorServiceTest {

    @Inject
    ColorRepository colorRepository;

    @Inject
    ColorPersistenceServiceImpl colorService;

    @Inject
    ColorConfig colorConfig;

    @Inject
    EntityManager entityManager;

    @BeforeEach
    @Transactional
    void beforeEach() {
        colorRepository.deleteAllInBatch();
        assertThat(colorRepository.count()).isZero();
    }

    @Test
    void test_getOrCreate_notPresent() {

        var color = colorService.getColor();

        assertThat(color).isEqualTo(new Color(255, 0, 0));
        assertThat(colorRepository.findAll()).hasSize(1);
    }

    @Test
    @Transactional
    void test_getOrCreate_notValid() {
        entityManager.createNativeQuery(
                "INSERT INTO colorentity (id, r, g, b) VALUES (1, 256, -1, -2)").executeUpdate();

        assertThat(colorRepository.findAll().get(0)).isEqualTo(new ColorEntity(1L, 256, -1, -2));

        var color = colorService.getColor();
        assertThat(color).isEqualTo(new Color(255, 0, 0));

        assertThat(colorRepository.count()).isEqualTo(1);
        assertThat(colorRepository.findAll().get(0)).isEqualTo(new ColorEntity(1L, 255, 0, 0));
    }

    @Test
    void test_getOrCreate_normal() {
        colorRepository.deleteAllInBatch();
        colorRepository.save(new ColorEntity(colorConfig.getColorId(), 0, 127, 255));

        var color = colorService.getColor();

        assertThat(color).isEqualTo(new Color(0, 127, 255));
        assertThat(colorRepository.count()).isEqualTo(1);
    }

}
