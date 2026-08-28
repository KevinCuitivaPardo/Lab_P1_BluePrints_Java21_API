package edu.eci.arsw.blueprints.filters;

import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RedundancyFilterTest {

    private final RedundancyFilter filter = new RedundancyFilter();

    @Test
    void removesConsecutiveDuplicatePoints() {
        Blueprint bp = new Blueprint("john", "dup", List.of(
                new Point(1, 1), new Point(1, 1), new Point(2, 2), new Point(2, 2), new Point(3, 3)));

        Blueprint filtered = filter.apply(bp);

        assertEquals(List.of(new Point(1, 1), new Point(2, 2), new Point(3, 3)), filtered.getPoints());
    }

    @Test
    void keepsNonConsecutiveDuplicates() {
        Blueprint bp = new Blueprint("john", "house", List.of(
                new Point(0, 0), new Point(1, 1), new Point(0, 0)));

        Blueprint filtered = filter.apply(bp);

        assertEquals(3, filtered.getPoints().size());
    }

    @Test
    void handlesEmptyPointsList() {
        Blueprint bp = new Blueprint("john", "empty", List.of());

        Blueprint filtered = filter.apply(bp);

        assertEquals(List.of(), filtered.getPoints());
    }
}
