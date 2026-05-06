# Puncuation-Check
An in-development Java program using Stanford CoreNLP and parallelism to efficiently detect puncuation errors in text

This program is still in-development. It uses Java 25 features such as IO.println() rather than System.out. As well, it uses the Stanford CoreNLP Simple API.

Setup:

Be sure to have at least JDK 25 installed



Build & Run:

Run: javac -cp "lib/*" GrammarCheckDriver.java Rules_Map.java
Run: java -cp ".:lib/*" GrammarCheckDriver



JMC Profiling:

Download JMC here: https://www.oracle.com/java/technologies/javase/products-jmc9-downloads.html
Drag recording.jfr into JMC to view the profile metrics
