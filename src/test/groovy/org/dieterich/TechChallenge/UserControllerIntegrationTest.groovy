package org.dieterich.TechChallenge

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
        context = SpringApplication.run(TechChallengeApplication.class, args)
        context.registerShutdownHook()
    }

    def cleanupSpec() {
        if (context && context.isRunning()) context.stop()
    }

    def "I can request a user, by username, that does not exist, and get an acceptable error"() {
        setup:
        client = new URL("http://localhost:8080/users/username/joe")
        connection = client.openConnection()
        connection.setRequestMethod("GET")
        connection.connect()


        expect:
        assert 404 == connection.getResponseCode()
        assert '{"statusString":"Not Found","errorMessage":"Cannot find any matching user"}' == connection.errorStream.getText()
    }

    def "I can request a user by id, that does not exist, and get an acceptable error if there is no user"() {
        setup:
        client = new URL("http://localhost:8080/users/id/${UUID.randomUUID().toString()}")
        connection = client.openConnection()
        connection.setRequestMethod("GET")
        connection.connect()


        expect:
        assert 404 == connection.getResponseCode()
        assert '{"statusString":"Not Found","errorMessage":"Cannot find any matching user"}' == connection.errorStream.getText()
    }

    def "I can create a user"() {
        setup:
        def userData = createUser("joe", "joe.user@example.com", "Joe User")
        client = new URL("http://localhost:8080/users/username/joe")
        connection = client.openConnection()
        connection.setRequestMethod("GET")
        connection.connect()
        def checkUserData = new JsonSlurper().parseText(connection.inputStream.text)

        expect:
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

    def "I get an acceptable error if I try to create a user with a duplicate username"() {
        setup:
        createUser("zan", "zan@wondertwins.example.com", "Zan")

        client = new URL("http://localhost:8080/users")
        connection = client.openConnection()
        connection.doOutput = true
        connection.setRequestMethod("POST")
        connection.setRequestProperty("Content-Type", "application/json")
        connection.outputStream.withPrintWriter { writer ->
            writer.write('{"name":"Jane","email":"jane@wondertwins.example.com","username":"zan"}')
        }
        connection.connect()
        def responseCode = connection.getResponseCode()
        def errorMessage = connection.errorStream.getText()

        expect:
        assert 400 == responseCode
        assert '{"statusString":"Unable to complete","errorMessage":"User data provided must be unique"}' == errorMessage
    }

    def "I can update a user"() {
        setup:
        def userData = createUser("w00t","another.user@example.com","Another User")
        def updateJson = '{"id":"' + userData.id + '","name":"Just A. User","email":"different.email@example.com","username":"w00t"}'

        when: "when post data to update the user user"
        client = new URL("http://localhost:8080/users/id/${userData.id}")
        connection = client.openConnection()
        connection.doOutput = true
        connection.setRequestMethod("POST")
        connection.setRequestProperty("Content-Type", "application/json")
        connection.outputStream.withPrintWriter { writer ->
            writer.write(updateJson)
        }

        then: "it should succeed"
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

    def "I get an acceptable error if I try to update a users username to an existing username"() {
        given:
        createUser("tornado-don", "don@tornadotwins.example.com", "Don Allen")
        def userData = createUser("tornado-dawn", "dawn@tornadotwins.example.com", "Dawn Allen")
        String badUpdate =  String.format('{"name":"%s","email":"%s","username":"%s"}', "Dawn Allen", "dawn@tornadotwins.example.com", "tornado-don")

        when:
        client = new URL("http://localhost:8080/users/id/${userData.id}")
        connection = client.openConnection()
        connection.doOutput = true
        connection.setRequestMethod("POST")
        connection.setRequestProperty("Content-Type", "application/json")
        connection.outputStream.withPrintWriter { writer ->
            writer.write(badUpdate)
        }
        connection.connect()
        def responseCode = connection.getResponseCode()
        def errorMessage = connection.errorStream.getText()

        then:
        assert 400 == responseCode
        assert '{"statusString":"Unable to complete","errorMessage":"User data provided must be unique"}' == errorMessage
    }

    def "I can delete a user"() {
        setup:
        def userData = createUser("byebye", "shorty@example,com", "Short Term User")

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

    def createUser(String username, String email, String name) {
        URL client
        HttpURLConnection connection

        client = new URL("http://localhost:8080/users")
        connection = client.openConnection()
        connection.doOutput = true
        connection.setRequestMethod("POST")
        connection.setRequestProperty("Content-Type", "application/json")
        connection.outputStream.withPrintWriter { writer ->
            writer.write(String.format('{"name":"%s","email":"%s","username":"%s"}', name, email, username))
        }
        connection.connect()
        assert 200 == connection.getResponseCode()
        def userData = new JsonSlurper().parseText(connection.inputStream.text)
        return userData
    }
}