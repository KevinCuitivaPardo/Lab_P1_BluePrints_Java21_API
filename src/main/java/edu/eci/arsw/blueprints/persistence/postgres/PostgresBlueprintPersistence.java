package edu.eci.arsw.blueprints.persistence.postgres;

import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import edu.eci.arsw.blueprints.persistence.BlueprintNotFoundException;
import edu.eci.arsw.blueprints.persistence.BlueprintPersistence;
import edu.eci.arsw.blueprints.persistence.BlueprintPersistenceException;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Persistencia respaldada por PostgreSQL (perfil "postgres").
 * El esquema (tablas blueprints/points) se crea vía init-db/init.sql al levantar el contenedor de Docker.
 */
@Repository
@Profile("postgres")
public class PostgresBlueprintPersistence implements BlueprintPersistence {

    private final JdbcTemplate jdbc;

    public PostgresBlueprintPersistence(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void saveBlueprint(Blueprint bp) throws BlueprintPersistenceException {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM blueprints WHERE author = ? AND name = ?",
                Integer.class, bp.getAuthor(), bp.getName());
        if (count != null && count > 0) {
            throw new BlueprintPersistenceException(
                    "Blueprint already exists: " + bp.getAuthor() + ":" + bp.getName());
        }
        Long blueprintId = jdbc.queryForObject(
                "INSERT INTO blueprints (author, name) VALUES (?, ?) RETURNING id",
                Long.class, bp.getAuthor(), bp.getName());
        insertPoints(blueprintId, bp.getPoints());
    }

    private void insertPoints(Long blueprintId, List<Point> points) {
        int order = 0;
        for (Point p : points) {
            jdbc.update("INSERT INTO points (blueprint_id, x, y, point_order) VALUES (?, ?, ?, ?)",
                    blueprintId, p.x(), p.y(), order++);
        }
    }

    @Override
    public Blueprint getBlueprint(String author, String name) throws BlueprintNotFoundException {
        Long id = findBlueprintId(author, name);
        return new Blueprint(author, name, loadPoints(id));
    }

    private Long findBlueprintId(String author, String name) throws BlueprintNotFoundException {
        try {
            return jdbc.queryForObject(
                    "SELECT id FROM blueprints WHERE author = ? AND name = ?",
                    Long.class, author, name);
        } catch (EmptyResultDataAccessException e) {
            throw new BlueprintNotFoundException("Blueprint not found: %s/%s".formatted(author, name));
        }
    }

    private List<Point> loadPoints(Long blueprintId) {
        return jdbc.query("SELECT x, y FROM points WHERE blueprint_id = ? ORDER BY point_order",
                (rs, rowNum) -> new Point(rs.getInt("x"), rs.getInt("y")), blueprintId);
    }

    @Override
    public Set<Blueprint> getBlueprintsByAuthor(String author) throws BlueprintNotFoundException {
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT id, name FROM blueprints WHERE author = ?", author);
        if (rows.isEmpty()) {
            throw new BlueprintNotFoundException("No blueprints for author: " + author);
        }
        Set<Blueprint> result = new HashSet<>();
        for (Map<String, Object> row : rows) {
            Long id = ((Number) row.get("id")).longValue();
            String name = (String) row.get("name");
            result.add(new Blueprint(author, name, loadPoints(id)));
        }
        return result;
    }

    @Override
    public Set<Blueprint> getAllBlueprints() {
        List<Map<String, Object>> rows = jdbc.queryForList("SELECT id, author, name FROM blueprints");
        Set<Blueprint> result = new HashSet<>();
        for (Map<String, Object> row : rows) {
            Long id = ((Number) row.get("id")).longValue();
            String author = (String) row.get("author");
            String name = (String) row.get("name");
            result.add(new Blueprint(author, name, loadPoints(id)));
        }
        return result;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void updateBlueprint(String author, String name, List<Point> points) throws BlueprintNotFoundException {
        Long id = findBlueprintId(author, name);
        jdbc.update("DELETE FROM points WHERE blueprint_id = ?", id);
        insertPoints(id, points == null ? List.of() : points);
    }

    @Override
    public void deleteBlueprint(String author, String name) throws BlueprintNotFoundException {
        Long id = findBlueprintId(author, name);
        jdbc.update("DELETE FROM blueprints WHERE id = ?", id); // points: ON DELETE CASCADE
    }

    @Override
    public void addPoint(String author, String name, int x, int y) throws BlueprintNotFoundException {
        Long id = findBlueprintId(author, name);
        Integer maxOrder = jdbc.queryForObject(
                "SELECT COALESCE(MAX(point_order), -1) FROM points WHERE blueprint_id = ?", Integer.class, id);
        jdbc.update("INSERT INTO points (blueprint_id, x, y, point_order) VALUES (?, ?, ?, ?)",
                id, x, y, maxOrder + 1);
    }
}
