-- ==========================================================
-- SCRIPT DE POBLACIÓN DE DATOS (SEEDER) - GRUPO 16
-- ==========================================================

-- MÓDULO 1: IDENTIDAD
-- Inserción de Usuarios base (Se fuerza el ID para coincidir con el ejemplo)
INSERT INTO usuarios (id, nombre, apellido, telefono, email, password, rol_id) VALUES 
(1, 'Juan', 'Pérez', '71234567', 'juan.perez@gmail.com', '$2a$10$xyz', 3), -- Cliente del flujo
(2, 'Maria', 'Gomez', '72345678', 'maria.admin@chifones.com', '$2a$10$xyz', 1), -- Propietario
(3, 'Ana', 'Rios', '73456789', 'ana.sec@chifones.com', '$2a$10$xyz', 2) -- Secretaria
ON CONFLICT (id) DO NOTHING;
SELECT setval('usuarios_id_seq', (SELECT MAX(id) FROM usuarios));

-- MÓDULO 1: CATÁLOGO
-- Inserción de Productos (Torta de chocolate del ejemplo + Chifones peruanos)
INSERT INTO productos (id, nombre, descripcion, precio_unitario, disponible) VALUES 
(10, 'Torta de Chocolate', 'Deliciosa torta artesanal con cobertura de fudge y cacao al 70%', 100.00, TRUE),
(11, 'Chifón de Naranja Clásico', 'Auténtico chifón peruano, esponjoso y aromático, receta de la abuela', 45.00, TRUE),
(12, 'Chifón de Vainilla y Chispas', 'Chifón suave de vainilla con abundantes chispas de chocolate', 50.00, TRUE)
ON CONFLICT (id) DO NOTHING;
SELECT setval('productos_id_seq', (SELECT MAX(id) FROM productos));

-- MÓDULO 3: INSUMOS
-- Insumos básicos y para chifones
INSERT INTO insumos (id, nombre, unidad_medida, stock_actual, stock_minimo, costo_unitario) VALUES 
(100, 'Harina de trigo', 'g', 4000.000, 1000.000, 0.02), -- Saldo final tras el flujo (5000 inicial - 1000 consumidos)
(101, 'Azúcar', 'g', 2500.000, 500.000, 0.01),          -- Saldo final (3000 - 500)
(102, 'Cacao', 'g', 800.000, 200.000, 0.05),             -- Saldo final (1000 - 200)
(103, 'Huevos', 'unidad', 100.000, 20.000, 1.00),        -- Para chifones
(104, 'Naranja (Zumo y Ralladura)', 'unidad', 50.000, 10.000, 1.50), -- Para chifones
(105, 'Aceite vegetal', 'ml', 2000.000, 500.000, 0.015)  -- Clásico en chifones peruanos
ON CONFLICT (id) DO NOTHING;
SELECT setval('insumos_id_seq', (SELECT MAX(id) FROM insumos));

-- MÓDULO 3: RECETAS
INSERT INTO recetas (id, nombre, descripcion, producto_id) VALUES 
(50, 'Receta Torta de Chocolate', 'Receta estándar para tortas de chocolate del Grupo 16', 10),
(51, 'Receta Chifón de Naranja', 'Proporciones exactas para el chifón clásico peruano (molde 24)', 11)
ON CONFLICT (id) DO NOTHING;
SELECT setval('recetas_id_seq', (SELECT MAX(id) FROM recetas));

-- Detalle de Recetas
INSERT INTO receta_detalle (id, receta_id, insumo_id, cantidad) VALUES 
(200, 50, 100, 500.000), -- Torta: Harina
(201, 50, 101, 250.000), -- Torta: Azúcar
(202, 50, 102, 100.000), -- Torta: Cacao
(203, 51, 100, 250.000), -- Chifón: Harina
(204, 51, 101, 150.000), -- Chifón: Azúcar
(205, 51, 103, 6.000),   -- Chifón: 6 Huevos
(206, 51, 104, 2.000),   -- Chifón: 2 Naranjas
(207, 51, 105, 120.000)  -- Chifón: 120ml Aceite
ON CONFLICT (id) DO NOTHING;
SELECT setval('receta_detalle_id_seq', (SELECT MAX(id) FROM receta_detalle));

-- MÓDULO 4: GESTIÓN DE ENVASES
-- Mantenemos 2 envases clave: el molde especial de chifón y un tupper de entrega
INSERT INTO envases (id, nombre, descripcion, stock_total, stock_disponible) VALUES 
(1, 'Molde de Aluminio para Chifón N°24', 'Molde clásico con tubo central, indispensable para que el chifón no se baje', 50, 50),
(2, 'Tupper Domo Transparente Grande', 'Envase plástico retornable para entregar tortas enteras de forma segura', 30, 28) -- 2 prestados en los pedidos del ejemplo
ON CONFLICT (id) DO NOTHING;
SELECT setval('envases_id_seq', (SELECT MAX(id) FROM envases));


