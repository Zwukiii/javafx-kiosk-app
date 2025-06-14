package org.example.mappers;


import org.example.domain.AdditionalProduct;
import org.example.enumeration.Size;
import org.example.enumeration.Type;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class AdditionalProductMapper {

    public AdditionalProduct mapSqlResponseToAdditionalProduct(final ResultSet rs) throws SQLException {


        if (!rs.next()) {
            return null;
        }

        return AdditionalProduct.builder()
                .id(rs.getLong("id"))
                .name(rs.getString("name"))
                .image(rs.getString("image"))
                .price(rs.getBigDecimal("price"))
                .type(Type.valueOf(rs.getString("type")
                        .toUpperCase()))
                .size(Size.valueOf(rs.getString("size")
                        .toUpperCase()))
                .build();
    }

    public List<AdditionalProduct> mapSqlResponseToListOfAdditionalProducts(final ResultSet rs) throws SQLException {
        if (rs == null) {
            throw new IllegalArgumentException("ResultSet con not be null");
        }

        List<AdditionalProduct> additionalProducts = new ArrayList<>();

        while (rs.next()) {
            AdditionalProduct additionalProduct = AdditionalProduct.builder()
                    .id(rs.getLong("id"))
                    .name(rs.getString("name"))
                    .image(rs.getString("image"))
                    .price(rs.getBigDecimal("price"))
                    .type(Type.valueOf(rs.getString("type")
                            .toUpperCase()))
                    .size(Size.valueOf(rs.getString("size")
                            .toUpperCase()))
                    .build();
            additionalProducts.add(additionalProduct);
        }

        return additionalProducts;
    }
}
