/*
 * Copyright 2016, 2019, 2023 Uppsala University Library
 *
 * This file is part of Cora.
 *
 *     Cora is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Cora is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Cora.  If not, see <http://www.gnu.org/licenses/>.
 */

package se.uu.ub.cora.httphandler;

import java.io.InputStream;

/**
 * HttpHandler is an interface to do a single http request to a specified url. HttpHandlers should
 * be factored by using a {@link HttpHandlerFactory} usually {@link HttpHandlerFactoryImp}. A
 * HttpHandler cannot be re-used and needs to be factored for every call.
 * <p>
 * The request method SHOULD by default be set to GET by implementing classes, so that a call to the
 * {@link #setRequestMethod(String)} only is necessary if a diffrent request method is to be used.
 * 
 */
public interface HttpHandler {

	/**
	 * setRequestMethod set the request method to use in the http request.
	 * <p>
	 * Currently supported request methods are:
	 * <ul>
	 * <li>GET</li>
	 * <li>HEAD</li>
	 * <li>POST</li>
	 * <li>PUT</li>
	 * <li>DELETE</li>
	 * <li>PATCH</li>
	 * </ul>
	 * <p>
	 * By default should GET be set by implementing classes, so a call to this method is only
	 * necessary if a diffrent request method is to be used.
	 * 
	 * @param requestMethod
	 *            A String with the requestMethod to use.
	 */
	void setRequestMethod(String requestMethod);

	/**
	 * getResponseCode returns the response code for the request, or 500 if there is a problem with
	 * the request.
	 * <p>
	 * getResponseCode triggers the request to be sent, if it has not been sent since before.
	 * 
	 * @return An int with the response code from the request or 500 if something has gone wrong.
	 */
	int getResponseCode();

	/**
	 * Reads a String from the Response from a call to a Http service
	 * <p>
	 * getResponseText triggers the request to be sent, if it has not been sent since before.
	 * 
	 * Can throw a {@link RuntimeException} if any problem occurs while reading the String
	 * 
	 * @return A String with the response text from the request
	 */
	String getResponseText();

	/**
	 * Reads a binary from the Response from a call to a Http service
	 * 
	 * getResponseBinary triggers the request to be sent, if it has not been sent since before.
	 * 
	 * Can throw a {@link RuntimeException} if any problem occurs while reading the binary
	 * 
	 * @return The incoming inputStream from the
	 */
	InputStream getResponseBinary();

	/**
	 * Set a header for the request call.
	 * 
	 * @param key
	 *            The header key
	 * @param value
	 *            The header value
	 */
	void setRequestProperty(String key, String value);

	/**
	 * Get value of specific header field This does not trigger a request to be sent and must
	 * therefore be used after an actual request have been made using triggering methods.
	 * 
	 * @param name
	 *            The name of the header field
	 * @return The value of the header field
	 */
	String getHeaderField(String name);

	/**
	 * Read the body of the request
	 * 
	 * getErrorText triggers the request to be sent, if it has not been sent since before. Can throw
	 * a {@link RuntimeException} if any problem occurs while reading the String
	 * 
	 * @return A String with the reponse text from the request
	 */
	String getErrorText();

	/**
	 * Set outgoing body from a String.
	 * 
	 * setOutput triggers the request to be sent, if it has not been sent since before. Can throw a
	 * {@link RuntimeException} if any problem occurs while sending the body
	 * 
	 * @param outputString
	 *            The String to set as output
	 */
	void setOutput(String outputString);

	/**
	 * Set outgoing body as an InputStream.
	 * 
	 * setStreamOutput triggers the request to be sent, if it has not been sent since before. Can
	 * throw a {@link RuntimeException} if any problem occurs while sending the body
	 * 
	 * @param stream
	 *            The stream to use as output
	 */
	void setStreamOutput(InputStream stream);

	/**
	 * Set the basic authorization as a header for the request call
	 * 
	 * @param username
	 *            The username to use
	 * @param password
	 *            The password to use
	 */
	void setBasicAuthorization(String username, String password);

}
