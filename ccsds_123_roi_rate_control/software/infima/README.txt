1. HOW TO RUN THIS PROGRAM
	You need Java 1.7, at least. You need the JDK 1.7 if you want to modify the sources and run it, or the JRE 1.7 if you only want to run it.

	Open a terminal and execute the following command to run INFIMA
	#java -Xmx128m -classpath dist/infima.jar infima.Infima

	and you will see the list of parameters that the program can receive. You can add any of these parameters at the end of the command. For example, to compare tow images:
	#java -Xmx128m -classpath dist/infima.jar infima.Infima -i image1.pgm image2.pgm

	To avoid typing the above command(s), GNU/Linux users may create a shell script including:
	#!/bin/bash
	java -Xmx128m -classpath dist/infima.jar infima.Infima $@

	and Windows users may create a .bat file including:
	java -Xmx128m -classpath dist/infima.jar infima.Infima %*

2. HOW TO COMPILE THIS PROGRAM
	You need Apache ant to compile this program.

	To compile the program, execute the following command
	#ant compile

	To generate the documentation, execute
	#ant doc

	To clean the object files and the documentation, execute
	#ant clean