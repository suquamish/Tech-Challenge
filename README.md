# WSECU Tech Challenge

Build a RESTful service using java and the spring framework to manage users. You can use any sort of persistence including in memory for storing the user data.

A user model must have at least these fields:
   - username
   - name
   - email
   
## Instructions

This project is Gradle based, with a wrapper.  From your desktop, you should be able to compile, test, and run this project.
_(all instructions are assuming a Linux commandline)_

### Compiling

From the project root:
```
$ ./gradlew clean build
```
`gradlew.bat` is used for Windows.

This will downlownd all necessary components of Gradle to run the tasks. By default the `build` task runs the entire test suite. You should see something like:

```
BUILD SUCCESSFUL in 15s
6 actionable tasks: 6 executed
```

### Starting the application

After building (see above), starting the server from the project root is as simple as:

```
java -jar build/libs/WSECUTechChallenge-0.0.1.jar
```

### Making request to the application

The root path for getting and setting user data is `/users`

As user's data is returned in the default JSON format.  An example `User` might look like:
```json
{
  "id": "30532965-6f5b-4aaa-aad1-bbcc9db4c2a5",
  "username": "suquamish",
  "name": "Thom Dieterich",
  "email": "thom.dieterich@example.com"
}
```

### Getting data

Reading data is done via an HTTP `GET`

You can retrieve users by username `.../username/{username}` or id `.../id/{user id}`

### Setting data

Creating or Updating data is done via an HTTP `POST`

You can create new users via `.../create-new`

To create a new user, you need to `POST` a user model in JSON format with the `name`, `username`, and `email` attributes.

You can update existing users via `.../update-existing`

To update an existing user, you need to `POST` a fully populated user model in JSON format, including the `id`.

### Request Examples

#### Create a new user
`curl -X POST -H 'Content-Type: application/json' -d '{"username": "suquamish", "email": "thom.dieterich@example.com", "name": "Thom Dieterich" }' http://localhost:8080/users/create-new`
```json
{"id":"30532965-6f5b-4aaa-aad1-bbcc9db4c2a5","username":"suquamish","name":"Thom Dieterich","email":"thom.dieterich@example.com"}
```
#### Retreive an existing user by username
`curl -H 'Accept: application/json' http://localhost:8080/users/username/suquamish`
```json
{"id":"30532965-6f5b-4aaa-aad1-bbcc9db4c2a5","username":"suquamish","name":"Thom Dieterich","email":"thom.dieterich@example.com"}
```

#### Update an existing user
`curl -X POST -H 'Content-Type: application/json' -d '{"username": "thom.dieterich", "email": "thom@example.com", "name": "Thom Dieterich","id":"30532965-6f5b-4aaa-aad1-bbcc9db4c2a5"}' http://localhost:8080/users/update-existing`
```json
{"id":"30532965-6f5b-4aaa-aad1-bbcc9db4c2a5","username":"thom.dieterich","name":"Thom Dieterich","email":"thom@example.com"}
```

#### Retreive an existing user by user id
`curl -H 'Accept: application/json' http://localhost:8080/users/id/30532965-6f5b-4aaa-aad1-bbcc9db4c2a5`
```json
{"id":"30532965-6f5b-4aaa-aad1-bbcc9db4c2a5","username":"thom.dieterich","name":"Thom Dieterich","email":"thom@example.com"}
```
#### Making a bad request

trying to get a user with a bad id

`curl -v -H 'Accept: application/json' http://localhost:8080/users/id/borked`
```bash
*   Trying 127.0.0.1...
* Connected to localhost (127.0.0.1) port 8080 (#0)
> GET /users/id/borked HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/7.47.0
> Accept: application/json
> 
< HTTP/1.1 404 
< Content-Type: application/json;charset=UTF-8
< Transfer-Encoding: chunked
< Date: Mon, 03 Jun 2019 19:57:55 GMT
< 
* Connection #0 to host localhost left intact
{"statusString":"Not Found","errorMessage":"Cannot find any matching user"}
```

trying to create a new when there's one with an already existing username

`curl -v -XPOST -H 'Content-Type: application/json' -d '{"username":"suquamish","email":"thom@example.com","name":"Thom Dieterich","id":"30532965-6f5b-4aaa-aad1-bbcc9db4c2a5"}' http://localhost:8080/users/create-new`
```bash
*   Trying 127.0.0.1...
* Connected to localhost (127.0.0.1) port 8080 (#0)
> POST /users/create-new HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/7.47.0
> Accept: */*
> Content-Type: application/json
> Content-Length: 119
> 
* upload completely sent off: 119 out of 119 bytes
< HTTP/1.1 400 
< Content-Type: application/json;charset=UTF-8
< Transfer-Encoding: chunked
< Date: Mon, 03 Jun 2019 20:02:03 GMT
< Connection: close
< 
* Closing connection 0
{"statusString":"Unable to complete","errorMessage":"User data provided must be unique"}
```

### Caveats

The server retains it's default port of 8080 to bind to. If you've got something else running on that port you'll need change the server port by setting a server.port property:

`java -jar -Dserver.port=8081 build/libs/WSECUTechChallenge-0.0.1.jar`

The `UserControlerIntegrationTest` is marked as `@Ignored`.  As mentioned in one of my commits:

> Sometimes spring is slow to start, spring-test allows the test suite to
> begin executing before the server starts, resulting in false failures.

I've run into this before, and running the server in the background before
executing the test should keep things reliable.

```
$ ./gradlew clean test

   BUILD SUCCESSFUL in 5s
   6 actionable tasks: 6 executed
```