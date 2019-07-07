# dahua-sdk
Used SDK from Official web site support -> Download Center

```
sudo apt install libcanberra-gtk-module
sudo cp -r libs/lin64 /usr/lib
mvn install:install-file -Dfile=./libs/jna.jar -DgroupId=com.dahua -DartifactId=jna -Dversion=1.0 -Dpackaging=jar -DgeneratePom=true
mvn clean package
java -jar -Xms256m -Xmx512m ./target/netsdk-1.0-demo.jar
```