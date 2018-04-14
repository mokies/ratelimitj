import es.moki.ratelimitj.core.limiter.request.RequestLimitRule;
import es.moki.ratelimitj.core.limiter.request.RequestRateLimiter;
import es.moki.ratelimitj.inmemory.request.InMemorySlidingWindowRequestRateLimiter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

/**
 * This servlet limits a single remote IP address to 10 request per minute
 */
public class ServletRateLimitedExample extends HttpServlet {

    static final long serialVersionUID = 123L;

    // transient to keep findbugs happy.
    private final transient RequestRateLimiter rateLimiter = new InMemorySlidingWindowRequestRateLimiter(
            RequestLimitRule.of(1, TimeUnit.MINUTES, 10) );

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        // This won't work if you are HTTP proxies fronting the servlet
        String clientIp = request.getRemoteAddr();

        if (rateLimiter.overLimitWhenIncremented("remote-ip:" + clientIp)) {

            response.setStatus(429);

        } else {

            response.setStatus(200);
            response.setContentType("text/html;charset=UTF-8");
            try (PrintWriter out = response.getWriter()) {
                out.println("hello RateLimiterJ");

            }
        }
    }

}
