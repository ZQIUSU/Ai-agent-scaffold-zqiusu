package site.zqiusu.test.trigger;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import site.zqiusu.domain.agent.service.IChatService;
import site.zqiusu.trigger.http.AgentServiceController;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** 仅验证 HTTP 契约，不启动模型、数据库或 MCP 服务。 */
public class AgentServiceControllerTest {

    private IChatService chatService;
    private MockMvc mvc;
    private AnnotationConfigWebApplicationContext context;

    @Before
    public void setUp() {
        context = new AnnotationConfigWebApplicationContext();
        context.setServletContext(new MockServletContext());
        context.register(WebConfig.class);
        context.refresh();
        chatService = context.getBean(IChatService.class);
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @After
    public void tearDown() {
        context.close();
    }

    @Configuration
    @EnableWebMvc
    public static class WebConfig {
        @Bean
        public IChatService chatService() {
            return mock(IChatService.class);
        }

        @Bean
        public AgentServiceController controller(IChatService chatService) {
            AgentServiceController controller = new AgentServiceController();
            ReflectionTestUtils.setField(controller, "chatService", chatService);
            return controller;
        }
    }

    @Test
    public void createsSessionUsingBrowserCompatiblePost() throws Exception {
        when(chatService.createSession("100003", "admin")).thenReturn("session-123");
        mvc.perform(post("/api/v1/create_session")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"agentId\":\"100003\",\"userId\":\"admin\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("0000"))
                .andExpect(jsonPath("$.data.sessionId").value("session-123"));
        verify(chatService).createSession("100003", "admin");
    }

    @Test
    public void preservesExistingGetSessionRequests() throws Exception {
        when(chatService.createSession("100003", "admin")).thenReturn("session-legacy");
        mvc.perform(get("/api/v1/create_session")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"agentId\":\"100003\",\"userId\":\"admin\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sessionId").value("session-legacy"));
    }

    @Test
    public void acceptsCrossOriginSessionCreation() throws Exception {
        mvc.perform(options("/api/v1/create_session")
                        .header("Origin", "http://127.0.0.1:18765")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "*"));
    }

    @Test
    public void bindsAllChatFormParameters() throws Exception {
        when(chatService.handleMessage("100003", "admin", "session-123", "你好"))
                .thenReturn(List.of("第一段", "第二段"));
        mvc.perform(post("/api/v1/chat")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("agentId", "100003").param("userId", "admin")
                        .param("sessionId", "session-123").param("message", "你好"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("0000"))
                .andExpect(jsonPath("$.data.content").value("第一段\n第二段"));
        verify(chatService).handleMessage("100003", "admin", "session-123", "你好");
        verify(chatService, never()).createSession(anyString(), anyString());
    }

    @Test
    public void usesCreatedSessionWhenChatOmitsSessionId() throws Exception {
        when(chatService.createSession("100003", "admin")).thenReturn("session-auto");
        when(chatService.handleMessage("100003", "admin", "session-auto", "你好"))
                .thenReturn(List.of("你好，admin"));
        mvc.perform(post("/api/v1/chat").param("agentId", "100003")
                        .param("userId", "admin").param("message", "你好"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").value("你好，admin"));
        verify(chatService).handleMessage("100003", "admin", "session-auto", "你好");
    }
}
