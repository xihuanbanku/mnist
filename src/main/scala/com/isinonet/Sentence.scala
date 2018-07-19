package com.isinonet

import com.huaban.analysis.jieba.{JiebaSegmenter, SegToken}
import com.huaban.analysis.jieba.JiebaSegmenter.SegMode
import org.apache.spark.ml.feature.{HashingTF, IDF}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._

/**
  * Created by Administrator on 2018-07-19.
  * 2018-07-19
  */
object Sentence {

  def main(a: Array[String]) :Unit = {

    val sparkSession = SparkSession.builder()
      .master("local")
      .appName("Similar analysis")
      .config("spark.testing.memory", 2147480000)
      .getOrCreate()
    val sentences = sparkSession.createDataFrame(Seq(
      (1, "我很喜欢看电影, 也喜欢看书"),
      (2, "我不喜欢看电影, 但喜欢看书")
    )).toDF("id", "sentence")

    val wordsData = sentences.select("id, jieba(sentence)").toDF("id", "words")
    wordsData.show(false)
    val hashingTF = new HashingTF()
      .setInputCol("words").setOutputCol("rawFeatures")

    val featurizedData = hashingTF.transform(wordsData)

    val idf = new IDF().setInputCol("rawFeatures")
      .setOutputCol("features")
    val iDFModel = idf.fit(featurizedData)

    val finalResult = iDFModel.transform(featurizedData)

    finalResult.select("id", "features").show(false)

    sparkSession.stop()

  }
}
