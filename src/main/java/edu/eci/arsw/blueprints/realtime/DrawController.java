package edu.eci.arsw.blueprints.realtime;

import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import edu.eci.arsw.blueprints.persistence.BlueprintNotFoundException;
import edu.eci.arsw.blueprints.persistence.BlueprintPersistenceException;
import edu.eci.arsw.blueprints.services.BlueprintsServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class DrawController {

    private static final Logger log = LoggerFactory.getLogger(DrawController.class);

    private static final int MAX_COORD = 10_000;

    public record DrawMessage(String author, String name, Point point) { }

    public record BlueprintUpdate(String author, String name, List<Point> points) { }

    private final BlueprintsServices services;
    private final SimpMessagingTemplate template;

    public DrawController(BlueprintsServices services, SimpMessagingTemplate template) {
        this.services = services;
        this.template = template;
    }

    /** Persiste el punto (creando el plano si no existe) y reenvía el plano completo a todos los suscriptores. */
    @MessageMapping("/draw")
    public void draw(DrawMessage msg) throws BlueprintNotFoundException, BlueprintPersistenceException {
        if (msg == null || msg.point() == null || isBlank(msg.author()) || isBlank(msg.name())
                || msg.author().length() > 100 || msg.name().length() > 100
                || Math.abs(msg.point().x()) > MAX_COORD || Math.abs(msg.point().y()) > MAX_COORD) {
            log.warn("draw ignorado: payload inválido {}", msg);
            return;
        }
        try {
            services.addPoint(msg.author(), msg.name(), msg.point().x(), msg.point().y());
        } catch (BlueprintNotFoundException e) {
            services.addNewBlueprint(new Blueprint(msg.author(), msg.name(), List.of(msg.point())));
        }
        Blueprint bp = services.getBlueprint(msg.author(), msg.name());
        log.info("draw {}/{} -> {} puntos", msg.author(), msg.name(), bp.getPoints().size());
        template.convertAndSend("/topic/blueprints.%s.%s".formatted(msg.author(), msg.name()),
                new BlueprintUpdate(bp.getAuthor(), bp.getName(), bp.getPoints()));
    }

    private static boolean isBlank(String s) { return s == null || s.isBlank(); }
}
