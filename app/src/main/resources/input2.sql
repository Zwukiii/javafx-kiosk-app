------------------------------------------------------------
-- 1. Clean the database (drop all tables and enum types)
------------------------------------------------------------

DO
$$
    DECLARE
        r RECORD;
    BEGIN
        FOR r IN (SELECT tablename FROM pg_tables WHERE schemaname = 'public')
            LOOP
                EXECUTE 'DROP TABLE IF EXISTS "' || r.tablename || '" CASCADE';
            END LOOP;
    END;
$$;
------------------------------------------------------------
-- 3. Create tables (ingredients, products, meals, discount codes)
------------------------------------------------------------
-- Toppings / ingredients
CREATE TABLE ingredient
(
    id    BIGSERIAL PRIMARY KEY,
    name  VARCHAR(255),
    price DECIMAL(19, 2),
    image VARCHAR(255)
);

-- Side items & drinks
CREATE TABLE additional_product
(
    id    BIGSERIAL PRIMARY KEY,
    name  VARCHAR(255),
    price DECIMAL(19, 2),
    image VARCHAR(255),
    type  VARCHAR(50),
    size  VARCHAR(50)
);

-- Core menu items (pizzas, burgers, platters, etc.)
CREATE TABLE basic_product
(
    id    BIGSERIAL PRIMARY KEY,
    name  VARCHAR(255),
    price NUMERIC(19, 2),
    image VARCHAR(255),
    type  VARCHAR(50),
    size  VARCHAR(50)
);


-- Link toppings to pizzas / platters
CREATE TABLE basic_product_ingredient
(
    basic_product_id BIGINT REFERENCES basic_product (id) ON DELETE CASCADE,
    ingredient_id    BIGINT REFERENCES ingredient (id) ON DELETE CASCADE,
    PRIMARY KEY (basic_product_id, ingredient_id)
);

-- Meal bundles (one core item + multiple side/drink items)
CREATE TABLE meal
(
    id               BIGSERIAL PRIMARY KEY,
    name             VARCHAR(255)   NOT NULL,
    image            VARCHAR(255),
    price            NUMERIC(19, 2) NOT NULL,
    basic_product_id BIGINT REFERENCES basic_product (id) ON DELETE CASCADE
);

CREATE TABLE meal_additional_product
(
    meal_id               BIGINT REFERENCES meal (id) ON DELETE CASCADE,
    additional_product_id BIGINT REFERENCES additional_product (id) ON DELETE CASCADE,
    PRIMARY KEY (meal_id, additional_product_id)
);


CREATE TABLE rabat_code
(
    id         SERIAL PRIMARY KEY,
    code       VARCHAR(255)     NOT NULL,
    rabat      DOUBLE PRECISION NOT NULL,
    start_time TIMESTAMP        NOT NULL,
    end_time   TIMESTAMP        NOT NULL
);

-- Discount codes
-- recommended Items
CREATE TABLE recommended_products
(
    product_id    SERIAL PRIMARY KEY,
    product_name  VARCHAR(100)   NOT NULL,
    product_price NUMERIC(19, 2) NOT NULL
);

------------------------------------------------------------
-- 4. Insert sample discount codes
------------------------------------------------------------
INSERT INTO rabat_code (code, rabat, start_time, end_time)
VALUES ('CODE1', 15.0, '2025-04-01 00:00:00', '2025-06-30 23:59:59'),
       ('CODE2', 20.0, '2025-01-01 00:00:00', '2025-04-30 23:59:59');

------------------------------------------------------------
-- 5. Insert 15 additional products (drinks, sides, platters, dessert)
------------------------------------------------------------
INSERT INTO additional_product (name, price, image, type, size)
VALUES ('Beer 0.3L', 4.50, 'bear.jpg', 'DRINK', 'SMALL'),
       ('Beer 0.5L', 6.50, 'bear.jpg', 'DRINK', 'MEDIUM'),
       ('Aperol Spritz 0.2L', 7.00, 'aperol.jpg', 'DRINK', 'SMALL'),
       ('Aperol Spritz 0.4L', 12.00, 'aperol.jpg', 'DRINK', 'LARGE'),
       ('Glass of Red Wine', 6.50, 'wine_red_glass.jpg', 'DRINK', 'GLASS'),
       ('Glass of White Wine', 6.00, 'wine_white_glass.jpg', 'DRINK', 'GLASS'),
       ('Bottle of Red Wine', 28.00, 'wine_red_bottle.jpg', 'DRINK', 'BOTTLE'),
       ('Bottle of White Wine', 25.00, 'wine_white_bottle.jpg', 'DRINK', 'BOTTLE'),
       ('Large Fries', 5.99, 'fries_large.jpg', 'FRIES', 'LARGE'),
       ('Small Fries', 3.99, 'fries_small.jpg', 'FRIES', 'SMALL'),
       ('Sparkling Water 0.5L', 3.00, 'sparkling_water.jpg', 'DRINK', 'MEDIUM');

