#--------------------------------------------------------------
# MSK Configuration
#--------------------------------------------------------------
resource "aws_msk_configuration" "main" {
  name           = "${local.name_prefix}-msk-config"
  kafka_versions = [var.msk_kafka_version]
  description    = "MSK configuration for payment orchestration"

  server_properties = <<PROPERTIES
auto.create.topics.enable=false
default.replication.factor=3
min.insync.replicas=2
num.partitions=12
num.io.threads=8
num.network.threads=5
log.retention.hours=168
log.segment.bytes=1073741824
unclean.leader.election.enable=false
message.max.bytes=1048576
compression.type=lz4
PROPERTIES
}

#--------------------------------------------------------------
# MSK Cluster
#--------------------------------------------------------------
resource "aws_msk_cluster" "main" {
  cluster_name           = "${local.name_prefix}-msk"
  kafka_version          = var.msk_kafka_version
  number_of_broker_nodes = var.msk_number_of_brokers

  configuration_info {
    arn      = aws_msk_configuration.main.arn
    revision = aws_msk_configuration.main.latest_revision
  }

  broker_node_group_info {
    instance_type   = var.msk_instance_type
    client_subnets  = aws_subnet.private[*].id
    security_groups = [aws_security_group.msk.id]

    storage_info {
      ebs_storage_info {
        volume_size = 100
      }
    }
  }

  encryption_info {
    encryption_in_transit {
      client_broker = "TLS_PLAINTEXT"
      in_cluster    = true
    }
  }

  logging_info {
    broker_logs {
      cloudwatch_logs {
        enabled   = true
        log_group = aws_cloudwatch_log_group.msk.name
      }
    }
  }

  tags = {
    Name = "${local.name_prefix}-msk"
  }
}

#--------------------------------------------------------------
# MSK CloudWatch Log Group
#--------------------------------------------------------------
resource "aws_cloudwatch_log_group" "msk" {
  name              = "/aws/msk/${local.name_prefix}"
  retention_in_days = 30

  tags = {
    Name = "${local.name_prefix}-msk-logs"
  }
}
