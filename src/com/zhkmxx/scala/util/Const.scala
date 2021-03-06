package com.zhkmxx.scala.util

/**
  * Created by zhaozihe on 2017/1/19.
  */
class Const {
  //


}

object Const{
  val USERNAME = "SNP"//

  val PASSWORD = "SNP"//

  val DRIVER_CLASS_NAME = "oracle.jdbc.driver.OracleDriver"

  val ORACLE_DRIVER_URL = "10.2.32.1"//inner-nw

  val ORACLE_DRIVER_PORT = "1521"

  val ORACLE_DRIVER_DATABASE = "orcl"

  val BROKER_LIST = "server1:9092,server2:9092,server3:9092"

  val APP_NAME = "SNP_STREAMING"

  val SPARK_SERIALIZER = "org.apache.spark.serializer.KryoSerializer"

  val SPARK_TMP_LOG_DIR = "~/tmp/SNP_SPARK_LOG"

  val SPARK_CLUSTER_MASTER_URL = "yarn-cluster"//"spark://server4:7077"

  val SPARK_CHECKPOINT_DIR = "hdfs://server4:9000/user/hadoop/spark/snp_checkpoint"

  val SPARK_STREAMING_INTERVAL = 10

  //进度表(Oracle)
  val PROCESS_TABLE = "SNP_TASKEXECUTIONMONITOR"

  val SUM_TABLE = "SNP_SUMTASKINFO"

  val PARSE_FORMULA_FAILURE = "[SNP== Formula Parse Failed ===SNP]"

  val PARSE_JSON_FAILURE = "[SNP_ERROR]No Json type received"

  //正常执行状态
  val PROCESS_STATUS_NORMAL = 2

  //异常执行状态
  val PROCESS_STATUS_ERROR = 5
}