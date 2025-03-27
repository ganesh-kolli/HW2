# HW3

CSE 360

Individual Homework 3

Ganesh Kolli




A Listing of the Five Automated Tests

Mark Question as Resolved — Verifies that a question moves from the unresolved list to the resolved list.

Open Resolved Question Thread — Validates that a resolved question’s entire Q&A thread can be viewed.

Avoid Duplicate Resolved Entries — Ensures the resolved list only contains one instance of a resolved question.

Question Has Student’s Username in DB — Confirms that the student’s name is correctly stored in the database.

Answer Has Username Stored and Displayed — Validates that each answer shows the username of the respondent.



A Description of the Five Automated Tests

Mark Question as Resolved
This test simulates marking a question as resolved. It removes the question from the unresolved list, marks it as resolved, and adds it to the resolved list. The test asserts that the unresolved list no longer contains the question and that it appears in the resolved list, ensuring correctness of transition and state change.

Open Resolved Question Thread
This test checks whether a fully resolved question thread displays both the question and its answers. It creates a resolved question with an answer and verifies that the display output contains both the question content and answer content, proving the integrity of Q&A retrieval.

Avoid Duplicate Resolved Entries
This test ensures that a resolved question is not added to the resolved list more than once. It adds the same question twice but manually guards against duplication. It then counts occurrences of the question and asserts that it appears exactly once, ensuring list integrity.

Question Has Student’s Username in DB
This test confirms that when a question is stored in the database, it correctly records which student asked it. It stores a question using a mock database helper and retrieves it to assert that the stored username matches the expected one.

Answer Has Username Stored and Displayed
This test ensures that answers retain and show the respondent’s username. It adds an answer to a question and checks that the rendered thread includes both the answer content and the student’s name, validating user visibility in responses.

The Link to the Source for my Javadoc Inspiration

Oracle Java Collections Framework Documentationhttps://docs.oracle.com/javase/8/docs/technotes/guides/collections/overview.html



The Link to my Screencast
https://asu.zoom.us/rec/share/MoyyjP24jlyn11PZ8Q_QqhTXhXZAbge1ZCWut0tRN0XTzfzkuluVz1PSxj1qSIKu.1NR7TenudRV5zyM0 
Passcode: 8jZjbEy^
