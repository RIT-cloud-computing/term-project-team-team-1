package com.myorg;

import java.util.List;
import java.util.Arrays;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.core.StackProps;
import software.amazon.awscdk.core.Duration;
import software.amazon.awscdk.services.*;
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

public class IacStack extends Stack {
    public IacStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public IacStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);
        
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


        StateMachine stateMachine = StateMachine.Builder.create(this, "MyStateMachine")
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
    }
}

