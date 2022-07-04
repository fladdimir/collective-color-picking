package org.hwp.domain.color;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import lombok.Getter;

@ApplicationScoped
public class ColorConfig {

    @Getter
    @ConfigProperty(name = "x.color.id", defaultValue = "1")
    long colorId;

}
