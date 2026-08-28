package edu.eci.arsw.blueprints.filters;

import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UndersamplingFilterTest {

    private final UndersamplingFilter filter = new UndersamplingFilter();

    @Test
    void keepsOnlyEvenIndexPoints() {
        Blueprint bp = new Blueprint("john", "under", List.of(
                new Point(0, 0), new Point(1, 1), new Point(2, 2), new Point(3, 3), new Point(4, 4)));

        Blueprint filtered = filter.apply(bp);

        assertEquals(List.of(new Point(0, 0), new Point(2, 2), new Point(4, 4)), filtered.getPoints());
    }

    @Test
    void leavesShortListsUnchanged() {
        Blueprint bp = new Blueprint("john", "short", List.of(new Point(0, 0), new Point(1, 1)));

        Blueprint filtered = filter.apply(bp);

        assertEquals(2, filtered.getPoints().size());
    }
}
