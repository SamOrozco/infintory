package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import models.shared.EnvironmentModel;

import javax.persistence.*;
import java.util.Map;

@Entity
@Table(name = "product")
public class Product extends EnvironmentModel {
    @Id
    @Column
    private Integer productId;
    @Column
    private String productName;
    @Column
    private String productDescription;
    @Column
    private String productUom;

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public String getProductUom() {
        return productUom;
    }

    public void setProductUom(String productUom) {
        this.productUom = productUom;
    }
}
