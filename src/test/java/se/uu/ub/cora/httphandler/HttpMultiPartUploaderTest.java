/*
 * Copyright 2016, 2018 Uppsala University Library
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
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.httphandler.internal.HttpMultiPartUploaderImp;
import se.uu.ub.cora.httphandler.urlconnection.HttpURLConnectionErrorSpy;
import se.uu.ub.cora.httphandler.urlconnection.HttpURLConnectionPartlyErrorSpy;
import se.uu.ub.cora.httphandler.urlconnection.HttpURLConnectionSpy;

public class HttpMultiPartUploaderTest {
	private URL url;

	@BeforeMethod
	public void setUp() throws MalformedURLException {
		url = new URL("http://google.se");
	}

	@Test
	public void testSetStreamOutput() throws IOException {
		HttpURLConnectionSpy urlConnection = new HttpURLConnectionSpy(url);
		HttpMultiPartUploader httpHandler = HttpMultiPartUploaderImp
				.usingURLConnection(urlConnection);
		httpHandler.addHeaderField("Accept", "application/vnd.uub.record+json");
		httpHandler.addFormField("some", "value");
		InputStream fakeStream = new ByteArrayInputStream(
				"a string".getBytes(StandardCharsets.UTF_8));
		httpHandler.addFilePart("file", "adele.png", fakeStream);
		httpHandler.done();
		// String expected = "Accept: application/vnd.uub.record+json2\n";
		String expected = "";

		expected += "--xxxYYYxxx\n";
		expected += "Content-Disposition: form-data; name=\"some\"\n";
		expected += "Content-Type: text/plain; charset=UTF-8\n";
		expected += "\n";
		expected += "value\n";
		expected += "--xxxYYYxxx\n";
		expected += "Content-Disposition: form-data; name=\"file\"; filename=\"adele.png\"\n";
		expected += "Content-Type: image/png\n";
		expected += "Content-Transfer-Encoding: binary\n";
		expected += "\n";
		expected += "a string\n";
		expected += "--xxxYYYxxx--\n";

		assertEquals(urlConnection.getOutputStreamAsString(), expected);

		assertEquals(urlConnection.requestProperties.get("Accept"),
				"application/vnd.uub.record+json");
	}

	@Test
	public void testUrlConnectionSetUpCorrectly() throws IOException {
		HttpURLConnectionSpy urlConnection = new HttpURLConnectionSpy(url);
		HttpMultiPartUploader httpHandler = HttpMultiPartUploaderImp
				.usingURLConnection(urlConnection);
		httpHandler.addHeaderField("Accept", "application/vnd.uub.record+json");
		httpHandler.addFormField("some", "value");
		InputStream fakeStream = new ByteArrayInputStream(
				"a string".getBytes(StandardCharsets.UTF_8));
		httpHandler.addFilePart("file", "adele.png", fakeStream);
		httpHandler.done();

		assertFalse(urlConnection.usecaches.get(0));
		urlConnection.MCR.assertParameters("setDoOutput", 0, true);
		assertTrue(urlConnection.doinput.get(0));
		assertEquals(urlConnection.requestProperties.get("Content-Type"),
				"multipart/form-data; boundary=xxxYYYxxx");
		assertEquals(urlConnection.requestProperties.get("User-Agent"),
				"HttpMultipartUploader Agent");
	}

	@Test
	public void testUrlConnectionOnlySetUpOnce() throws IOException {
		HttpURLConnectionSpy urlConnection = new HttpURLConnectionSpy(url);
		HttpMultiPartUploader httpHandler = HttpMultiPartUploaderImp
				.usingURLConnection(urlConnection);
		httpHandler.addFormField("some", "value");
		InputStream fakeStream = new ByteArrayInputStream(
				"a string".getBytes(StandardCharsets.UTF_8));
		httpHandler.addFilePart("file", "adele.png", fakeStream);
		httpHandler.done();

		assertEquals(urlConnection.usecaches.size(), 1);
		urlConnection.MCR.assertParameters("setDoOutput", 0, true);
		assertEquals(urlConnection.doinput.size(), 1);
		assertEquals(urlConnection.setRequestPropertiesCalledNoTimes, 2);
		urlConnection.MCR.assertNumberOfCallsToMethod("getOutputStream", 1);
	}

	@Test
	public void testSetStreamOutputError() throws IOException {
		try {
			HttpURLConnectionSpy urlConnection = new HttpURLConnectionErrorSpy(url);
			HttpMultiPartUploader httpHandler = HttpMultiPartUploaderImp
					.usingURLConnection(urlConnection);
			httpHandler.addFormField("some", "value");

		} catch (Exception e) {
			assertTrue(e instanceof RuntimeException);
			assertEquals(e.getMessage(), "Failed to upload multipart");
			assertEquals(e.getCause().getMessage(),
					"this is an ioException getting outputStream from HttpURLConnectionErrorSpy");
		}
	}

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ""
			+ "Headers can not be set after connection is setup on multipart requests, "
			+ "headerName: SomeHeader headerValue: someHeaderValue")
	public void testSetHeaderAfterSendingDataHasStarted() throws IOException {
		HttpURLConnectionSpy urlConnection = new HttpURLConnectionSpy(url);
		HttpMultiPartUploader httpHandler = HttpMultiPartUploaderImp
				.usingURLConnection(urlConnection);
		httpHandler.addHeaderField("Accept", "application/vnd.uub.record+json");
		httpHandler.addFormField("some", "value");
		InputStream fakeStream = new ByteArrayInputStream(
				"a string".getBytes(StandardCharsets.UTF_8));
		httpHandler.addFilePart("file", "adele.png", fakeStream);

		httpHandler.addHeaderField("SomeHeader", "someHeaderValue");

	}

	@Test
	public void testGetResponseCode() {
		HttpURLConnectionSpy urlConnection = new HttpURLConnectionSpy(url);
		HttpMultiPartUploader httpHandler = HttpMultiPartUploaderImp
				.usingURLConnection(urlConnection);

		urlConnection.setResponseCode(200);
		assertEquals(httpHandler.getResponseCode(), 200);
	}

	@Test
	public void testGetBrokenResponseCode() {
		HttpURLConnectionSpy urlConnection = new HttpURLConnectionPartlyErrorSpy(url);
		HttpMultiPartUploader httpHandler = HttpMultiPartUploaderImp
				.usingURLConnection(urlConnection);
		assertEquals(httpHandler.getResponseCode(), 500);
	}

	@Test
	public void testGetResponseText() {
		HttpURLConnectionSpy urlConnection = new HttpURLConnectionSpy(url);
		HttpMultiPartUploader httpHandler = HttpMultiPartUploaderImp
				.usingURLConnection(urlConnection);

		urlConnection.setResponseText("some text");
		assertEquals(httpHandler.getResponseText(), "some text");
	}

	@Test()
	public void testGetBrokenResponseText() {
		try {

			HttpURLConnectionSpy urlConnection = new HttpURLConnectionPartlyErrorSpy(url);
			HttpMultiPartUploader httpHandler = HttpMultiPartUploaderImp
					.usingURLConnection(urlConnection);

			httpHandler.getResponseText();
		} catch (Exception e) {
			assertTrue(e instanceof RuntimeException);
			assertEquals(e.getMessage(), "Error getting response text: ");
			assertEquals(e.getCause().getMessage(),
					"this is an ioException getting inputStream from HttpURLConnectionPartlyErrorSpy");
		}
	}

	@Test
	public void testGetErrorText() {
		HttpURLConnectionSpy urlConnection = new HttpURLConnectionSpy(url);
		HttpMultiPartUploader httpHandler = HttpMultiPartUploaderImp
				.usingURLConnection(urlConnection);

		urlConnection.setErrorText("some text");
		assertEquals(httpHandler.getErrorText(), "some text");
	}

	@Test()
	public void testGetErrorTextBroken() {
		try {
			HttpURLConnectionSpy urlConnection = new HttpURLConnectionPartlyErrorSpy(url);
			HttpMultiPartUploader httpHandler = HttpMultiPartUploaderImp
					.usingURLConnection(urlConnection);

			assertEquals(httpHandler.getErrorText(), "some text");

		} catch (Exception e) {
			assertTrue(e instanceof RuntimeException);
			assertEquals(e.getMessage(), "Error getting response text: ");
		}
	}

}
