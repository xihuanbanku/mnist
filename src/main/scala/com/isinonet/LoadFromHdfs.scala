package com.isinonet

import com.huaban.analysis.jieba.{JiebaSegmenter, SegToken}
import com.huaban.analysis.jieba.JiebaSegmenter.SegMode
import org.apache.spark.ml.classification.{NaiveBayes, NaiveBayesModel}
import org.apache.spark.ml.feature.{HashingTF, IDF, IDFModel}
import org.apache.spark.sql.SparkSession
import scala.collection.JavaConversions._
import org.apache.spark.sql.functions._

/**
  * Created by Administrator on 2018-07-20.
  * 2018-07-20
  */
object LoadFromHdfs {

  def main(a:Array[String]) :Unit = {
    val sparkSession = SparkSession.builder()
      .master("local")
      .appName("LoadFromHdfs")
      .config("spark.testing.memory", 1024 * 1024 * 1024)
      .getOrCreate()

    import sparkSession.implicits._

    // 停用词
    val stopWords = sparkSession.read.textFile("data/stopwords.txt").collectAsList().mkString(",")
    val broadStop = sparkSession.sparkContext.broadcast(stopWords)

    val bayesModel = NaiveBayesModel.load("hdfs://192.168.1.200:9000/bayesModel")
    val hashingTF = HashingTF.load("hdfs://192.168.1.200:9000/hashingTF")
    val iDFModel = IDFModel.load("hdfs://192.168.1.200:9000/iDFModel")


    val myComment = sparkSession.createDataFrame(Seq((1,"这部电影没有意思，剧情老套，真没劲, 后悔来看了"),
      (2,"太精彩了讲了一个关于梦想的故事剧情很反转制作也很精良"),
      (3,"赞到惊人，大迪斯尼动画一年比一年精彩！朱迪只身去陌生的大城市追梦，让我又追忆起自己那美好而充实的留学生活了。们的每次出场自己都要笑到面瘫了！而朱迪跟尼克哭着道歉的那段我也跟着哭了，朋友之间有时候就应该这样把脸拉下来好好道歉！总体反映的各种社会现象以及政治问题都很贴切到位。")))
      .toDF("id", "comment")
    val frame = myComment.mapPartitions(it =>{
      val segmenter = new JiebaSegmenter()
      val stopWordsStr = broadStop.value
      it.map(comt => {
        val array = segmenter.process(comt.getAs[String]("comment"), SegMode.SEARCH).toArray.map(comment => {
          comment.asInstanceOf[SegToken].word
        }).filter(stopWordsStr.indexOf(_) < 0)
        array
      })
    }).toDF("word")
    val myTF = hashingTF.transform(frame)

    val myTFIDF = iDFModel.transform(myTF)

    bayesModel.transform(myTFIDF).select(when($"prediction" === 0.0, "5星好评").when($"prediction" === 1.0, "4星好评").otherwise("3星")).show(false)

    sparkSession.stop()
  }
}
