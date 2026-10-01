package edu.eci.arsw.blueprints.persistence;

import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import java.util.List;
import java.util.Set;

public interface BlueprintPersistence {

    void saveBlueprint(Blueprint bp) throws BlueprintPersistenceException;

    Blueprint getBlueprint(String author, String name) throws BlueprintNotFoundException;

    Set<Blueprint> getBlueprintsByAuthor(String author) throws BlueprintNotFoundException;

    Set<Blueprint> getAllBlueprints();

    void updateBlueprint(String author, String name, List<Point> points) throws BlueprintNotFoundException;

    void deleteBlueprint(String author, String name) throws BlueprintNotFoundException;

    void addPoint(String author, String name, int x, int y) throws BlueprintNotFoundException;
}
