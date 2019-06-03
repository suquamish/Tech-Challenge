package org.dieterich.WSECUTechChallenge

import groovy.json.JsonSlurper
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

@SpringBootTest
class UserControllerIntegrationTest extends Specification {
    URL client
    HttpURLConnection connection

    def "I can request a user that does not exist, and get an acceptable error"() {
        setup:
        client = new URL("http://localhost:8080/users/username/joe")
        connection = client.openConnection()
        connection.setRequestMethod("GET")
        connection.connect()


        expect:
        assert '{"statusString":"Not Found","errorMessage":"Cannot find any matching user"}' == connection.errorStream.getText()
        assert 404 == connection.getResponseCode()
    }

    def "I can create a user"() {
        setup:
        client = new URL("http://localhost:8080/users/create-new")
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

        when: "I request that user, they should exist"
        client = new URL("http://localhost:8080/users/username/joe")
        connection = client.openConnection()
        connection.setRequestMethod("GET")
        connection.connect()
        def checkUserData = new JsonSlurper().parseText(connection.inputStream.text)

        then:
        assert 200 == connection.getResponseCode()
        assert checkUserData.id == userData.id
        assert checkUserData.name == userData.name
        assert checkUserData.email == userData.email
        assert checkUserData.username == userData.username

        when: "I request that user by the assigned id, they should exist"
        client = new URL("http://localhost:8080/users/id/${userData.id}")
        connection = client.openConnection()
        connection.setRequestMethod("GET")
        connection.connect()
        checkUserData = new JsonSlurper().parseText(connection.inputStream.text)

        then:
        assert 200 == connection.getResponseCode()
        assert checkUserData.id == userData.id
        assert checkUserData.name == userData.name
        assert checkUserData.email == userData.email
        assert checkUserData.username == userData.username
    }

    def "I can update a user"() {
        setup:
        client = new URL("http://localhost:8080/users/create-new")
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
        client = new URL("http://localhost:8080/users/update-existing")
        connection = client.openConnection()
        connection.doOutput = true
        connection.setRequestMethod("POST")
        connection.setRequestProperty("Content-Type", "application/json")
        connection.outputStream.withPrintWriter { writer ->
            writer.write(updateJson)
        }

        then:
        assert 200 == connection.getResponseCode()

        when: "I request that user by the assigned id, they should exist with the new data"
        client = new URL("http://localhost:8080/users/id/${userData.id}")
        connection = client.openConnection()
        connection.setRequestMethod("GET")
        connection.connect()
        def checkUserData = new JsonSlurper().parseText(connection.inputStream.text)

        then:
        assert 200 == connection.getResponseCode()
        assert checkUserData.id == userData.id
        assert checkUserData.name == "Just A. User"
        assert checkUserData.email == "different.email@example.com"
        assert checkUserData.username == "w00t"
    }
}
