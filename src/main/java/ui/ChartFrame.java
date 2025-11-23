package ui;

import database.JsonDatabaseManager;
import model.*;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class ChartFrame extends BaseFrame {
    private final String courseId;
    private final JsonDatabaseManager db = JsonDatabaseManager.getInstance();
    private Course course;
    private Map<String, List<Integer>> lessonScores;
    private Map<String, Integer> lessonCompletions;

    public ChartFrame(String courseId) {
        super("Course Analytics");
        this.courseId = courseId;
        this.course = db.findCourseById(courseId);
        setSize(900, 650);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        calculateStatistics();
        initComponents();
    }

    private void calculateStatistics() {
        lessonScores = new HashMap<>();
        lessonCompletions = new HashMap<>();

        for (Lesson l : course.getLessons()) {
            lessonScores.put(l.getLessonId(), new ArrayList<>());
            lessonCompletions.put(l.getLessonId(), 0);
        }

        // Gather quiz scores from all students
        for (Student student : db.getAllStudents()) {
            if (!student.getEnrolledCourses().contains(courseId)) continue;

            // Check completions
            CourseProgress progress = student.getProgress().get(courseId);
            if (progress != null) {
                for (String lessonId : progress.getCompletedLessons()) {
                    lessonCompletions.merge(lessonId, 1, Integer::sum);
                }
            }

            // Check quiz scores
            for (QuizAttempt attempt : student.getQuizAttempts()) {
                if (attempt.getCourseId().equals(courseId)) {
                    List<Integer> scores = lessonScores.get(attempt.getLessonId());
                    if (scores != null) {
                        scores.add(attempt.getScore());
                    }
                }
            }
        }
    }

    @Override
    protected void initComponents() {
        setLayout(new BorderLayout());

        // Header
        JPanel header = new JPanel();
        JLabel titleLbl = new JLabel("Analytics: " + course.getTitle());
        titleLbl.setFont(new Font("Dialog", Font.BOLD, 12));
        titleLbl.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        header.add(titleLbl);
        add(header, BorderLayout.NORTH);

        // Charts panel
        JPanel chartsPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        chartsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Bar chart - Quiz Averages
        chartsPanel.add(createQuizAveragesChart());
        // Bar chart - Completion rates
        chartsPanel.add(createCompletionChart());

        add(chartsPanel, BorderLayout.CENTER);

        // Stats panel
        JPanel statsPanel = createStatsPanel();
        add(statsPanel, BorderLayout.SOUTH);
    }

    private JPanel createQuizAveragesChart() {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth(), h = getHeight();
                int margin = 50, barWidth = 40, gap = 20;

                // Title
                g2d.setColor(Color.BLACK);
                g2d.setFont(new Font("Dialog", Font.BOLD, 12));
                g2d.drawString("Quiz Score Averages", margin, 20);

                List<Lesson> lessons = course.getLessons();
                if (lessons.isEmpty()) {
                    g2d.drawString("No lessons available", w/2 - 50, h/2);
                    return;
                }

                // Calculate averages
                List<Double> averages = new ArrayList<>();
                for (Lesson l : lessons) {
                    List<Integer> scores = lessonScores.get(l.getLessonId());
                    if (scores != null && !scores.isEmpty()) {
                        averages.add(scores.stream().mapToInt(Integer::intValue).average().orElse(0));
                    } else {
                        averages.add(0.0);
                    }
                }

                // Draw axes
                g2d.setColor(Color.DARK_GRAY);
                g2d.drawLine(margin, h - margin, w - margin, h - margin); // X
                g2d.drawLine(margin, margin + 20, margin, h - margin); // Y

                // Y axis labels
                g2d.setFont(new Font("Dialog", Font.PLAIN, 10));
                for (int i = 0; i <= 100; i += 25) {
                    int y = h - margin - (int)((h - 2*margin - 20) * i / 100.0);
                    g2d.drawString(i + "%", 10, y + 5);
                    g2d.setColor(Color.LIGHT_GRAY);
                    g2d.drawLine(margin, y, w - margin, y);
                    g2d.setColor(Color.DARK_GRAY);
                }

                // Draw bars
                int totalBarsWidth = lessons.size() * (barWidth + gap);
                int startX = (w - totalBarsWidth) / 2;

                for (int i = 0; i < lessons.size(); i++) {
                    double avg = averages.get(i);
                    int barH = (int)((h - 2*margin - 20) * avg / 100.0);
                    int x = startX + i * (barWidth + gap);
                    int y = h - margin - barH;

                    // Bar
                    g2d.setColor(Color.BLUE);
                    g2d.fillRect(x, y, barWidth, barH);

                    // Value on top
                    g2d.setColor(Color.BLACK);
                    g2d.setFont(new Font("Dialog", Font.PLAIN, 10));
                    String val = String.format("%.0f%%", avg);
                    g2d.drawString(val, x + barWidth/2 - 12, y - 5);

                    // Lesson label
                    String label = "L" + (i + 1);
                    g2d.drawString(label, x + barWidth/2 - 8, h - margin + 15);
                }
            }
        };
    }

    private JPanel createCompletionChart() {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth(), h = getHeight();
                int margin = 50, barWidth = 40, gap = 20;

                // Title
                g2d.setColor(Color.BLACK);
                g2d.setFont(new Font("Dialog", Font.BOLD, 12));
                g2d.drawString("Lesson Completion Rates", margin, 20);

                List<Lesson> lessons = course.getLessons();
                int totalStudents = course.getStudentCount();

                if (lessons.isEmpty() || totalStudents == 0) {
                    g2d.drawString("No data available", w/2 - 50, h/2);
                    return;
                }

                // Draw axes
                g2d.setColor(Color.DARK_GRAY);
                g2d.drawLine(margin, h - margin, w - margin, h - margin);
                g2d.drawLine(margin, margin + 20, margin, h - margin);

                // Y axis labels
                g2d.setFont(new Font("Dialog", Font.PLAIN, 10));
                for (int i = 0; i <= 100; i += 25) {
                    int y = h - margin - (int)((h - 2*margin - 20) * i / 100.0);
                    g2d.drawString(i + "%", 10, y + 5);
                    g2d.setColor(Color.LIGHT_GRAY);
                    g2d.drawLine(margin, y, w - margin, y);
                    g2d.setColor(Color.DARK_GRAY);
                }

                int totalBarsWidth = lessons.size() * (barWidth + gap);
                int startX = (w - totalBarsWidth) / 2;

                for (int i = 0; i < lessons.size(); i++) {
                    Lesson l = lessons.get(i);
                    int completed = lessonCompletions.getOrDefault(l.getLessonId(), 0);
                    double pct = (completed * 100.0) / totalStudents;
                    int barH = (int)((h - 2*margin - 20) * pct / 100.0);
                    int x = startX + i * (barWidth + gap);
                    int y = h - margin - barH;

                    g2d.setColor(Color.GREEN);
                    g2d.fillRect(x, y, barWidth, barH);

                    g2d.setColor(Color.BLACK);
                    g2d.setFont(new Font("Dialog", Font.PLAIN, 10));
                    String val = String.format("%.0f%%", pct);
                    g2d.drawString(val, x + barWidth/2 - 12, y - 5);

                    g2d.drawString("L" + (i + 1), x + barWidth/2 - 8, h - margin + 15);
                }
            }
        };
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 10));
        panel.setPreferredSize(new Dimension(getWidth(), 60));

        int totalStudents = course.getStudentCount();
        int totalLessons = course.getLessonCount();

        // Calculate overall completion
        int totalCompletions = lessonCompletions.values().stream().mapToInt(Integer::intValue).sum();
        double avgCompletion = totalStudents > 0 && totalLessons > 0 ?
                (totalCompletions * 100.0) / (totalStudents * totalLessons) : 0;

        // Calculate overall quiz average
        double overallQuizAvg = lessonScores.values().stream()
                .flatMap(List::stream)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0);

        panel.add(createStatBox("Enrolled Students", String.valueOf(totalStudents)));
        panel.add(createStatBox("Total Lessons", String.valueOf(totalLessons)));
        panel.add(createStatBox("Avg Completion", String.format("%.1f%%", avgCompletion)));
        panel.add(createStatBox("Avg Quiz Score", String.format("%.1f%%", overallQuizAvg)));

        return panel;
    }

    private JPanel createStatBox(String label, String value) {
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        JLabel valLbl = new JLabel(value);
        valLbl.setFont(new Font("Dialog", Font.BOLD, 14));
        valLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblLbl = new JLabel(label);
        lblLbl.setFont(new Font("Dialog", Font.PLAIN, 11));
        lblLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        box.add(Box.createVerticalStrut(5));
        box.add(valLbl);
        box.add(lblLbl);
        box.add(Box.createVerticalStrut(5));
        return box;
    }
}