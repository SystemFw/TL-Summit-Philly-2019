import cats._, implicits._
import cats.effect._, concurrent._
import cats.effect.implicits._

object Playground extends IOApp {
  def run(args: List[String]) = ExitCode.Success.pure[IO]

  def report(trace: Ref[IO, List[String]], msg: String) =
    for {
      t <- trace.get
      _ <- trace.set(msg :: t)
    } yield ()

  def flow =
    for {
      trace <- Ref[IO].of(List.empty[String])
      _ <- report(trace, "one").start
      _ <- report(trace, "two").start
    } yield ()

  def sprinter(name: String, finishLine: Ref[IO, Int]): IO[Unit] =
    for {
      pos <- finishLine.modify { old =>
        (old + 1, old)
      }
      _ <- IO { println(s"$name arrived at position $pos") }
    } yield ()

  def sprint = Ref[IO].of(0).flatMap { finishLine =>
    List(
      sprinter("A", finishLine),
      sprinter("B", finishLine),
      sprinter("C", finishLine)
    ).parSequence
  }

  trait Cached[F[_], A] {
    def get: F[A]
    def expire: F[Unit]
  }
  object Cached {
    def create[F[_]: Concurrent, A](fa: F[A]): F[Cached[F, A]] = {
      sealed trait State
      case class Value(v: A) extends State
      case class Updating(d: Deferred[F, Either[Throwable, A]])
          extends State
      case object NoValue extends State

      Ref.of[F, State](NoValue).map { state =>
        new Cached[F, A] {

          def get: F[A] = Deferred[F, Either[Throwable, A]].flatMap {
            newValue =>
              state.modify {
                case st @ Value(v) =>
                  st -> v.pure[F]
                case st @ Updating(inFlightValue) =>
                  st -> inFlightValue.get.rethrow
                case NoValue =>
                  Updating(newValue) -> fetch(newValue).rethrow
              }.flatten
            }

          def fetch(d: Deferred[F, Either[Throwable, A]]) = {
            for {
              r <- fa.attempt
              _ <- state.set {
                r match {
                  case Left(_) => NoValue
                  case Right(v) => Value(v)
                }
              }
              _ <- d.complete(r)
            } yield r
          }.guaranteeCase {
            case ExitCase.Completed => ().pure[F]
            case ExitCase.Error(_) => ().pure[F]
            case ExitCase.Canceled =>
              state.modify {
                case st @ Value(v) => st -> d.complete(v.asRight).attempt.void
                case NoValue | Updating(_) =>
                  val appropriateError = new Exception("Couldn't retrieve")
                  NoValue -> d.complete(appropriateError.asLeft).attempt.void
              }.flatten

          }

          def expire: F[Unit] = state.update {
            case Value(_) => NoValue
            case NoValue => NoValue
            case st @ Updating(_) => st
          }
        }
      }
    }
  }
}
