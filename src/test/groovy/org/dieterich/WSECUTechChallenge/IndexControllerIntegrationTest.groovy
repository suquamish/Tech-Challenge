package org.dieterich.WSECUTechChallenge

import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

@SpringBootTest(classes = WSECUTechChallengeApplication.class)
class IndexControllerIntegrationTest extends Specification {
    def "first test"() {
        expect:
        Math.max(1, 2) == 3
    }
}
