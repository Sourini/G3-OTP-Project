# Database Localization Plan — Simply Flashcards

## Goal

Users can create and study the same flashcard set in multiple languages. Each set defines its supported languages and a default language. 

The study language is independent of the app’s interface language, so a learner can study Arabic cards while keeping the menus in English.

## Changes made

| Change                                                                       | Reason                                                                                                                                    |
|------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------|
| Added `flashcard_set_translations` for localized set names and descriptions. | A single set can have a name and description in each supported language without duplicating the set.                                      |
| Added `flashcard_translations` for localized question and answer pairs.      | Each card keeps one identity. The default question and answer remain in `flashcards`; other language versions are saved as separate rows. |
| Added `default_language_code` to `flashcard_sets`.                           | Language for the set used as the default / fallbabck choice.                                                                              |
| Used `UTF-8` encodign in the translation tables.                             | Database can store all supported languages.                                                                                               |

When creating a card, the author provides a question and answer for every language supported by the set. During study, the app loads the selected language. 

If a translation is missing, it falls back to using the default language for the set.

## The database is localized using row localization

Each translation is stored as a separate row in a dedicated translation table. A set ID or card ID combined with a language code identifies the translation. 

For example, the Finnish and Arabic translations of card 42 are two rows in the same `flashcard_translations` table: `(42, fi)` and `(42, ar)`.