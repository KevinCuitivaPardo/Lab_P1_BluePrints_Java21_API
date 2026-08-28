package edu.eci.arsw.blueprints.filters;

import edu.eci.arsw.blueprints.model.Blueprint;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Default filter: returns the blueprint unchanged.
 * Activo solo cuando ningún perfil de filtro (redundancy, undersampling) está activo,
 * para evitar ambigüedad de beans al inyectar BlueprintsFilter en BlueprintsServices.
 */
@Component
@Profile("!redundancy & !undersampling")
public class IdentityFilter implements BlueprintsFilter {
    @Override
    public Blueprint apply(Blueprint bp) { return bp; }
}
