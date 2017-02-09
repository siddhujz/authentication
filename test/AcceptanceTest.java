import org.junit.Test;

import play.i18n.Messages;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;
import static play.test.Helpers.*;

public class AcceptanceTest {

    /**
     * in this example we just check if the welcome page is being shown
     */
    @Test
    public void test() {
        running(testServer(3333, fakeApplication(inMemoryDatabase())), HTMLUNIT, browser -> {
            browser.goTo("http://localhost:3333");
            assertThat(browser.pageSource(), containsString("Login"));
            assertThat(browser.pageSource(), containsString("Register"));
        });
    }

}