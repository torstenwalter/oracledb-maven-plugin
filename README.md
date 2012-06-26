oracledb-maven-plugin
=====================

The plugin provides you some goals to help execute oracle database tools like sqlplus, impdp and expdp.

Installation
------------
1. Download jar file and pom for the latest version.
2. Install plugin in your local maven repository
   `mvn install:install-file -Dfile=oracledb-maven-plugin-1.0.jar -DpomFile=oracledb-maven-plugin-1.0.pom`


Usage
-----

    <project>
      ...
      <build>
         <plugins>
            <plugin>
               <groupId>de.torstenwalter.maven.plugins</groupId>
               <artifactId>oracledb-maven-plugin</artifactId>
               <version>1.0</version>
               <executions>
                  <execution>
                     ...
                     <goals>
                        <goal>sqlplus</goal>
                     </goals>
                     <configuration>
                        <sqlFile>run-update.sql</sqlFile>
                     </configuration>
                  </execution>
                  <execution>
                     <id>import-db</id>
                     <phase>process-sources</phase>
                     <goals>
                        <goal>impdp</goal>
                     </goals>
                     <configuration>
                        <schemas>MYSCHEMA</schemas>
                        <directory>DUMPDIR</directory>
                        <dumpfile>database.dmp</dumpfile>
                        <logfile>import.log</logfile>
                     </configuration>
                  </execution>
               </executions>
               <configuration>
                  <serviceName>DEVELOPMENT</serviceName>
                  <username>user</username>
                  <password>secret</password>
                  <hostname>localhost</hostname>
                  <port>1521</port>
            </plugin>
         </plugins>
      </build>
      ...
    </project>


Help
----

You can use the maven-help-plugin to get more information about all possible parameters like this:
mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:describe -Dplugin=de.torstenwalter.maven.plugins:oracl
edb-maven-plugin:1.0 -Ddetail