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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.http.HttpResponse.BodyHandler;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.httphandler.internal.HttpHandlerImp;
import se.uu.ub.cora.httphandler.spy.BuilderSpy;
import se.uu.ub.cora.httphandler.spy.HttpClientSpy;
import se.uu.ub.cora.httphandler.spy.HttpRequestSpy;
import se.uu.ub.cora.httphandler.spy.HttpResponseSpy;
import se.uu.ub.cora.httphandler.spy.InputStreamSpy;
import se.uu.ub.cora.httphandler.urlconnection.HttpURLConnectionErrorSpy;
import se.uu.ub.cora.httphandler.urlconnection.HttpURLConnectionSpy;

public class HttpHandlerTest {

	private static final String PUBLISHER_NO_BODY = "jdk.internal.net.http.RequestPublishers$EmptyPublisher";
	private URL url;
	private BuilderSpy builderSpy;
	private HttpClientSpy httpClientSpy;
	private HttpHandler httpHandler;

	@BeforeMethod
	public void setUp() throws MalformedURLException {
		url = new URL("http://google.se");
		builderSpy = new BuilderSpy();
		httpClientSpy = new HttpClientSpy();
		httpHandler = HttpHandlerImp.usingBuilderAndHttpClient(builderSpy, httpClientSpy);
	}

	@Test
	public void testSetRequestMethodAndGetResponseText() {
		httpHandler.setRequestMethod("GET");
		String responseText = httpHandler.getResponseText();

		assertRequestMethodHasBeenSetInBuilder("GET");
		assertCreatedBodyPublisherIs(PUBLISHER_NO_BODY);
		HttpResponseSpy<String> responseSpy = assertSendOnHttpClientReturnResponseSpy();

		responseSpy.MCR.assertReturn("body", 0, responseText);
	}

	@Test
	public void testSetRequestMethodBadRequestMethodThrowsRuntimeException() {
		RuntimeException sendException = new RuntimeException("someMessage");
		httpClientSpy.MRV.setAlwaysThrowException("send", sendException);

		try {
			httpHandler.setRequestMethod("UNKNOWN_REQUEST_METHOD");
			fail("Exception should have been thrown");
		} catch (Exception e) {
			assertTrue(e instanceof RuntimeException);
			assertEquals(e.getMessage(), "Not an ok requestMethod: UNKNOWN_REQUEST_METHOD");
		}
	}

	@Test
	public void testSetRequestMethodAndGetResponseTextThrowsRuntimeExceptionOnError() {
		RuntimeException sendException = new RuntimeException("someMessage");
		httpClientSpy.MRV.setAlwaysThrowException("send", sendException);

		httpHandler.setRequestMethod("GET");
		try {
			httpHandler.getResponseText();
			fail("Exception should have been thrown");
		} catch (Exception e) {
			assertTrue(e instanceof RuntimeException);
			assertEquals(e.getCause(), sendException);
			assertEquals(e.getMessage(), "Error getting response text: ");
		}
	}

	@Test
	public void testSetRequestMethodAndGetResponseCode() {
		httpHandler.setRequestMethod("GET");
		int responseCode = httpHandler.getResponseCode();

		assertRequestMethodHasBeenSetInBuilder("GET");
		assertCreatedBodyPublisherIs(PUBLISHER_NO_BODY);
		HttpResponseSpy<String> responseSpy = assertSendOnHttpClientReturnResponseSpy();

		responseSpy.MCR.assertReturn("statusCode", 0, responseCode);
	}

	@Test
	public void testSetRequestMethodAndGetResponseCodeShouldBe500OnError() {
		RuntimeException sendException = new RuntimeException("someMessage");
		httpClientSpy.MRV.setAlwaysThrowException("send", sendException);

		httpHandler.setRequestMethod("GET");

		assertEquals(httpHandler.getResponseCode(), 500);
	}

	private HttpResponseSpy<String> assertSendOnHttpClientReturnResponseSpy() {
		BuilderSpy builder2Spy = (BuilderSpy) builderSpy.MCR.getReturnValue("method", 0);
		HttpRequestSpy httpRequestSpy = (HttpRequestSpy) builder2Spy.MCR.getReturnValue("build", 0);
		httpClientSpy.MCR.assertParameter("send", 0, "request", httpRequestSpy);

		// will not work if not bodyHandler of string
		BodyHandler<String> bodyHandler = (BodyHandler<String>) httpClientSpy.MCR
				.getValueForMethodNameAndCallNumberAndParameterName("send", 0,
						"responseBodyHandler");
		// assertEquals(bodyHandler.getClass().getName(), "");
		// assertEquals(bodyHandler.getClass().getName(), "");

		return (HttpResponseSpy<String>) httpClientSpy.MCR.getReturnValue("send", 0);
	}

