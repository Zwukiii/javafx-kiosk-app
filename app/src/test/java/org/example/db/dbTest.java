package org.example.db;

import org.example.config.DatabaseInitializer;
import org.example.domain.AdditionalProduct;
import org.example.domain.BasicProduct;
import org.example.domain.Ingredient;
import org.example.domain.Meal;
import org.example.mappers.AdditionalProductMapper;
import org.example.mappers.BasicProductMapper;
import org.example.mappers.IngredientMapper;
import org.example.mappers.MealMapper;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class dbTest {
    private static DatabaseInitializer initializer;
    private Connection conn;

    @BeforeAll
    static void initDatabase() {
        initializer = new DatabaseInitializer();
        // initializer.initialize();
    }

    @BeforeEach
    void openConnection() throws SQLException {
        conn = initializer.connection();
        conn.setAutoCommit(false);
    }


    @AfterEach
    void rollbackAndClose() throws SQLException {
        conn.rollback();
        conn.close();
    }

    // AdditionalProductDb

    @Test
    void getAdditionalProductByIdFromDb_Test() throws Exception {

        AdditionalProductMapper mapper = new AdditionalProductMapper();
        AdditionalProductDb aaaa = new AdditionalProductDb(mapper);

        long id = 1L;
        AdditionalProduct ap = aaaa.getAdditionalProductByIdFromDb(conn, id);

        assertNotNull(ap, "Can't find this id=" + id + "  additional_product");
        assertEquals(id, ap.getId());
    }

    @Test
    void getAllAdditionalProducts_Test() throws Exception {
        AdditionalProductMapper mapper = new AdditionalProductMapper();
        AdditionalProductDb aaaa = new AdditionalProductDb(mapper);

        List<AdditionalProduct> products = new ArrayList<>();
        products = aaaa.getAllAdditionalProducts(conn);
        assertNotNull(products, "Can't find any additional_product");
    }


    // BasicProductDb

    @Test
    void getBasicProductByIdFromDb_Test() throws Exception {
        BasicProductMapper bpm = new BasicProductMapper();
        IngredientMapper im = new IngredientMapper();
        BasicProductDb bbbb = new BasicProductDb();

        long id = 1L;
        BasicProduct bp = bbbb.getBasicProductByIdFromDb(conn, id);

        assertNotNull(bp, "Can't find id=" + id + " in basic_product");
        assertEquals(id, bp.getId());
        assertNotNull(bp.getIngredients(), "ingredients shouldn't be null");
    }

    @Test
    void getAllBasicProducts_Test() throws Exception {
        BasicProductMapper bpm = new BasicProductMapper();
        IngredientMapper im = new IngredientMapper();
        BasicProductDb bbbb = new BasicProductDb();

        List<BasicProduct> products = bbbb.getAllBasicProducts(conn);
        assertNotNull(products, "Can't find any basic_product");
        assertFalse(products.isEmpty(), "basic_product table empty");
    }


    // IngredientsDb

    @Test
    void getAllAvailableIngredients_Test() throws Exception {
        IngredientMapper im = new IngredientMapper();
        IngredientsDb ingDb = new IngredientsDb(im);

        List<Ingredient> list = ingDb.getAllAvailableIngredients(conn);
        assertNotNull(list, "ingredient table empty");
        assertFalse(list.isEmpty(), "ingredient table empty");
    }

    // MealDb
    @Test
    void getBasicMealByIdFromDb_Test() throws Exception {

        long basicId = 100L;
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO basic_product (id, name, image, price, type, size) "
                        + "VALUES (?, ?, ?, ?, ?, ?)")) {
            ps.setLong   (1, basicId);
            ps.setString (2, "TestBasic");
            ps.setString (3, "test.png");
            ps.setBigDecimal(4, new BigDecimal("12.34"));
            ps.setString (5, "FOOD");
            ps.setString (6, "SMALL");
            ps.executeUpdate();
        }

        long addId = 200L;
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO additional_product (id, name, image, price, type, size) "
                        + "VALUES (?, ?, ?, ?, ?, ?)")) {
            ps.setLong   (1, addId);
            ps.setString (2, "TestAdd");
            ps.setString (3, "add.png");
            ps.setBigDecimal(4, new BigDecimal("5.67"));
            ps.setString (5, "DRINK");
            ps.setString (6, "ONESIZE");
            ps.executeUpdate();
        }

        long mealId = 300L;
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO meal (id, name, price, basic_product_id) VALUES (?, ?, ?, ?)")) {
            ps.setLong   (1, mealId);
            ps.setString (2, "TestMeal");
            ps.setBigDecimal(3, new BigDecimal("20.00"));
            ps.setLong   (4, basicId);
            ps.executeUpdate();
        }

        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO meal_additional_product (meal_id, additional_product_id) VALUES (?, ?)")) {
            ps.setLong(1, mealId);
            ps.setLong(2, addId);
            ps.executeUpdate();
        }

        BasicProductDb bpDb = new BasicProductDb();
        AdditionalProductDb apDb = new AdditionalProductDb(new AdditionalProductMapper());
        MealMapper mMapper = new MealMapper();
        MealDb repo = new MealDb();

        Meal meal = repo.getBasicMealByIdFromDb(conn, mealId);

        assertNotNull(meal,   "Should find meal id=" + mealId);
        assertEquals(mealId,  meal.getId());
        assertNotNull(meal.getBasicProduct(), "basicProduct must be filled");
        assertEquals(basicId, meal.getBasicProduct().getId());
        assertFalse(meal.getAdditionalProducts().isEmpty(), "Should have additionalProducts");
        assertEquals(addId, meal.getAdditionalProducts().get(0).getId());
    }

    @Test
    void getAllMeals_Test() throws Exception {
        MealDb mealDb = new MealDb();
        List<Meal> meals = mealDb.getAllMeals(conn);
        assertNotNull(meals, "Can't find any meal");
    }
}
