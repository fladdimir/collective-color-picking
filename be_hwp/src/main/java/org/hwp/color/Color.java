package org.hwp.color;

import org.hwp.color.ColorValidation.Rgb0255;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@RegisterForReflection
public class Color {

    @Rgb0255
    private int r;
    @Rgb0255
    private int g;
    @Rgb0255
    private int b;

}
