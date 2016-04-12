import akka.actor.{ ActorPath, ActorSystem }
import com.google.inject.{ Inject, Singleton }
import play.api.Configuration

import scala.concurrent.ExecutionContext

@Singleton
class SharedJournalStarter @Inject() (actorSystem: ActorSystem, executionContext: ExecutionContext, configuration: Configuration)
    extends SharedJournalSupport {

  val clusterPort = configuration.getInt("akka.remote.netty.tcp.port")

  startupSharedJournal(
    startStore = clusterPort.contains(2551),
    path       = ActorPath.fromString("akka.tcp://application@127.0.0.1:2551/user/store")
  )(actorSystem, executionContext)

}
