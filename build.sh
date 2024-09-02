./gradlew build
echo "Copying jars to other projects"
cp build/libs/*.jar ../OrleansRealms/lib/
cp build/libs/*.jar ../OrleansShops/lib/
cp build/libs/*.jar ../OrleansInteractions/lib/
echo "Done"