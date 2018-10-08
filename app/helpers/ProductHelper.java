package helpers;

import com.avaje.ebean.Ebean;
import models.Product;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class ProductHelper {


    /**
     * returns an optional of a product. This methods attempts to get a product by Ids and envId
     *
     * @param productID
     * @param envKey
     * @return
     */
    public static Optional<Product> getProductById(int productID,
                                                   String envKey) {
        return Optional.ofNullable(Ebean.find(Product.class)
                                        .where()
                                        .eq("product_id", productID)
                                        .eq("env_id", envKey)
                                        .findUnique());
    }


    /**
     * This method determines if a product exists with the given productId and envId
     *
     * @param productID
     * @param envId
     * @return
     */
    public static boolean exists(int productID,
                                 String envId) {
        return Ebean.find(Product.class)
                    .where()
                    .eq("product_id", productID)
                    .eq("env_id", envId)
                    .findUnique() != null;
    }

    /**
     * This method takes a collection of productIds and an envId and returns a list of any
     * product ids that don't exist
     *
     * @param ids
     * @param envId
     * @return
     */
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
