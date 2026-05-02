# Puncuation-Check
A Java program using Stanford CoreNLP and parallelism to efficiently detect puncuation errors in text

This program is still in-development. It uses Java 25 features such as IO.println() rather than System.out. As well, it uses the Stanford CoreNLP Simple API.

Build & Run Instructions for the terminal:

1) Be sure to have at least JDK 25 installed
2) Download all files
4) Run: cd Grammar
5) Run: javac -cp "lib/*" GrammarCheckDriver.java Rules_Map.java
6) Run: java -cp ".:lib/*" GrammarCheckDriver

Build & Run Instructions for Eclipse IDE:

1) Be sure to have at least JDK 25 installed & chosen as your JRE
2) Download Eman_Qundes_Parallel_FinalProj_Spring26.zip
3) Open Eclipse and go to File, Import, General, and then Existing Projects into Workspace
4) Choose Select archive file, Browse, and then find the .zip file
5) Click Open and then Finish
6) Be sure you have and run GrammarCheckDriver.java as your main class
7) Go to Run Configurations and input
	-XX:StartFlightRecording:filename=recording.jfr,dumponexit=true
if you want a .jfr file to profile the program in JMC
8) You can download JMC here: https://www.oracle.com/java/technologies/javase/products-jmc9-downloads.html
