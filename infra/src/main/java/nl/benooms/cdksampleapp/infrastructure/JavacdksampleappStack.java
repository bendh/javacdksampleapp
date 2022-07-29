package nl.benooms.cdksampleapp.infrastructure;

import software.amazon.awscdk.BundlingOptions;
import software.amazon.awscdk.DockerVolume;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.services.dynamodb.Attribute;
import software.amazon.awscdk.services.dynamodb.AttributeType;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.FunctionProps;
import software.amazon.awscdk.services.lambda.HttpMethod;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.logs.RetentionDays;
import software.amazon.awscdk.services.s3.assets.AssetOptions;
import software.amazon.awscdk.services.apigateway.LambdaRestApi;
import software.constructs.Construct;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.dynamodb.Table;

import java.util.Arrays;
import java.util.List;

import static java.util.Collections.singletonList;
import static software.amazon.awscdk.BundlingOutput.ARCHIVED;

public class JavacdksampleappStack extends Stack {
    public JavacdksampleappStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public JavacdksampleappStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);
        List<String> lambdaFunctionPackagingInstructions = Arrays.asList(
                "/bin/sh",
                "-c",
                "cd application " +
                        "&& mvn clean install " +
                        "&& cp /asset-input/application/target/product-aws-lambda.jar /asset-output/"
        );

        BundlingOptions.Builder builderOptions = BundlingOptions.builder()
                .command(lambdaFunctionPackagingInstructions)
                .image(Runtime.JAVA_11.getBundlingImage())
                .volumes(singletonList(
                        DockerVolume.builder()
                                .hostPath(System.getProperty("user.home") + "/.m2/")
                                .containerPath("/root/.m2/")
                                .build()
                ))
                .user("root")
                .outputType(ARCHIVED);

        Function productFunction = new Function(this, "product-handler", FunctionProps.builder()
                .functionName("product-handler")
                .runtime(Runtime.JAVA_11)
                .code(Code.fromAsset("../", AssetOptions.builder().bundling(
                                builderOptions.command(
                                        lambdaFunctionPackagingInstructions
                                ).build()
                        ).build()
                ))
                .handler("nl.benooms.cdksampleapp.app.functions.ProductHandler")
                .memorySize(1024)
                .timeout(Duration.seconds(10))
                .logRetention(RetentionDays.ONE_WEEK)
                .build());

        Table productTable = Table.Builder.create(this, "Product-table")
                .partitionKey(Attribute.builder().name("id").type(AttributeType.STRING).build())
                .tableName("Product")
                .build();
        productTable.grantReadWriteData(productFunction);

        LambdaRestApi api = LambdaRestApi.Builder.create(this, "product-api")
                .handler(productFunction)
                .build();
        var productResource = api.getRoot().addResource("product");
        productResource.addMethod(HttpMethod.GET.name());
        productResource.addMethod(HttpMethod.POST.name());
        var productDetailResource = productResource.addResource("{productId}");
        productDetailResource.addMethod(HttpMethod.GET.name());
        productDetailResource.addMethod(HttpMethod.DELETE.name());
    }
}
