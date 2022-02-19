package org.hwp.color;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Stream;

import org.hwp.QuarkusPostgresLocalOrTestContainerTestResource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@QuarkusTestResource(QuarkusPostgresLocalOrTestContainerTestResource.class)
class ColorControllerTest {

    @Test
    void test_PutGet() {

        var sentBody = new Color(96, 144, 192);

        var response = given().contentType("application/json").body(sentBody).when().put("/color");
        var responseBodyString = response.getBody().asString();
        assertThat(responseBodyString).isNotBlank();
        var putResponseBody = response.then()
                .statusCode(200).extract()
                .as(Color.class);

        assertThat(putResponseBody).isEqualTo(sentBody);

        var getResponseBody = given()
                .when().get("/color")
                .then()
                .statusCode(200)
                .extract().as(Color.class);

        assertThat(getResponseBody).isEqualTo(putResponseBody);
    }

    @MethodSource
    @ParameterizedTest
    void test_PutInvalid(Color invalidDto) {
        given().contentType("application/json").body(invalidDto).when().put("/color")
                .then()
                .statusCode(400);
    }

    static Stream<?> test_PutInvalid() {
        var invalidValues = List.of(-1, 256);
        var validValues = List.of(0, 255);
        return invalidValues.stream()
                .flatMap(invalid -> validValues.stream()
                        .flatMap(valid -> // invalid value on each possible position
                        List.of(List.of(invalid, valid, valid), List.of(valid, invalid, valid),
                                List.of(valid, valid, invalid)).stream().map(ColorControllerTest::create)));
    }

    private static Color create(List<Integer> args) {
        return new Color(args.get(0), args.get(1), args.get(2));
    }

}