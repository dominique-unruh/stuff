package stuff

import java.nio.file.Path

object Paths {
  val dbPath: Path = Path.of("../my-stuff").toAbsolutePath.normalize()
  val filesPath: Path = dbPath.resolve("files")
}