	private void assertCreatedBodyPublisherIs(String publisherType) {
		var bodyPublisher = builderSpy.MCR
				.getValueForMethodNameAndCallNumberAndParameterName("method", 0, "bodyPublisher");
		assertEquals(bodyPublisher.getClass().getName(), publisherType);
	}

	private void assertRequestMethodHasBeenSetInBuilder(String requestMethod) {
		builderSpy.MCR.assertParameter("method", 0, "method", requestMethod);
	}

	// @Test(expectedExceptions = RuntimeException.class)
	// public void testSetWrongRequestMethod() {
	// HttpURLConnectionSpy urlConnection = new HttpURLConnectionErrorSpy(url);
	// HttpHandler httpHandler = HttpHandlerImp.usingURLConnection(urlConnection);
	// httpHandler.setRequestMethod("NOT_A_REQUEST_METHOD");
	// }

	// @Test
	// public void testGetResponseCode() {
	// HttpURLConnectionSpy urlConnection = new HttpURLConnectionSpy(url);
	// HttpHandler httpHandler = HttpHandlerImp.usingURLConnection(urlConnection);
	//
	// urlConnection.setResponseCode(200);
	// assertEquals(httpHandler.getResponseCode(), 200);
	// }

	@Test
	public void testGetBrokenResponseCode() {
		HttpURLConnectionSpy urlConnection = new HttpURLConnectionErrorSpy(url);
		HttpHandler httpHandler = HttpHandlerImp.usingURLConnection(urlConnection);
		assertEquals(httpHandler.getResponseCode(), 500);
	}

	// @Test
	// public void testGetResponseText() {
	// HttpURLConnectionSpy urlConnection = new HttpURLConnectionSpy(url);
	// HttpHandler httpHandler = HttpHandlerImp.usingURLConnection(urlConnection);
	//
	// urlConnection.setResponseText("some text åäö");
	// assertEquals(httpHandler.getResponseText(), "some text åäö");
	// }

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
		String str = "some text åäö";
		httpHandler.setOutput(str);
		urlConnection.MCR.assertParameters("setDoOutput", 0, true);
		assertEquals(urlConnection.getOutputStreamAsString(), str);
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
		InputStreamSpy stream = new InputStreamSpy();
		HttpHandler httpHandler = HttpHandlerImp.usingURLConnection(urlConnection);

		httpHandler.setStreamOutput(stream);

		urlConnection.MCR.assertParameters("setDoOutput", 0, true);
		urlConnection.MCR.assertParameters("setChunkedStreamingMode", 0, 8192);
		stream.MCR.assertParameter("transferTo", 0, "out",
				urlConnection.MCR.getReturnValue("getOutputStream", 0));
	}

	@Test(expectedExceptions = RuntimeException.class)
	public void testSetStreamOutputBroken_ClosesStream() {
		HttpURLConnectionSpy urlConnection = new HttpURLConnectionSpy(url);
		RuntimeException error = new RuntimeException("someError");
		urlConnection.MRV.setAlwaysThrowException("getOutputStream", error);
		InputStreamSpy stream = new InputStreamSpy();
		HttpHandler httpHandler = HttpHandlerImp.usingURLConnection(urlConnection);

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

	@Test
	public void testSetBasicAuthorization() {
		HttpURLConnectionSpy urlConnection = new HttpURLConnectionSpy(url);
		HttpHandler httpHandler = HttpHandlerImp.usingURLConnection(urlConnection);

		String username = "someUserId";
		String password = "somePassword";
		httpHandler.setBasicAuthorization(username, password);

		String encoded = Base64.getEncoder()
				.encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
		assertEquals(urlConnection.requestProperties.get("Authorization"), "Basic " + encoded);

	}

	@Test
	public void testGetResponseBinary() {
		HttpURLConnectionSpy urlConnection = new HttpURLConnectionSpy(url);
		HttpHandler httpHandler = HttpHandlerImp.usingURLConnection(urlConnection);

		urlConnection.setResponseText("some text åäö");
		InputStream responseBinary = httpHandler.getResponseBinary();
		assertNotNull(responseBinary);
		assertSame(responseBinary, urlConnection.returnedInputStream);
	}

}
