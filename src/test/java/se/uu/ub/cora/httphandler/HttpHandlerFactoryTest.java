/*
 * Copyright 2016, 2018, 2023 Uppsala University Library
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
import static org.testng.Assert.assertTrue;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.httphandler.internal.HttpHandlerImp;
import se.uu.ub.cora.httphandler.internal.HttpMultiPartUploaderImp;

public class HttpHandlerFactoryTest {
	private HttpHandlerFactory factory;
	private String url;

	@BeforeMethod
	public void setUp() {
		url = "http://google.se";
		factory = new HttpHandlerFactoryImp();
	}

	@Test
	public void testFactor() {
		HttpHandler httpHandler = factory.factor(url);
		assertTrue(httpHandler instanceof HttpHandlerImp);
	}

	@Test
	public void testFactorHttpHandler() throws Exception {
		HttpHandlerImp httpHandler = (HttpHandlerImp) factory.factor(url);

		assertHttpFactoring(httpHandler);
	}

	private void assertHttpFactoring(HttpHandlerImp httpHandler) {
		Builder httpRequestBuilder = httpHandler.onlyForTestGetBuilder();
		HttpRequest httpRequest = httpRequestBuilder.build();
		assertEquals(httpRequest.uri().toString(), url);

		HttpClient httpClient = httpHandler.onlyForTestGetHttpClient();
		assertNotNull(httpClient);
	}

	@Test(expectedExceptions = RuntimeException.class)
	public void testBrokenFactor() {
		String url = "notAnValidProtocol://google.se";
		factory.factor(url);
	}

	@Test
	public void testFactorHttpMultiPartUploader() {
		HttpMultiPartUploader factored = factory.factorHttpMultiPartUploader("http://www.uu.se");
		assertTrue(factored instanceof HttpMultiPartUploaderImp);
	}

	@Test(expectedExceptions = RuntimeException.class)
	public void testFactorHttpMultiPartUploaderNonExistingClassName() {
		factory.factorHttpMultiPartUploader("/()&/()%&");
	}

	@Test
	public void testFactorUsingHttpVersion_1_1() throws Exception {
		HttpHandlerImp httpHandler = (HttpHandlerImp) factory.factorUsingHttpVersion_1_1(url);
		assertTrue(httpHandler instanceof HttpHandlerImp);

		Builder httpRequestBuilder = httpHandler.onlyForTestGetBuilder();
		HttpRequest httpRequest = httpRequestBuilder.build();
		assertEquals(httpRequest.uri().toString(), url);
		assertEquals(httpRequest.version().get().toString(), "HTTP_1_1");

		HttpClient httpClient = httpHandler.onlyForTestGetHttpClient();
		assertNotNull(httpClient);
	}
}
