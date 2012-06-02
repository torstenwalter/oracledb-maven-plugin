oracledb-maven-plugin
=====================

The plugin provides you some goals to help execute oracle database tools like sqlplus, impdp and expdp.

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

