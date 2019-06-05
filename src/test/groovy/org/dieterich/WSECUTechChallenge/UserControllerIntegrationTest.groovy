package org.dieterich.WSECUTechChallenge

import groovy.json.JsonSlurper
import org.springframework.boot.SpringApplication
import org.springframework.context.ConfigurableApplicationContext
import spock.lang.Shared
import spock.lang.Specification

class UserControllerIntegrationTest extends Specification {
    URL client
    HttpURLConnection connection

    @Shared
    ConfigurableApplicationContext context

    def setupSpec() {
        String[] args = []
        context = SpringApplication.run(WSECUTechChallengeApplication.class, args)
        context.registerShutdownHook()
    }

    def cleanupSpec() {
        if (context && context.isRunning()) context.stop()
    }

    def "I can request a user that does not exist, and get an acceptable error"() {
        setup:
        client = new URL("http://localhost:8080/users/username/joe")
        connection = client.openConnection()
        connection.setRequestMethod("GET")
        connection.connect()


        expect:
        assert 404 == connection.getResponseCode()
        assert '{"statusString":"Not Found","errorMessage":"Cannot find any matching user"}' == connection.errorStream.getText()
    }

    def "I can create a user"() {
        setup:
        client = new URL("http://localhost:8080/users")
        connection = client.openConnection()
        connection.doOutput = true
        connection.setRequestMethod("POST")
        connection.setRequestProperty("Content-Type", "application/json")
        connection.outputStream.withPrintWriter { writer ->
            writer.write('{"name":"Joe User","email":"joe.user@example.com","username":"joe"}')
        }

        when:
        connection.connect()
        def userData = new JsonSlurper().parseText(connection.inputStream.text)

        then:
        assert 200 == connection.getResponseCode()
        assert userData.containsKey("id") && UUID.fromString(userData.id)
        assert userData.containsKey("name") && userData.name == "Joe User"
        assert userData.containsKey("email") && userData.email == "joe.user@example.com"
        assert userData.containsKey("username") && userData.username == "joe"

        when: "I request that user by username"
        client = new URL("http://localhost:8080/users/username/joe")
        connection = client.openConnection()
        connection.setRequestMethod("GET")
        connection.connect()
        def checkUserData = new JsonSlurper().parseText(connection.inputStream.text)

        then: "they should exist"
        assert 200 == connection.getResponseCode()
        assert checkUserData.id == userData.id
        assert checkUserData.name == userData.name
        assert checkUserData.email == userData.email
        assert checkUserData.username == userData.username

        when: "I request that user by the assigned id"
        client = new URL("http://localhost:8080/users/id/${userData.id}")
        connection = client.openConnection()
        connection.setRequestMethod("GET")
        connection.connect()
        checkUserData = new JsonSlurper().parseText(connection.inputStream.text)

        then: "they should exist"
        assert 200 == connection.getResponseCode()
        assert checkUserData.id == userData.id
        assert checkUserData.name == userData.name
        assert checkUserData.email == userData.email
        assert checkUserData.username == userData.username
    }

    def "I can update a user"() {
        setup:
        client = new URL("http://localhost:8080/users")
        connection = client.openConnection()
        connection.doOutput = true
        connection.setRequestMethod("POST")
        connection.setRequestProperty("Content-Type", "application/json")
        connection.outputStream.withPrintWriter { writer ->
            writer.write('{"name":"Another User","email":"another.user@example.com","username":"w00t"}')
        }
        connection.connect()
        def userData = new JsonSlurper().parseText(connection.inputStream.text)
        def updateJson = '{"id":"' + userData.id + '","name":"Just A. User","email":"different.email@example.com","username":"w00t"}'

        when:
        client = new URL("http://localhost:8080/users/id/${userData.id}")
        connection = client.openConnection()
        connection.doOutput = true
        connection.setRequestMethod("POST")
        connection.setRequestProperty("Content-Type", "application/json")
        connection.outputStream.withPrintWriter { writer ->
            writer.write(updateJson)
        }

        then:
        assert 200 == connection.getResponseCode()

        when: "I request that user by the assigned id"
        client = new URL("http://localhost:8080/users/id/${userData.id}")
        connection = client.openConnection()
        connection.setRequestMethod("GET")
        connection.connect()
        def checkUserData = new JsonSlurper().parseText(connection.inputStream.text)

        then: "that user should exist with the new data"
        assert 200 == connection.getResponseCode()
        assert checkUserData.id == userData.id
        assert checkUserData.name == "Just A. User"
        assert checkUserData.email == "different.email@example.com"
        assert checkUserData.username == "w00t"
    }

    def "I can delete a user"() {
        setup:
        client = new URL("http://localhost:8080/users")
        connection = client.openConnection()
        connection.doOutput = true
        connection.setRequestMethod("POST")
        connection.setRequestProperty("Content-Type", "application/json")
        connection.outputStream.withPrintWriter { writer ->
            writer.write('{"name":"Short Term User","email":"shorty@example.com","username":"byebye"}')
        }
        connection.connect()
        def userData = new JsonSlurper().parseText(connection.inputStream.text)

        when:
        client = new URL("http://localhost:8080/users/id/${userData.id}")
        connection = client.openConnection()
        connection.setRequestMethod("DELETE")
        connection.connect()

        then:
        assert 200 == connection.getResponseCode()
        assert connection.inputStream.text.isEmpty()

        when: "I request that user id"
        client = new URL("http://localhost:8080/users/id/${userData.id}")
        connection = client.openConnection()
        connection.setRequestMethod("GET")
        connection.connect()

        then: "I should get a 404"
        assert 404 == connection.getResponseCode()

        when: "I request that username"
        client = new URL("http://localhost:8080/users/username/${userData.username}")
        connection = client.openConnection()
        connection.setRequestMethod("GET")
        connection.connect()

        then: "I should get a 404"
        assert 404 == connection.getResponseCode()
    }
}
