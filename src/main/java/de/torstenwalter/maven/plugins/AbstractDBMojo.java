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

import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;

public abstract class AbstractDBMojo extends AbstractMojo {

	/**
	 * User name for your database.
	 * 
	 * @parameter expression="${oracledb.username}"
	 */
	protected String username;

	/**
	 * Password for your database.
	 * 
	 * @parameter expression="${oracledb.password}"
	 */
	protected String password;

	/**
	 * It is also possible to specify user name and password for the database in
	 * your settings.xml The <code>serverId</code> specified here is the
	 * reference to the server with the same <code>id</code> in your
	 * settings.xml
	 * 
	 * @parameter expression="${oracledb.serverId}"
	 */
	protected String serverId;

	/**
	 * The {@link Settings} object.
	 * 
	 * @parameter default-value="${settings}"
	 * @required
	 * @readonly
	 */
	private Settings settings;

	/**
	 * Host name of your database server.
	 * 
	 * @parameter expression="${oracledb.hostname}" default-value="localhost"
	 * @required
	 */
	private String hostname;

	/**
	 * Port for your database server.
	 * 
	 * @parameter expression="${oracledb.port}" default-value="1521"
	 * @required
	 */
	private int port;

	/**
	 * The serviceName of your oracle database instance.
	 * 
	 * @parameter expression="${oracledb.serviceName}"
	 * @required
	 */
	private String serviceName;

	/**
	 * The instanceName of your oracle database instance, commonly used in Oracle RAC databases with multiple instances.
	 * 
	 * @parameter expression="${oracledb.instanceName}"
	 */
	private String instanceName;

	/**
	 * Specify role which should be used in the "as" clause in the connection
	 * identifier. Possible values: SYSOPER and SYSDBA. Other values are
	 * ignored.
	 * 
	 * @parameter default-value=""
	 */
	private String asClause;

    /**
     * Specify connection string style which should be used (<connect_identifier> can be in the form of Net Service Name or Easy Connect)
     *
     * @parameter expression="${oracledb.useEasyConnect}" default-value="false"
     */
    protected boolean useEasyConnect;
	
	
	public AbstractDBMojo() {
		super();
	}

	protected Credentials getCredentials() throws MojoFailureException {
		if (!StringUtils.isEmpty(serverId)) {
			getLog().info("using credentials from serverId '" + serverId + "'");
			Server server = settings.getServer(serverId);
			if (server == null) {
				throw new MojoFailureException("serverId '" + serverId
						+ "' not found!");
			}
			return new Credentials(server.getUsername(), server.getPassword());
		} else if (!StringUtils.isEmpty(username)) {
			return new Credentials(username, password);
		} else {
			throw new MojoFailureException(
					"Credentials needed. Specify either username and password or serverId");
		}
	}

	/**
	 * @see http://docs.oracle.com/cd/B19306_01/server.102/b14357/toc.htm
	 *      (SQL*Plus User's Guide and Reference)
	 * 
	 * @param credentials
	 * @return
	 */
	protected String getConnectionIdentifier(Credentials credentials) {
		StringBuilder connectionIdentifier = new StringBuilder();
		// fist add the username
		connectionIdentifier.append(credentials.getUsername());
		// then add the password if given
		if (!StringUtils.isEmpty(credentials.getPassword())) {
			connectionIdentifier.append("/").append(credentials.getPassword());
		}

		// now add the connect_identifier:
		if (!useEasyConnect) {
    		// To make it more robust and to not to rely on TNSNAMES we specify the
    		// full connect identifier like:
    		// (DESCRIPTION=
    		// (ADDRESS=(PROTOCOL=tcp)(HOST=host)(PORT=port) )
    		// (CONNECT_DATA=
    		// (SERVICE_NAME=service_name) ) )
    		connectionIdentifier
    				.append("@(DESCRIPTION=(ADDRESS_LIST=(ADDRESS=(PROTOCOL=tcp)(HOST=")
    				.append(hostname).append(")(PORT=").append(port)
    				.append(")))(CONNECT_DATA=(SERVICE_NAME=").append(serviceName).append(")");
    				if (!StringUtils.isEmpty(instanceName)){
    					connectionIdentifier.append("(INSTANCE_NAME=").append(instanceName).append(")");
    				}
    					connectionIdentifier.append("))");
		}
		else {
		    // "[//]Host[:Port]/<service_name>"
            connectionIdentifier.append("@//").append(hostname).append(":").append(port).append("/").append(serviceName);
		}
		// add as clause if necessary
		if (StringUtils.equalsIgnoreCase(asClause, "SYSDBA")
				|| StringUtils.equalsIgnoreCase(asClause, "SYSOPER")) {
			connectionIdentifier.append(" AS ").append(
					StringUtils.upperCase(asClause));
		}

		return connectionIdentifier.toString();
	}

	protected String getConnectionIdentifier() throws MojoFailureException {
		return getConnectionIdentifier(getCredentials());
	}

	protected String obfuscateCredentials(String string, Credentials credentials) {
		return StringUtils.replaceOnce(StringUtils.replaceOnce(string,
				credentials.getUsername(), "<username>"), credentials
				.getPassword(), "<password>");
	}

}