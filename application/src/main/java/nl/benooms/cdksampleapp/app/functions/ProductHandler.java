package nl.benooms.cdksampleapp.app.functions;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import exception.ProductNotFoundException;
import lombok.SneakyThrows;
import nl.benooms.cdksampleapp.app.domain.Product;
import software.amazon.lambda.powertools.utilities.JsonConfig;

import static nl.benooms.cdksampleapp.app.service.ProductPersistenceService.*;

import java.util.Map;

public class ProductHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

	private static final Map<String, String> DEFAULT_HEADERS = Map.of("Content-Type", "application/json");
	private static final String ERROR_JSON_STRING = "{\"error\":\"#message\"}";
	private static final String SUCCESS_JSON_STRING = "{\"message\":\"#message\"}";
	private static final ObjectMapper JSON_MAPPER = JsonConfig.get().getObjectMapper();

	@SneakyThrows
	@Override
	public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
		
		var requestPath = request.getPath();

		if(requestPath.startsWith("/product")) {
			var productId = request.getPathParameters() != null? request.getPathParameters().get("productId"): null;
			if (productId == null || productId.isEmpty()) {
				switch (request.getHttpMethod()){
					case "GET":
						var products = getProducts();
						return createSucessResponseWith(JSON_MAPPER.writeValueAsString(products));
					case "POST":
						var productToSave =deserialize(request.getBody());
						var savedProduct = saveProduct(productToSave);
						return createSucessResponseWith(JSON_MAPPER.writeValueAsString(savedProduct));
					default:
						createErrorResponseWith("Method not allowed");
				}
			} else {
				switch (request.getHttpMethod()){
					case "GET":
						try {
							var product = getProductWith(productId);
							return createSucessResponseWith(JSON_MAPPER.writeValueAsString(product));
						} catch (ProductNotFoundException ex) {
							return createErrorResponseWith(ex.getMessage());
						}
					case "PUT":
						var productToSave =deserialize(request.getBody());
						var savedProduct = saveProduct(productToSave);
						return createSucessResponseWith(JSON_MAPPER.writeValueAsString(savedProduct));
					case "DELETE":
						deleteProductWith(productId);
						return createSucessResponseWith(JSON_MAPPER.writeValueAsString(SUCCESS_JSON_STRING.replace("#message", "Deleted product with id: " + productId)));
					default:
						createErrorResponseWith("Method not allowed");
				}
			}
		}

		return createErrorResponseWith("Unable to process request"); // return error response
	}
	private APIGatewayProxyResponseEvent createErrorResponseWith(String message) {
		return new APIGatewayProxyResponseEvent()
				.withHeaders(DEFAULT_HEADERS)
				.withBody(ERROR_JSON_STRING.replace("#message", message));
	}

	private APIGatewayProxyResponseEvent createSucessResponseWith(String body) {
		return new APIGatewayProxyResponseEvent()
				.withHeaders(DEFAULT_HEADERS)
				.withStatusCode(200)
				.withBody(body);
	}

	@SneakyThrows
	private Product deserialize(String product) {
		return JSON_MAPPER.readValue(product, Product.class);
	}
}
