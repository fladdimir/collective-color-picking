package org.hwp;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.concat;
import static org.hwp.position.Position.posOf;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.enterprise.context.ApplicationScoped;
import javax.validation.Valid;

import org.hwp.color.Color;
import org.hwp.color.ColorPersistenceService;
import org.hwp.position.Position;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApplicationScoped
public class ColorBoard {

    @RegisterForReflection
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class PositionColor {
        private String id;
        private Position position;
        private Color color;
    }

    ColorBoard(ColorPersistenceService colorPersistenceService) {
        this.currentAvgColor = colorPersistenceService.getColor();
    }

    private final Map<String, PositionColor> positionColors = new ConcurrentHashMap<>();
    private Color currentAvgColor;

    public synchronized void addNewPositionColor(String id) {
        positionColors.put(id, new PositionColor(id, getFirstFreePosition(), currentAvgColor));
    }

    public synchronized void updateColor(String id, @Valid Color color) {
        positionColors.get(id).setColor(color);
        determineColor();
    }

    public synchronized void remove(String id) {
        positionColors.remove(id);
        determineColor();
    }

    public synchronized Collection<PositionColor> getAllPositionsAndColors() {
        return positionColors.values();
    }

    private void determineColor() {
        currentAvgColor = new Color(getAvg(Color::getR), getAvg(Color::getG), getAvg(Color::getB));
    }

    private int getAvg(ToIntFunction<Color> extractor) {
        return (int) Math
                .round(positionColors.values().stream().map(PositionColor::getColor).mapToInt(extractor)
                        .asDoubleStream().average()
                        .orElseGet(() -> extractor.applyAsInt(currentAvgColor)));
    }

    Position getFirstFreePosition() {
        Map<Integer, Set<Integer>> takenXY = positionColors.values().stream().map(PositionColor::getPosition)
                .collect(toMap(Position::getX, (Position p) -> Set.of(p.getY()),
                        (s1, s2) -> concat(s1.stream(), s2.stream()).collect(toSet())));

        Predicate<Position> isFree = pos -> takenXY.get(pos.getX()) == null
                || !takenXY.get(pos.getX()).contains(pos.getY());

        return PositionIterator.getPositionStream().filter(isFree).findAny().orElseThrow();
    }

    static class PositionIterator implements Iterator<Position> {
        // returns a spiral of positions

        static Stream<Position> getPositionStream() {
            Iterable<Position> iterable = PositionIterator::new;
            return StreamSupport.stream(iterable.spliterator(), false);
        }

        @Override
        public boolean hasNext() {
            return true;
        }

        private static final List<Position> DIRECTIONS = List.of(posOf(0, 1), posOf(1, 0), posOf(0, -1),
                posOf(-1, 0));

        Position currPos = posOf(0, -1); // next is (0,0)
        int stepsPerDir = 1;
        int currDirIdx = 0;
        int currStep = -1;

        @SuppressWarnings("squid:S2272")
        @Override
        public Position next() {

            var currentDirection = DIRECTIONS.get(currDirIdx);

            // step into current direction
            currPos = currPos.add(currentDirection);

            currStep++;
            // update direction if necessary
            if (currStep == stepsPerDir) {
                currStep = 0; // reset counter
                // change direction:
                if (currDirIdx < DIRECTIONS.size() - 1) {
                    currDirIdx++;
                } else {
                    currDirIdx = 0; // after 3 comes 0
                }
                // increase every two direction changes:
                if (currDirIdx % 2 == 0) {
                    stepsPerDir++;
                }
            }

            return currPos;
        }
    }

}
