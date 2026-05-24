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

    List<HistorialBusqueda> findByUsuario_Id(Long usuarioId);

    List<HistorialBusqueda> findByUsuario_IdOrderByFechaDesc(Long usuarioId);

    List<HistorialBusqueda> findTop10ByUsuario_IdOrderByFechaDesc(Long usuarioId);
}