package otpproju.config;

import otpproju.repository.FlashcardRepository;
import otpproju.repository.FlashcardSetRepository;
import otpproju.repository.UserRepository;
import otpproju.service.FlashcardService;
import otpproju.service.FlashcardSetService;
import otpproju.service.UserService;

public final class ApplicationServices {

    private final UserService userService;
    private final FlashcardSetService flashcardSetService;
    private final FlashcardService flashcardService;

    private ApplicationServices() {
        UserRepository userRepository =
                new UserRepository();

        FlashcardSetRepository setRepository =
                new FlashcardSetRepository();

        FlashcardRepository cardRepository =
                new FlashcardRepository();

        userService = new UserService(userRepository);

        flashcardSetService =
                new FlashcardSetService(setRepository);

        flashcardService = new FlashcardService(
                cardRepository,
                setRepository
        );
    }

    public static ApplicationServices getInstance() {
        return Holder.INSTANCE;
    }

    public UserService getUserService() {
        return userService;
    }

    public FlashcardSetService getFlashcardSetService() {
        return flashcardSetService;
    }

    public FlashcardService getFlashcardService() {
        return flashcardService;
    }

    public boolean isDatabaseAvailable() {
        return DatabaseConnection.testConnection();
    }

    private static class Holder {
        private static final ApplicationServices INSTANCE =
                new ApplicationServices();
    }
}