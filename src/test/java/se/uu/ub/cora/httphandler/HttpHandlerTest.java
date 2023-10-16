/*
 * Copyright 2016, 2019, 2023 Uppsala University Library
 * Copyright 2023 Olov McKie
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
import static org.testng.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
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

public class HttpHandlerTest {

	private static final String PUBLISHER_NO_BODY = "jdk.internal.net.http.RequestPublishers$EmptyPublisher";
	private static final String PUBLISHER_STRING = "jdk.internal.net.http.RequestPublishers$StringPublisher";
	private static final String PUBLISHER_INPUTSTREAM = "jdk.internal.net.http.RequestPublishers$InputStreamPublisher";
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
	public void testDefaultMethodIsGET() {
		String responseText = httpHandler.getResponseText();

		assertRequestMethodHasBeenSetInBuilder("GET");
		assertCreatedBodyPublisherIs(PUBLISHER_NO_BODY);
		HttpResponseSpy<?> responseSpy = assertSendOnHttpClientReturnResponseSpy();

		InputStreamSpy inStreamSpy = (InputStreamSpy) responseSpy.MCR.getReturnValue("body", 0);
		byte[] bytes = (byte[]) inStreamSpy.MCR.getReturnValue("readAllBytes", 0);
		assertEquals(responseText.getBytes(), bytes);
	}

	@Test
	public void testSetRequestMethodAndGetResponseText() {
		httpHandler.setRequestMethod("GET");
		String responseText = httpHandler.getResponseText();

		assertRequestMethodHasBeenSetInBuilder("GET");
		assertCreatedBodyPublisherIs(PUBLISHER_NO_BODY);
		HttpResponseSpy<?> responseSpy = assertSendOnHttpClientReturnResponseSpy();

		InputStreamSpy inStreamSpy = (InputStreamSpy) responseSpy.MCR.getReturnValue("body", 0);
		byte[] bytes = (byte[]) inStreamSpy.MCR.getReturnValue("readAllBytes", 0);
		assertEquals(responseText.getBytes(), bytes);
	}

	@Test
	public void testSetRequestMethodAndGetErrorText() {
		HttpResponseSpy<String> errorResponseSpy = createSetAndReturnStringResponseSpyInHttpClientSpy();
		errorResponseSpy.MRV.setDefaultReturnValuesSupplier("statusCode", () -> 403);

		httpHandler.setRequestMethod("GET");
		String errorText = httpHandler.getErrorText();
		int responseCode = httpHandler.getResponseCode();

		assertRequestMethodHasBeenSetInBuilder("GET");
		assertCreatedBodyPublisherIs(PUBLISHER_NO_BODY);
		HttpResponseSpy<?> responseSpy = assertSendOnHttpClientReturnResponseSpy();

		InputStreamSpy inStreamSpy = (InputStreamSpy) responseSpy.MCR.getReturnValue("body", 0);
		byte[] bytes = (byte[]) inStreamSpy.MCR.getReturnValue("readAllBytes", 0);
		assertEquals(errorText.getBytes(), bytes);

		responseSpy.MCR.assertReturn("statusCode", 0, responseCode);
		assertEquals(responseCode, 403);
	}

	private HttpResponseSpy<String> createSetAndReturnStringResponseSpyInHttpClientSpy() {
		HttpResponseSpy<String> responseSpy = new HttpResponseSpy<>();
		responseSpy.MRV.setDefaultReturnValuesSupplier("body", InputStreamSpy::new);
		httpClientSpy.MRV.setDefaultReturnValuesSupplier("send", () -> responseSpy);
		return responseSpy;
	}

	@Test
	public void testSetRequestMethodAndGetResponseBinary() {
		httpHandler.setRequestMethod("GET");
		InputStream responseBinary = httpHandler.getResponseBinary();

		assertRequestMethodHasBeenSetInBuilder("GET");
		assertCreatedBodyPublisherIs(PUBLISHER_NO_BODY);
		HttpResponseSpy<?> responseSpy = assertSendOnHttpClientReturnResponseSpy();

		responseSpy.MCR.assertReturn("body", 0, responseBinary);
	}

	@Test
	public void testSetRequestMethodAndGetResponseBinaryThrowsException() {
		RuntimeException sendException = new RuntimeException("someMessage");
		httpClientSpy.MRV.setAlwaysThrowException("send", sendException);

		httpHandler.setRequestMethod("GET");
		try {
			httpHandler.getResponseBinary();
			fail("Exception should have been thrown");
		} catch (Exception e) {
			assertTrue(e instanceof RuntimeException);
			assertEquals(e.getMessage(), "Error getting response binary: ");
		}
	}

	@Test
	public void testSetRequestMethodAndSetStreamOutput() {
		String inputStreamString = "some inputStream";
		InputStream inputStream = new ByteArrayInputStream(inputStreamString.getBytes());

		httpHandler.setRequestMethod("PUT");
		httpHandler.setStreamOutput(inputStream);

		int responseCode = httpHandler.getResponseCode();

		assertRequestMethodHasBeenSetInBuilder("PUT");
		assertCreatedBodyPublisherIs(PUBLISHER_INPUTSTREAM);
		HttpResponseSpy<?> responseSpy = assertSendOnHttpClientReturnResponseSpy();

		responseSpy.MCR.assertReturn("statusCode", 0, responseCode);
	}

	@Test
	public void testSetRequestMethodAndSetStreamOutputThrowsException() {
		RuntimeException sendException = new RuntimeException("someMessage");
		httpClientSpy.MRV.setAlwaysThrowException("send", sendException);

		String inputStreamString = "some inputStream";
		InputStream inputStream = new ByteArrayInputStream(inputStreamString.getBytes());

		httpHandler.setRequestMethod("PUT");
		try {
			httpHandler.setStreamOutput(inputStream);
			fail();
		} catch (Exception e) {
			assertTrue(e instanceof RuntimeException);
			assertEquals(e.getMessage(), "Error writing output from stream: ");
		}
	}

	@Test
	public void testSetRequestMethodAndSetOutput() {
		httpHandler.setRequestMethod("PUT");
		httpHandler.setOutput("hejsan");
		int responseCode = httpHandler.getResponseCode();

		assertRequestMethodHasBeenSetInBuilder("PUT");
		assertCreatedBodyPublisherIs(PUBLISHER_STRING);
		HttpResponseSpy<?> responseSpy = assertSendOnHttpClientReturnResponseSpy();

		responseSpy.MCR.assertReturn("statusCode", 0, responseCode);
	}

	@Test
	public void testSetRequestMethodAndSetOutputThrowsException() {
		RuntimeException sendException = new RuntimeException("someMessage");
		httpClientSpy.MRV.setAlwaysThrowException("send", sendException);

		httpHandler.setRequestMethod("PUT");
		try {
			httpHandler.setOutput("hejsan");
			fail("Exception should have been thrown");

		} catch (Exception e) {
			assertTrue(e instanceof RuntimeException);
			assertEquals(e.getMessage(), "Error setting output: ");
		}
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
	public void testSetRequestMethodAndGetHeader() {
		HttpResponseSpy<InputStream> inputStreamresponseSpy = new HttpResponseSpy<>();
		inputStreamresponseSpy.MRV.setDefaultReturnValuesSupplier("body", InputStreamSpy::new);
		httpClientSpy.MRV.setDefaultReturnValuesSupplier("send", () -> inputStreamresponseSpy);
		setUpResponseWithHeaderUsingNameAndValue(inputStreamresponseSpy, "someHeader",
				"someHeaderValue");
		httpHandler.setRequestMethod("GET");

		httpHandler.getResponseCode();
		String headerValue = httpHandler.getHeaderField("someHeader");
		assertEquals(headerValue, headerValue);

	}

	private void setUpResponseWithHeaderUsingNameAndValue(
			HttpResponseSpy<InputStream> inputStreamresponseSpy, String headerName,
			String headerValue) {
		Builder newBuilder = HttpRequest.newBuilder();
		newBuilder.uri(URI.create(url.toString()));
		HttpHeaders headers = newBuilder.header(headerName, headerValue).build().headers();
		inputStreamresponseSpy.MRV.setDefaultReturnValuesSupplier("headers", () -> headers);
	}

	@Test
	public void testSetBasicAuthorization() {
		String username = "someUserId";
		String password = "somePassword";
		httpHandler.setBasicAuthorization(username, password);
		assertBasicAuthIsSet(username, password);
	}

	private void assertBasicAuthIsSet(String username, String password) {
		String encoded = Base64.getEncoder()
				.encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
		builderSpy.MCR.assertParameters("setHeader", 0, "Authorization", "Basic " + encoded);
	}

	@Test
	public void testSetRequestProperty() {
		httpHandler.setRequestProperty("someKey", "someValue");
		assertPropertyIsSet("someKey", "someValue");
	}

	private void assertPropertyIsSet(String key, String value) {
		builderSpy.MCR.assertParameters("setHeader", 0, key, value);
	}

	@Test
	public void testSetRequestPropertyWithNullValuesShouldNotBeSet() {
		httpHandler.setRequestProperty("someKey", null);
		assertNoPropertyIsSet();
		httpHandler.setRequestProperty(null, "someValue");
		assertNoPropertyIsSet();
	}

	private void assertNoPropertyIsSet() {
		builderSpy.MCR.assertMethodNotCalled("setHeader");
	}

	@Test
	public void testSetRequestMethodAndGetResponseCode() {
		httpHandler.setRequestMethod("GET");
		int responseCode = httpHandler.getResponseCode();

		assertRequestMethodHasBeenSetInBuilder("GET");
		assertCreatedBodyPublisherIs(PUBLISHER_NO_BODY);
		HttpResponseSpy<?> responseSpy = assertSendOnHttpClientReturnResponseSpy();

		responseSpy.MCR.assertReturn("statusCode", 0, responseCode);
	}

	@Test
	public void testSetRequestMethodAndGetResponseCodeShouldBe500OnError() {
		RuntimeException sendException = new RuntimeException("someMessage");
		httpClientSpy.MRV.setAlwaysThrowException("send", sendException);

		httpHandler.setRequestMethod("GET");

		assertEquals(httpHandler.getResponseCode(), 500);
	}

	private HttpResponseSpy<?> assertSendOnHttpClientReturnResponseSpy() {
		BuilderSpy builder2Spy = (BuilderSpy) builderSpy.MCR.getReturnValue("method", 0);
		HttpRequestSpy httpRequestSpy = (HttpRequestSpy) builder2Spy.MCR.getReturnValue("build", 0);
		httpClientSpy.MCR.assertParameter("send", 0, "request", httpRequestSpy);

		return (HttpResponseSpy<?>) httpClientSpy.MCR.getReturnValue("send", 0);
	}

	private void assertCreatedBodyPublisherIs(String publisherType) {
		var bodyPublisher = builderSpy.MCR
				.getValueForMethodNameAndCallNumberAndParameterName("method", 0, "bodyPublisher");
		assertEquals(bodyPublisher.getClass().getName(), publisherType);
	}

	private void assertRequestMethodHasBeenSetInBuilder(String requestMethod) {
		builderSpy.MCR.assertParameter("method", 0, "method", requestMethod);
	}
}
