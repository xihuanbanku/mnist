package example

import org.apache.spark.sql.SparkSession

/**
  * Created by Administrator on 2018-07-19.
  * 2018-07-19
  */
class XX {

  def main(a: Array[String]): Unit = {
     val session = SparkSession.builder().master("").getOrCreate()

    session.read.textFile()

  }
}
