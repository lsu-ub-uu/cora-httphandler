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

import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class HttpHandlerFactoryTest {
	private HttpHandlerFactory factory;

	@BeforeMethod
	public void setUp() {
		factory = new HttpHandlerFactoryImp();
	}

	@Test
	public void testFactor() {
		String url = "http://google.se";
		HttpHandler httpHandler = factory.factor(url);
		assertTrue(httpHandler instanceof HttpHandlerImp);
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
}
