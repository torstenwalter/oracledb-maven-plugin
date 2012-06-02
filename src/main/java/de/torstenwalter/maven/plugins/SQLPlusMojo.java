/**
 * Copyright 2012 Torsten Walter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.torstenwalter.maven.plugins;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.cli.CommandLineUtils;

/**
 * Executes scripts or code snippet using SQL*Plus command line utility
 * 
 * @goal sqlplus
 */
public class SQLPlusMojo extends AbstractDBMojo {
	/**
	 * The sqlplus command to execute. If sqlplus is not in you PATH you can
	 * simply specify the full path to the sqlplus command here
	 * 
	 * @parameter expression="${oracledb.sqlplus}" default-value="sqlplus"
	 */
	private String sqlplus;

	/**
	 * These statements are executed before the statements in sqlCommand or
	 * sqlFile. They should make sure the build fails if an error occurs.
	 * 
	 * Technically speaking these statements are written to a login.sql file and
	 * executed directly after sqlplus starts.
	 * 
	 * @parameter expression="${oracledb.beforeSql}" default-value=
	 *            "WHENEVER SQLERROR EXIT FAILURE ROLLBACK;\nWHENEVER OSERROR EXIT FAILURE ROLLBACK;"
	 * @required
	 */
	private String beforeSql;

	/**
	 * Specify commands which SQL*Plus should executed.
	 * 
	 * @parameter expression="${oracledb.sqlCommand}" defaultValue=""
	 */
	private String sqlCommand;

	/**
	 * File containing commands which SQL*Plus should execute.
	 * 
	 * @parameter
	 */
	private File sqlFile;

	/**
	 * @parameter default-value="${project}"
	 * @readonly
	 */
	private MavenProject project;

	public void execute() throws MojoExecutionException, MojoFailureException {
		if (!StringUtils.isEmpty(sqlCommand)) {
			// write statements to temporary file which can be passed to
			// sqlplus
			File tmpSqlFile;
			try {
				tmpSqlFile = File.createTempFile("statements-", ".sql");
			} catch (IOException e) {
				throw new MojoExecutionException(
						"Could not create file for sql statements", e);
			}
			tmpSqlFile.deleteOnExit();

			try {
				FileOutputStream fos = new FileOutputStream(tmpSqlFile);
				fos.write(sqlCommand.getBytes());
				fos.flush();
				fos.close();
			} catch (FileNotFoundException e) {
				throw new MojoExecutionException(
						"Could not write sql statements to file", e);
			} catch (IOException e) {
				throw new MojoExecutionException(
						"Could not write sql statements to file", e);
			}

			runScriptWithSqlPlus(tmpSqlFile, getEnvVars());
		} else if (sqlFile != null) {
			runScriptWithSqlPlus(sqlFile, getEnvVars());
		}
	}

	private File getPluginTempDirectory() {
		File dir = new File(project.getBuild().getDirectory(),
				"oracledb-maven-plugin");
		dir.mkdirs();
		return dir;
	}

	private Map getEnvVars() throws MojoExecutionException {
		if (beforeSql != null) {
			Map envVars = new HashMap();
			try {
				envVars.putAll(CommandLineUtils.getSystemEnvVars());
			} catch (IOException e) {
				throw new MojoExecutionException(
						"Could not copy system environment variables.", e);
			}
			envVars.put("SQLPATH", getPluginTempDirectory().getAbsolutePath());
			File login = new File(getPluginTempDirectory(), "login.sql");
			// login.deleteOnExit();
			try {
				login.createNewFile();
				FileOutputStream loginFos;
				loginFos = new FileOutputStream(login);
				loginFos.write(beforeSql.getBytes());

				loginFos.flush();
				loginFos.close();
			} catch (FileNotFoundException e) {
				throw new MojoExecutionException("Could not write "
						+ login.getPath(), e);
			} catch (IOException e) {
				throw new MojoExecutionException("Could not write "
						+ login.getPath(), e);
			}
			return envVars;
		} else {
			return null;
		}

	}

	private void runScriptWithSqlPlus(File file, Map environment)
			throws MojoExecutionException, MojoFailureException {
		checkFileIsReadable(file);

		CommandLine commandLine = new CommandLine(sqlplus);
		// logon only once, without this sql*plus would prompt for
		// credentials if given ones are not correct
		commandLine.addArgument("-L");
		commandLine.addArgument(getConnectionIdentifier());
		commandLine.addArgument("@" + file.getName());

		getLog().info(
				"Executing command line: "
						+ obfuscateCredentials(commandLine.toString(),
								getCredentials()));

		Executor exec = new DefaultExecutor();
		exec.setWorkingDirectory(file.getParentFile());
		exec.setStreamHandler(new PumpStreamHandler(System.out, System.err));
		try {
			exec.execute(commandLine, environment);
		} catch (ExecuteException e) {
			throw new MojoExecutionException("program exited with exitCode: "
					+ e.getExitValue());
		} catch (IOException e) {
			throw new MojoExecutionException("Command execution failed.", e);
		}
	}

	private void checkFileIsReadable(File file) throws MojoFailureException {
		if (!file.exists() || !file.canRead() || !file.isFile()) {
			throw new MojoFailureException(file.getName()
					+ "problem rading file '" + file.getAbsolutePath() + "'");
		}
	}
}
