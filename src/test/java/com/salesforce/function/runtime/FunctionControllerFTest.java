package com.salesforce.function.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.URL;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import com.salesforce.function.context.Context;
import com.salesforce.function.context.UserContext;
import com.salesforce.function.context.impl.ContextImpl;
import com.salesforce.function.context.impl.UserContextImpl;
import com.salesforce.function.request.Request;
import com.salesforce.function.request.Response;
import com.salesforce.function.request.impl.RequestImpl;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FunctionControllerFTest {

	@LocalServerPort
	private int port;

	private URL base;

	@Autowired
	private TestRestTemplate template;

	@Before
	public void setUp() throws Exception {
		this.base = new URL("http://localhost:" + port);
	}

	@Test
	public void invoke() throws Exception {
		final String username = "admin@salesforce.com";
		final String orgId = "00D000000000000AAA";
		UserContext userContext = new UserContextImpl(orgId, "005000000000000AAA", username,
				"http://cwall-wsl1.localhost.internal.salesforce.com:6109",
				"http://platform-energy-5763-dev-ed.localhost.internal.salesforce.com:6109");
		Context context = new ContextImpl(userContext);
		Request request = new RequestImpl(context, null);

		String endpoint = "/invoke";
		String url = base.toString() + endpoint;

		HttpHeaders headers = new HttpHeaders();
		HttpEntity<Request> entity = new HttpEntity<Request>(request, headers);
		ResponseEntity<Response> response = template.postForEntity(url, entity, Response.class);
		Response responseObj = response.getBody();
		assertNotNull(responseObj);
		assertNotNull(responseObj.getResult());
		assertEquals(responseObj.getResult(), "Function invoked!");
	}
}
