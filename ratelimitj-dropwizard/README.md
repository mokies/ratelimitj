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

#### Basic Redis Configuration Example
```java
public class RateLimitApplication extends Application<Configuration> {

    public void initialize(Bootstrap<Configuration> bootstrap) {
        RedisClient redisClient = RedisClient.create("redis://localhost");
        RateLimiterFactory factory = new RedisRateLimiterFactory(redisClient);

        bootstrap.addBundle(new RateLimitBundle(factory));
    }
}
```

#### Basic InMemory Configuration Example
```java
public class RateLimitApplication extends Application<Configuration> {

    public void initialize(Bootstrap<Configuration> bootstrap) {
        RateLimiterFactor factory = new InMemoryRateLimiterFactory();

        bootstrap.addBundle(new RateLimitBundle(factory));
    }
}
```

#### Dropwizard Authenticated User Usage Example
```java
@Path("/user")
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    @GET
    @Path("/{id}")
    @RateLimited(key = Key.AUTHENTICATED, rates = {@Rate(duration = 10, timeUnit = TimeUnit.HOURS, limit = 10)})
    public Response getLimitedByAuthenticatedUser(@Auth PrincipalImpl principle, @PathParam("id") final Integer id) {
        return Response.ok().build();
    }
}
```

#### Dark Launch
When introducing rate limiters to a production environment it can be helpful to first evaluate request patterns to avoid over limiting.
To disable enforcement include the 'reportOnly = true' on the @RateLimit annotation.

```java
    @POST
    @RateLimited(
            reportOnly = true,
            key = Key.DEFAULT, 
            rates = { @Rate(duration = 10, timeUnit = TimeUnit.HOURS, limit = 5) })
```


### Dependencies

* Java 8

