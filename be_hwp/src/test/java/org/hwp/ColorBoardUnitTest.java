package org.hwp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hwp.position.Position.posOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.hwp.ColorBoard.PositionColor;
import org.hwp.ColorBoard.PositionIterator;
import org.hwp.color.Color;
import org.hwp.color.ColorPersistenceService;
import org.hwp.position.Position;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest // only for simple inclusion into jacoco coverage measurement
class ColorBoardUnitTest {

    static Position getPositionN(int n) {
        return PositionIterator.getPositionStream().skip(n).findAny().orElseThrow();
    }

    @ParameterizedTest
    @MethodSource
    void test_getPositionN(int n, Position expectedPosition) {
        assertThat(getPositionN(n)).isEqualTo(expectedPosition);
    }

    static Stream<Arguments> test_getPositionN() {
        return Stream.of(
                Arguments.of(0, posOf(0, 0)) //
                , Arguments.of(1, posOf(0, 1)) //
                , Arguments.of(2, posOf(1, 1)) //
                , Arguments.of(3, posOf(1, 0)) //
                , Arguments.of(4, posOf(1, -1)) //
                , Arguments.of(5, posOf(0, -1)) //
                , Arguments.of(6, posOf(-1, -1)) //
                , Arguments.of(7, posOf(-1, 0)) //
                , Arguments.of(8, posOf(-1, 1)) //
                , Arguments.of(9, posOf(-1, 2)) //
                , Arguments.of(10, posOf(0, 2)) //
                , Arguments.of(20, posOf(-2, -2)) //
                , Arguments.of(21, posOf(-2, -1)) //
        );
    }

    @ParameterizedTest
    @MethodSource
    void test_boardInteraction_positions(int n, Collection<Integer> free) {

        var cps = mock(ColorPersistenceService.class);
        var color = new Color(1, 2, 3);
        when(cps.getColor()).thenReturn(color);

        var board = new ColorBoard(cps);
        for (int i = 0; i < n; i++) {
            board.addNewPositionColor("" + i);
        }
        for (var f : free) {
            board.remove("" + f);
        }

        List<Position> takenExpected = IntStream.range(0, n).filter(i -> !free.contains(i))
                .mapToObj(i -> getPositionN(i)).collect(Collectors.toList());

        var apc = board.getAllPositionsAndColors();
        assertThat(apc.stream().map(PositionColor::getPosition).collect(Collectors.toList()))
                .containsExactlyInAnyOrderElementsOf(takenExpected);
        assertThat(apc.stream().map(PositionColor::getColor))
                .allMatch(c -> c.equals(color));
        assertThat(apc.stream().map(PositionColor::getId)).containsExactlyInAnyOrderElementsOf(
                IntStream.range(0, n).filter(i -> !free.contains(i)).mapToObj(i -> "" + i)
                        .collect(Collectors.toList()));

        Position firstFree = getPositionN(free.stream().mapToInt(i -> i).min().orElse(n));
        takenExpected = new ArrayList<>(takenExpected);
        takenExpected.add(firstFree);

        board.addNewPositionColor("" + n);

        apc = board.getAllPositionsAndColors();
        assertThat(apc.stream().map(PositionColor::getPosition).collect(Collectors.toList()))
                .containsExactlyInAnyOrderElementsOf(takenExpected);
    }

    static Stream<Arguments> test_boardInteraction_positions() {
        return Stream.of(
                Arguments.of(0, List.of()) //
                , Arguments.of(1, List.of()) //
                , Arguments.of(2, List.of()) //
                , Arguments.of(2, List.of(0)) //
                , Arguments.of(3, List.of(0, 1, 2)) //
                , Arguments.of(4, List.of(3)) //
                , Arguments.of(25, List.of(3)) //
        );
    }

}
