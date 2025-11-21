-- Script de inicialización de categorías predeterminadas
-- Ubicación: src/main/resources/data.sql

-- 20 categorías que cubren la mayoría de casos de uso
-- ⚠️ Ajusta el nombre de la tabla según tu @Entity Categoria
-- Si tu tabla se llama "categoria" (sin s), cambia todas las líneas

-- 1. ALIMENTACIÓN Y BEBIDAS
INSERT INTO categoria (nombre, descripcion, icono, color, tipo, activa) VALUES
('Alimentación', 'Compras de comida y supermercado', '🍔', '#FF6B6B', 'EXPENSE', true);

INSERT INTO categoria (nombre, descripcion, icono, color, tipo, activa) VALUES
('Restaurantes', 'Comidas fuera de casa y delivery', '🍽️', '#FF8787', 'EXPENSE', true);

-- 2. TRANSPORTE
INSERT INTO categoria (nombre, descripcion, icono, color, tipo, activa) VALUES
('Transporte', 'Taxi, metro, bus, gasolina', '🚗', '#4ECDC4', 'EXPENSE', true);

INSERT INTO categoria (nombre, descripcion, icono, color, tipo, activa) VALUES
('Vehículo', 'Mantenimiento, seguro, parqueadero', '🔧', '#45B7D1', 'EXPENSE', true);

-- 3. VIVIENDA
INSERT INTO categoria (nombre, descripcion, icono, color, tipo, activa) VALUES
('Vivienda', 'Alquiler o cuota hipotecaria', '🏠', '#20B2AA', 'EXPENSE', true);

INSERT INTO categoria (nombre, descripcion, icono, color, tipo, activa) VALUES
('Servicios', 'Luz, agua, gas, internet', '💡', '#3FBAA4', 'EXPENSE', true);

-- 4. ENTRETENIMIENTO
INSERT INTO categoria (nombre, descripcion, icono, color, tipo, activa) VALUES
('Entretenimiento', 'Cine, conciertos, eventos', '🎮', '#95E1D3', 'EXPENSE', true);

INSERT INTO categoria (nombre, descripcion, icono, color, tipo, activa) VALUES
('Suscripciones', 'Netflix, Spotify, apps', '📱', '#88D8C0', 'EXPENSE', true);

-- 5. SALUD Y BIENESTAR
INSERT INTO categoria (nombre, descripcion, icono, color, tipo, activa) VALUES
('Salud', 'Consultas médicas, medicinas', '⚕️', '#F38181', 'EXPENSE', true);

INSERT INTO categoria (nombre, descripcion, icono, color, tipo, activa) VALUES
('Gimnasio', 'Membresía y deporte', '💪', '#F08080', 'EXPENSE', true);

-- 6. EDUCACIÓN
INSERT INTO categoria (nombre, descripcion, icono, color, tipo, activa) VALUES
('Educación', 'Cursos, libros, formación', '📚', '#FFA07A', 'EXPENSE', true);

-- 7. COMPRAS PERSONALES
INSERT INTO categoria (nombre, descripcion, icono, color, tipo, activa) VALUES
('Ropa', 'Vestuario y accesorios', '👕', '#DDA15E', 'EXPENSE', true);

INSERT INTO categoria (nombre, descripcion, icono, color, tipo, activa) VALUES
('Belleza', 'Peluquería, productos de cuidado', '💄', '#F4ACB7', 'EXPENSE', true);

-- 8. TECNOLOGÍA
INSERT INTO categoria (nombre, descripcion, icono, color, tipo, activa) VALUES
('Tecnología', 'Electrónicos, software, gadgets', '💻', '#6C91BF', 'EXPENSE', true);

-- 9. VIAJES
INSERT INTO categoria (nombre, descripcion, icono, color, tipo, activa) VALUES
('Viajes', 'Vacaciones, hoteles, turismo', '✈️', '#9B72AA', 'EXPENSE', true);

-- 10. MASCOTAS
INSERT INTO categoria (nombre, descripcion, icono, color, tipo, activa) VALUES
('Mascotas', 'Comida, veterinario, accesorios', '🐾', '#C49A6C', 'EXPENSE', true);

-- 11. REGALOS Y DONACIONES
INSERT INTO categoria (nombre, descripcion, icono, color, tipo, activa) VALUES
('Regalos', 'Obsequios y donaciones', '🎁', '#E9967A', 'EXPENSE', true);

-- 12. SEGUROS
INSERT INTO categoria (nombre, descripcion, icono, color, tipo, activa) VALUES
('Seguros', 'Seguros de vida, hogar, salud', '🛡️', '#708090', 'EXPENSE', true);

-- 13. INGRESOS
INSERT INTO categoria (nombre, descripcion, icono, color, tipo, activa) VALUES
('Salario', 'Ingreso principal mensual', '💰', '#FFD700', 'INCOME', true);

INSERT INTO categoria (nombre, descripcion, icono, color, tipo, activa) VALUES
('Freelance', 'Trabajos independientes', '💼', '#F0E68C', 'INCOME', true);

-- 14. OTROS (COMODÍN PARA TODO)
INSERT INTO categoria (nombre, descripcion, icono, color, tipo, activa) VALUES
('Otros', 'Gastos varios no clasificados', '📦', '#AA96DA', 'BOTH', true);