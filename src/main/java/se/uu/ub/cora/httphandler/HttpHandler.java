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
 * be factored by using a {@link HttpHandlerFactory} usually {@link HttpHandlerFactoryImp}.
 * <p>
 * There is an order that is needed to use HttpHandler, correctly.
 * <ol>
 * <li>Factor a HttpHandler using the HttpHandlerFactory</li>
 * <li>Set the requestMethod using the {@link #setRequestMethod(String)}</li>
 * <li>...</li>
 * </ol>
 */
public interface HttpHandler {
	/**
	 * setRequestMethod set the request method to use in the http request.
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
	 * Can throw a {@link RuntimeException} if any problem occurs while reading the binary
	 * 
	 * @return
	 */
	InputStream getResponseBinary();

	void setOutput(String outputString);

	void setRequestProperty(String key, String value);

	String getErrorText();

	void setStreamOutput(InputStream stream);

	String getHeaderField(String name);

	void setBasicAuthorization(String username, String password);

}