------------------------------------------------------------
-- 6. Insert 20 pizza toppings (ingredients)
------------------------------------------------------------
INSERT INTO ingredient (name, price, image)
VALUES ('Tomato', 0.50, 'tomato.png'),
       ('Mozzarella', 1.20, 'mozzarella.png'),
       ('Prosciutto', 1.80, 'prosciutto.png'),
       ('Mushrooms', 0.70, 'mushrooms.png'),
       ('Olives', 0.60, 'olives.png'),
       ('Onion', 0.40, 'onion.png'),
       ('Bell Pepper', 0.65, 'bell_pepper.png'),
       ('Basil', 0.30, 'basil.png'),
       ('Arugula', 0.90, 'arugula.png'),
       ('Spinach', 0.80, 'spinach.png'),
       ('Artichoke', 1.00, 'artichoke.png'),
       ('Pineapple', 0.75, 'pineapple.png'),
       ('Ham', 1.50, 'ham.png'),
       ('Pepperoni', 1.70, 'pepperoni.png'),
       ('Bacon', 1.60, 'bacon.png'),
       ('Italian Sausage', 1.85, 'sausage.png'),
       ('Goat Cheese', 1.40, 'goat_cheese.png'),
       ('Anchovies', 1.30, 'anchovies.png'),
       ('Jalapeños', 0.55, 'jalapenos.png'),
       ('Parmesan', 1.25, 'parmesan.png');

------------------------------------------------------------
-- 7. Insert 5 pizzas into basic_product
------------------------------------------------------------
INSERT INTO basic_product (name, price, image, type, size)
VALUES ('Margherita', 9.99, 'Margherita.png', 'PIZZA', 'LARGE'),
       ('Salami', 11.49, 'salami.png', 'PIZZA', 'LARGE'),
       ('Hawaiian', 10.99, 'hawaii.png', 'PIZZA', 'LARGE'),
       ('Quattro Formaggi', 12.99, 'quattro-formagi.png', 'PIZZA', 'LARGE'),
       ('Veggie', 10.49, 'veggiePizza.png', 'PIZZA', 'LARGE');

-- 7.1 Insert Cheese & Ham Platter as a core product for meals
INSERT INTO basic_product (name, price, image, type, size)
VALUES ('Cheese & Ham Platter', 27.98, 'cheese_ham_platter.jpg', 'APPETIZER', 'PLATTER'),
       ('Italian Ham Plate', 14.99, 'ham_plate.jpg', 'APPETIZER', 'PLATTER'),
       ('Cheese Plate', 12.99, 'cheese_plate.jpg', 'APPETIZER', 'PLATTER'),
       ('Garlic Bread', 4.50, 'garlic_bread.jpg', 'APPETIZER', 'MEDIUM'),
       ('Tiramisu Slice', 6.00, 'tiramisu.jpg', 'APPETIZER', 'ONESIZE');

------------------------------------------------------------
-- 8. Map each pizza to its toppings
------------------------------------------------------------
-- Margherita
INSERT INTO basic_product_ingredient (basic_product_id, ingredient_id)
SELECT bp.id, i.id
FROM basic_product bp
         JOIN ingredient i ON i.name IN ('Tomato', 'Mozzarella', 'Basil')
WHERE bp.name = 'Margherita';

-- Pepperoni
INSERT INTO basic_product_ingredient (basic_product_id, ingredient_id)
SELECT bp.id, i.id
FROM basic_product bp
         JOIN ingredient i ON i.name IN ('Tomato', 'Mozzarella', 'Pepperoni')
WHERE bp.name = 'Salami';

-- Hawaiian
INSERT INTO basic_product_ingredient (basic_product_id, ingredient_id)
SELECT bp.id, i.id
FROM basic_product bp
         JOIN ingredient i ON i.name IN ('Tomato', 'Mozzarella', 'Ham', 'Pineapple')
WHERE bp.name = 'Hawaiian';

-- Four Cheese
INSERT INTO basic_product_ingredient (basic_product_id, ingredient_id)
SELECT bp.id, i.id
FROM basic_product bp
         JOIN ingredient i ON i.name IN ('Mozzarella', 'Goat Cheese', 'Parmesan')
WHERE bp.name = 'Quattro Formaggi';

-- Veggie
INSERT INTO basic_product_ingredient (basic_product_id, ingredient_id)
SELECT bp.id, i.id
FROM basic_product bp
         JOIN ingredient i
              ON i.name IN ('Tomato', 'Mozzarella', 'Mushrooms', 'Olives', 'Bell Pepper', 'Onion', 'Spinach')
WHERE bp.name = 'Veggie';

------------------------------------------------------------
-- 9. Define meals (include image, computed price, and side items)
------------------------------------------------------------
-- 9.1 Aperitiv Happy Hours: Cheese & Ham Platter + 2 Aperols
INSERT INTO meal (name, image, price, basic_product_id)
VALUES ('Aperitiv Happy Hours',
        'meal1.jpg',
        (SELECT bp.price FROM basic_product bp WHERE bp.name = 'Cheese & Ham Platter')
            + (SELECT ap.price FROM additional_product ap WHERE ap.name = 'Aperol Spritz 0.2L')
            + (SELECT ap.price FROM additional_product ap WHERE ap.name = 'Aperol Spritz 0.4L'),
        (SELECT bp.id FROM basic_product bp WHERE bp.name = 'Cheese & Ham Platter'));

