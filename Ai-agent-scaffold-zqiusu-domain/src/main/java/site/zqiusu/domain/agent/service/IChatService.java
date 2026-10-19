package site.zqiusu.domain.agent.service;

import com.google.adk.events.Event;
import io.reactivex.rxjava3.core.Flowable;
import site.zqiusu.domain.agent.model.entity.ChatCommandEntity;
import site.zqiusu.domain.agent.model.valobj.AiAgentConfigTableVO;

import java.util.List;

public interface IChatService {

    List<AiAgentConfigTableVO.Agent> queryAiAgentConfigList();

    String createSession(String agentId, String userId);

    List<String> handleMessage(String agentId, String userId,String message);

    List<String> handleMessage(String agentId, String userId,String sessionId, String message);

    List<String> handleMessage(ChatCommandEntity chatCommandEntity);

    Flowable<Event> handleMessageStream(String agentId, String userId, String sessionId, String message);

}