-- ==========================================================
-- TRANSACCIONES DEL FLUJO DE EJEMPLO (flujo_pedidos_ejemplo.md)
-- ==========================================================

-- CASO A: Pedido 1001 (Al Contado)
INSERT INTO pedidos (id, fecha, estado, total, usuario_id) VALUES 
(1001, '2026-05-05 10:00:00', 'entregado', 100.00, 1) ON CONFLICT (id) DO NOTHING;

INSERT INTO detalle_pedido (id, pedido_id, producto_id, cantidad, precio_unitario) VALUES 
(5001, 1001, 10, 1, 100.00) ON CONFLICT (id) DO NOTHING;

INSERT INTO pagos (id, fecha, tipo_pago, pedido_id) VALUES 
(8001, '2026-05-05 10:05:00', 'contado', 1001) ON CONFLICT (id) DO NOTHING;

INSERT INTO movimientos_insumo (id, fecha, tipo, cantidad, descripcion, insumo_id, pedido_id) VALUES 
(9001, '2026-05-05 10:00:00', 'consumo', -500.000, 'Descuento automático por pedido #1001', 100, 1001),
(9002, '2026-05-05 10:00:00', 'consumo', -250.000, 'Descuento automático por pedido #1001', 101, 1001),
(9003, '2026-05-05 10:00:00', 'consumo', -100.000, 'Descuento automático por pedido #1001', 102, 1001)
ON CONFLICT (id) DO NOTHING;

-- Envase prestado en Caso A
INSERT INTO pedido_envase (id, cantidad_prestada, cantidad_devuelta, fecha_devolucion, pedido_origen_id, pedido_devolucion_id, envase_id) VALUES 
(7001, 1, 0, NULL, 1001, NULL, 2) ON CONFLICT (id) DO NOTHING;


-- CASO B: Pedido 1002 (En Cuotas)
INSERT INTO pedidos (id, fecha, estado, total, usuario_id) VALUES 
(1002, '2026-05-06 15:00:00', 'pendiente', 100.00, 1) ON CONFLICT (id) DO NOTHING;

INSERT INTO detalle_pedido (id, pedido_id, producto_id, cantidad, precio_unitario) VALUES 
(5002, 1002, 10, 1, 100.00) ON CONFLICT (id) DO NOTHING;

INSERT INTO pagos (id, fecha, tipo_pago, pedido_id) VALUES 
(8002, '2026-05-06 15:05:00', 'cuotas', 1002) ON CONFLICT (id) DO NOTHING;

INSERT INTO cuotas (id, numero_cuota, monto_cuota, fecha_vencimiento, fecha_pago, pagado, pago_id) VALUES 
(3001, 1, 50.00, '2026-05-15', NULL, FALSE, 8002),
(3002, 2, 50.00, '2026-05-30', NULL, FALSE, 8002)
ON CONFLICT (id) DO NOTHING;

INSERT INTO movimientos_insumo (id, fecha, tipo, cantidad, descripcion, insumo_id, pedido_id) VALUES 
(9004, '2026-05-06 15:00:00', 'consumo', -500.000, 'Descuento automático por pedido #1002', 100, 1002),
(9005, '2026-05-06 15:00:00', 'consumo', -250.000, 'Descuento automático por pedido #1002', 101, 1002),
(9006, '2026-05-06 15:00:00', 'consumo', -100.000, 'Descuento automático por pedido #1002', 102, 1002)
ON CONFLICT (id) DO NOTHING;

-- Envase prestado en Caso B
INSERT INTO pedido_envase (id, cantidad_prestada, cantidad_devuelta, fecha_devolucion, pedido_origen_id, pedido_devolucion_id, envase_id) VALUES 
(7002, 1, 0, NULL, 1002, NULL, 2) ON CONFLICT (id) DO NOTHING;

-- Actualizar secuencias para futuras inserciones orgánicas
SELECT setval('pedidos_id_seq', (SELECT MAX(id) FROM pedidos));
SELECT setval('detalle_pedido_id_seq', (SELECT MAX(id) FROM detalle_pedido));
SELECT setval('pagos_id_seq', (SELECT MAX(id) FROM pagos));
SELECT setval('cuotas_id_seq', (SELECT MAX(id) FROM cuotas));
SELECT setval('movimientos_insumo_id_seq', (SELECT MAX(id) FROM movimientos_insumo));
SELECT setval('pedido_envase_id_seq', (SELECT MAX(id) FROM pedido_envase));
