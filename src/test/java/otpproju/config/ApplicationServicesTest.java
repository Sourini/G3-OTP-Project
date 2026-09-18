package otpproju.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationServicesTest {

    @Test
    void returnsSameApplicationServicesInstance() {
        ApplicationServices first =
                ApplicationServices.getInstance();

        ApplicationServices second =
                ApplicationServices.getInstance();

        assertSame(first, second);
    }

    @Test
    void providesAllBackendServices() {
        ApplicationServices services =
                ApplicationServices.getInstance();

        assertNotNull(services.getUserService());
        assertNotNull(
                services.getFlashcardSetService()
        );
        assertNotNull(services.getFlashcardService());
    }

    @Test
    void reportsAvailableDatabase() {
        ApplicationServices services =
                ApplicationServices.getInstance();

        assertTrue(services.isDatabaseAvailable());
    }
}