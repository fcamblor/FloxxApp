package engine

import fixtures.HttpAppFixture
import io.circe.generic.auto._
import org.floxx.env.api.ApiTask
import org.floxx.env.api.entriesPointApi.LoginResquest
import org.floxx.env.repository.userRepository
import org.floxx.env.service.securityService.AuthenticatedUser
import repository.UserRepositoryTest.users
import sttp.capabilities.fs2.Fs2Streams
import sttp.client3._
import sttp.client3.circe._
import zio._
import zio.test.Assertion.isRight
import zio.test.environment.TestEnvironment
import zio.test._

object EngineTest extends DefaultRunnableSpec with HttpAppFixture {
  import userRepository.insertUsers

  override def spec: Spec[TestEnvironment, TestFailure[Throwable], TestSuccess] =
    suite("Engine")(
      testM("login") {
        for {
          _ <- insertUsers(users)
          backend <- ZIO.service[SttpBackend[ApiTask, Fs2Streams[ApiTask]]]
          response <- backend
            .send(
              basicRequest
                .post(uri"/login")
                .body(
                  LoginResquest("fsznaj", "123")
                )
                .response(asJson[AuthenticatedUser])
            )
        } yield assert(response)(isRight[AuthenticatedUser])
      }
    ).provideCustomLayerShared((appTestEnvironment >+> userRepository.layer).orDie)
}
