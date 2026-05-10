# Puncuation-Check
An in-development Java program that checks and rectifies punctuation errors. The program uses the Stanford CoreNLP Simple API, a Natural Language Processing library for the Java platform, to analyze the grammatical structure of each sentence and determine if there exists a pattern which would entail that a certain punctuation mark is missing. As well, the program uses some of Java's modern support for concurrent and parallel programming, like Virtual Threads, and exisiting structures, like the Concurrent Skip List Map, to make parsing, sorting, and editing operations more efficient and to increase throughput. The program also uses Java 25 features such as IO.println() rather than System.out.println (). 

Setup:

Be sure to have at least JDK 25 installed



Build & Run:

Run: javac -cp "lib/*" GrammarCheckDriver.java Rules_Map.java
Run: java -cp ".:lib/*" GrammarCheckDriver



JMC Profiling:

Download JMC here: https://www.oracle.com/java/technologies/javase/products-jmc9-downloads.html
Drag recording.jfr into JMC to view the profile metrics
