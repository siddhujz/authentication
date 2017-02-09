import org.junit.*;

import play.i18n.Messages;
import play.libs.ws.WS;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.mvc.*;
import play.test.*;

import static play.test.Helpers.*;
import static org.junit.Assert.*;
import static org.fluentlenium.core.filter.FilterConstructor.*;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.concurrent.CompletionStage;


/**
 * Integration testing that involves starting up an application or a server.
 * <p>
 * https://www.playframework.com/documentation/2.5.x/JavaFunctionalTest
 */
public class IntegrationTest extends WithServer {

    /**
     * add your integration test here
     * in this example we just check if the welcome page is being shown
     */
    @Test
    public void test() {
        running(testServer(3333, fakeApplication(inMemoryDatabase())), HTMLUNIT, browser -> {
            browser.goTo("http://localhost:3333");
            assertTrue(browser.pageSource().contains(Messages.get("login")));
            assertTrue(browser.pageSource().contains(Messages.get("register")));
        });
    }

    @Test
    public void testInServerThroughUrl() throws Exception {
        // Tests using a scoped WSClient to talk to the server through a port.
        try (WSClient ws = WS.newClient(this.testServer.port())) {
            CompletionStage<WSResponse> stage = ws.url("/").get();
            WSResponse response = stage.toCompletableFuture().get();
            String body = response.getBody();
            assertThat(body, containsString(Messages.get("login")));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testInServerThroughApp() throws Exception {
        // Tests using the internal application available in the server.
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(GET)
                .uri("/");

        Result result = route(app, request);
        final String body = contentAsString(result);
        assertThat(body, containsString(Messages.get("login")));
    }

}