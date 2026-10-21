package site.zqiusu.test.domain.agent;

import com.google.adk.agents.BaseAgent;
import com.google.adk.events.Event;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.sessions.Session;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;
import site.zqiusu.domain.agent.model.valobj.AiAgentRegisterVO;
import site.zqiusu.domain.agent.service.armory.factory.DefaultArmoryFactory;
import site.zqiusu.domain.agent.service.chat.ChatService;

import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/** 使用真实 ADK 内存会话验证生命周期，不调用模型或启动外部服务。 */
public class ChatSessionLifecycleTest {

    private static final String APP_NAME = "session-lifecycle-test";
    private ChatService chatService;
    private InMemoryRunner runner;

    @Before
    public void setUp() {
        runner = new InMemoryRunner(mock(BaseAgent.class), APP_NAME);
        DefaultArmoryFactory factory = mock(DefaultArmoryFactory.class);
        when(factory.getAiAgentRegisterVO("100003")).thenReturn(AiAgentRegisterVO.builder()
                .appName(APP_NAME).agentId("100003").runner(runner).build());
        when(factory.getAiAgentRegisterVO("100004")).thenReturn(AiAgentRegisterVO.builder()
                .appName(APP_NAME).agentId("100004").runner(runner).build());
        chatService = new ChatService();
        ReflectionTestUtils.setField(chatService, "defaultArmoryFactory", factory);
    }

    @Test
    public void reusesSessionForTheSameUserAndAgent() {
        String first = chatService.createSession("100003", "admin");
        String second = chatService.createSession("100003", "admin");

        assertEquals(first, second);
        assertEquals("admin", session(first).userId());
        assertEquals("admin", session(second).userId());
        assertTrue(session(first).events().isEmpty());
        assertTrue(session(second).events().isEmpty());
    }

    @Test
    public void preservesContextWhenTheSameSessionIsRequestedAgain() {
        String first = chatService.createSession("100003", "admin");
        Event event = Event.builder().author("user")
                .content(Content.fromParts(Part.fromText("请记住：我的项目叫星河。"))).build();
        runner.sessionService().appendEvent(session(first), event).blockingGet();

        String second = chatService.createSession("100003", "admin");

        assertEquals(1, session(first).events().size());
        assertEquals("请记住：我的项目叫星河。", session(first).events().get(0).stringifyContent());
        assertEquals(first, second);
        assertEquals(1, session(second).events().size());
    }

    @Test
    public void isolatesSessionsForDifferentUsers() {
        String adminSession = chatService.createSession("100003", "admin");
        String otherSession = chatService.createSession("100003", "other-user");

        assertNotEquals(adminSession, otherSession);
        Session other = runner.sessionService().getSession(APP_NAME, "other-user", otherSession, Optional.empty()).blockingGet();
        assertEquals("other-user", other.userId());
    }

    @Test
    public void isolatesSessionsForDifferentAgents() {
        String firstAgent = chatService.createSession("100003", "admin");
        String secondAgent = chatService.createSession("100004", "admin");

        assertNotEquals(firstAgent, secondAgent);
        assertEquals(firstAgent, chatService.createSession("100003", "admin"));
        assertEquals(secondAgent, chatService.createSession("100004", "admin"));
    }

    private Session session(String sessionId) {
        return runner.sessionService().getSession(APP_NAME, "admin", sessionId, Optional.empty()).blockingGet();
    }
}
