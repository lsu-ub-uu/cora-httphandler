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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

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
		httpHandler.addHeaderField("Accept", "application/uub+record+json2");
		httpHandler.addFormField("some", "value");
		InputStream fakeStream = new ByteArrayInputStream(
				"a string".getBytes(StandardCharsets.UTF_8));
		httpHandler.addFilePart("file", "adele.png", fakeStream);
		httpHandler.done();
		String expected = "Accept: application/uub+record+json2\n";

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

		assertEquals(urlConnection.byteArrayOutputStream.toString(), expected);
	}

	@Test(expectedExceptions = RuntimeException.class)
	public void testSetStreamOutputError() throws IOException {
		HttpURLConnectionSpy urlConnection = new HttpURLConnectionErrorSpy(url);
		HttpMultiPartUploaderImp.usingURLConnection(urlConnection);
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

	@Test(expectedExceptions = RuntimeException.class)
	public void testGetBrokenResponseText() {
		HttpURLConnectionSpy urlConnection = new HttpURLConnectionPartlyErrorSpy(url);
		HttpMultiPartUploader httpHandler = HttpMultiPartUploaderImp
				.usingURLConnection(urlConnection);

		httpHandler.getResponseText();
	}

	@Test
	public void testGetErrorText() {
		HttpURLConnectionSpy urlConnection = new HttpURLConnectionSpy(url);
		HttpMultiPartUploader httpHandler = HttpMultiPartUploaderImp
				.usingURLConnection(urlConnection);

		urlConnection.setErrorText("some text");
		assertEquals(httpHandler.getErrorText(), "some text");
	}

	@Test(expectedExceptions = RuntimeException.class)
	public void testGetErrorTextBroken() {
		HttpURLConnectionSpy urlConnection = new HttpURLConnectionPartlyErrorSpy(url);
		HttpMultiPartUploader httpHandler = HttpMultiPartUploaderImp
				.usingURLConnection(urlConnection);

		assertEquals(httpHandler.getErrorText(), "some text");
	}
}
