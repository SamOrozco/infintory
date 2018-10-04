package helpers;

import com.avaje.ebean.Ebean;
import models.Product;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class ProductHelper {


    public static Optional<Product> getProductById(int productID,
                                                   String envKey) {
        return Optional.ofNullable(Ebean.find(Product.class)
                                        .where()
                                        .eq("product_id", productID)
                                        .eq("env_id", envKey)
                                        .findUnique());
    }


    public static boolean exists(int productID,
                                 String envKey) {
        return Ebean.find(Product.class)
                    .where()
                    .eq("product_id", productID)
                    .eq("env_id", envKey)
                    .findUnique() != null;
    }

    public static Optional<Collection<Integer>> validateProductIds(Collection<Integer> ids,
                                                                   String envId) {
        List<Product> products = Ebean.find(Product.class)
                                      .select("productId")
                                      .where()
                                      .in("product_id", ids)
                                      .eq("env_id", envId)
                                      .findList();
        if (products == null) {
            return Optional.of(ids);
        }
        for (Product prod : products) {
            ids.remove(prod.getProductId());
        }

        return ids.size() > 0 ?
               Optional.of(ids) :
               Optional.empty();
    }


}
