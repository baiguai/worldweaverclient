javac -cp .:release/lib/* -d ./compiled ./code/*.java
cd compiled
jar -cfm ../release/WorldWeaver.jar manifest.txt *.class
cd ../
chmod +x release/WorldWeaver.jar
