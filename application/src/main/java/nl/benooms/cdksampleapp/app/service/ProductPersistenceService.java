package nl.benooms.cdksampleapp.app.service;

import nl.benooms.cdksampleapp.app.domain.Product;

import java.math.BigDecimal;
import java.util.List;

public class ProductPersistenceService {

	public static List<Product> getProducts() {
		return List.of();
	}

	public static Product getProductWith(String id) {
		var product = new Product();
		product.setId("123");
		product.setDescription("Test product");
		product.setTitle("Some prod title");
		product.setPrice(new BigDecimal("1.99"));
		return product;
	}

	public static Product saveProduct(Product tosave) {
		return tosave;
	}

	public static void deleteProductWith(String productId) {

	}
}
