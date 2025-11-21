package utils;

import java.util.concurrent.atomic.AtomicInteger;

public class IdGenerator {
    private static final AtomicInteger userCounter = new AtomicInteger(100);
    private static final AtomicInteger courseCounter = new AtomicInteger(100);
    private static final AtomicInteger lessonCounter = new AtomicInteger(100);
    private static final AtomicInteger quizCounter = new AtomicInteger(100);
    private static final AtomicInteger questionCounter = new AtomicInteger(100);
    private static final AtomicInteger certificateCounter = new AtomicInteger(100);

    private IdGenerator() {}

    public static String generateUserId() {
        return "USR" + String.format("%03d", userCounter.incrementAndGet());
    }

    public static String generateCourseId() {
        return "CRS" + String.format("%03d", courseCounter.incrementAndGet());
    }

    public static String generateLessonId() {
        return "LSN" + String.format("%03d", lessonCounter.incrementAndGet());
    }

    public static String generateQuizId() {
        return "QZ" + String.format("%03d", quizCounter.incrementAndGet());
    }

    public static String generateQuestionId() {
        return "Q" + String.format("%03d", questionCounter.incrementAndGet());
    }

    public static String generateCertificateId() {
        return "CERT" + String.format("%03d", certificateCounter.incrementAndGet());
    }

    public static void initializeCounters(int maxUserId, int maxCourseId,
                                          int maxLessonId, int maxQuizId, int maxQuestionId, int maxCertId) {
        userCounter.set(maxUserId);
        courseCounter.set(maxCourseId);
        lessonCounter.set(maxLessonId);
        quizCounter.set(maxQuizId);
        questionCounter.set(maxQuestionId);
        certificateCounter.set(maxCertId);
    }
}