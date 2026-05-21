import com.budgetoptimizer.budget_optimizer_backend.model.HistorialBusqueda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HistorialBusquedaRepository
        extends JpaRepository<HistorialBusqueda, Long> {

    // ==========================================
    // CONSULTAS POR USUARIO
    // ==========================================

    List<HistorialBusqueda> findByUsuarioId(Long usuarioId);

    List<HistorialBusqueda> findByUsuarioIdOrderByFechaDesc(Long usuarioId);

    List<HistorialBusqueda> findTop10ByUsuarioIdOrderByFechaDesc(Long usuarioId);
}