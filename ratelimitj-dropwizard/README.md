RateLimitJ - Dropwizard
======================

The RateLimitJ Dropwizard module provides RateLimitJ integration with Dropwizard
 
### Setup
 
 ```xml
 <dependency>
   <groupId>es.moki.ratelimitj</groupId>
   <artifactId>ratelimitj-dropwizard</artifactId>
   <version>x.x.x</version>
 </dependency>
 ```
 
### Usage

#### Basic Configuration Example
```java
public class RateLimitApplication extends Application<Configuration> {

    public void initialize(Bootstrap<Configuration> bootstrap) {
        RedisClient redisClient = RedisClient.create("redis://localhost");
        RateLimiterFactory factory = new RedisRateLimiterFactory(redisClient);

        bootstrap.addBundle(new RateLimitBundle(factory));
    }

    @Override
    public void run(Configuration configuration, Environment environment) throws Exception {

        environment.jersey().register(new LoginResource());
    }
}
```

#### Basic Usage Example
```java
@Path("/login")
@Consumes(MediaType.APPLICATION_JSON)
public class LoginResource {

    @POST
    @RateLimited(key = Key.DEFAULT, rates = {
            @Rate(duration = 10, timeUnit = TimeUnit.HOURS, limit = 5)
    })
    public Response login(final LoginRequest login) {

        return Response.ok().build();
    }
}
```


### Dependencies

* Java 8

