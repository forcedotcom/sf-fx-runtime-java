package com.salesforce.function.runtime;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.salesforce.function.context.Context;
import com.salesforce.function.context.UserContext;
import com.salesforce.function.context.impl.ContextImpl;
import com.salesforce.function.context.impl.UserContextImpl;
import com.salesforce.function.request.Request;
import com.salesforce.function.request.impl.RequestImpl;
import com.unite.TestFunction;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class FunctionControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void getName() throws Exception {
        TestFunction fx = new TestFunction();
        assertEquals(fx.getName(), TestFunction.class.getSimpleName());
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
        mvc.perform(MockMvcRequestBuilders.post("/invoke")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(content().string(equalTo("{\"result\":\"Function invoked!\"}")));
    }
}
