package org.hwp.application;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.hwp.domain.color.ColorValidation.Rgb0255;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ColorEntity {

    @Id
    private Long id;

    @Rgb0255
    private int r = 255;
    @Rgb0255
    private int g = 0;
    @Rgb0255
    private int b = 0;

}
