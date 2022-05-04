package repository

import fixtures.PostgresRunnableFixture
import org.floxx.env.repository.userRepository
import org.floxx.model.AuthUser
import zio.test.Assertion.equalTo
import zio.test.environment.TestEnvironment
import zio.test._

object UserRepositoryTest extends DefaultRunnableSpec with PostgresRunnableFixture {
  import userRepository._

  val users = Seq(
    AuthUser(
      Option("aheritier"),
      "Arnaud",
      "Heritier",
      "aheritier@gmail.com",
      "123",
      true
    ),
    AuthUser(
      Option("fsznaj"),
      "fabrice",
      "Sznajderman",
      "fabszn@pm.me",
      "123"
    ),
    AuthUser(
      Option("mpierre"),
      "Michel",
      "Pierre",
      "mpierre@pm.me",
      "123"
    )
  )

  override def spec: Spec[TestEnvironment, TestFailure[Any], TestSuccess] =
    suite("Postgres UserRepository")(
      testM("Get all Users") {
        for {
          _ <- insertUsers(users)
          actual <- allUser
        } yield assert(actual.size)(equalTo(users.size))
      }
    ).provideCustomLayerShared((dbLayer >>> userRepository.layer).orDie)
}
