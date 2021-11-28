package com.myorg;

import java.util.List;
import java.util.Arrays;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.core.StackProps;
import software.amazon.awscdk.core.Duration;
import software.amazon.awscdk.services.dynamodb.AttributeType;
import software.amazon.awscdk.services.dynamodb.GlobalSecondaryIndexProps;
import software.amazon.awscdk.services.dynamodb.LocalSecondaryIndexProps;
import software.amazon.awscdk.services.dynamodb.Table;
import software.amazon.awscdk.services.dynamodb.TableProps;
import software.amazon.awscdk.services.dynamodb.Attribute;
import software.amazon.awscdk.services.stepfunctions.*;
import software.amazon.awscdk.services.stepfunctions.StateMachine;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.stepfunctions.tasks.LambdaInvoke;
import software.amazon.awscdk.services.iam.*;
import software.amazon.awscdk.services.iam.Role;
import software.amazon.awscdk.services.iam.ServicePrincipal;
import software.amazon.awscdk.services.iam.PolicyStatement;
import software.amazon.awscdk.services.s3.Bucket;
//import software.amazon.awscdk.services.s3.deployment.*;
import software.amazon.awscdk.services.apigateway.*;
import software.amazon.awscdk.core.CfnOutput;
import software.amazon.awscdk.core.RemovalPolicy;

public class IacStack extends Stack {
    public IacStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public IacStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        // IAM Roles
        Role stepFunctionRole = Role.Builder.create(this, "StepFuncRole")
                .assumedBy(new ServicePrincipal("states.amazonaws.com"))
                .build();

        stepFunctionRole.addToPolicy(PolicyStatement.Builder.create()
                .resources(Arrays.asList("*"))
                .actions(Arrays.asList("*"))
                .build());

        Role lambdaRole = Role.Builder.create(this, "LambdaRole")
                .assumedBy(new ServicePrincipal("lambda.amazonaws.com"))
                .build();

        lambdaRole.addToPolicy(PolicyStatement.Builder.create()
                .resources(Arrays.asList("*"))
                .actions(Arrays.asList("*"))
                .build());

        // Lambda Functions
        Function newsGetterFunction = Function.Builder.create(this, "NewsAPIGetter")
                .runtime(Runtime.NODEJS_14_X)
                .code(Code.fromAsset("resources"))
                .role(lambdaRole)
                .timeout(Duration.seconds(30))
                .handler("NewsAPIGetter.handler").build();

        Function runComprehendFunction = Function.Builder.create(this, "RunComprehend")
                .runtime(Runtime.PYTHON_3_9)
                .code(Code.fromAsset("resources"))
                .role(lambdaRole)
                .timeout(Duration.seconds(30))
                .handler("GetSentiments.lambda_handler").build();

        Function sendEmailsFunction = Function.Builder.create(this, "SendEmails")
                .runtime(Runtime.NODEJS_14_X)
                .code(Code.fromAsset("resources"))
                .role(lambdaRole)
                .timeout(Duration.seconds(30))
                .handler("SendEmails.handler").build();

        Function registerUserFunction = Function.Builder.create(this, "RegisterUser")
                .runtime(Runtime.PYTHON_3_9)
                .code(Code.fromAsset("resources"))
                .role(lambdaRole)
                .timeout(Duration.seconds(30))
                .handler("RegisterUser.lambda_handler").build();

        Function triggerStepFunctionsFunction = Function.Builder.create(this, "TriggerStepFunctions") //will need to manually change the arn
                .runtime(Runtime.NODEJS_14_X)
                .code(Code.fromAsset("resources"))
                .role(lambdaRole)
                .timeout(Duration.seconds(30))
                .handler("TriggerStepFunctions.handler").build();

        // State Machine
        StateMachine stateMachine = StateMachine.Builder.create(this, "CovAlertStateMachine")
                .definition(LambdaInvoke.Builder.create(this, "newsGetterFunction")
                        .lambdaFunction(newsGetterFunction)
                        .build()
                        .next(LambdaInvoke.Builder.create(this, "runComprehendFunction")
                                .lambdaFunction(runComprehendFunction)
                                .build())
                        .next(LambdaInvoke.Builder.create(this, "sendEmailsFunction")
                                .lambdaFunction(sendEmailsFunction)
                                .build()))
                .role(stepFunctionRole)
                .build();
        CfnOutput.Builder.create(this, "StateMachineArn").value(stateMachine.getStateMachineArn()).build();

        // Dynamo DB
        String tableName = "SWEN514__User_Registration_Log";
        TableProps tableProps = TableProps.builder()
                .partitionKey(Attribute.builder()
                        .name("location")
                        .type(AttributeType.STRING)
                        .build())
                .readCapacity(5)
                .writeCapacity(5)
                .tableName(tableName)
                .removalPolicy(RemovalPolicy.DESTROY)
                .build();
        Table userRegistrationLogTable = new Table(this, tableName, tableProps);

        // bucket/frontend
        Bucket frontendBucket = Bucket.Builder.create(this, "FrontendBucket")
                .websiteIndexDocument("index.html")
                .publicReadAccess(true)
                .autoDeleteObjects(true)
                .removalPolicy(RemovalPolicy.DESTROY)
                .build();
//        BucketDeployment.Builder.create(this, "DeployWebsite") //can't get the import to work for this
//                .sources(List.of(s3deploy.Source.asset("resources/reactAppBuild")))
//                .destinationBucket(frontendBucket)
//                .destinationKeyPrefix("web/static")
//                .build();
        String websiteURL = frontendBucket.getBucketDomainName() + "/index.html";
        CfnOutput.Builder.create(this, "WebsiteURL").value(websiteURL).build();

        // API Gateway
        LambdaRestApi api = LambdaRestApi.Builder.create(this, "RegisterUserAPI")
                .handler(registerUserFunction)
                .proxy(false)
                .defaultCorsPreflightOptions(CorsOptions.builder()
                        .allowOrigins(Cors.ALL_ORIGINS)
                        .allowMethods(Cors.ALL_METHODS)
                        .build())
                .build();
        api.getRoot().addMethod("POST"); //POST on /
    }
}

