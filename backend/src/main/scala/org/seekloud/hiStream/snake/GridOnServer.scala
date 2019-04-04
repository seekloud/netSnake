package org.seekloud.hiStream.snake

import org.slf4j.LoggerFactory

/**
  * User: Taoz
  * Date: 9/3/2016
  * Time: 9:55 PM
  */
class GridOnServer(override val boundary: Point) extends Grid {


  private[this] val log = LoggerFactory.getLogger(this.getClass)

  override def debug(msg: String): Unit = log.debug(msg)

  override def info(msg: String): Unit = log.info(msg)


  private[this] var waitingJoin = Map.empty[Long, String]
  private[this] var feededApples: List[Ap] = Nil


  var currentRank = List.empty[Score]
  private[this] var historyRankMap = Map.empty[Long, Score]
  var historyRankList = historyRankMap.values.toList.sortBy(_.k).reverse

  private[this] var historyRankThreshold = if (historyRankList.isEmpty) -1 else historyRankList.map(_.k).min

  def addSnake(id: Long, name: String) = waitingJoin += (id -> name)


  private[this] def genWaitingSnake() = {
    waitingJoin.filterNot(kv => snakes.contains(kv._1)).foreach { case (id, name) =>
      val header = randomEmptyPoint()
      grid += header -> Body(id, defaultLength - 1)
      snakes += id -> SkDt(id, name, header)
    }
    waitingJoin = Map.empty[Long, String]
  }

  implicit val scoreOrdering = new Ordering[Score] {
    override def compare(x: Score, y: Score): Int = {
      var r = y.k - x.k
      if (r == 0) {
        r = y.l - x.l
      }
      if (r == 0) {
        r = (x.id - y.id).toInt
      }
      r
    }
  }

  private[this] def updateRanks() = {
    currentRank = snakes.values.map(s => Score(s.id, s.name, s.kill, s.length)).toList.sorted
    var historyChange = false
    currentRank.foreach { cScore =>
      historyRankMap.get(cScore.id) match {
        case Some(oldScore) if cScore.k > oldScore.k =>
          historyRankMap += (cScore.id -> cScore)
          historyChange = true
        case None if cScore.k > historyRankThreshold =>
          historyRankMap += (cScore.id -> cScore)
          historyChange = true
        case _ => //do nothing.
      }
    }

    if (historyChange) {
      historyRankList = historyRankMap.values.toList.sorted.take(historyRankLength)
      historyRankThreshold = historyRankList.lastOption.map(_.k).getOrElse(-1)
      historyRankMap = historyRankList.map(s => s.id -> s).toMap
    }

  }

  override def feedApple(appleCount: Int): Unit = {
    feededApples = Nil
    var appleNeeded = snakes.size * 2 + appleNum - appleCount
    while (appleNeeded > 0) {
      val p = randomEmptyPoint()
      val score = random.nextDouble() match {
        case x if x > 0.95 => 10
        case x if x > 0.8 => 5
        case x => 1
      }
      val apple = Apple(score, appleLife)
      feededApples ::= Ap(score, appleLife, p.x, p.y)
      grid += (p -> apple)
      appleNeeded -= 1
    }
  }

  override def update(): Unit = {
    super.update()
    genWaitingSnake()
    updateRanks()
  }

  def getFeededApple = feededApples

}
