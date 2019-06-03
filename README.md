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

After building (see above), starting the server is as simple as:

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