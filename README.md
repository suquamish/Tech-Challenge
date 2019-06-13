# Tech Challenge

Build a RESTful service using java and the spring framework to manage users. You can use any sort of persistence including in memory for storing the user data.

A user model must have at least these fields:
   - username
   - name
   - email
   
The solution [must] be able to meet the following needs:
   - Find a user by username
   - Create a user
   - Delete a user
   - Update a user's email and name
   
You have 24 hours... Go!
___

# Solution

This project is Gradle based, with a wrapper.  From your desktop, you should be able to compile, test, and run this project.
_(all instructions are assuming a Linux commandline)_

### Compiling

From the project root:
```
$ ./gradlew clean build
```
*`gradlew.bat` is used for Windows.*

`gradlew` will downlownd all necessary components of Gradle to run the build tasks. By default the `build` task runs the entire test suite. You should see something like:

```
BUILD SUCCESSFUL in 15s
6 actionable tasks: 6 executed
```

### Starting the application

After building (see above), starting the server from the project root is as simple as:

```
java -jar build/libs/TechChallenge-0.0.1.jar
```

If everything goes according to plan, you should see something like:
```json
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::        (v2.1.5.RELEASE)

2019-06-12 16:28:14.655  INFO 9972 --- [           main] o.d.T.TechChallengeApplication           : Starting TechChallengeApplication on miu with PID 9972 (/home/thom/Development/tech_challenge/build/libs/TechChallenge-0.0.1.jar started by thom in /home/thom/Development/tech_challenge)
2019-06-12 16:28:14.659  INFO 9972 --- [           main] o.d.T.TechChallengeApplication           : No active profile set, falling back to default profiles: default
2019-06-12 16:28:16.247  INFO 9972 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port(s): 8080 (http)
2019-06-12 16:28:16.288  INFO 9972 --- [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2019-06-12 16:28:16.289  INFO 9972 --- [           main] org.apache.catalina.core.StandardEngine  : Starting Servlet engine: [Apache Tomcat/9.0.19]
2019-06-12 16:28:16.392  INFO 9972 --- [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2019-06-12 16:28:16.392  INFO 9972 --- [           main] o.s.web.context.ContextLoader            : Root WebApplicationContext: initialization completed in 1683 ms
2019-06-12 16:28:16.830  INFO 9972 --- [           main] pertySourcedRequestMappingHandlerMapping : Mapped URL path [/v2/api-docs] onto method [public org.springframework.http.ResponseEntity<springfox.documentation.spring.web.json.Json> springfox.documentation.swagger2.web.Swagger2Controller.getDocumentation(java.lang.String,javax.servlet.http.HttpServletRequest)]
2019-06-12 16:28:16.964  INFO 9972 --- [           main] o.s.s.concurrent.ThreadPoolTaskExecutor  : Initializing ExecutorService 'applicationTaskExecutor'
2019-06-12 16:28:17.328  INFO 9972 --- [           main] d.s.w.p.DocumentationPluginsBootstrapper : Context refreshed
2019-06-12 16:28:17.355  INFO 9972 --- [           main] d.s.w.p.DocumentationPluginsBootstrapper : Found 1 custom documentation plugin(s)
2019-06-12 16:28:17.398  INFO 9972 --- [           main] s.d.s.w.s.ApiListingReferenceScanner     : Scanning for api listing references
2019-06-12 16:28:17.613  INFO 9972 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8080 (http) with context path ''
2019-06-12 16:28:17.617  INFO 9972 --- [           main] o.d.T.TechChallengeApplication           : Started TechChallengeApplication in 3.324 seconds (JVM running for 3.746)
```
The last two lines being markers of a successful startup.

### Making requests to the application

`springfox-swagger` documentation is provided, assuming defaults, at http://localhost:8080/swagger-ui.html

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

Creating or Updating data is done via an HTTP `POST`. Deleting data is via an HTTP `DELETE`

You can create new users via `.../`

To create a new user, you need to `POST` a user model in JSON format with the `name`, `username`, and `email` attributes.

You can update existing users via `.../id/{user id uuid}`

To update an existing user, you need to `POST` a fully populated user model in JSON format, including the `id`.

You can delete an existing user via `.../id/{user id uuid}` 

### Request Examples

Below are a few curl examples, but the best documentation is the swagger ui ([see above](#making-requests-to-the-application))

#### Create a new user
`curl -X POST -H 'Content-Type: application/json' -d '{"username": "suquamish", "email": "thom.dieterich@example.com", "name": "Thom Dieterich" }' http://localhost:8080/users`
```json
{"id":"30532965-6f5b-4aaa-aad1-bbcc9db4c2a5","username":"suquamish","name":"Thom Dieterich","email":"thom.dieterich@example.com"}
```
#### Retreive an existing user by username
`curl -H 'Accept: application/json' http://localhost:8080/users/username/suquamish`
```json
{"id":"30532965-6f5b-4aaa-aad1-bbcc9db4c2a5","username":"suquamish","name":"Thom Dieterich","email":"thom.dieterich@example.com"}
```

#### Update an existing user
`curl -X POST -H 'Content-Type: application/json' -d '{"username": "thom.dieterich", "email": "thom@example.com", "name": "Thom Dieterich","id":"30532965-6f5b-4aaa-aad1-bbcc9db4c2a5"}' http://localhost:8080/users/id/30532965-6f5b-4aaa-aad1-bbcc9db4c2a5`
```json
{"id":"30532965-6f5b-4aaa-aad1-bbcc9db4c2a5","username":"thom.dieterich","name":"Thom Dieterich","email":"thom@example.com"}
```

#### Retreive an existing user by user id
`curl -H 'Accept: application/json' http://localhost:8080/users/id/30532965-6f5b-4aaa-aad1-bbcc9db4c2a5`
```json
{"id":"30532965-6f5b-4aaa-aad1-bbcc9db4c2a5","username":"thom.dieterich","name":"Thom Dieterich","email":"thom@example.com"}
```

#### Deleting data
`curl -v -H 'Accept: application/json' http://localhost:8080/users/id/30532965-6f5b-4aaa-aad1-bbcc9db4c2a5`
```bash
*   Trying 127.0.0.1...
* Connected to localhost (127.0.0.1) port 8080 (#0)
> DELETE /users/id/afc25f07-2421-4f53-a210-2068646357b4/delete HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/7.47.0
> Accept: */*
> Content-Type: application/json
> 
< HTTP/1.1 200 
< Content-Length: 0
< Date: Mon, 03 Jun 2019 20:37:04 GMT
< 
* Connection #0 to host localhost left intact
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

trying to create a new user with an already existing username

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

I opted for in memory *persistence* option, so each time you restart the service, you've got a clean slate.

The server retains it's default port of 8080 to bind to. If you've got something else running on that port you'll need change the server port by setting a server.port property:

`java -jar -Dserver.port=8081 build/libs/TechChallenge-0.0.1.jar`
