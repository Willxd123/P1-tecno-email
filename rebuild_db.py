import psycopg2

db_host = "tecnoweb.org.bo"
db_port = 5432
db_user = "grupo16sc"
db_pass = "grup016grup016*"
db_name = "db_grupo16sc"

try:
    conn = psycopg2.connect(
        host=db_host,
        port=db_port,
        user=db_user,
        password=db_pass,
        database=db_name
    )
    conn.autocommit = False
    cur = conn.cursor()
    
    print("Step 1: Truncating all tables with CASCADE to clean up all test data...")
    cur.execute("""
        TRUNCATE TABLE 
            cuotas, 
            pagos, 
            pedido_envase, 
            detalle_pedido, 
            movimientos_insumo, 
            pedidos, 
            cartillas,
            receta_detalle,
            recetas,
            productos,
            categoria_producto,
            usuarios,
            roles,
            insumos,
            envases
        RESTART IDENTITY CASCADE;
    """)
    print("Database truncated successfully!")
    
    print("\nStep 2: Seeding default clean master records...")
    
    # 2.1 Seed Roles
    cur.execute("INSERT INTO roles (id, nombre) VALUES (1, 'Propietario'), (2, 'Secretaria'), (3, 'Cliente');")
    
    # 2.2 Seed Categorías
    cur.execute("INSERT INTO categoria_producto (id, nombre) VALUES (1, 'Tradicional'), (2, 'Clásico');")
    
    # 2.3 Seed Usuarios
    # password is plain text in database
    cur.execute("""
        INSERT INTO usuarios (id, nombre, apellido, telefono, email, password, rol_id, activo) VALUES 
        (1, 'Juan', 'Pérez', '77012345', 'juan.perez@gmail.com', 'juan123', 3, true),
        (2, 'Maria', 'Gomez', '77012346', 'maria.admin@chifones.com', 'admin123', 1, true),
        (3, 'Ana', 'Rios', '77012347', 'ana.sec@chifones.com', 'secretaria123', 2, true),
        (4, 'Carlos', 'Mamani', '77012348', 'carlos@gmail.com', 'carlos123', 3, true),
        (9, 'Grace', 'Pariona', '77012349', 'tiffanypado@gmail.com', 'grace123', 3, true);
    """)
    
    # 2.4 Seed Productos
    cur.execute("""
        INSERT INTO productos (id, nombre, descripcion, precio_unitario, disponible, categoria_producto_id) VALUES 
        (10, 'Torta de Chocolate', 'Bizcocho húmedo de chocolate con cobertura de fudge', 100.00, true, 1),
        (11, 'Chifón de Naranja Clásico', 'Esponjoso chifón tradicional con zumo natural de naranja', 45.00, true, 2),
        (12, 'Chifón de Vainilla y Chispas', 'Esponjoso chifón de vainilla con chispas de chocolate', 50.00, true, 2),
        (13, 'Chifón Chocomani', 'Chifón de chocolate con trozos de maní tostado', 70.00, true, 1);
    """)
    
    # 2.5 Seed Insumos
    cur.execute("""
        INSERT INTO insumos (id, nombre, unidad_medida, stock_actual, stock_minimo, costo_unitario) VALUES 
        (100, 'Harina de trigo', 'g', 50000.000, 1000.000, 0.02),
        (101, 'Azúcar', 'g', 50000.000, 1000.000, 0.01),
        (102, 'Cacao en polvo', 'g', 10000.000, 500.000, 0.03),
        (103, 'Huevos', 'unidad', 3000.000, 100.000, 1.00),
        (104, 'Naranja (Zumo y Ralladura)', 'unidad', 500.000, 30.000, 1.50),
        (105, 'Aceite vegetal', 'ml', 5000.000, 200.000, 0.02),
        (106, 'Harina de arroz', 'g', 10000.000, 500.000, 0.01);
    """)
    
    # 2.6 Seed Recetas
    cur.execute("""
        INSERT INTO recetas (id, nombre, descripcion, producto_id) VALUES 
        (50, 'Receta Torta de Chocolate', 'Fórmula estándar de chocolate', 10),
        (51, 'Receta Chifón de Naranja', 'Fórmula clásica de naranja', 11),
        (52, 'Receta Chifón Chocomani', 'Fórmula de chocolate con maní', 13);
    """)
    
    # 2.7 Seed Receta Detalle
    cur.execute("""
        INSERT INTO receta_detalle (id, receta_id, insumo_id, cantidad) VALUES 
        (200, 50, 100, 500.000), -- Harina para Torta de Chocolate
        (201, 50, 101, 250.000), -- Azúcar para Torta de Chocolate
        (202, 50, 102, 100.000), -- Cacao para Torta de Chocolate
        (203, 51, 100, 250.000), -- Harina para Chifón Naranja
        (204, 51, 101, 150.000), -- Azúcar para Chifón Naranja
        (205, 51, 103, 6.000),   -- Huevos para Chifón Naranja
        (206, 51, 104, 2.000),   -- Naranjas para Chifón Naranja
        (207, 51, 105, 120.000), -- Aceite para Chifón Naranja
        (208, 52, 100, 200.000), -- Harina para Chocomani
        (209, 52, 101, 200.000), -- Azúcar para Chocomani
        (210, 52, 103, 6.000);   -- Huevos para Chocomani
    """)
    
    # 2.8 Seed Envases
    cur.execute("""
        INSERT INTO envases (id, nombre, descripcion, stock_total, stock_disponible) VALUES 
        (1, 'Molde de Aluminio para Chifón Nº24', 'Envase de metal para entrega', 50, 50),
        (2, 'Tupper Domo Transparente Grande', 'Envase plástico protector grande', 100, 100);
    """)
    
    # 2.9 Seed default active Cartillas for existing clients
    cur.execute("""
        INSERT INTO cartillas (usuario_id, estado, fecha_inicio) VALUES
        (1, 'activa', CURRENT_TIMESTAMP),
        (4, 'activa', CURRENT_TIMESTAMP),
        (9, 'activa', CURRENT_TIMESTAMP);
    """)
    
    print("Default master records seeded successfully!")
    
    print("\nStep 3: Resetting auto-increment sequences in PostgreSQL...")
    tables_with_sequences = [
        'roles', 'categoria_producto', 'usuarios', 'productos', 
        'insumos', 'recetas', 'receta_detalle', 'envases', 'cartillas'
    ]
    for table in tables_with_sequences:
        cur.execute(f"SELECT setval(pg_get_serial_sequence('{table}', 'id'), coalesce(max(id), 1)) FROM {table};")
        
    conn.commit()
    print("Database successfully rebuilt and seeded with clean data!")
    
    cur.close()
    conn.close()
except Exception as e:
    print("Error during database rebuild and seeding:", e)
