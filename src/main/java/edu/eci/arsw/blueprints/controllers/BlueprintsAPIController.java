package edu.eci.arsw.blueprints.controllers;

import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import edu.eci.arsw.blueprints.persistence.BlueprintNotFoundException;
import edu.eci.arsw.blueprints.persistence.BlueprintPersistenceException;
import edu.eci.arsw.blueprints.services.BlueprintsServices;
import edu.eci.arsw.blueprints.web.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/blueprints")
@Tag(name = "Blueprints", description = "Operaciones CRUD sobre planos (blueprints) y sus puntos")
public class BlueprintsAPIController {

    private final BlueprintsServices services;

    public BlueprintsAPIController(BlueprintsServices services) { this.services = services; }

    // GET /api/v1/blueprints
    @Operation(summary = "Listar todos los blueprints",
            description = "Retorna todos los blueprints registrados en el sistema.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Listado obtenido correctamente")
    @GetMapping
    public ResponseEntity<ApiResponse<Set<Blueprint>>> getAll() {
        Set<Blueprint> all = services.getAllBlueprints();
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "execute ok", all));
    }

    // GET /api/v1/blueprints/{author}
    @Operation(summary = "Obtener blueprints por autor",
            description = "Retorna todos los blueprints pertenecientes al autor indicado.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Blueprints encontrados"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "No existen blueprints para el autor")
    })
    @GetMapping("/{author}")
    public ResponseEntity<ApiResponse<Set<Blueprint>>> byAuthor(@PathVariable String author)
            throws BlueprintNotFoundException {
        Set<Blueprint> byAuthor = services.getBlueprintsByAuthor(author);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "execute ok", byAuthor));
    }

    // GET /api/v1/blueprints/{author}/{bpname}
    @Operation(summary = "Obtener un blueprint por autor y nombre",
            description = "Retorna un blueprint específico, aplicando el filtro activo (identity/redundancy/undersampling).")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Blueprint encontrado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Blueprint no encontrado")
    })
    @GetMapping("/{author}/{bpname}")
    public ResponseEntity<ApiResponse<Blueprint>> byAuthorAndName(@PathVariable String author,
                                                                    @PathVariable String bpname)
            throws BlueprintNotFoundException {
        Blueprint bp = services.getBlueprint(author, bpname);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "execute ok", bp));
    }

    // POST /api/v1/blueprints
    @Operation(summary = "Crear un nuevo blueprint",
            description = "Crea un blueprint con su lista inicial de puntos.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Blueprint creado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos inválidos o blueprint ya existente")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<Blueprint>> add(@Valid @RequestBody NewBlueprintRequest req)
            throws BlueprintPersistenceException {
        Blueprint bp = new Blueprint(req.author(), req.name(), req.points());
        services.addNewBlueprint(bp);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), "blueprint created", bp));
    }

    // PUT /api/v1/blueprints/{author}/{bpname}
    @Operation(summary = "Actualizar un blueprint",
            description = "Reemplaza la lista de puntos del blueprint indicado.")
    @PutMapping("/{author}/{bpname}")
    public ResponseEntity<ApiResponse<Void>> update(@PathVariable String author, @PathVariable String bpname,
                                                      @Valid @RequestBody UpdateBlueprintRequest req)
            throws BlueprintNotFoundException {
        services.updateBlueprint(author, bpname, req.points());
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "blueprint updated", null));
    }

    // DELETE /api/v1/blueprints/{author}/{bpname}
    @Operation(summary = "Eliminar un blueprint")
    @DeleteMapping("/{author}/{bpname}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String author, @PathVariable String bpname)
            throws BlueprintNotFoundException {
        services.deleteBlueprint(author, bpname);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "blueprint deleted", null));
    }

    // PUT /api/v1/blueprints/{author}/{bpname}/points
    @Operation(summary = "Agregar un punto a un blueprint",
            description = "Agrega un nuevo punto (x,y) al final del blueprint indicado.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "202", description = "Punto agregado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Blueprint no encontrado")
    })
    @PutMapping("/{author}/{bpname}/points")
    public ResponseEntity<ApiResponse<Void>> addPoint(@PathVariable String author, @PathVariable String bpname,
                                                        @Valid @RequestBody Point p)
            throws BlueprintNotFoundException {
        services.addPoint(author, bpname, p.x(), p.y());
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(new ApiResponse<>(HttpStatus.ACCEPTED.value(), "point added", null));
    }

    public record UpdateBlueprintRequest(@Valid List<Point> points) { }

    public record NewBlueprintRequest(
            @NotBlank String author,
            @NotBlank String name,
            @Valid List<Point> points
    ) { }
}
