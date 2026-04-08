-- Script de inicialización de categorías predeterminadas
-- IMPORTANTE: Verifica que el nombre de la tabla coincida con @Entity Categoria

-- ✅ Si tu entidad tiene @Table(name = "categorias"), usa este script
-- ⚠️ Si no especificaste @Table, la tabla podría llamarse "categoria" (sin s)

-- 1. ALIMENTACIÓN Y BEBIDAS
INSERT INTO categorias (nombre, descripcion, icono, color, tipo, activa) VALUES
('Alimentación', 'Compras de comida y supermercado', '🍔', '#FF6B6B', 'EXPENSE', true)
ON CONFLICT (nombre) DO NOTHING;

INSERT INTO categorias (nombre, descripcion, icono, color, tipo, activa) VALUES
('Restaurantes', 'Comidas fuera de casa y delivery', '🍽️', '#FF8787', 'EXPENSE', true)
ON CONFLICT (nombre) DO NOTHING;

-- 2. TRANSPORTE
INSERT INTO categorias (nombre, descripcion, icono, color, tipo, activa) VALUES
('Transporte', 'Taxi, metro, bus, gasolina', '🚗', '#4ECDC4', 'EXPENSE', true)
ON CONFLICT (nombre) DO NOTHING;

INSERT INTO categorias (nombre, descripcion, icono, color, tipo, activa) VALUES
('Vehículo', 'Mantenimiento, seguro, parqueadero', '🔧', '#45B7D1', 'EXPENSE', true)
ON CONFLICT (nombre) DO NOTHING;

-- 3. VIVIENDA
INSERT INTO categorias (nombre, descripcion, icono, color, tipo, activa) VALUES
('Vivienda', 'Alquiler o cuota hipotecaria', '🏠', '#20B2AA', 'EXPENSE', true)
ON CONFLICT (nombre) DO NOTHING;

INSERT INTO categorias (nombre, descripcion, icono, color, tipo, activa) VALUES
('Servicios', 'Luz, agua, gas, internet', '💡', '#3FBAA4', 'EXPENSE', true)
ON CONFLICT (nombre) DO NOTHING;

-- 4. ENTRETENIMIENTO
INSERT INTO categorias (nombre, descripcion, icono, color, tipo, activa) VALUES
('Entretenimiento', 'Cine, conciertos, eventos', '🎮', '#95E1D3', 'EXPENSE', true)
ON CONFLICT (nombre) DO NOTHING;

INSERT INTO categorias (nombre, descripcion, icono, color, tipo, activa) VALUES
('Suscripciones', 'Netflix, Spotify, apps', '📱', '#88D8C0', 'EXPENSE', true)
ON CONFLICT (nombre) DO NOTHING;

-- 5. SALUD Y BIENESTAR
INSERT INTO categorias (nombre, descripcion, icono, color, tipo, activa) VALUES
('Salud', 'Consultas médicas, medicinas', '⚕️', '#F38181', 'EXPENSE', true)
ON CONFLICT (nombre) DO NOTHING;

INSERT INTO categorias (nombre, descripcion, icono, color, tipo, activa) VALUES
('Gimnasio', 'Membresía y deporte', '💪', '#F08080', 'EXPENSE', true)
ON CONFLICT (nombre) DO NOTHING;

-- 6. EDUCACIÓN
INSERT INTO categorias (nombre, descripcion, icono, color, tipo, activa) VALUES
('Educación', 'Cursos, libros, formación', '📚', '#FFA07A', 'EXPENSE', true)
ON CONFLICT (nombre) DO NOTHING;

-- 7. COMPRAS PERSONALES
INSERT INTO categorias (nombre, descripcion, icono, color, tipo, activa) VALUES
('Ropa', 'Vestuario y accesorios', '👕', '#DDA15E', 'EXPENSE', true)
ON CONFLICT (nombre) DO NOTHING;

INSERT INTO categorias (nombre, descripcion, icono, color, tipo, activa) VALUES
('Belleza', 'Peluquería, productos de cuidado', '💄', '#F4ACB7', 'EXPENSE', true)
ON CONFLICT (nombre) DO NOTHING;

-- 8. TECNOLOGÍA
INSERT INTO categorias (nombre, descripcion, icono, color, tipo, activa) VALUES
('Tecnología', 'Electrónicos, software, gadgets', '💻', '#6C91BF', 'EXPENSE', true)
ON CONFLICT (nombre) DO NOTHING;

-- 9. VIAJES
INSERT INTO categorias (nombre, descripcion, icono, color, tipo, activa) VALUES
('Viajes', 'Vacaciones, hoteles, turismo', '✈️', '#9B72AA', 'EXPENSE', true)
ON CONFLICT (nombre) DO NOTHING;

-- 10. MASCOTAS
INSERT INTO categorias (nombre, descripcion, icono, color, tipo, activa) VALUES
('Mascotas', 'Comida, veterinario, accesorios', '🐾', '#C49A6C', 'EXPENSE', true)
ON CONFLICT (nombre) DO NOTHING;

-- 11. REGALOS Y DONACIONES
INSERT INTO categorias (nombre, descripcion, icono, color, tipo, activa) VALUES
('Regalos', 'Obsequios y donaciones', '🎁', '#E9967A', 'EXPENSE', true)
ON CONFLICT (nombre) DO NOTHING;

-- 12. SEGUROS
INSERT INTO categorias (nombre, descripcion, icono, color, tipo, activa) VALUES
('Seguros', 'Seguros de vida, hogar, salud', '🛡️', '#708090', 'EXPENSE', true)
ON CONFLICT (nombre) DO NOTHING;

-- 13. INGRESOS
INSERT INTO categorias (nombre, descripcion, icono, color, tipo, activa) VALUES
('Salario', 'Ingreso principal mensual', '💰', '#FFD700', 'EXPENSE', true)
ON CONFLICT (nombre) DO NOTHING;

INSERT INTO categorias (nombre, descripcion, icono, color, tipo, activa) VALUES
('Freelance', 'Trabajos independientes', '💼', '#F0E68C', 'EXPENSE', true)
ON CONFLICT (nombre) DO NOTHING;

-- 14. OTROS (COMODÍN PARA TODO)
INSERT INTO categorias (nombre, descripcion, icono, color, tipo, activa) VALUES
('Otros', 'Gastos varios no clasificados', '📦', '#AA96DA', 'BOTH', true)
ON CONFLICT (nombre) DO NOTHING;

-- Verificación: Consultar todas las categorías insertadas
-- SELECT id, nombre, tipo, activa FROM categorias ORDER BY id;