echo "Cleaning target folder"
mvn clean
echo "mvn clean executed successfully"

echo "Compiling project"
mvn compile
echo "Project has been compiled successfully"

# echo "Running project"
# mvn exec:java -Dexec.mainClass="com.github.bernardodemarco.textretrieval.worker.Worker" -Dexec.args="8001"
