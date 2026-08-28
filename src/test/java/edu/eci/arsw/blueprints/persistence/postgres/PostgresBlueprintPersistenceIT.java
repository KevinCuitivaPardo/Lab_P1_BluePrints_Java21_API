package edu.eci.arsw.blueprints.persistence.postgres;

import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import edu.eci.arsw.blueprints.persistence.BlueprintNotFoundException;
import edu.eci.arsw.blueprints.persistence.BlueprintPersistence;
import edu.eci.arsw.blueprints.persistence.BlueprintPersistenceException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prueba de integración contra PostgreSQL real.
 * Requiere levantar la base de datos primero: {@code docker compose up -d}.
 * No se ejecuta con "mvn test"/"mvn install" por defecto (sufijo *IT, fuera de los patrones de Surefire);
 * para correrla explícitamente: {@code mvn -Dtest=PostgresBlueprintPersistenceIT test}.
 */
@SpringBootTest
@ActiveProfiles("postgres")
class PostgresBlueprintPersistenceIT {

    @Autowired
    private BlueprintPersistence persistence;

    @Test
    void seedDataIsReachable() throws BlueprintNotFoundException {
        Blueprint house = persistence.getBlueprint("john", "house");
        assertEquals("john", house.getAuthor());
        assertEquals(4, house.getPoints().size());
    }

    @Test
    void saveAndRetrieveNewBlueprint() throws BlueprintPersistenceException, BlueprintNotFoundException {
        String name = "it-" + UUID.randomUUID();
        Blueprint bp = new Blueprint("integration-test", name, List.of(new Point(1, 1), new Point(2, 2)));

        persistence.saveBlueprint(bp);
        Blueprint fetched = persistence.getBlueprint("integration-test", name);

        assertEquals(List.of(new Point(1, 1), new Point(2, 2)), fetched.getPoints());
    }

    @Test
    void addPointAppendsToExistingBlueprint() throws BlueprintPersistenceException, BlueprintNotFoundException {
        String name = "it-" + UUID.randomUUID();
        persistence.saveBlueprint(new Blueprint("integration-test", name, List.of(new Point(0, 0))));

        persistence.addPoint("integration-test", name, 9, 9);

        Blueprint fetched = persistence.getBlueprint("integration-test", name);
        assertTrue(fetched.getPoints().contains(new Point(9, 9)));
    }

    @Test
    void getBlueprintNotFoundThrows() {
        assertThrows(BlueprintNotFoundException.class,
                () -> persistence.getBlueprint("no-existe", "no-existe"));
    }
}
