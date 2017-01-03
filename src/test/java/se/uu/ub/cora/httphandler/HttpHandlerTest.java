/*
 * Copyright 2016 Uppsala University Library
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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class HttpHandlerTest {
	private URL url;

	@BeforeMethod
	public void setUp() throws MalformedURLException {
		url = new URL("http://google.se");
	}

	@Test
	public void testSetRequestMethod() {
		HttpURLConnectionSpy urlConnection = new HttpURLConnectionSpy(url);
		HttpHandler httpHandler = HttpHandlerImp.usingURLConnection(urlConnection);
		httpHandler.setRequestMethod("GET");
		assertEquals(urlConnection.requestMethod, "GET");
	}

	@Test(expectedExceptions = RuntimeException.class)
	public void testSetWrongRequestMethod() {
		HttpURLConnectionSpy urlConnection = new HttpURLConnectionErrorSpy(url);
		HttpHandler httpHandler = HttpHandlerImp.usingURLConnection(urlConnection);
		httpHandler.setRequestMethod("NOT_A_REQUEST_METHOD");
	}

	@Test
	public void testGetResponseCode() {
		HttpURLConnectionSpy urlConnection = new HttpURLConnectionSpy(url);
		HttpHandler httpHandler = HttpHandlerImp.usingURLConnection(urlConnection);

		urlConnection.setResponseCode(200);
		assertEquals(httpHandler.getResponseCode(), 200);
	}

	@Test
	public void testGetBrokenResponseCode() {
		HttpURLConnectionSpy urlConnection = new HttpURLConnectionErrorSpy(url);
		HttpHandler httpHandler = HttpHandlerImp.usingURLConnection(urlConnection);
		assertEquals(httpHandler.getResponseCode(), 500);
	}

	@Test
	public void testGetResponseText() {
		HttpURLConnectionSpy urlConnection = new HttpURLConnectionSpy(url);
		HttpHandler httpHandler = HttpHandlerImp.usingURLConnection(urlConnection);

		urlConnection.setResponseText("some text");
		assertEquals(httpHandler.getResponseText(), "some text");
	}

	@Test(expectedExceptions = RuntimeException.class)
	public void testGetBrokenResponseText() {
		HttpURLConnectionSpy urlConnection = new HttpURLConnectionErrorSpy(url);
		HttpHandler httpHandler = HttpHandlerImp.usingURLConnection(urlConnection);

		httpHandler.getResponseText();
	}

	@Test
	public void testGetErrorText() {
		HttpURLConnectionSpy urlConnection = new HttpURLConnectionSpy(url);
		HttpHandler httpHandler = HttpHandlerImp.usingURLConnection(urlConnection);

		urlConnection.setErrorText("some text");
		assertEquals(httpHandler.getErrorText(), "some text");
	}

	@Test(expectedExceptions = RuntimeException.class)
	public void testGetErrorTextBroken() {
		HttpURLConnectionSpy urlConnection = new HttpURLConnectionErrorSpy(url);
		HttpHandler httpHandler = HttpHandlerImp.usingURLConnection(urlConnection);

		assertEquals(httpHandler.getErrorText(), "some text");
	}

	@Test
	public void testSetOutput() {
		HttpURLConnectionSpy urlConnection = new HttpURLConnectionSpy(url);
		HttpHandler httpHandler = HttpHandlerImp.usingURLConnection(urlConnection);

		httpHandler.setOutput("some text");
		assertTrue(urlConnection.doOutput);
		assertEquals(urlConnection.byteArrayOutputStream.toString(), "some text");
	}

	@Test(expectedExceptions = RuntimeException.class)
	public void testSetOutputIOException() {
		HttpURLConnectionSpy urlConnection = new HttpURLConnectionErrorSpy(url);
		HttpHandler httpHandler = HttpHandlerImp.usingURLConnection(urlConnection);

		httpHandler.setOutput("some text");
	}

	@Test
	public void testSetRequestProperty() {
		HttpURLConnectionSpy urlConnection = new HttpURLConnectionSpy(url);
		HttpHandler httpHandler = HttpHandlerImp.usingURLConnection(urlConnection);

		httpHandler.setRequestProperty("someKey", "someValue");
		assertEquals(urlConnection.requestProperties.get("someKey"), "someValue");
	}

	@Test
	public void testSetStreamOutput() {
		HttpURLConnectionSpy urlConnection = new HttpURLConnectionSpy(url);
		HttpHandler httpHandler = HttpHandlerImp.usingURLConnection(urlConnection);
		InputStream stream = new ByteArrayInputStream("a string".getBytes(StandardCharsets.UTF_8));
		httpHandler.setStreamOutput(stream);
		assertTrue(urlConnection.doOutput);
		assertEquals(urlConnection.byteArrayOutputStream.toString(), "a string");
	}

	@Test(expectedExceptions = RuntimeException.class)
	public void testSetStreamOutputBroken() {
		HttpURLConnectionSpy urlConnection = new HttpURLConnectionErrorSpy(url);
		HttpHandler httpHandler = HttpHandlerImp.usingURLConnection(urlConnection);
		InputStream stream = new ByteArrayInputStream("a string".getBytes(StandardCharsets.UTF_8));
		httpHandler.setStreamOutput(stream);
	}

	@Test
	public void testGetHeaderField() {
		HttpURLConnectionSpy urlConnection = new HttpURLConnectionSpy(url);
		HttpHandler httpHandler = HttpHandlerImp.usingURLConnection(urlConnection);

		urlConnection.setHeaderField("someName", "someValue");
		assertEquals(httpHandler.getHeaderField("someName"), "someValue");
	}

	@Test
	public void testGetNotFoundHeaderField() {
		HttpURLConnectionSpy urlConnection = new HttpURLConnectionSpy(url);
		HttpHandler httpHandler = HttpHandlerImp.usingURLConnection(urlConnection);

		assertEquals(httpHandler.getHeaderField("someOtherName"), null);
	}
}
