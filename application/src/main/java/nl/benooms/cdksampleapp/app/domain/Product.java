package nl.benooms.cdksampleapp.app.domain;

import lombok.Data;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.math.BigDecimal;

@Data
@DynamoDbBean
public class Product {
	private String id;
	private String title;
	private String description;
	private BigDecimal price;

	@DynamoDbPartitionKey
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
