package jnipap;

import java.net.URL;
import java.util.List;

import java.security.InvalidParameterException;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcHttpTransportException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import jnipap.JnipapException;
import jnipap.InputException;
import jnipap.MissingInputException;
import jnipap.NonExistentException;
import jnipap.DuplicateException;
import jnipap.ConnectionException;

/**
 * A singelton class containing a connection to the Jnipap XML-RPC server
 *
 * This class is used by all NIPAP mapped objects to obtain a connection to
 * the NIPAP XML-RPC server.
 *
 * @author Lukas Garberg <lukas@spritelink.net>
 */
public class Connection {

	public XmlRpcClient connection;
	private XmlRpcClientConfigImpl config;

	private static Connection _instance;


	/**
	 * Creates a JnipapConnection
	 *
	 * The constructor is made private as it is only called from the
	 * getInstance-method.
	 *
	 * @param srv_url URL to the NIPAP server
	 */
	private Connection(URL srv_url) {

		// Create configuration object
		this.config = new XmlRpcClientConfigImpl();
		this.config.setServerURL(srv_url);
		this.config.setEnabledForExtensions(true);

		// Create client object
		this.connection = new XmlRpcClient();
		this.connection.setConfig(this.config);
		this.connection.setTypeFactory(new NonExNullParser(this.connection));

	}

	public void setUsername(String username) {
		this.config.setBasicUserName(username);
	}

	public void setPassword(String password) {
		this.config.setBasicPassword(password);
	}

	public Object execute(String pMethodName, Object[] pParams) throws JnipapException {
		try {
			return this.connection.execute(pMethodName, pParams);
		} catch(XmlRpcException e) {
			throw (JnipapException)this.xmlRpcExceptionToJnipapException(e);
		}
	}

	public Object execute(String pMethodName, List pParams) throws JnipapException {
		try {
			return this.connection.execute(pMethodName, pParams);
		} catch(XmlRpcException e) {
			throw (JnipapException)this.xmlRpcExceptionToJnipapException(e);
		}
	}

	/**
	 * Converts an XmlRpcException to a JnipapException
	 *
	 * @param e XmlRpcException to convert
	 * @return A JnipapException or subclass of it describing the error
	 */
	private Exception xmlRpcExceptionToJnipapException(XmlRpcException e) {

		if (e instanceof XmlRpcHttpTransportException ) {

			XmlRpcHttpTransportException e2 = (XmlRpcHttpTransportException)e;

			// Failed authentications turn up here
			if (e2.getStatusCode() == 401) {
				return new AuthFailedException("Authentication failed.");
			} else {
				return new ConnectionException(e2);
			}

		} else {

			// one of our own NIPAP-errors?
			switch (e.code) {
				case 1000:
					return new JnipapException(e.getMessage());
				case 1100:
					return new InputException(e.getMessage());
				case 1110:
					return new MissingInputException(e.getMessage());
				case 1120:
					return new InvalidParameterException(e.getMessage());
				case 1130:
					return new IllegalArgumentException(e.getMessage());
				case 1200:
					return new IllegalArgumentException(e.getMessage());
				case 1300:
					return new NonExistentException(e.getMessage());
				case 1400:
					return new DuplicateException(e.getMessage());
				default:
					return new JnipapException(e);
			}

		}

	}

	/**
	 * Get an instance of the JnipapConnection
	 *
	 * If no instance previously has been created an error will be thrown.
	 *
	 * @return A reference to the JnipapConnection object
	 */
	public static Connection getInstance() {

		// Throw exception if no connection previously has been created
		if (_instance == null) {
			throw new IllegalStateException("JnipapConnection not configured. Specify URL first!");
		}

		return _instance;

	}

	/**
	 * Get an instance of the JnipapConnection
	 *
	 * If no instance of the JnipapConnection exists, a new one will be created
	 * to the specified URL. Otherwise, the old object is returned and the URL ignored.
	 *
	 * Should this behavior be changed to alter the URL?
	 *
	 * @param srv_url URL to the NIPAP server
	 * @return A reference to the JnipapConnection object
	 */
	public static Connection getInstance(URL srv_url) {

		// Create new instance if none exist.
		if (_instance == null) {
			_instance = new Connection(srv_url);
		}

		return _instance;

	}

        /**
         * Get an instance of the JnipapConnection
         *
         * Function is equal to getInstance(URL srv_url) with the addition that
         * the username & password which will be used to authenticate the
         * queries can be specified.
         *
         * @param srv_url URL to the NIPAP server
         * @param username Username to authenticate as
         * @param password Password to authenticate with
         * @return A reference to the JnipapConnection object
         */
        public static Connection getInstance(URL srv_url, String username,
            String password) {

            Connection conn = Connection.getInstance(srv_url);
            conn.setUsername(username);
            conn.setPassword(password);

            return conn;

        }
}