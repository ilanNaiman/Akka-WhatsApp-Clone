Tomer Ittah
Ilan Naiman

Important notes:
	1- Keep in mind that the project is split between "whatsappaclient" - The client package and "akka-whatsApp-java" - the manager (server) package.
	2- You can run the projects in multiple ways, one of them is by packaging the projects into jar file by running - "mvn package -DskipTests=True",
	     bare in mind that -DskipTests=True is neccassary as our unittests requires special handling.
	     After creating the jar file execution is availiable with "java -jar ./target/<JAR_NAME>-allinone.jar".
