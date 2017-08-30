package actors

trait RemoteMessage //extends Serializable
case class ClientSubmitTask(dataPath:String, name:String) extends RemoteMessage
case class TestResult(result:String) extends RemoteMessage
//ALS消息
case class AlsTask(alsMasterHost: String, alsMasterPort: String, alsDataPath: String, alsResultPath: String,
                   alsResultNumber: Int, alsName: String, alsRank: Int, numIterations: Int,delimiter:String) extends RemoteMessage
//决策树消息
case class DTTask(DTMasterHost: String, DTMasterPort: String, dtTrainDataPath: String,DataPath:String,
                  modelResultPath: String,resultPath:String, numClasses: Int, DTName: String,
                  impurity: String, maxDepth: Int, maxBins:Int, Delimiter:String) extends RemoteMessage
case class DTTaskResult(modelResult:String,precison:String,predictResultPath:String) extends RemoteMessage
//随机森林消息
case class RFTask(rfMasterHost: String, rfMasterPort: String, rfTrainDataPath: String,DataPath:String,
                  modelResultPath: String,resultPath:String, numClasses: Int,numTrees:Int, rfName: String,
                  featureSubsetStrategy:String,impurity: String, maxDepth: Int, maxBins:Int, Delimiter:String) extends RemoteMessage
case class RFTaskResult(modelResult:String,precison:String,predictResultPath:String) extends RemoteMessage
//SVM消息
case class SvmTask(svmMasterHost:String,svmMasterPort:String,svmTrainDataPath:String,
                     svmPredictDataPath:String,svmModelResultPath:String,svmPredictResultPath:String,
                     name:String,iter:Int)
case class SvmTaskResult(auROC:Double, svmModelResultPath:String,svmPredictResultPath:String)