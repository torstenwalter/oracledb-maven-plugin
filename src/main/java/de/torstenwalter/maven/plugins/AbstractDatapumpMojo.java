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

import org.apache.commons.exec.CommandLine;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.MojoFailureException;

public abstract class AbstractDatapumpMojo extends AbstractDBMojo {

	/**
	 * @parameter
	 */
	protected String content;

	/**
	 * @parameter
	 * @required
	 */
	protected String directory;

	/**
	 * @parameter
	 */
	protected String dumpfile;

	/**
	 * @parameter
	 */
	protected String exclude;

	/**
	 * @parameter
	 */
	protected String include;

	/**
	 * @parameter
	 */
	private String logfile;

	/**
	 * @parameter
	 */
	private String network_link;

	/**
	 * @parameter
	 */
	protected String schemas;

	/**
	 * @parameter
	 */
	protected String tables;

	public AbstractDatapumpMojo() {
		super();
	}

	protected void addCommonArguments(CommandLine commandLine)
			throws MojoFailureException {
		commandLine.addArgument("'" + getConnectionIdentifier() + "'", false);

		if (StringUtils.isNotEmpty(content)) {
			commandLine.addArgument("CONTENT=" + content);
		}

		commandLine.addArgument("DIRECTORY=" + directory);

		if (StringUtils.isNotEmpty(dumpfile)) {
			commandLine.addArgument("DUMPFILE=" + dumpfile);
		}

		if (StringUtils.isNotEmpty(exclude)) {
			commandLine.addArgument("EXCLUDE=" + exclude);
		}

		if (StringUtils.isNotEmpty(include)) {
			commandLine.addArgument("INCLUDE=" + include);
		}

		if (StringUtils.isNotEmpty(logfile)) {
			commandLine.addArgument("LOGFILE=" + logfile);
		}

		if (StringUtils.isNotEmpty(network_link)) {
			commandLine.addArgument("NETWORK_LINK=" + network_link);
		}

		if (StringUtils.isNotEmpty(schemas)) {
			commandLine.addArgument("SCHEMAS=" + schemas);
		}

		if (StringUtils.isNotEmpty(tables)) {
			commandLine.addArgument("TABLES=" + tables);
		}
	}

}