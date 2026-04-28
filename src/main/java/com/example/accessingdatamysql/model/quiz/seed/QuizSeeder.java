package com.example.accessingdatamysql.model.quiz.seed;


import com.example.accessingdatamysql.model.quiz.entity.QuizOption;
import com.example.accessingdatamysql.model.quiz.entity.QuizQuestion;
import com.example.accessingdatamysql.model.quiz.enums.QuizDifficulty;
import com.example.accessingdatamysql.model.quiz.repository.QuizQuestionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class QuizSeeder implements CommandLineRunner {

    private final QuizQuestionRepository quizQuestionRepository;

    public QuizSeeder(QuizQuestionRepository quizQuestionRepository) {
        this.quizQuestionRepository = quizQuestionRepository;
    }

    @Override
    public void run(String... args) {
        if (quizQuestionRepository.count() > 0) {
            return;
        }

        seedEasyQuestions();
        seedMediumQuestions();
        seedHardQuestions();
    }

    private void seedEasyQuestions() {
        saveQuestion(
                "Vilken färg har en gran?",
                QuizDifficulty.EASY,
                "En gran är oftast grön.",
                "Blå", false,
                "Grön", true,
                "Rosa", false
        );

        saveQuestion(
                "Vad kan man plocka i skogen?",
                QuizDifficulty.EASY,
                "I skogen kan man till exempel plocka bär.",
                "Bär", true,
                "Godis", false,
                "Chips", false
        );

        saveQuestion(
                "Vilket djur säger \"mu\"?",
                QuizDifficulty.EASY,
                "Det är kon som säger mu.",
                "Hund", false,
                "Ko", true,
                "Katt", false
        );

        saveQuestion(
                "Vilken färg har löv på sommaren?",
                QuizDifficulty.EASY,
                "På sommaren är löven oftast gröna.",
                "Grön", true,
                "Lila", false,
                "Orange", false
        );

        saveQuestion(
                "Vilket djur kan flyga i skogen?",
                QuizDifficulty.EASY,
                "En fågel kan flyga.",
                "Fågel", true,
                "Hund", false,
                "Ko", false
        );
    }

    private void seedMediumQuestions() {
        saveQuestion(
                "Vilken färg har en flugsvamp?",
                QuizDifficulty.MEDIUM,
                "En flugsvamp är oftast röd med vita prickar.",
                "Röd med vita prickar", true,
                "Gul med blå prickar", false,
                "Helt svart", false
        );

        saveQuestion(
                "Vilket djur samlar honung?",
                QuizDifficulty.MEDIUM,
                "Det är bin som samlar honung.",
                "Bi", true,
                "Björn", false,
                "Fisk", false
        );

        saveQuestion(
                "Vad behöver en blomma för att växa?",
                QuizDifficulty.MEDIUM,
                "Blommor behöver vatten och sol för att växa bra.",
                "Kaffe", false,
                "Vatten och sol", true,
                "Saft", false
        );

        saveQuestion(
                "Vad händer med träden när det blir höst?",
                QuizDifficulty.MEDIUM,
                "På hösten ändrar löven färg och faller av.",
                "Det kommer nya blommor", false,
                "Löven ändrar färg och faller av", true,
                "Trädet ramlar omkull", false
        );

        saveQuestion(
                "Vart kan du hitta blåbär?",
                QuizDifficulty.MEDIUM,
                "Blåbär hittar man i skogen.",
                "Skogen", true,
                "Stranden", false,
                "Havet", false
        );
    }

    private void seedHardQuestions() {
        saveQuestion(
                "Vad heter djuret som sover länge på vintern?",
                QuizDifficulty.HARD,
                "Björnen går i ide under vintern.",
                "Björn", true,
                "Gris", false,
                "Ko", false
        );

        saveQuestion(
                "Vad kallas små nya blad på våren?",
                QuizDifficulty.HARD,
                "De kallas knoppar.",
                "Knoppar", true,
                "Knappar", false,
                "Droppar", false
        );

        saveQuestion(
                "Vilket djur kan man höra hacka på träd?",
                QuizDifficulty.HARD,
                "Hackspetten hackar på träd.",
                "Hackspett", true,
                "Ekorre", false,
                "Mus", false
        );

        saveQuestion(
                "Hur många ben har en spindel?",
                QuizDifficulty.HARD,
                "En spindel har 8 ben.",
                "32", false,
                "16", false,
                "8", true
        );

        saveQuestion(
                "Vilket djur bygger dammar i vatten?",
                QuizDifficulty.HARD,
                "Det är bävern som bygger dammar.",
                "Bäver", true,
                "Katt", false,
                "Ko", false
        );
    }

    private void saveQuestion(String questionText,
                              QuizDifficulty difficulty,
                              String explanation,
                              String optionA, boolean correctA,
                              String optionB, boolean correctB,
                              String optionC, boolean correctC) {

        QuizQuestion question = new QuizQuestion();
        question.setQuestionText(questionText);
        question.setDifficulty(difficulty);
        question.setActive(true);
        question.setImageUrl(null);
        question.setExplanation(explanation);

        addOption(question, optionA, correctA);
        addOption(question, optionB, correctB);
        addOption(question, optionC, correctC);

        quizQuestionRepository.save(question);
    }

    private void addOption(QuizQuestion question, String optionText, boolean correct) {
        QuizOption option = new QuizOption();
        option.setOptionText(optionText);
        option.setCorrect(correct);
        option.setImageUrl(null);
        option.setQuizQuestion(question);

        question.getOptions().add(option);
    }
}