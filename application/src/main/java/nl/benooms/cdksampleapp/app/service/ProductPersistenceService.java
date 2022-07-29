package nl.benooms.cdksampleapp.app.service;

import exception.ProductNotFoundException;
import lombok.extern.java.Log;
import nl.benooms.cdksampleapp.app.domain.Product;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Log
public class ProductPersistenceService {
	private static DynamoDbTable<Product> productTable;

	static {
		DynamoDbClient ddb = DynamoDbClient.builder()
				.build();
		DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder()
				.dynamoDbClient(ddb)
				.build();
		ProductPersistenceService.productTable = enhancedClient.table("Product", TableSchema.fromBean(Product.class));
	}

	public static List<Product> getProducts() {
		return productTable.scan().items().stream().collect(Collectors.toList());
	}

	public static Product getProductWith(String id) throws ProductNotFoundException {
		Product product = null;
		try {
			product = productTable.getItem(Key.builder().partitionValue(id).build());
		} catch (DynamoDbException ex) {
			log.severe(ex.getMessage());
		}
		if (product == null) {
			throw new ProductNotFoundException("Product not found with id: "+id);
		}
		return product;
	}

	public static Product saveProduct(Product tosave) {
		if (tosave.getId() == null) tosave.setId(UUID.randomUUID().toString());
		try {
			productTable.putItem(tosave);
		} catch (DynamoDbException ex) {
			log.severe(ex.getMessage());
		}
		return tosave;
	}

	public static void deleteProductWith(String productId) {
		try {
			productTable.deleteItem(Key.builder().partitionValue(productId).build());
		} catch (DynamoDbException ex) {
			log.severe(ex.getMessage());
		}
	}
}
