/*
 * Copyright 2022 Uppsala University Library
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
package se.uu.ub.cora.httphandler.urlconnection;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class HttpURLConnectionHandlerTest {

	private HttpURLConnectionSpy urlConnection;
	private HttpURLConnectionHandler connectionHandler;

	@BeforeMethod
	public void setUp() throws MalformedURLException {
		urlConnection = new HttpURLConnectionSpy(new URL("http://google.se"));
		urlConnection.setResponseText("some text åäö");
		connectionHandler = new HttpURLConnectionHandler(urlConnection);

	}

	@Test
	public void testGetResponseText() {
		String textToReturnAsResponse = "a text to be returned from spy";
		urlConnection.setResponseText(textToReturnAsResponse);

		String responseText = connectionHandler.getResponseText();
		assertEquals(urlConnection.methodCalled, "getInputStream");
		assertEquals(responseText, textToReturnAsResponse);
	}

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ""
			+ "Error getting response text: ")
	public void testErrorGetResponseText() throws MalformedURLException {
		useErrorConnectionSpy();
		connectionHandler.getResponseText();
	}

	@Test
	public void testGetResponseBinary() {
		InputStream inputStream = connectionHandler.getResponseBinary();
		assertNotNull(inputStream);
		assertSame(inputStream, urlConnection.returnedInputStream);
	}

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ""
			+ "Error reading binary from response.")
	public void testErrorGetResponseBinary() throws MalformedURLException {
		useErrorConnectionSpy();
		connectionHandler.getResponseBinary();

	}

	@Test
	public void testGetResponseCode() throws IOException {
		urlConnection.setResponseCode(418);

		int responseCode = connectionHandler.getResponseCode();
		assertEquals(responseCode, urlConnection.getResponseCode());
		assertEquals(urlConnection.methodCalled, "getResponseCode");
	}

	@Test
	public void testGetResponseCodeError() throws MalformedURLException {
		useErrorConnectionSpy();
		int responseCode = connectionHandler.getResponseCode();
		assertEquals(responseCode, 500);
	}

	private void useErrorConnectionSpy() throws MalformedURLException {
		urlConnection = new HttpURLConnectionErrorSpy(new URL("http://google.se"));
		connectionHandler = new HttpURLConnectionHandler(urlConnection);
	}

	@Test
	public void testGetErrorText() {
		String errorTextToReturnAsResponse = "some error text to return from spy";
		urlConnection.setErrorText(errorTextToReturnAsResponse);

		String errorText = connectionHandler.getErrorText();

		assertEquals(urlConnection.methodCalled, "getErrorStream");
		assertEquals(errorText, errorTextToReturnAsResponse);
	}

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ""
			+ "Error getting response text: ")
	public void testErrorGetErrorText() throws MalformedURLException {
		useErrorConnectionSpy();
		connectionHandler.getErrorText();

	}

}
