------------------------------------------------------------
-- 1. CleanDB
------------------------------------------------------------
DO $$
    DECLARE
        r RECORD;
    BEGIN
        FOR r IN (SELECT tablename FROM pg_tables WHERE schemaname = 'public') LOOP
                EXECUTE 'DROP TABLE IF EXISTS "' || r.tablename || '" CASCADE';
            END LOOP;
    END;
$$;

------------------------------------------------------------
-- Create tabeles
------------------------------------------------------------
--ingredient
CREATE TABLE ingredient (
    id    BIGSERIAL PRIMARY KEY,
    name  VARCHAR(255),
    price DECIMAL(19, 2),
    image VARCHAR(255)
);
--Additional
CREATE TABLE additional_product (
    id    BIGSERIAL PRIMARY KEY,
    name  VARCHAR(255),
    price DECIMAL(19, 2),
    image VARCHAR(255),
    type  VARCHAR(50),
    size  VARCHAR(50)
);
--Basic 
CREATE TABLE basic_product (
    id    BIGSERIAL PRIMARY KEY,
    name  VARCHAR(255),
    price NUMERIC(19, 2),
    image VARCHAR(255),
    type  VARCHAR(50),
    size  VARCHAR(50)
);

CREATE TABLE basic_product_ingredient (
    basic_product_id BIGINT REFERENCES basic_product(id)  ON DELETE CASCADE,
    ingredient_id    BIGINT REFERENCES ingredient(id)     ON DELETE CASCADE,
    PRIMARY KEY (basic_product_id, ingredient_id)
);

--MEAL 
CREATE TABLE meal (
    id               BIGSERIAL PRIMARY KEY,
    name             VARCHAR(255)      NOT NULL,
    price            NUMERIC(19, 2)    NOT NULL,
    basic_product_id BIGINT REFERENCES basic_product(id) ON DELETE CASCADE
);

CREATE TABLE meal_additional_product (
    meal_id              BIGINT REFERENCES meal(id)              ON DELETE CASCADE,
    additional_product_id BIGINT REFERENCES additional_product(id) ON DELETE CASCADE,
    PRIMARY KEY (meal_id, additional_product_id)
);


CREATE TABLE recomended_products (
    product_id  SERIAL PRIMARY KEY,
    product_name varchar(100) NOT NULL

);





------------------------------------------------------------
--  additional_products 
------------------------------------------------------------
INSERT INTO additional_product (name, price, image, type, size) VALUES
    ('Small Fries',        5.99, 'small_fries.jpg',      'FOOD',  'SMALL'),
    ('Medium Fries',       7.99, 'medium_fries.jpg',     'FOOD',  'MEDIUM'),
    ('Large Fries',        9.99, 'large_fries.jpg',      'FOOD',  'LARGE'),
    ('Cola 0.3L',          4.50, 'cola_03.jpg',          'DRINK', 'SMALL'),
    ('Cola 0.5L',          6.00, 'cola_05.jpg',          'DRINK', 'MEDIUM'),
    ('Cola 1L',            8.00, 'cola_1l.jpg',          'DRINK', 'LARGE'),
    ('Still Water 0.5L',   3.50, 'still_water_05.jpg',   'DRINK', 'MEDIUM');

------------------------------------------------------------
--  ingredients
------------------------------------------------------------
INSERT INTO ingredient (name, price, image) VALUES
    ('Tomato',        0.50, 'tomato.png'),
    ('Cheese',        1.00, 'cheese.png'),
    ('Bacon',         1.50, 'bacon.png'),
    ('Lettuce',       0.70, 'lettuce.png'),
    ('Onion',         0.40, 'onion.png'),
    ('Pickles',       0.60, 'pickles.png'),
    ('Mayo',          0.30, 'mayo.png'),
    ('Ketchup',       0.30, 'ketchup.png'),
    ('Beef Patty',    2.50, 'beef_patty.png'),
    ('Chicken Patty', 2.00, 'chicken_patty.png');

------------------------------------------------------------
--  basic_products
------------------------------------------------------------
INSERT INTO basic_product (name, price, image, type, size) VALUES
    -- id will autogenerate (1…)
    ('Classic Beef Burger',   12.99, 'beef_burger.jpg',   'FOOD',  'REGULAR'),
    ('Chicken Burger',        11.49, 'chicken_burger.jpg','FOOD',  'REGULAR'),
    ('Veggie Burger',         10.99, 'veggie_burger.jpg', 'FOOD',  'REGULAR');



-- Classic Beef Burger  (id = 1)
INSERT INTO basic_product_ingredient (basic_product_id, ingredient_id)
SELECT bp.id, i.id
FROM   basic_product bp
JOIN   ingredient i ON i.name IN ('Beef Patty','Cheese','Tomato','Lettuce','Onion','Ketchup')
WHERE  bp.name = 'Classic Beef Burger';

-- Chicken Burger      (id = 2)
INSERT INTO basic_product_ingredient (basic_product_id, ingredient_id)
SELECT bp.id, i.id
FROM   basic_product bp
JOIN   ingredient i ON i.name IN ('Chicken Patty','Cheese','Tomato','Lettuce','Mayo')
WHERE  bp.name = 'Chicken Burger';

-- Veggie Burger       (id = 3)
INSERT INTO basic_product_ingredient (basic_product_id, ingredient_id)
SELECT bp.id, i.id
FROM   basic_product bp
JOIN   ingredient i ON i.name IN ('Tomato','Lettuce','Onion','Pickles','Cheese')
WHERE  bp.name = 'Veggie Burger';



/* 4-a  Meals themselves (price already includes side-items) */
INSERT INTO meal (name, price, basic_product_id) VALUES
    ('Classic Beef Combo (S-Fries + 0.3 L Cola)', 18.99,
        (SELECT id FROM basic_product WHERE name = 'Classic Beef Burger')),
    ('Chicken Combo (M-Fries + 0.5 L Cola)',      19.49,
        (SELECT id FROM basic_product WHERE name = 'Chicken Burger')),
    ('Veggie Combo (L-Fries + 0.5 L Still Water)',17.99,
        (SELECT id FROM basic_product WHERE name = 'Veggie Burger'));

/* 4-b  Map each meal to its side items */
-- Classic Beef Combo  → Small Fries + Cola 0.3 L
INSERT INTO meal_additional_product (meal_id, additional_product_id)
SELECT m.id, ap.id
FROM   meal m
JOIN   additional_product ap
       ON ap.name IN ('Small Fries','Cola 0.3L')
WHERE  m.name = 'Classic Beef Combo (S-Fries + 0.3 L Cola)';

-- Chicken Combo       → Medium Fries + Cola 0.5 L
INSERT INTO meal_additional_product (meal_id, additional_product_id)
SELECT m.id, ap.id
FROM   meal m
JOIN   additional_product ap
       ON ap.name IN ('Medium Fries','Cola 0.5L')
WHERE  m.name = 'Chicken Combo (M-Fries + 0.5 L Cola)';

-- Veggie Combo        → Large Fries + Still Water 0.5 L
INSERT INTO meal_additional_product (meal_id, additional_product_id)
SELECT m.id, ap.id
FROM   meal m
JOIN   additional_product ap
       ON ap.name IN ('Large Fries','Still Water 0.5L')
WHERE  m.name = 'Veggie Combo (L-Fries + 0.5 L Still Water)';

INSERT INTO recomended_products(product_id, product_name) VALUES
(1, 'Burger'),
(2, 'Cola'),
(3, 'Fries')
