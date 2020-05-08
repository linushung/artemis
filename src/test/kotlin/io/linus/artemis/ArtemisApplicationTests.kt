package io.linus.artemis

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

/* Ref: https://spring.io/guides/gs/testing-web/ */
/* Ref: https://spring.io/blog/2016/04/15/testing-improvements-in-spring-boot-1-4 */
/* Ref: https://docs.spring.io/spring-framework/docs/current/spring-framework-reference/testing.html */
/* Issue: https://stackoverflow.com/questions/38890944/does-webmvctest-require-springbootapplication-annotation */
/* Issue: https://stackoverflow.com/questions/56712707/springboottest-vs-contextconfiguration-vs-import-in-spring-boot-unit-test */

@SpringBootTest
class ArtemisApplicationTests {

	@Test
	fun contextLoads() {
	}

}
