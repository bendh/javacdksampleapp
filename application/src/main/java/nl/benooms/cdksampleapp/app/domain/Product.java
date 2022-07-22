package nl.benooms.cdksampleapp.app.domain;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class Product {
	private String id;
	private String title;
	private String description;
	private BigDecimal price;
}
