package kim.half.graduated

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class GraduatedApplication

fun main(args: Array<String>) {
    runApplication<GraduatedApplication>(*args)
}
