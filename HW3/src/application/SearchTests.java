package application;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

/**
 * <p>Title: ResolvedQuestionTests</p>
 *
 * <p>Description: Automated JUnit test cases for verifying resolved question functionality
 * including marking as resolved, viewing resolved threads, avoiding duplication,
 * and confirming correct student and answer attribution.</p>
 *
 * <p>Author: Ganesh Kolli</p>
 *
 * @version 1.1
 */
public class SearchTests {

    private List<Question> unresolved;
    private List<Question> resolved;
    private DatabaseHelperMock db;

    @BeforeEach
    void setup() {
        unresolved = new ArrayList<>();
        resolved = new ArrayList<>();
        db = new DatabaseHelperMock();
    }

    /**
     * Test Case 1: Mark Question as Resolved.
     * Expected: The question moves from unresolved list to resolved list.
     */
    @Test
    public void testMarkQuestionAsResolved() {
        Question q = new Question("How do I submit HW3?", "student1");
        unresolved.add(q);

        unresolved.remove(q);
        q.setResolved(true);
        resolved.add(q);

        assertFalse(unresolved.contains(q));
        assertTrue(resolved.contains(q));
        assertTrue(q.isResolved());
    }

    /**
     * Test Case 2: Open Resolved Question Thread.
     * Expected: The full Q&amp;A thread should be accessible.
     */
    @Test
    public void testOpenResolvedThread() {
        Question q = new Question("Explain Javadoc.", "student2");
        q.addAnswer("Javadoc generates HTML docs from comments.", "instructor1");
        q.setResolved(true);
        resolved.add(q);

        String thread = q.displayThread();
        assertTrue(thread.contains("Explain Javadoc."));
        assertTrue(thread.contains("Javadoc generates HTML"));
    }

    /**
     * Test Case 3: Avoid Duplicate Resolved Entries.
     * Expected: A question should appear only once in the resolved list.
     */
    @Test
    public void testAvoidDuplicateResolvedEntries() {
        Question q = new Question("What is polymorphism?", "student3");
        q.setResolved(true);

        if (!resolved.contains(q)) {
            resolved.add(q);
        }
        if (!resolved.contains(q)) {
            resolved.add(q);
        }

        long count = resolved.stream().filter(x -> x.equals(q)).count();
        assertEquals(1, count);
    }

    /**
     * Test Case 4: Question Has Student’s Username in DB.
     * Expected: Database stores correct student username.
     */
    @Test
    public void testStudentUsernameStoredInDB() {
        Question q = new Question("What is inheritance?", "student4");
        db.storeQuestion(q);

        Question retrieved = db.getQuestionByContent("What is inheritance?");
        assertNotNull(retrieved);
        assertEquals("student4", retrieved.getAskedBy());
    }

    /**
     * Test Case 5: Answer Has Username Stored and Displayed.
     * Expected: Answer displays 'Answer: ... by student1' correctly.
     */
    @Test
    public void testAnswerShowsUsername() {
        Question q = new Question("Explain encapsulation.", "student5");
        q.addAnswer("Encapsulation hides internal state.", "student1");

        String thread = q.displayThread();
        assertTrue(thread.contains("Answer: Encapsulation hides internal state. by student1"));
    }

    // --------- Supporting mock classes ----------

    static class Question {
        private String content;
        private String askedBy;
        private boolean resolved = false;
        private List<Answer> answers = new ArrayList<>();

        public Question(String content, String askedBy) {
            this.content = content;
            this.askedBy = askedBy;
        }

        public void addAnswer(String text, String username) {
            answers.add(new Answer(text, username));
        }

        public void setResolved(boolean val) {
            resolved = val;
        }

        public boolean isResolved() {
            return resolved;
        }

        public String getAskedBy() {
            return askedBy;
        }

        public String getContent() {
            return content;
        }

        public String displayThread() {
            StringBuilder sb = new StringBuilder();
            sb.append(content).append("\n");
            for (Answer a : answers) {
                sb.append("Answer: ").append(a.text)
                  .append(" by ").append(a.username).append("\n");
            }
            return sb.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Question)) return false;
            Question q = (Question) o;
            return Objects.equals(content, q.content) && Objects.equals(askedBy, q.askedBy);
        }

        @Override
        public int hashCode() {
            return Objects.hash(content, askedBy);
        }
    }

    static class Answer {
        String text;
        String username;

        public Answer(String text, String username) {
            this.text = text;
            this.username = username;
        }
    }

    static class DatabaseHelperMock {
        private Map<String, Question> questionMap = new HashMap<>();

        public void storeQuestion(Question q) {
            questionMap.put(q.getContent(), q);
        }

        public Question getQuestionByContent(String content) {
            return questionMap.get(content);
        }
    }
}