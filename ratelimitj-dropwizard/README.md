RateLimitJ - Dropwizard
======================

The RateLimitJ Dropwizard module provides integration with Dropwizard and annotation based configuration
 
### Setup
 
 ```xml
 <dependency>
   <groupId>es.moki.ratelimitj</groupId>
   <artifactId>ratelimitj-dropwizard</artifactId>
   <version>${ratelimitj-dropwizard.version}</version>
 </dependency>
 <dependency>
   <groupId>es.moki.ratelimitj</groupId>
   <artifactId>ratelimitj-redis</artifactId>
   <version>${ratelimitj-redis.version}</version>
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