INSERT INTO meal_additional_product (meal_id, additional_product_id)
SELECT m.id, ap.id
FROM meal m
         JOIN additional_product ap
              ON ap.name IN ('Aperol Spritz 0.2L', 'Aperol Spritz 0.4L')
WHERE m.name = 'Aperitiv Happy Hours';

-- 9.2 Margherita & Sip: Margherita + Aperol + Glass of Red Wine
INSERT INTO meal (name, image, price, basic_product_id)
VALUES ('Margherita & Sip',
        'meal2.jpg',
        (SELECT bp.price FROM basic_product bp WHERE bp.name = 'Margherita')
            + (SELECT ap.price FROM additional_product ap WHERE ap.name = 'Aperol Spritz 0.4L')
            + (SELECT ap.price FROM additional_product ap WHERE ap.name = 'Glass of Red Wine'),
        (SELECT bp.id FROM basic_product bp WHERE bp.name = 'Margherita'));

INSERT INTO meal_additional_product (meal_id, additional_product_id)
SELECT m.id, ap.id
FROM meal m
         JOIN additional_product ap
              ON ap.name IN ('Aperol Spritz 0.4L', 'Glass of Red Wine')
WHERE m.name = 'Margherita & Sip';



CREATE TABLE orders
(
    id                       BIGSERIAL PRIMARY KEY,
    order_id                 VARCHAR(50)      NOT NULL,
    total_price              NUMERIC(19, 2)   NOT NULL,
    promo_code               DOUBLE PRECISION NOT NULL,
    created_at               TIMESTAMP        NOT NULL,
    ordered_products_summary JSONB            NOT NULL,
    dine_type                VARCHAR(50)
);

INSERT INTO orders (
    order_id,
    total_price,
    promo_code,
    created_at,
    ordered_products_summary,
    dine_type
)
VALUES
    ('K5GYS2J7',
     29.98,
     0.0,
     '2025-05-01 10:15:00',
     '[
       {
         "name": "Fries",
         "price": 5.00,
         "type": "ADDITIONAL_PRODUCT",
         "size": "M",
         "quantity": 2
       },
       {
         "name": "Cola",
         "price": 8.99,
         "type": "ADDITIONAL_PRODUCT",
         "size": "S",
         "quantity": 2
       }
     ]'::jsonb,
     'Eat here'),

    ('K21YS2J7',
     24.49,
     10.0,
     '2025-05-02 14:20:00',
     '[
       {
         "name": "Margherita Pizza",
         "price": 15.00,
         "type": "BASIC_PRODUCT",
         "size": "M",
         "quantity": 2
       },
       {
         "name": "Garlic Bread",
         "price": 4.99,
         "type": "ADDITIONAL_PRODUCT",
         "size": "S",
         "quantity": 2
       },
       {
         "name": "Coke",
         "price": 4.50,
         "type": "ADDITIONAL_PRODUCT",
         "size": "S",
         "quantity": 2
       }
     ]'::jsonb,
     'Takeaway'),

    ('K51YS212',
     12.99,
     0.0,
     '2025-05-03 09:05:00',
     '[
       {
         "name": "Caesar Salad",
         "price": 8.99,
         "type": "BASIC_PRODUCT",
         "size": "M",
         "quantity": 1
       },
       {
         "name": "Water",
         "price": 4.00,
         "type": "ADDITIONAL_PRODUCT",
         "size": "S",
         "quantity": 1
       }
     ]'::jsonb,
     'Takeaway'),

    ('F5GYS2J7',
     45.97,
     20.0,
     '2025-05-04 18:45:00',
     '[
       {
         "name": "Family Meal",
         "price": 30.00,
         "type": "BASIC_PRODUCT",
         "size": "XL",
         "quantity": 1
       },
       {
         "name": "Extra Chicken",
         "price": 10.00,
         "type": "ADDITIONAL_PRODUCT",
         "size": "L",
         "quantity": 1
       },
       {
         "name": "Dessert",
         "price": 5.97,
         "type": "ADDITIONAL_PRODUCT",
         "size": "S",
         "quantity": 1
       }
     ]'::jsonb,
     'Eat here'),

    ('A5GYS127',
     9.99,
     0.0,
     '2025-05-05 13:15:00',
     '[
       {
         "name": "Coffee",
         "price": 3.50,
         "type": "BASIC_PRODUCT",
         "size": "S",
         "quantity": 1
       },
       {
         "name": "Muffin",
         "price": 6.49,
         "type": "ADDITIONAL_PRODUCT",
         "size": "M",
         "quantity": 1
       }
     ]'::jsonb,
     'Takeaway');