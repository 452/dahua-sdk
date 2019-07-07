echo "maven install jna.jar"
call mvn install:install-file -Dfile=./libs/jna.jar -DgroupId=com.dahua -DartifactId=jna -Dversion=1.0 -Dpackaging=jar -DgeneratePom=true

echo "maven install examples.jar"
call mvn install:install-file -Dfile=./libs/examples.jar -DgroupId=com.dahua -DartifactId=examples -Dversion=1.0 -Dpackaging=jar -DgeneratePom=true

echo "maven clean package"
call mvn clean package

echo "ending."








