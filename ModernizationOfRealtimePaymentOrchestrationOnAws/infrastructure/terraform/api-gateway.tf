#--------------------------------------------------------------
# API Gateway REST API
#--------------------------------------------------------------
resource "aws_api_gateway_rest_api" "main" {
  name        = "${local.name_prefix}-api"
  description = "Edge-optimized API Gateway for Payment Orchestration"

  endpoint_configuration {
    types = ["EDGE"]
  }

  tags = {
    Name = "${local.name_prefix}-api"
  }
}

#--------------------------------------------------------------
# API Resource: /api/v1/payments
#--------------------------------------------------------------
resource "aws_api_gateway_resource" "api" {
  rest_api_id = aws_api_gateway_rest_api.main.id
  parent_id   = aws_api_gateway_rest_api.main.root_resource_id
  path_part   = "api"
}

resource "aws_api_gateway_resource" "v1" {
  rest_api_id = aws_api_gateway_rest_api.main.id
  parent_id   = aws_api_gateway_resource.api.id
  path_part   = "v1"
}

resource "aws_api_gateway_resource" "payments" {
  rest_api_id = aws_api_gateway_rest_api.main.id
  parent_id   = aws_api_gateway_resource.v1.id
  path_part   = "payments"
}

#--------------------------------------------------------------
# API Method + VPC Link Integration
#--------------------------------------------------------------
resource "aws_api_gateway_method" "payments_any" {
  rest_api_id   = aws_api_gateway_rest_api.main.id
  resource_id   = aws_api_gateway_resource.payments.id
  http_method   = "ANY"
  authorization = "NONE"
}

resource "aws_api_gateway_integration" "payments_mock" {
  rest_api_id = aws_api_gateway_rest_api.main.id
  resource_id = aws_api_gateway_resource.payments.id
  http_method = aws_api_gateway_method.payments_any.http_method
  type        = "MOCK"
  request_templates = {
    "application/json" = "{\"statusCode\": 200}"
  }
}

resource "aws_api_gateway_method_response" "payments_200" {
  rest_api_id = aws_api_gateway_rest_api.main.id
  resource_id = aws_api_gateway_resource.payments.id
  http_method = aws_api_gateway_method.payments_any.http_method
  status_code = "200"

  response_models = {
    "application/json" = "Empty"
  }
}

resource "aws_api_gateway_integration_response" "payments_200" {
  rest_api_id = aws_api_gateway_rest_api.main.id
  resource_id = aws_api_gateway_resource.payments.id
  http_method = aws_api_gateway_method.payments_any.http_method
  status_code = aws_api_gateway_method_response.payments_200.status_code

  response_templates = {
    "application/json" = "{\"message\": \"Payment API Gateway operational\"}"
  }
}

#--------------------------------------------------------------
# API Deployment + Stage
#--------------------------------------------------------------
resource "aws_api_gateway_deployment" "main" {
  rest_api_id = aws_api_gateway_rest_api.main.id

  triggers = {
    redeployment = sha1(jsonencode([
      aws_api_gateway_resource.payments.id,
      aws_api_gateway_method.payments_any.id,
      aws_api_gateway_integration.payments_mock.id,
    ]))
  }

  lifecycle {
    create_before_destroy = true
  }
}

resource "aws_api_gateway_stage" "main" {
  deployment_id = aws_api_gateway_deployment.main.id
  rest_api_id   = aws_api_gateway_rest_api.main.id
  stage_name    = var.environment

  tags = {
    Name = "${local.name_prefix}-api-stage"
  }
}

#--------------------------------------------------------------
# API Gateway Usage Plan & Throttling
#--------------------------------------------------------------
resource "aws_api_gateway_usage_plan" "main" {
  name        = "${local.name_prefix}-usage-plan"
  description = "Usage plan for payment API"

  api_stages {
    api_id = aws_api_gateway_rest_api.main.id
    stage  = aws_api_gateway_stage.main.stage_name
  }

  throttle_settings {
    burst_limit = 500
    rate_limit  = 1000
  }
}
