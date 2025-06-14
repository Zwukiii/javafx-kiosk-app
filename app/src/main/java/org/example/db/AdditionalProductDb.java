package org.example.db;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.example.domain.AdditionalProduct;
import org.example.mappers.AdditionalProductMapper;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
public class AdditionalProductDb {
    AdditionalProductMapper additionalProductMapper = new AdditionalProductMapper();


    public AdditionalProduct getAdditionalProductByIdFromDb(Connection connection, long id) throws SQLException {
        String query = "SELECT * FROM additional_product WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return additionalProductMapper.mapSqlResponseToAdditionalProduct(rs);
            }
        }
    }


    public List<AdditionalProduct> getAllAdditionalProducts(Connection connection) throws SQLException {

        final String PRODUCT_QUERY = "SELECT * FROM additional_product";

        List<AdditionalProduct> products = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(PRODUCT_QUERY);
             ResultSet rs = stmt.executeQuery()) {

            AdditionalProduct product;
            while ((product = additionalProductMapper.mapSqlResponseToAdditionalProduct(rs)) != null) {

                products.add(product);
            }
        }
        return products;
    }


    public AdditionalProduct createNewAdditionalProductInDb(Connection connection, final AdditionalProduct additionalProduct) throws SQLException{
        final String INSERT_ING =
                "INSERT INTO additional_product (name, price, image, type, size) VALUES (?, ?, ?, ?, ?) ";
        try(PreparedStatement statement = connection.prepareStatement(INSERT_ING, Statement.RETURN_GENERATED_KEYS)){

            statement.setString(1, additionalProduct.getName());
            statement.setBigDecimal(2, additionalProduct.getPrice());
            statement.setString(3, additionalProduct.getImage());
            statement.setString(4, additionalProduct.getType().toString());
            statement.setString(5, additionalProduct.getSize().toString());

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating ingredient failed: no rows affected.");
            }
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {

                    long id = generatedKeys.getLong(1);
                    //long id = generatedKeys.getLong("id");

                    additionalProduct.setId(id);
                } else {
                    throw new SQLException("Creating ingredient failed: no ID obtained.");
                }
            }
        }
        return additionalProduct;
    }



    public void deleteAdditionalProductFromDb(Connection connection, long id) throws SQLException{
        final String DEL = "DELETE FROM additional_product WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(DEL)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        }
    }

    public AdditionalProduct updateAdditionalProductInDb(Connection connection, final AdditionalProduct additionalProduct) throws SQLException {
        final String UPDATE_SQL =
                "UPDATE additional_product " +
                        "SET name = ?, price = ?, image = ?, type = ?, size = ? " +
                        "WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(UPDATE_SQL)) {
            stmt.setString(1, additionalProduct.getName());
            stmt.setBigDecimal(2, additionalProduct.getPrice());
            stmt.setString(3, additionalProduct.getImage());
            stmt.setString(4, additionalProduct.getType().toString());
            stmt.setString(5, additionalProduct.getSize().toString());
            stmt.setLong(6, additionalProduct.getId());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating AdditionalProduct failed: no rows affected.");
            }
        }

        return additionalProduct;
    }


}
