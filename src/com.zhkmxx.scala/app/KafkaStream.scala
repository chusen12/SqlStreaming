package com.zhkmxx.scala.app

import java.net.URL
import java.util.Properties

import com.zhkmxx.scala.dao.JdbcDao
import com.zhkmxx.scala.parser.ExprParsre
import com.zhkmxx.scala.util.{Const, JsonParser}
import kafka.serializer.StringDecoder
import org.apache.spark.SparkConf
import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.sql.jdbc.{JdbcDialect, JdbcDialects, JdbcType}
import org.apache.spark.sql.types._
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.kafka._

/**
  * Created by zhaozihe on 2016/12/13.
  */
object KafkaStream {
  def main(args: Array[String]): Unit = {
    val brokers = Const.BROKER_LIST
    val jdbc = new JdbcDao
    val sparkConf = new SparkConf()
    sparkConf.setAppName(Const.APP_NAME)
    //sparkConf.setMaster(Const.SPARK_CLUSTER_MASTER_URL)
    sparkConf.setMaster(Const.SPARK_CLUSTER_MASTER_URL)
    sparkConf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
   // sparkConf.set("spark.executor.instances","6")
    val ssc = new StreamingContext(sparkConf, Seconds(Const.SPARK_STREAMING_INTERVAL))
    val currentFormulaCount = ssc.sparkContext.accumulator(0,"currentFormulaCount")//全局累加变量，用于统计是否为一个任务集合
    var tempTaskID = ""
    val topicsSet = Set("snptest")
    val kafkaParams = Map[String, String]("metadata.broker.list" -> brokers)
    val messages = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](ssc, kafkaParams, topicsSet)

    messages.foreachRDD(rdd => {

      if (rdd.isEmpty()) {
        println("[SNP-INFO-APP]No data received![SNP-INFO-APP]")
      } else {
        val parser = new ExprParsre
        val hiveContext = new HiveContext(rdd.context)
        import hiveContext.implicits._
        import hiveContext.sql

        rdd.collect().foreach(element => {
          println(element._2)

          val jp = new JsonParser
          val kafkaContentMap = jp.parseKafkaJsonString(element._2)
          println("======" + kafkaContentMap + "=======")
          //SumTable related
          val recId = kafkaContentMap("RECID").toString
          val sumTable = kafkaContentMap("SUMTABLE").toString
          val recVer = kafkaContentMap("RECVER").toString.toInt
          val unitCode = kafkaContentMap("UNITCODE").toString
          val rowIndex = kafkaContentMap("ROWINDEX").toString.toInt
          val colIndex = kafkaContentMap("COLINDEX").toString.toInt
          val formula = kafkaContentMap("FORMULA").toString
          val institutionGUID = kafkaContentMap("INSTITUTIONGUID").toString

          //TaskTable related
          val taskRecid = kafkaContentMap("TASKRECID").toString
          val taskRecver = kafkaContentMap("TASKRECVER").toString
          val taskID = kafkaContentMap("TASKID").toString
          val formulaCount = kafkaContentMap("FORMULACOUNT").toString.toInt


          if(!tempTaskID.equals(taskID)) {
            tempTaskID = taskID
            currentFormulaCount.setValue(1)
          } else{
            currentFormulaCount += 1
          }

          val ExpressPaser = parser.parserAll(parser.expr, formula)//Parsing
          var hiveSql = ""

          if(ExpressPaser.successful){

            hiveSql = ExpressPaser.get
            val hiveQueryResultDF = sql(hiveSql)//Hive Query Result DataFrame
            val hiveQueryRDD = hiveQueryResultDF.rdd
            val storageDataSet = hiveQueryRDD.map(row => {
              val result = row.getDouble(0).toInt.toString
              result
            })
            //Write to Oracle

            storageDataSet.collect.foreach(sumResultValue => {
              val dataSet = (recId,sumTable,recVer,unitCode,rowIndex,colIndex,sumResultValue,institutionGUID)
              jdbc.execute2Oracle(dataSet)
              jdbc.process2Oracle(currentFormulaCount.value, formulaCount, taskID, taskRecid)
            })

          } else{
            println(Const.PARSE_FORMULA_FAILURE + " => " + ExpressPaser)
            jdbc.process2Oracle(currentFormulaCount.value, formulaCount, taskID,taskRecid)
          }

        })
      }
    })

    // Start the computation
    ssc.start()
    ssc.awaitTermination()
  }


}