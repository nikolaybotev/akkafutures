package tellorask

object IdiomaticActors extends App {

  import akka.actor.Actor
  import akka.actor.ActorRef
  import akka.actor.ActorSystem
  import akka.actor.Props
  import com.typesafe.config.ConfigFactory

  lazy val actorSystem = ActorSystem("test", ConfigFactory.parseString("""
      dispatchers {
        client.type = "PinnedDispatcher"
        service.type = "PinnedDispatcher"
      }
      """))

  def namedThreadActorOf(f: => Actor, name: String) =
    actorSystem.actorOf(Props(f).withDispatcher("dispatchers." + name))

  def log(msg: String) {
    println("[" + Thread.currentThread.getName + "] " + msg)
  }

  class ServiceActor extends Actor {
    def receive = { case x: Int =>
        log("Service processing " + x)
        sender ! (x * x)
    }
  }

  class ClientActor extends Actor {
    def receive = {
      case serviceProvider: ActorRef => serviceProvider ! 42
      case response: Int => log("Client got back " + response)
    }
  }

  val service = namedThreadActorOf(new ServiceActor, "service")
  val client = namedThreadActorOf(new ClientActor, "client")

  client ! service

}