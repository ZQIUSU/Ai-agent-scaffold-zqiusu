package site.zqiusu.domain.agent.service.chat;

import com.google.adk.events.Event;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.sessions.Session;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import io.reactivex.rxjava3.core.Flowable;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import site.zqiusu.domain.agent.model.entity.ChatCommandEntity;
import site.zqiusu.domain.agent.model.valobj.AiAgentConfigTableVO;
import site.zqiusu.domain.agent.model.valobj.AiAgentRegisterVO;
import site.zqiusu.domain.agent.model.valobj.properties.AiAgentAutoConfigProperties;
import site.zqiusu.domain.agent.service.IChatService;
import site.zqiusu.domain.agent.service.armory.factory.DefaultArmoryFactory;
import site.zqiusu.types.enums.ResponseCode;
import site.zqiusu.types.exception.AppException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class ChatService implements IChatService {

    @Resource
    private DefaultArmoryFactory defaultArmoryFactory;

    @Resource
    private AiAgentAutoConfigProperties aiAgentAutoConfigProperties;

    private final Map<SessionKey, String> userSessions = new ConcurrentHashMap<>();

    private record SessionKey(String agentId, String userId) {}

    @Override
    public List<AiAgentConfigTableVO.Agent> queryAiAgentConfigList() {
        Map<String, AiAgentConfigTableVO> tables = aiAgentAutoConfigProperties.getTables();

        List<AiAgentConfigTableVO.Agent> agentList = new ArrayList<>();
        if(null != tables){
            for (AiAgentConfigTableVO vo: tables.values()){
                if (null != vo.getAgent()) {
                    agentList.add(vo.getAgent());
                }
            }
        }

        return agentList;
    }

    @Override
    public String createSession(String agentId, String userId) {
        AiAgentRegisterVO aiAgentRegisterVO = defaultArmoryFactory.getAiAgentRegisterVO(agentId);

        if(null == aiAgentRegisterVO){
            throw new AppException(ResponseCode.NOT_FOUNT.getCode());
        }

        String appName = aiAgentRegisterVO.getAppName();
        InMemoryRunner runner = aiAgentRegisterVO.getRunner();

        return userSessions.computeIfAbsent(new SessionKey(agentId, userId), key ->{
            Session session = runner.sessionService().createSession(appName, key.userId()).blockingGet();
            return session.id();
        });

    }

    @Override
    public List<String> handleMessage(String agentId, String userId, String message) {
        AiAgentRegisterVO aiAgentRegisterVO = defaultArmoryFactory.getAiAgentRegisterVO(agentId);

        if(null == aiAgentRegisterVO){
            throw new AppException(ResponseCode.NOT_FOUNT.getCode());
        }

        String sessionId = createSession(agentId, userId);

        return handleMessage(agentId, userId, sessionId, message);
    }

    @Override
    public List<String> handleMessage(String agentId, String userId, String sessionId, String message) {
        AiAgentRegisterVO aiAgentRegisterVO = defaultArmoryFactory.getAiAgentRegisterVO(agentId);

        if(null == aiAgentRegisterVO){
            throw new AppException(ResponseCode.NOT_FOUNT.getCode());
        }

        InMemoryRunner runner = aiAgentRegisterVO.getRunner();

        Content userMsg = Content.fromParts(Part.fromText(message));
        Flowable<Event> events = runner.runAsync(userId, sessionId, userMsg);

        List<String> outputs = new ArrayList<>();
        events.blockingForEach(event -> outputs.add(event.stringifyContent()));

        return outputs;
    }

    @Override
    public List<String> handleMessage(ChatCommandEntity chatCommandEntity) {
        AiAgentRegisterVO aiAgentRegisterVO = defaultArmoryFactory.getAiAgentRegisterVO(chatCommandEntity.getAgentId());

        if(null == aiAgentRegisterVO){
            throw new AppException(ResponseCode.NOT_FOUNT.getCode());
        }

        List<Part> parts = new ArrayList<>();

        List<ChatCommandEntity.Content.Text> texts = chatCommandEntity.getTexts();
        if (null != texts && !texts.isEmpty()) {
            for(ChatCommandEntity.Content.Text text: texts ){
                parts.add(Part.fromText(text.getMessage()));
            }
        }

        List<ChatCommandEntity.Content.File> files = chatCommandEntity.getFiles();
        if (null != files && !files.isEmpty()) {
            for(ChatCommandEntity.Content.File file: files ){
                parts.add(Part.fromUri(file.getFileUri(),file.getMimeType()));
            }
        }

        List<ChatCommandEntity.Content.InlineData> datas = chatCommandEntity.getInlineDatas();
        if (null != datas && !datas.isEmpty()) {
            for(ChatCommandEntity.Content.InlineData data: datas ){
                parts.add(Part.fromBytes(data.getBytes(),data.getMimeType()));
            }
        }

        Content userMsg = Content.builder()
                .role("user")
                .parts(parts)
                .build();

        InMemoryRunner runner = aiAgentRegisterVO.getRunner();
        Flowable<Event> events = runner.runAsync(chatCommandEntity.getUserId(), chatCommandEntity.getSessionId(), userMsg);

        List<String> outputs = new ArrayList<>();
        events.blockingForEach(event -> outputs.add(event.stringifyContent()));

        return outputs;
    }

    @Override
    public Flowable<Event> handleMessageStream(String agentId, String userId, String sessionId, String message) {
        AiAgentRegisterVO aiAgentRegisterVO = defaultArmoryFactory.getAiAgentRegisterVO(agentId);

        if(null == aiAgentRegisterVO){
            throw new AppException(ResponseCode.NOT_FOUNT.getCode());
        }

        InMemoryRunner runner = aiAgentRegisterVO.getRunner();

        Content userMsg = Content.fromParts(Part.fromText(message));
        return runner.runAsync(userId, sessionId, userMsg);
    }
}
