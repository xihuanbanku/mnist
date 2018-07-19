package com.isinonet

import org.apache.spark.sql.SparkSession

object IBMseniment {
  def main(a:Array[String]): Unit= {
    val sparkSession = SparkSession.builder()
    .appName("IBMseniment")
    .master("local")
    .getOrCreate()
    import sparkSession.implicits._

    val comments = sparkSession.read.textFile("/comment.txt")
        .map(_.split("\t")(1))
                .map(unicode2cn(_)).foreach(println(_))
//    val rateDocument = comments.distinct().map(line => {
//      line.split("\t")
//    }).filter(_.length == 2)
//    rateDocument
  }

  def unicode2cn(str:String): String = {

    var res: String = ""

    import java.util.regex.Pattern
    val pattern = Pattern.compile("(\\\\u(\\p{XDigit}{4}))")
    val matcher = pattern.matcher(str)
    var ch = 0
    while (matcher.find) { //group 6728
      val group = matcher.group(2)
      //ch:'木' 26408
      ch = Integer.parseInt(group, 16).toChar
      //group1 \u6728
      val group1 = matcher.group(1)
      res = str.replace(group1, ch + "")
    }
    res
  }
}
