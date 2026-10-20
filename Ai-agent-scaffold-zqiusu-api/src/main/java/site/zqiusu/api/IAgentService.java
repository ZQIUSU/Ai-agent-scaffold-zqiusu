package site.zqiusu.api;

import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import site.zqiusu.api.dto.*;
import site.zqiusu.api.response.Response;

import java.util.List;

public interface IAgentService {

    Response<List<AiAgentConfigResponseDTO>> queryAiAgentConfigList();

    Response<CreateSessionResponseDTO> createSession(CreateSessionRequestDTO createSessionRequestDTO);

    Response<ChatResponseDTO> chat(ChatRequestDTO chatRequestDTO);

    ResponseBodyEmitter chatStream(ChatRequestDTO chatRequestDTO);
}
