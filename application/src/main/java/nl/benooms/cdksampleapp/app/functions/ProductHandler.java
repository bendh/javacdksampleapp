package nl.benooms.cdksampleapp.app.functions;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import nl.benooms.cdksampleapp.app.domain.Product;
import software.amazon.lambda.powertools.utilities.JsonConfig;
import static software.amazon.lambda.powertools.utilities.EventDeserializer.extractDataFrom;
import static nl.benooms.cdksampleapp.app.service.ProductPersistenceService.*;

import java.util.Map;

public class ProductHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

	private static final Map<String, String> DEFAULT_HEADERS = Map.of("Content-Type", "application/json");
	private static final String errorJsonString = "{\"error\":\"#message\"}";
	private static final String sucessJsonString = "{\"message\":\"#message\"}";
	private ObjectMapper jsonMapper = JsonConfig.get().getObjectMapper();

	@SneakyThrows
	@Override
	public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {

		var apiGatewayResponse = new APIGatewayProxyResponseEvent().withHeaders(DEFAULT_HEADERS);
		var requestPath = request.getPath();

		if(requestPath.startsWith("/product")) {
			var productId = request.getPathParameters() != null? request.getPathParameters().get("productId"): null;
			if (productId == null || productId.isEmpty()) {
				switch (request.getHttpMethod()){
					case "GET":
						var products = getProducts();
						return createSucessResponseWith(jsonMapper.writeValueAsString(products));
					case "POST":
						var productToSave =deserialize(request.getBody());
						var savedProduct = saveProduct(productToSave);
						return createSucessResponseWith(jsonMapper.writeValueAsString(savedProduct));
				}
			} else {
				switch (request.getHttpMethod()){
					case "GET":
						var product = getProductWith(productId);
						return createSucessResponseWith(jsonMapper.writeValueAsString(product));
					case "DELETE":
						deleteProductWith(productId);
						return createSucessResponseWith(jsonMapper.writeValueAsString(sucessJsonString.replace("#message", "Deleted product with id: " + productId)));
				}
			}
		}

		return createErrorResponseWith("Unable to process request"); // return error response
	}
	private APIGatewayProxyResponseEvent createErrorResponseWith(String message) {
		return new APIGatewayProxyResponseEvent()
				.withHeaders(DEFAULT_HEADERS)
				.withBody(errorJsonString.replace("#message", message));
	}

	private APIGatewayProxyResponseEvent createSucessResponseWith(String body) {
		return new APIGatewayProxyResponseEvent()
				.withHeaders(DEFAULT_HEADERS)
				.withStatusCode(200)
				.withBody(body);
	}

	@SneakyThrows
	private Product deserialize(String product) {
		return jsonMapper.readValue(product, Product.class);
	}
}
