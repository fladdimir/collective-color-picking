package org.hwp.interfaces;

import javax.validation.Valid;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

import org.hwp.application.ColorPersistenceServiceImpl;
import org.hwp.domain.color.Color;

import lombok.RequiredArgsConstructor;

@Path("/color")
@RequiredArgsConstructor
public class ColorController {

    final ColorPersistenceServiceImpl colorService;

    @GET
    public Color get() {
        return colorService.getColor();
    }

    @PUT
    public Color put(@Valid Color colorDto) {
        return colorService.saveColor(colorDto);
    }

}
