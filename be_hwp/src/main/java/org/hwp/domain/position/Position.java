package org.hwp.domain.position;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@RegisterForReflection
public class Position {

    private int x;
    private int y;

    public static Position posOf(int x, int y) {
        return new Position(x, y);
    }

    public Position add(Position other) {
        return new Position(this.x + other.x, this.y + other.y);
    }

}
