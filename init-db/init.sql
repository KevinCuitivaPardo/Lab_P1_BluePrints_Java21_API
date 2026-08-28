-- Esquema para la persistencia de Blueprints en PostgreSQL.
-- Se ejecuta automáticamente en el primer arranque del contenedor (docker-entrypoint-initdb.d).

CREATE TABLE IF NOT EXISTS blueprints (
    id     BIGSERIAL PRIMARY KEY,
    author VARCHAR(100) NOT NULL,
    name   VARCHAR(100) NOT NULL,
    UNIQUE (author, name)
);

CREATE TABLE IF NOT EXISTS points (
    id           BIGSERIAL PRIMARY KEY,
    blueprint_id BIGINT NOT NULL REFERENCES blueprints(id) ON DELETE CASCADE,
    x            INT NOT NULL,
    y            INT NOT NULL,
    point_order  INT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_points_blueprint_id ON points(blueprint_id);

-- Datos semilla (mismos datos de ejemplo que la persistencia en memoria)
INSERT INTO blueprints (author, name) VALUES ('john', 'house') ON CONFLICT DO NOTHING;
INSERT INTO blueprints (author, name) VALUES ('john', 'garage') ON CONFLICT DO NOTHING;
INSERT INTO blueprints (author, name) VALUES ('jane', 'garden') ON CONFLICT DO NOTHING;

INSERT INTO points (blueprint_id, x, y, point_order)
SELECT id, x, y, ord FROM blueprints,
    (VALUES (0,0,0), (10,0,1), (10,10,2), (0,10,3)) AS p(x,y,ord)
WHERE author='john' AND name='house';

INSERT INTO points (blueprint_id, x, y, point_order)
SELECT id, x, y, ord FROM blueprints,
    (VALUES (5,5,0), (15,5,1), (15,15,2)) AS p(x,y,ord)
WHERE author='john' AND name='garage';

INSERT INTO points (blueprint_id, x, y, point_order)
SELECT id, x, y, ord FROM blueprints,
    (VALUES (2,2,0), (3,4,1), (6,7,2)) AS p(x,y,ord)
WHERE author='jane' AND name='garden';
