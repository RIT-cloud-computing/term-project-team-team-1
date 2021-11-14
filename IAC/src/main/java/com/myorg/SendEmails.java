package com.myorg;

import java.util.HashMap;

import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.services.apigateway.LambdaIntegration;
import software.amazon.awscdk.services.apigateway.Resource;
import software.amazon.awscdk.services.apigateway.RestApi;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.s3.Bucket;

public class SendEmails extends Construct {

    @SuppressWarnings("serial")
    public SendEmails(Construct scope, String id) {
        super(scope, id);

        Function handler = Function.Builder.create(this, "SendEmails")
            .runtime(Runtime.NODEJS_14_X)
            .code(Code.fromAsset("resources"))
            .handler("SendEmails.handler").build();
    }
}
